package com.example.bazsi.networklayer;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by BalazsAdmin on 2/20/2018.
 */

public class Server {
        Activity context;
        ServerSocket serverSocket;
        String message = "";
        static final int socketServerPORT = 8080;
        Service service;

        List<Socket> socketList = new ArrayList<>();

        public Server(final Activity context0) {
            Handler mUpdateHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    final String chatLine = msg.getData().getString("msg");
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView tv=context.findViewById(R.id.msg);
                            tv.append(chatLine+'\n');
                        }
                    });
                }
            };

            service = new Service(context0.getApplicationContext(),mUpdateHandler);
            service.initializeNsd();
            service.registerService(8080);


            context = context0;
            Thread socketServerThread = new Thread(new SocketServerThread());
            socketServerThread.start();
        }

        public int getPort() {
            return socketServerPORT;
        }

        public void onDestroy() {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        private class SocketServerThread extends Thread {

            int count = 0;

            @Override
            public void run() {
                try {
                    // create ServerSocket using specified port
                    serverSocket = new ServerSocket(socketServerPORT);

                    while (true) {
                        // block the call until connection is created and return
                        // Socket object
                        Socket socket = serverSocket.accept();
                        //socketList.add(new Socket(socket.getInetAddress(),socket.getPort()+1));

                        //read data from client
                        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                        message =(String) in.readObject();
                        //-----
                        count++;
                        message += "#" + count + " from "
                                + socket.getInetAddress() + ":"
                                + socket.getPort() + "\n";

                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView msg = context.findViewById(R.id.msg);
                                msg.setText(message);
                            }
                        });

                        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                        outputStream.writeObject(socket.getInetAddress().getAddress()+";"+socket.getPort());

                        in.close();
                        outputStream.close();
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

        private class SocketServerReplyThread extends Thread {

            private Socket hostThreadSocket;
            int cnt;

            SocketServerReplyThread(Socket socket, int c) {
                hostThreadSocket = socket;
                cnt = c;
            }

            @Override
            public void run() {
                OutputStream outputStream;
                String msgReply = "Hello from Server, you are #" + cnt;

                try {
                    outputStream = hostThreadSocket.getOutputStream();
                    PrintStream printStream = new PrintStream(outputStream);
                    printStream.print(msgReply);
                    printStream.close();

                    message += "replayed: " + msgReply + "\n";
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView msg = context.findViewById(R.id.msg);
                            msg.setText(message);
                        }
                    });
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    message += "Something wrong! " + e.toString() + "\n";
                }
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView msg = context.findViewById(R.id.msg);
                        msg.setText(message);

                        //sendBroadcastMessage("broadcast");
                    }
                });
            }

        }

        public String getIpAddress() {
            String ip = "";
            try {
                Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (enumNetworkInterfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = enumNetworkInterfaces
                            .nextElement();
                    Enumeration<InetAddress> enumInetAddress = networkInterface
                            .getInetAddresses();
                    while (enumInetAddress.hasMoreElements()) {
                        InetAddress inetAddress = enumInetAddress
                                .nextElement();

                        if (inetAddress.isSiteLocalAddress()) {
                            ip += "Server running at : "
                                    + inetAddress.getHostAddress();
                        }
                    }
                }

            } catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                ip += "Something Wrong! " + e.toString() + "\n";
            }
            return ip;
        }

        public void sendBroadcastMessage(final String message){
            if(socketList.size()>0){
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        Socket socket = socketList.get(0);
                        try {
                            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                            outputStream.writeObject(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                }.execute();
            }
        }
}
