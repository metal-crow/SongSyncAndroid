package gui;

import com.example.songsyncandroid.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends ActionBarActivity {
    public static final String PREFS_NAME = "SongSyncSettings";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //go to sync layout
        Button syncLayout=(Button) findViewById(R.id.syncLayout);
        syncLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextScreen = new Intent(getApplicationContext(), SyncScreen.class);
                startActivity(nextScreen);
            }
        });
        
        //go to delete song layout
        Button deleteLayout=(Button) findViewById(R.id.DeleteLayout);
        deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextScreen = new Intent(getApplicationContext(), DeleteScreen.class);
                startActivity(nextScreen);
            }
        });
        
        //go to settings layout
        Button settings=(Button) findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextScreen = new Intent(getApplicationContext(), SettingsScreen.class);
                startActivity(nextScreen);
            }
        });
    }
}
