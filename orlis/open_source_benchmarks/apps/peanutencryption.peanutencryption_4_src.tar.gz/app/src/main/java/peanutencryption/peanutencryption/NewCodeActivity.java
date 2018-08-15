/*
 * @author Gabriel Oexle
 * 2015.
 */

package peanutencryption.peanutencryption;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;


public class NewCodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_code);
        android.support.v7.widget.Toolbar myToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar_NewCode);

        setSupportActionBar(myToolbar);


    }

    @Override
    protected void onPause(){
        super.onPause();
        Intent broadcastIntent = new Intent();

        setResult(RESULT_CANCELED, broadcastIntent);
        finish();

    }

    public void OnButtonClickNewCode(View view)
    {
        TextInputLayout editTextCodeName = (TextInputLayout) findViewById(R.id.input_layout_codeName);
        TextInputLayout editTextCodeSecret = (TextInputLayout) findViewById(R.id.input_layout_CodeSecret);

        String codeName_Dialog_str = editTextCodeName.getEditText().getText().toString();
        String code_Dialog_str = editTextCodeSecret.getEditText().getText().toString();

        if(codeName_Dialog_str.isEmpty())
        {
            editTextCodeName.setError("Please insert code name");
        }
        if(code_Dialog_str.isEmpty())
        {
            editTextCodeSecret.setError("Please insert code");
        }
        Intent broadcastIntent = new Intent();

        broadcastIntent.putExtra("CodeName", codeName_Dialog_str);
        broadcastIntent.putExtra("Code", code_Dialog_str);

        setResult(RESULT_OK, broadcastIntent);
        finish();

    }
}
