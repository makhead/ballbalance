package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.lang.Math;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Activity2 extends AppCompatActivity {
    //Timer
    private Timer mTimer; // the ball position refresh timer
    private Timer mTimerScore; // the score count and refresh timer
    private CountDownTimer ballJumpSizeCounter; // to control the size of the ball using a function
    private Timer EventMsgTimer; // to count the timer and erase the "jump is ready" message
    private Timer DisplayMsgTimer;
    private Timer randomEventActivateTimer; //not used
    private CountDownTimer randomEventCountDownTimer; // to manage the event message to vanish
    private Timer randomEventMsgTimer;  // determine the wind speed when activity and display the event message
    private Timer ballJumpCoolDownTimer; // the jump cool down counter
    private Timer plateDestroyTimer; // the interval time when a event that a plate disappear
    private CountDownTimer plateFlashCountDownTimer; // to handle the animation of flashing plate
    //layout
    private FrameLayout frameLayout; // the background layout
    //Elements
    private ImageView ballView; // the ball image view
    private TextView scoreLabel; // the score text view
    private TextView DisplayMsg; // to display the wind speed
    private TextView EventMsg; // to display the text jump is ready
    private Ball ball ; // save to info of the ball
    private int score = 0; // the score at present
    private TextView testsensor; // used for debug
    private ImageView plateView; // used to get to size of the plate
    private ImageView imageView; // used to point to the vanishing plate to control its color and visibility
    private Context context;
    private TableRow tableRow; // table layout, to find the vanish plate image view
    private TableLayout tableLayout; // table layout to store all the plate

    //sensor
    private SensorEventListener gravitySensorLister;
    private SensorManager sensorManager;
    private Sensor sensor;

    //map
    private boolean map[][] = new boolean[10][8]; // bitmap of all the plate to record which plate is valid
    private int plateX = 0; // the next plate pos to vanish
    private int plateY = 0; // the next plate pos to vanish
    //Size
    private int screenHeight; // to store the screen height
    private int screenWidth;
    private int plateWidth;
    private int plateHeight;
    private float density;
    //flag
    private boolean action_flag = false; // blocks to jump while jumping
    //parameter
    private final double speedRatio = 10;
    private int windSpeedX = 0;
    private int windSpeedY = 0;
    private int direction = 0;
    private String directionString = "E";
    private int debugX = 0;
    private int debugY = 0;
    private int debugZ = 0;
 //   private ImageView DebugBall;
    private Random r = new Random();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        // used to calculate the plate pixel position in order to judge if you lose
        final WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        Point size = new Point();
        display.getSize(size);
        density = metrics.density;


        scoreLabel = (TextView)findViewById(R.id.score);
        testsensor = (TextView)findViewById(R.id.sensor);
        DisplayMsg = (TextView)findViewById(R.id.DisplayMsg);
        EventMsg = (TextView)findViewById(R.id.EventMsg);
        ballView = (ImageView)findViewById(R.id.ball);
        plateView = (ImageView)findViewById(R.id.plate0_0);
        frameLayout = (FrameLayout)findViewById(R.id.frameLayout);
        tableLayout = (TableLayout)findViewById(R.id.tableLayout);
  //      DebugBall = (ImageView)findViewById(R.id.ballTest);
 //       DebugBall.setColorFilter(Color.RED);

        //init ball pos and size
        ball = new Ball(size.x/2,size.y/2);
        ball.r = size.x/8;
        screenWidth = size.x;
        screenHeight = size.y;

        //timer
        mTimer = new Timer();
        mTimerScore = new Timer();
        EventMsgTimer = new Timer();
        DisplayMsgTimer = new Timer();
        randomEventActivateTimer = new Timer();;
        randomEventMsgTimer = new Timer();
        ballJumpCoolDownTimer = new Timer();
        plateDestroyTimer = new Timer();
        plateFlashCountDownTimer = new CountDownTimer(6000,1000) {
            @Override
            public void onTick(long millisUntilFinished) { // the animation of plate flashing
                if((int)(millisUntilFinished/1000)%2==0)
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                                imageView.setColorFilter(Color.RED);
                        }
                    });
                else
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setColorFilter(Color.WHITE);

                        }
                    });
            }

            @Override
            public void onFinish() {
                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setVisibility(View.INVISIBLE);// the plate vanish
                        map[plateY][plateX]= false;
                    }
                });
            }
        };
        ballJumpSizeCounter = new CountDownTimer(3000, 100) {

            public void onTick(long millisUntilFinished) {
                ball.jump();
                ball.now_r = (int)(ball.r * (0.5* Math.pow((3000 - millisUntilFinished)/1500.0-1,2) +0.5)); // to control the size of the ball while jumping
             //   double y = 0.5* Math.pow((3000 - millisUntilFinished)/1500.0-1,2) +0.5;
                ballView.post(new Runnable() {
                    @Override
                    public void run() {
                        ballView.getLayoutParams().width = ball.now_r;
                        ballView.getLayoutParams().height = ball.now_r;
                    }
                });

            }
            public void onFinish() {
                ballView.post(new Runnable() {
                    @Override
                    public void run() {
                        ballView.getLayoutParams().width = ball.r;  // to recover the ball size
                        ballView.getLayoutParams().height = ball.r;
                        ball.now_r = ball.r;
                    }
                });
                EventMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        EventMsg.setVisibility(View.VISIBLE);  // to display jump is ready message
                        EventMsg.setText(getString(R.string.ballCoolDownFinishedMsg));
                        EventMsgTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                ball.grounded();
                                EventMsg.setVisibility(View.INVISIBLE);
                            }
                        },1000);
                    }
                });
            }

        };



        randomEventCountDownTimer = new CountDownTimer(10000,5000) {
            @Override
            public void onTick(long millisUntilFinished) {
                DisplayMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        DisplayMsg.setText(getString(R.string.RandomEventRunning,directionString)); // display event message
                        DisplayMsg.setVisibility(View.VISIBLE);
                    }
                });

            }

            @Override
            public void onFinish() {
                DisplayMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        DisplayMsg.setVisibility(View.INVISIBLE); // erase event message
                    }
                });
                windSpeedX = 0; // reset wind speed from event
                windSpeedY = 0;
            }
        };



        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);




        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR); // disable screen spinning
        setTimerTask(); // start essential timer

        //gravity sensor
        gravitySensorLister = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                ball.xspeed = -(int)(event.values[0]*speedRatio); // transform values from gravity sensor to ball speed
                ball.yspeed = (int)(event.values[1]*speedRatio);
                ball.zspeed = (int)(event.values[2]);
                testsensor.post(new Runnable() {
                    @Override
                    public void run() {
                        testsensor.setText(getString(R.string.sensor,debugX,debugY,debugZ)); // used to debug
                    }
                });
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        sensorManager.registerListener(gravitySensorLister,sensor, SensorManager.SENSOR_DELAY_NORMAL);


        //initialize
        ballView.setY(ball.y);
        ballView.setX(ball.x);
        ballView.getLayoutParams().width = ball.r;
        ballView.getLayoutParams().height = ball.r;
        scoreLabel.setText("Score :" + score);
        DisplayMsg.setVisibility(View.INVISIBLE);
        EventMsg.setVisibility(View.INVISIBLE);
        init_map();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // cancel timer
        mTimerScore.cancel();
    }

    @Override
    protected void onResume(){
        super.onResume();

    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onStop(){
        super.onStop();
        sensorManager.unregisterListener(gravitySensorLister);
    }

    private void setTimerTask(){

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                changePos(); // refresh ball pos
                if(checkEnd()) // to see if the player is lose
                    endGame(); // end the game
            }
        },1000,30);

        mTimerScore.schedule(new TimerTask() {
            @Override
            public void run() {
                score += 100;
                scoreLabel.post(new Runnable() {
                    @Override
                    public void run() {
                        scoreLabel.setText(getString(R.string.scoreLabel,score)); // change the score text display
                    }
                });

            }
        },1000,100);

        randomEventMsgTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Random r = new Random(); // generate a random windspeed
                int temp = r.nextInt(15)+1;
                direction = r.nextInt(9);
                switch(direction){
                    case 1:  //E
                        windSpeedX = temp;
                        windSpeedY = 0;
                        directionString = String.valueOf(temp) + "E";
                        break;
                    case 2: //SE
                        windSpeedX = temp;
                        windSpeedY = -temp;
                        directionString = String.valueOf(temp) + "SE";
                        break;
                    case 3: //S
                        windSpeedX = 0;
                        windSpeedY = -temp;
                        directionString = String.valueOf(temp) + "S";
                        break;
                    case 4: // SW
                        windSpeedX = -temp;
                        windSpeedY = -temp;
                        directionString = String.valueOf(temp) + "SW";
                        break;
                    case 5: //W
                        windSpeedX = -temp;
                        windSpeedY = 0;
                        directionString = String.valueOf(temp) + "W";
                        break;
                    case 6: //NW
                        windSpeedX = -temp;
                        windSpeedY = temp;
                        directionString = String.valueOf(temp) + "NW";
                        break;
                    case 7: //N
                        windSpeedX = 0;
                        windSpeedY = temp;
                        directionString = String.valueOf(temp) + "N";
                        break;
                    case 8: //NE
                        windSpeedX = temp;
                        windSpeedY = temp;
                        directionString = String.valueOf(temp) + "NE";
                        break;
                }
                randomEventCountDownTimer.start(); // display the message count the timer to stop the event
            }
        },5000,20000);
        
        plateDestroyTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                int x = r.nextInt(8) % 8;
                int y = r.nextInt(10) % 10;
                while(map[y][x]==false){ // find the next eliminate plate
                    x = r.nextInt(8) % 8;
                    y = r.nextInt(10) % 10;
                }
                //map[y][x]= false;
                setImageView(x,y); // let image view point to the next eliminate plate
                plateFlashCountDownTimer.start(); // start eliminate plate animation
            }
        },5000,10000);


    }



    public void endGame(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent); // turn the interface to start game interface
        this.finish();
        mTimer.cancel();
        mTimerScore.cancel();
        randomEventActivateTimer.cancel();
    }

    public void changePos(){ // calculate the ball next pos and refresh to ball image pos
        ball.x += ball.xspeed + windSpeedX;
        ball.y += ball.yspeed + windSpeedY;

        ballView.setY(ball.y);
        ballView.setX(ball.x);
    //    DebugBall.setY(ball.y+ball.r/2);
    //    DebugBall.setX(ball.x+ball.r/2);
        //if(checkEnd())
        //    endGame();
        //jump
        if(ball.zspeed>14  && !ball.isJump() ){
    //        ballJumpSizeCounter.start();

        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(!action_flag){
            action_flag = true;
            ballJumpSizeCounter.start(); // if touch ,start jump and jump cool down
            ballJumpCoolDownTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    action_flag = false;
                }
            },4000);
        }
        return super.onTouchEvent(event);
    }


    public boolean checkEnd(){ // check if the game has end
        if(ball.x<-ball.r/2)
            return true;
        if(ball.y<frameLayout.getHeight()/2-5*(plateView.getHeight())-(screenHeight-frameLayout.getHeight()))
            return true;
        if(ball.x>frameLayout.getWidth()-ball.r/2)
            return true;
        if(ball.y>frameLayout.getHeight()/2+5*(plateView.getHeight())-(screenHeight-frameLayout.getHeight()))
            return true;
        if(!ball.isJump())
        {
            int temp1[] = new int [2];
            plateView.getLocationInWindow(temp1);
            int x = (ball.x-ball.r/2) / plateView.getWidth();
            int y = (ball.y-ball.r/2-(screenHeight-frameLayout.getHeight())) / plateView.getHeight();
            debugZ = frameLayout.getHeight();

            if(x<0) x=0;
            else if(x>7) x=7;
            else if(y<0) y=0;
            else if(y>9) y=9;
            if(!map[y][x])
                return true;
        }

        return false;
    }

    public void setImageView(int x, int y){
        tableRow = (TableRow)tableLayout.getChildAt(y); // let image view point to the plate that need to be eliminated
        imageView = (ImageView)tableRow.getChildAt(x);
        plateX = x;
        plateY = y;
    }

    public void init_map(){
        for(int i=0;i<10;i++)
            for(int j=0;j<8;j++)
                map[i][j]=true;
    }
}



