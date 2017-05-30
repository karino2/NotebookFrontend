package karino2.livejournal.com.notebookfrontend;

import com.google.gson.Gson;

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

    boolean isWaiting = false;

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
            handleOneMessage();
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
        if(isWaiting || isReady())
            return;
        handleOneMessage();
    }

    void handleOneMessage() {
        isWaiting = true;

        KernelMessage msg = messageQueue.remove();
        Completable.create(emitter -> {
            webSocket.send(msg.toJson());
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(()-> {
                    isWaiting = false;
                    if(!messageQueue.isEmpty())
                        handleOneMessage();
                });

    }
}
