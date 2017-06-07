package com.github.stepinto.asshd;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.sshd.SshServer;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

/*
*   本程序是用來 當系統重新 Wakeup (醒來)時, 呼叫啟動一次 SSHD.
*
**/

public class WakeupService extends Service {
	private Thread thread;
    private MyHandler handler;
  
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
	ConnectionChangeReceiver wifiWatcher = null;
	
	@Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
  	 
    	
    	passwordAuth.setUser("root");
		passwordAuth.setPassword("root");
		status = Status.STARTING;
		sshd.setPort(PORT);
		sshd.setKeyPairProvider( new SimpleGeneratorHostKeyProvider(
				"key.ser"));
		String HW_device = android.os.Build.DEVICE;
		
		
		if(  HW_device.matches("zoom2") ) //|| HW_device.matches("rk30sdk") )
		{
			//-----------  for Original , Nook, Kddi_6"EPD ---------
			sshd.setShellFactory(new PseudoTerminalFactory(
			"/system/bin/su", "-i" ));	
		}
		//else if(  HW_device.matches("rk30sdk") )  //shuttleTablet
		//{
			//-----------  for ASUS_CM10_TF101, PB_A10(EP10) ---------
		//	sshd.setShellFactory(new PseudoTerminalFactory(
		//			"/system/xbin/sh", "-i" ,"-l" ));
		//}
		else if(HW_device.matches("wing-D820")){
		    sshd.setShellFactory(new PseudoTerminalFactory("/system/bin/su","-i"));
		}
		else  //"tf101 , "imx50_rdp"
		{
			//-----------  for ASUS_CM10_TF101, PB_A10(EP10) ---------
			sshd.setShellFactory(new PseudoTerminalFactory(
					"/system/bin/sh", "-i"));
		}
		
		sshd.setPasswordAuthenticator(passwordAuth);
		
		execRootCmd("setprop service.adb.tcp.port -1"); 
		execRootCmd("setprop persist.service.adb.enable 1");
		execRootCmd("setprop service.adb.root 1");
		execRootCmd("setprop ctl.stop adbd");
		execRootCmd("setprop ctl.start adbd");
		 // SSHD provides a CommandFactory to support SCP that can be configure
		  //  in the following way:   //在 sshd-core-0.5.0版中不穩定, 傳送會失敗
	   // sshd.setCommandFactory(new ScpCommandFactory());
	 
		try {
 			  if( isOnline() )
 			  {
				sshd.start();
				log.info("SSHD is started.");
				status = Status.STARTED;

 			  }
 			  else
 			  {
 				  
 				 new Handler().postDelayed(new Runnable(){  
 			 	     public void run() {
 			 	        //execute the task 
 			 	    	if( isOnline() )
 			 	    	{
	 			 	    	try {
								sshd.start();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	 						log.info("SSHD is started.");
	 						status = Status.STARTED;
 			 	    	}
 			 	    	else
 			 	    		Toast.makeText(getBaseContext(), "WiFi closed,Can not run service port 8022", 0).show();
 			 	    
 			 	     }  
 			 	   },2000); 
 				  
 			  }
 
		} catch (IOException e) {
				// TODO Auto-generated catch block
				//Toast.makeText(this, HW_device+" wireless:"+wifi_getLocalIpAddress()+":8022", 0).show(); 
				e.printStackTrace();
		}
	
		RegisterWifiWatcher();
		
        registerReceiver(new BroadcastReceiver() {

        	  @Override
        	  public void onReceive(Context context, Intent intent) {
        	    // do something
        		  Log.e("Device","Screen ON.");
        	
				  
        		//  Intent callIntent = new Intent(Intent.ACTION_CALL); 
        		//  callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        		//  callIntent.setClass(context,MainActivity.class);
        		//  startActivity(callIntent);
        		  
        		/*   // 也可以用來呼叫別的 APK-activity 程序
				  Intent exit = new Intent(Intent.ACTION_MAIN);  
				  exit.setComponent(new ComponentName("com.android.gallery", "com.android.camera.GalleryPicker"));
				  exit.addCategory(Intent.CATEGORY_HOME);  
                  exit.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
                  startActivity(exit);  
               //  System.exit(0);  
                  */
        		  
        	  }
        	}, new IntentFilter(Intent.ACTION_SCREEN_ON));
        
        
        
        registerReceiver(new BroadcastReceiver() {
      	  @Override
      	  public void onReceive(Context context, Intent intent) {
      	    // do something
      		  Log.e("Device","Screen OFF.");
      	
				  
      		//  Intent callIntent = new Intent(Intent.ACTION_CALL); 
      		//  callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
      		//  callIntent.setClass(context,MainActivity.class);
      		//  startActivity(callIntent);
      		  
      		/*   // 也可以用來呼叫別的 APK-activity 程序
				  Intent exit = new Intent(Intent.ACTION_MAIN);  
				  exit.setComponent(new ComponentName("com.android.gallery", "com.android.camera.GalleryPicker"));
				  exit.addCategory(Intent.CATEGORY_HOME);  
                exit.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
                startActivity(exit);  
             //  System.exit(0);  
                */
      		  
      		//   WakeUp 喚 醒
      		// PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            // WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
            // wakeLock.acquire();
            
      	  }
      	}, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        
        super.onCreate();
    }
    
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Message message = new Message();
        message.what = 1;
        handler.handleMessage(message);
        handler.removeCallbacks(thread);
        super.onDestroy();
    }
    private class MyHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch(msg.what){
                case 0:
                	
