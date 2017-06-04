package karino2.livejournal.com.notebookfrontend;

import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by _ on 2017/05/29.
 */

public class Kernel extends WebSocketListener {
    WebSocket webSocket;
    KernelMessageQueue messageQueue;

    String sessionId;


    public Kernel(String sessionId, KernelMessageQueue queue) {
        this.sessionId = sessionId;

        messageQueue = queue;
        messageQueue.setMessageArriveListener(()->notifyMessageArrive());
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        this.webSocket = webSocket;

        if(!messageQueue.isEmpty())
            notifyMessageArrive();
   }

   Gson gson = new Gson();

    @Override
    public void onMessage(WebSocket webSocket, String text)
    {
        KernelReply reply = gson.fromJson(text, KernelReply.class);
        messageQueue.onReplyMessage(reply);
        // debugPrintTS("onMsg: " + text);
    }
   boolean isReady() {
       return webSocket != null;
   }

    void notifyMessageArrive() {
        if(!isReady())
            return;
        long delayms = 0;
        while(!messageQueue.isEmpty()){
            KernelMessage msg = messageQueue.remove();
            msg.setSessionId(sessionId);

            Completable.fromAction(() -> {
                webSocket.send(msg.toJson());
            }).delay(delayms, TimeUnit.MILLISECONDS)
              .subscribeOn(Schedulers.io())
              .subscribe();
            delayms = 100;
        }
    }

}
