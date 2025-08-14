import com.jogamp.opengl.*;

public class ModelNode extends SGNode {
  
  //Slight modification by changing it from Model class to ModelMultipleLights
  protected ModelMultipleLights model;

  public ModelNode(String name, ModelMultipleLights m) {
    super(name);
    model = m; 
  }

  public void draw(GL3 gl) {
    model.render(gl, worldTransform);
    for (int i=0; i<children.size(); i++) {
      children.get(i).draw(gl);
    }
  }

}