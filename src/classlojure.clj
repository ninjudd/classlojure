(ns classlojure
  (:import [java.net URL URLClassLoader]))

(def ext-classloader
  (.getParent (.getClassLoader clojure.lang.RT)))

(defn classlojure [& urls]
  (URLClassLoader.
   (into-array URL (map #(URL. %) urls))
   ext-classloader))

(defmacro with-classloader [cl & body]
  `(binding [*use-context-classloader* true]
     (let [cl# (.getContextClassLoader (Thread/currentThread))]
       (try (.setContextClassLoader (Thread/currentThread) ~cl)
            ~@body
            (finally
             (.setContextClassLoader (Thread/currentThread) cl#))))))

(defn eval-in [cl form]
  (with-classloader cl
    (let [clj       (.loadClass cl "clojure.main")
          signature (into-array Class [(class (into-array String []))])
          method    (.getDeclaredMethod clj "main" signature)
          args      (into-array Object [(into-array String ["-e" (pr-str form)])])]
      (.invoke method clj args))))