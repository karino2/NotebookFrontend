package karino2.livejournal.com.notebookfrontend;

import java.util.Collection;
import java.util.Map;

/**
 * Created by _ on 2017/05/24.
 */

public class Cell {
    String cellType;
    Integer executionCount;
    // metadata

    // always 1 element.
    Collection<Outputs> outputs;

    public static class Outputs {
        public String outputType;

        // name, text or data
        public String name = "";
        public String text = "";
        public Map<String, String> data;

    }
}
