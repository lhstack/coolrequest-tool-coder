package dev.coolrequest.tool.coder.utils;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
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

    public static List<Class<?>> scan(Predicate<Class<?>> clazzPredicate, String... basePackages) {
        return Stream.of(basePackages).flatMap(basePackage -> {
            String packageDir = basePackage.replaceAll("\\.", "/");
            List<Class<?>> classes = new ArrayList<>();
            try {
                Enumeration<URL> resources = CLASS_LOADER.getResources(packageDir);
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    String protocol = url.getProtocol();
                    if ("file".equals(protocol)) {
                        resolveFile(packageDir, new File(url.getFile()), clazzPredicate, classes);
                    } else if ("jar".equals(protocol)) {
                        resolveJar(packageDir,clazzPredicate, (JarURLConnection) url.openConnection(), classes);
                    }
                }
            } catch (Throwable ignore) {

            }
            return classes.stream();
        }).collect(Collectors.toList());
    }

    private static void resolveFile(String packageDir, File file, Predicate<Class<?>> clazzPredicate, List<Class<?>> classes) {
        if (file.exists() && file.isFile()) {
            String name = file.getAbsolutePath();
            if (name.endsWith(".class")) {
                String className = name.substring(0, name.length() - 6).replaceAll("\\\\", "/");
                int index = className.indexOf(packageDir);
                if (index != -1) {
                    className = className.substring(index).replaceAll("/", ".");
                    try {
                        Class<?> clazz = CLASS_LOADER.loadClass(className);
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
                        if (name.endsWith(".class")) {
                            String className = name.substring(0, name.length() - 6).replaceAll("\\\\", "/");
                            int index = className.indexOf(packageDir);
                            if (index != -1) {
                                className = className.substring(index).replaceAll("/", ".");
                                try {
                                    Class<?> clazz = CLASS_LOADER.loadClass(className);
                                    if (clazzPredicate.test(clazz)) {
                                        classes.add(clazz);
                                    }
                                } catch (Throwable ignore) {

                                }
                            }
                        }
                    } else {
                        resolveFile(packageDir, f, clazzPredicate, classes);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        List<Class<?>> scan = ClassLoaderUtils.scan(clazz -> {
            return true;
        }, "dev.coolrequest.tool.coder.encoder");
        System.out.println(scan);
    }

    private static void resolveJar(String packageDir, Predicate<Class<?>> clazzPredicate, JarURLConnection connection, List<Class<?>> classes) throws Throwable {
        JarFile jarFile = connection.getJarFile();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.endsWith(".class") && name.contains(packageDir)) {
                String className = name.substring(0, name.length() - 6).replaceAll("/", ".");
                try {
                    Class<?> clazz = CLASS_LOADER.loadClass(className);
                    if (clazzPredicate.test(clazz)) {
                        classes.add(clazz);
                    }
                } catch (Throwable ignore) {

                }
            }
        }
    }
}
