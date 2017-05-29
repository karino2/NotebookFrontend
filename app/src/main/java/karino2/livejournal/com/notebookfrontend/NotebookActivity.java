package karino2.livejournal.com.notebookfrontend;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class NotebookActivity extends Activity {


    ArrayAdapter<Cell> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);

        ListView lv = (ListView)findViewById(R.id.listView);

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        /*
        Note note = Note.fromJson(TEST_JSON_CONTENT);
        List<Cell> cells = note.content.cells;
        */

        JsonObject contentjson = gson.fromJson(TEST_JSON_CONTENT, JsonObject.class);
        JsonArray cellsJson = contentjson.get("cells").getAsJsonArray();
        /*
        cellsJson.
        List<Cell> cells = gson.fromJson(cellsJson, List.class);
        */
        List<Cell> cells = new ArrayList<Cell>();
        for(JsonElement elem : cellsJson) {
            Cell one = gson.fromJson(elem, Cell.class);
            cells.add(one);

        }

        listAdapter = new ArrayAdapter<Cell>(this, 0, cells) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                CellView cellView;
                if(convertView == null) {
                    cellView = (CellView)getLayoutInflater().inflate(R.layout.cell_item, null);
                } else {
                    cellView = (CellView)convertView;
                }

                Cell cell = getItem(position);
                cellView.bindCell(cell);
                return cellView;
            }
        };
        lv.setAdapter(listAdapter);

    }



    public static final String TEST_JSON_CONTENT = "{\n" +
            " \"cells\": [\n" +
            "  {\n" +
            "   \"cell_type\": \"code\",\n" +
            "   \"execution_count\": 11,\n" +
            "   \"metadata\": {\n" +
            "    \"collapsed\": false\n" +
            "   },\n" +
            "   \"outputs\": [\n" +
            "    {\n" +
            "     \"name\": \"stdout\",\n" +
            "     \"output_type\": \"stream\",\n" +
            "     \"text\": [\n" +
            "      \"Hello\\n\"\n" +
            "     ]\n" +
            "    }\n" +
            "   ],\n" +
            "   \"source\": [\n" +
            "    \"print(\\\"Hello\\\")\"\n" +
            "   ]\n" +
            "  },\n" +
            "  {\n" +
            "   \"cell_type\": \"code\",\n" +
            "   \"execution_count\": 1,\n" +
            "   \"metadata\": {\n" +
            "    \"collapsed\": false\n" +
            "   },\n" +
            "   \"outputs\": [\n" +
            "    {\n" +
            "     \"name\": \"stdout\",\n" +
            "     \"output_type\": \"stream\",\n" +
            "     \"text\": [\n" +
            "      \"Hello2\\n\",\n" +
            "      \"\\n\",\n" +
            "      \"NextLine\\n\"\n" +
            "     ]\n" +
            "    }\n" +
            "   ],\n" +
            "   \"source\": [\n" +
            "    \"print(\\\"Hello2\\\\n\\\")\\n\",\n" +
            "    \"print(\\\"NextLine\\\")\"\n" +
            "   ]\n" +
            "  },\n" +
            "  {\n" +
            "   \"cell_type\": \"markdown\",\n" +
            "   \"metadata\": {\n" +
            "    \"collapsed\": true\n" +
            "   },\n" +
            "   \"source\": [\n" +
            "    \"## Markdown cell\\n\",\n" +
            "    \"\\n\",\n" +
            "    \"Here is the test of markdown.\\n\",\n" +
            "    \"Next line.\"\n" +
            "   ]\n" +
            "  },\n" +
            "  {\n" +
            "   \"cell_type\": \"code\",\n" +
            "   \"execution_count\": 4,\n" +
            "   \"metadata\": {\n" +
            "    \"collapsed\": false\n" +
            "   },\n" +
            "   \"outputs\": [\n" +
            "    {\n" +
            "     \"data\": {\n" +
            "      \"image/png\": \"iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAIAAACQkWg2AAAACXBIWXMAADXUAAA11AFeZeUIAAAA\\n70lEQVQokZWSMQ6CQBBFP4YYOmxJKCwMrYWdBdkz0MoJqOAQdDRewMYzGFuvYOEBKExoCIWJEQL5\\nFrgqumbjZIqdyX+T+btrkMQ/YQ6qpkGWYbdDVQHAeAzbxmSCIMBqBdMEAD6jLLlcElCnEKxrkhLo\\nOgrxU93nek1y9Fhmv8fhoFn/dAIggeNR7/d6fQNuNz3Qtm+AZemBIACet7TZKFwuFq9zkvRC+Q6u\\nqxgZhkhT5DmEgOf1PQk4jgI4nxHHHz3pwfNg25/AdKqY8nrpLBsYmM14ufArMKi2W/o+53NGEYvi\\nW03S+Pe3jvSSYdwBxsfDQdbYcZMAAAAASUVORK5CYII=\\n\",\n" +
            "      \"text/plain\": [\n" +
            "       \"<IPython.core.display.Image object>\"\n" +
            "      ]\n" +
            "     },\n" +
            "     \"execution_count\": 4,\n" +
            "     \"metadata\": {},\n" +
            "     \"output_type\": \"execute_result\"\n" +
            "    }\n" +
            "   ],\n" +
            "   \"source\": [\n" +
            "    \"from IPython.display import Image\\n\",\n" +
            "    \"Image(\\\"test.png\\\")\"\n" +
            "   ]\n" +
            "  },\n" +
            "  {\n" +
            "   \"cell_type\": \"code\",\n" +
            "   \"execution_count\": null,\n" +
            "   \"metadata\": {\n" +
            "    \"collapsed\": true\n" +
            "   },\n" +
            "   \"outputs\": [],\n" +
            "   \"source\": []\n" +
            "  }\n" +
            " ],\n" +
            " \"metadata\": {\n" +
            "  \"kernelspec\": {\n" +
            "   \"display_name\": \"Python 3\",\n" +
            "   \"language\": \"python\",\n" +
            "   \"name\": \"python3\"\n" +
            "  },\n" +
            "  \"language_info\": {\n" +
            "   \"codemirror_mode\": {\n" +
            "    \"name\": \"ipython\",\n" +
            "    \"version\": 3\n" +
            "   },\n" +
            "   \"file_extension\": \".py\",\n" +
            "   \"mimetype\": \"text/x-python\",\n" +
            "   \"name\": \"python\",\n" +
            "   \"nbconvert_exporter\": \"python\",\n" +
            "   \"pygments_lexer\": \"ipython3\",\n" +
            "   \"version\": \"3.6.0\"\n" +
            "  }\n" +
            " },\n" +
            " \"nbformat\": 4,\n" +
            " \"nbformat_minor\": 2\n" +
            "}\n";
}
