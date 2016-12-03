/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.material.katha.wifidirectmp3;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static android.widget.Toast.*;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener, Runnable {
    protected InputStream is;
    protected OutputStream os;
    String datapath;
    public static int pause = 0;
    public static int s_count = 0, c_count = 0;
    public static String client_ip = null;
    public static final String SERVER_IP = "192.168.49.1";
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private static View mContentView = null;
    private static WifiP2pDevice device;
    private WifiP2pInfo info;
    public static ProgressDialog progressDialog = null;
    public static ProgressDialog pd;
    public static ProgressDialog pdr;
    public static int start_receive = 0;
    public static String fileext;
    public static String devicename = null;
    public static int copy_start = 0;
    public static int receive_complete = 0;
    public String Localip;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        filelist();
        FileInputStream inputStream= null;
        try {
            inputStream = getActivity().openFileInput("myfile");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader inputStreamReader=new InputStreamReader(inputStream);
        mContentView = inflater.inflate(R.layout.device_detail, null);
        Log.d("katha", "2");
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            //  @Override
            public void onClick(View v) {

                s_count = 0;
                c_count = 0;
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceName, true, true);
//
// new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
                devicename = device.deviceName;
                Toast.makeText(getActivity(), devicename, Toast.LENGTH_LONG).show();
                ((DeviceListFragment.DeviceActionListener) getActivity()).connect(config);


            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceListFragment.DeviceActionListener) getActivity()).disconnect();
                        s_count = 0;
                        c_count = 0;
                        DeviceListFragment.state = "avail";
                    }
                });


        mContentView.findViewById(R.id.btn_resume).setClickable(false);

        mContentView.findViewById(R.id.btn_resume).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (pause % 2 == 0) {
                            pause++;
                            Toast.makeText(getActivity(), "Paused !!!", Toast.LENGTH_SHORT).show();
                            Button b = (Button) v.findViewById(R.id.btn_resume);
                            b.setClickable(false);
                            b.setVisibility(View.GONE);
                            b.setText("Resume");
                        } else {
                            pause++;
                            pd.show();
                            Toast.makeText(getActivity(), "Resumed !!!", Toast.LENGTH_SHORT).show();
                            Button b = (Button) v.findViewById(R.id.btn_resume);
                            b.setClickable(false);
                            b.setVisibility(View.GONE);
                            b.setText("Resume");
                        }

                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
