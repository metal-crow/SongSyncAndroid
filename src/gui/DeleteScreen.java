package gui;

import java.io.IOException;
import java.util.ArrayList;

import com.example.songsyncandroid.R;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class DeleteScreen extends ActionBarActivity{
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_song);

        EditText song_data = (EditText) findViewById(R.id.DeleteSongData);
        Button find_song_from_data = (Button) findViewById(R.id.delete_song_finder);
        
        find_song_from_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //search through the song list for songs with any data that matches
                ArrayList<String> listOfSongsOldMaster=new ArrayList<String>();
                try {
                    loadPreviousSongList(storage,listOfSongsOldMaster);
                } catch (IOException e) {
                    MainActivity.gui.reportError("Unable to read the song list.");
                }
            }
        });
    }

}
