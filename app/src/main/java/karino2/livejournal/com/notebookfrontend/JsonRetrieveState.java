package karino2.livejournal.com.notebookfrontend;

import android.os.Bundle;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by _ on 2017/05/28.
 */

public class JsonRetrieveState implements  StateMachine.State {
    StateMachine stateMachine;
    OkHttpClient httpClient;

    public JsonRetrieveState(StateMachine stmac, OkHttpClient client) {
        stateMachine = stmac;
        httpClient = client;
    }

    public interface StringReceiver {
        void onStringCome(String resp);
    }

    StringReceiver onJsonReceive = json->{};

    public void setJsonReceiver(StringReceiver onReceiveListener) {
        onJsonReceive = onReceiveListener;
    }

    @Override
    public void begin(Bundle bundle) {
        String ipynbPath = bundle.getString("IPYNB_PATH");
        String baseUrl = stateMachine.baseHttpUrl();
        String url = baseUrl + "/api/notebooks/" + ipynbPath;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Single.create(emitter -> {
            Response resp = httpClient.newCall(request).execute();
            String jsonString = resp.body().string();

            emitter.onSuccess(jsonString);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(json -> {
                    onJsonReceive.onStringCome((String)json);
                    Bundle createSessionArg = new Bundle();
                    createSessionArg.putString("SESSION_ARG_NAME", ipynbPath);
                    createSessionArg.putString("SESSION_ARG_PATH", ipynbPath);
                    stateMachine.gotoNextState(StateMachine.STATE_CREATE_SESSION, createSessionArg);
                });

    }
}
