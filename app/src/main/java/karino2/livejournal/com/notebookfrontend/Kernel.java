package karino2.livejournal.com.notebookfrontend;

import com.google.gson.Gson;

import java.util.Queue;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
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

    boolean isHandling = false;
    void notifyMessageArrive() {
        if(!isReady() || isHandling || messageQueue.isEmpty())
            return;
        isHandling = true;
        Queue<KernelMessage> queue = messageQueue.detach();
        Observable.fromIterable(queue)
                    .subscribeOn(Schedulers.io())
                .subscribe(new Observer<KernelMessage>() {
                               @Override
                               public void onSubscribe(@NonNull Disposable d) {

                               }

                               @Override
                               public void onNext(@NonNull KernelMessage msg) {
                                   msg.setSessionId(sessionId);
                                   webSocket.send(msg.toJson());
                               }

                               @Override
                               public void onError(@NonNull Throwable e) {
                                   isHandling = false;
                               }

                               @Override
                               public void onComplete() {
                                   isHandling = false;
                                   notifyMessageArrive();
                               }
                           });
    }

}
