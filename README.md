## Native Libraries in Jars

The goal of this unfortunately named package is to allow you to use jars that contain
native libraries just like regular jars. No need to run 'lein native-deps', no need to set
LD_LIBRARY_PATH. As long as the Jar contains native libraries like this:

    native/{osname}/{osarch}/libawesome.{jniext}

and requires the library like this:

    System.loadLibrary("awesome");

then you can import the library in Clojure like this:

    (import-native 'com.awesome.Awesome)