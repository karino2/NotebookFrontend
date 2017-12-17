package karino2.livejournal.com.notebookfrontend;

/**
 * Created by _ on 2017/12/16.
 */

public class NotebookStateMachine extends StateMachine {
    public static final int STATE_GET_BASE_JSON = 2;
    public static final int STATE_CREATE_SESSION = 3;
    public static final int STATE_CONNECT_TO_KERNEL = 4;
    public static final int STATE_WAIT_MESSAGE = 5;
    public static final int STATE_SEND_REQUEST = 6;
    public static final int STATE_RESPONSE_RECDEIVE = 7;

}
