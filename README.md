#NOTE:#

This is broken with the current version of clojure because of a bug that makes
`*use-context-classloader*` not work with `import`. Here is the
[thread](http://groups.google.com/group/clojure-dev/browse_thread/thread/f61b550abf7f9c52/da25ba7e31b9431c?q=)
discussing it.


## Native Libraries in Jars

The goal of this unfortunately named package is to allow you to use jars that contain
native libraries just like regular jars. No need to run 'lein native-deps', no need to set
LD_LIBRARY_PATH. As long as the Jar contains native libraries like this:

    native/{osname}/{osarch}/libawesome.{jniext}

and requires the library like this:

    System.loadLibrary("awesome");

then you can import the library in Clojure like this:

    (import-native 'com.awesome.Awesome)