package com.example.songsyncandroid;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.content.SharedPreferences;

public class DiscoverServerThread extends Thread {
    
    private static DatagramSocket socket;
    private static SharedPreferences settings;
    private boolean discoverServer=true;//trigger on start in case connected to wifi
    private InetAddress inetAddress;
    
    public DiscoverServerThread(SharedPreferences sharedPreferences, InetAddress inetAddress) {
        //start datagram socket
        try {
            socket = new DatagramSocket(9091);
            socket.setBroadcast(true);
            socket.setSoTimeout(10000);
        } catch (SocketException e1) {
            e1.printStackTrace();
        }
        settings=sharedPreferences;
        this.inetAddress=inetAddress;
    }
    
    public void DiscoverServer(){
        discoverServer=true;
    }

    @Override
    public void run() {
        SharedPreferences.Editor editor = settings.edit();

        while(true){
            if(discoverServer){
                try{
                    //send out multicast udp packet
                    sleep(5000);//wait for wifi to connect, even though the intent filter SHOULD have waited for this already, but it doesnt
                    String data_packet=new String("Discover_SongSyncServer_Request");
                    DatagramPacket sent_packet = new DatagramPacket(data_packet.getBytes(), data_packet.length(), inetAddress, 9091);
                    socket.send(sent_packet);
                    
                    //listen for response from SongSync server until receive result or timeout
                    byte[] buf = new byte[150];
                    DatagramPacket response = new DatagramPacket(buf, buf.length);
                    do{
                        response.setData(new byte[150]);
                        socket.receive(response);
                    }while(new String(response.getData()).trim().equals(data_packet));
                        
                    //response packet contains ip address
                    editor.putString("ipaddress", new String(response.getData()).trim());
                    editor.commit();
                }catch(IOException | InterruptedException e){
                    System.err.println("Error discovering server");
                    e.printStackTrace();
                }
                
                discoverServer=false;
            }
        }
    }
}
