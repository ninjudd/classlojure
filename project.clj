(defproject classlojure "0.0.4-SNAPSHOT"
  :description "Advanced classloading for clojure."
  :namespaces [classlojure]
  :dependencies [[org.clojure/clojure "1.2.0-master-SNAPSHOT"]]
  :source-path "src/clj"
  :java-source-path "src/jvm")

(ns leiningen.compile
  (:require lancet)
  (:use leiningen.compile
        [leiningen.classpath :only [make-path get-classpath]])
  (:refer-clojure :exclude [compile]))

(def compile* compile)
(defn compile [project]
  (lancet/javac {:srcdir    (make-path (:java-source-path project))
                 :destdir   (:compile-path project)
                 :classpath (apply make-path (get-classpath project))})
  (compile* project))