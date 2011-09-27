(ns classlojure-test
  (:use clojure.test classlojure.core
        [clojure.java.io :only [copy input-stream]]))

(def filepath "test/clojure-1.3.0.jar")
(def clj-13 (str "file:" filepath))

(defn with-clojure-jar [f]
  (let [file (java.io.File. filepath)]
    (when-not (.exists file)
      (.createNewFile file)
      (let [out (java.io.FileOutputStream. file)
            in (input-stream "http://repo1.maven.org/maven2/org/clojure/clojure/1.3.0/clojure-1.3.0.jar")
            ]
        (copy in out))))
  (f))

(use-fixtures :once with-clojure-jar)

(deftest separate-classloader
  (let [cl (classlojure clj-13)]
    (let [v (eval-in cl '*clojure-version*)]
      (is (= 1       (:major v)))
      (is (= 3       (:minor v)))
      (is (= 0       (:incremental v)))
      (is (nil?      (:qualifier v))))))

(deftest require-another-namespace
  (let [cl (classlojure clj-13)]
    (is (= "foo-bar-baz"
           (eval-in cl '(do
                          (require 'clojure.string)
                          (clojure.string/join "-" ["foo" "bar" "baz"])))))))

(deftest require-srcfile
  (let [cl (classlojure "file:test-src/" clj-13)]
    (is (= "foo"
           (eval-in cl '(do
                          (require 'foo)
                          (foo/foo)))))))

(deftest returning-nil
  (let [cl (classlojure clj-13)]
    (is (= nil
           (eval-in cl 'nil)))))

(deftest eval-with-direct-forms
  (let [cl (classlojure clj-13)]
    (is (= [(.hashCode System/out) (.hashCode System/err)]
           (eval-in cl '(fn [a b] [(.hashCode a) (.hashCode b)])
                    System/out System/err)))))

(deftest eval-an-evaled-function
  (let [cl (classlojure clj-13)]
    (is (= [(.hashCode System/out) (.hashCode System/err)]
           (eval-in cl '(eval '(fn [a b] [(.hashCode a) (.hashCode b)]))
                    System/out System/err)))))
