package karino2.livejournal.com.notebookfrontend;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by _ on 2017/05/29.
 */

public class KernelMessage {
    String msgId;
    String sessionId;

    public String getMsgId() { return msgId; }

    String pythonCode;

    public KernelMessage(String msgid, String sessionid, String pycode) {
        msgId = msgid;
        sessionId = sessionid;
        pythonCode = pycode;
    }

    public String toJson() {
        //"{\"header\":{\"msg_id\":\"" + uuid() + "\",\"username\":\"username\",\"session\":\"" + sessionId + "\",\"msg_type\":\"execute_request\",\"version\":\"5.0\"},\"metadata\":{},\"content\":{\"code\":\"print(\\\"Hello\\\")\",\"silent\":false,\"store_history\":true,\"user_expressions\":{},\"allow_stdin\":true,\"stop_on_error\":true},\"buffers\":[],\"parent_header\":{},\"channel\":\"shell\"}";
        ByteArrayOutputStream bstream = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(bstream));
        try {
            writer.beginObject();

            // {\"msg_id\":\"" + uuid() + "\",\"username\":\"username\",\"session\":\"" + sessionId + "\",\"msg_type\":\"execute_request\",\"version\":\"5.0\"}
            writer.name("header");
            writer.beginObject()
                    .name("msg_id").value(msgId)
                    .name("username").value("username")
                    .name("session").value(sessionId)
                    .name("msg_type").value("execute_request")
                    .name("version").value("5.0")
                    .endObject();


            // \"metadata\":{},
            writer.name("metadata")
                    .beginObject()
                    .endObject();


            // \"content\":{\"code\":\"print(\\\"Hello\\\")\",\"silent\":false,\"store_history\":true,\"user_expressions\":{},\"allow_stdin\":true,\"stop_on_error\":true},
            writer.name("content")
                    .beginObject()
                    .name("code").value(pythonCode)
                    .name("silent").value(false)
                    .name("store_history").value(true)
                    .name("user_expressions").beginObject().endObject()
                    .name("allow_stdin").value(true)
                    .name("stop_on_error").value(true)
                    .endObject();

            // \"buffers\":[],\"parent_header\":{},\"channel\":\"shell\"}"
            writer.name("buffers").beginArray().endArray();
            writer.name("parent_header").beginObject().endObject();
            writer.name("channel").value("shell");

            writer.endObject();
            writer.close();
            return bstream.toString("UTF-8");
        } catch (IOException e) {
            // never happen in this case.
            e.printStackTrace();
            return "";
        }
    }



    //"{\"header\":{\"msg_id\":\"" + uuid() + "\",\"username\":\"username\",\"session\":\"" + sessionId + "\",\"msg_type\":\"execute_request\",\"version\":\"5.0\"},\"metadata\":{},\"content\":{\"code\":\"print(\\\"Hello\\\")\",\"silent\":false,\"store_history\":true,\"user_expressions\":{},\"allow_stdin\":true,\"stop_on_error\":true},\"buffers\":[],\"parent_header\":{},\"channel\":\"shell\"}";

    /*

    static class Header {
        // {\"msg_id\":\"" + uuid() + "\",\"username\":\"username\",\"session\":\"" + sessionId + "\",\"msg_type\":\"execute_request\",\"version\":\"5.0\"}
        String msg_id;
        String username = "username";
        String session;
        String msg_type = "execute_request";
        String version  = "5.0";
        Header(String msgId, String sessionId) {
            msg_id = msgId;
            session = sessionId;
        }
    }

    Header header;

    */

}
