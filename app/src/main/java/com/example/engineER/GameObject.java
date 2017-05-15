package com.example.engineER;


import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;

public class GameObject {

    Context activity;

    vec3 pos, scale, rotAxis, angle, baseangle;
    boolean p,s,r,noDepthTest;
    float[] mModelMatrix;

    ArrayList<Component> components;

    public GameObject() {

        this.activity = EngineActivity.getInstance();
        this.components = new ArrayList<Component>();
        this.pos=new vec3(0,0,0);
        this.scale=new vec3(1,1,1);
        this.rotAxis=new vec3(0,0,0);
        this.angle=new vec3(0,0,0);
        this.baseangle=new vec3(0,0,0);
        mModelMatrix = new float[16];
        Matrix.setIdentityM(mModelMatrix, 0);
        p=s=r=false;
        noDepthTest=false;

    }

    public GameObject(vec3 pos) {

        super();
        this.pos=pos;

    }

    public void addComponent(Component component) {
        components.add(component);
    }

    public void setPostion(vec3 pos) {
        this.pos=pos;
        p=true;
    }

    public void setScale(vec3 scale) {
        this.scale=scale;
        s=true;
    }

    public void setRotation(float angle, vec3 rotAxis) {
        rotAxis.multiplyScalar(angle);
        this.baseangle=rotAxis.plus(this.baseangle);
        if(this.angle.x>=360||this.angle.x<=-360) this.angle.x=0;
        if(this.angle.y>=360||this.angle.y<=-360) this.angle.y=0;
        if(this.angle.z>=360||this.angle.z<=-360) this.angle.z=0;
        r=true;
    }

    public void addRotation(float angle, vec3 rotAxis) {
        rotAxis.multiplyScalar(angle);
        this.angle=rotAxis.plus(this.baseangle);
        if(this.angle.x>=360||this.angle.x<=-360) this.angle.x=0;
        if(this.angle.y>=360||this.angle.y<=-360) this.angle.y=0;
        if(this.angle.z>=360||this.angle.z<=-360) this.angle.z=0;
        r=true;
    }

    public ArrayList<Component> getComponents() {


        return components;
    }

    public vec3 getPosition() { return pos; }

    public vec3 getScale() { return scale; }

    public vec3 getAngle() { return angle; }
    public vec3 getBaseAngle() { return baseangle; }

    public float[] getModelMatrix() {

            Matrix.setIdentityM(mModelMatrix, 0);
            if(p) Matrix.translateM(mModelMatrix, 0, this.pos.x, this.pos.y, this.pos.z);
            if(s) Matrix.scaleM(mModelMatrix, 0, this.scale.x, this.scale.y, this.scale.z);
            if(r) {
                Matrix.rotateM(mModelMatrix, 0, this.angle.x, 1, 0, 0);
                Matrix.rotateM(mModelMatrix, 0, this.angle.y, 0, 1, 0);
                Matrix.rotateM(mModelMatrix, 0, this.angle.z, 0, 0, 1);
            }

        return mModelMatrix;
    }

    public void disableDepth() {
        noDepthTest=true;
    }
}
