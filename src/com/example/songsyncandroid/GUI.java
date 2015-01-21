package com.example.songsyncandroid;

import android.widget.ProgressBar;
import android.widget.TextView;

public class GUI extends Thread{
    
    private MainActivity c;
    private ProgressBar totalsongssyncedbar;
    private TextView actioninfo;
    private TextView songnameTV;
    
    public GUI(MainActivity context, ProgressBar totalsongssyncedbar, TextView actioninfo, TextView songname) {
        this.totalsongssyncedbar=totalsongssyncedbar;
        this.actioninfo=actioninfo;
        this.songnameTV=songname;
        c=context;
    }

    /**
     * update gui with number of songs downloaded or removed
     * @param curPos
     */
    public void songAction(final int curPos,final String songname,final String actionOccuring){
        c.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                totalsongssyncedbar.setProgress(curPos);
                actioninfo.setText(actionOccuring);
                songnameTV.setText(songname);
            }
        });
    }
    
    
    public void totalNumberofSongs(final int max){
        c.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                totalsongssyncedbar.setProgress(0);
                totalsongssyncedbar.setIndeterminate(false);
                totalsongssyncedbar.setMax(max);
            }
        });
    }
    
    /**
     * Use this to indicate the song list is being received and an unknown amount of time remains
     */
    public void recievingSongList(){
        c.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    totalsongssyncedbar.setIndeterminate(true);
                    actioninfo.setText("Downloading Song List");
            }
        });
    }
}
