package classlojure;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.JarURLConnection;
import java.util.jar.JarEntry;

public class NativeClassLoader extends URLClassLoader {

static final URL[] EMPTY_URLS = new URL[]{};

public NativeClassLoader() {
  this(NativeClassLoader.class.getClassLoader());
}

public NativeClassLoader(ClassLoader parent) {
	super((parent instanceof URLClassLoader) ? ((URLClassLoader)parent).getURLs() : EMPTY_URLS, parent);
}

public NativeClassLoader(URL[] urls, ClassLoader parent) {
  super(urls, parent);
}

static String osName() {
  String osname = System.getProperty("os.name");
  if (osname.startsWith("Linux"))    return "linux";
  if (osname.startsWith("Mac OS X")) return "macosx";
  if (osname.startsWith("SunOS"))    return "solaris";
  if (osname.startsWith("Windows"))  return "windows";
  return "unknown";
}

static String osArch() {
  String osarch = System.getProperty("os.arch");
  if (osarch == "amd64") return "x86_64";
  if (osarch == "i386")  return "x86";
  return osarch;
}

static String extractResource(URL url) {
  try {
    // Extract resource file from Jar.
    JarURLConnection jar = (JarURLConnection) url.openConnection();

    File jarfile = new File(jar.getJarFile().getName());
    File tmpdir  = new File(System.getProperty("java.io.tmpdir"), jarfile.getName());
    File outfile = new File(tmpdir, jar.getJarEntry().getName());
    outfile.getParentFile().mkdirs();
    InputStream  in  = jar.getInputStream();
    OutputStream out = new FileOutputStream(outfile);

    byte[] buffer = new byte[1024];
    int len;
    while ((len = in.read(buffer))>0) out.write(buffer, 0, len);
    out.close();
    in.close();

    return outfile.getPath();
  } catch (ClassCastException e) {
    return url.getFile();
  } catch (java.io.IOException e) {
    return null;
  }
}

protected String findLibrary(String libname) {
  String lib = System.mapLibraryName(libname);
  URL url = findResource(lib);
  if (url == null) {
    url = findResource("native/" + osName() + "/" + osArch() + "/" + lib);
    if (url == null) return null;
  }

  return extractResource(url);
}

protected synchronized Class<?> loadClass(String name, boolean resolve)
  throws ClassNotFoundException
{
  Class c = findLoadedClass(name);
  if (c == null) {
    if (name.startsWith("clojure.") || name.startsWith("java.")) {
      c = getParent().loadClass(name);
    } else {
      try {
        c = findClass(name);
      } catch (ClassNotFoundException e) {
        c = getParent().loadClass(name);
      }
    }
  }

  if (resolve) {
    resolveClass(c);
  }
  return c;
}
}
