package com.example.bazsi.networklayer;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by BalazsAdmin on 2/20/2018.
 */

public class Client extends AsyncTask<Void, Void, Void> {

    String dstAddress;
    int dstPort;
    String response = "";
    TextView textResponse;
    ServerSocket clientServer;
    Activity activity;

    Client(String addr, int port, TextView textResponse, Activity activity0) {
        dstAddress = addr;
        dstPort = port;
        this.textResponse = textResponse;
        activity = activity0;
    }

    @Override
    protected Void doInBackground(Void... arg0) {

        Socket socket = null;
        ObjectInputStream inputStream = null;
        ObjectOutputStream outputStream = null;
        try {
            socket = new Socket(dstAddress, dstPort);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
                    1024);
            byte[] buffer = new byte[1024];

            int bytesRead;
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            String message = "HelloServer";
            outputStream.writeObject(message);

            inputStream = new ObjectInputStream(socket.getInputStream());
			/*
             * notice: inputStream.read() will block if no data return
			 */
            response = inputStream.readObject().toString();
            String[] addressPort = response.split(";");
            socket.close();
            int localPort = socket.getLocalPort();
            socket = null;
            clientServer = new ServerSocket();
            clientServer.setReuseAddress(true);
            clientServer.bind(new InetSocketAddress(localPort+1));
            new SocketServerThread().run();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "IOException: " + e.toString();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(inputStream!= null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        textResponse.setText(response);
        super.onPostExecute(result);
    }

    private class SocketServerThread extends Thread {

        int count = 0;

        @Override
        public void run() {
            try {

                while (true) {
                    // block the call until connection is created and return
                    // Socket object
                    Socket socket = clientServer.accept();
                    String message;
                    //read data from client
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    message =(String) in.readObject();
                    //-----
                    count++;
                    message += "#" + count + " from "
                            + socket.getInetAddress() + ":"
                            + socket.getPort() + "\n";

                    final String finalMessage = message;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView msg = activity.findViewById(R.id.msg);
                            textResponse.setText(finalMessage);
                        }
                    });

                    in.close();
                        /*
                        SocketServerReplyThread socketServerReplyThread =
                                new SocketServerReplyThread(socket, count);
                        socketServerReplyThread.run();
                        */

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
