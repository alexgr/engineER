package com.example.engineER;


import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Shader {

    private String vertexShader, fragmentShader;
    private int programHandle, vertexShaderHandle, fragmentShaderHandle;

    public Shader(String vertexShader, String fragmentShader) {
          this.vertexShader = vertexShader;
          this.fragmentShader = fragmentShader;
          this.createProgram();
    }

    public Shader(int vertexShader, int fragmentShader) {
        this.vertexShader = readFile(vertexShader);
        this.fragmentShader = readFile(fragmentShader);
        this.createProgram();
    }

    private String readFile(int file) {
        StringBuffer sb = new StringBuffer();
        Context activity= EngineActivity.getInstance();
        try {
            // Read the file from the resource
            InputStream inputStream = activity.getResources().openRawResource(file);
            // setup Bufferedreader
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

            String read = in.readLine();
            while (read != null) {
                sb.append(read + "\n");
                read = in.readLine();
            }

            sb.deleteCharAt(sb.length() - 1);

        } catch (Exception e) {
            Log.d("ERROR-readingShader", "Could not read shader: " + e.getLocalizedMessage());
        }

        return sb.toString();
    }

    private int createShader(String source, int GL_ShaderType) {

        int ShaderHandle = GLES20.glCreateShader(GL_ShaderType);

        if (ShaderHandle != 0)
        {
            // Sursa va fi String-ul de mai sus
            GLES20.glShaderSource(ShaderHandle, source);

            // Compilare shader
            GLES20.glCompileShader(ShaderHandle);

            // Status compilare
            final int[] compileStatus = new int[1];
            int error;
            GLES20.glGetShaderiv(ShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // In cazul in care a existat eroare
            if (compileStatus[0] == 0)
            {
                Log.e("SHADER--------", "SHADER compile >>>>> " + GLES20.glGetShaderInfoLog(ShaderHandle));
                GLES20.glDeleteShader(ShaderHandle);
                ShaderHandle = 0;


            }
        }

        if (ShaderHandle == 0)
        {
            throw new RuntimeException("Eroare la crearea shaderului");
        }

        return ShaderHandle;

    }

    private int createProgram() {
        // Incarcare VS
        vertexShaderHandle = createShader(vertexShader, GLES20.GL_VERTEX_SHADER);
        Log.e("SHADER--------", "VERTEX SHADER compile >>>>> OK");
        // Incarcare FS
        fragmentShaderHandle = createShader(fragmentShader, GLES20.GL_FRAGMENT_SHADER);
        Log.e("SHADER--------", "FRAGMENT SHADER compile >>>>> OK");

        // Crearea programului shader
        programHandle = GLES20.glCreateProgram();

        if (programHandle != 0)
        {
            // Atasare VS la program
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // Atasare FS la program
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Specificate pozitii atribute ce vor fi folosite pentru transmisia datelor
            //GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
            //GLES20.glBindAttribLocation(programHandle, 1, "a_Color");

            // Linkare shaderelor
            GLES20.glLinkProgram(programHandle);

            // Status linkare
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // In cazul in care a existat eroare
            if (linkStatus[0] == 0)
            {
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0)
        {
            throw new RuntimeException("Eroare la crearea/linkarea programului shader");
        }

        return 1;

    }

    public int getProgram() {
        return programHandle;
    }
}
