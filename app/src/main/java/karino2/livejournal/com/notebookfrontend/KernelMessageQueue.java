package karino2.livejournal.com.notebookfrontend;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;

/**
 * Created by _ on 2017/05/29.
 */

public class KernelMessageQueue {
    Queue<KernelMessage> queue = new LinkedList<>();
    Runnable messageArriveListener = ()->{};



    public void setMessageArriveListener(Runnable listener) {
        messageArriveListener = listener;
    }


    ObservableEmitter<KernelReply> emitter;

    Observable<KernelReply> replyObservable = Observable.create(new ObservableOnSubscribe<KernelReply>() {
        @Override
        public void subscribe(@NonNull ObservableEmitter<KernelReply> e) throws Exception {
            emitter = e;
        }
    });

    public void onReplyMessage(KernelReply reply) {
        emitter.onNext(reply);
    }

    public Observable<KernelReply> getReplyObservable() {
        return replyObservable;
    }

    public KernelMessage remove() { return queue.remove(); }

    public boolean isEmpty() { return queue.isEmpty(); }

    public Observable<KernelReply> enqueue(KernelMessage message) {
        queue.add(message);

        messageArriveListener.run();
        return replyObservable;
    }


}
