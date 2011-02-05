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

## YourKit

YourKit is kindly supporting open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:
[YourKit Java Profiler](http://www.yourkit.com/java/profiler/index.jsp) and
[YourKit .NET Profiler](http://www.yourkit.com/.net/profiler/index.jsp).