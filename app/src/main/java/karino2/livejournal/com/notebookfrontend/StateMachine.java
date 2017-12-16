package karino2.livejournal.com.notebookfrontend;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Random;

import okhttp3.HttpUrl;

/**
 * Created by _ on 2017/05/28.
 */

public class StateMachine {
    public static final int STATE_NONE = 0;
    public static final int STATE_LOGIN= 1;
    public static final int STATE_GET_BASE_JSON = 2;
    public static final int STATE_CREATE_SESSION = 3;
    public static final int STATE_CONNECT_TO_KERNEL = 4;
    public static final int STATE_WAIT_MESSAGE = 5;
    public static final int STATE_SEND_REQUEST = 6;
    public static final int STATE_RESPONSE_RECDEIVE = 7;

    int currentState = STATE_NONE;
    boolean changing = false;
    Bundle stateInit = null;

    String token;

    public void setToken(String token) {
        this.token = token;
    }

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



}