//
// Allow user to pick an image from Gallery or other
//                        // registered apps
//                        /////////////////////////////////
//                        ContentResolver cr = getActivity().getContentResolver();
//                        InputStream is = null;
//                        int fsize = 0;
////                        try {
////                            is=cr.openInputStream(Uri.parse(new File("/data/data/com.material.katha.wifidirectmp3/app_Shared/Screenshot_2016-04-11-08-25-12.png").toString()));
////                            // is = cr.openInputStream(uri);
////                            fsize = is.available();
////
////                            // Log.d(WiFiDirectActivity.TAG,"File size is:"+is.available()+"               "+f.getName()+"uri  "+uri + "f  "+ f+ "file name " +f.getName());
////                        } catch (FileNotFoundException e) {
////                            e.printStackTrace();
////                        } catch (IOException e) {
////                            e.printStackTrace();
////                        }
//
//                        //Log.d("katha", fileext + "fileext");
//                        //;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;change fileext = abc.substring(abc.lastIndexOf('.'));
//
//           /*progressDialog = new ProgressDialog(getActivity());
//           progressDialog.setMessage("Sending file:"+abc);
//           progressDialog.show();
//           */
//
//                        // progressDialog = ProgressDialog.show(getActivity(), "Sending","Copying file :" + fileext, true, true);
//                        // makeText(getActivity(), fileext, LENGTH_LONG).show();
//                        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
//                        //  statusText.setText("Sending: " + uri);
//                        // String devicename = "abc";
//                        devicename = device.deviceName;
//                        // Toast.makeText(getActivity(),devicename,Toast.LENGTH_LONG).show();
//
//                       // Log.d("WiFiDirectActivity", "Intent----------- " + Uri.parse(new File("DCIM/Camera/IMG_20160118_090231.jpg").toString()));
//                        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
//                        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
//                        //Log.d("WiFiDirectActivity", "Action" + FileTransferService.ACTION_SEND_FILE + "\n\n\n\n");
//                        ////////////////////serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
//                        //serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH,"DCIM/Camera/IMG_20160118_090231.jpg");
//
//                        //serviceIntent.putExtra(FileTransferService.FILE_SIZE, fsize);
//
//                         serviceIntent.putExtra(FileTransferService.device_name,devicename);
//                        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
//                        String localip = getDottedDecimalIP(getLocalIPAddress());
//                        Log.d("WiFiDirectActivity", "DEVICE_NAME: " + devicename);
//                        serviceIntent.putExtra(FileTransferService.DEVICE_NAME, devicename);
//
//                        if (localip.equals("192.168.49.1")) {
//                            Log.d("WiFiDirectActivity", "Flag is 0.");
//                            //  devicename = device.deviceName;
//                            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, client_ip);
//                            serviceIntent.putExtra(FileTransferService.Client_add, client_ip);
//                            ;
//                        } else {
//                            Log.d("WiFiDirectActivity", "Flag is 1.");
//                            //devicename = device.deviceName;
//                            // Toast.makeText(getActivity(),devicename,Toast.LENGTH_LONG).show();
//                            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
//                                    info.groupOwnerAddress.getHostAddress());
//                            serviceIntent.putExtra(FileTransferService.Client_add, localip);
//
//                        }
//                        getActivity().startService(serviceIntent);
//                        Log.d("WiFiDirectActivity","here");

                        //////////////////////////////////
                        if (!info.groupOwnerAddress.getHostAddress().equals("")) {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("*/*");
                            startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                        }

                    }
                });


        return mContentView;
    }

    public void filelist() {
        String filename = "myfile";
        String data = "";
        File file[] = MainActivity.mydir.listFiles();
        try {
            FileOutputStream outputStream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
            for (int i = 0; i < file.length; i++) {
                data = data + ";" + file[i].getName();
            }
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("WiFiDirectActivity", "Size: " + file.length);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.

        if (requestCode >= 0 && resultCode == -1 && data != null) {


            // count++;
            Uri uri = data.getData();
            String abc = uri.toString();
            // System.out.println(abc);
            Log.d("katha", abc + "abc");
            try {
                datapath = getPath(getActivity(), uri);
                Log.d("WiFiDirectActivity", datapath.substring(datapath.lastIndexOf("/") + 1, datapath.length()));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            String filename = abc.substring(abc.lastIndexOf("/") + 1);
            pd = new ProgressDialog(getActivity());
            pd.setMessage("Sending:" + datapath);
            pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Pause", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    Log.d("WiFiDirectActivity", "pause pressed");
                    resetafterdismiss(0);
                    pause++;
                    //pd.show();
                }
            });
            pd.show();

            File f = new File(uri.getPath());
            //long file_size = f.length();
            ContentResolver cr = getActivity().getContentResolver();
            InputStream is = null;
            int fsize = 0;
            try {
                //is=cr.openInputStream(Uri.parse(new File("DCIM/Camera/IMG_20160118_090231.jpg").toString()));
                is = cr.openInputStream(uri);
                fsize = is.available();

                Log.d("WiFiDirectActivity", "File size is:" + is.available() + "               " + f.getName() + "uri  " + uri + "f  " + f + "file name " + f.getName());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d("WifiDirectActivity", "here " + e);
            } catch (IOException e) {
                Log.d("WifiDirectActivity", "here " + e);
                e.printStackTrace();
            }

            //Log.d("katha", fileext + "fileext");
            //;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;change fileext = abc.substring(abc.lastIndexOf('.'));

           /*progressDialog = new ProgressDialog(getActivity());
           progressDialog.setMessage("Sending file:"+abc);
           progressDialog.show();
           */

            // progressDialog = ProgressDialog.show(getActivity(), "Sending","Copying file :" + fileext, true, true);
            // makeText(getActivity(), fileext, LENGTH_LONG).show();
            TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
            //  statusText.setText("Sending: " + uri);
            // String devicename = "abc";
//            devicename = device.deviceName;
            // Toast.makeText(getActivity(),devicename,Toast.LENGTH_LONG).show();

            Log.d("WiFiDirectActivity", "Intent----------- " + uri);
            Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            Log.d("WiFiDirectActivity", "Action" + FileTransferService.ACTION_SEND_FILE + "\n\n\n\n");
            ////////////////////serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, datapath);

            serviceIntent.putExtra(FileTransferService.FILE_SIZE, fsize);

            //serviceIntent.putExtra(FileTransferService.device_name,devicename);
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
            String localip = getDottedDecimalIP(getLocalIPAddress());
            Localip = localip;
            Log.d("WiFiDirectActivity", "DEVICE_NAME: " + devicename);
            serviceIntent.putExtra(FileTransferService.DEVICE_NAME, devicename);

            if (localip.equals("192.168.49.1")) {
                Log.d("WiFiDirectActivity", "Flag is 0.");
                //  devicename = device.deviceName;
                serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, client_ip);
                serviceIntent.putExtra(FileTransferService.Client_add, client_ip);
                ;
            } else {
                Log.d("WiFiDirectActivity", "Flag is 1.");
                //devicename = device.deviceName;
                // Toast.makeText(getActivity(),devicename,Toast.LENGTH_LONG).show();
                try {
                    serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                            info.groupOwnerAddress.getHostAddress());
                    serviceIntent.putExtra(FileTransferService.Client_add, localip);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Error!!", LENGTH_LONG).show();
                    Log.d("WiFiDirectActivity", "error in catch!!");
                    return;
                }
            }
            getActivity().startService(serviceIntent);
            Log.d("WiFiDirectActivity", "here");
        } else {
            return;
        }

    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;


        this.getView().setVisibility(View.VISIBLE);

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);


        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);

        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());
        view = (TextView) mContentView.findViewById(R.id.local_ip);

        String localip = getDottedDecimalIP(getLocalIPAddress());
        view.setText("Local IP - " + localip);
        if (!localip.equals(SERVER_IP)) {
            if (c_count == 0) {

                Toast.makeText(getActivity(), "This is Client.", Toast.LENGTH_SHORT).show();
                //devicename = device.deviceName;
                // Toast.makeText(getActivity(),devicename,Toast.LENGTH_LONG).show();
                new iptransfer_client(getActivity(), mContentView.findViewById(R.id.status_text)).execute(localip);
                c_count++;
            }
        } else {
            if (s_count == 0) {
                Toast.makeText(getActivity(), "This is Server.", Toast.LENGTH_SHORT).show();

                new iptransfer_server(getActivity(), mContentView.findViewById(R.id.status_text)).execute();
                s_count++;
            }
            //Toast.makeText(getActivity(), "Client IP: "+ client_ip, Toast.LENGTH_SHORT).show();
        }
        view = (TextView) mContentView.findViewById(R.id.client_ip);
        view.setText("Client IP - " + client_ip);


        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.

        new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text)).execute();
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);


        //((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources().getString(R.string.client_text));

        // hide the connect button
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);

        //devicename = device.deviceName;
        // Toast.makeText(getActivity(),device.deviceName,Toast.LENGTH_LONG).show();

    }

    public void resetafterdismiss(int flag) {
        if (flag == 0) {
            mContentView.findViewById(R.id.btn_resume).setVisibility(View.VISIBLE);
            mContentView.findViewById(R.id.btn_resume).setClickable(true);
        } else
            mContentView.findViewById(R.id.btn_resume).setVisibility(View.GONE);
    }

    /**
     * Updates the UI with device data
     *
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        devicename = device.deviceName;
        Toast.makeText(getActivity(), device.deviceName, Toast.LENGTH_LONG).show();
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.deviceName);
        Log.d("katha", "1");

    }

    public byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) {
                            return inetAddress.getAddress();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            // Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        } catch (NullPointerException ex) {
            // Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        }
        return null;
    }

    public String getDottedDecimalIP(byte[] ipAddr) {
        if (ipAddr != null) {
            String ipAddrStr = "";
            for (int i = 0; i < ipAddr.length; i++) {
                if (i > 0) {
                    ipAddrStr += ".";
                }
                ipAddrStr += ipAddr[i] & 0xFF;
            }
            return ipAddrStr;
        } else {
            return "null";
        }
    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        Log.d("katha", "3");
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

    @Override
    public void run() {
        copyFile(is, os, 1);
        // resetafterdismiss(1);
    }


    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     * *
     */

    public static class iptransfer_client extends AsyncTask<String, Void, String> {
        private Context context;
        private TextView statusText;

        /**
         * @param context
         * @param statusText
         */
        public iptransfer_client(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }


        @Override
        protected String doInBackground(String... params) {

            String localip = params[0];
            Socket socket = new Socket();
            try {
                socket.setReuseAddress(true);
                socket.connect((new InetSocketAddress(SERVER_IP, 8990)), 5000);
                Log.d("WiFiDirectActivity", "Client: Socket opened for ip transfer");
                OutputStream os = null;
                os = socket.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);
                oos.writeObject(localip);
                oos.close();
                os.close();
                socket.close();
                Log.d("WiFiDirectActivity", "Transferred ip is: " + localip);

                return localip;
            } catch (IOException e) {
                Log.e("WiFiDirectActivity", e.getMessage());
                return null;
            }
        }
    }


    public static class iptransfer_server extends AsyncTask<Void, Void, String> {
        private Context context;
        private TextView statusText;

        /**
         * @param context
         * @param statusText
         */
        public iptransfer_server(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        protected void onPostExecute(String result) {
            if (result != null) {
                statusText.setText("Client IP: " + result);
                Intent intent = new Intent();
                //intent.setAction(android.content.Intent.ACTION_VIEW);
                //intent.setDataAndType(Uri.parse("file://" + result), "*/*");
                //context.startActivity(intent);
            }

        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(8990);
                Log.d("WiFiDirectActivity", "Server: Socket opened for ip transfer");
                Socket client = serverSocket.accept();

                Log.d("WiFiDirectActivity", "Server: connection done");

                ObjectInputStream ip = new ObjectInputStream(client.getInputStream());

                String arr = null;
                try {
                    arr = (String) ip.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (client_ip == null)
                    client_ip = arr;


                Log.d("WiFiDirectActivity", "Transfered ip is: " + arr);
                //Log.d(WiFiDirectActivity.TAG, "server: copying files " + arr[1]);

                serverSocket.close();
                // return f.getName();
                return arr;
            } catch (IOException e) {
                Log.e("WiFiDirectActivity", e.getMessage());
                return null;
            }
        }

    }


    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;


        /**
         * @param context
         * @param statusText
         */
        public FileServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
            pdr = new ProgressDialog(this.context);
            pdr.setMessage("Receiving File...");
        }


        @Override
        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d("WiFiDirectActivity", "Server: Socket opened");
                Socket client = serverSocket.accept();

                Log.d("WiFiDirectActivity", "Server: connection done");

                ObjectInputStream ip = new ObjectInputStream(client.getInputStream());

                String[] arr = new String[2];
                // String arr = null;
                try {
                    arr = (String[]) ip.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                final File f = new File(Environment.getExternalStorageDirectory() + "/"
                        + context.getPackageName() + "/" + arr[1]);


                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();

                f.createNewFile();
                start_receive = 1;


                if (client_ip == null)
                    client_ip = arr[0];

                Log.d("WiFiDirectActivity", "Copying files from: " + arr[0]);
                Log.d("WiFiDirectActivity", "server: copying files " + arr[1]);


                InputStream inputstream = client.getInputStream();

                FileOutputStream out = new FileOutputStream(f);


                //pdr.show();
                copyFile(inputstream, new FileOutputStream(f) {
                }, 0);
                //pdr.dismiss();


                serverSocket.close();
                return f.getName();
            } catch (IOException e) {
                Log.e("WiFiDirectActivity", e.getMessage());
                Log.e("WiFdataiDirectActivity", e.getMessage());
                return null;
            }
        }


        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                statusText.setText("File copied - " + result);
                Intent intent = new Intent();
                String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                Log.d("WiFiDirectActivity", "Copied on " + mydate);
                dbadapter m = new dbadapter(context);
                m.open();

                m.insertEntry(result, devicename, "Received", mydate);
                Log.d("WiFiDirectActivity", result + " " + devicename + " " + "Received");
                m.close();


                // new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text)).execute();
                //intent.setAction(android.content.Intent.ACTION_VIEW);
                //intent.setDataAndType(Uri.parse("file://" + result), "*/*");
                //context.startActivity(intent);
            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            //statusText.setText("Opening a server socket");


        }

    }

    public static boolean copyFile(InputStream inputStream, OutputStream out, int flag) {
        byte buf[] = new byte[1024];
        int len, bytes_tran = 0;
        long startTime = System.currentTimeMillis();


        if (flag == 0) {
            // pdr.show();
        }


        try {
            while ((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len);

                int count = 0;
                while (pause % 2 == 1) {
                    if (count == 0) {
                        Log.d("WiFiDirectActivity", "Pause is pressed..");
                        Log.d("WiFiDirectActivity", "Bytes transfered till now: " + bytes_tran);
                    }

                    count++;
                }
                bytes_tran += len;
                Log.d("WiFiDirectActivity", "Transfering.." + bytes_tran);
            }
            //hello world
            out.close();
            inputStream.close();
            long endTime = System.currentTimeMillis() - startTime;
            Log.v("", "Time taken to transfer all bytes is : " + endTime);
            Log.d("WiFiDirectActivity", "total bytes transfered:" + bytes_tran);
            // progressDialog.dismiss();
            receive_complete = 1;
            if (flag == 1) {
                pd.dismiss();
            }
        } catch (IOException e) {
            Log.d("WiFiDirectActivity", e.toString());
            return false;
        }
        return true;
    }


    public static int checkpause() {
        if (pause % 2 != 0)
            return 1;
        else
            return 0;
    }


}
