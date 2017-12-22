package karino2.livejournal.com.notebookfrontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class EditActivity extends Activity {
    int cellPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        EditText et = (EditText)findViewById(R.id.editText);
        et.setOnKeyListener((v, keyCode, e)-> {
            if(keyCode == KeyEvent.KEYCODE_ENTER && e.getAction() == KeyEvent.ACTION_DOWN &&
                    e.isShiftPressed()) {
                finshAndExecute();
                return true;
            }
            return false;
        });


        Intent intent = getIntent();
        if(intent != null) {
            cellPosition = intent.getIntExtra("CELL_POSITION", -1);
            et.setText(intent.getStringExtra("CELL_CONTENT"));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.exec_item:
                finshAndExecute();
                break;
        }
        return super.onOptionsItemSelected(item);

}

    private void finshAndExecute() {
        Intent intent = getResultIntent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @NonNull
    private Intent getResultIntent() {
        Intent intent = new Intent();
        intent.putExtra("CELL_POSITION", cellPosition);
        intent.putExtra("CELL_CONTENT", ((EditText)findViewById(R.id.editText)).getText().toString());
        return intent;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED, getResultIntent());
        finish();
    }
}
