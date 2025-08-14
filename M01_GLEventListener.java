import gmaths.*;

import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;

/**
 * @author Steve Maddock (7_1_stack_of_objects), modified by Keshav Joshi
 */
public class M01_GLEventListener implements GLEventListener {
  
  private static final boolean DISPLAY_SHADERS = false;
  private Camera camera; 
  public M01_GLEventListener(Camera camera) {
    this.camera = camera;
    this.camera.setPosition(new Vec3(4f,6f,15f));
    this.camera.setTarget(new Vec3(0f,0,0f));
  }
  
  // ***************************************************
  /*
   * METHODS DEFINED BY GLEventListener
   */

  /* Initialisation */
  public void init(GLAutoDrawable drawable) {   
    GL3 gl = drawable.getGL().getGL3();
    System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
    gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); 
    gl.glClearDepth(1.0f);
    gl.glEnable(GL.GL_DEPTH_TEST);
    gl.glDepthFunc(GL.GL_LESS);
    gl.glFrontFace(GL.GL_CCW);    // default is 'CCW'
    gl.glEnable(GL.GL_CULL_FACE); // default is 'not enabled'
    gl.glCullFace(GL.GL_BACK);   // default is 'back', assuming CCW
    initialise(gl);
    startTime = getSeconds();
  }
  
  /* Called to indicate the drawing surface has been moved and/or resized  */
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    GL3 gl = drawable.getGL().getGL3();
    gl.glViewport(x, y, width, height);
    float aspect = (float)width/(float)height;
    camera.setPerspectiveMatrix(Mat4Transform.perspective(45, aspect));
  }

  /* Draw */
  public void display(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    render(gl);
  }

  /* Clean up memory, if necessary */
  public void dispose(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    robot1.dispose(gl);
    robot2.dispose(gl);
    disposeModels(gl);
  }

  // ***************************************************
  /* THE SCENE
   * Now define all the methods to handle the scene.
   * This will be added to in later examples.
   */
  
  // textures
  private TextureLibrary textures;

  
  private Walls walls;
  private Globe globe;
  private Robot2 robot2;
  private Robot1 robot1;
  private Skybox skybox;
  private ModelMultipleLights fakeskybox;
  private Light[] lights = new Light[2];
  private boolean overridePause = false;
  private boolean robot1Paused;
  private Vec3 robot1Pos;

  // private Robot1 bigRobot;
  float wallThickness = 0.2f;
  float wallHeight = 7.5f;
  float backWallLength = 15f;
  float otherWallLength = 22.5f;
  float pathGapFromWall = 1.75f;
  float pathThickness = 0.25f;
  float pathHeight = 0.25f;
  float globeDistanceFromPath = 4.0f;
  Vec3 light0Position = new Vec3(0,3,0);


  /**
   * Method for disposing models, calls each model's dispose or destroy function.
   * @param gl
   */
  private void disposeModels(GL3 gl) {
    
    skybox.dispose(gl);
    // fakeskybox.dispose(gl);

    lights[0].dispose(gl);
    lights[1].dispose(gl);

    walls.dispose(gl);

    //Globe
    globe.dispose(gl);
    robot1.dispose(gl);


    textures.destroy(gl);
  }

  /**
   * Texture Method for loading all the textures
   * @param gl GL3 for OpenGL
   */
  private void loadTextures(GL3 gl) {
    textures = new TextureLibrary();


    textures.add(gl, "diffuse_container", "assets/textures/container2.jpg");
    textures.add(gl, "specular_container", "assets/textures/container2_specular.jpg");
    
    textures.add(gl, "skybox_map", "assets/textures/Space.jpg");

    textures.add(gl, "earth_map", "assets/textures/earth_diffuse.jpg");
    textures.add(gl, "earth_specular", "assets/textures/earth_specular.jpg");

    textures.add(gl, "gold_colour", "assets/textures/GoldDiffuse.png");
    textures.add(gl, "pure_spec", "assets/textures/PureSpec.png");

    textures.add(gl, "floor_diffuse", "assets/textures/Floor.jpg");
    textures.add(gl, "floor_spec", "assets/textures/Floor_Spec.jpg");

    textures.add(gl, "metal_wall_diff", "assets/textures/metal_wall_diff.jpg");

    textures.add(gl, "name_diff", "assets/textures/diffuse_keshav.png");
    textures.add(gl, "name_spec", "assets/textures/specular_keshav.png");

    textures.add(gl, "silver_diff", "assets/textures/Silver.png");

    textures.addRepeating(gl, "vader_repeat", "assets/textures/vader.png");

    textures.add(gl, "robot2", "assets/textures/robot2.jpg");
    textures.add(gl, "robot2_spec", "assets/textures/robot2_spec.jpg");

    textures.addRepeating(gl, "roof", "assets/textures/Roof3.jpg");
    textures.addRepeating(gl, "roof_spec", "assets/textures/Roof2Spc.jpg");

    textures.add(gl, "robot_eyes", "assets/textures/Eyes.jpg");

    textures.add(gl, "robot1", "assets/textures/Robot1.jpg");

    String[] skyboxTextures = {
      "assets/textures/skybox/Right.png", //Right
      "assets/textures/skybox/Left.png", //Left
      "assets/textures/skybox/Top.png", //Top
      "assets/textures/skybox/Bottom.png", //Bottom
      "assets/textures/skybox/Front.png", //Front
      "assets/textures/skybox/Back.png", //Back
      
    };

    textures.addSkybox(gl,"skybox",skyboxTextures);
  
  }
  
  /**
   * Method for initialising each object within the scene
   * @param gl GL3 for OpenGL
   */
  public void initialise(GL3 gl) {

    
    loadTextures(gl);
    skybox = new Skybox(gl, textures.get("skybox"), camera);


    lights[0] = new Light(gl);
    lights[0].setCamera(camera);
    lights[0].setPosition(new Vec3(0,0,0));
    lights[0].setIntensity(.5f);
    
    lights[1] = new Light(gl);
    lights[1].setIsSpotlight(true);
    lights[1].setIntensity(1);
    lights[1].setCamera(camera);
    lights[1].setPosition(0,1,0);

    walls = new Walls(gl, wallThickness, wallHeight, backWallLength, otherWallLength, camera, lights, textures);
    // Globe
    globe = new Globe(gl, camera, backWallLength, otherWallLength, wallThickness, pathGapFromWall, globeDistanceFromPath, lights, textures);

    robot2 = new Robot2(gl, camera, lights, textures);

    robot1 = new Robot1(gl, camera, lights, textures);

    // String name = "fakeSkybox";
    // Mesh mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
    // Shader shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_standard_m_1t.txt");
    // Mat4 modelMatrix = Mat4.multiply(Mat4Transform.translate(0,0,0), Mat4Transform.scale(45,45,45));
    // Material material = new Material(new Vec3(0.3f, 0.3f, 0.3f), new Vec3(0.7f, 0.7f, 0.7f), new Vec3(0.3f, 0.3f, 0.3f), 20.0f);
    // fakeskybox = new ModelMultipleLights(name, mesh, modelMatrix, shader, material, lights, camera, textures.get("skybox_map"));
    
    this.robot1Pos = robot1.getPosition();
    this.robot1Paused = robot1.toggleAwake();
  }


  // Using the 3D Distance calculation
  private void activateRobots() {
    Vec3 robot2Pos = getRobot2CurrentPosition();

    double distance = Math.sqrt(Math.pow((robot2Pos.x - robot1Pos.x), 2) + Math.pow((robot2Pos.y-robot1Pos.y),2) + Math.pow((robot2Pos.z - robot1Pos.z),2));


    if (distance <= 6.8 || overridePause) {
      if (robot1Paused) {
        robot1Paused = robot1.toggleAwake();
      }
    } else {
      if (!robot1Paused) {
        robot1Paused = robot1.toggleAwake();
      }
    }
    
  }


  /**
   * Renders all objects
   * @param gl GL3 for OpenGl
   */
  private void render(GL3 gl) {
    
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

    robot2.render(gl, getSeconds()-startTime);
    robot1.render(gl);
    activateRobots();

    lights[0].render(gl);
    lights[0].setPosition(light0Position);

    lights[1].render(gl);
    walls.render(gl);

    globe.render(gl, getSeconds()-startTime);

    gl.glDepthFunc(GL3.GL_LEQUAL);
    gl.glDisable(GL.GL_CULL_FACE);
    gl.glDepthMask(false);
    skybox.render(gl, getSeconds()-startTime);
    // fakeskybox.render(gl);
    gl.glEnable(GL.GL_CULL_FACE);
    gl.glCullFace(GL.GL_BACK); 
    gl.glDepthMask(true);
    gl.glDepthFunc(GL3.GL_LESS);

  }
  
    // ***************************************************
  /* TIME
   */ 
  
  private double startTime;
  
  private double getSeconds() {
    return System.currentTimeMillis()/1000.0;
  }


  
   // ***************************************************
  /* INTERACTION
   *
   *
   *
   */

  public void makeRobot1Dance() {
    overridePause = !overridePause;
  }

  public void startStopRobot2() {
    robot2.startStopRobot2();
  }

  public Vec3 getRobot2CurrentPosition() {
    return robot2.getCurrentLocation();
  }

  public Vec3 getRobot1BasePosition() {
    return robot1.getPosition();
  }

  public void setGeneralLightIntensity(int i) {
    lights[0].setIntensity((float)i/100);
  }

  public void setSpotlightIntensity(int i) {
    lights[1].setIntensity((float)i/100);
  }
  
  
}