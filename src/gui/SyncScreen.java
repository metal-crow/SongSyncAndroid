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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.songsyncandroid.R;
import com.example.songsyncandroid.SyncWithPC;

public class SyncScreen extends ActionBarActivity {
    private static GUI gui;
    private static ArrayList<String> listOfSongsOldMaster=new ArrayList<String>();
    
    public static int saved_space=500;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sync_state);
        
        ProgressBar totalsongssyncedbar = (ProgressBar) findViewById(R.id.totalsongssyncedbar);
        ProgressBar singlesongdownloadprogress = (ProgressBar) findViewById(R.id.singlesongdownloadprogress);
        Button sync= (Button) findViewById(R.id.syncStart);
        TextView actioninfo = (TextView) findViewById(R.id.actioninfo);
        TextView songname = (TextView) findViewById(R.id.songname);
        
        gui=new GUI(this,totalsongssyncedbar,actioninfo,songname,singlesongdownloadprogress);
        
        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dont sync unless we have mounted storage
                //use internal or external storage
                String storage="/extSdCard";
                if(!new File(storage).isDirectory()){
                    storage=Environment.getExternalStorageDirectory().getPath();
                }
                
                if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
                    //load previous list of songs (if it exists)
                    try {
                        loadPreviousSongList(storage);
                    } catch (IOException e) {
                        gui.reportError("Unable to read the song list. Starting anew.");
                    }
                    //read ipaddress from settings
                    String ipaddress=getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("ipaddress", "");
                    
                    if(ipaddress.equals("")){
                        gui.reportError("IP address not set. Go into settings to set it.");
                    }else{
                        //start sync thread
                        new SyncWithPC(listOfSongsOldMaster,gui,ipaddress,storage).start();
                    }
                }
                
            }
        });
        
    }
    
    /**
     * Load the previous master list from storage.
     * @throws IOException If some IO error occurs (storage dismounted in reading)
     */
    private void loadPreviousSongList(String storage) throws IOException {
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
