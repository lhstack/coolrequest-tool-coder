package dev.coolrequest.tool.state;

import java.util.Locale;

public class GlobalState {

    private String i18n = Locale.CHINESE.getLanguage();

    private static final GlobalState INSTANCE = new GlobalState();

    public static GlobalState getInstance() {
        return INSTANCE;
    }

    public String getI18n() {
        return i18n;
    }

    public GlobalState setI18n(String i18n) {
        this.i18n = i18n;
        return this;
    }

    public Locale getLocale() {
        return Locale.forLanguageTag(this.i18n);
    }

}
