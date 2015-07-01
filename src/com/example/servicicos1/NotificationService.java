package com.example.servicicos1;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;
import android.location.GpsStatus;

public class NotificationService extends Service {
	//private  NotificationManager nNM;
	//private Location mLocation;
	//private int conteo;
	WakeLock wl;
	TcpClient mTcpClient;
	String mensaje;
	String rx_str;
	String imei;
	int watchdog=0;
	int conteos;
	boolean is_load=false;
	boolean is_conected=false;
	boolean only_time_initimei=false;
	boolean only_time_position=false;
	boolean sw=false;
	boolean is_fix=false;
	String str_rx;
	String trama_pos;
	String ano,mes,dia,hora,minuto,segundo;
	String coordenadas;
	Date d;
	static int velocidad=0;
	static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
	Location mLastLocation;
	long mLastLocationMillis;
	static float lati;
	static float longi=0;
	static String latiString;
	static String longiString;
	protected static final long GPS_UPDATE_TIME_INTERVAL=3000;  //millis
	protected static final float GPS_UPDATE_DISTANCE_INTERVAL=0; //meters
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate(){
		super.onCreate();
		Log.i("Debug","Inicializando servicio oncreate");
		
		//nNM=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		PowerManager pm = (PowerManager) this.getSystemService(POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
		final TelephonyManager mngr = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
		wl.acquire();
		configGPS();
		imei= mngr.getDeviceId();
		mensaje="";
		new ConnectTask().execute(""); 
		
		
		
		new CountDownTimer(30000, 1000) {
			public void onTick(long millisUntilFinished) {
				//Log.i("Debug","Conteo: "+ ++conteo);
				try {
                	if (mensaje.equals("")){
                    	watchdog++;
                    	Log.i("Debug","Wacthdog: "+watchdog);
                    	if (watchdog>80){
                    		watchdog=0;
                    		//hay problemas en la conexion reiniciando conexion
                    		Log.i("Debug","Problemas con TCP/IP.....reciniciando");
                    		Toast.makeText(getApplicationContext(), "Revise su conexion de datos de internet!!!", Toast.LENGTH_LONG).show();
                    		only_time_initimei=false;
                    		only_time_position=false;
                    		is_fix=false;
                    		is_load=false;
                    		mTcpClient.stopClient();
                    		Thread.sleep(10000);              		
                    		new ConnectTask().execute(""); 
                    		Thread.sleep(10000);
                    	} 	
                    }
                }catch (Exception e){
                	Log.i("Debug","Eror....");
                }
				try {
                	if (mensaje.equals("LOAD") || mensaje.equals("OK") || mensaje.equals("ON") ){
                		mensaje="";
                		watchdog=0;
                	}
                	
                }catch (Exception e){
                	Log.i("Debug","Error....");
                }
				
				if (mTcpClient.estado_conexion() && !only_time_initimei){
                	//cuando se inicia el programa se envia trama de comienzo solo una vez
                	only_time_initimei=true;
                	Log.i("Debug","Enviando trama de conexion");
                	//enviar_txt.setText("Conectado....");
                	mTcpClient.sendMessage("##"+"imei:"+imei+','+"A;");
                }
                if (is_load && !only_time_position){
                	//se envia una sola vez al comienzo la posicion
                	Log.i("Debug","Enviando primera posicion...");
                	only_time_position=true;
                	//enviar posicion inicial......
                	trama_pos="imei:"+imei+','+"tracker"+','+ano+mes+dia+hora+minuto+','+','+'F'+','+hora+minuto+segundo+".000"+','+'A'+','+coordenadas+','+velocidad+','+"0.00"+';';
                	if (is_fix){
                		Log.i("Debug",trama_pos);
        	        }
                }
			}
			public void onFinish() {
				//cada 30 segundos
				d=new Date(System.currentTimeMillis());
    			String  s= (String) DateFormat.format("dd/MM/yyyy HH:mm:ss", d.getTime());
    			s=GetUTCdatetimeAsString();    			
    			ano=s.substring(0 ,4);
    	        dia=s.substring(8,10);
    	        mes=s.substring(5, 7);
    	        hora=s.substring(11, 13);
    	        minuto=s.substring(14, 16);
    	        segundo=s.substring(17, 19);
    	        
    	        if (!sw){  
    	        	is_fix=true;//ojo quitar
                	if (is_fix){
                		Log.i("Debug","Enviando posicion a server....");
		                trama_pos="imei:"+imei+','+"tracker"+','+ano+mes+dia+hora+minuto+','+','+'F'+','+hora+minuto+segundo+".000"+','+'A'+','+coordenadas+','+velocidad+','+"0"+';';
		        	    mTcpClient.sendMessage(trama_pos);
                	}
	                sw=true;
                }else {
                	// aca se envia la posicion cada minuto....
                	Log.i("Debug","Enviando heartbeat");
                	mTcpClient.sendMessage(imei+';');
                	sw=false;
                }
    	        imei= mngr.getDeviceId();//lectura numero imei
				
				
				
				
				this.start();
				
			}
			
		}.start();
		
		
	}
	
	public static String GetUTCdatetimeAsString()
    {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new java.util.Date());

