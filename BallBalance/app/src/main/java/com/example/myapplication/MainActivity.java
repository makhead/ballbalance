package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Button button; // start button
    private TextView GameName; // display game name
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // initialize
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startGame();
            }
        });
        GameName = (TextView)findViewById(R.id.GameName);
        GameName.setTextColor(Color.WHITE); // set game name font color
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR); // disable screen spinning
    }

    public void startGame(){
        Intent intent = new Intent(this, Activity2.class); // change to game interface
        startActivity(intent);
        this.finish();
    }


}