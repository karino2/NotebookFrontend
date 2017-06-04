package karino2.livejournal.com.notebookfrontend;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by _ on 2017/06/03.
 */

public class CellListSerializer {
    List<Cell> cells = new ArrayList<Cell>();
    public void clear() {
        cells.clear();
    }

    public void add(Cell cell) {
        cells.add(cell);
    }


    /*
    "{"type":"notebook",
"content":{
    "cells":[
        {"metadata":{"collapsed":false,"trusted":false},"cell_type":"code","source":"print(\"Hello\")","execution_count":11,
        "outputs":[{"name":"stdout","output_type":"stream","text":"Hello\n"}]},
        {"metadata":{"collapsed":false,"trusted":false},
            "cell_type":"code","source":"print(\"Hello2\\n\")\nprint(\"NextLine\")","execution_count":1,
            "outputs":[{"name":"stdout","output_type":"stream","text":"Hello2\n\nNextLine\n"}]},
        {"metadata":{"collapsed":true},"cell_type":"markdown","source":"## Markdown cell\n\nHere is the test of markdown.\nNext line."},
        {"metadata":{"collapsed":false,"trusted":false},"cell_type":"code","source":"from IPython.display import Image\nImage(\"test.png\")","execution_count":4,
        "outputs":[{"data":{"image/png":"iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAIAAACQkWg2AAAACXBIWXMAADXUAAA11AFeZeUIAAAA\n70lEQVQokZWSMQ6CQBBFP4YYOmxJKCwMrYWdBdkz0MoJqOAQdDRewMYzGFuvYOEBKExoCIWJEQL5\nFrgqumbjZIqdyX+T+btrkMQ/YQ6qpkGWYbdDVQHAeAzbxmSCIMBqBdMEAD6jLLlcElCnEKxrkhLo\nOgrxU93nek1y9Fhmv8fhoFn/dAIggeNR7/d6fQNuNz3Qtm+AZemBIACet7TZKFwuFq9zkvRC+Q6u\nqxgZhkhT5DmEgOf1PQk4jgI4nxHHHz3pwfNg25/AdKqY8nrpLBsYmM14ufArMKi2W/o+53NGEYvi\nW03S+Pe3jvSSYdwBxsfDQdbYcZMAAAAASUVORK5CYII=\n","text/plain":"<IPython.core.display.Image object>"},"execution_count":4,"metadata":{},"output_type":"execute_result"}]},{"metadata":{"collapsed":false,"trusted":true},"cell_type":"code","source":"3+15","execution_count":5,"outputs":[{"output_type":"execute_result","data":{"text/plain":"18"},"metadata":{},"execution_count":5}]},
        {"metadata":{"trusted":true,"collapsed":true},"cell_type":"code","source":"","execution_count":null,"outputs":[]}],
    "metadata":{"kernelspec":{"name":"python3","display_name":"Python 3","language":"python"},
            "language_info":{"name":"python","version":"3.6.0","mimetype":"text/x-python","codemirror_mode":{"name":"ipython","version":3},"pygments_lexer":"ipython3","nbconvert_exporter":"python","file_extension":".py"}
           },
"nbformat":4,"nbformat_minor":2}}"

     */

    // may be we should pass writer and write websocket on streaming manner, but first just return String.
    public String toJsonForSaveMessage() throws IOException {
        Gson gson = Note.createGson();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(baos));

        writer.beginObject();
        writer.name("type").value("notebook");

        /*
"content":{
    "cells":[
        {"metadata":{"collapsed":false,"trusted":false},"cell_type":"code","source":"print(\"Hello\")","execution_count":11,
        ...
        ]

         */
        writer.name("content");
        writer.beginObject();

        writer.name("cells");
        writer.beginArray();
        for(Cell cell : cells) {
            cell.toJson(gson, writer);
        }

        writer.endArray();



        /*
            "metadata":{"kernelspec":{"name":"python3","display_name":"Python 3","language":"python"},
         */
        writer.name("metadata"); // begin metaata:
        writer.beginObject();

        writer.name("kernelspec")
                .beginObject()
                .name("name").value("python3")
                .name("display_name").value("Python 3")
                .name("language").value("python")
                .endObject();

        /*
          "language_info":{"name":"python","version":"3.6.0","mimetype":"text/x-python","codemirror_mode":{"name":"ipython","version":3},"pygments_lexer":"ipython3","nbconvert_exporter":"python","file_extension":".py"}
           },
         */
        writer.name("language_info")
                .beginObject()
                .name("name").value("python")
                .name("version").value("3.6.0")
                .name("mimetype").value("text/x-python")
                .name("codemirror_mode")
                .beginObject()
                .name("name").value("ipython")
                .name("version").value(3)
                .endObject()
                .name("pygments_lexer").value("ipython3")
                .name("nbconvert_exporter").value("python")
                .name("file_extension").value(".py")
                .endObject();

        writer.endObject(); // end metadata;

        /*
        "nbformat":4,"nbformat_minor":2}}"
         */
        writer.name("nbformat").value(4);
        writer.name("nbformat_minor").value(2);

        writer.endObject(); // end contents

        writer.endObject(); // end of all


        writer.close();
        return baos.toString("UTF-8");

    }
}
