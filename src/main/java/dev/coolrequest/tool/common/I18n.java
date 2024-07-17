package dev.coolrequest.tool.common;

import dev.coolrequest.tool.state.GlobalState;

import java.util.ResourceBundle;

public class I18n {

    public static String getString(String key) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n/msg", GlobalState.getInstance().getLocale());
        return bundle.getString(key);
    }
}
