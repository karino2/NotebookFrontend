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

// similar to JsonRetrieveState, maybe refactoring later.
public class FileListRetrieveState implements StateMachine.State{
    StateMachine stateMachine;
    OkHttpClient httpClient;

    public FileListRetrieveState(StateMachine stmac, OkHttpClient client) {
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
        String contentsPath = bundle.getString("CONTENTS_PATH");
        String method = "/api/contents";
        if(!contentsPath.isEmpty()) {
            method = "/api/contents/" + contentsPath;
        }

        String url = stateMachine.buildUrl(method);

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
                    stateMachine.gotoNextState(StateMachine.STATE_NONE, new Bundle());
                });

    }
}
