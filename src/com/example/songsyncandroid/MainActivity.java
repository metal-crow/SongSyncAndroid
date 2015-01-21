package com.example.songsyncandroid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends ActionBarActivity {
    
    Button sync;
    ArrayList<String> listOfSongsOldMaster=new ArrayList<String>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //load previous list of songs (if it exists)
        try {
            loadPreviousSongList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        sync= (Button) findViewById(R.id.button1);//get button
        sync.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                //dont sync unless we have mounted storage
                if(v==sync && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
                    new SyncWithPC(listOfSongsOldMaster).start();
                }
                
            }
        });
        
    }

    /**
     * Load the previous master list from storage.
     * @throws IOException If some IO error occurs (storage dismounted in reading)
     */
    private void loadPreviousSongList() throws IOException {
        File mastersonglist=new File(Environment.getExternalStorageDirectory()+"/SongSync/SongSync_Song_List.txt");
        if(mastersonglist.exists() && mastersonglist.isFile()){
            BufferedReader in=new BufferedReader(new FileReader(mastersonglist));
            
            String line = null;
            while ((line = in.readLine()) != null) {
                listOfSongsOldMaster.add(line);
            }
            in.close();
        }else{
            mastersonglist.getParentFile().mkdirs();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
