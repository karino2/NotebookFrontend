package karino2.livejournal.com.notebookfrontend;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class NoteTest {
    static Date dateFrom(String isoformat) throws ParseException {
        Gson gson = new Gson();
        return gson.fromJson("\"" + isoformat + "\"", Date.class);
        /*
        DateTimeFormatter
        SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        return sourceFormat.parse(isoformat);
        */
    }
    @Test
    public void fromJson_basic_test() throws Exception {
        Note note = getTestNote();
        assertEquals("notebooktest.ipynb", note.name);
        assertEquals(dateFrom("2017-05-24T03:49:48.038970+00:00"), note.lastModified);
        assertEquals(dateFrom("2017-05-18T08:16:35.851660+00:00"), note.created);

    }

    private Note getTestNote() throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("test_response.json");
        return Note.fromJson(in);
    }

    @Test
    public void fromJson_cell_test() throws IOException {
        Note note = getTestNote();

        List<Cell> content = note.content.cells;
        assertEquals(5, content.size());

        Cell cell = content.get(0);
        assertEquals(Cell.CellType.CODE, cell.getCellType());
        assertEquals("print(\"Hello\")", cell.getSource());

        cell = content.get(2);
        assertEquals(Cell.CellType.MARKDOWN, cell.getCellType());
        assertEquals("## Markdown cell\n\nHere is the test of markdown.\nNext line.", cell.getSource());



    }
    @Test
    public void fromJson_outputText_test() throws IOException {
        Note note = getTestNote();

        Cell cell = note.content.cells.get(0);
        Cell.Output output = cell.getOutput();

        assertFalse(output.isImage());
        assertEquals("Hello\n", output.getText());


    }

    @Test
    public void fromJson_outputImage_test() throws IOException {
        Note note = getTestNote();

        Cell cell = note.content.cells.get(3);
        Cell.Output output = cell.getOutput();

        assertTrue(output.isImage());
        assertTrue(output.getImageAsBase64().length() > 0);


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


    @Test
    public void TEST_JSON_CONTENT_parseTest() {
        // wrong test format. put here for note purpose.
        String json = TEST_JSON_CONTENT;
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        JsonObject contentjson = gson.fromJson(json, JsonObject.class);
        JsonArray cellsJson = contentjson.get("cells").getAsJsonArray();

        List<Cell> cells = new ArrayList<Cell>();
        for(JsonElement elem : cellsJson) {
            Cell one = gson.fromJson(elem, Cell.class);
            cells.add(one);
        }
        assertEquals(5, cells.size());
    }

    static final String SESSION_RESPONSE = "{\"id\": \"43bbed35-0737-485a-a8c0-922e7048926f\", \"path\": \"notebooktest2.ipynb\", \"name\": null, \"type\": \"notebook\", \"kernel\": {\"id\": \"4f8148c6-8fc8-4514-bbfa-84ffa8b3dc56\", \"name\": \"python3\"}, \"notebook\": {\"path\": \"notebooktest2.ipynb\", \"name\": null}}\n";
    @Test
    public void sessionTest() {
        SessionInfo si = SessionInfo.fromJson(SESSION_RESPONSE);
        assertEquals("4f8148c6-8fc8-4514-bbfa-84ffa8b3dc56", si.kernel.id);
    }


    @Test
    public void messageToJsonTest() {
        String expect = "{\"header\":{\"msg_id\":\"66F42110529DA3FB465C41131CDA7E97\",\"username\":\"username\",\"session\":\"53E06D4E1777ECF2D9D324F2C94701DA\",\"msg_type\":\"execute_request\",\"version\":\"5.0\"},\"metadata\":{},\"content\":{\"code\":\"print(\\\"Hello\\\")\",\"silent\":false,\"store_history\":true,\"user_expressions\":{},\"allow_stdin\":true,\"stop_on_error\":true},\"buffers\":[],\"parent_header\":{},\"channel\":\"shell\"}";

        KernelMessage message = new KernelMessage("66F42110529DA3FB465C41131CDA7E97", "53E06D4E1777ECF2D9D324F2C94701DA", "print(\"Hello\")");

        String actual = message.toJson();
        assertEquals(expect, actual);

    }

    @Test
    public void messageReplyParseTest() {
        String input = "{\"metadata\": {\"status\": \"ok\", \"started\": \"2017-05-25T12:36:30.944747\", \"engine\": \"4c748c84-62c1-418d-819a-323e7687a3f3\", \"dependencies_met\": true}, \"msg_type\": \"execute_reply\", \"msg_id\": \"a45bb0ff-950b-4c8c-b904-d48d73928d82\", \"parent_header\": {\"username\": \"username\", \"msg_type\": \"execute_request\", \"version\": \"5.0\", \"msg_id\": \"66F42110529DA3FB465C41131CDA7E97\", \"session\": \"53E06D4E1777ECF2D9D324F2C94701DA\", \"date\": \"2017-05-25T12:36:30.944332\"}, \"content\": {\"status\": \"ok\", \"payload\": [], \"user_expressions\": {}, \"execution_count\": 2}, \"channel\": \"shell\", \"buffers\": [], \"header\": {\"username\": \"_\", \"msg_type\": \"execute_reply\", \"version\": \"5.0\", \"msg_id\": \"a45bb0ff-950b-4c8c-b904-d48d73928d82\", \"session\": \"cf4feba4-ff39-4eb1-a05e-d232b4eaf444\", \"date\": \"2017-05-25T12:36:30.946914\"}}";

        Gson gson = new Gson();
        KernelReply reply = gson.fromJson(input, KernelReply.class);
        assertEquals("execute_reply", reply.getMessageType());
        assertEquals("66F42110529DA3FB465C41131CDA7E97", reply.getParentMessageId());
    }

}