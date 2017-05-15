package com.example.engineER;


import android.util.Log;

public class CharacterControllerComponent extends Component{

    private GameObject go;
    private int acc=0;
    private int max=5;
    private int count=100;
    private int count1=100;
    private boolean pause=false;
    private boolean RIGHT, LEFT, FORWARD, freeRUN;
    float ANGLE_FACTOR, TRANS_FACTOR, ANGLE_M, TRANS_M;
    float currentAngle;
    float pitch;
    float dist;
    int DEFAULT_SPEED;
    int CAR_SPEED;
    float angle;
    vec3 offset;

    public CharacterControllerComponent(GameObject go) {
        this.go=go;
        currentAngle = 0;
        angle = 0;
        dist = 0;
        offset = new vec3(0,0,0);
        DEFAULT_SPEED = 1;
        CAR_SPEED = 3;
        RIGHT=false;
        LEFT=false;
        FORWARD=false;
        freeRUN=false;
        ANGLE_FACTOR =0.1f;
        TRANS_FACTOR =1f;
        ANGLE_M =0.2f;
        TRANS_M =2f;
        pause=true;
    }

    public void register() {
        EngineActivity.gameControllers.add(this);
    }

    public void decelerate() {
        vec3 changes=go.getPosition();
        if(acc>0) {
            if(++count1>50) { acc--; count1=0;}
            changes.z+=acc;
            this.accelerate(changes);
        }
    }

    public boolean isPause() {
        return pause;
    }

    public void pauseAction() {
        FORWARD=false;
        RIGHT=false;
        LEFT=false;
    }

    public void positionAction(vec3 changes) {
        pause=false;
        if(acc<max && ++count>100) { acc++; count=0;}
        changes.z+=acc;
        this.accelerate(changes);
    }

    public void accelerate(vec3 changes) {
        vec3 oldpos=go.getPosition();
        go.setPostion(changes);
        for(Component c : go.getComponents()) {
            if(c instanceof CameraComponent) {
                ((CameraComponent) c).translateSide((changes.x-oldpos.x));
                ((CameraComponent) c).translateForward(-(changes.z-oldpos.z));
            }

           /* if(c instanceof LightComponent) {
                ((LightComponent) c).setLightPos(new vec3(changes.x,20,changes.z));
            }  */


        }
    }

    public void sideMovement(float pitch) {
        this.pitch=0;
        if(pitch>0) {RIGHT=true; LEFT=false;this.pitch=pitch;}
        else {RIGHT=false;LEFT=true;this.pitch=pitch;}

        //Log.d("PITCH",":: "+pitch);

    }

    public void forwardMovement(float pitch) {
        FORWARD=true;
    }

    public void setPosition(vec3 changes) {
        vec3 oldpos=go.getPosition();
        go.setPostion(changes);
        for(Component c : go.getComponents()) {
            if(c instanceof CameraComponent) {
                ((CameraComponent) c).translateSide((changes.x-oldpos.x));
                ((CameraComponent) c).translateForward(-(changes.z-oldpos.z));
            }

           /* if(c instanceof LightComponent) {
                ((LightComponent) c).setLightPos(new vec3(changes.x,20,changes.z));
            }  */


        }
    }

    @Override
    public void notify(int programHandle) {

        angle=pitch*8;

        if(FORWARD && dist < CAR_SPEED) dist += TRANS_FACTOR;
        else if(!FORWARD && dist < TRANS_M && dist > -TRANS_M) dist = 0;
        else if(!FORWARD && dist >= TRANS_M) dist -= TRANS_FACTOR;
        else if(!FORWARD && dist <= -TRANS_M) dist += TRANS_FACTOR;

        vec3 temp = new vec3((float) Math.sin(angle * 3.1415 / 180)*dist,0, (float)Math.cos(angle * 3.1415 / 180)*-dist).plus(offset);

        if(!freeRUN && (temp.x < -38 || temp.x > 38 )) {
            if(temp.x > 40) angle=-1;
            if(temp.x < -40) angle=1;
                offset.plus(new vec3(0, 0, (float) Math.cos(angle * 3.1415 / 180) * -dist));
        } else {
            offset.plus(new vec3((float) Math.sin(angle * 3.1415 / 180)*dist,0, (float)Math.cos(angle * 3.1415 / 180)*-dist));

        }

        //this.setPosition(offset);
        go.setPostion(offset);
        go.addRotation(-this.pitch*4, new vec3(0, 1, 0));

        RIGHT=false;
        LEFT=false;
    }

}
