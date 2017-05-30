package karino2.livejournal.com.notebookfrontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.buttonOpen).setOnClickListener(v-> {
            int port = Integer.parseInt(findText(R.id.editTextPort));
            String path = findText(R.id.editTextPath);

            Intent intent = new Intent(MainActivity.this, NotebookActivity.class);
            intent.putExtra("PORT", port);
            intent.putExtra("IPYNB_PATH", path);
            startActivity(intent);
            // should I finish here?
        });

    }

    String findText(int resId) {
        return ((EditText)findViewById(resId)).getText().toString();
    }
}
