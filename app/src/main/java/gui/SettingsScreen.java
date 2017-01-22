package gui;

import com.example.songsyncandroid.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SettingsScreen extends ActionBarActivity {
    
    private static EditText ipaddress;
    private static RadioGroup radioSetGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        //select type of connection
        radioSetGroup = (RadioGroup) findViewById(R.id.connect_options);
        
        //hard set ip address
        ipaddress = (EditText) findViewById(R.id.ipaddress);
        ipaddress.setText(getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("ipaddress", "0.0.0.0"));//set to storage if it exists
        
        //save settings
        Button savesettings = (Button) findViewById(R.id.saveSettings);
        
        savesettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                
                //get ip address if hardcoded
                editor.putString("ipaddress", ipaddress.getText().toString());
                
                // get selected radio button from radioGroup
                int selectedId = radioSetGroup.getCheckedRadioButtonId();
                String connection_choice=((RadioButton) findViewById(selectedId)).getText().toString();
                
                editor.putString("connection_type", connection_choice);
                
                if(connection_choice.equals("USB")){
                    editor.putString("ipaddress", "0.0.0.0");
                    //refresh ip
                    ipaddress.setText("0.0.0.0");
                }
                

                // Commit the edits!
                editor.commit();
            }
        });
    }
}
