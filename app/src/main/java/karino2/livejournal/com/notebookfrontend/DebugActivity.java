package karino2.livejournal.com.notebookfrontend;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.jakewharton.rxbinding2.view.RxView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DebugActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        RxView.clicks(findViewById(R.id.buttonSend))
                .subscribe(click-> onSend());





    }

    void onSend() {
        int port = Integer.parseInt(((EditText)findViewById(R.id.editTextPort)).getText().toString());
        String token = ((EditText)findViewById(R.id.editTextToken)).getText().toString();
        // http://localhost:52688/notebooks/features.ipynb

            Single.create(emitter -> {
                try {
                    // final String urlstr = "http://localhost:52688/notebooks/features.ipynb";
                    final String urlstr = "http://localhost:52688/api/notebooks/features.ipynb";
                    URL url = new URL(urlstr);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    int code = connection.getResponseCode();
                    Log.d("NotebookFE", "code: " + code);
                    if(code != 200) {
                        emitter.onError(new RuntimeException("code not 200"));
                        return;
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuffer buf = new StringBuffer();

                    String line;
                    while( (line = br.readLine()) != null ) {
                        buf.append(line);
                    }
                    br.close();

                    emitter.onSuccess(buf.toString());
                    return;

                } catch (MalformedURLException e) {
                    emitter.onError(e);
                    e.printStackTrace();
                } catch (IOException e) {
                    emitter.onError(e);
                    e.printStackTrace();
                }
            }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(cont -> {
                        debugPrint((String)cont);
            });
    }

    void debugPrint(String msg) {
        EditText et = (EditText)findViewById(R.id.editTextConsole);
        String res = msg+ "\n" + et.getText().toString();
        et.setText(res);
    }
}
