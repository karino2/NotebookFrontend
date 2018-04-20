package karino2.livejournal.com.notebookfrontend;

import android.os.Bundle;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by _ on 2017/05/28.
 */

public class ConnectToKernelState implements StateMachine.State {
    NotebookStateMachine stateMachine;
    OkHttpClient httpClient;

    Function<String, Kernel> kernelFactory;

    public ConnectToKernelState(NotebookStateMachine stmac, OkHttpClient client, Function<String, Kernel> kernelFactory) {
        stateMachine = stmac;
        httpClient = client;
        this.kernelFactory = kernelFactory;
    }

    String sessionId;
    SessionInfo sessionInfo;

    Kernel kernel;

    // use for reconnect
    String argJson;

    @Override
    public void begin(Bundle bundle) {

        boolean isReconnect = bundle.getBoolean("IS_RECONNECT", false);
        if(!isReconnect) {
            argJson = bundle.getString("SESSION_INFO_JSON");
            sessionInfo = SessionInfo.fromJson(argJson);
            stateMachine.notifySessionInfo(sessionInfo);

            sessionId = StateMachine.uuid();
        }

        String baseUrl = stateMachine.baseWsUrl();
        String url = baseUrl + "/api/kernels/" + sessionInfo.kernel.id + "/channels?session_id=" + sessionId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        Completable.create(emitter -> {
            if(!isReconnect) {
                kernel = kernelFactory.apply(sessionId);
            }
            httpClient.newWebSocket(request, kernel);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Bundle newArg = new Bundle();
                    newArg.putString("SESSION_ID", sessionId);
                    stateMachine.gotoNextState(NotebookStateMachine.STATE_WAIT_MESSAGE, newArg);
                });



    }
}
