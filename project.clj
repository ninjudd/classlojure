(defproject classlojure "0.1.0-SNAPSHOT"
  :description "Advanced classloading for clojure."
  :dependencies [[clojure "1.2.0-master-SNAPSHOT"]]
  :source-path "src/clj"
  :java-source-path "src/jvm")

(use 'clojure.contrib.with-ns)
(require 'leiningen.compile)
(with-ns 'leiningen.compile
  (defn compile [project]
    (lancet/javac {:srcdir    (make-path (:java-source-path project))
                   :destdir   (:compile-path project)
                   :classpath (apply make-path (get-classpath project))})))
