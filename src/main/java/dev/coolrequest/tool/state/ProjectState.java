package dev.coolrequest.tool.state;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class ProjectState {

    private String i18n = Locale.CHINESE.getLanguage();

    /**
     * 自定义coder使用项目依赖
     */
    private boolean customCoderUsingProjectLibrary = false;

    private Scope scope = Scope.PROJECT;

    private final Map<String, Object> cache = new HashMap<String, Object>();

    public Scope getScope() {
        return Optional.ofNullable(scope).orElse(Scope.PROJECT);
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public String getI18n() {
        return i18n;
    }

    public ProjectState setI18n(String i18n) {
        this.i18n = i18n;
        return this;
    }

    public ProjectState putCache(String key, Object value) {
        cache.put(key, value);
        return this;
    }

    public Object getCache(String key) {
        return cache.get(key);
    }

    public boolean isCustomCoderUsingProjectLibrary() {
        return customCoderUsingProjectLibrary;
    }

    public ProjectState setCustomCoderUsingProjectLibrary(boolean customCoderUsingProjectLibrary) {
        this.customCoderUsingProjectLibrary = customCoderUsingProjectLibrary;
        return this;
    }

    public Map<String, Object> getCache() {
        return cache;
    }

    public Locale getLocale() {
        return Locale.forLanguageTag(this.i18n);
    }

    public Optional<String> getOpStrCache(String key) {
        return Optional.ofNullable(this.getCache(key)).map(String::valueOf).filter(StringUtils::isNotBlank);
    }

    public boolean getBooleanCache(String name) {
        Object o = cache.get(name);
        if (o == null) {
            return false;
        }
        return Boolean.parseBoolean(String.valueOf(o));
    }

    public void removeCache(String name) {
        cache.remove(name);
    }

    public Object getJsonObjCache(String name) {
        Object obj = cache.get(name);
        if (obj == null) {
            return new JSONObject();
        }
        return obj;
    }
}
