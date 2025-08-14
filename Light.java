import gmaths.*;
import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;

/**
 * @author Steve Maddock, modified by Keshav Joshi
 * 
 * This class handles both pointlight and spotlight objects.
 * Uses Attenuation, CutOff and OuterCutOff as well as a Intensity float for handling lights.
 */
public class Light {
  
  private Material material;
  private Vec3 position = new Vec3(0,0,0);
  private Mat4 model = Mat4.multiply(Mat4Transform.scale(.1f,.1f,.1f), Mat4Transform.translate(-6,1.1f + 0.375f,-9));
  
  private Shader shader;
  private Camera camera;

  private float intensity = 1;
  
  // Spotlight
  private float cutOff = 0.9976f;
  private float outerCutOff = .953f;
  private Vec3 direction = new Vec3(0.0f, 1.1f, 0.0f);
  private Vec3 upVector = new Vec3(0,1,0);
  private boolean lightSpotlight = false;

  //Attentuation
  private float constant = 1;
  private float linear = 0.14f;
  private float quadratic = 0.07f;

  
  /**
   * Constructor, constructs the Light object
   * @param gl GL3 for OpenGL
   */
  public Light(GL3 gl) {
    material = new Material();
    material.setAmbient(0.3f, 0.3f, 0.3f);
    material.setDiffuse(0.7f, 0.7f, 0.7f);
    material.setSpecular(0.8f, 0.8f, 0.8f);
    position = new Vec3(3f,2f,1f);
    
    shader = new Shader(gl, "assets/shaders/vs_light_01.txt", "assets/shaders/fs_light_01.txt");

    fillBuffers(gl);
  }
  
  /**
   * Method for changing the position of the Light using 3D Vector
   * @param v New Position Vector
   */
  public void setPosition(Vec3 v) {
    position.x = v.x;
    position.y = v.y;
    position.z = v.z;
  }
  
  /**
   * Method for changing the position of the Light using 3 floats
   * @param v New Position Vector
   */
  public void setPosition(float x, float y, float z) {
    position.x = x;
    position.y = y;
    position.z = z;
  }

  /**
   * Method for getting spotlight enabled as a float for shading.
   * @return Float value (1 or 0)
   */
  public float getIsSpotlight() {
    return this.lightSpotlight ? 1 : 0;
  }

  /**
   * Method for enabling or disabling a light to be a spotlight (default is pointlight)
   * @param bool spotlight boolean
   */
  public void setIsSpotlight(boolean bool) {
    this.lightSpotlight = bool;
  }

  /**
   * Method for setting the intensity of the light
   * @param newIntensity
   */
  public void setIntensity(float newIntensity) {
    this.intensity = newIntensity;
  }

  /**
   * Method for getting the light's intensity
   * @return Float value
   */
  public float getIntensity() {
    return this.intensity;
  }
  
  public Vec3 getPosition() {
    return this.position;
  }
  
  public void setMaterial(Material m) {
    material = m;
  }
  
  public Material getMaterial() {
    return material;
  }
  
  public void setCamera(Camera camera) {
    this.camera = camera;
  }

  public void setCutOff(float cutOff) {
    this.cutOff = cutOff;
  }

  public void setouterCutOff(float outerCutOff) {
      this.outerCutOff = outerCutOff;
  }

  public void setDirection(Vec3 direction) {
      this.direction = direction;
  }

  public float getCutOff() {
    return this.cutOff;
  }

  public float getOuterCutOff() {
    return this.outerCutOff;
  }

  public Vec3 getDirection() {
    return this.direction;
  }

  public void setAttenuation(Vec3 attenuation) {
      this.constant = attenuation.x;
      this.linear = attenuation.y;
      this.quadratic = attenuation.z;
  }

  public float getConstant() {
      return this.constant;
  }

  public float getLinear() {
      return this.linear;
  }

  public float getQuadratic() {
      return this.quadratic;
  }

  public Mat4 getModel() {
    return this.model;
  }
  public void setModel(Mat4 m) {
    this.model = m;
  }
  
  public void render(GL3 gl) { 
    model = new Mat4(1);
    model = Mat4.multiply(Mat4Transform.scale(0.1f,0.1f,0.1f), model);
    model = Mat4.multiply(Mat4Transform.translate(position), model);
   
    Mat4 mvpMatrix = Mat4.multiply(camera.getPerspectiveMatrix(), Mat4.multiply(camera.getViewMatrix(), model));

    shader.use(gl);
    shader.setFloatArray(gl, "mvpMatrix", mvpMatrix.toFloatArrayForGLSL());

    gl.glBindVertexArray(vertexArrayId[0]);
    gl.glDrawElements(GL.GL_TRIANGLES, indices.length, GL.GL_UNSIGNED_INT, 0);
    gl.glBindVertexArray(0);
  }

  public void dispose(GL3 gl) {
    gl.glDeleteBuffers(1, vertexBufferId, 0);
    gl.glDeleteVertexArrays(1, vertexArrayId, 0);
    gl.glDeleteBuffers(1, elementBufferId, 0);
  }

    // ***************************************************
  /* THE DATA
   */
  // anticlockwise/counterclockwise ordering
  
    private float[] vertices = new float[] {  // x,y,z
      -0.5f, -0.5f, -0.5f,  // 0
      -0.5f, -0.5f,  0.5f,  // 1
      -0.5f,  0.5f, -0.5f,  // 2
      -0.5f,  0.5f,  0.5f,  // 3
       0.5f, -0.5f, -0.5f,  // 4
       0.5f, -0.5f,  0.5f,  // 5
       0.5f,  0.5f, -0.5f,  // 6
       0.5f,  0.5f,  0.5f   // 7
     };
    
    private int[] indices =  new int[] {
      0,1,3, // x -ve 
      3,2,0, // x -ve
      4,6,7, // x +ve
      7,5,4, // x +ve
      1,5,7, // z +ve
      7,3,1, // z +ve
      6,4,0, // z -ve
      0,2,6, // z -ve
      0,4,5, // y -ve
      5,1,0, // y -ve
      2,3,7, // y +ve
      7,6,2  // y +ve
    };
    
  private int vertexStride = 3;
  private int vertexXYZFloats = 3;
  
  // ***************************************************
  /* THE LIGHT BUFFERS
   */

  private int[] vertexBufferId = new int[1];
  private int[] vertexArrayId = new int[1];
  private int[] elementBufferId = new int[1];
    
  private void fillBuffers(GL3 gl) {
    gl.glGenVertexArrays(1, vertexArrayId, 0);
    gl.glBindVertexArray(vertexArrayId[0]);
    gl.glGenBuffers(1, vertexBufferId, 0);
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferId[0]);
    FloatBuffer fb = Buffers.newDirectFloatBuffer(vertices);
    
    gl.glBufferData(GL.GL_ARRAY_BUFFER, Float.BYTES * vertices.length, fb, GL.GL_STATIC_DRAW);
    
    int stride = vertexStride;
    int numXYZFloats = vertexXYZFloats;
    int offset = 0;
    gl.glVertexAttribPointer(0, numXYZFloats, GL.GL_FLOAT, false, stride*Float.BYTES, offset);
    gl.glEnableVertexAttribArray(0);
     
    gl.glGenBuffers(1, elementBufferId, 0);
    IntBuffer ib = Buffers.newDirectIntBuffer(indices);
    gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, elementBufferId[0]);
    gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, Integer.BYTES * indices.length, ib, GL.GL_STATIC_DRAW);

  } 

}