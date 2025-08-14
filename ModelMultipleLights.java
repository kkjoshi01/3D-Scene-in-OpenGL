import gmaths.*;
import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.texture.awt.*;
import com.jogamp.opengl.util.texture.spi.JPEGImage;

public class ModelMultipleLights {

  private String name;
  private Mesh mesh;
  private Mat4 modelMatrix;
  private Shader shader;
  private Material material;
  private Camera camera;
  private Light[] lights;
  private Texture diffuse;
  private Texture specular;

  public ModelMultipleLights() {
    name = null;
    mesh = null;
    modelMatrix = null;
    material = null;
    camera = null;
    lights = null;
    shader = null;
  }

  public ModelMultipleLights(String name, Mesh mesh, Mat4 modelMatrix, Shader shader, Material material, Light[] lights,
      Camera camera, Texture diffuse, Texture specular) {
    this.name = name;
    this.mesh = mesh;
    this.modelMatrix = modelMatrix;
    this.shader = shader;
    this.material = material;
    this.lights = lights;
    this.camera = camera;
    this.diffuse = diffuse;
    this.specular = specular;
  }

  public ModelMultipleLights(String name, Mesh mesh, Mat4 modelMatrix, Shader shader, Material material, Light[] lights,
      Camera camera, Texture diffuse) {
    this(name, mesh, modelMatrix, shader, material, lights, camera, diffuse, null);
  }

  public ModelMultipleLights(String name, Mesh mesh, Mat4 modelMatrix, Shader shader, Material material, Light[] lights,
      Camera camera) {
    this(name, mesh, modelMatrix, shader, material, lights, camera, null, null);
  }

  public void setName(String s) {
    this.name = s;
  }

  public void setMesh(Mesh m) {
    this.mesh = m;
  }

  public void setModelMatrix(Mat4 m) {
    modelMatrix = m;
  }

  public void setMaterial(Material material) {
    this.material = material;
  }

  public void setShader(Shader shader) {
    this.shader = shader;
  }

  public void setCamera(Camera camera) {
    this.camera = camera;
  }

  public void setLights(Light[] lights) {
    this.lights = lights;
  }

  public void setDiffuse(Texture t) {
    this.diffuse = t;
  }

  public void setSpecular(Texture t) {
    this.specular = t;
  }

  public void renderName(GL3 gl) {
    System.out.println("Name = " + name);
  }

  public void render(GL3 gl) {
    render(gl, modelMatrix);
  }

  // second version of render is so that modelMatrix can be overriden with a new parameter
  public void render(GL3 gl, Mat4 modelMatrix) {
    if (mesh_null()) {
      System.out.println("Error: null in model render");
      return;
    }

    Mat4 mvpMatrix = Mat4.multiply(camera.getPerspectiveMatrix(), Mat4.multiply(camera.getViewMatrix(), modelMatrix));
    shader.use(gl);
    shader.setFloatArray(gl, "model", modelMatrix.toFloatArrayForGLSL());
    shader.setFloatArray(gl, "mvpMatrix", mvpMatrix.toFloatArrayForGLSL());

    shader.setVec3(gl, "viewPos", camera.getPosition());

    shader.setInt(gl,"numLights", lights.length);

    for (int i=0; i<lights.length; i++) {
      
      shader.setVec3(gl, "lights["+i+"].position", lights[i].getPosition());
      shader.setVec3(gl, "lights["+i+"].ambient", lights[i].getMaterial().getAmbient());
      shader.setVec3(gl, "lights["+i+"].diffuse", lights[i].getMaterial().getDiffuse());
      shader.setVec3(gl, "lights["+i+"].specular", lights[i].getMaterial().getSpecular());

      //Intensity
      shader.setFloat(gl, "lights["+i+"].intensity", lights[i].getIntensity());

      //Attenuation
      shader.setFloat(gl, "lights["+i+"].constant", lights[i].getConstant());
      shader.setFloat(gl, "lights["+i+"].linear", lights[i].getLinear());
      shader.setFloat(gl, "lights["+i+"].quadratric", lights[i].getQuadratic());
      
      //Spotlight
      shader.setVec3(gl, "lights["+i+"].direction", lights[i].getDirection());
      shader.setFloat(gl, "lights["+i+"].cutOff", lights[i].getCutOff());
      shader.setFloat(gl, "lights["+i+"].outerCutOff", lights[i].getOuterCutOff());
      shader.setFloat(gl, "lights["+i+"].spotLightEnabled", lights[i].getIsSpotlight());
    }

    shader.setVec3(gl, "material.ambient", material.getAmbient());
    shader.setVec3(gl, "material.diffuse", material.getDiffuse());
    shader.setVec3(gl, "material.specular", material.getSpecular());
    shader.setFloat(gl, "material.shininess", material.getShininess());

    if (diffuse!=null) {
      shader.setInt(gl, "first_texture", 0);  // be careful to match these with GL_TEXTURE0 and GL_TEXTURE1
      gl.glActiveTexture(GL.GL_TEXTURE0);
      diffuse.bind(gl);
    }
    if (specular!=null) {
      shader.setInt(gl, "second_texture", 1);
      gl.glActiveTexture(GL.GL_TEXTURE1);
      specular.bind(gl);
    }

    // then render the mesh
    mesh.render(gl);
  }

  private boolean mesh_null() {
    return (mesh==null);
  }

  public void dispose(GL3 gl) {
    mesh.dispose(gl);  // only need to dispose of mesh
  }

}