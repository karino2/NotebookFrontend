package karino2.livejournal.com.notebookfrontend;

import com.google.gson.JsonObject;

/**
 * Created by _ on 2017/05/29.
 */

public class KernelReply {
    JsonObject metadata;
    String msg_type;
    JsonObject parent_header;
    JsonObject content;
    JsonObject header;

    public String getParentMessageId() {
        return parent_header.get("msg_id").getAsString();
    }

    public String getMessageType() {
        return msg_type;
    }

}
