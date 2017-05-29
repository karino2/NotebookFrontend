package karino2.livejournal.com.notebookfrontend;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.List;
import java.util.Map;

/**
 * Created by _ on 2017/05/24.
 */

public class Cell {
    String cellType;
    JsonElement source;
    Integer executionCount;
    // metadata

    // always 1 element.
    List<Output> outputs;

    static Gson s_gson = new Gson();

    public String getSource() {
        return jsonElementToString(source);
    }


    static String jsonElementToString(JsonElement obj) {
        if(obj.isJsonArray()) {
            List<String> sources = s_gson.fromJson(obj, List.class);
            return mergeAll(sources);
        }
        return obj.getAsString();

    }


    static String mergeAll(List<String> texts) {
        StringBuilder buf = new StringBuilder();
        for(String source : texts) {
            buf.append(source);
            // should I handle return code here?
        }
        return buf.toString();
    }


    public static class Output {
        public String outputType;

        // (name, text) or data
        public String name = "";
        public JsonElement text;
        public Map<String, JsonElement> data;

        public boolean isImage() {
            return data != null;
        }

        public String getImageAsBase64() {
            for(String key : data.keySet()) {
                if(key.startsWith("image/png") ||
                        key.startsWith("image/jpeg")) {
                    return jsonElementToString(data.get(key));
                }
            }
            return null;
        }

        public String getText() {
            return jsonElementToString(text);
        }

    }

    Output getOutput() {
        if(outputs.isEmpty())
            return null;
        return outputs.get(0);
    }

    public enum CellType {
        UNINITIALIZE,
        CODE,
        MARKDOWN
    }

    public CellType getCellType() {
        if("code".equals(cellType)) {
            return CellType.CODE;
        } else if("markdown".equals(cellType)) {
            return CellType.MARKDOWN;
        } else {
            return CellType.UNINITIALIZE;
        }
    }


}
