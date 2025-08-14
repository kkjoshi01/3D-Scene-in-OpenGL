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
 * @author Keshav Joshi kjoshi3@sheffield.ac.uk
 * Class handles the Globe object within the scene.
 * I declare that this code is my own work 
 */
public class Globe {

    private Camera camera;
    private Light[] lights;
    private ModelMultipleLights globe, globestand, centralaxis;
    private float backWallLength, otherWallLength, wallThickness, pathGapFromWall, globeDistanceFromPath;

    public void dispose(GL3 gl) {
        globe.dispose(gl);
        globestand.dispose(gl);
        centralaxis.dispose(gl);
    }

    /**
     * Constructs the globe class using parameters
     * @param gl GL3 for OpenGL
     * @param cameraIn Camera class for ModelMultipleLights
     * @param roomWidth Float value of the room's width
     * @param roomLength Float value of the room's length
     * @param wallThickIn Float value of wall thickness
     * @param pathwallGap Float value of the gap between the path and wall
     * @param globePathGap Float value of the desired gap between the globe and the path
     * @param lightIn Light classes for ModelMultipleLights
     * @param textures Texture class for ModelMultipleLights
     */
    public Globe (GL3 gl, Camera cameraIn, float roomWidth, float roomLength, float wallThickIn, float pathwallGap, float globePathGap, Light[] lightIn, TextureLibrary textures) {
        this.camera = cameraIn;
        this.lights = lightIn;
        this.backWallLength = roomWidth;
        this.otherWallLength = roomLength;
        this.wallThickness = wallThickIn;
        this.pathGapFromWall = pathwallGap;
        this.globeDistanceFromPath = globePathGap;

        String name = "globe";
        Mesh mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
        Shader shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_standard_m_2t.txt");
        Material material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
        Mat4 modelMatrix = Mat4Transform.translate(0,0,0);
        modelMatrix = Mat4.multiply(modelMatrix, Mat4Transform.scale(1.5f,1.5f,1.5f));
        modelMatrix = Mat4.multiply(modelMatrix, Mat4Transform.translate(0,0,0));
        modelMatrix = Mat4.multiply(modelMatrix, Mat4Transform.translate((((backWallLength / 2) + (wallThickness / 2)) - pathGapFromWall - globeDistanceFromPath)/1.5f,-1.25f,(((otherWallLength / 2) + (wallThickness / 2)) - pathGapFromWall - globeDistanceFromPath)/1.5f));
        globe = new ModelMultipleLights(name, mesh, modelMatrix, shader, material, lights, camera, textures.get("earth_map"), textures.get("earth_specular"));

        name = "globeStand";
        mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
        shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_standard_m_2t.txt");
        material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
        modelMatrix = Mat4.multiply(Mat4Transform.scale(1f,1f,1f),Mat4Transform.rotateAroundY(-45f));
        modelMatrix = Mat4.multiply(Mat4Transform.translate((((backWallLength / 2) + (wallThickness / 2)) - pathGapFromWall - globeDistanceFromPath),0.5f,(((otherWallLength / 2) + (wallThickness / 2)) - pathGapFromWall - globeDistanceFromPath)), modelMatrix);
        globestand = new ModelMultipleLights(name, mesh, modelMatrix, shader, material, lights, camera, textures.get("diffuse_container"), textures.get("specular_container"));

        name = "globeCentralAxis";
        mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
        shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_standard_m_2t.txt");
        material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
        modelMatrix = Mat4Transform.translate(0,0,0);
        modelMatrix = Mat4.multiply(modelMatrix, Mat4Transform.scale(0.15f,2.75f,.15f));
        modelMatrix = Mat4.multiply(Mat4Transform.translate((((backWallLength / 2) + (wallThickness / 2)) - pathGapFromWall - globeDistanceFromPath),0.5f + 2.75f/2,(((otherWallLength / 2) + (wallThickness / 2)) - pathGapFromWall - globeDistanceFromPath)), modelMatrix);
        centralaxis = new ModelMultipleLights(name, mesh, modelMatrix, shader, material, lights, camera, textures.get("gold_colour"), textures.get("pure_spec"));
    }

    public void render(GL3 gl, double elapsedTime) {
        Mat4 matrix = globeRotation(elapsedTime);
        globe.setModelMatrix(matrix);
        globe.render(gl);
        globestand.render(gl);
        centralaxis.render(gl);
    }

    /**
     * Method for rotating the globe based on elapsedTime for smooth rotating.
     * @param elapsedTime
     * @return Mat4 matrix of the Robe's rotation.
     */
    private Mat4 globeRotation(double elapsedTime) {
        float angle = (float)(elapsedTime*-50);
        Mat4 rotationMatrix = Mat4Transform.rotateAroundY(angle);
        Mat4 modelMatrix = Mat4.multiply(Mat4Transform.translate(0,0,0), rotationMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.scale(1.5f,1.5f,1.5f), modelMatrix);
        modelMatrix = Mat4.multiply(Mat4Transform.translate((((backWallLength / 2) + (wallThickness / 2)) - pathGapFromWall - globeDistanceFromPath),2f,(((otherWallLength / 2) + (wallThickness / 2)) - pathGapFromWall - globeDistanceFromPath)), modelMatrix);
        return modelMatrix;
      }

}
