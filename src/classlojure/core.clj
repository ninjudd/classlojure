(ns classlojure.core
  (:use [useful.java :only [invoke-private]]
        [clojure.java.io :only [as-url]])
  (:import [java.net URL URLClassLoader]))

(def base-classloader
  (.getClassLoader clojure.lang.RT))

(def ext-classloader
  (.getParent ^ClassLoader base-classloader))

(defn- url-classloader [urls ext]
  (URLClassLoader.
   (into-array URL (map as-url (flatten urls)))
   ext))

(defn wrap-ext-classloader [& urls]
  (alter-var-root #'ext-classloader
    (fn [ext] ;; only permit wrapping once
      (if (= URLClassLoader (class ext))
        ext
        (url-classloader urls ext)))))

(defn get-classpath [^URLClassLoader cl]
  (for [^URL url (.getURLs cl)]
    (let [^String path (.getPath url)]
      (if (.endsWith path "/")
        (.substring path 0 (- (count path) 1))
        path))))

(defmacro with-classloader [cl & body]
  `(binding [*use-context-classloader* true]
     (let [cl# (.getContextClassLoader (Thread/currentThread))]
       (try (.setContextClassLoader (Thread/currentThread) ~cl)
            ~@body
            (finally
             (.setContextClassLoader (Thread/currentThread) cl#))))))

(defn append-classpath! [^URLClassLoader cl & urls]
  (let [existing? (set (map #(.getPath ^URL %) (.getURLs cl)))]
    (doseq [url urls]
      (when-not (existing? url)
        (invoke-private cl "addURL" (URL. url))))))

(defn invoke-in* [^ClassLoader cl class-name method & [signature & params]]
  (let [class     (.loadClass cl class-name)
        signature (into-array Class (or signature []))
        method    (.getDeclaredMethod class method signature)]
    (.invoke method class (into-array Object params))))

(defmacro invoke-in
  "Invoke class-method (with optional signature) inside the given classloader, passing the optional params."
  [cl class-method & args]
  (let [class  (namespace class-method)
        method (name class-method)]
    `(invoke-in* ~cl ~class ~method ~@args)))

(defn printable? [object]
  (or (nil? object)
      (and (class object)
           (.getClassLoader (class object)))))

(defn eval-in* [cl form & objects]
  (let [print-read-eval (fn [form]
                          (->> (pr-str form)
                               (invoke-in cl clojure.lang.RT/readString [String])
                               (invoke-in cl clojure.lang.Compiler/eval [Object])))]
    (with-classloader cl
      (let [result-or-fn (print-read-eval form)
            result (if (seq objects)
                     (-> (class result-or-fn)
                         (.getMethod "invoke"
                                     (into-array (repeat (count objects) Object)))
                         (.invoke result-or-fn (to-array objects)))
                     result-or-fn)]
        (if-not (printable? result)
          result
          (let [string (invoke-in cl clojure.lang.RT/printString [Object] result)]
            (try (read-string string)
                 (catch RuntimeException e
                   string))))))))

(defn eval-in
  "Eval the given form in a separate classloader. If objects are passed after form, then the form
   is assumed to be a function and it is applied to the list of objects. This lets you pass objects
   between classloaders."
  [cl form & objects]
  (apply eval-in* cl
         `(clojure.main/with-bindings (eval '~form))
         objects))

(defn classlojure [& urls]
  (let [^URLClassLoader cl (url-classloader urls ext-classloader)]
    (.loadClass cl "clojure.lang.RT")
    (eval-in* cl '(require 'clojure.main))
    cl))
