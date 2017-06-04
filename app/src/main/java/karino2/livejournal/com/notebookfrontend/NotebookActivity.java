package karino2.livejournal.com.notebookfrontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import io.reactivex.Completable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotebookActivity extends Activity {

    final int REQUEST_ACTIVITY_EDIT_ID = 1;

    ArrayAdapter<Cell> listAdapter;
    StateMachine stateMachine;
    KernelMessageQueue messageQueue;

    String notebookPath;

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

        findViewById(R.id.buttonNew).setOnClickListener(v-> {
            addCellBelow(-1);
        });



        Intent intent = getIntent();
        if(intent == null) {
            showMessage("Intent null. NYI case.");
            return;
        }
        int port = intent.getIntExtra("PORT", 51234);
        notebookPath = intent.getStringExtra("IPYNB_PATH");

        setupStateMachine(port, notebookPath);

        /*


        String jsonContent = TEST_JSON_CONTENT;
        bindNewContent(jsonContent);
        */


    }

    private void addCellBelow(int selectedIdx) {
        Gson gson = Note.getGson();
        Cell newCell = gson.fromJson("{\"cell_type\":\"code\",\"source\":\"\",\"outputs\":[{\"name\":\"stdout\",\"output_type\":\"stream\",\"text\":\"\"}]}", Cell.class);
        int newIndex = selectedIdx +1;
        if(selectedIdx == -1) {
            newIndex = listAdapter.getCount();
        }
        listAdapter.insert(newCell, newIndex);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notebook_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.save_item:
                saveNotebook();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveNotebook() {
        String baseUrl = stateMachine.baseHttpUrl();
        String url = baseUrl + "/api/contents/" + notebookPath;

        // TODO: synchronize.
        CellListSerializer cls = new CellListSerializer();
        for(int i = 0; i < listAdapter.getCount(); i++) {
            cls.add(listAdapter.getItem(i));
        }

        String data;
        try {
            data = cls.toJsonForSaveMessage();
        } catch (IOException e) {
            showMessage("Fail to deserialize, never happen: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), data);


        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        Completable.create(emitter -> {
            Response resp = httpClient.newCall(request).execute();
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                   showMessage("saved.") ;
                });

    }

}
