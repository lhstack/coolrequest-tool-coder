package dev.coolrequest.tool.common;

import com.intellij.openapi.util.IconLoader;
import dev.coolrequest.tool.utils.ClassLoaderUtils;

import javax.swing.*;

public interface Icons {

    Icon DEMO_ACTION = IconLoader.findIcon(ClassLoaderUtils.getResource("icons/demo_action.svg"));
    Icon COMPILE_ACTION = IconLoader.findIcon(ClassLoaderUtils.getResource("icons/compile_action.svg"));
    Icon SAVE_ACTION = IconLoader.findIcon(ClassLoaderUtils.getResource("icons/save_action.svg"));
    Icon INSTALL_ACTION = IconLoader.findIcon(ClassLoaderUtils.getResource("icons/install_action.svg"));
}
