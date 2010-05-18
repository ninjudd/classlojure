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

(defn extract-resource
  ([name] (extract-resource name nil))
  ([name outdir]
     (let [outdir (when outdir (java.io.File. outdir))]
       (.extractResource (NativeClassLoader.) #^String name #^File outdir))))