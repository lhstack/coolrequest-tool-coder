package dev.coolrequest.tool.state;

import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class GlobalStateManager {

    private static Map<String, GlobalState> globalStateCache = new HashMap<>();

    /**
     * 持久化state
     */
    public static synchronized void persistence(Project project) {
        GlobalState globalState = globalStateCache.computeIfAbsent(project.getLocationHash(), key -> new GlobalState());
        try {
            File parent = new File(project.getPresentableUrl(), ".coolrequest");
            if (!parent.exists()) {
                parent.mkdirs();
            }
            File file = new File(parent, ".coder");
            if (!file.exists()) {
                file.createNewFile();
            }
            Gson gson = new Gson();
            String json = gson.toJson(globalState);
            FileUtils.write(file, json, StandardCharsets.UTF_8);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载state
     *
     * @return
     */
    public static synchronized GlobalState loadState(Project project) {
        return globalStateCache.computeIfAbsent(project.getLocationHash(), key -> {
            File parent = new File(project.getPresentableUrl(), ".coolrequest");
            if (!parent.exists()) {
                parent.mkdirs();
            }
            File file = new File(parent, ".coder");
            if (file.exists()) {
                try {
                    byte[] bytes = FileUtils.readFileToByteArray(file);
                    Gson gson = new Gson();
                    return gson.fromJson(new String(bytes, StandardCharsets.UTF_8), GlobalState.class);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            return new GlobalState();
        });
    }

}
