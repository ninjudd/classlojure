(ns classlojure
  (:import classlojure.NativeClassLoader))

(defmacro import-native [& import-symbols-or-lists]
  `(let [cl# (.getContextClassLoader (Thread/currentThread))]
     (try (.setContextClassLoader (Thread/currentThread) (clojure.lang.NativeClassLoader.))
          (binding [*use-context-classloader* true]
            (import ~@import-symbols-or-lists))
          (finally
           (.setContextClassLoader (Thread/currentThread) cl#)))))
