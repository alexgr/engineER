package com.example.engineER;

import android.content.Context;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.renderscript.Matrix3f;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Vector;

public class GLES20Renderer extends GLRenderer 
{

	// Matricea de modelare folosita pentru transformarea de modelare (object space -> world space)
	private float[] mModelMatrix = new float[16];

	// Matricea de vizualizare folosita pentru transformarea de vizualizare (world space -> eye space)
	private float[] mViewMatrix = new float[16];

	// Matricea de proiectie folosita pentru transformarea de proiectie (eye space -> 2D clip space) 
	private float[] mProjectionMatrix = new float[16];
	
	// Spatiu pentru matricea MVP finala care va fi produsul celor 3 matrici de mai sus
	private float[] mMVPMatrix = new float[16];
    private float[] normalMatrix = new float[16];
	
	// Datele meshului ce va fi randat stocate toate intr-un buffer float mare
	private FloatBuffer mCubeVertices;
	
	// Handle folosit pentru transmiterea la shader a matricei MVP
	private int mMVPMatrixHandle;
	
	// Handle folosit pentru transmiterea la shader a pozitiilor varfurilor
	private int mPositionHandle;
	
	// Handle folosit pentru transmiterea la shader a culorii
	private int mColorHandle;

	// Cati octeti are un float (folosit la calculul total al unui element din buffer-ul de date ce va ajunge la shader)
	private final int mBytesPerFloat = 4;
	
	// Cati octeti are un short (folosit la calculul total al unui element din buffer-ul de date ce va ajunge la shader)
	private final int mBytesPerShort = 2;
	
	// Cate elemente sunt asociate unui varf (folosit la calculul total al unui element din buffer-ul de date ce va ajunge la shader)
	private final int mStrideBytes = 7 * mBytesPerFloat;	
	
	// Offset al pozitiei datelor varfului in bufferul de date mare
	private final int mPositionOffset = 0;
	
	// Cate elemente au in total datele despre pozitia varfului (x,y,z) -> 3
	private final int mPositionDataSize = 3;
	
	// Offset al culorii varfului in bufferul de date mare
	private final int mColorOffset = 3;
	
	// Cate elemente au in total datele despre culoarea varfului (r,g,b,a) -> 4
	private final int mColorDataSize = 4;		
	
	// Indecsi verteci ce vor fi folositi pentru a desena fetele obiectului
	private ShortBuffer mCubeIndices;

    private Context activity;

    ArrayList<GameObject> gameObjects;

    private static String TAG = "Renderer";

    int programHandle;

    public float mAngleX;
    public float mScale;

    // eye pos
    private float[] eyePos = {-5.0f, 0.0f, 0.0f};

    float[] matrixR = new float[16];
    float[] matrixI = new float[9];
    float[] matrixAccelerometer = new float[9];
    float[] matrixMagnetic = new float[9];
    float[] lookingDir = new float[4];
    float[] topDir = new float[4];

    public GameObject mycar;
    private String TYPE="";
		
