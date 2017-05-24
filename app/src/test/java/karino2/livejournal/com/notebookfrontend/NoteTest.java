package karino2.livejournal.com.notebookfrontend;

import com.google.gson.Gson;

import org.junit.Test;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        InputStream in = getClass().getClassLoader().getResourceAsStream("test_response.json");
        Note note = Note.fromJson(in);
        assertEquals("notebooktest.ipynb", note.name);
        assertEquals(dateFrom("2017-05-24T03:49:48.038970+00:00"), note.lastModified);
        assertEquals(dateFrom("2017-05-18T08:16:35.851660+00:00"), note.created);

    }
}