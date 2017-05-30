package karino2.livejournal.com.notebookfrontend;

import android.os.Bundle;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocketListener;

/**
 * Created by _ on 2017/05/28.
 */

public class ConnectToKernelState implements StateMachine.State {
    StateMachine stateMachine;
    OkHttpClient httpClient;

    Function<String, Kernel> kernelFactory;

    public ConnectToKernelState(StateMachine stmac, OkHttpClient client, Function<String, Kernel> kernelFactory) {
        stateMachine = stmac;
        httpClient = client;
        this.kernelFactory = kernelFactory;
    }

    String sessionId;
    SessionInfo sessionInfo;


    Kernel kernel;

    @Override
    public void begin(Bundle bundle) {
        String argJson = bundle.getString("SESSION_INFO_JSON");
        sessionInfo = SessionInfo.fromJson(argJson);

        sessionId = StateMachine.uuid();
        String baseUrl = stateMachine.baseWsUrl();
        String url = baseUrl + "/api/kernels/" + sessionInfo.kernel.id + "/channels?session_id=" + sessionId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        Completable.create(emitter -> {
            kernel = kernelFactory.apply(sessionId);
            httpClient.newWebSocket(request, kernel);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Bundle newArg = new Bundle();
                    newArg.putString("SESSION_ID", sessionId);
                    stateMachine.gotoNextState(StateMachine.STATE_WAIT_MESSAGE, newArg);
                });



    }
}
