package gui;

import com.example.songsyncandroid.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingsScreen extends ActionBarActivity {
    
    private static EditText ipaddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        ipaddress = (EditText) findViewById(R.id.ipaddress);
        ipaddress.setText(getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("ipaddress", ""));//set to storage if it exists
        
        Button savesettings = (Button) findViewById(R.id.saveSettings);
        
        savesettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("ipaddress", ipaddress.getText().toString());

                // Commit the edits!
                editor.commit();
            }
        });
    }
}
