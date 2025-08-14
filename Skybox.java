import gmaths.*;
import java.nio.*;

import org.w3c.dom.Text;

import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.texture.awt.*;
import com.jogamp.opengl.util.texture.spi.JPEGImage;

/**
 * Class for handling Skybox, unfortunately I had issues with the viewMatrix so I locked it to Projection Matrix only.
 * @author Keshav Joshi
 * Done using LearnOpenGL example
 */
public class Skybox {

    private Shader shader;
    private Camera camera;
    private Texture skyboxTexture;

    private int vertexXYZFloats = 3;
    private int[] vertexBufferId = new int[1];
    private int[] vertexArrayId = new int[1];
    
    /**
     * Constructor, creates the skybox using method inspired from Light.java by Steve Maddock
     * @param gl GL3 for OpenGL
     * @param t Cubemap Texture for the Skybox
     * @param cam Camera class for collecting the perspective and view matrices.
     */
    public Skybox(GL3 gl, Texture t, Camera cam) {
        shader = new Shader(gl, "assets/shaders/vs_skybox.txt", "assets/shaders/fs_skybox.txt");
        this.skyboxTexture = t;
        this.camera = cam;
        fillBuffers(gl);
    }
    
    public static final float[] vertices = {
        // Cube Map Positions using CCW
        // Right (+X)
        1.0f, -1.0f, -1.0f,
        1.0f, -1.0f,  1.0f,
        1.0f,  1.0f,  1.0f,
        1.0f,  1.0f,  1.0f,
        1.0f,  1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,

        // Left (-X)
        -1.0f, -1.0f,  1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f,  1.0f, -1.0f,
        -1.0f,  1.0f, -1.0f,
        -1.0f,  1.0f,  1.0f,
        -1.0f, -1.0f,  1.0f,

        // Top (+Y)
        -1.0f,  1.0f, -1.0f,
        1.0f,  1.0f, -1.0f,
        1.0f,  1.0f,  1.0f,
        1.0f,  1.0f,  1.0f,
        -1.0f,  1.0f,  1.0f,
        -1.0f,  1.0f, -1.0f,

        // Bottom (-Y)
        -1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f,  1.0f,
        1.0f, -1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f,  1.0f,
        1.0f, -1.0f,  1.0f,

        // Front (+Z)
        -1.0f, -1.0f,  1.0f,
        -1.0f,  1.0f,  1.0f,
        1.0f,  1.0f,  1.0f,
        1.0f,  1.0f,  1.0f,
        1.0f, -1.0f,  1.0f,
        -1.0f, -1.0f,  1.0f,

        // Back (-Z)
        1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f,  1.0f, -1.0f,
        -1.0f,  1.0f, -1.0f,
        1.0f,  1.0f, -1.0f,
        1.0f, -1.0f, -1.0f
         
    };


    /**
     * Method, renders the Skybox
     * @param gl GL3 for OpenGL
     * @param elapsed Uses elapsed time to rotate the skybox making it look moving. (Fix as I couldn't get the view direction to work)
     */
    public void render(GL3 gl, double elapsed) {
        double angle = (elapsed * 10.0);
        Mat4 model = Mat4Transform.scale(0.5f, 0.5f, 0.5f); // Scale up by 10x
        
        Mat4 camViewMatrix = camera.getViewMatrix();
        camViewMatrix.set(3, 0, 0.0f);
        camViewMatrix.set(3, 1, 0.0f);
        camViewMatrix.set(3, 2, 0.0f);

        Mat4 projection = camera.getPerspectiveMatrix();

        // model = Mat4.multiply(model, Mat4Transform.rotateAroundZ((float)angle));

        projection = Mat4.multiply(projection, Mat4Transform.rotateAroundY((float)angle));
        
        shader.use(gl);
        shader.setFloatArray(gl, "model", model.toFloatArrayForGLSL());
        shader.setFloatArray(gl, "view", camViewMatrix.toFloatArrayForGLSL());
        shader.setFloatArray(gl, "projection", projection.toFloatArrayForGLSL());

        shader.setInt(gl, "skybox", 0);  // be careful to match these with GL_TEXTURE0 and GL_TEXTURE1
        gl.glActiveTexture(GL.GL_TEXTURE0);
        
        skyboxTexture.bind(gl);
        
        gl.glBindVertexArray(vertexArrayId[0]);
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertices.length);
        gl.glBindVertexArray(0);
    }

    /**
     * Buffer method for constructing the VBO, uses a modified version of Mesh.java's fillBuffers by Steve Maddock
     * @param gl
     */
    private void fillBuffers(GL3 gl) {

        gl.glGenVertexArrays(1, vertexArrayId, 0);
        gl.glGenBuffers(1, vertexBufferId, 0);
        gl.glBindVertexArray(vertexArrayId[0]);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferId[0]);

        FloatBuffer fb = Buffers.newDirectFloatBuffer(vertices);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, Float.BYTES * vertices.length, fb, GL.GL_STATIC_DRAW);
        
        int offset = 0;

        
        gl.glVertexAttribPointer(0, vertexXYZFloats, GL.GL_FLOAT, false, vertexXYZFloats * Float.BYTES, offset);
        gl.glEnableVertexAttribArray(0);
    }

    public void dispose(GL3 gl) {
        gl.glDeleteBuffers(1, vertexBufferId, 0);
        gl.glDeleteVertexArrays(1, vertexArrayId, 0);
    }

    
}

