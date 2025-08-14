import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Text;

import com.jogamp.opengl.*;


/**
 * This class stores the Material properties for a Mesh
 *
 * @author Dr Steve Maddock, modified by Keshav Joshi
 * 
 */
import com.jogamp.opengl.util.texture.*;

public class TextureLibrary {
  
  private Map<String,Texture> textures;

  public TextureLibrary() {
    textures = new HashMap<String, Texture>();
  }

  /**
   * Method. Calls to generate a cubemap and stores it within the texture map.
   * @param gl GL3 package for OpenGL work.
   * @param files filepaths of the images.
   * @param name String identifier for the texture in the texture map.
   */ 
  public void addSkybox(GL3 gl, String name, String[] files) {
    Texture texture = createCubeMapTexture(gl, files);
    textures.put(name, texture);
  }

  public void add(GL3 gl, String name, String filename) {
    Texture texture = loadTexture(gl, filename);
    textures.put(name, texture);
  }
  
  /**
   * Method. Calls to generate a repeating texture and stores it within the texture map.
   * @param gl GL3 package for OpenGL work.
   * @param filename filepath of the image
   * @param name String identifier for the texture in the texture map.
   * 
   */ 
  public void addRepeating(GL3 gl, String name, String filename) {
    Texture texture = loadRepeatingTexture(gl, filename);
    textures.put(name, texture);
  }

  public Texture get(String name) {
    return textures.get(name);
  }

  public static Texture loadTexture(GL3 gl3, String filename) {
    Texture t = null; 
    try {
      File f = new File(filename);
      t = (Texture)TextureIO.newTexture(f, true);
      t.bind(gl3);
      t.setTexParameteri(gl3, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE);
      t.setTexParameteri(gl3, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE); 
      t.setTexParameteri(gl3, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR_MIPMAP_LINEAR);
      t.setTexParameteri(gl3, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);
      gl3.glGenerateMipmap(GL3.GL_TEXTURE_2D);
    }
    catch(Exception e) {
      System.out.println("Error loading texture " + filename); 
    }
    return t;
  }

  /**
   * Method. Creates a repeating texture using GL3 and the filepath.
   * @param gl3 GL3 package for OpenGL work.
   * @param filename Filepath of the image
   * @return a Mipmapped Texture.
   * @throws Exception when there's an issue loading the texture.
   */ 
  public static Texture loadRepeatingTexture(GL3 gl3, String filename) {
    Texture t = null; 
    try {
      File f = new File(filename);
      t = (Texture)TextureIO.newTexture(f, true);
      t.bind(gl3);
      t.setTexParameteri(gl3, GL3.GL_TEXTURE_WRAP_S, GL3.GL_REPEAT);
      t.setTexParameteri(gl3, GL3.GL_TEXTURE_WRAP_T, GL3.GL_REPEAT); 
      t.setTexParameteri(gl3, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR_MIPMAP_LINEAR);
      t.setTexParameteri(gl3, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);
      gl3.glGenerateMipmap(GL3.GL_TEXTURE_2D);
    }
    catch(Exception e) {
      System.out.println("Error loading texture " + filename); 
    }
    return t;
  }

  /**
   * Method. Creates a cubemap texture using GL3 and a list of file paths. Uses ideas from: https://learnopengl.com/Advanced-OpenGL/Cubemaps and
   * https://github.com/sgothel/jogl-demos/blob/master/src/demos/util/Cubemap.java#L79
   * @param gl3 GL3 package for OpenGL work.
   * @param files String list of file paths
   * @return a mipmapped cube map texture.
   * @throws Exception when there's an issue loading the cubemap.
   */
  public static Texture createCubeMapTexture(GL3 gl3, String[] files) {
    Texture t = TextureIO.newTexture(GL3.GL_TEXTURE_CUBE_MAP);
    try {
      for (int i = 0; i<files.length; i++) {

        File file = new File(files[i]);
        TextureData data = TextureIO.newTextureData(gl3.getGLProfile(), file, false, null);
        if (data == null) {
          System.err.println("Couldn't load a texture " + files[i]);
        }
        t.updateImage(gl3, data, GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i);
        t.bind(gl3);
      }
      t.setTexParameteri(gl3, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR_MIPMAP_LINEAR);
      t.setTexParameteri(gl3, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);
      t.setTexParameteri(gl3, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE);
      t.setTexParameteri(gl3, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE);
      t.setTexParameteri(gl3, GL3.GL_TEXTURE_WRAP_R, GL3.GL_CLAMP_TO_EDGE);
      t.bind(gl3);
      
      gl3.glGenerateMipmap(GL3.GL_TEXTURE_CUBE_MAP);
    } catch(Exception e) {
      System.out.println("Error loading cubemap textures.");
    }
    return t;
  }

  public void destroy(GL3 gl3) {
    for (var entry : textures.entrySet()) {
      entry.getValue().destroy(gl3);
    }
  }
}