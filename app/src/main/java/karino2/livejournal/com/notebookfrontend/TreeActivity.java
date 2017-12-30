package karino2.livejournal.com.notebookfrontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import io.reactivex.functions.Action;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TreeActivity extends Activity {

    TreeStateMachine stateMachine;

    ArrayAdapter<Directory.FileModel> listAdapter;
    Directory directory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree);

        getActionBar().setDisplayHomeAsUpEnabled(true);

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
                openBook(file.path);
            } else if(file.isDirectory()) {
                openDirectory(file.path);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tree_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.new_item:
                createNewBook();
                return true;
            case android.R.id.home:
                upOrFinish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void upOrFinish() {
        if( "".equals(getCurrentPath())) {
            finish();
            return;
        }
        openDirectory(directory.getParentDirectory());
    }

    private void createNewBook() {
        String url = stateMachine.buildUrl("/api/contents");

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), "{\"type\": \"notebook\"}");

        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(body);

        Action onAfter = () -> {
            showMessage("new file.") ;
            // TODO: check current state.
            gotoLoginState();
        };

        stateMachine.sendRequest(url, builder, onAfter);

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

                listAdapter.clear();
                listAdapter.addAll(directory.content);

                // listAdapter.notifyDataSetChanged();
            }
        }));

        stateMachine.registerState(TreeStateMachine.STATE_GET_DIRECTORY, flistState);

        // strange. and dup from NotebookActivity.
        stateMachine.registerWakeupHandler(()-> {
            if(stateMachine.isChanging()) {
                stateMachine.doOne();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        gotoLoginState();
    }

    private String getCurrentPath() {
        if(directory == null)
            return "";

        return directory.path;
    }

    private void openDirectory(String newDir) {
        Bundle bundle = new Bundle();
        bundle.putString("CONTENTS_PATH", newDir);
        stateMachine.gotoNextState(TreeStateMachine.STATE_GET_DIRECTORY, bundle);

    }

    private void gotoLoginState() {
        Bundle bundle = new Bundle();
        bundle.putString("CONTENTS_PATH", getCurrentPath());
        bundle.putInt("NEXT_STATE", TreeStateMachine.STATE_GET_DIRECTORY);

        stateMachine.gotoNextState(StateMachine.STATE_LOGIN, bundle);
    }


    void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

}
