package gui;

import java.io.File;

import com.example.songsyncandroid.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

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
        gui=new GUI(this,
                ((ProgressBar) findViewById(R.id.totalsongssyncedbar)),
                ((TextView) findViewById(R.id.actioninfo)),
                ((TextView) findViewById(R.id.songname)),
                ((ProgressBar) findViewById(R.id.singlesongdownloadprogress)));
        
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
