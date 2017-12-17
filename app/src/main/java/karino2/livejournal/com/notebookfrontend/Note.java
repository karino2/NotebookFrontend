package karino2.livejournal.com.notebookfrontend;

import android.support.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

/**
 * Created by _ on 2017/05/24.
 */

public class Note {
    public String name;
    public String path;
    public Date lastModified;
    public Date created;
    public Content content;
    public String format;
    public String mimetype;
    public boolean writable;
    public String type;


    public static class Content {
        public List<Cell> cells;

    }

    public static Note fromJson(String buf) {
        Gson gson = getGson();
        return gson.fromJson(buf, Note.class);
    }


    static Gson s_gson;
    @NonNull
    public static Gson getGson() {
        if(s_gson != null)
            return s_gson;
        s_gson =  new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();
        return s_gson;
    }

    public static Note fromJson(InputStream is) throws IOException {
            // JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
        String json = readAll(is);
        return fromJson(json);
    }

    public static String readAll(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuffer buf = new StringBuffer();

        String line;
        while( (line = br.readLine()) != null ) {
            buf.append(line);
        }
        br.close();
        return buf.toString();
    }
}
