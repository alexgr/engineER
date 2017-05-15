package com.example.engineER;


import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

// Clasa abstracta pentru un OpenGL ES Renderer
public abstract class GLRenderer implements Renderer
{

	private final static boolean DEBUG = true;
    private boolean mFirstDraw;
    private boolean mSurfaceCreated;
    private int mWidth;
    private int mHeight;
    private long mLastTime;
    private int mFPS;



    // Initializeaza valorile default
    public GLRenderer() 
    {
        mFirstDraw = true;
        mSurfaceCreated = false;
        mWidth = -1;
        mHeight = -1;
        mLastTime = System.currentTimeMillis();
        mFPS = 0;
    }

    @Override
    public void onSurfaceCreated(GL10 notUsed, EGLConfig config)
    {
        if (DEBUG) 
        {
            Log.i("OpenGL ES Ex 2", "Suprafata a fost creata.");
        }
        mSurfaceCreated = true;
        mWidth = -1;
        mHeight = -1;
    }

    @Override
    public void onSurfaceChanged(GL10 notUsed, int width, int height)
    {
        if (!mSurfaceCreated && width == mWidth && height == mHeight) 
        {
            if (DEBUG) 
            {
                Log.i("OpenGL ES Ex 2", "Suprafata schimbata dar sunt folosite aceleasi valori.");
            }
            return;
        }
        if (DEBUG) 
        {
            String msg = "Suprafata schimbata ! Noile valori -> width:" + width + " height:" + height;
            
            if (mSurfaceCreated) 
            {
                msg += " contextul pierdut.";
            } 
            else 
            {
                msg += ".";
            }
            Log.i("OpenGL ES Ex 2", msg);
        }

        mWidth = width;
        mHeight = height;

        onCreate(mWidth, mHeight, mSurfaceCreated);
        mSurfaceCreated = false;
    }

    @Override
    // Metoda care deseneaza fiecare cadru
    public void onDrawFrame(GL10 notUsed)
    {
        onDrawFrame(mFirstDraw);

        if (DEBUG) 
        {
            mFPS++;
            long currentTime = System.currentTimeMillis();
            if (currentTime - mLastTime >= 1000) {
                mFPS = 0;
                mLastTime = currentTime;
            }
        }

        if (mFirstDraw) 
        {
            mFirstDraw = false;
        }
    }

    public int getFPS() 
    {
        return mFPS;
    }

    // Metoda abstracta ce va trebui sa fie implmentata de clasa care va extinde clasa curenta
    public abstract void onCreate(int width, int height,
            boolean contextLost);

    // Metoda abstracta ce va trebui sa fie implmentata de clasa care va extinde clasa curenta
    public abstract void onDrawFrame(boolean firstDraw);

}