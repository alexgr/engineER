package com.example.engineER;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.*;
import android.widget.Button;

import java.util.ArrayList;


public class EngineActivity extends Activity implements SensorEventListener {

    View pauseContainer;
    View resumeButton;
    View endButton;

    EngineActivity myinstance;

    private SensorManager sm = null;
    private float[] mGData = new float[3];
    private float[] mMData = new float[3];
    private float[] mR = new float[16];
    private float[] mI = new float[16];
    private float[] mOrientation = new float[3];
    private int mCount;

    GLES20Renderer myGameRenderer;
    GLES20Renderer myMenuRenderer;

    /**
     * Called when the activity is first created.
     */

    // Referinta catre suprafata GL ce va fi folosita
    private GLSurfaceView mGLSurfaceView;
    private static EngineActivity instance = null;

    public static EngineActivity getInstance() {
        return instance;
    }

    // Metoda pentru a verifica daca device-ul suporta OpenGL ES 2.0
    private boolean hasGLES20()
    {
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        Log.i("OpenGL ES Ex 1", "Versiunea ES suportata :" + info.reqGlEsVersion);

        return info.reqGlEsVersion >= 0x20000;
    }

    @Override
    // In onCreate-ul activitatii se creaza suprafata OpenGL si se initializeaza contextul OpenGL
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        myinstance = this;
        if (hasGLES20())
        {
            Log.i("OpenGL ES Ex 1", "Device-ul suporta ES 2.0");

            instance = this;
            sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            this.newGame();

        } else
        {

            Log.i("OpenGL ES Ex 1", "Device-ul NU suporta ES 2.0");

            return;
            // Time to get a new phone, OpenGL ES 2.0 not supported.
        }

