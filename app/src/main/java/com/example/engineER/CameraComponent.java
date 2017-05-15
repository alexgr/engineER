package com.example.engineER;


import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.util.Vector;


public class CameraComponent extends Component {

     private vec3 position, forward, up, right;
    private float[] mViewMatrix = new float[16];
    private GameObject go;
    private boolean THIRD_PERSON_CAMERA=false;
    private float THIRD_PERSON_DISTANCE=40, THIRD_PERSON_UP=20;

     public CameraComponent() {
         position = new vec3(0,0,20);
         forward = new vec3(0,0,-1);
         up = new vec3(0,1,0);
         right = new vec3(1,0,0);
     }

    public CameraComponent(vec3 position, vec3 center, vec3 up) {
        this.position = position;
        forward = MyMath.normalize(center.minus(position));
        right = MyMath.cross(forward, up);
        this.up=MyMath.cross(right,forward);
    }

    public void set3rdPerson(GameObject go, float distance, float up) {
        THIRD_PERSON_CAMERA=true;
        THIRD_PERSON_DISTANCE=distance;
        THIRD_PERSON_UP=up;
        this.go=go;
    }

    public void set(vec3 position, vec3 center, vec3 up) {
        this.position = position;
        forward = MyMath.normalize(center.minus(position));
        right = MyMath.cross(forward, up);
        this.up=MyMath.cross(right,forward);
    }

    void translateForward(float distance){
        position = position.plus(MyMath.normalize(forward).multiplyScalar(distance));
    }

    void translateSide(float distance){
        position = position.plus(MyMath.normalize(right).multiplyScalar(distance));
    }

    void translateUp(float distance){
        position = position.plus(MyMath.normalize(up).multiplyScalar(distance));
    }

    public float[] getViewMatrix(){
        forward = MyMath.normalize(forward);
        Matrix.setLookAtM(mViewMatrix, 0, position.x, position.y, position.z, position.x + forward.x, position.y + forward.y, position.z + forward.z, up.x, up.y, up.z);
        return mViewMatrix;
    }

    public float[] getEyePosition() {
        float[] eyePos = {position.x, position.y, position.z};
        return eyePos;
    }

    public float[] getEyePosition(vec3 eye) {
        float[] eyePos = {eye.x, eye.y, eye.z};
        return eyePos;
    }

    @Override
    public void notify(int programHandle) {

        if(THIRD_PERSON_CAMERA) {
            vec3 carangle = go.getAngle();
            vec3 carbangle = go.getBaseAngle();
            float angle=carangle.y-carbangle.y;
            vec3 carpos=go.getPosition();
            vec3 eye=(new vec3((float)Math.sin(angle*3.1415/180)*THIRD_PERSON_DISTANCE, THIRD_PERSON_UP, (float)Math.cos(angle*3.1415/180)*THIRD_PERSON_DISTANCE).plus(carpos));
            Matrix.setLookAtM(mViewMatrix, 0, eye.x, eye.y, eye.z, carpos.x, carpos.y, carpos.z, up.x, up.y, up.z);

            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(programHandle, "view_matrix"), 1, false, mViewMatrix, 0);
            GLES20.glUniform3fv(GLES20.glGetUniformLocation(programHandle, "eye_position"), 1, this.getEyePosition(eye), 0);

        } else {

            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(programHandle, "view_matrix"), 1, false, this.getViewMatrix(), 0);
            GLES20.glUniform3fv(GLES20.glGetUniformLocation(programHandle, "eye_position"), 1, this.getEyePosition(), 0);
        }


    }
}
