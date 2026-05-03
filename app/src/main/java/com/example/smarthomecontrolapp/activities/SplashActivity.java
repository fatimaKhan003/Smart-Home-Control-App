package com.example.smarthomecontrolapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smarthomecontrolapp.R;

public class SplashActivity extends AppCompatActivity {
ImageView logo;
TextView appname;
TextView tagline;
Animation logoAnim,textAnim;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
init();
logo.startAnimation(logoAnim);
appname.startAnimation(logoAnim);
tagline.startAnimation(textAnim);
        new Handler().postDelayed(()->
        {
            startActivity(new Intent(SplashActivity.this, RegisterActivity.class));
            finish();
        },3000);

    }
    private void init()
    {
        logo=findViewById(R.id.ivSplashLogo);
        appname=findViewById(R.id.tvAppName);
        tagline=findViewById(R.id.tvTagline);
        logoAnim= AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
        textAnim=AnimationUtils.loadAnimation(this,R.anim.slide_up);

    }
}
