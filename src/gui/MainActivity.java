package gui;

import java.io.File;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.example.songsyncandroid.R;

public class MainActivity extends ActionBarActivity {
    public static final String PREFS_NAME = "SongSyncSettings";
    public static GUI gui;
    public static String storage="/extSdCard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //use internal or external storage
        if(!new File(storage).isDirectory()){
            storage=Environment.getExternalStorageDirectory().getPath();
        }
        
        //cross activity gui
        gui=new GUI(this);
        
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
        
        //auto-detect and set settings
        autoDetectSettings();
    }
    
    //add "settings" button to menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }
    
    //check which menu button pressed
    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
 
        case R.id.menu_settings:
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SettingsScreen.class);
            startActivity(intent); 
            break;
        }
 
        return true;
    }
    
    private void autoDetectSettings() {
        // TODO Auto-generated method stub
        
    }
}
