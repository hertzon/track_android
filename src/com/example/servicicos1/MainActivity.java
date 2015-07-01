package com.example.servicicos1;

//import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        startService(new Intent(this,NotificationService.class));
        
        
    }
    
    @Override protected void onStart() {
    	   super.onStart();
    	   Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show();
    }
    
    @Override protected void onResume() {
    	   super.onResume();
    	   Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
    }
    
    @Override protected void onPause() {
    	   Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
    	   super.onPause();
    }
    
    @Override protected void onStop() {
    	   super.onStop();
    	   Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show(); 
    }
    	 
    	@Override protected void onRestart() {
    	   super.onRestart();
    	   Toast.makeText(this, "onRestart", Toast.LENGTH_SHORT).show();
    }
    	 
    	@Override protected void onDestroy() {
    	   super.onDestroy();
    	   Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();     
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
