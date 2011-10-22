(ns classlojure.io
  (:use [classlojure.core :only [base-classloader]]
        [clojure.java.io :only [copy file]])
  (:import (java.io File FileInputStream)
           (java.net JarURLConnection)))

(defn resource-stream
  ([name]
     (resource-stream (base-classloader) name))
  ([^java.net.URLClassLoader classloader name]
     (if-let [url (.findResource classloader name)]
       (let [conn (.openConnection url)]
         (if (instance? JarURLConnection conn)
           (let [^JarURLConnection jar (cast JarURLConnection conn)]
             (.getInputStream jar))
           (FileInputStream. (File. (.getFile url))))))))

(defn extract-resource
  ([name dest-dir]
     (extract-resource (base-classloader) name dest-dir))
  ([classloader name dest-dir]
     (if-let [s (resource-stream classloader name)]
       (let [dest (file dest-dir name)]
         (.mkdirs (.getParentFile dest))
         (copy s dest)
         dest)
       (throw (Exception. (format "unable to find %s on classpath" name))))))
