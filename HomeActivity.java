package com.example.shwetapathak.ambulanceservicesfinal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import static java.lang.Thread.sleep;

public class HomeActivity extends AppCompatActivity {
    ImageView splashImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        splashImage = (ImageView) findViewById(R.id.imagesplash);

        Animation myanim = AnimationUtils.loadAnimation(this,R.anim.myanimation);

        splashImage.startAnimation(myanim);

        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    sleep(3000);
                    Intent i = new Intent(HomeActivity.this,MainActivity.class);
                    startActivity(i);


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        myThread.start();
    }
}