        //setContentView(mGLSurfaceView);
    }

    /*
	 * Creates the menu and populates it via xml
	 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return true;
    }

    /*
	 * On selection of a menu item
	 */
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.quit:				// Quit the program
                quit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // finds spacing
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }


    // Quit the app
    private void quit() {
        //super.onDestroy();
        this.finish();
    }

    // rotation
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    // shader constants
    private final int GOURAUD_SHADER = 0;
    private final int PHONG_SHADER = 1;
    private final int NORMALMAP_SHADER = 2;


    // object constants
    private final int OCTAHEDRON = 0;
    private final int TETRAHEDRON = 1;
    private final int CUBE = 2;

    // touch events
    private final int NONE = 0;
    private final int DRAG = 0;
    private final int ZOOM = 0;

    // pinch to zoom
    float oldDist = 100.0f;
    float newDist;

    int mode = 0;

    public static ArrayList<CharacterControllerComponent> gameControllers = new ArrayList<CharacterControllerComponent>();

    int fpos=0;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        switch (e.getAction()) {


            case MotionEvent.ACTION_DOWN:
                mode = DRAG;

                if(mGLSurfaceView!=null && myGameRenderer!=null) {
                    for (CharacterControllerComponent cc : gameControllers) {
                        cc.forwardMovement(fpos--);
                    }
                }

                return true;

            case MotionEvent.ACTION_UP:
                mode = NONE;

                if(mGLSurfaceView!=null && myGameRenderer!=null) {
                    for (CharacterControllerComponent cc : gameControllers) {
                        cc.pauseAction();
                    }
                }

                break;

        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Sensor gsensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor msensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sm.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(this, msensor, SensorManager.SENSOR_DELAY_GAME);

        // Trebuie sa fie apelat onResume() al suprafetei OpenGL
        if (mGLSurfaceView != null)
        {
            mGLSurfaceView.onResume();


        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        sm.unregisterListener(this);
        // Trebuie sa fie apelat onPause() al suprafetei OpenGL
        if (mGLSurfaceView != null)
        {
            mGLSurfaceView.onPause();

        }
    }

    protected void newGame() {
        setContentView(R.layout.main);

        Button nextButton = (Button) findViewById(R.id.newgame);
        Button prevButton = (Button) findViewById(R.id.quit);

        nextButton.setOnClickListener(startGameListener);
        prevButton.setOnClickListener(exitGameListener);
    }



    protected void startGame() {



        //gameControllers = new ArrayList<CharacterControllerComponent>();

        setContentView(R.layout.game);
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.glsurfaceview);

        // Seteaza un context compatibil OpenGL ES 2.0
        mGLSurfaceView.setEGLContextClientVersion(2);

        // setPreserveEGLContextOnPause are nevoie de min API 11
        mGLSurfaceView.setPreserveEGLContextOnPause(true);
        // Se seteaza clasa de randare in care practic se va pune tot ceea ce tine de randarea OpenGL
        mGLSurfaceView.setRenderer(myGameRenderer=new GLES20Renderer("game"));


        pauseContainer = findViewById(R.id.hidecontainer);


        resumeButton = findViewById(R.id.resume);
        resumeButton.setOnClickListener(resumeListener);
        endButton = findViewById(R.id.endgame);
        endButton.setOnClickListener(prevStepListener);

        // Find our buttons
        Button pauseButton = (Button) findViewById(R.id.pause);
        Button hornButton = (Button) findViewById(R.id.horn);
        Button speedButton = (Button) findViewById(R.id.speed);

        // Wire each button to a click listener
        pauseButton.setOnClickListener(PauseListener);
        hornButton.setOnClickListener(HornListener);
        speedButton.setOnClickListener(SpeedListener);

    }

    private void gameMenu() {

        setContentView(R.layout.game_menu);

        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.glsurfaceview);

        // Seteaza un context compatibil OpenGL ES 2.0
        mGLSurfaceView.setEGLContextClientVersion(2);

        // setPreserveEGLContextOnPause are nevoie de min API 11
        mGLSurfaceView.setPreserveEGLContextOnPause(true);
        // Se seteaza clasa de randare in care practic se va pune tot ceea ce tine de randarea OpenGL
        mGLSurfaceView.setRenderer(myMenuRenderer=new GLES20Renderer("menu"));

        Button nextButton = (Button) findViewById(R.id.next);
        Button prevButton = (Button) findViewById(R.id.prev);

        nextButton.setOnClickListener(nextStepListener);
        prevButton.setOnClickListener(prevStepListener);
    }

    View.OnClickListener nextStepListener = new View.OnClickListener() {
        public void onClick(View v) {
            myinstance.startGame();
        }
    };

    View.OnClickListener prevStepListener = new View.OnClickListener() {
        public void onClick(View v) {
            myinstance.newGame();
        }
    };

    View.OnClickListener startGameListener = new View.OnClickListener() {
        public void onClick(View v) {
            myinstance.gameMenu();
        }
    };

    View.OnClickListener exitGameListener = new View.OnClickListener() {
        public void onClick(View v) {
            myinstance.finish();
        }
    };

    View.OnClickListener resumeListener = new View.OnClickListener() {
        public void onClick(View v) {
            resumeButton.setVisibility(View.INVISIBLE);
            endButton.setVisibility(View.INVISIBLE);
            pauseContainer.setVisibility(View.INVISIBLE);
        }
    };

    View.OnClickListener PauseListener = new View.OnClickListener() {
        public void onClick(View v) {
            resumeButton.setVisibility(View.VISIBLE);
            endButton.setVisibility(View.VISIBLE);
            pauseContainer.setVisibility(View.VISIBLE);
        }
    };

    View.OnClickListener HornListener = new View.OnClickListener() {
        public void onClick(View v) {
        }
    };

    View.OnClickListener SpeedListener = new View.OnClickListener() {
        public void onClick(View v) {
        }
    };

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    int ipos=0;
    int ii=1;
    float pitch=0;

    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        float[] data;
        if (type == Sensor.TYPE_ACCELEROMETER) {
            data = mGData;
        } else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
            data = mMData;
        } else {
            // we should not be here.
            return;
        }
        for (int i=0 ; i<3 ; i++)
            data[i] = event.values[i];

        SensorManager.getRotationMatrix(mR, mI, mGData, mMData);
        SensorManager.getOrientation(mR, mOrientation);
        float incl = SensorManager.getInclination(mI);


        vec3 mycarpos=new vec3(ipos,0,fpos);
        pitch=-mOrientation[1]*10;
        ipos+=pitch;

       /* if (mCount++ > 50) {
            final float rad2deg = (float)(180.0f/Math.PI);
            mCount = 0;
            //ipos+=ii;
            pitch=-mOrientation[1]*10;
            ipos+=pitch;
            //if(ipos>50) ii*=-1;

            Log.d("Compass", "yaw: " + (int)(mOrientation[0]*rad2deg) +
                    "  pitch: " + (int)(mOrientation[1]*rad2deg) +
                    "  roll: " + (int)(mOrientation[2]*rad2deg) +
                    "  incl: " + (int)(incl*rad2deg)
            );

            Log.d("Compass","  mypitch: " + pitch);


        }  */

        if(mGLSurfaceView!=null && myGameRenderer!=null) {
            myGameRenderer.getFPS();
            for (CharacterControllerComponent cc : gameControllers) {
                //cc.positionAction(mycarpos);
                cc.sideMovement(pitch);
            }
            //if(myGameRenderer.mycar!=null) myGameRenderer.mycar.setPostion(mycarpos);
        }
    }
}
