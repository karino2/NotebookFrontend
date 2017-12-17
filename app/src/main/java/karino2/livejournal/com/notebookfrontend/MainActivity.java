package karino2.livejournal.com.notebookfrontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import okhttp3.OkHttpClient;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.buttonOpen).setOnClickListener(v-> {
            int port = Integer.parseInt(findText(R.id.editTextPort));
            String token = findText(R.id.editTextToken);


            Intent intent = new Intent(MainActivity.this, TreeActivity.class);
            intent.putExtra("PORT", port);
            intent.putExtra("TOKEN", token);
            startActivity(intent);
            // should I finish here?
        });

    }

    static OkHttpClient httpClient = null;
    static MyCookieJar cookieJar = new MyCookieJar();

    public static MyCookieJar getCookieJar() {
        return cookieJar;
    }

    public static OkHttpClient getHttpClient() {
        if(httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .build();
        }
        return httpClient;
    }



    String findText(int resId) {
        return ((EditText)findViewById(resId)).getText().toString();
    }
}
