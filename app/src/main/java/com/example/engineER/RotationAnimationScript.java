package com.example.engineER;

import android.util.Log;

public class RotationAnimationScript extends ScriptComponent{

    private GameObject go;
    private float angle, ii;
    private vec3 rotAxis;
    private int i;

    public RotationAnimationScript(GameObject go, float angle, vec3 rotAxis) {
        this.go=go;
        this.angle=angle;
        this.ii=angle;
        this.rotAxis=rotAxis;
    }

    @Override
    public void notify(int programHandle) {

        go.addRotation(angle, new vec3(rotAxis.x, rotAxis.y, rotAxis.z));
        angle+=ii;
        if(angle>=360) angle=ii;
    }

}