	public GLES20Renderer(String type)
	{

        this.TYPE=type;
        activity = EngineActivity.getInstance();
		// Definirea punctelor


		
		// METODA 2 - Se foloseste in desenare cu drawElements()
		// CUB - se specifica cele 8 varfuri ale cubului
		//     - se specifica cele 12 fete ale cubului prin indecsi
		final float[] cubeVerticesData_Indexed = 
			{
				// X, Y, Z, R, G, B, A
				
				// V0
				-10.0f,-10.0f,10.0f,		0.0f,0.0f,1.0f,1.0f,
				// V1
				10.0f,-10.0f,10.0f,     	1.0f,0.0f,1.0f,1.0f,
				// V2
				10.0f,-10.0f,-10.0f,		1.0f,0.0f,0.0f,1.0f,
				// V3
				-10.0f,-10.0f,-10.0f,		0.0f,0.0f,0.0f,1.0f,
				// V4
				-10.0f,10.0f,10.0f,			0.0f,1.0f,1.0f,1.0f,
				// V5
				10.0f,10.0f,10.0f,			1.0f,1.0f,1.0f,1.0f,
				// V6
				10.0f,10.0f,-10.0f,			1.0f,1.0f,0.0f,1.0f,
				// V7
				-10.0f,10.0f,-10.0f,		0.0f,1.0f,0.0f,1.0f
				
			};
		// Indecii fetelor din care este alcatuit cubul
		short indices[] = { 
				0, 1, 2, 2, 3, 0, 

		        4, 5, 6, 6, 7, 4,
		        
		        0, 1, 5, 5, 4, 0,
		        
		        3, 2, 6, 6, 7, 3,
		        
		        1, 2, 6, 6, 5, 1,
		        
		        3, 0, 4, 4, 7, 3
		        };
		
		// Initializarea bufferelor - METODA 2
		mCubeVertices = ByteBuffer.allocateDirect(cubeVerticesData_Indexed.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mCubeVertices.put(cubeVerticesData_Indexed).position(0);
		
		mCubeIndices = ByteBuffer.allocateDirect(indices.length * mBytesPerShort)
				.order(ByteOrder.nativeOrder()).asShortBuffer();
		mCubeIndices.put(indices).position(0);
		
	
	}
	
    @Override
    // Metoda apelata la crearea / modificarea suprafetei
    public void onCreate(int width, int height,
            boolean contextLost) 
    {

    	// Setarea viewport-ului la aceeasi dimesiune cu cea suprafetei
    	GLES20.glViewport(0, 0, width, height);

    	// Crearea matricei de proiectie de tip perspectiva -> se foloseste 
    	// Matrix.frustumM() insa se putea folosi si Matrix.perspectiveM()
    	final float ratio = (float) width / height;
    	final float left = -ratio;
    	final float right = ratio;
    	final float bottom = -1.0f;
    	final float top = 1.0f;
    	final float near = 1.0f;
    	final float far = 100.0f;

    	Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    	    	
    	// Se sterge ecranul si se seteaza un background gri
    	GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f);



        GLES20.glEnable   (GLES20.GL_DEPTH_TEST);
        GLES20.glClearDepthf(1.0f);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glDepthMask(true);

        // cull backface
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

    	// Observatorul se va afla la aceasta pozitie
    	final float eyeX = 0.0f;
    	final float eyeY = 20.0f;
    	final float eyeZ = 30f;

    	// Unde priveste
    	final float lookX = 0.0f;
    	final float lookY = 0.0f;
    	final float lookZ = 0f;

    	// Vectorul UP
    	final float upX = 0.0f;
    	final float upY = 1.0f;
    	final float upZ = 0.0f;

    	// Setarea matricea de vizualizare (pozitia camerei/observatorului)
    	Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        //Load Shaders
        Shader shader= new Shader(R.raw.myshader_vert, R.raw.myshader_frag);
        programHandle = shader.getProgram();
        // Activare folosire program shader
        GLES20.glUseProgram(programHandle);

        mAngleX=90;
        mScale=6.0f;

        //Load Assets - todo - preloader   -splash screen - loading
        gameObjects = new ArrayList<GameObject>();

        this.loadSceneObjects();

    }

