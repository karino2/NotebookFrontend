package karino2.livejournal.com.notebookfrontend;

import java.util.List;

/**
 * Created by _ on 2017/12/17.
 */

public class Directory {
    public String type;
    public List<FileModel> content;
    public String name;
    public String path;

    public static class FileModel {
        public String type;
        public String name;
        public String path;

        /*
                            if("notebook".equals(file.type)) {
                        textView.setEnabled(true);
                    } else if("directory".equals(file.type)) {
                        textView.setEnabled(true);

         */
        public boolean isNotebook() {
            return "notebook".equals(type);
        }
        public boolean isDirectory() {
            return "directory".equals(type);
        }
    }
}
