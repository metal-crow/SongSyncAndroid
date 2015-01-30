package com.example.songsyncandroid;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

import android.os.Environment;

public class SyncWithPC extends Thread{

    private ArrayList<String> listOfSongsToRemove;
    private ArrayList<String> listOfSongsToAdd;
    private GUI gui;
    
    public SyncWithPC(ArrayList<String> listOfSongsOldMaster, GUI gui) {
        this.listOfSongsToRemove=listOfSongsOldMaster;
        listOfSongsToAdd=new ArrayList<String>();
        this.gui=gui;
    }
    
    @Override
    public void run() {
        try{
            //tell view we are trying to connect
            gui.waiting("Connecting to PC");
            
            //emulator address is "10.0.2.2"
            Socket pcconnection=new Socket("10.0.2.2", 9091);
    
            BufferedReader in=new BufferedReader(new InputStreamReader(pcconnection.getInputStream()));
            PrintWriter out=new PrintWriter(pcconnection.getOutputStream(), true);
            //write new master song list to txt ONLY when we receive them. This stops sync failures after disconnects.
            File mastersonglist=new File(Environment.getExternalStorageDirectory()+"/SongSync/SongSync_Song_List.txt");
            
            //tell the view we are downloading the song list
            gui.waiting("Downloading Song List");

            //Use FileWriter which can write without calling .close() because if we have a disconnect we still keep the records of the songs that did sync.
            FileWriter mastersonglistwrite=new FileWriter(mastersonglist,false);
            
            downloadSongList(mastersonglist, in, mastersonglistwrite);
            
            //tell the view the number of songs to remove
            gui.totalNumberofSongs(listOfSongsToRemove.size());
            
            //remove all the songs to be removed
            for(int songid=0;songid<listOfSongsToRemove.size();songid++){
                String song=listOfSongsToRemove.get(songid);
                gui.songAction(songid,song.substring(song.lastIndexOf("/")),"Removing song");//tell view we are removing song
                new File(Environment.getExternalStorageDirectory()+"/SongSync/Music/"+song).delete();
            }
            
            //tell the view the number of songs we have to download 
            gui.totalNumberofSongs(listOfSongsToAdd.size());
                        
            BufferedInputStream is = new BufferedInputStream(pcconnection.getInputStream());
            //send server list of requested songs
            //for every request wait to receive the song before sending the next request
            //TODO need support for resending if not gotten
            for(int reqsongid=0;reqsongid<listOfSongsToAdd.size();reqsongid++){
                downloadandRequestASong(mastersonglistwrite, out, in, is, reqsongid);
            }

            is.close();
            in.close();
            out.close();
            mastersonglistwrite.close();
            
            gui.songAction(listOfSongsToAdd.size(),"","Finished Downloading");
            
            pcconnection.close();
            
        }catch(ConnectException e){
            gui.reportError("Connection timedout. Is the server available?");
        }catch(Exception e){
            e.printStackTrace();
            gui.reportError(e.getMessage());
        }
        gui.resetUI();
    }

    /**
     * Method to send the server a song request and download it
     * @param mastersonglistwrite
     * @param out
     * @param in
     * @param is
     * @param reqsongid 
     * @throws IOException 
     */
    private void downloadandRequestASong(FileWriter mastersonglistwrite, PrintWriter out, BufferedReader in, BufferedInputStream is, int reqsongid) throws IOException {
        String reqsongOrig=listOfSongsToAdd.get(reqsongid);

        //TODO for now im hardcoding that the song is an mp3, but in the future the filetype will have to be gotten from the server before downloading all the songs, so we know here
        String reqsong=reqsongOrig.replace(reqsongOrig.substring(reqsongOrig.lastIndexOf(".")), ".mp3");
        
        //send song request
        out.println(reqsongOrig);
            
        //inform view we are waiting for the server to finish converting the song and adding the metadata
        gui.waitingSong("Waiting for song to be converted...",reqsong.substring(reqsong.lastIndexOf("/")+1));
        
        //Receive the length of the song in bytes
        String songlength=null;
        do{
            //since the server must convert before sending this, wait and listen until we receive the length
            songlength=in.readLine();
        }while(songlength==null);
        
        //return ready to receive song bytes
        out.println("READY");
        
        //inform view we are downloading song
        gui.songAction(reqsongid,reqsong.substring(reqsong.lastIndexOf("/")+1),"Downloading song");
        
        //System.out.println("recived length "+songlength);
        byte[] song=new byte[Integer.valueOf(songlength)];
        //amount to download for single song
        gui.singleSongDownloadProgressMax(song.length);
        
        //Receive the song in bytes (split into multiple packets)
        int count=0;
        while(count<song.length){
            count+=is.read(song,count,song.length-count);
            //current download progress
            gui.singleSongDownloadProgress(count);
        }
            
        //write the song to storage
        //make the directories the file is in
        File SongFileStructure=new File(Environment.getExternalStorageDirectory()+"/SongSync/Music/"+reqsong);
        SongFileStructure.getParentFile().mkdirs();
            
        //write the song
        FileOutputStream writesong = new FileOutputStream(SongFileStructure);
        writesong.write(song);
        writesong.flush();
        writesong.close();
        
        //write the song we just received to the master list
        mastersonglistwrite.write(reqsongOrig+"\n");
        mastersonglistwrite.flush();//do not close
    }

    /**
     * Download the song list
     * @param mastersonglist
     * @param in
     * @param mastersonglistwrite
     * @throws IOException
     */
    private void downloadSongList(File mastersonglist, BufferedReader in, FileWriter mastersonglistwrite) throws IOException {
        //Receive the list of songs the computer has
        String recieve=in.readLine();
        while(!recieve.equals("ENDOFLIST")){
            //i dont know why android uses a / for a file delimiter and windows uses a \.
            recieve=recieve.replaceAll("\\\\", "/");
            
            //when we receive a song title, check if we already have it in the old list
            if(listOfSongsToRemove.contains(recieve)){
                //if so, we remove it from the old list. At the end, the songs remaining in the old list no longer exist on the pc and will be removed from the phone
                listOfSongsToRemove.remove(recieve);
                //also write this song, which we already have downloaded, to the master list
                mastersonglistwrite.write(recieve+"\n");
            }
            else{
                //if it isnt in the previous master list, we need to get it
                listOfSongsToAdd.add(recieve);
            }
            recieve=in.readLine();
        }
        
        //only flush the buffered write stream after we have finished receiving the master list again. this prevents loss of master list
        mastersonglistwrite.flush();
    }

}
