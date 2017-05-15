package com.example.engineER;


import android.opengl.GLES20;

public class LightComponent extends Component {

    // light parameters
    private float[] lightPos;
    private float[] lightColor;
    private float[] lightAmbient;
    private float[] lightDiffuse;

    // angle rotation for light
    float angle = 0.0f;
    boolean lightRotate = false;

    public LightComponent() {

        // light variables
        float[] lightP = {50.0f, 50.0f, -10.0f};
        this.lightPos = lightP;

        float[] lightC = {0.5f, 0.5f, 0.5f};
        this.lightColor = lightC;

    }

    @Override
    public void notify(int programHandle) {

        // rotate the light?
        if (lightRotate) {
            angle += 0.000005f;
            if (angle >= 6.2)
                angle = 0.0f;

            // rotate light about y-axis
            float newPosX = (float)(Math.cos(angle) * lightPos[0] - Math.sin(angle) * lightPos[2]);
            float newPosZ = (float)(Math.sin(angle) * lightPos[0] + Math.cos(angle) * lightPos[2]);
            lightPos[0] = newPosX; lightPos[2] = newPosZ;
        }

        // lighting variables
        // send to shaders
        GLES20.glUniform3fv(GLES20.glGetUniformLocation(programHandle, "light_position"), 1, lightPos, 0);
        //GLES20.glUniform4fv(GLES20.glGetUniformLocation(programHandle, "lightColor"), 1, lightColor, 0);

    }

    public void setLightPos(vec3 lightP) {
        this.lightPos = new float[] {lightP.x,lightP.y,lightP.z};
    }
}
