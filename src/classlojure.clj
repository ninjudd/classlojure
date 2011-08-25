(ns classlojure
  (:use [useful.java :only [invoke-private]])
  (:import [java.net URL URLClassLoader]))

(def base-classloader
  (.getClassLoader clojure.lang.RT))

(def ext-classloader
  (.getParent base-classloader))

(defn- url-classloader [urls ext]
  (URLClassLoader.
   (into-array URL (map #(URL. %) (flatten urls)))
   ext))

(defn wrap-ext-classloader [& urls]
  (alter-var-root #'ext-classloader
    (fn [ext] ;; only permit wrapping once
      (if (= URLClassLoader (class ext))
        ext
        (url-classloader urls ext)))))

(defn get-classpath [cl]
  (for [url (.getURLs cl)]
    (let [path (.getPath url)]
      (if (.endsWith path "/")
        (.substring path 0 (- (count path) 1))
        path))))

(defn classlojure [& urls]
  (let [cl (url-classloader urls ext-classloader)]
    (.loadClass cl "clojure.lang.RT")
    cl))

(defmacro with-classloader [cl & body]
  `(binding [*use-context-classloader* true]
     (let [cl# (.getContextClassLoader (Thread/currentThread))]
       (try (.setContextClassLoader (Thread/currentThread) ~cl)
            ~@body
            (finally
             (.setContextClassLoader (Thread/currentThread) cl#))))))

(defn append-classpath! [cl & urls]
  (let [existing? (set (map (memfn getPath) (.getURLS cl)))]
    (doseq [url urls]
      (when-not (existing? url)
        (invoke-private cl "addURL" (URL. url))))))

(defn invoke-in* [cl class-name method & [signature & params]]
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

(defn eval-in* [cl form]
  (with-classloader cl
    (let [form (invoke-in cl clojure.lang.RT/readString [String] (pr-str form))]
      (invoke-in cl clojure.lang.Compiler/eval [Object] form))))

(defn eval-in
  "Eval the given form in a separate classloader. If objects are passed after form, then the form
   is assumed to be a function and it is applied to the list of objects. This lets you pass objects
   between classloaders."
  [cl form & objects]
  (let [result (if (seq objects)
                 (-> (eval-in* cl `(fn [~'args] (apply ~form ~'args)))
                     (.invoke objects))
                 (eval-in* cl form))
        string (invoke-in cl clojure.lang.RT/printString [Object] result)]
    (try (read-string string)
         (catch RuntimeException e
           string))))