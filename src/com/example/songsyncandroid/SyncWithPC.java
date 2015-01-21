package com.example.songsyncandroid;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
            //delete old list. TODO: if we get a disconnect in between here and before we finish receiving the new master list, lots of redundant downloading will occur next time.
            mastersonglist.delete();
            
            //tell the view we are downloading the song list
            gui.waiting("Downloading Song List");
            
            //Use FileWriter which can write without calling .close() because if we have a disconnect we still keep the records of the songs that did sync.
            FileWriter mastersonglistwrite=new FileWriter(mastersonglist);
            
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
                    mastersonglistwrite.flush();
                }
                else{
                    //if it isnt in the previous master list, we need to get it
                    listOfSongsToAdd.add(recieve);
                }
                recieve=in.readLine();
            }
            
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
                        
            //send server list of requested songs
            //for every request wait to receive the song before sending the next request
            //TODO need support for resending if not gotten
            BufferedInputStream is = new BufferedInputStream(pcconnection.getInputStream());
            for(int reqsongid=0;reqsongid<listOfSongsToAdd.size();reqsongid++){
                String reqsong=listOfSongsToAdd.get(reqsongid);
                //send song request
                out.println(reqsong);
                    System.out.println("requesting "+reqsong);
                    
                //inform view we are downloading song
                gui.songAction(reqsongid,reqsong.substring(reqsong.lastIndexOf("/")+1),"Downloading song");
                
                //Receive the length of the song in bytes
                String songlength=in.readLine();
                    //System.out.println("recived length "+songlength);
                byte[] song=new byte[Integer.valueOf(songlength)];
                //amount to download for single song
                gui.singleSongDownloadProgressMax(song.length);
                
                //return ready to receive song bytes
                out.println("READY");
                
                //Receive the song in bytes (split into multiple packets)
                int count=0;
                while(count<song.length){
                    count+=is.read(song,count,song.length-count);
                    //current download progress
                    gui.singleSongDownloadProgress(count);
                }
                    System.out.println("recived song "+count);
                    
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
                mastersonglistwrite.write(reqsong+"\n");
                mastersonglistwrite.flush();//do not close
            }
            is.close();
            in.close();
            out.close();
            mastersonglistwrite.close();
            
            System.out.println("Sync finished");
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

}
