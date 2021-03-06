package com.melecio.futbolito;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private float xPos, xAccel, xVel = 0.0f;
    private float yPos, yAccel, yVel = 0.0f;
    private float xMax, yMax;
    private float frameTime = 0.666f;
    private Bitmap ball, porteriaS, porteriaI;
    private SensorManager sensorManager;
    private Sensor sensorACCELEROMETER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        BallView ballView = new BallView(this);
        setContentView(ballView);

        Point size = new Point();
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(size);
        xMax = (float) size.x - 100;
        yMax = (float) size.y - 340;

        Log.d("pelota", "Xmax: "+xMax);
        Log.d("pelota", "Ymax: "+yMax);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorACCELEROMETER = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sensorACCELEROMETER!=null){
            sensorManager.registerListener(this, sensorACCELEROMETER, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(sensorACCELEROMETER!=null){
            sensorManager.unregisterListener(this);
        }
    }

    private void updateBall() {
        xVel += (xAccel * frameTime);
        yVel += (yAccel * frameTime);

        float xS = (xVel / 2) * frameTime;
        float yS = (yVel / 2) * frameTime;

        xPos -= xS;
        yPos -= yS;

        if (xPos > xMax) {
            xPos = xMax;
        } else if (xPos < 0) {
            xPos = 0;
        }

        if (yPos > yMax) {
            yPos = yMax;
        } else if (yPos < 0) {
            yPos = 0;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            xAccel = sensorEvent.values[0];
            yAccel = -sensorEvent.values[1];
            updateBall();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class BallView extends View {
        public BallView(Context context) {
            super(context);
            Bitmap ballSrc = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
            final int ballWidth = 100, portWidth=290;
            final int ballHeight = 100, portHeight=250;
            ball = Bitmap.createScaledBitmap(ballSrc, ballWidth, ballHeight, true);
            Bitmap porteriaSrcS = BitmapFactory.decodeResource(getResources(), R.drawable.porteria_s);
            porteriaS = Bitmap.createScaledBitmap(porteriaSrcS, portWidth, portHeight, true);
            Bitmap porteriaSrcI = BitmapFactory.decodeResource(getResources(), R.drawable.porteria);
            porteriaI = Bitmap.createScaledBitmap(porteriaSrcI, portWidth, portHeight, true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(porteriaS,395, 0,null);
            canvas.drawBitmap(porteriaI,395, yMax-150,null);
            canvas.drawBitmap(ball, xPos, yPos, null);
            invalidate();
            Log.d("pelota", "Xpos: "+xPos);
            Log.d("pelota", "Ypos: "+yPos);
        }
    }
}


