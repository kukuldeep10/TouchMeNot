package com.example.dell.touch_me_not_cetpa;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    ImageView iv;
    SensorManager sm;
    Sensor proximitySensor,acceleroeterSensor;
    int[] imagesID=new int[69];
     int currentImage=68;
     Handler handlerNear=new Handler();
     Handler handlerFar=new Handler();

     Runnable runNear=new Runnable() {
        @Override
        public void run() {
            if(currentImage>0)
            {
                iv.setImageResource(imagesID[--currentImage]);
                handlerNear.postDelayed(runNear,30);
            }
        }
    };
     Runnable runfar=new Runnable() {
         @Override
         public void run() {
             if(currentImage<68)
             {
                 iv.setImageResource(imagesID[++currentImage]);
                 handlerFar.postDelayed(runfar,30);
             }
         }
     };
     int mov_count=2;
     int mov_threashold=4;
     float alpha=0.8f;
     int shake_intervel=50;
     int counter=0;
     long firstMoveTime;
     float gravity[]=new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       //find the widgets....
        iv = findViewById(R.id.imageView);
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);

        for (int i = 0; i < sensors.size(); i++) {
            String name = sensors.get(i).getName();
            String company = sensors.get(i).getVendor();
            Toast.makeText(this, name + ":" + company, Toast.LENGTH_LONG).show();

        }
        proximitySensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
       // LightSensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
        acceleroeterSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, acceleroeterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        initialise();
    }


  public   void initialise()
    {
        for(int i=1;i<=69;i++)
        {
            int id=getResources().getIdentifier("rose"+i,"drawable",getPackageName());
            imagesID[i-1]=id;

        }
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor==proximitySensor)
        {
            float value = sensorEvent.values[0];
            if (value < proximitySensor.getMaximumRange())

            {
                if (handlerFar != null) {
                    handlerFar.removeCallbacks(runfar);

                }
                handlerNear = new Handler();

                handlerNear.post(runNear);

            } else {
                if (handlerNear != null) {
                    handlerNear.removeCallbacks(runNear);
                }
                handlerFar = new Handler();
                handlerFar.post(runfar);
            }
        }

               /*if(sensorEvent.sensor==LightSensor)
                {

                }*/
                else if(sensorEvent.sensor==acceleroeterSensor)
                {
                    float x=sensorEvent.values[0];
                    float y=sensorEvent.values[1];
                    float z=sensorEvent.values[2];
                    float max=maxAcceleration(x,y,z);
                    if(max>mov_threashold)
                    {
                        if(counter==0)
                        {
                            counter++;

                        }

                        else
                        {
                            firstMoveTime=System.currentTimeMillis();
                            long now=System.currentTimeMillis();
                            long diff=now-firstMoveTime;
                            if(diff<shake_intervel)
                            {
                                counter++;
                            }
                            else
                            {
                                counter=0;
                                firstMoveTime=System.currentTimeMillis();
                                return;

                            }
                            if(counter>=mov_count)
                            {
                                RelativeLayout rl=(RelativeLayout)findViewById(R.id.rl);
                                Random random=new Random();
                                rl.setBackgroundColor(Color.rgb(random.nextInt(255),
                                        random.nextInt(255),
                                        random.nextInt(255)));
                            }
                        }
                    }
                }
            }



    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }



        float maxAcceleration ( float x, float y, float z)
        {
            gravity[0] = calGravity(x, 0);
            gravity[1] = calGravity(x, 1);
            gravity[2] = calGravity(x, 2);
            float fx = x - gravity[0];
            float fy = x - gravity[1];
            float fz = x - gravity[2];
            float max1 = Math.max(fx, fy);
            return Math.max(max1, fz);

        }


    private float calGravity(float x, int i) {
        return alpha*gravity[i]+(1-alpha)*x;

    }

}