                	/*   //------- 檢查是否存在 -------
                	String run_name = execRootCmd("ps |busybox grep 'asshd'"); 
                	int len = run_name.length();
                	if( len > 0)
                	{
                		int pos_1 = run_name.indexOf("asshd");
                		String str = run_name.substring(pos_1+1, run_name.length());
                		int pos_2 = str.lastIndexOf("asshd");
                		
                		if( pos_2 <= 0 )
                		{
                			 Intent callIntent = new Intent(Intent.ACTION_CALL); 
                   		  	callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
                   		  	callIntent.setClass(getBaseContext(),MainActivity.class);
                   		  	startActivity(callIntent);
                		}
                	}
                  */
                	
                //    Log.e("givemepass","I am alive.");
                    break;
                case 1:
                //    Log.e("givemepass","I am dead.");
                    break;
            }
            super.handleMessage(msg);
        }
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
  //-------------------------------------------------------
  	private void RegisterWifiWatcher()
      {
  		 if(wifiWatcher == null)
  		 wifiWatcher = new ConnectionChangeReceiver();
  		 
  		IntentFilter intentFilter = new IntentFilter();
  		 intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
  		 intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
  		 intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
  		 intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
  		 registerReceiver(wifiWatcher, intentFilter);
      }
   	private class ConnectionChangeReceiver extends BroadcastReceiver {
  		
  		 public void onReceive( Context context, Intent intent ) {
  		 
  		 CheckWifiStatus(  context );
  		 }
  		 
  		 void CheckWifiStatus( Context context )
  		 {
  			 WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
  			// iv_wifi = (ImageView) statusbar.findViewById(R.id.wifi);
  			 
  			 if(wifiMgr == null)
  			 {
  			    // Toast.makeText(context, "No WiFi available on device.",Toast.LENGTH_LONG).show();
  				 //iv_wifi.setImageResource(R.drawable.s_wifi_close);
  			 }
  			 else if( !wifiMgr.isWifiEnabled() )
  			 {
  				 //iv_wifi.setImageResource(R.drawable.s_wifi_close);
  			 }
  			 else
  			 {
  			     // Toast.makeText(context, "WiFi enabled.",Toast.LENGTH_LONG).show();
  		 
  			 ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
  			 NetworkInfo[] netInf = conMgr.getAllNetworkInfo();
  					 for(NetworkInfo inf : netInf){
  					     if(inf.getTypeName().contains("WIFI"))
  					     {
  					         if(inf.isConnected()){
  					        	 
  					        	if( isOnline() )
  		 			 	    	{
  			 			 	    	try {
  										sshd.start();
  									} catch (IOException e) {
  										// TODO Auto-generated catch block
  										e.printStackTrace();
  									}
  			 						log.info("SSHD is started.");
  			 						status = Status.STARTED;
  		 			 	    	}
  		 			 	    	else
  		 			 	    		Toast.makeText(getBaseContext(), "WiFi closed,Can not run service port 8022", 0).show();
  		 			 	    
  					            // Toast.makeText(context, "WiFi is connected.",Toast.LENGTH_LONG).show();
  					            // iv_wifi.setImageResource(R.drawable.s_wifi_open);
  					        }   
  					        else{
  					           //  Toast.makeText(context, "WiFi NOT connected.",Toast.LENGTH_LONG).show();
  					            // iv_wifi.setImageResource(R.drawable.s_wifi_close);
  					        }
  					     } // if(inf.getTypeName()
  					 } //for(NetworkInfo
  				 } // else
   		} // void CheckWifiStatus
  		 
  	}  // private class ConnectionChangeReceiver
  	 //-------------------------------------------------------
}
  /*
  // Will be called asynchronously be Android
  @Override
  protected void onHandleIntent(Intent intent) {
  
	  registerReceiver(new BroadcastReceiver() {

		  @Override
		  public void onReceive(Context context, Intent intent) {
		    // do something
			  intent.setComponent(new ComponentName("com.github.stepinto.asshd", "com.github.stepinto.asshd.MainActivity"));
				startActivity(intent);
				
		  }
		}, new IntentFilter(Intent.ACTION_SCREEN_ON));

  }
  */
  
  

