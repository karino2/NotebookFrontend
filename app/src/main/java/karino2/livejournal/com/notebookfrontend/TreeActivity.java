package karino2.livejournal.com.notebookfrontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;

public class TreeActivity extends Activity {

    TreeStateMachine stateMachine;

    ArrayAdapter<Directory.FileModel> listAdapter;
    Directory directory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree);


        listAdapter = new ArrayAdapter<Directory.FileModel>(this, R.layout.list_item) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView)convertView;
                if(textView == null) {
                    textView = (TextView) getLayoutInflater().inflate(R.layout.list_item, null);
                }

                if(directory != null) {
                    Directory.FileModel file = directory.content.get(position);
                    textView.setTag(file);
                    textView.setText(file.name);

                    if(file.isNotebook()) {
                        textView.setEnabled(true);
                    } else if(file.isDirectory()) {
                        textView.setEnabled(true);
                    } else {
                        textView.setEnabled(false);
                    }
                }
                return textView;
            }
        };

        ListView lv = getListView();
        lv.setAdapter(listAdapter);
        lv.setOnItemClickListener( (adapterView, view, i, l) -> {
            Directory.FileModel file = directory.content.get(i);
            if(file.isNotebook()) {
                // todo: open
                openBook(file.path);
            } else if(file.isDirectory()) {
                showMessage("TODO: open directory: " + file.path);
            }
        });



        stateMachine = new TreeStateMachine();



        Intent intent = getIntent();
        if(intent == null) {
            showMessage("Intent null. NYI case.");
            return;
        }
        int port = intent.getIntExtra("PORT", 51234);
        String token = intent.getStringExtra("TOKEN");

        setupStateMachine(port, token);

    }

    private void openBook(String path) {
        Intent intent = new Intent(this, NotebookActivity.class);
        intent.putExtra("PORT", stateMachine.getPort());
        intent.putExtra("IPYNB_PATH", path);
        intent.putExtra("TOKEN", stateMachine.getToken());
        startActivity(intent);
    }

    private ListView getListView() {
        return (ListView)findViewById(R.id.filerListView);
    }


    Handler handler = new Handler();

    private void setupStateMachine(int port, String token) {
        OkHttpClient httpClient = MainActivity.getHttpClient();
        MyCookieJar cookieJar = MainActivity.getCookieJar();

        stateMachine.setPort(port);
        stateMachine.setToken(token);
        stateMachine.setCookieJar(cookieJar);

        stateMachine.registerState(StateMachine.STATE_LOGIN, new LoginState(stateMachine, httpClient));

        FileListRetrieveState flistState = new FileListRetrieveState(stateMachine, httpClient);
        flistState.setJsonReceiver(resp -> handler.post(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                directory = gson.fromJson(resp, Directory.class);

                listAdapter.addAll(directory.content);
                // listAdapter.notifyDataSetChanged();
            }
        }));

        stateMachine.registerState(TreeStateMachine.STATE_GET_BASE_DIRECTORY, flistState);

        // strange. and dup from NotebookActivity.
        stateMachine.registerWakeupHandler(()-> {
            if(stateMachine.isChanging()) {
                stateMachine.doOne();
            }
        });



        Bundle bundle = new Bundle();
        bundle.putString("CONTENTS_PATH", "");
        bundle.putInt("NEXT_STATE", TreeStateMachine.STATE_GET_BASE_DIRECTORY);

        stateMachine.gotoNextState(StateMachine.STATE_LOGIN, bundle);
    }


    void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

}
