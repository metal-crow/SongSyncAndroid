package com.example.songsyncandroid;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import android.os.Environment;

public class SyncWithPC extends Thread{

    private ArrayList<String> listOfSongsToRemove;
    private ArrayList<String> listOfSongsToAdd;
    
    public SyncWithPC(ArrayList<String> listOfSongsOldMaster) {
        this.listOfSongsToRemove=listOfSongsOldMaster;
        listOfSongsToAdd=new ArrayList<String>();
    }
    
    @Override
    public void run() {
        try{
            //emulator address is "10.0.2.2"
            Socket pcconnection=new Socket("10.0.2.2", 9091);
    
            BufferedReader in=new BufferedReader(new InputStreamReader(pcconnection.getInputStream()));
            PrintWriter out=new PrintWriter(pcconnection.getOutputStream(), true);
            //write new master song list to txt ONLY when we receive them. This stops sync failures after disconnects.
            File mastersonglist=new File(Environment.getExternalStorageDirectory()+"/SongSync/SongSync_Song_List.txt");
            mastersonglist.delete();
            
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
            
            //remove all the songs to be removed
            for(String song:listOfSongsToRemove){
                new File(Environment.getExternalStorageDirectory()+"/SongSync/Music/"+song).delete();
            }
                        
            //send server list of requested songs
            //for every request wait to receive the song before sending the next request
            //TODO need support for resending if not gotten
            BufferedInputStream is = new BufferedInputStream(pcconnection.getInputStream());
            for(String reqsong:listOfSongsToAdd){
                out.println(reqsong);
                    System.out.println("requesting "+reqsong);
                
                //recieve the length of the song in bytes
                String songlength=in.readLine();
                    System.out.println("recived length "+songlength);
                byte[] song=new byte[Integer.valueOf(songlength)];
                
                //return ready to receive song bytes
                out.println("READY");
                
                //Receive the song in bytes (split into multiple packets)
                int count=0;
                while(count<song.length){
                    count+=is.read(song,count,song.length-count);
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
            pcconnection.close();
            
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