        return utcTime;
    }
	
	private void configGPS(){
		LocationManager mLocationManager;
		LocationListener mLocationListener;
		
		mLocationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mLocationListener=new MyLocationListener();
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, mLocationListener);
	}
	
	public class ConnectTask extends AsyncTask {
		@Override
			protected Object doInBackground(Object... params) {
				// TODO Auto-generated method stub
				 mTcpClient = new TcpClient(new TcpClient.OnMessageReceived(){
					@Override
					public void messageReceived(String message) {
						// TODO Auto-generated method stub
						mensaje=message;
						Log.i("Debug","Cadena recibida: " + message);
						if (message.equals("LOAD")){
							is_load=true;						
						}			
					}			
				});
				mTcpClient.run();
				return null;
			}		
	    }
	
	
	
	private class MyLocationListener implements LocationListener{

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			//Log.i("Debug",String.valueOf(location.getLatitude())+" "+String.valueOf(location.getLongitude())+" "+location.getTime());
			//mLocation=location;
			//showNotification();
			if (location != null){
				mLastLocationMillis = SystemClock.elapsedRealtime();
				
				mLastLocation = location;
			}
			if (is_fix){
				coordenadas=locacion_formateada(location.getLatitude(), location.getLongitude());
				velocidad=(int)(location.getSpeed()/1000)*3600;
			}
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class MyGPSListener implements GpsStatus.Listener {
    	public void onGpsStatusChanged(int event) {
    		switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                if (mLastLocation != null){
                	if ((SystemClock.elapsedRealtime() - mLastLocationMillis) < GPS_UPDATE_TIME_INTERVAL * 2) {
                    	is_fix =true;
                    }else {
                    	is_fix=false;
                    }
                }

                if (is_fix) { // A fix has been acquired.
                    // Do something.
                	Log.i("Debug","FIX GPS ok!!!!");
                } else { // The fix has been lost.
                    // Do something.
                	Log.i("Debug","NO FIX!!!!");
                }

                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                // Do something.
                is_fix = true;
                Log.i("Debug","FIX GPS ok!!!!");

                break;
    		}
    		
    	}
    }
	
	
	public static String locacion_formateada(double latitude, double longitude) {
    	
        try {
            //float latSeconds = (float) Math.round(latitude * 3600);
        	float latSeconds = (float) latitude * 3600;
            int latDegrees = (int) (latSeconds / 3600);
            latSeconds = Math.abs(latSeconds % 3600);
            int latMinutes = (int) (latSeconds / 60);
            latSeconds %= 60;
            
            //lati=(float) (latDegrees+latMinutes*0.0166666666667+latSeconds/3600);
            //latiString=String.format("%02.6f", lati);
            //latiString=Float.toString((float) lati);
            //String.format("%02.6f", latiString);
            
            

            //int longSeconds = (int) Math.round(longitude * 3600);
            float longSeconds = (float) longitude * 3600;
            int longDegrees = (int) (longSeconds / 3600);
            longSeconds = Math.abs(longSeconds % 3600);
            int longMinutes = (int) (longSeconds / 60);
            longSeconds %= 60;
            
            velocidad=(int) (velocidad/1.852);
            
            //longi=(float) (longDegrees+longMinutes*0.0166666666667+longSeconds/3600);
            //longiString=String.format("%02.6f", longi);
            //longiString=Float.toString((float) longi);
            
            
            
            String latDegree = latDegrees >= 0 ? "N" : "S";
            //String lonDegrees = latDegrees >= 0 ? "E" : "W";
            String longDegree = longDegrees >= 0 ? "E" : "W";
            
            //return latiString+latDegree+" "+longiString+lonDegrees;
            
            //Log.i("Debug","Originales:"+latitude+" "+longitude);
            
            lati=Math.abs(latDegrees)*100+latMinutes+latSeconds/60;
            longi=Math.abs(longDegrees)*100+longMinutes+longSeconds/60;
            latiString=String.format("%02.4f", lati);
            longiString=String.format("%02.4f", longi);
            if (Math.abs(latDegrees)<10){
            	latiString="0"+latiString;
            }
            if (Math.abs(longDegrees)<100){
            	longiString="0"+longiString;
            }
            
            
            //Log.i("Debug",latiString+','+longiString);

            //Log.i("Debug","xexun: "+lati+latDegree+','+longi+longDegree);
            latiString=latiString.replace(',','.')+','+latDegree;
            longiString=longiString.replace(',','.')+','+longDegree;
            
            return latiString+','+longiString;
            
//            return  Math.abs(latDegrees) + "°" + latMinutes + "'" + latSeconds
//                    + "\"" + latDegree +" "+ Math.abs(longDegrees) + "°" + longMinutes
//                    + "'" + longSeconds + "\"" + longDegree;
//            return  Math.abs(latDegrees) + latMinutes + latSeconds
//                     + latDegree +","+ Math.abs(longDegrees)  + longMinutes
//                    + longSeconds  + lonDegrees;
        } catch (Exception e) {
        	Log.i("Debug","Error....");

            return ""+ String.format("%8.5f", latitude) + "  "
                    + String.format("%8.5f", longitude) ;
        }
    }
	
//	private void showNotification(){
//		//Notification notification= new Notification(R.drawable.)
//		Intent iNotification =new Intent(this,new MainActivity().getClass());
//		iNotification.putExtra("NOTIFY", true);
//		iNotification.putExtra("LATITUDE", mLocation.getLatitude());
//		iNotification.putExtra("LONGITUDE", mLocation.getLongitude());
//		iNotification.putExtra("UTC", mLocation.getTime());
//		
//		PendingIntent contentIntent=PendingIntent.getActivity(this, 0, iNotification, PendingIntent.FLAG_UPDATE_CURRENT);
//		
//		
//	}

}
