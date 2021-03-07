package com.melecio.futbolito;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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


    private String contadorS = "0";
    private String contadorI = "0";
    private float xPos, xAccel, xVel = 0.0f;
    private float yPos, yAccel, yVel = 0.0f;
    private float xMax, yMax, portIni, portFin;
    private float frameTime = 0.666f;
    private final int ballWidth = 100, portWidth=290;
    private final int ballHeight = 100, portHeight=250;
    private int scoreA, scoreB = 0;
    private boolean gol = false;
    private Bitmap ball, porteriaA, porteriaB;
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
        xMax = (float) size.x - ballWidth;
        yMax = (float) size.y - ballHeight - 240;

        xPos = ((xMax+ballWidth)/2)-(ballWidth/2);
        yPos = ((yMax+ballWidth)/2)-(ballWidth/2);

        portIni = ((xMax+ballWidth)/2)-(portWidth/2);
        portFin = portIni + portWidth;

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
        } else if((xPos>portIni-ballWidth&&xPos<portIni) && (yPos>yMax-portHeight||yPos<portHeight)){
            xPos = portIni-ballWidth;
        } else if((xPos<portFin&&xPos>portFin-50) && (yPos>yMax-portHeight||yPos<portHeight)){
            xPos = portFin;
        } else if(!gol && (xPos>portIni && xPos<portFin) && (yPos>yMax+ballHeight-portHeight || yPos<portHeight-ballHeight)){
            gol = true;
            if(yPos<portHeight-ballHeight) {
                scoreA++;
                contadorS = String.valueOf(scoreA);
            }
            else if(yPos>yMax+ballHeight-portHeight){
                scoreB++;
                contadorI = String.valueOf(scoreB);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("¡GOOOOL!").setMessage("Puntaje A: "+scoreA+"\n\nPuntaje B: "+scoreB);
            builder.setPositiveButton("CONTINUAR", (dialog, id) -> {
                dialog.dismiss();
                xPos = ((xMax+ballWidth)/2)-(ballWidth/2);
                yPos = ((yMax+ballWidth)/2)-(ballWidth/2);
                gol = false;
            });
            builder.setNegativeButton("REINICIAR", (dialog, id) -> {
                scoreA = 0;
                scoreB = 0;
                contadorS = String.valueOf(scoreA);
                contadorI = String.valueOf(scoreB);
                dialog.dismiss();
                xPos = ((xMax+ballWidth)/2)-(ballWidth/2);
                yPos = ((yMax+ballWidth)/2)-(ballWidth/2);
                gol = false;
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        if (yPos > yMax) {
            yPos = yMax;
        } else if (yPos < 0) {
            yPos = 0;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER && !gol) {
            xAccel = sensorEvent.values[0];
            yAccel = -sensorEvent.values[1];
            updateBall();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class BallView extends View {

        Paint paintTexto = new Paint();

        public BallView(Context context) {
            super(context);
            Bitmap ballSrc = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
            ball = Bitmap.createScaledBitmap(ballSrc, ballWidth, ballHeight, true);
            Bitmap porteriaSrcS = BitmapFactory.decodeResource(getResources(), R.drawable.porteria_s);
            porteriaA = Bitmap.createScaledBitmap(porteriaSrcS, portWidth, portHeight, true);
            Bitmap porteriaSrcI = BitmapFactory.decodeResource(getResources(), R.drawable.porteria);
            porteriaB = Bitmap.createScaledBitmap(porteriaSrcI, portWidth, portHeight, true);
        }

        @Override
        protected void onDraw(Canvas canvas) {

            //Marcador izquierdo
            paintTexto.setColor(Color.BLACK);
            paintTexto.setStyle(Paint.Style.FILL);
            paintTexto.setTextSize(250);
            canvas.drawText(
                    contadorS, 10, yMax/2 ,paintTexto
            );

            //Marcador derecho
            canvas.drawText(
                    contadorI, xMax-50, yMax/2 ,paintTexto
            );

            canvas.drawBitmap(porteriaA, portIni, 0,null);
            canvas.drawBitmap(porteriaB, portIni, yMax+ballHeight-portHeight,null);
            canvas.drawBitmap(ball, xPos, yPos, null);
            invalidate();
            Log.d("pelota", "Xpos: "+xPos);
            Log.d("pelota", "Ypos: "+yPos);
        }
    }
}


