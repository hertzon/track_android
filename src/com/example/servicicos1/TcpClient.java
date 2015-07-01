package com.example.servicicos1;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TcpClient {
	public static final String SERVER_IP = "200.71.32.61"; //your computer IP address
    public static final int SERVER_PORT = 22022; //PUERTO DE GPSGATESERVER
    //public static final int SERVER_PORT = 21272; //PUERTO DE OPENGTS
    //public static final int SERVER_PORT = 23; //PUERTO DE PRUEBAS
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    //
    char[] array;
    private char conteo;
    private int i=0;
    private String str;
    private boolean is_conected=false;
    
    
    
    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }
    
    
    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {
        if (mBufferOut != null && !mBufferOut.checkError()) {
            //mBufferOut.println(message);
        	mBufferOut.print(message);
            mBufferOut.flush();
        }
    }
    
    /**
     * Close the connection and release the members
     */
    public void stopClient() {
    	Log.i("Debug", "stopClient");

        // send mesage that we are closing the connection
        //sendMessage(Constants.CLOSED_CONNECTION + "Kazy");

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
        
    }
    
    public boolean estado_conexion(){
    	return is_conected;
    }
    
    
    public void run() {
    	mRun = true;
    	try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            Log.i("Debug", "C: Connecting...");

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVER_PORT);

            try {
            	Log.i("Debug", "inside try catch");
                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // send login name
                //sendMessage(Constants.LOGIN_NAME + PreferencesManager.getInstance().getUserName());
                //sendMessage("Hi");
                //in this while the client listens for the messages sent by the server
                str="";
                is_conected=true;
                
                while (mRun) {
                    //mServerMessage = mBufferIn.readLine();
                	//conteo=mBufferIn.read(buff);
                	if (mBufferIn.ready()){
                		conteo=(char) mBufferIn.read();
                		str=str+conteo;
                		//Log.i("Debug","Str: "+str);
                		if (str.equals("LOAD")){
                			mServerMessage=str;
                			Log.i("Debug","Str: "+str);
                		}
                		if (str.equals("OK")){
                			mServerMessage=str;
                			Log.i("Debug","Str: "+str);
                		}
                	}
                    if (mServerMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(mServerMessage);
                        str="";
                    }
                    mServerMessage=null;
                }
               
                //Log.i("Debug", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {

                Log.i("Debug", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {

            Log.i("Debug", "C: Error", e);

        }
    	
    }
    
    
    
    
    
    
    
    
  //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
    

}
