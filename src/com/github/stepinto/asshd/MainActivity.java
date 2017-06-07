package com.github.stepinto.asshd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.KeyPair;
import java.util.Enumeration;
import java.util.List;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.KeyPairProvider;
import org.apache.sshd.common.Session;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.FileSystemFactory;
import org.apache.sshd.server.FileSystemView;
import org.apache.sshd.server.SshFile;
import org.apache.sshd.server.command.ScpCommand;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	private enum Status {
		STOPPED, STARTING, STARTED, STOPPING
	}

	private static final int PORT = 8022;

	private final Logger log = LoggerFactory.getLogger(MainActivity.class);
	private final SshServer sshd = SshServer.setUpDefaultServer();
	private final SimplePasswordAuthenticator passwordAuth = new SimplePasswordAuthenticator();
	private final SimplePublicKeyAuthenticator publicKeyAuth = new SimplePublicKeyAuthenticator();
	private final SimpleForwardingFilter forwardingFilter = new SimpleForwardingFilter();

	public static final String PREF = "KEY_PAIR";
	public static final String PREF_KEY = "KEY";

	//private static final KeyPairProvider KeyPairProvider = null;
	//KeyPairProvider keypair;
	
	// UI components
	//private Button startButton = null;
	private Status status;
	private PowerManager.WakeLock mWakeLock;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	//	setContentView(R.layout.main);
	
		
		final Intent intent = new Intent();
		intent.setClass(this, WakeupService.class);
	   startService(intent);
	
