package karino2.livejournal.com.notebookfrontend;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;


import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Random;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class DebugActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        findViewById(R.id.buttonSend).setOnClickListener(view->onSend());


    }

    public static String uuid() {
        String hexDigits = "0123456789ABCDEF";
        StringBuffer buf = new StringBuffer();
        Random random = new Random();
        for(int i = 0;i < 32; i++) {
            int idx = random.nextInt(0x10);
            buf.append(hexDigits.charAt(idx));
        }
        return buf.toString();
    }

    OkHttpClient client = new OkHttpClient();

    void onSend() {
        int port = Integer.parseInt(((EditText)findViewById(R.id.editTextPort)).getText().toString());
        String token = ((EditText)findViewById(R.id.editTextToken)).getText().toString();
        // http://localhost:52688/notebooks/features.ipynb



        String requestbody = "{\"notebook\": {\"name\":\"test.ipynb\", \"path\": \"test.ipynb\"}}";
        Request request = new Request.Builder()
                .url("http://localhost:52688/api/sessions")
                .post(RequestBody.create(MediaType.parse("application/json") , requestbody))
                .build();




            Single.create(emitter -> {
                try {
                    Response resp = client.newCall(request).execute();
                    String res = resp.body().string();

                    /*


                    // final String urlstr = "http://localhost:52688/notebooks/features.ipynb";
                    final String urlstr = "http://localhost:52688/api/notebooks/features.ipynb";
                    URL url = new URL(urlstr);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    int code = connection.getResponseCode();
                    Log.d("NotebookFE", "code: " + code);
                    if(code != 200) {
                        emitter.onError(new RuntimeException("code not 200"));
                        return;
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuffer buf = new StringBuffer();

                    String line;
                    while( (line = br.readLine()) != null ) {
                        buf.append(line);
                    }
                    br.close();
                    String res = buf.toString();
                    */

                    emitter.onSuccess(res);
                    return;

                } catch (MalformedURLException e) {
                    emitter.onError(e);
                    e.printStackTrace();
                } catch (IOException e) {
                    emitter.onError(e);
                    e.printStackTrace();
                }
            }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(cont -> {
                        String json = (String)cont;
                        debugPrint(json);
                        SessionInfo si = SessionInfo.fromJson(json);
                        debugPrint("kernelID: " + si.kernel.id);
                        connectWS(si);
            });
    }


    class KernelListener extends WebSocketListener {

        SessionInfo sessionInfo;
        String sessionId;
        KernelListener(String sesid, SessionInfo si) {
            this.sessionInfo = si;
            sessionId = sesid;
        }


        @Override
        public void onOpen(WebSocket webSocket, Response response) {

            String exemsg = "{\"header\":{\"msg_id\":\"" + uuid() + "\",\"username\":\"username\",\"session\":\"" + sessionId + "\",\"msg_type\":\"execute_request\",\"version\":\"5.0\"},\"metadata\":{},\"content\":{\"code\":\"print(\\\"Hello\\\")\",\"silent\":false,\"store_history\":true,\"user_expressions\":{},\"allow_stdin\":true,\"stop_on_error\":true},\"buffers\":[],\"parent_header\":{},\"channel\":\"shell\"}";
            webSocket.send(exemsg);
            debugPrintTS("sentmsg: " + exemsg);
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            debugPrintTS("onMsg: " + text);
        }
    }

    void debugPrintTS(String msg) {
        Single.create(emitter -> {
            debugPrint(msg);
        }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    void connectWS(SessionInfo si) {
        String sessionid = uuid();
        String url = "ws://localhost:52688/api/kernels/" + si.kernel.id + "/channels?session_id=" + sessionid;

        Request request = new Request.Builder()
                .url(url)
                .build();

        Single.create(emitter -> {
            KernelListener listener = new KernelListener(sessionid, si);
            client.newWebSocket(request, listener);
        }).subscribeOn(Schedulers.io())
        .subscribe();





    }


    void debugPrint(String msg) {
        EditText et = (EditText)findViewById(R.id.editTextConsole);
        String res = msg+ "\n" + et.getText().toString();
        et.setText(res);
    }
}
