package karino2.livejournal.com.notebookfrontend;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.junit.Assert.*;

/**
 * Created by _ on 2017/06/03.
 */

public class CellTest {
    Gson createGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }


    final String CODE_CELL_JSON = "{\"cell_type\":\"code\",\"execution_count\":11,\"metadata\":{\"collapsed\":false,\"trusted\":false},\"source\":\"print(\\\"Hello\\\")\",\"outputs\":[{\"name\":\"stdout\",\"output_type\":\"stream\",\"text\":\"Hello\\n\"}]}";


    Gson gson;
    @Before
    public void init() {
        gson = createGson();
    }

    @Test
    public void desirializeTest() {
        Cell cell = gson.fromJson(CODE_CELL_JSON, Cell.class);
        assertEquals("{\"collapsed\":false,\"trusted\":false}",  gson.toJson(cell.metadata));
        assertEquals("print(\"Hello\")", cell.getSource());

    }

    @Test
    public void toJson_sourceCell_test() throws IOException {
        verityToJson(CODE_CELL_JSON, CODE_CELL_JSON);
    }

    private void verityToJson(String input, String expect) throws IOException {
        Cell cell = gson.fromJson(input, Cell.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(baos));

        cell.toJson(gson, writer);

        writer.close();
        String actual = baos.toString("UTF-8");
        assertEquals(expect, actual);
    }

    final String CODE_IMAGE_CELL_JSON = "{\"cell_type\":\"code\",\"execution_count\":4,\"metadata\":{\"collapsed\":false,\"trusted\":false},\"source\":\"from IPython.display import Image\\nImage(\\\"test.png\\\")\",\"outputs\":[{\"metadata\":{},\"output_type\":\"execute_result\",\"data\":{\"image/png\":\"iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAIAAACQkWg2AAAACXBIWXMAADXUAAA11AFeZeUIAAAA\\n70lEQVQokZWSMQ6CQBBFP4YYOmxJKCwMrYWdBdkz0MoJqOAQdDRewMYzGFuvYOEBKExoCIWJEQL5\\nFrgqumbjZIqdyX+T+btrkMQ/YQ6qpkGWYbdDVQHAeAzbxmSCIMBqBdMEAD6jLLlcElCnEKxrkhLo\\nOgrxU93nek1y9Fhmv8fhoFn/dAIggeNR7/d6fQNuNz3Qtm+AZemBIACet7TZKFwuFq9zkvRC+Q6u\\nqxgZhkhT5DmEgOf1PQk4jgI4nxHHHz3pwfNg25/AdKqY8nrpLBsYmM14ufArMKi2W/o+53NGEYvi\\nW03S+Pe3jvSSYdwBxsfDQdbYcZMAAAAASUVORK5CYII=\\n\",\"text/plain\":\"<IPython.core.display.Image object>\"},\"execution_count\":4}]}";

    // a little different, but also legal.
    final String CODE_IMAGE_CELL_SERIALIZE_EXPECTED_JSON = "{\"cell_type\":\"code\",\"execution_count\":4,\"metadata\":{\"collapsed\":false,\"trusted\":false},\"source\":\"from IPython.display import Image\\nImage(\\\"test.png\\\")\",\"outputs\":[{\"name\":\"\",\"output_type\":\"execute_result\",\"data\":{\"image/png\":\"iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAIAAACQkWg2AAAACXBIWXMAADXUAAA11AFeZeUIAAAA\\n70lEQVQokZWSMQ6CQBBFP4YYOmxJKCwMrYWdBdkz0MoJqOAQdDRewMYzGFuvYOEBKExoCIWJEQL5\\nFrgqumbjZIqdyX+T+btrkMQ/YQ6qpkGWYbdDVQHAeAzbxmSCIMBqBdMEAD6jLLlcElCnEKxrkhLo\\nOgrxU93nek1y9Fhmv8fhoFn/dAIggeNR7/d6fQNuNz3Qtm+AZemBIACet7TZKFwuFq9zkvRC+Q6u\\nqxgZhkhT5DmEgOf1PQk4jgI4nxHHHz3pwfNg25/AdKqY8nrpLBsYmM14ufArMKi2W/o+53NGEYvi\\nW03S+Pe3jvSSYdwBxsfDQdbYcZMAAAAASUVORK5CYII\\u003d\\n\",\"text/plain\":\"\\u003cIPython.core.display.Image object\\u003e\"},\"execution_count\":4}]}";
    @Test
    public void toJson_sourceCell_Image_test() throws IOException {
        verityToJson(CODE_IMAGE_CELL_JSON, CODE_IMAGE_CELL_SERIALIZE_EXPECTED_JSON);
    }

    final String MD_CELL_JSON = "{\"cell_type\":\"markdown\",\"metadata\":{\"collapsed\":true},\"source\":\"## Markdown cell\\n\\nHere is the test of markdown.\\nNext line.\"}";
    @Test
    public void testJson_markDownCell_test() throws IOException {
        verityToJson(MD_CELL_JSON, MD_CELL_JSON);
    }


    final String SAVE_MESSAGE_HEADER_EXPECT = "{\"type\":\"notebook\",\"content\":{\"cells\":[";
    final String SAVE_MESSAGE_FOOTER_EXPECT = "],\"metadata\":{\"kernelspec\":{\"name\":\"python3\",\"display_name\":\"Python 3\",\"language\":\"python\"},\"language_info\":{\"name\":\"python\",\"version\":\"3.6.0\",\"mimetype\":\"text/x-python\",\"codemirror_mode\":{\"name\":\"ipython\",\"version\":3},\"pygments_lexer\":\"ipython3\",\"nbconvert_exporter\":\"python\",\"file_extension\":\".py\"}},\"nbformat\":4,\"nbformat_minor\":2}}";

    @Test
    public void cellListSerializer_toJsonForSaveMessage_test() throws IOException {
        CellListSerializer cls = new CellListSerializer();
        cls.add(gson.fromJson(CODE_CELL_JSON, Cell.class));
        cls.add(gson.fromJson(MD_CELL_JSON, Cell.class));

        String expect = SAVE_MESSAGE_HEADER_EXPECT + CODE_CELL_JSON + "," + MD_CELL_JSON + SAVE_MESSAGE_FOOTER_EXPECT;


        String actual = cls.toJsonForSaveMessage();
        assertEquals(expect, actual);
    }

}
