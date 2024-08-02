package dev.coolrequest.tool.state;

import com.alibaba.fastjson2.JSONObject;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GlobalState {

    private static final File GLOBAL_FILE;

    private static GlobalState INSTANCE;

    static {
        File parent = new File(System.getProperty("user.home") + "/.config/.cool-request/request/coder");
        if (!parent.exists()) {
            parent.mkdirs();
        }
        GLOBAL_FILE = new File(parent, ".coder.json");
        if (!GLOBAL_FILE.exists()) {
            try {
                GLOBAL_FILE.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            INSTANCE = new GlobalState();
        } else {
            try {
                byte[] bytes = FileUtils.readFileToByteArray(GLOBAL_FILE);
                INSTANCE = JSONObject.parseObject(new String(bytes, StandardCharsets.UTF_8), GlobalState.class);
                INSTANCE = INSTANCE == null ? new GlobalState() : INSTANCE;
            } catch (IOException e) {
                INSTANCE = new GlobalState();
                throw new RuntimeException(e);
            }
        }
    }

    private Map<String, Object> cache = new HashMap<String, Object>();


    public Map<String, Object> getCache() {
        return cache;
    }

    public void setCache(Map<String, Object> cache) {
        this.cache = cache;
    }

    public static void putCache(String key, Object value) {
        INSTANCE.cache.put(key, value);
        try {
            FileUtils.write(GLOBAL_FILE, JSONObject.toJSONString(INSTANCE), StandardCharsets.UTF_8);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getCache(String key) {
        return INSTANCE.cache.get(key);
    }

    public static Optional<String> getOpStrCache(String key) {
        return Optional.ofNullable(getCache(key)).map(String::valueOf).filter(StringUtils::isNotBlank);
    }

    public static Object getJsonObjCache(String name) {
        Object obj = INSTANCE.cache.get(name);
        if (obj == null) {
            return new JSONObject();
        }
        return obj;
    }

    public static void removeCache(String name) {
        INSTANCE.cache.remove(name);
        try {
            FileUtils.write(GLOBAL_FILE, JSONObject.toJSONString(INSTANCE), StandardCharsets.UTF_8);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


}
