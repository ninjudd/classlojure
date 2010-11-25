(ns classlojure
  (:import [java.net URL URLClassLoader]))

(def ext-classloader
  (.getParent (.getClassLoader clojure.lang.RT)))

(defn wrap-ext-classloader [& urls]
  (alter-var-root #'ext-classloader
                  (fn [ext] ;; only permit wrapping once
                    (if (= URLClassLoader (class ext))
                      ext
                      (URLClassLoader.
                       (into-array URL (map #(URL. %) (flatten urls)))
                       ext)))))

(defn classlojure [& urls]
  (let [cl (URLClassLoader.
            (into-array URL (map #(URL. %) (flatten urls)))
            ext-classloader)]
    (try (.loadClass cl "clojure.lang.RT")
         cl
         (catch ClassNotFoundException e))))

(defmacro with-classloader [cl & body]
  `(binding [*use-context-classloader* true]
     (let [cl# (.getContextClassLoader (Thread/currentThread))]
       (try (.setContextClassLoader (Thread/currentThread) ~cl)
            ~@body
            (finally
             (.setContextClassLoader (Thread/currentThread) cl#))))))

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

(defn eval-in
  "Eval the given form in a separate classloader. If objects are passed after form, then the form
   is assumed to be a function and it is applied to the list of objects. This lets you pass objects
   between classloaders."
  [cl form & objects]
  (let [result-string
        (with-classloader cl
          (let [string (if (seq objects)
                         (format "(fn [args] (apply %s args))" (pr-str form))
                         (pr-str form))
                form   (invoke-in cl clojure.lang.RT/readString [String] string)
                result (invoke-in cl clojure.lang.Compiler/eval [Object] form)
                result (if (seq objects)
                         (.invoke result objects)
                         result)]
            (invoke-in cl clojure.lang.RT/printString [Object] result)))]
    (try (read-string result-string)
         (catch RuntimeException e
           result-string))))
