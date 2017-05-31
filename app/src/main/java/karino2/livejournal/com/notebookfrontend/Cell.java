package karino2.livejournal.com.notebookfrontend;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by _ on 2017/05/24.
 */

public class Cell {
    public static final int EXEC_COUNT_RUNNING = -1;

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
        if(obj == null)
            return "";
        if(obj.isJsonArray()) {
            List<String> sources = s_gson.fromJson(obj, List.class);
            return mergeAll(sources);
        }
        return obj.getAsString();

    }

    public void setSource(String newContent) {
        source = new JsonPrimitive(newContent);
    }

    public void setExecutionCount(int execCount) {
        executionCount = execCount;
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
            if(data == null)
                return false;
            for(String key : data.keySet()) {
                if(key.startsWith("image/png") ||
                        key.startsWith("image/jpeg")) {
                    return true;
                }
            }
            return false;
        }

        public void setData(JsonObject newData) {
            Type dataType = new TypeToken<Map<String, JsonElement>>(){}.getType();
            data = s_gson.fromJson(newData, dataType);
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

        public String getText()
        {
            if(data == null)
                return jsonElementToString(text);
            return jsonElementToString(data.get("text/plain"));
        }

        public void appendResult(String newcontents) {
            // in this case, output is cleared at first and text must be JsonArray.
            JsonArray array = (JsonArray)text;
            array.add(newcontents);
        }

    }

    public void clearOutput() {
        outputs.clear();
        Output newoutput = new Output();
        newoutput.outputType = "stream";
        newoutput.name = "stdout";
        newoutput.text = new JsonArray();
        outputs.add(newoutput);
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
