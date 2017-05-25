package karino2.livejournal.com.notebookfrontend;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by _ on 2017/05/24.
 */

public class SessionInfo {
    public static class Kernel {
        public String id;
        public String name;
    }

    public Kernel kernel;

    public static SessionInfo fromJson(String buf) {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        return gson.fromJson(buf, SessionInfo.class);
    }
}
