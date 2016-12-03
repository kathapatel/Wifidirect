// Copyright 2011 Google Inc. All Rights Reserved.

package com.material.katha.wifidirectmp3;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Calendar;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.material.katha.wifidirectmp3.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
    public static String Client_add;
    public static String DEVICE_NAME = "dname";

    public static String FILE_SIZE = "fsize";

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
//    @Override
//    protected void onHandleIntent(Intent intent) {
//
//
//        Context context = getApplicationContext();
//        Log.d("WiFiDirectActivity", "Action" + intent.getAction() + "\n\n\n\n");
//        Log.d("WiFiDirectActivity", "Action" + ACTION_SEND_FILE + "\n\n\n\n");
//
//        if (intent.getAction().equals(ACTION_SEND_FILE)) {
//            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
//            //"/data/data/com.material.katha.wifidirectmp3/app_Shared/Screenshot_2016-04-03-10-34-27.png";
//            //
//            int fsize = intent.getExtras().getInt(FILE_SIZE);
//            Log.d("WiFiDirectActivity", "file size..." + fsize);
//
//
//            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
//            String client_add = intent.getExtras().getString(Client_add);
//            // String devicename = intent.getExtras().getString(device_name);
//            String devicename = intent.getExtras().getString(DEVICE_NAME);
//
//            //String filesize = intent.getExtras().getString(FILE_SIZE);
//
//            //  long fsize = Long.parseLong(filesize);
//            //size = Long.parseLong(filesize);
//            Log.d("entery", "here");
//            Log.d("WiFiDirectActivity", "DEVICE_NAME: " + devicename);
//            Log.d("WiFiDirectActivity", "client_addt: " + client_add);
//            Log.d("WiFiDirectActivity", "host: " + host);
//            Socket socket = new Socket();
//            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
//            byte buf[] = new byte[1024];
//
//            try {
//                Log.d("WiFiDirectActivity", "Opening client socket - ");
//                socket.bind(null);
//
//                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
//
//                Log.d("WiFiDirectActivity", "Client socket - " + socket.isConnected());
//                String[] arr = new String[2];
//
//                arr[0] = client_add;
//
//
//                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//                //  String fileext = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());
//                String filename = fileUri.substring(fileUri.lastIndexOf("/") + 1);
//                // Toast.makeText(FileTransferService.this,filename, Toast.LENGTH_LONG).show();
//                arr[1] = filename;
//
//                out.writeObject(arr);
//                out.flush();
//
//                Log.d("WiFiDirectActivity", "URI for the file is: " + fileUri.toString());
//
//                OutputStream outStream = socket.getOutputStream();
//                ContentResolver cr = context.getContentResolver();
//                String myfile="hello_file";
//                FileInputStream inputStream=openFileInput(myfile);
//                InputStream is = null;
//                try {
//                  is = cr.openInputStream(Uri.parse(fileUri));
//                } catch (FileNotFoundException e) {
//                    Log.d("WiFiDirectActivity", e.toString());
//                }
//
//                //DeviceDetailFragment.copyFilestart(is, outStream);
//                DeviceDetailFragment dt = new DeviceDetailFragment();
//                dt.is = is;
//                dt.os = outStream;
//                Thread t = new Thread(dt);
//
//                t.start();
//
//
//                for (int i = 0; i < 25; i++)
//                    for (int j = 0; j < fsize; j++) {
//                        while (DeviceDetailFragment.checkpause() != 0) {
//
//                        }
//                    }
//                //this.wait(5000);
//                //  outStream.writeObject(fileext);
//
//
//                outStream.close();
//                is.close();
//
//                dbadapter m = new dbadapter(context);
//                m.open();
//                String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
//                Log.d("WiFiDirectActivity", "Copied on " + mydate);
//                m.insertEntry(filename, devicename, "Sent", mydate);
//                Log.d("WiFiDirectActivity", filename + " " + devicename + " " + "Sent");
//                m.close();
//
//
//                Log.d("WiFiDirectActivity", "Client: Data written");
//
//            } catch (IOException e) {
//                Log.e("WiFiDirectActivity", e.getMessage());
//            } finally {
//                if (socket != null) {
//                    if (socket.isConnected()) {
//                        try {
//
//                            socket.close();
//                        } catch (Exception e) {
//                            // Give up
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//
//        }
//    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("WiFiDirectActivity","here here");
        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            try {
                Log.d("WiFiDirectActivity", "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
                Log.d("WiFiDirectActivity", "Client socket - " + socket.isConnected());
                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
                try {
                    is = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.d("WiFiDirectActivity", e.toString());
                }
                DeviceDetailFragment.copyFile(is, stream, 1);
                Log.d("WiFiDirectActivity", "Client: Data written");
            } catch (IOException e) {
                Log.e("WiFiDirectActivity","error, error, error "+ e.getMessage());
            } finally {
                Log.d("WiFiDirectActivity","completed!!!");
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
