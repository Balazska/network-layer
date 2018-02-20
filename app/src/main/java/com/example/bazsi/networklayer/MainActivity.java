package com.example.bazsi.networklayer;

import android.content.Intent;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set buttons to start the right activity
        Button btnRunAsServer = findViewById(R.id.btnRunAsServer);
        btnRunAsServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serverIntent = new Intent(MainActivity.this, ServerActivity.class);
                startActivity(serverIntent);
            }
        });
        Button btnRunAsClient = findViewById(R.id.btnRunAsClient);
        btnRunAsClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent clientIntent = new Intent(MainActivity.this, ClientActivity.class);
                startActivity(clientIntent);
            }
        });
    }
}
