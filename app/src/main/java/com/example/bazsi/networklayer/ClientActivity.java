package com.example.bazsi.networklayer;

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.bazsi.networklayer.interfaces.ServiceDiscoverListener;

import java.net.Socket;

public class ClientActivity extends AppCompatActivity {

    TextView response;
    EditText editTextAddress, editTextPort;
    Button buttonConnect, buttonClear, discoverButton;
    Service service;
    ServiceDiscoverListener serviceDiscoverListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        Handler mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                final String chatLine = msg.getData().getString("msg");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tv=findViewById(R.id.responseTextView);
                        tv.append(chatLine+'\n');
                    }
                });
            }
        };
        service = new Service(getApplicationContext(),mUpdateHandler);
        service.initializeNsd();

        editTextAddress = (EditText) findViewById(R.id.addressEditText);
        editTextPort = (EditText) findViewById(R.id.portEditText);
        buttonConnect = (Button) findViewById(R.id.connectButton);
        buttonClear = (Button) findViewById(R.id.clearButton);
        response = (TextView) findViewById(R.id.responseTextView);
        // set discover button
        discoverButton = (Button)findViewById(R.id.discoverButton);
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                service.discoverServices(new ServiceDiscoverListener() {
                    @Override
                    public void serviceFound(Socket socket, final NsdServiceInfo mService) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                response.append(mService.getServiceName()+'\n'+mService.getHost()+'\n'+mService.getPort()+'\n');

                                Client myClient = new Client(mService.getHost().getHostAddress(), mService.getPort(), response,ClientActivity.this);
                                myClient.execute();
                            }
                        });

                    }
                });
            }
        });

        buttonConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Client myClient = new Client(editTextAddress.getText()
                        .toString(), Integer.parseInt(editTextPort
                        .getText().toString()), response, ClientActivity.this);
                myClient.execute();
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                response.setText("");
            }
        });
    }

}
