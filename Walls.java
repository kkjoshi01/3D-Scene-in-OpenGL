import gmaths.*;

import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.texture.awt.*;
import com.jogamp.opengl.util.texture.spi.JPEGImage;

/**
 * @author Keshav Joshi, based on Room.java by Steve Maddock in 8_1_multiple_lights.
 * I declare that this code is my own work 
 */
public class Walls {

  private Camera camera;
  private Light[] lights;

  private ModelMultipleLights tt1, rightWall, backWall, ceiling, windowWall1, windowWall2, windowWall3Upper, windowWall3Lower;

  public void dispose(GL3 gl) {
    tt1.dispose(gl);
    rightWall.dispose(gl);
    backWall.dispose(gl);
    ceiling.dispose(gl);
    windowWall1.dispose(gl);
    windowWall2.dispose(gl);
    windowWall3Upper.dispose(gl);
    windowWall3Lower.dispose(gl);
  }

  /**
   * Constructor, constructs the wall class. Creates a window wall by connecting 4 cube objects and using a seamless colour as a texture to
   * hide the edges of the walls that would be seen with other textures.
   * @param gl
   * @param wallThickness Specified size for the thickness of the walls
   * @param wallHeight Specified height for the walls.
   * @param backWallLength Specified length of the back wall
   * @param otherWallLength Specified length of the right and left side walls.
   * @param cameraIn Camera class for ModelMultipleLights constructor.
   * @param lights Light classes for ModelMultipleLights constructor.
   * @param textures Texture class for ModelMultipleLights constructor.
   */
  public Walls(GL3 gl, float wallThickness, float wallHeight, float backWallLength, float otherWallLength, Camera cameraIn, Light[] lights, TextureLibrary textures) {
    this.camera = cameraIn;
    this.lights = lights;
    String name = "flat plane";
    Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
    Shader shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_standard_m_2t.txt");
    Material material = new Material(new Vec3(0.3f, 0.3f, 0.3f), new Vec3(0.7f, 0.7f, 0.7f), new Vec3(0.3f, 0.3f, 0.3f), 20.0f);
    Mat4 modelMatrix = Mat4Transform.scale(backWallLength,1f,otherWallLength);
    tt1 = new ModelMultipleLights(name, mesh, modelMatrix, shader, material, lights, camera, textures.get("floor_diffuse"), textures.get("floor_spec"));

    name = "rightWall";
    mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
    shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_repeated_overlay_2t.txt");
    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
    modelMatrix = Mat4.multiply(Mat4Transform.translate((backWallLength / 2) + (wallThickness / 2),wallHeight / 2,0f),Mat4Transform.scale(wallThickness,wallHeight,otherWallLength));
    rightWall = new ModelMultipleLights(name, mesh, modelMatrix, shader, material, lights, camera, textures.get("metal_wall_diff"), textures.get("vader_repeat"));

    name = "backWall";
    mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
    shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_standard_m_2t.txt");
    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
    modelMatrix = Mat4.multiply(Mat4Transform.translate(0,wallHeight/2,-((otherWallLength/2) + (wallThickness / 2))), Mat4Transform.scale(backWallLength+(wallThickness*2),wallHeight,wallThickness));
    backWall = new ModelMultipleLights(name, mesh, modelMatrix, shader, material, lights, camera, textures.get("name_diff"), textures.get("name_spec"));

    name = "ceiling";
    mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
    shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_repeated_m_2t.txt");
    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
    modelMatrix = Mat4.multiply(Mat4Transform.translate(0,wallHeight+(wallThickness/2),0), Mat4Transform.scale(backWallLength+(wallThickness*2),wallThickness,otherWallLength));
    ceiling = new ModelMultipleLights(name, mesh, modelMatrix, shader, material, lights, camera, textures.get("roof"), textures.get("roof_spec"));

    // Window Wall ----------------------------------------------------------------------

    name = "leftWall1";
    mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
    shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_standard_m_2t.txt");
    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
    modelMatrix = Mat4.multiply(Mat4Transform.translate(-((backWallLength / 2) + (wallThickness / 2)),wallHeight / 2,(otherWallLength *.25f / 2) - (otherWallLength / 2)), Mat4Transform.scale(wallThickness,wallHeight,otherWallLength *.25f));
    windowWall1 = new ModelMultipleLights(name, mesh, modelMatrix, shader, material, lights, camera, textures.get("silver_diff"), textures.get("pure_spec"));

    name = "leftWall2";
    mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
    shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_standard_m_2t.txt");
    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
    modelMatrix = Mat4.multiply(Mat4Transform.translate(-((backWallLength / 2) + (wallThickness / 2)),wallHeight / 2,-(otherWallLength *.25f / 2) + (otherWallLength / 2)), Mat4Transform.scale(wallThickness,wallHeight,otherWallLength *.25f));
    windowWall2 = new ModelMultipleLights(name, mesh, modelMatrix, shader, material, lights, camera, textures.get("silver_diff"), textures.get("pure_spec"));

    name = "leftWall3Upper";
    mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
    shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_standard_m_2t.txt");
    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
    modelMatrix = Mat4.multiply(Mat4Transform.translate(-((backWallLength / 2) + (wallThickness / 2)),  (wallHeight * 7 / 8),0), Mat4Transform.scale(wallThickness,wallHeight/4,otherWallLength*.5f));
    windowWall3Upper = new ModelMultipleLights(name, mesh, modelMatrix, shader, material, lights, camera, textures.get("silver_diff"), textures.get("pure_spec"));

    name = "leftWall3Lower";
    mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
    shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_standard_m_2t.txt");
    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
    modelMatrix = Mat4.multiply(Mat4Transform.translate(-((backWallLength / 2) + (wallThickness / 2)), (wallHeight / 8),0), Mat4Transform.scale(wallThickness,wallHeight/4,otherWallLength*.5f));
    windowWall3Lower = new ModelMultipleLights(name, mesh, modelMatrix, shader, material, lights, camera, textures.get("silver_diff"), textures.get("pure_spec"));
  }

  /**
   * Method for rendering the walls
   * @param gl GL3 for OpenGL
   */
  public void render(GL3 gl) {
    tt1.render(gl);
    rightWall.render(gl);
    backWall.render(gl);
    ceiling.render(gl);
    windowWall1.render(gl);
    windowWall2.render(gl);
    windowWall3Upper.render(gl);
    windowWall3Lower.render(gl);
  }
}