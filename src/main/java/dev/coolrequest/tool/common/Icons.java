package dev.coolrequest.tool.common;

import com.intellij.openapi.util.IconLoader;
import dev.coolrequest.tool.utils.ClassLoaderUtils;

import javax.swing.*;

public interface Icons {

    Icon DEMO = IconLoader.findIcon(ClassLoaderUtils.getResource("icons/demo.svg"));
    Icon COMPILE = IconLoader.findIcon(ClassLoaderUtils.getResource("icons/compile.svg"));
    Icon LIBRARY = IconLoader.findIcon(ClassLoaderUtils.getResource("icons/library.svg"));
    Icon INSTALL = IconLoader.findIcon(ClassLoaderUtils.getResource("icons/install.svg"));
    Icon RUN = IconLoader.findIcon(ClassLoaderUtils.getResource("icons/run.svg"));
    Icon ENV = IconLoader.findIcon(ClassLoaderUtils.getResource("icons/env.svg"));
}