//	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 //  	wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNjfdhotDimScreen");
	  
	   setWakeLock( true ); // 保持 CPU運轉	 
   	
	   disableLockScreen();   // Wakeup後, 不需要滑屏解鎖
	   
	   if( isOnline() )
		   Toast.makeText(this,  android.os.Build.DEVICE+" start service:"+wifi_getLocalIpAddress()+":8022", 0).show(); 
	   
	   finish();
	   
	   /*
	   
	   
		passwordAuth.setUser("root");
		passwordAuth.setPassword("root");
		
	//	startButton.setEnabled(false);
	 //  startButton.setText("Starting");
		status = Status.STARTING;

		sshd.setPort(PORT);
		
	//	SharedPreferences settings = getSharedPreferences(PREF, 0);
		//SimpleGeneratorHostKeyProvider keypair;
		
	//	if( settings.getString(PREF_KEY, null).length() !=0 )
		//{
		//	keypair.loadKey(settings.getString(PREF_KEY, ""));
		
		//sshd.setKeyPairProvider( keypair = new SimpleGeneratorHostKeyProvider(
			//	"key.ser"));
		
		sshd.setKeyPairProvider( new SimpleGeneratorHostKeyProvider(
					"key.ser"));
				
	//	SharedPreferences settings = null;
		//settings.edit()
		//	.putString(PREF_KEY, keypair.toString())
		//	.commit();
	
	//	String aaa = keypair.toString();
		
		String HW_device = android.os.Build.DEVICE;
	
		if(  HW_device.matches("tf101")  )
		{
			//-----------  for ASUS_CM10_TF101, PB_A10(EP10) ---------
			sshd.setShellFactory(new PseudoTerminalFactory(
					"/system/xbin/su", "-i"  ));
		}
		else if ( HW_device.matches("imx50_rdp") )
		{
			sshd.setShellFactory(new PseudoTerminalFactory(
					"/system/bin/sh", "-i"));
		}
		else  // "ZOOM2"
		{
			//-----------  for Original , Nook, Kddi_6"EPD ---------
				sshd.setShellFactory(new PseudoTerminalFactory(
				"/system/bin/su", "-i"));
		}
		
		
		sshd.setPasswordAuthenticator(passwordAuth);
		//sshd.setPublickeyAuthenticator(publicKeyAuth);
		//sshd.setForwardingFilter(forwardingFilter);
		
		  // SSHD provides a CommandFactory to support SCP that can be configure
		  //  in the following way:   //在 sshd-core-0.5.0版中不穩定, 傳送會失敗
		   //  sshd.setCommandFactory(new ScpCommandFactory());
	
		
		
		try {
	   			  if( isOnline() )
	   			  {
					sshd.start();
					
					log.info("SSHD is started.");
					
				//	startButton.setEnabled(true);
				//	startButton.setText("Stop");
					status = Status.STARTED;
					Toast.makeText(this, HW_device+" wireless:"+wifi_getLocalIpAddress()+":8022", 0).show(); 
					
	   			  }
	   			  else
	   				Toast.makeText(this, "WiFi still not work", 0).show(); 
	   				  
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Toast.makeText(this, HW_device+" wireless:"+wifi_getLocalIpAddress()+":8022", 0).show(); 
					e.printStackTrace();
				}
		
		this.finish();
		*/
	   
		
	}
	//----------------------------------------------------------------
    public String getThisIpAddress() { 
    WifiManager wim= (WifiManager) getSystemService(WIFI_SERVICE)  ;     
    List<WifiConfiguration> l=  wim.getConfiguredNetworks();      
    if( l.isEmpty() ) return "";
    WifiConfiguration wc=l.get(0); 
    String this_ip = ""+ Formatter.formatIpAddress(wim.getConnectionInfo().getIpAddress());
    return this_ip;
   }
    //----------------------------------------------------------------    
	public boolean isOnline() 
   	{
		String this_ip = getThisIpAddress();
        if( this_ip.length() < 9 ) 
            return false;
        else return true;
   	}
	
	//----------------------------------------------------------------
	public String wifi_getLocalIpAddress() {
		
	 WifiManager wim= (WifiManager) getSystemService(WIFI_SERVICE);     
     List<WifiConfiguration> l=  wim.getConfiguredNetworks();     
     if( l.isEmpty() ) return "";
     WifiConfiguration wc=l.get(0); 
     String this_ip = ""+ Formatter.formatIpAddress(wim.getConnectionInfo().getIpAddress());
     return this_ip;
	}
	
	//----------------------------------------------------------------
    public static String getLocalIpAddress() { 
        try { 
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) { 
                NetworkInterface intf = en.nextElement(); 
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) { 
                    InetAddress inetAddress = enumIpAddr.nextElement(); 
                    if (!inetAddress.isLoopbackAddress()) { 
                        return inetAddress.getHostAddress().toString(); 
                    } 
                } 
            } 
        } catch (SocketException ex) { 
           // Log.e(LOG_TAG, ex.toString()); 
        } 
        return null; 
    } 
    
  //----------------------------------------------------------------
    public void disableLockScreen()
    {
    	KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);  
    	KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);  
    	lock.disableKeyguard();  
    }
    
  //-------------------------------------------------------------  
    // 執行命令並且輸出結果
       public static String execRootCmd(String cmd) 
       {
           String result = ""; 
           DataOutputStream dos = null; 
           DataInputStream dis = null; 
           try { 
           	   Process p = Runtime.getRuntime().exec("su");// 經過Root處理的android系統即有su命令
           	   dos = new DataOutputStream(p.getOutputStream()); 
           	   dis = new DataInputStream( p.getInputStream()); 
   			    Log.i( "RootCmd", cmd); 
   			    dos.writeBytes(cmd + "\n"); 
   			    dos.flush(); 
   			    dos.writeBytes("exit\n"); 
   			    dos.flush() ; 
   			    String line = null; 
   			    while ((line = dis.readLine()) != null) 
   			    { 
   			    	Log.d("result", line); 
   			    	result += line; 
   			    } 
   			    p.waitFor(); 
   		}
           catch (Exception e ) 
           { 
           	e.printStackTrace(); 
           } 
           finally { 
           	if (dos != null) 
           	{ 
           		try { dos.close(); } 
           		catch (IOException e) { e.printStackTrace(); } 
           	} 
           	if (dis != null) 
           	{ 
           		try { dis.close(); } 
           		catch (IOException e) { e.printStackTrace(); }
           	} 
           } 
           return result; 
       }
     //-------------------------------------------------------------  
       protected void setWakeLock(boolean on){
           if(mWakeLock == null){
               PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
               // 保持 CPU運轉
               mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK |
                                           PowerManager.ON_AFTER_RELEASE, "myscreen_dim_wlocker");
               
              // mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
              ///                              PowerManager.ON_AFTER_RELEASE, "myfull_weaklocker");
           }
           if(on){
            mWakeLock.acquire();
           }else{
               if(mWakeLock.isHeld()){
                   mWakeLock.release();
               }
            mWakeLock = null;
           }

       }
	/*
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		startButton = (Button) findViewById(R.id.start_button);
		
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onStartButtonClicked();
			}
		});

		// Temporarily we add user for test
		passwordAuth.setUser("root");
		passwordAuth.setPassword("root");
		
		
	}
  
		
	private void onStartButtonClicked() {
		try {
			if (status == Status.STOPPED) {
				startButton.setEnabled(false);
				startButton.setText("Starting");
				status = Status.STARTING;

				sshd.setPort(PORT);
				sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(
						"key.ser"));
				sshd.setShellFactory(new PseudoTerminalFactory(
						"/system/bin/sh", "-i"));
				sshd.setPasswordAuthenticator(passwordAuth);
				sshd.setPublickeyAuthenticator(publicKeyAuth);
				sshd.setForwardingFilter(forwardingFilter);

				sshd.start();
				log.info("SSHD is started.");
				
				startButton.setEnabled(true);
				startButton.setText("Stop");
				status = Status.STARTED;
			}
			else if (status == Status.STARTED) {
				startButton.setEnabled(false);
				startButton.setText("Stopping");
				status = Status.STOPPING;
				
				sshd.stop();
				log.info("SSHD is stopped.");
				
				startButton.setEnabled(true);
				startButton.setText("Start");
				status = Status.STOPPED;
			}
		} catch (IOException e) {
			log.error(e.toString());
		} catch (InterruptedException e) {
			log.error(e.toString());
		}
	}
	
	*/
	
}