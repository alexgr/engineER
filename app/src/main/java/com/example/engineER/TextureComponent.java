package com.example.engineER;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class TextureComponent extends Component {
    int file; // The id of the stored mesh file (raw resource)

    // Constants
    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int SHORT_SIZE_BYTES = 2;

    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 8 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_NOR_OFFSET = 3;
    private static final int TRIANGLE_VERTICES_DATA_TEX_OFFSET = 6;

    // Store the context
    Context activity;

    private int[] files;
    private int[] texIDs;

    // material properties
    private float[] matAmbient;
    private float[] matDiffuse;
    private float[] matSpecular;
    private float matShininess;

    public TextureComponent(int[] files) {
        this.files = files;
        this.activity = EngineActivity.getInstance();

        texIDs = new int[files.length];
        this.setupTextures();

        // material properties
        float[] mA = {0.1f, 0.1f, 0.1f, 1.0f};
        matAmbient = mA;

        float[] mD = {0.1f, 0.1f, 0.1f, 1.0f};
        matDiffuse = mD;

        float[] mS =  {0.1f, 0.1f, 0.1f, 1.0f};
        matSpecular = mS;

        matShininess = 0.1f;
    }

    @Override
    public void notify(int programHandle) {

        // material
        GLES20.glUniform4fv(GLES20.glGetUniformLocation(programHandle, "matAmbient"), 1, matAmbient, 0);
        GLES20.glUniform4fv(GLES20.glGetUniformLocation(programHandle, "matDiffuse"), 1, matDiffuse, 0);
        GLES20.glUniform4fv(GLES20.glGetUniformLocation(programHandle, "matSpecular"), 1, matSpecular, 0);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(programHandle, "matShininess"), matShininess);

        // Texture info

        for(int i = 0; i < files.length; i++) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
            //Log.d("TEXTURE BIND: ", i + " " + texIDs[i]);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIDs[i]);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(programHandle, "texture" + (i + 1)), i);
        }
    }

    /**
     * Sets up texturing for the object
     */
    private void setupTextures() {
        // create new texture ids if object has them
        int[] textures = new int[texIDs.length];

        //Log.d("TEXFILES LENGTH: ", files.length + "");
        GLES20.glGenTextures(texIDs.length, textures, 0);

        for(int i = 0; i < texIDs.length; i++) {
            texIDs[i] = textures[i];

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIDs[i]);

            // parameters
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_REPEAT);

            InputStream is = activity.getResources()
                    .openRawResource(files[i]);
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(is);
            } finally {
                try {
                    is.close();
                } catch(IOException e) {
                    // Ignore.
                }
            }

            // create it
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();

           // Log.d("ATTACHING TEXTURES: ", "Attached " + i);
        }
    }
}
