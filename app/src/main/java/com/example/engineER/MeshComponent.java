package com.example.engineER;

import android.content.Context;
import android.opengl.GLES20;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class MeshComponent extends Component {
    int file; // The id of the stored mesh file (raw resource)

    // Constants
    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int SHORT_SIZE_BYTES = 2;

    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 8 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_NOR_OFFSET = 3;
    private static final int TRIANGLE_VERTICES_DATA_TEX_OFFSET = 6;

    // the number of elements for each vertex
    // [coordx, coordy, coordz, normalx, normaly, normalz....]
    private final int VERTEX_ARRAY_SIZE = 8;

    // if tex coords exist
    private final int VERTEX_TC_ARRAY_SIZE = 8;

    // Vertices
    private float _vertices[];

    // Normals
    private float _normals[];

    // Texture coordinates
    private float _texCoords[];

    // Indices
    private short _indices[];

    // Buffers - index, vertex, normals and texcoords
    private FloatBuffer _vb;
    private FloatBuffer _nb;
    private ShortBuffer _ib;
    private FloatBuffer _tcb;

    // Normals
    private float[] _faceNormals;
    private int[]   _surroundingFaces; // # of surrounding faces for each vertex

    // Store the context
    Context activity;

    public MeshComponent(int file) {
        this.file = file;
        this.activity = EngineActivity.getInstance();

        loadFile();
    }

    /**
     * Tries to load a file - either a .OBJ or a .OFF
     * @return 1 if file was loaded properly, 0 if not
     */
    private int loadFile() {
        //Log.d("Start-loadFile", "Starting loadFile");
        try {
            // Read the file from the resource
            //Log.d("loadFile", "Trying to buffer read");
            InputStream inputStream = activity.getResources().openRawResource(file);

            // setup Bufferedreader
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

            // Try to parse the file
            //Log.d("loadFile", "Trying to buffer read2");
            String str = in.readLine();

            // Load obj mesh
            loadOBJ(in);

            // Generate your vertex, normal and index buffers
            // vertex buffer
            _vb = ByteBuffer.allocateDirect(_vertices.length
                    * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
            _vb.put(_vertices);
            _vb.position(0);

            // index buffer
            _ib = ByteBuffer.allocateDirect(_indices.length
                    * SHORT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asShortBuffer();
            _ib.put(_indices);
            _ib.position(0);

            //Log.d("loadFile - size", _indices.length/3 + "," + _vertices.length);
            // close the reader
            in.close();
            return 1;
        } catch (Exception e) {
            //Log.d("Error-LoadFile", "FOUND ERROR: " + e.toString());
            return 0;
        }
    }


    private int loadOBJ(BufferedReader in) throws Exception {
        try {


            int numVertices = 0;
            int numTexCoords = 0;
            ArrayList<Float> vs = new ArrayList<Float>(1000); // vertices
            ArrayList<Float> tc = new ArrayList<Float>(1000); // texture coords
            ArrayList<Float> ns = new ArrayList<Float>(1000); // normals

            String fFace, sFace, tFace;
            ArrayList<Float> mainBuffer = new ArrayList<Float>(1000 * 6);
            ArrayList<Short> indicesB = new ArrayList<Short>(1000 * 3);
            StringTokenizer lt, ft; // the face tokenizer
            int numFaces = 0;
            short index = 0;

            String str;

            while((str = in.readLine())!=null) {

                StringTokenizer t = new StringTokenizer(str);
                String type="";
                if(t.hasMoreTokens())  type = t.nextToken();

                //Log.d("MYTEST", str+ "::" + vs.size());
                if(type.equals("v")) {
                    vs.add(Float.parseFloat(t.nextToken())); 	// x
                    vs.add(Float.parseFloat(t.nextToken()));	// y
                    vs.add(Float.parseFloat(t.nextToken()));	// z
                    numVertices++;
                }

                if(type.equals("vt")) {
                    tc.add(Float.parseFloat(t.nextToken())); 	// u
                    tc.add(Float.parseFloat(t.nextToken()));	// v
                    numTexCoords++;
                }

                if(type.equals("vn")) {
                    ns.add(Float.parseFloat(t.nextToken())); 	// x
                    ns.add(Float.parseFloat(t.nextToken()));	// y
                    ns.add(Float.parseFloat(t.nextToken()));	// y
                }


                if (type.equals("f")) {
                        // Each line: f v1/vt1/vn1 v2/vt2/vn2
                        // Figure out all the vertices
                        for (int j = 0; j < 3; j++) {
                            fFace = t.nextToken();
                            // another tokenizer - based on /
                            ft = new StringTokenizer(fFace, "/");
                            int vert = Integer.parseInt(ft.nextToken()) - 1;
                            int texc = Integer.parseInt(ft.nextToken()) - 1;
                            int vertN = Integer.parseInt(ft.nextToken()) - 1;

                            // Add to the index buffer
                            indicesB.add(index++);

                            // Add all the vertex info
                            mainBuffer.add(vs.get(vert * 3)); 	 // x
                            mainBuffer.add(vs.get(vert * 3 + 1));// y
                            mainBuffer.add(vs.get(vert * 3 + 2));// z

                            // add the normal info
                            mainBuffer.add(-ns.get(vertN * 3));     // x
                            mainBuffer.add(-ns.get(vertN * 3 + 1)); // y
                            mainBuffer.add(-ns.get(vertN * 3 + 2)); // z

                            // add the tex coord info
                            mainBuffer.add(tc.get(texc * 2)); 	  // u
                            mainBuffer.add(tc.get(texc * 2 + 1)); // v

                        }

                    numFaces++;
                }

               // Log.d("MYTEST", str+ "::" + mainBuffer.size());
            }

            _texCoords = new float[numTexCoords * 2];

            for(int i = 0; i < numTexCoords; i++) {
                _texCoords[i * 2] 	  = tc.get(i * 2);
                _texCoords[i * 2 + 1] = tc.get(i * 2 + 1);
            }

            mainBuffer.trimToSize();
            //Log.d("COMPLETED MAINBUFFER:", "" + mainBuffer.size());

            _vertices = new float[mainBuffer.size()];

            // copy over the mainbuffer to the vertex + normal array
            for(int i = 0; i < mainBuffer.size(); i++)
                _vertices[i] = mainBuffer.get(i);

            //Log.d("COMPLETED TRANSFER:", "VERTICES: " + _vertices.length);

            // copy over indices buffer
            indicesB.trimToSize();
            _indices = new short[indicesB.size()];
            for(int i = 0; i < indicesB.size(); i++) {
                _indices[i] = indicesB.get(i);
            }

        }  catch(Exception e) {
            throw e;
            //Log.d("MYTEST", "Exception :: "+e.toString());

        }

        return 1;
    }



    @Override
    public void notify(int programHandle) {

        // the vertex coordinates
        _vb.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(GLES20.glGetAttribLocation(programHandle, "a_Position"), 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, _vb);
        GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(programHandle, "a_Position"));

        // the normal info
        _vb.position(TRIANGLE_VERTICES_DATA_NOR_OFFSET);
        GLES20.glVertexAttribPointer(GLES20.glGetAttribLocation(programHandle, "a_Normal"), 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, _vb);
        GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(programHandle, "a_Normal"));

        // texture coordinates
        _vb.position(TRIANGLE_VERTICES_DATA_TEX_OFFSET);
        GLES20.glVertexAttribPointer(GLES20.glGetAttribLocation(programHandle, "texCoord")/*shader.maTextureHandle*/, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, _vb);
        GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(programHandle, "texCoord"));//GLES20.glEnableVertexAttribArray(shader.maTextureHandle);
    }

    public int getIndicesNo() {
        return this._indices.length;
    }

    public ShortBuffer getIndices() {
        return this._ib;
    }
}
