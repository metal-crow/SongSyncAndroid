package gui;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.songsyncandroid.DiscoverServerThread;
import com.example.songsyncandroid.R;

public class MainActivity extends ActionBarActivity {
    public static final String PREFS_NAME = "SongSyncSettings";
    public static GUI gui;
    public static String storage = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //cross activity gui
        gui=new GUI(this);

        //use internal or external storage
        File head = new File("/storage");
        for(File f:head.listFiles()){
            if ( f.isDirectory() && f.canRead() && f.canWrite() && !f.getAbsolutePath().contains("emulated")){
                storage = f.getAbsolutePath();
                gui.reportError("Using external storage "+storage);
                break;
            }
        }
        if(storage==null){
            storage=Environment.getExternalStorageDirectory().getPath();
            gui.reportError("Using internal storage "+storage);
        }
        
        //go to sync layout
        Button syncLayout=(Button) findViewById(R.id.syncLayout);
        syncLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextScreen = new Intent(getApplicationContext(), SyncScreen.class);
                startActivity(nextScreen);
            }
        });
        
        //go to delete song layout
        Button deleteLayout=(Button) findViewById(R.id.DeleteLayout);
        deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextScreen = new Intent(getApplicationContext(), DeleteScreen.class);
                startActivity(nextScreen);
            }
        });
        
        //auto-detect and set settings
        autoDetectSettingsListener();
    }
    
    //add "settings" button to menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }
    
    //check which menu button pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
 
        case R.id.menu_settings:
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SettingsScreen.class);
            startActivity(intent); 
            break;
        }
 
        return true;
    }
    
    private void autoDetectSettingsListener() {        
        //check if plugged into pc/connected to wifi
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_STATE");
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        
        //get udp broadcast inet address
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++){
          quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        }
        InetAddress addr = null;
        try {
            addr = InetAddress.getByAddress(quads);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        
        //start discover server thread
        final DiscoverServerThread dst=new DiscoverServerThread(getSharedPreferences(MainActivity.PREFS_NAME, 0),addr);
        dst.start();
        
        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                
                //check if usb connected
                boolean usb=intent.getAction().equals("android.hardware.usb.action.USB_STATE") && intent.getBooleanExtra("connected",false);
                //check if wifi connected
                boolean wifi=intent.getAction().equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION) && intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED,false);

                //check if user set connection type
                String connection_type=settings.getString("connection_type", "Auto Detect");
                //usb has priority if both are connected
                if(usb && !connection_type.equals("Wifi")){
                    editor.putString("ipaddress", "0.0.0.0");
                    editor.commit();
                }else if(!usb && wifi && !connection_type.equals("USB")){
                    dst.DiscoverServer();
                }
            }
        };
        registerReceiver(receiver,filter);
    }
}
