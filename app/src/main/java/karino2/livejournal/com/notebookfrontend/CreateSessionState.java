package karino2.livejournal.com.notebookfrontend;

import android.os.Bundle;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by _ on 2017/05/28.
 */

public class CreateSessionState implements StateMachine.State {
    StateMachine stateMachine;
    OkHttpClient httpClient;

    public CreateSessionState(StateMachine stmac, OkHttpClient client) {
        stateMachine = stmac;
        httpClient = client;
    }


    @Override
    public void begin(Bundle bundle) {
        String name = bundle.getString("SESSION_ARG_NAME");
        String path = bundle.getString("SESSION_ARG_PATH");

        String baseUrl = stateMachine.baseHttpUrl();
        String url = baseUrl + "/api/sessions";

        String requestbody = "{\"notebook\": {\"name\":\"" + name + "\", \"path\": \"" + path +"\"}}";
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.parse("application/json") , requestbody))
                .build();

        Single.create(emitter -> {
            Response resp = httpClient.newCall(request).execute();
            String res = resp.body().string();
            emitter.onSuccess(res);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(arg-> {
                    Bundle newArg = new Bundle();
                    newArg.putString("SESSION_INFO_JSON", (String)arg);
                    stateMachine.gotoNextState(StateMachine.STATE_CONNECT_TO_KERNEL, newArg);
                });

    }
}
