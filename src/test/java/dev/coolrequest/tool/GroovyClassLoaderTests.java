package dev.coolrequest.tool;


import groovy.lang.GroovyClassLoader;
import groovy.util.GroovyScriptEngine;
import org.junit.Test;

public class GroovyClassLoaderTests {

    @Test
    public void test() {
        try (GroovyClassLoader gcl = new GroovyClassLoader()) {
            gcl.addClasspath("E:\\projects\\java\\coolrequest-tool-coder\\scripts");
            GroovyScriptEngine scriptEngine = new GroovyScriptEngine(gcl.getURLs());
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }
}
