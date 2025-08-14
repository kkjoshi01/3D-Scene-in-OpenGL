import gmaths.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

/**
 * @author Steve Maddock (7_1_stack_of_objects M01.java), modified by Keshav Joshi
 */
public class Spacecraft extends JFrame implements ActionListener, ChangeListener {
  
  private static final int WIDTH = 1024;
  private static final int HEIGHT = 768;
  private static final Dimension dimension = new Dimension(WIDTH, HEIGHT);
  private GLCanvas canvas;
  private M01_GLEventListener glEventListener;
  private JSlider generalLight, spotlight;
  private final FPSAnimator animator;
  private Camera camera;

  public static void main(String[] args) {
    Spacecraft b1 = new Spacecraft("Spacecraft");
    b1.getContentPane().setPreferredSize(dimension);
    b1.pack();
    b1.setVisible(true);
  }

  public Spacecraft(String textForTitleBar) {
    super(textForTitleBar);
    GLCapabilities glcapabilities = new GLCapabilities(GLProfile.get(GLProfile.GL3));
    canvas = new GLCanvas(glcapabilities);
    camera = new Camera(Camera.DEFAULT_POSITION, Camera.DEFAULT_TARGET, Camera.DEFAULT_UP);
    glEventListener = new M01_GLEventListener(camera);
    canvas.addGLEventListener(glEventListener);
    canvas.addMouseMotionListener(new MyMouseInput(camera));
    canvas.addKeyListener(new MyKeyboardInput(camera));
    getContentPane().add(canvas, BorderLayout.CENTER);

    JMenuBar menuBar=new JMenuBar();
    this.setJMenuBar(menuBar);
      JMenu fileMenu = new JMenu("File");
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(this);
        fileMenu.add(quitItem);
    menuBar.add(fileMenu);

    JPanel p = new JPanel();
      JButton b = new JButton("Start / Stop Robot 2");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("Override Dancing Animation");
      b.addActionListener(this);
      p.add(b);
      
    this.add(p, BorderLayout.SOUTH);
    generalLight = new JSlider(0,100,50);
      generalLight.setPaintTicks(true);
      generalLight.setPaintTrack(true);
      generalLight.setPaintLabels(true);
      generalLight.setMajorTickSpacing(20);
      generalLight.setMajorTickSpacing(5);
      generalLight.addChangeListener(this);
      p.add(generalLight);
      JLabel l = new JLabel("General Light Intensity (%)");
      p.add(l);

    spotlight = new JSlider(0,100,100);
      spotlight.setPaintTicks(true);
      spotlight.setPaintTrack(true);
      spotlight.setPaintLabels(true);
      spotlight.setMajorTickSpacing(20);
      spotlight.setMajorTickSpacing(5);
      spotlight.addChangeListener(this);
      p.add(spotlight);
      l = new JLabel("Spotlight Intensity (%)");
      p.add(l);

    

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        animator.stop();
        remove(canvas);
        dispose();
        System.exit(0);
      }
    });
    animator = new FPSAnimator(canvas, 60);
    animator.start();
  }
  /**
   * Method for handling action events of buttons
   */
  public void actionPerformed(ActionEvent e) {
    
    if (e.getActionCommand().equalsIgnoreCase("Start / Stop Robot 2")) {
      glEventListener.startStopRobot2();
    } else if (e.getActionCommand().equalsIgnoreCase("Override Dancing Animation")) {
      glEventListener.makeRobot1Dance();
    }
  }

  /**
   * Method for handling state changes of sliders and updating intensities
   */
  public void stateChanged(ChangeEvent e) {
    
    glEventListener.setGeneralLightIntensity(generalLight.getValue());
    glEventListener.setSpotlightIntensity(spotlight.getValue());
  }

  
}


 
class MyKeyboardInput extends KeyAdapter  {
  private Camera camera;
  
  public MyKeyboardInput(Camera camera) {
    this.camera = camera;
  }
  
  public void keyPressed(KeyEvent e) {
    Camera.Movement m = Camera.Movement.NO_MOVEMENT;
    switch (e.getKeyCode()) {
      case KeyEvent.VK_LEFT:  m = Camera.Movement.LEFT;  break;
      case KeyEvent.VK_RIGHT: m = Camera.Movement.RIGHT; break;
      case KeyEvent.VK_UP:    m = Camera.Movement.UP;    break;
      case KeyEvent.VK_DOWN:  m = Camera.Movement.DOWN;  break;
      case KeyEvent.VK_A:  m = Camera.Movement.FORWARD;  break;
      case KeyEvent.VK_Z:  m = Camera.Movement.BACK;  break;
    }
    camera.keyboardInput(m);
  }
}

class MyMouseInput extends MouseMotionAdapter {
  private Point lastpoint;
  private Camera camera;
  
  public MyMouseInput(Camera camera) {
    this.camera = camera;
  }
  
    /**
   * mouse is used to control camera position
   *
   * @param e  instance of MouseEvent
   */    
  public void mouseDragged(MouseEvent e) {
    Point ms = e.getPoint();
    float sensitivity = 0.001f;
    float dx=(float) (ms.x-lastpoint.x)*sensitivity;
    float dy=(float) (ms.y-lastpoint.y)*sensitivity;
    //System.out.println("dy,dy: "+dx+","+dy);
    if (e.getModifiersEx()==MouseEvent.BUTTON1_DOWN_MASK)
      camera.updateYawPitch(dx, -dy);
    lastpoint = ms;
  }

  /**
   * mouse is used to control camera position
   *
   * @param e  instance of MouseEvent
   */  
  public void mouseMoved(MouseEvent e) {   
    lastpoint = e.getPoint(); 
  }
}