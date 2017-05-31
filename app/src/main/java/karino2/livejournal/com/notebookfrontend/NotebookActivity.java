package karino2.livejournal.com.notebookfrontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;

public class NotebookActivity extends Activity {

    final int REQUEST_ACTIVITY_EDIT_ID = 1;

    ArrayAdapter<Cell> listAdapter;
    StateMachine stateMachine;
    KernelMessageQueue messageQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);

        ListView lv = (ListView)findViewById(R.id.listView);



        listAdapter = new ArrayAdapter<Cell>(this, 0) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                CellView cellView;
                if(convertView == null) {
                    cellView = (CellView)getLayoutInflater().inflate(R.layout.cell_item, null);
                } else {
                    cellView = (CellView)convertView;
                }

                Cell cell = getItem(position);
                cellView.bindCell(cell);
                return cellView;
            }
        };
        lv.setAdapter(listAdapter);
        stateMachine = new StateMachine();

        messageQueue = new KernelMessageQueue();

        lv.setOnItemClickListener((lview, cellview, pos, id)-> {
            CellView cv = (CellView)cellview;
            Intent editintent= new Intent(this, EditActivity.class);
            editintent.putExtra("CELL_POSITION", pos);
            editintent.putExtra("CELL_CONTENT", cv.getBoundCell().getSource());
            startActivityForResult(editintent, REQUEST_ACTIVITY_EDIT_ID);
        });



        Intent intent = getIntent();
        if(intent == null) {
            showMessage("Intent null. NYI case.");
            return;
        }
        int port = intent.getIntExtra("PORT", 51234);
        String path = intent.getStringExtra("IPYNB_PATH");

        setupStateMachine(port, path);

        /*


        String jsonContent = TEST_JSON_CONTENT;
        bindNewContent(jsonContent);
        */


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ACTIVITY_EDIT_ID) {
            int cellpos = data.getIntExtra("CELL_POSITION", -1);
            String content = data.getStringExtra("CELL_CONTENT");
            Cell cell = listAdapter.getItem(cellpos);
            cell.setSource(content);
            cell.clearOutput();

            // use OK as exec, cancel as just save. a little tricky.
            if(resultCode == RESULT_OK && cell.getCellType() == Cell.CellType.CODE) {
                executeCell(cell, content);
            }
            listAdapter.notifyDataSetChanged();
        }
    }

    private void executeCell(final Cell cell, String content) {
        cell.setExecutionCount(Cell.EXEC_COUNT_RUNNING);
        String msgid = StateMachine.uuid();

        messageQueue.enqueue(new KernelMessage(msgid, "", content))
                .filter(reply -> reply.getParentMessageId().equals(msgid))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<KernelReply>() {
            Disposable disp;
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                disp = d;
            }

            @Override
            public void onNext(@NonNull KernelReply kernelReply) {
                handleKernelReply(kernelReply, cell, disp);
            }

            @Override
            public void onError(@NonNull Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    void handleKernelReply(KernelReply reply, Cell cell, Disposable disposable) {
        JsonObject content = reply.getContent();
        String repmsgtype = reply.getMessageType();
        if("execute_input".equals(repmsgtype)) {
            cell.setExecutionCount(content.get("execution_count").getAsInt());
            listAdapter.notifyDataSetChanged();
        }else if("stream".equals(repmsgtype)) {
            cell.getOutput().appendResult(content.get("text").getAsString());
            listAdapter.notifyDataSetChanged();
        } else if("execute_result".equals(repmsgtype)) {
            cell.getOutput().setData(content.get("data").getAsJsonObject());
            listAdapter.notifyDataSetChanged();
        }else if("execute_reply".equals(repmsgtype)) {
            disposable.dispose();
        }

    }

    OkHttpClient httpClient = new OkHttpClient();


    private void setupStateMachine(int port, String path) {
        stateMachine.setPort(port);

        JsonRetrieveState jsonState = new JsonRetrieveState(stateMachine, httpClient);
        jsonState.setJsonReceiver(json-> {
            bindNewContent(json);
        });
        stateMachine.registerState(StateMachine.STATE_GET_BASE_JSON, jsonState);
        stateMachine.registerState(StateMachine.STATE_CREATE_SESSION, new CreateSessionState(stateMachine, httpClient));
        stateMachine.registerState(StateMachine.STATE_CONNECT_TO_KERNEL, new ConnectToKernelState(stateMachine, httpClient, sesid-> new Kernel(sesid, messageQueue)));
        stateMachine.registerState(StateMachine.STATE_WAIT_MESSAGE, new StateMachine.State() {
            @Override
            public void begin(Bundle bundle) {
                showMessage("wait message start!");
            }
        });

        // strange.
        stateMachine.registerWakeupHandler(()-> {
            if(stateMachine.isChanging()) {
                stateMachine.doOne();
            }
        });


        Bundle bundle = new Bundle();
        bundle.putString("IPYNB_PATH", path);
        stateMachine.gotoNextState(StateMachine.STATE_GET_BASE_JSON, bundle);
    }

    class KernelFactory implements Function<String, Kernel> {

        @Override
        public Kernel apply(String s) {
            return createKernel(s);
        }
    }

    Kernel createKernel(String sessionId) {
        return new Kernel(sessionId, messageQueue);
    }

    void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void bindNewContent(String jsonContent) {
        Note note = Note.fromJson(jsonContent);
        if (note.content == null) {
            showMessage("Invalid json retreive: " + jsonContent);
            return;

        }
        listAdapter.addAll(note.content.cells);
    }

    public static final String TEST_JSON_CONTENT = "{\"mimetype\": null, \"format\": \"json\", \"type\": \"notebook\", \"writable\": true, \"path\": \"test.ipynb\", \"content\": {\"metadata\": {\"kernelspec\": {\"display_name\": \"Python 3\", \"name\": \"python3\", \"language\": \"python\"}, \"language_info\": {\"mimetype\": \"text/x-python\", \"nbconvert_exporter\": \"python\", \"pygments_lexer\": \"ipython3\", \"version\": \"3.5.2\", \"file_extension\": \".py\", \"codemirror_mode\": {\"version\": 3, \"name\": \"ipython\"}, \"name\": \"python\"}}, \"nbformat_minor\": 0, \"nbformat\": 4, \"cells\": [{\"metadata\": {\"collapsed\": false, \"trusted\": true}, \"outputs\": [{\"output_type\": \"stream\", \"text\": \"Hello\\n\", \"name\": \"stdout\"}], \"source\": \"print(\\\"Hello\\\")\", \"cell_type\": \"code\", \"execution_count\": 1}, {\"metadata\": {\"collapsed\": true, \"trusted\": true}, \"outputs\": [], \"source\": \"\", \"cell_type\": \"code\", \"execution_count\": null}]}, \"created\": \"2017-05-30T03:39:14.787459+00:00\", \"last_modified\": \"2017-05-30T03:39:14.787459+00:00\", \"name\": \"test.ipynb\"}\n" +
            "\n";

}
