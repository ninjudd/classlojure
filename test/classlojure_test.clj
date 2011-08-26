(ns classlojure-test
  (:use clojure.test classlojure))

(deftest separate-classloader
  (let [cl (classlojure "file:clojure-1.3.0-beta1.jar")]
    (let [v (eval-in cl '*clojure-version*)]
      (is (= 1       (:major v)))
      (is (= 3       (:minor v)))
      (is (= 0       (:incremental v)))
      (is (= "beta1" (:qualifier v))))))

(deftest require-another-namespace
  (let [cl (classlojure "file:clojure-1.3.0-beta1.jar")]
    (is (= "foo-bar-baz"
           (eval-in cl '(do
                          (require 'clojure.string)
                          (clojure.string/join "-" ["foo" "bar" "baz"])))))))

(deftest require-srcfile
  (let [cl (classlojure "file:test-src/" "file:clojure-1.3.0-beta1.jar")]
    (is (= "foo"
           (eval-in cl '(do
                          (require 'foo)
                          (foo/foo)))))))

(deftest returning-nil
  (let [cl (classlojure "file:clojure-1.3.0-beta1.jar")]
    (is (= nil
           (eval-in cl 'nil)))))

(deftest eval-with-direct-forms
  (let [cl (classlojure "file:clojure-1.3.0-beta1.jar")]
    (is (= [(.hashCode System/out) (.hashCode System/err)]
           (eval-in cl '(fn [a b] [(.hashCode a) (.hashCode b)])
                    System/out System/err)))))

(deftest eval-an-evaled-function
  (let [cl (classlojure "file:clojure-1.3.0-beta1.jar")]
    (is (= [(.hashCode System/out) (.hashCode System/err)]
           (eval-in cl '(eval '(fn [a b] [(.hashCode a) (.hashCode b)]))
                    System/out System/err)))))
