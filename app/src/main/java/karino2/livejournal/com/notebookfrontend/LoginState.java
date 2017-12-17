package karino2.livejournal.com.notebookfrontend;

import android.os.Bundle;
import android.util.Log;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by _ on 2017/12/16.
 */

public class LoginState implements StateMachine.State  {
    StateMachine stateMachine;
    OkHttpClient httpClient;

    public LoginState(StateMachine stmac, OkHttpClient client) {
        stateMachine = stmac;
        httpClient = client;
    }

    @Override
    public void begin(Bundle bundle) {

        int nextState = bundle.getInt("NEXT_STATE");

        // send request to tree to get cookie.
        String url = stateMachine.buildUrlWithToken("/tree");

        if(stateMachine.isCookieExist(url)) {
            // already logined. goto next state.

            // pass through bundle (IPYNB_PATH).
            stateMachine.gotoNextState(nextState, bundle);
            return;
        }


        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Log.d("NotebookFrontend", "login request begin");

        Single.create(emitter -> {
            Response resp = httpClient.newCall(request).execute();
            // String jsonString = resp.body().string();

            Log.d("NotebookFrontend", "login response come");
            emitter.onSuccess("dummy");
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(json -> {
                    Log.d("NotebookFrontend", "login done");

                    // pass through bundle (IPYNB_PATH).
                    stateMachine.gotoNextState(nextState, bundle);
                });

    }
}
