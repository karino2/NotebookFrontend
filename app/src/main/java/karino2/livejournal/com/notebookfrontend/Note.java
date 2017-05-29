package karino2.livejournal.com.notebookfrontend;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
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
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        return gson.fromJson(buf, Note.class);
    }

    public static Note fromJson(InputStream is) throws IOException {
            // JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
        String json = readAll(is);
        return fromJson(json);
    }

    private static String readAll(InputStream is) throws IOException {
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
