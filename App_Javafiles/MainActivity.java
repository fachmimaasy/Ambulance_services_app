package com.example.shwetapathak.ambulanceservicesfinal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    private Button kdriver,scustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        kdriver=(Button)findViewById(R.id.driver);
        scustomer=(Button)findViewById(R.id.client);
        runtime_permission();

        kdriver.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(MainActivity.this,DriverLoginActivity.class);
                startActivity(i);
                finish();
                return;
            }
        });
        scustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(MainActivity.this,CustomerLoginActivity.class);
                startActivity(i);
                finish();
                return;
            }
        });
    }

    private boolean runtime_permission() {

        if (Build.VERSION.SDK_INT>=23 && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},100);

                return  true;
        }
        return  false;
    }
}


