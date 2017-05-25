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

    static final String SESSION_RESPONSE = "{\"id\": \"43bbed35-0737-485a-a8c0-922e7048926f\", \"path\": \"notebooktest2.ipynb\", \"name\": null, \"type\": \"notebook\", \"kernel\": {\"id\": \"4f8148c6-8fc8-4514-bbfa-84ffa8b3dc56\", \"name\": \"python3\"}, \"notebook\": {\"path\": \"notebooktest2.ipynb\", \"name\": null}}\n";
    @Test
    public void sessionTest() {
        SessionInfo si = SessionInfo.fromJson(SESSION_RESPONSE);
        assertEquals("4f8148c6-8fc8-4514-bbfa-84ffa8b3dc56", si.kernel.id);
    }

}