package karino2.livejournal.com.notebookfrontend;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by _ on 2017/05/28.
 */

public class StateMachine {
    public static final int STATE_NONE = 0;
    public static final int STATE_LOGIN= 1;

    int currentState = STATE_NONE;
    boolean changing = false;
    Bundle stateInit = null;

    String token;

    public void setToken(String token) {
        this.token = token;
    }
    public String getToken() { return token; }


    public interface State {
        void begin(Bundle bundle);
    }


    State nullState = (bundle) -> {};

    HashMap<Integer, State> stateCollection = new HashMap<>();


    Runnable notifyWakeup = ()->{};

    public void registerWakeupHandler(Runnable listener) {
        this.notifyWakeup = listener;
    }


    int port;
    public void setPort(int port) {
        this.port = port;
    }
    public int getPort() { return port; }

    public void registerNullState(int stateVal) {
        registerState(stateVal, nullState);
    }

    public StateMachine() {
        registerNullState(STATE_NONE);
    }

    public void registerState(int stateVal, State state) {
        stateCollection.put(stateVal, state);
    }

    public boolean isChanging() { return changing; }

    public void gotoNextState(int nextState, Bundle initData) {
        changing = true;
        currentState = nextState;
        stateInit = initData;
        notifyWakeup.run();
    }

    public void gotoDone() {
        gotoNextState(STATE_NONE, null);
    }

    public void doOne() {
        if(!isChanging())
            return;

        changing = false;
        State state = stateCollection.get(currentState);
        state.begin(stateInit);
    }


    // misc utility

    public String buildUrlWithToken(String method) {
        String query = getQueryString();
        return "http://localhost:" + port + method + query;
    }

    public String buildUrl(String method) {
        return "http://localhost:" + port + method;

    }

    public boolean isCookieExist(String url) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        List<Cookie> cookies = cookieJar.loadForRequest(httpUrl);
        return !cookies.isEmpty();
    }

    @NonNull
    private String getQueryString() {
        String query = "";
        if(!token.isEmpty()) {
            query = "?token=" + token;
        }
        return query;
    }

    MyCookieJar cookieJar = null;
    public void setCookieJar(MyCookieJar jar) {
        cookieJar = jar;
    }

    public void ensureXSRFParam(Request.Builder builder, String url) {
        String xsrf = getXSRFVal(HttpUrl.parse(url));
        if(!xsrf.isEmpty()) {
            builder.addHeader("X-XSRFToken", xsrf);
        }
    }

    public String getXSRFVal(HttpUrl url) {
        return cookieJar.getXSRFVal(url);
    }


    public String baseWsUrl() {
        return "ws://localhost:"+port;
    }

    public static String uuid() {
        String hexDigits = "0123456789ABCDEF";
        StringBuffer buf = new StringBuffer();
        Random random = new Random();
        for(int i = 0;i < 32; i++) {
            int idx = random.nextInt(0x10);
            buf.append(hexDigits.charAt(idx));
        }
        return buf.toString();
    }

    public void sendRequest(String url, Request.Builder builder, Action onAfter) {
        OkHttpClient httpClient = MainActivity.getHttpClient();

        ensureXSRFParam(builder, url);

        Request request = builder.build();

        Completable.create(emitter -> {
            Response resp = httpClient.newCall(request).execute();
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onAfter);
    }


}
