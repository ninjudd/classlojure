classlojure lets you easily create a classloader with an alternate classpath and evaluate
clojure forms in it. This classloader can even use a different version of clojure than
your primary classloader.

## Usage

    (use 'classlojure)

    (def clojure-13 (classlojure "file:clojure-1.3.0-alpha2-SNAPSHOT.jar"))

    (eval-in clojure-13 '*clojure-version*)
    ;; {:interim true, :major 1, :minor 3, :incremental 0, :qualifier "alpha2"}

    (eval '*clojure-version*)
    ;; {:major 1, :minor 2, :incremental 0, :qualifier ""}

#### Lisps and lists

A quick note that can save you from despair. The third parameter to `eval-in` is a quoted list and won't be treated any differently from what we are used to in Lisps. Having said that, if you are closing over something and you use unquote like this:

    (let [list-of-strings (map str (range 10))]
      (classlojure/eval-in cl
      `(println ~list-of-strings)))
      
You shouldn't be surprised by the dreaded `java.lang.String cannot be cast to clojure.lang.IFn`; the first item of the list is indeed a string and not a function as Lisps dictate.

You need something more sophisticated in order for the above to work:

    (let [list-of-strings (map str (range 10))]
      (classlojure/eval-in cl
      `(println '~list-of-strings)))
    
The order counts here. You first evaluate `list-of-strings` and get a list back which thereafter you quote so that it is not expected to have a function in first position.

## YourKit

YourKit is kindly supporting open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:
[YourKit Java Profiler](http://www.yourkit.com/java/profiler/index.jsp) and
[YourKit .NET Profiler](http://www.yourkit.com/.net/profiler/index.jsp).
