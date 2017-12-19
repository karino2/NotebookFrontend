package karino2.livejournal.com.notebookfrontend;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
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
    JsonElement metadata;

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

    public void setExecutionCount(Integer execCount) {
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
        // (name, text) or data

        // name is deleted? just comment out for a while.
        // public String name = "";

        public String outputType;

        public JsonElement text;
        public Map<String, JsonElement> data;

        public Integer executionCount;

        // now metadata seems to become MUST.
        // {"metadata":{},"data":{"text/plain":"7"},"output_type":"execute_result","execution_count":1}
        public void writeToJson(Gson gson, JsonWriter writer) throws IOException {

            // these days, validation is rigid. just support what I met.
            // based on https://ipython.org/ipython-doc/3/notebook/nbformat.html
            if("execute_result".equals(outputType)) {
                writer.beginObject()
                        .name("metadata")
                        .beginObject().endObject()
                        .name("data")
                        .jsonValue(gson.toJson(data))
                        .name("output_type")
                        .value(outputType)
                        .name("execution_count")
                        .value(executionCount)
                        .endObject();
            }else if("stream".equals(outputType)) {
                writer.beginObject()
                    .name("output_type")
                    .value(outputType)
                    .name("name")
                        .value("stdout")
                    .name("text")
                    .jsonValue(gson.toJson(text))
                    .endObject();
            }
        }


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

        public void setResult(Integer newExecCount, JsonObject  newData) {
            setData(newData);
            outputType = "execute_result";
            executionCount = newExecCount;
        }

        void setData(JsonObject newData) {
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
        // newoutput.name = "stdout";
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

    public void setCellType(CellType newType) {
        switch(newType) {
            case CODE:
                cellType = "code";
                break;
            case MARKDOWN:
                cellType = "markdown";
                break;
            default:
                throw new IllegalArgumentException("Unknown cell type: " + newType);
        }
    }

    public void toJson(Gson gson, JsonWriter writer) throws IOException {
        if(CellType.MARKDOWN == getCellType()) {
            toJsonMarkdownCell(gson, writer);
        } else {
            toJsonCodeCell(gson, writer);
        }
    }

    Integer getExecCountForSave() {
        if(executionCount == null)
            return null;
        if(executionCount  == EXEC_COUNT_RUNNING)
            return null;
        return executionCount;
    }

    private void toJsonCodeCell(Gson gson, JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("cell_type").value("code")
                .name("execution_count").value(getExecCountForSave());


        writeMetadata(gson, writer);

        writer.name("source")
                .value(getSource());

        writer.name("outputs")
                .beginArray();
        writeOutput(gson, writer);
        writer.endArray();


        writer.endObject();

    }

    void writeOutput(Gson gson, JsonWriter writer) throws IOException {
        Output o = getOutput();
        if(o == null) {
            return;
        }
        o.writeToJson(gson, writer);

    }


    private void toJsonMarkdownCell(Gson gson, JsonWriter writer) throws IOException {
        //         {"metadata":{"collapsed":true},"cell_type":"markdown","source":"## Markdown cell\n\nHere is the test of markdown.\nNext line."},
        writer.beginObject()
                .name("cell_type").value("markdown");

        writeMetadata(gson, writer);

        writer.name("source").value(getSource());


        writer.endObject();

    }

    private void writeMetadata(Gson gson, JsonWriter writer) throws IOException {
        writer.name("metadata");

        if(metadata == null) {
            writer.beginObject().endObject();
        } else {
            writer.jsonValue(gson.toJson(metadata));
        }
    }


}
