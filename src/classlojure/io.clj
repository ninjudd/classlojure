(ns classlojure.io
  (:use [classlojure.core :only [base-classloader]]
        [clojure.java.io :only [copy file reader]])
  (:import (java.io File FileInputStream)
           (java.net JarURLConnection)
           (clojure.lang LineNumberingPushbackReader)))

(defn resource-stream
  ([name]
     (resource-stream base-classloader name))
  ([^java.net.URLClassLoader classloader name]
     (if-let [url (.findResource classloader name)]
       (let [conn (.openConnection url)]
         (if (instance? JarURLConnection conn)
           (let [^JarURLConnection jar (cast JarURLConnection conn)]
             (.getInputStream jar))
           (FileInputStream. (File. (.getFile url))))))))

(defn resource-reader
  ([name]
     (resource-reader base-classloader name))
  ([classloader name]
     (LineNumberingPushbackReader. (reader (resource-stream classloader name)))))

;;; Taken from useful
(defn read-seq
  "Read all forms from *in* until an EOF is reached. Throws an exception on incomplete forms."
  []
  (lazy-seq
   (let [form (read *in* false ::EOF)]
     (when-not (= ::EOF form)
       (cons form (read-seq))))))

(defn resource-forms
  ([name]
     (resource-forms base-classloader name))
  ([classloader name]
     (with-open [in (resource-reader classloader name)]
       (binding [*in* in]
         (doall (read-seq))))))

(defn extract-resource
  ([name dest-dir]
     (extract-resource base-classloader name dest-dir))
  ([classloader name dest-dir]
     (if-let [s (resource-stream classloader name)]
       (let [dest (file dest-dir name)]
         (.mkdirs (.getParentFile dest))
         (copy s dest)
         dest)
       (throw (Exception. (format "unable to find %s on classpath" name))))))
