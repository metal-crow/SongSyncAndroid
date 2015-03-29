package com.example.songsyncandroid;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

public class SyncWithPC extends Thread{

    private ArrayList<String> listOfSongsToRemove;
    private ArrayList<String> listOfSongsToAdd;
    private GUI gui;
    private String SongFileType;
    private String ip;
    private String storage;
    
    public SyncWithPC(ArrayList<String> listOfSongsOldMaster, GUI gui, String ip, String storage) {
        this.listOfSongsToRemove=listOfSongsOldMaster;
        listOfSongsToAdd=new ArrayList<String>();
        this.gui=gui;
        this.ip=ip;
        this.storage=storage;
    }
    
    @Override
    public void run() {
        try{
            //tell view we are trying to connect
            gui.waiting("Connecting to PC");
            
            //emulator address is "10.0.2.2"
            Socket pcconnection=new Socket(ip, 9091);
    
            BufferedReader in=new BufferedReader(new InputStreamReader(pcconnection.getInputStream(), "utf-8"));
            PrintWriter out=new PrintWriter(new OutputStreamWriter(pcconnection.getOutputStream(), "utf-8"), true);
            
            //tell the view we are downloading the song list
            gui.waiting("Downloading Song List");
            
            //write new master song list to txt ONLY when we receive them. This stops sync failures after disconnects.
            //Use FileWriter which can write without calling .close() because if we have a disconnect we still keep the records of the songs that did sync.
            FileWriter mastersonglistwrite=new FileWriter(new File(storage+"/SongSync/SongSync_Song_List.txt"),false);
            
            downloadSongList(in, mastersonglistwrite);
            
            //tell the view the number of songs to remove
            gui.totalNumberofSongs(listOfSongsToRemove.size());
            
            //remove all the songs to be removed
            for(int songid=0;songid<listOfSongsToRemove.size();songid++){
                String song=listOfSongsToRemove.get(songid);
                //since song filename has the converted filetype, change the masterlist's file extension to the converted value
                song=song.substring(0,song.lastIndexOf("."))+SongFileType;
                gui.songAction(songid,song.substring(song.lastIndexOf("/")),"Removing song");//tell view we are removing song
                File deletedsong=new File(storage+"/SongSync/Music/"+song);
                deletedsong.delete();
                //clean up empty folders
                //check if parent folder is empty, if so remove, and repeat, moving up a parent folder
                while(deletedsong.getParentFile().list().length<1){
                    deletedsong=deletedsong.getParentFile();
                    deletedsong.delete();
                }
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
            out.println("END OF SONG DOWNLOADS");
            
            //delete the old playlists
            Runtime.getRuntime().exec("rm -r "+storage+"/SongSync/PlayLists");

            //read the playlists
            gui.waiting("Downloading Playlists");
            downloadPlayLists(in);
            
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
     * Download all the m3u playlist files and write them
     * @param in
     * @throws IOException
     */
    private void downloadPlayLists(BufferedReader in) throws IOException {
        String line="";
        String playlistTitle=null;
        FileWriter writeplaylist = null;
        while(!line.equals("NO MORE PLAYLISTS")){
            //wait to receive the playlist title
            line=in.readLine();
            
            //when we receive the title
            if(playlistTitle==null && line!=null && !line.equals("")){
                playlistTitle=line;
                //start the playlist file
                File m3uFile=new File(storage+"/SongSync/PlayLists/"+playlistTitle+".m3u");
                m3uFile.getParentFile().mkdirs();
                writeplaylist = new FileWriter(m3uFile);
            }
            
            //when we are receiving the songs
            else if(playlistTitle!=null && !line.equals("NEW LIST")){
                //make sure that the file extension matches what we are converting to
                writeplaylist.write(storage+"/SongSync/Music/"+line.substring(0,line.lastIndexOf("."))+SongFileType+"\n");
            }
            
            //at the end of this particular playlist, reset the title and restart 
            else if(line.equals("NEW LIST")){
                playlistTitle=null;
                writeplaylist.flush();
                writeplaylist.close();
            }
        }
        writeplaylist.flush();
        writeplaylist.close();
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

        //Make sure that the actual file saves is the correct converted extension, but the master list has the original server extension
        String reqsong=reqsongOrig.replace(reqsongOrig.substring(reqsongOrig.lastIndexOf(".")), SongFileType);
        
        //send song request
        out.println(reqsongOrig);
            
        //inform view we are waiting for the server to finish converting the song and adding the metadata
        gui.songAction(reqsongid,reqsong.substring(reqsong.lastIndexOf("/")+1),"Downloading song");
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
        File SongFileStructure=new File(storage+"/SongSync/Music/"+reqsong);
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
     * @param in
     * @param mastersonglistwrite
     * @throws IOException
     */
    private void downloadSongList(BufferedReader in, FileWriter mastersonglistwrite) throws IOException {
        //recieve what the filetype of the songs is
        SongFileType=in.readLine();
        
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
