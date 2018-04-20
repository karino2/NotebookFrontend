package karino2.livejournal.com.notebookfrontend;

import android.accounts.NetworkErrorException;
import android.util.Log;

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
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        Log.d("NotebookFrontend", "onClosing");
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        // called.
        Log.d("NotebookFrontend", "onClosed");
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        Log.d("NotebookFrontend", "onClosed");
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
                                   boolean isSuccess = webSocket.send(msg.toJson());
                                   Log.d("NotebookFrontend", "Is success? " + isSuccess);
                                   if(!isSuccess) {
                                       messageQueue.onError(new NetworkErrorException("send fail"));
                                   }
                               }

                               @Override
                               public void onError(@NonNull Throwable e) {
                                   isHandling = false;
                                   // Log.d("NotebookFrontend", "on error");
                               }

                               @Override
                               public void onComplete() {
                                   isHandling = false;
                                   notifyMessageArrive();
                               }
                           });
    }

}
