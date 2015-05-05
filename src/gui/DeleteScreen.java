package gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.songsyncandroid.R;

public class DeleteScreen extends Activity{
    
    private static ArrayList<String> orig_listOfSongs=new ArrayList<String>();
    private static ArrayList<String> filter_listOfSongs=new ArrayList<String>();
    private static ArrayAdapter<String> adapter;
    private static int selected_id=-1;
    private static FileWriter songs_to_delete;
    private static AlertDialog.Builder confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_song);

        //songs we want to delete
        try {
            songs_to_delete=new FileWriter(new File(MainActivity.storage+"/SongSync/SongSync_Delete_Song_List.txt"),true);
        } catch (IOException e1) {
            MainActivity.gui.reportError("Unable to open song deletion file.");
        }
        
        //get list
        ListView list = (ListView) findViewById(R.id.listview);
        //set list adaptor
        adapter = new ArrayAdapter<String>(this,R.layout.delete_list_item, filter_listOfSongs);
        list.setAdapter(adapter);
        //set selection of songs ability
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setSelector(android.R.color.darker_gray);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                selected_id=position;
            }
        });
           
        //load songs
        try {
            SyncScreen.loadPreviousSongList(MainActivity.storage,orig_listOfSongs);
        } catch (IOException e) {
            MainActivity.gui.reportError("Unable to read the song list.");
        }
        //make copy of original for destructive filtering
        for(String song:orig_listOfSongs){
            filter_listOfSongs.add(song);
        }
        
        //set search button functionality
        ((Button) findViewById(R.id.delete_song_finder)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //clear current filter
                filter_listOfSongs.clear();
                //get text in search bar
                String search=((EditText) findViewById(R.id.DeleteSongData)).getText().toString();
                //filter listofsongs based on this
                for(String song:orig_listOfSongs){
                    if(song.contains(search) || search.length()==0){
                        filter_listOfSongs.add(song);
                    }
                }
                //refresh list view
                adapter.notifyDataSetChanged();
            }
        });
        
        //confirmation popup settings
        confirm =new AlertDialog.Builder(this);
        confirm.setIcon(android.R.drawable.ic_dialog_alert);
        confirm.setTitle("Delete Song");
        confirm.setMessage("Are you sure you want to permantly delete this song off your computer?");
        confirm.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //get item selected on list
                String song=((ListView) findViewById(R.id.listview)).getItemAtPosition(selected_id).toString();
                //delete
                try {
                    songs_to_delete.write(song+"\n");
                    songs_to_delete.flush();
                } catch (IOException e) {
                    MainActivity.gui.reportError("Unable to write to the deletion songs file.");
                }
                //delete notice
                MainActivity.gui.reportError("Song Deleted");
                //remove from list
                filter_listOfSongs.remove(song);
                adapter.notifyDataSetChanged();
                selected_id=-1;
            }
        });
        confirm.setNegativeButton("No", null);
        
        //set delete song button functionality
        ((Button) findViewById(R.id.deleted_selected_song)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {         
                if(selected_id!=-1){
                    //confirmation popup
                    confirm.show();
                }else{
                    MainActivity.gui.reportError("No song selected");
                }
            }
        });
    }
    
}
