package gui;

import android.support.v7.app.ActionBarActivity;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class GUI extends Thread{
    
    private ActionBarActivity c;
    private ProgressBar totalsongssyncedbar;
    private TextView actioninfo;
    private TextView songnameTV;
    private ProgressBar singlesongdownloadprogress;
    
    public GUI(ActionBarActivity context) {
        c=context;
    }
    

    /**
     * Load the layout components for the sync layout
     * @param progressBar
     * @param textView
     * @param textView2
     * @param progressBar2
     */
    public void loadSyncComponents(ProgressBar totalsongssyncedbar, TextView actioninfo, TextView songname, ProgressBar singlesongdownloadprogress) {
        this.totalsongssyncedbar=totalsongssyncedbar;
        this.actioninfo=actioninfo;
        this.songnameTV=songname;
        this.singlesongdownloadprogress=singlesongdownloadprogress;
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
    
    /**
     * Set the max value for the progress bar for a single song
     * @param max
     */
    public void singleSongDownloadProgressMax(final int max){
        c.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                singlesongdownloadprogress.setProgress(0);
                singlesongdownloadprogress.setIndeterminate(false);
                singlesongdownloadprogress.setMax(max);
            }
        });
    }
    
    /**
     * Mesure in bytes how much of a single song has been downloaded
     * @param prog
     */
    public void singleSongDownloadProgress(final int prog){
        c.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                singlesongdownloadprogress.setProgress(prog);
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
     * Use this to indicate the something is occurring and an unknown amount of time remains
     */
    public void waiting(final String waitingon){
        c.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    totalsongssyncedbar.setIndeterminate(true);
                    actioninfo.setText(waitingon);
            }
        });
    }

    /**
     * If an error occurs make a toast with the error message
     * @param message
     */
    public void reportError(final String message) {
        c.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(c, message, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    /**
     * After a failure or completion of sync, reset UI to default states.
     */
    public void resetUI() {
        c.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                totalsongssyncedbar.setProgress(0);
                totalsongssyncedbar.setIndeterminate(false);
                actioninfo.setText("ACTIONNAME");
                songnameTV.setText("SONGNAME");
                singlesongdownloadprogress.setProgress(0);
            }
        });
    }

    /**
     * Same as waiting, but for the single song progress bar. Also add the song name.
     * @param string
     */
    public void waitingSong(final String action, final String songname) {
        c.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    singlesongdownloadprogress.setIndeterminate(true);
                    actioninfo.setText(action);
                    songnameTV.setText(songname);
            }
        });
    }
}
