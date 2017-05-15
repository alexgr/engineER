package com.example.engineER;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import java.text.DecimalFormat;

public class ScoreManagerScript extends ScriptComponent{

    private GameObject go;
    private float distance;
    protected Activity activity;

    public ScoreManagerScript(GameObject go) {
        this.go=go;
        this.distance=0;
        this.activity=EngineActivity.getInstance();
    }

    @Override
    public void notify(int programHandle) {

        vec3 carpos=go.getPosition();

        distance=(10-carpos.z)/1000;

        activity.runOnUiThread(new Runnable() {
            public void run() {
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(2);
                TextView tv = (TextView)activity.findViewById(R.id.scoreText);
                if(tv!=null) tv.setText("Distance: "+df.format(distance)+"km");
            }
        });



    }

}
