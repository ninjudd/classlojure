(ns classlojure
  (:import classlojure.NativeClassLoader))

(defmacro import-native [& import-symbols-or-lists]
  `(binding [*use-context-classloader* true]
     (let [cl# (.getContextClassLoader (Thread/currentThread))]
       (try
        (.setContextClassLoader (Thread/currentThread) (NativeClassLoader.))
        (import ~@import-symbols-or-lists)
        (finally
         (.setContextClassLoader (Thread/currentThread) cl#))))))

(defn extract-resource [name]
  (let [url #^java.net.URL (.findResource (NativeClassLoader.) name)]
    (NativeClassLoader/extractResource url)))