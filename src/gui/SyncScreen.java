package gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import com.example.songsyncandroid.R;
import com.example.songsyncandroid.SyncWithPC;

public class SyncScreen extends ActionBarActivity {
    public static int saved_space=500;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sync_state);
        
        Button sync= (Button) findViewById(R.id.syncStart);
                
        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dont sync unless we have mounted storage
                if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
                    //load previous list of songs (if it exists)
                    ArrayList<String> listOfSongsOldMaster=new ArrayList<String>();
                    try {
                        loadPreviousSongList(MainActivity.storage,listOfSongsOldMaster);
                    } catch (IOException e) {
                        MainActivity.gui.reportError("Unable to read the song list. Starting anew.");
                    }
                    //read ipaddress from settings
                    String ipaddress=getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("ipaddress", "");
                    
                    if(ipaddress.equals("")){
                        MainActivity.gui.reportError("IP address not set. Go into settings to set it.");
                    }else{
                        //start sync thread
                        new SyncWithPC(listOfSongsOldMaster,ipaddress).start();
                    }
                }else{
                    MainActivity.gui.reportError("External Storage not mounted!");
                }
                
            }
        });
        
    }
    
    /**
     * Load the previous master list from storage.
     * @param listOfSongsOldMaster
     * @throws IOException If some IO error occurs (storage dismounted in reading)
     */
    public static void loadPreviousSongList(String storage, ArrayList<String> listOfSongsOldMaster) throws IOException {
        File mastersonglist=new File(storage+"/SongSync/SongSync_Song_List.txt");
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
    
}
