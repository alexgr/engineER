package com.example.engineER;

public class ContinousRoadsScript extends ScriptComponent{

    private GameObject go;
    private GameObject ref;

    public ContinousRoadsScript(GameObject go, GameObject ref) {
        this.go=go;
        this.ref=ref;
    }

    @Override
    public void notify(int programHandle) {

        vec3 refpos=ref.getPosition();
        vec3 gopos=go.getPosition();
        int val = (int)(gopos.z-refpos.z);
        if(val > 120) {
            gopos.z-=320;
            go.setPostion(gopos);
        }

    }

}
