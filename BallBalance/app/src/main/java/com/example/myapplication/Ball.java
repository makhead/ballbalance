package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Ball {
    public int x;
    public int y;
    public int xspeed;
    public int yspeed;
    public int zspeed;
    private boolean isjump;
    public int r;
    public int now_r;
    Ball(int xpos, int ypos) {
        x = xpos;
        y = ypos;
        xspeed = 2;
        yspeed = 10;
        isjump =false;
        r = 50;
        now_r = 50;
    }


    void jump(){
        isjump = true;
    }

    void grounded(){
        isjump = false;
    }

    boolean isJump(){
        return  isjump;
    }
}
