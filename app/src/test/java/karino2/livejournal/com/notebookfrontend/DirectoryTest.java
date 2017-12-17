package karino2.livejournal.com.notebookfrontend;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by _ on 2017/12/17.
 */

public class DirectoryTest {
    @Test
    public void testJsonParse() throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("test_tree_response.json");
        String json = Note.readAll(in);

        Gson gson = new Gson();
        Directory result = gson.fromJson(json, Directory.class);

        assertNotNull(result.content);
    }
}
