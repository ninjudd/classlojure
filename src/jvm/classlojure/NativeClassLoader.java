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

void writeStream(InputStream in, File outfile) {
  outfile.getParentFile().mkdirs();

  try {
    OutputStream out = new FileOutputStream(outfile);

    byte[] buffer = new byte[1024];
    int len;
    while ((len = in.read(buffer))>0) out.write(buffer, 0, len);
    out.close();
    in.close();
  } catch (java.io.IOException e) {
  }
}

public String extractResource(String name) {
  return extractResource(name, null);
}

public String extractResource(String name, File outdir) {
  File outfile;
  InputStream in;
  URL url = findResource(name);
  if (url == null) return null;

  try {
    URLConnection c = url.openConnection();
    if (c instanceof JarURLConnection) {
      // Extract resource file from Jar.
      JarURLConnection jar = (JarURLConnection) c;
      File jarfile = new File(jar.getJarFile().getName());

      if (outdir == null) outdir = new File(System.getProperty("java.io.tmpdir"), jarfile.getName());
      outfile = new File(outdir, jar.getJarEntry().getName());
      in = jar.getInputStream();
    } else {
      String file = url.getFile();
      if (outdir == null) return file;

      outfile = new File(outdir, name);
      in = new FileInputStream(file);
    }
  } catch (java.io.IOException e) {
    return null;
  }

  writeStream(in, outfile);
  return outfile.getPath();
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

protected String findLibrary(String libname) {
  String lib = System.mapLibraryName(libname);
  String libfile = extractResource(lib);
  if (libfile == null) libfile = extractResource("native/" + osName() + "/" + osArch() + "/" + lib);
  return libfile;
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
