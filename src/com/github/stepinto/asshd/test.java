package com.github.stepinto.asshd;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;


public class test extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.
		
		Intent intent = new Intent( ); 
       	ComponentName cn = new ComponentName( test.class.getName(), test.class.getName()+".MainActivity" );
    	intent.setComponent(cn);
    	//intent.setAction("android.intent.action.MAIN"); 
		//intent.setFlags( intent.FLAG_ACTIVITY_SINGLE_TOP | intent.FLAG_ACTIVITY_CLEAR_TOP );
		//intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP );        
   	   // intent.addCategory(Intent.CATEGORY_DEFAULT);   
   	   // intent.putExtra( "Status" , "1" );
   	  
		
   	    
   	// Toast.makeText(this, " test ", 0).show(); 
   	 
      	startActivity(intent); 
      	
	 
	}
	
}