    private void loadSceneObjects() {

        if(TYPE.equals("menu")) {
            GameObject cube=new GameObject();

            cube.addComponent(new MeshComponent(R.raw.texturedcube));

            int[] cubeTextures = {R.raw.diffuse, R.raw.diffusenormalmap};

            cube.addComponent(new TextureComponent(cubeTextures));
            cube.setPostion(new vec3(0, 0, 0));
            cube.setScale(new vec3(3,3,3));
            //cube.setRotation(90, new vec3(0.0f, 1.0f, 0.0f));
            cube.addComponent(new RotationAnimationScript(cube, 1, new vec3(0.0f, 1.0f, 0.0f)));
            gameObjects.add(cube);
        }

        if(TYPE.equals("game")) {

            /* skybox */

            GameObject sky=new GameObject();

            sky.addComponent(new MeshComponent(R.raw.sphere));

            int[] skyTextures = {R.raw.sky_midafternoon};

            sky.addComponent(new TextureComponent(skyTextures));
            sky.setPostion(new vec3(0,10,0));
            sky.setScale(new vec3(10, 1, 1));
            sky.setRotation(90, new vec3(0.0f, 0.0f, 1.0f));
            sky.disableDepth();
            gameObjects.add(sky);

                /* mycar */

            mycar=new GameObject();
            //mycar.addComponent(new MeshComponent(R.raw.texturedcube));
            mycar.addComponent(new MeshComponent(R.raw.porsche));
            int[] mycarTextures = {R.raw.porsche1};
            //int[] mycarTextures = {R.raw.porsche1, R.raw.porsche2};
            int[] normalMapTextures1 = {R.raw.diffuse_old, R.raw.diffusenormalmap_deepbig};

            mycar.addComponent(new TextureComponent(mycarTextures));
            //mycar.addComponent(new TextureComponent(normalMapTextures1));
            //mycar.addComponent(new LightComponent());

            mycar.setPostion(new vec3(0, 5f, 10f));
            mycar.setScale(new vec3(mScale, mScale, mScale));
            //mycar.setRotation(90, new vec3(0.0f, 1.0f, 0.0f));
            CameraComponent mycam=new CameraComponent(new vec3(0, 0, 50), new vec3(0, 0, 0), new vec3(0, 1, 0));
            mycam.translateUp(20);
            mycam.set3rdPerson(mycar,20,20);
            mycar.addComponent(mycam);
            CharacterControllerComponent carc=new CharacterControllerComponent(mycar);
            carc.register();
            mycar.addComponent(carc);
            mycar.addComponent(new ScoreManagerScript(mycar));
            gameObjects.add(mycar);


            GameObject road=new GameObject();

            road.addComponent(new MeshComponent(R.raw.texturedcube));

            int[] roadTextures = {R.raw.roadtexture4};

            road.addComponent(new TextureComponent(roadTextures));
            road.setPostion(new vec3(0, 0, 0));
            road.setScale(new vec3(20, 0.1f, 40));
            road.setRotation(90, new vec3(0.0f, 0.0f, 1.0f));
            road.addComponent(new ContinousRoadsScript(road,mycar));
            gameObjects.add(road);

            GameObject road1=new GameObject();

            road1.addComponent(new MeshComponent(R.raw.texturedcube));
            road1.addComponent(new TextureComponent(roadTextures));
            road1.setPostion(new vec3(0,0,-160));
            road1.setScale(new vec3(20, 0.1f, 40));
            road1.setRotation(90, new vec3(0.0f, 0.0f, 1.0f));
            road1.addComponent(new ContinousRoadsScript(road1,mycar));
            gameObjects.add(road1);

            GameObject grass=new GameObject();

            grass.addComponent(new MeshComponent(R.raw.texturedcube));

            int[] grassTextures = {R.raw.grass};

            grass.addComponent(new TextureComponent(grassTextures));
            grass.setPostion(new vec3(0,-0.1f,0));
            grass.setScale(new vec3(120, 0.1f, 40));
            grass.setRotation(90, new vec3(0.0f, 0.0f, 1.0f));
            grass.addComponent(new ContinousRoadsScript(grass,mycar));
            gameObjects.add(grass);

            GameObject grass1=new GameObject();
            grass1.addComponent(new MeshComponent(R.raw.texturedcube));
            grass1.addComponent(new TextureComponent(grassTextures));
            grass1.setPostion(new vec3(0,-0.1f,-160));
            grass1.setScale(new vec3(120, 0.1f, 40));
            grass1.setRotation(90, new vec3(0.0f, 0.0f, 1.0f));
            grass1.addComponent(new ContinousRoadsScript(grass1,mycar));
            gameObjects.add(grass1);

            GameObject bamboo;
            int[] bambootextures = {R.raw.bamboo};
            Component bamboo_mesh=new MeshComponent(R.raw.bamboo_onj);
            Component bamboo_tex=new TextureComponent(bambootextures);
            float bamboo_scale=0.5f;
            float bamboo_repeat=32;
            float bamboo_posx=50;

            for(int b=0;b<10;b++) {

                bamboo=new GameObject();
                bamboo.addComponent(bamboo_mesh);
                bamboo.addComponent(bamboo_tex);
                bamboo.addComponent(new ContinousRoadsScript(bamboo,mycar));
                bamboo.setScale(new vec3(bamboo_scale, bamboo_scale, bamboo_scale));
                bamboo.setPostion(new vec3(-bamboo_posx,-0.1f,b*bamboo_repeat));
                gameObjects.add(bamboo);

                bamboo=new GameObject();
                bamboo.addComponent(bamboo_mesh);
                bamboo.addComponent(bamboo_tex);
                bamboo.addComponent(new ContinousRoadsScript(bamboo,mycar));
                bamboo.setScale(new vec3(bamboo_scale, bamboo_scale, bamboo_scale));
                bamboo.setPostion(new vec3(bamboo_posx,-0.1f,b*bamboo_repeat));
                gameObjects.add(bamboo);
            }

        }
    }

    @Override
    // Metoda de desenare
    public void onDrawFrame(boolean firstDraw) 
    {
    	
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // send to the shader
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(programHandle, "model_matrix"), 1, false, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(programHandle, "view_matrix"), 1, false, mViewMatrix, 0);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(programHandle, "projection_matrix"), 1, false, mProjectionMatrix, 0);

        // eye position
        GLES20.glUniform3fv(GLES20.glGetUniformLocation(programHandle, "eye_position"), 1, eyePos, 0);

       drawGameObjects();
                
    }

    private void drawGameObjects() {

        for (GameObject gameObject : this.gameObjects) {


            int indicesNo = 0;
            ShortBuffer indicesBuffer=null;
            for(Component c : gameObject.getComponents()) {
                  c.notify(programHandle);
                  if(c instanceof MeshComponent) {
                      indicesNo = ((MeshComponent) c).getIndicesNo();
                      indicesBuffer = ((MeshComponent) c).getIndices();
                  }
            }



            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(programHandle, "model_matrix"), 1, false, gameObject.getModelMatrix(), 0);
            if(gameObject.noDepthTest) {
                GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            }
            // Draw with indices
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, indicesNo, GLES20.GL_UNSIGNED_SHORT, indicesBuffer);
            checkGlError("glDrawElements");
            if(gameObject.noDepthTest) {
                GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            }
        }
    }

    // debugging opengl
    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}