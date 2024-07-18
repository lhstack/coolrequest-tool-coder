package dev.coolrequest.tool.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassLoaderUtils {

    private static final ClassLoader CLASS_LOADER = ClassLoaderUtils.class.getClassLoader();


    public static List<Class<?>> scan(ClassLoader classLoader, Predicate<Class<?>> clazzPredicate, String... basePackages) {
        return Stream.of(basePackages).flatMap(basePackage -> {
            String packageDir = basePackage.replaceAll("\\.", "/");
            List<Class<?>> classes = new ArrayList<>();
            try {
                Enumeration<URL> resources = classLoader.getResources(packageDir);
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    String protocol = url.getProtocol();
                    if ("file".equals(protocol)) {
                        resolveFile(classLoader, packageDir, new File(url.getFile()), clazzPredicate, classes, ".class");
                    } else if ("jar".equals(protocol)) {
                        resolveJar(classLoader, packageDir, clazzPredicate, (JarURLConnection) url.openConnection(), classes, ".class");
                    }
                }
            } catch (Throwable ignore) {

            }
            return classes.stream();
        }).collect(Collectors.toList());
    }

    public static List<Class<?>> scan(Predicate<Class<?>> clazzPredicate, String... basePackages) {
        return scan(CLASS_LOADER, clazzPredicate, basePackages);
    }

    private static void resolveFile(ClassLoader classLoader, String packageDir, File file, Predicate<Class<?>> clazzPredicate, List<Class<?>> classes, String fileSuffixName) {

        if (file.exists() && file.isFile()) {

            String name = file.getAbsolutePath();
            if (name.endsWith(fileSuffixName)) {
                String className = name.substring(0, name.length() - 6).replaceAll("\\\\", "/");
                int index = className.indexOf(packageDir);
                if (index != -1) {
                    className = className.substring(index).replaceAll("/", ".");
                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (clazzPredicate.test(clazz)) {
                            classes.add(clazz);
                        }
                    } catch (Throwable ignore) {

                    }
                }
            }
        } else if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        String name = f.getAbsolutePath();
                        if (name.endsWith(fileSuffixName)) {
                            String className = name.substring(0, name.length() - 6).replaceAll("\\\\", "/");
                            int index = className.indexOf(packageDir);
                            if (index != -1) {
                                className = className.substring(index).replaceAll("/", ".");
                                try {
                                    Class<?> clazz = classLoader.loadClass(className);
                                    if (clazzPredicate.test(clazz)) {
                                        classes.add(clazz);
                                    }
                                } catch (Throwable ignore) {

                                }
                            }
                        }
                    } else {
                        resolveFile(classLoader, packageDir, f, clazzPredicate, classes, fileSuffixName);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        URL resource = getResource("icons/demo_action.svg");
        System.out.println(resource);
    }

    private static void resolveJar(ClassLoader classLoader, String packageDir, Predicate<Class<?>> clazzPredicate, JarURLConnection connection, List<Class<?>> classes, String fileSuffixName) throws Throwable {
        JarFile jarFile = connection.getJarFile();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.endsWith(fileSuffixName) && name.contains(packageDir)) {
                String className = name.substring(0, name.length() - 6).replaceAll("/", ".");
                try {
                    Class<?> clazz = classLoader.loadClass(className);
                    if (clazzPredicate.test(clazz)) {
                        classes.add(clazz);
                    }
                } catch (Throwable ignore) {

                }
            }
        }
    }

    public static URL getResource(String name) {
        return CLASS_LOADER.getResource(name);
    }

    public static InputStream getResourceAsStream(String name) {
        return CLASS_LOADER.getResourceAsStream(name);
    }

    public static byte[] getResourceToBytes(String name) {
        URL resource = getResource(name);
        if (resource != null) {
            try (InputStream in = resource.openStream()) {
                return IOUtils.toByteArray(in);
            } catch (Throwable e) {
                return new byte[0];
            }
        }
        return new byte[0];
    }

    public static String getResourceToString(String name) {
        return new String(getResourceToBytes(name), StandardCharsets.UTF_8);
    }
}
