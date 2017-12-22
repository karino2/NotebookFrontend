package karino2.livejournal.com.notebookfrontend;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    final int RENAME_DIALOG_ID = 2;

    ArrayAdapter<Cell> listAdapter;
    NotebookStateMachine stateMachine;
    KernelMessageQueue messageQueue;

    String notebookPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);

        ListView lv = getListView();


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
        stateMachine = new NotebookStateMachine();

        messageQueue = new KernelMessageQueue();

        lv.setOnItemClickListener((lview, cellview, pos, id)-> {
            CellView cv = (CellView)cellview;
            Intent editintent= new Intent(this, EditActivity.class);
            editintent.putExtra("CELL_POSITION", pos);
            editintent.putExtra("CELL_CONTENT", cv.getBoundCell().getSource());
            startActivityForResult(editintent, REQUEST_ACTIVITY_EDIT_ID);
        });

        lv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(createMultiChoiceModeListener());


        findViewById(R.id.buttonNew).setOnClickListener(v-> {
            insertNewCellBelow(-1);
        });



        Intent intent = getIntent();
        if(intent == null) {
            showMessage("Intent null. NYI case.");
            return;
        }
        int port = intent.getIntExtra("PORT", 51234);
        notebookPath = intent.getStringExtra("IPYNB_PATH");

        getActionBar().setTitle(notebookPath);

        String token = intent.getStringExtra("TOKEN");


        setupStateMachine(port, notebookPath, token);
    }

    private ListView getListView() {
        return (ListView)findViewById(R.id.listView);
    }

    List<Cell> cutCells = new ArrayList<Cell>();

    @NonNull
    private AbsListView.MultiChoiceModeListener createMultiChoiceModeListener() {
        return new AbsListView.MultiChoiceModeListener() {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.notebook_context_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch(menuItem.getItemId()) {
                    case R.id.cut_item:
                    {
                        isLastCutX = false;
                        cutCells.clear();
                        collectSelectedCells(cutCells);
                        for(Cell cell : cutCells) {
                            listAdapter.remove(cell);
                        }
                        actionMode.finish();
                        return true;
                    }
                    case R.id.exec_item:
                        ArrayList<Cell> cells = new ArrayList<>();
                        collectSelectedCells(cells);
                        for(Cell cell : cells) {
                            executeCell(cell);
                        }
                        actionMode.finish();
                        return true;
                    case R.id.change_item: {
                        int pos = getSelectedCellPosition();
                        Cell selected = listAdapter.getItem(pos);
                        Cell.CellType curtype = selected.getCellType();

                        // I only suppose two cell type. If it becomes false, then we need to add list to select which cell type.
                        if(curtype == Cell.CellType.MARKDOWN) {
                            selected.setCellType(Cell.CellType.CODE);
                        } else {
                            selected.setCellType(Cell.CellType.MARKDOWN);
                        }
                        listAdapter.notifyDataSetChanged();
                        actionMode.finish();
                        return true;
                    }
                    case R.id.paste_item: {
                        pastCellsAt(getSelectedCellPosition());
                        actionMode.finish();
                        return true;
                    }
                    case R.id.insert_above_item: {
                        insertNewCellAbove(getSelectedCellPosition());
                        actionMode.finish();
                        return true;
                    }
                    case R.id.insert_below_item: {
                        insertNewCellBelow(getSelectedCellPosition());
                        actionMode.finish();
                        return true;
                    }
                }
                showMessage("NYI");
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }

            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                if (getListView().getCheckedItemCount() != 1) {
                    actionMode.getMenu().findItem(R.id.change_item).setVisible(false);
                    actionMode.getMenu().findItem(R.id.paste_item).setVisible(false);
                    actionMode.getMenu().findItem(R.id.insert_above_item).setVisible(false);
                    actionMode.getMenu().findItem(R.id.insert_below_item).setVisible(false);
                } else {
                    actionMode.getMenu().findItem(R.id.change_item).setVisible(true);
                    actionMode.getMenu().findItem(R.id.paste_item).setVisible(true);
                    actionMode.getMenu().findItem(R.id.insert_above_item).setVisible(true);
                    actionMode.getMenu().findItem(R.id.insert_below_item).setVisible(true);
                }
            }
        };
    }

    private void collectSelectedCells(List<Cell> cells) {
        ArrayList<Integer> cands = new ArrayList<>();

        SparseBooleanArray sba = getListView().getCheckedItemPositions();
        for(int i = 0; i < sba.size(); i++) {
            if(sba.valueAt(i)) {
                cands.add(sba.keyAt(i));
            }
        }
        Collections.sort(cands);
        for(int idx : cands) {
            cells.add(listAdapter.getItem(idx));
        }
    }

    int getSelectedCellPosition() {
        SparseBooleanArray sba = getListView().getCheckedItemPositions();
        for(int i = 0; i < sba.size(); i++) {
            if(sba.valueAt(i)) {
                return sba.keyAt(i);
            }
        }
        return -1;
    }

    private void insertNewCellBelow(int selectedIdx) {
        int newIndex = selectedIdx +1;
        if(selectedIdx == -1) {
            newIndex = listAdapter.getCount();
        }
        insertNewCell(newIndex);
    }
    private void insertNewCellAbove(int selectedIdx) {
        int newIndex = selectedIdx;
        if(selectedIdx == -1) {
            newIndex = 0;
        }
        insertNewCell(newIndex);
    }

    private void insertNewCell(int newIndex) {
        Gson gson = Note.getGson();
        Cell newCell = gson.fromJson("{\"cell_type\":\"code\",\"source\":\"\",\"outputs\":[{\"name\":\"stdout\",\"output_type\":\"stream\",\"text\":\"\"}]}", Cell.class);
        listAdapter.insert(newCell, newIndex);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ACTIVITY_EDIT_ID) {
            int cellpos = data.getIntExtra("CELL_POSITION", -1);
            String content = data.getStringExtra("CELL_CONTENT");
            Cell cell = listAdapter.getItem(cellpos);
            cell.setSource(content);
            if(cell.getCellType() == Cell.CellType.CODE) {
                cell.clearOutput();

                // use OK as exec, cancel as just save. a little tricky.
                if (resultCode == RESULT_OK) {
                    executeCell(cell);
                }
            }
            listAdapter.notifyDataSetChanged();
        }
    }

    boolean isLastCutX = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean withShift = ((event.getModifiers() & KeyEvent.META_SHIFT_ON) != 0);

        switch(event.getKeyCode()) {
            case KeyEvent.KEYCODE_A:
            {
                int pos = getListView().getSelectedItemPosition();
                if(pos == ListView.INVALID_POSITION) {
                    pos = 0;
                }
                insertNewCellAbove(pos);
                return true;
            }
            case KeyEvent.KEYCODE_B:
            {
                int pos = getListView().getSelectedItemPosition();
                if(pos == ListView.INVALID_POSITION) {
                    pos = -1;
                }
                insertNewCellBelow(pos);
                return true;
            }
            case KeyEvent.KEYCODE_X:
            {
                int pos = getListView().getSelectedItemPosition();
                if(pos == ListView.INVALID_POSITION) {
                    return false;
                }
                if(!isLastCutX)
                    cutCells.clear();
                Cell cell = listAdapter.getItem(pos);
                listAdapter.remove(cell);
                cutCells.add(cell);
                isLastCutX = true;
                return true;
            }
            case KeyEvent.KEYCODE_V: {
                int pos = getListView().getSelectedItemPosition();
                if (pos == ListView.INVALID_POSITION) {
                    return false;
                }
                pastCellsAt(pos);
                return true;
            }
            case KeyEvent.KEYCODE_ENTER:
            {
                if(!withShift)
                    return false;
                int pos = getListView().getSelectedItemPosition();
                if (pos == ListView.INVALID_POSITION) {
                    return false;
                }
                executeCell(listAdapter.getItem(pos));
                return true;
            }

        }


        return super.onKeyDown(keyCode, event);
    }

    private void pastCellsAt(int pos) {
        if(cutCells.isEmpty())
            return;
        for(int i = 0; i < cutCells.size(); i++) {
            listAdapter.insert(cutCells.get(i), pos+i+1);
        }
        cutCells.clear();
        isLastCutX = false;
    }

    private void executeCell(final Cell cell) {
        cell.clearOutput();
        cell.setExecutionCount(Cell.EXEC_COUNT_RUNNING);
        String msgid = StateMachine.uuid();

        messageQueue.enqueue(new KernelMessage(msgid, "", cell.getSource()))
                .filter(reply -> reply.getParentMessageId().equals(msgid))
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
            cell.getOutput().setResult(cell.getExecCountForSave(), content.get("data").getAsJsonObject());
            listAdapter.notifyDataSetChanged();
        }else if("execute_reply".equals(repmsgtype)) {
            // once execute_reply is comming after execute_result, but now spec seems changed and execute_reply is comming before execute_result.
            // so dispose() here discard result wrongly.
            // we dispose on idle instead
            // disposable.dispose();

            Log.d("NotebookFrontend", "execute reply comming.");
        } else if("idle".equals(repmsgtype)) {
            disposable.dispose();
        } else {
            // unknown message type.
            Log.d("NotebookFrontend", repmsgtype);
        }

    }

    private void setupStateMachine(int port, String path, String token) {
        OkHttpClient httpClient = MainActivity.getHttpClient();
        MyCookieJar cookieJar = MainActivity.getCookieJar();

        stateMachine.setPort(port);
        stateMachine.setToken(token);
        stateMachine.setCookieJar(cookieJar);

        stateMachine.registerState(StateMachine.STATE_LOGIN, new LoginState(stateMachine, httpClient));

        JsonRetrieveState jsonState = new JsonRetrieveState(stateMachine, httpClient);
        jsonState.setJsonReceiver(json-> {
            bindNewContent(json);
        });
        stateMachine.registerState(NotebookStateMachine.STATE_GET_BASE_JSON, jsonState);
        stateMachine.registerState(NotebookStateMachine.STATE_CREATE_SESSION, new CreateSessionState(stateMachine, httpClient));
        stateMachine.registerState(NotebookStateMachine.STATE_CONNECT_TO_KERNEL, new ConnectToKernelState(stateMachine, httpClient, sesid-> new Kernel(sesid, messageQueue)));
        stateMachine.registerState(NotebookStateMachine.STATE_WAIT_MESSAGE, new StateMachine.State() {
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
        bundle.putInt("NEXT_STATE", NotebookStateMachine.STATE_GET_BASE_JSON);

        stateMachine.gotoNextState(StateMachine.STATE_LOGIN, bundle);
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
                return true;
            case R.id.rename_item:
                Bundle args = new Bundle();
                args.putString("BOOK_NAME", notebookPath);
                showDialog(RENAME_DIALOG_ID, args);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        super.onPrepareDialog(id, dialog, args);
        switch(id) {
            case RENAME_DIALOG_ID:
                EditText et = (EditText)dialog.findViewById(R.id.book_name_edit);
                et.setText(args.getString("BOOK_NAME"));
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch(id) {
            case RENAME_DIALOG_ID:
                LayoutInflater factory = LayoutInflater.from(this);
                final View textEntryView = factory.inflate(R.layout.rename_book_entry, null);
                return new AlertDialog.Builder(this).setTitle("Rename Book")
                        .setView(textEntryView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                EditText et = (EditText)textEntryView.findViewById(R.id.book_name_edit);
                                String newBookName = et.getText().toString();

                                renameNotebook(newBookName);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .create();

        }
        return super.onCreateDialog(id, args);
    }

    private void renameNotebook(String newBookName) {
        String url = stateMachine.buildUrl("/api/contents/" + notebookPath);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(baos));
        try {
            // {path: "RenameTest2.ipynb"}
            writer.beginObject()
                    .name("path")
                    .value(newBookName)
                    .endObject();
            writer.close();
            String content = baos.toString("UTF-8");

            RequestBody body = RequestBody.create(MediaType.parse("application/json"), content);

            Request.Builder builder = new Request.Builder()
                    .url(url)
                    .patch(body);


            stateMachine.sendRequest(url, builder, () -> {
                showMessage("renamed.") ;
                notebookPath = newBookName;
                getActionBar().setTitle(notebookPath);
            });

        } catch (IOException e) {
            showMessage("IO Exception, never happen for our case. " + e.getMessage());
        }


    }


    private void saveNotebook() {

        String url = stateMachine.buildUrl("/api/contents/" + notebookPath);

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

        Request.Builder builder = new Request.Builder()
                .url(url)
                .put(body);


        stateMachine.sendRequest(url, builder, () -> {
            showMessage("saved.") ;
        });

    }

}
