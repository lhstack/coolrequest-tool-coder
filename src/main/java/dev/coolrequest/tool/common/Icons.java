package dev.coolrequest.tool.common;

import com.intellij.openapi.util.IconLoader;
import dev.coolrequest.tool.utils.ClassLoaderUtils;

import javax.swing.*;

public interface Icons {

    Icon DEMO_ACTION = IconLoader.findIcon(ClassLoaderUtils.getResource("icons/demo_action.svg"));
    Icon COMPILE_ACTION = IconLoader.findIcon(ClassLoaderUtils.getResource("icons/compile_action.svg"));
    Icon USING_PROJECT_LIBRARY_ACTION = IconLoader.findIcon(ClassLoaderUtils.getResource("icons/using_project_library_action.svg"));
    Icon INSTALL_ACTION = IconLoader.findIcon(ClassLoaderUtils.getResource("icons/install_action.svg"));
}
