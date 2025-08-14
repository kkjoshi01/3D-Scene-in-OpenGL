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
 * This class stores the Robot2 used in the assignmnt.
 *
 * @author Keshav Joshi, design based on Steve Maddock's 7_2_scene_graph M03_GLEventListener
 * 
 */
public class Robot2 {
    private Camera camera;
    private Light[] light;
    private ModelMultipleLights body, leftEye, rightEye, antennaBulbHolder, antenna, bulb;
    private NameNode robotRoot;
    private TransformNode robotTranslateX, robotRotateAll, robotRotateAntenna;

    
    // Rotation
    private boolean isRotating = false;
    private float rotateAngle = 35;
    private float antennaRotate = 20;
    private float rotationPoints[] = {
        90,
        0,
        -90,
        -180,
    };

    //Controls
    private boolean wasTranslating = true;
    private boolean isEnabled = true;
    private double pauseTime;

    //Spotlight
    private double spotlightStartTime = 0;
    private double spotlightElapsedOffset = 0;
    
    // Time
    private double moveStartTime = 0;
    private double rotationStartTime = 0;
    private float rotationDuration = 1.0f;
    private float moveDuration = 4.0f;
    
    // Translation
    private boolean isTranslating = true;
    private int currentPoint = 0;
    private Vec3 currentPosition = new Vec3(-6,0,-9);
    private Vec3[] endPositions = {
        new Vec3(-5,0,-8), //Top Left
        new Vec3(5,0,-8), //Top Right
        new Vec3(5,0,8), //Bottom Right
        new Vec3(-5,0,8) //Bottom Left

    };


    public void dispose(GL3 gl) {
        body.dispose(gl);
        leftEye.dispose(gl);
        rightEye.dispose(gl);
        antenna.dispose(gl);

    }

    /**
     * Constructor, creates the robot body and connects the using nodes.
     * @param gl GL3 package for OpenGL work.
     * @param cameraIn Camera class needed for constructing ModelMultipleLights
     * @param lightIn Light sources for constructing ModelMultipleLights
     * @param textures Textures for constructing ModelMultipleLights
     */
    public Robot2(GL3 gl, Camera cameraIn, Light[] lightIn,TextureLibrary textures) {
        this.camera = cameraIn;
        this.light = lightIn;
        
        String name = "body";
        Mesh mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
        Shader shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_standard_m_2t.txt");
        Material material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
        body = new ModelMultipleLights(name, mesh, new Mat4(1), shader, material, light, camera, textures.get("robot2"), textures.get("robot2_spec"));

        name = "leftEye";
        mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
        shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_standard_m_1t.txt");
        material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
        leftEye = new ModelMultipleLights(name, mesh, new Mat4(1), shader, material, light, camera, textures.get("robot_eyes"));

        name = "rightEye";
        mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
        shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_standard_m_1t.txt");
        material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
        rightEye = new ModelMultipleLights(name, mesh, new Mat4(1), shader, material, light, camera, textures.get("robot_eyes"));

        name = "antenna";
        mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
        shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_standard_m_2t.txt");
        material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
        antenna = new ModelMultipleLights(name, mesh, new Mat4(1), shader, material, light, camera, textures.get("robot2"), textures.get("robot2_spec"));

        name = "atennaBulbHolder";
        mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
        shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_standard_m_2t.txt");
        material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
        antennaBulbHolder = new ModelMultipleLights(name, mesh, new Mat4(1), shader, material, light, camera, textures.get("robot2"), textures.get("robot2_spec"));

        name = "bulb";
        mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
        shader = new Shader(gl, "assets/shaders/vs_light_01.txt", "assets/shaders/fs_light_01.txt");
        material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
        bulb = new ModelMultipleLights(name, mesh, new Mat4(1), shader, material, light, camera);


        robotRoot = new NameNode("robotStack");
        NameNode robotBody = new NameNode("robot body");
        Mat4 m = Mat4Transform.scale(.5f,.75f,1);
        m = Mat4.multiply(m, Mat4Transform.translate(0,0.4f,0));
        TransformNode robotBodyTransform = new TransformNode("scale(.5f,.6f,1);translate(0,0.32f,0)", m);
        ModelNode robotBodyShape = new ModelNode("Cube(0)", body);
        TransformNode translateToFace = new TransformNode("translate(0,0.175f,0.5f)", Mat4Transform.translate(0,0.375f,.5f));

        NameNode robotLeftEye = new NameNode("left eye");
        m = Mat4Transform.scale(0.175f, 0.175f, 0.0875f);
        m = Mat4.multiply(m, Mat4Transform.translate(0.75f,1f,0));
        TransformNode robotLeftEyeTransform = new TransformNode("scale(0.175f,0.175f,0.0875f);translate(0.75f,1,0)", m);
        ModelNode leftEyeShape = new ModelNode("Sphere(0)", leftEye);

        NameNode robotRightEye = new NameNode("right eye");
        m = Mat4Transform.scale(0.175f, 0.175f, 0.0875f);
        m = Mat4.multiply(m, Mat4Transform.translate(-0.75f,1f,0));
        TransformNode robotRightEyeTransform = new TransformNode("scale(0.175f,0.175f,0.0875f);translate(-0.75f,1,0)", m);
        ModelNode rightEyeShape = new ModelNode("Sphere(1)", rightEye);

        NameNode robotAntenna = new NameNode("robot antenna");
        m = Mat4Transform.scale(.175f,.9f,.175f);
        m = Mat4.multiply(m, Mat4Transform.translate(0,0,0));
        TransformNode robotAntennaTransform = new TransformNode("scale(.175f,.9f,.175f);translate(0,0,0)", m);
        ModelNode antennaShape = new ModelNode("antenna(0)", antenna);
        TransformNode antennaTranslate = new TransformNode("translate(0,.6f,0)", Mat4Transform.translate(0,1.1f,0));

        NameNode robotAntennaConnector = new NameNode("atennaConnector");
        m = Mat4Transform.scale(.3f,.3f,.3f);
        m = Mat4.multiply(m, Mat4Transform.translate(0,1.2f,0));
        TransformNode antennaConnectorTransform = new TransformNode("scale(.2f,.3f,.2f);translate(0,1.05f,0)", m);
        ModelNode antennaConnectorShape = new ModelNode("connector(0)", antennaBulbHolder);

        NameNode robotBulb = new NameNode("robotBulb");
        m = Mat4Transform.scale(.5f, .5f, .5f);
        m = Mat4.multiply(m, Mat4Transform.translate(-.7f,-0.3f,0));
        TransformNode robotBulbTransform = new TransformNode("scale(.2f,.3f,.2f);translate(0,1.05f,0)", m);
        ModelNode robotBulbShape = new ModelNode("bulb", bulb);


        robotTranslateX = new TransformNode("translate("+currentPosition.x+",0,0)", Mat4Transform.translate(currentPosition.x,currentPosition.y,currentPosition.z));
        robotRotateAll = new TransformNode("rotateAroundZ(0,0,"+rotateAngle+")", Mat4Transform.translate(0,0,rotateAngle));
        robotRotateAntenna = new TransformNode("rotateAroundZ", Mat4Transform.rotateAroundY(antennaRotate));
        

        robotRoot.addChild(robotTranslateX);
            robotTranslateX.addChild(robotRotateAll);
                robotRotateAll.addChild(robotBody);
                    robotBody.addChild(robotBodyTransform);
                        robotBodyTransform.addChild(robotBodyShape);
                    robotBody.addChild(translateToFace);
                        translateToFace.addChild(robotLeftEye);
                            robotLeftEye.addChild(robotLeftEyeTransform);
                                robotLeftEyeTransform.addChild(leftEyeShape);
                        translateToFace.addChild(robotRightEye);
                            robotRightEye.addChild(robotRightEyeTransform);
                                robotRightEyeTransform.addChild(rightEyeShape);
            robotTranslateX.addChild(antennaTranslate);
                antennaTranslate.addChild(robotAntenna);
                    robotAntenna.addChild(robotAntennaTransform);
                        robotAntennaTransform.addChild(antennaShape);
                antennaTranslate.addChild(robotAntennaConnector);
                    robotAntennaConnector.addChild(antennaConnectorTransform);
                        antennaConnectorTransform.addChild(robotRotateAntenna);
                            robotRotateAntenna.addChild(antennaConnectorShape);
                                robotRotateAntenna.addChild(robotBulb);
                                    robotBulb.addChild(robotBulbTransform);
                                        robotBulbTransform.addChild(robotBulbShape);
            


        robotRoot.update();

    }
    
    /**
     * Method, renders the robot.
     * @param gl GL3 package for OpenGL work
     */
    public void render(GL3 gl, double timeForAntenna) {
        updateBranches(timeForAntenna);
        robotRoot.draw(gl);
    }

    private double getSeconds() {
        return System.currentTimeMillis()/1000.0;
    }


    // Using the principle of linear interpolocation (LERP) 
    // Returns the next position based on time duration and gap between start and end vectors.
    
    /**
     * Method, linear interpolation (LERP) of 3D Vectors: https://docs.unity3d.com/ScriptReference/Vector3.Lerp.html
     * @param start Initial Vector3 from which the Robot is coming from.
     * @param end End Vector3 from which the Robot is going to.
     * @param t Time based on elapsed time and move duration, used to interpolate between start and end vectors.
     * @return Vector3 of position to translate to based on LERP calculations.
     */
    private Vec3 lerp(Vec3 start, Vec3 end, float t) {
        return new Vec3(
            (1-t) * start.x + t*end.x,
            (1-t) * start.y + t*end.y,
            (1-t) * start.z + t*end.z
        );
    }
    
    /**
     * Method, LERP of Angels
     * @param startAngle Initial angle from which the Robot is at or started with.
     * @param endAngle Desired end angle
     * @param t Time variable used to interpolate between start and end angles
     * @return Angle the robot should be rotated to based on LERP.
     */
    private float angleLerp (float startAngle, float endAngle, float t) {
        float angleDelta = endAngle - startAngle;
        if (angleDelta > 180) {
            angleDelta -= 360;
        } else if (angleDelta < -180) {
            angleDelta += 360;
        }

        return startAngle + t * angleDelta;
    }
    
    /**
     * Method, gets the next corner point based on endPositions.
     * @return Integer point that is the next point to traverse to.
     */
    private int getNextPoint() {
        if (currentPoint == 3) {
            return 0;
        } else {
            return currentPoint+1;
        }
    }

    /**
     * Method, rotates the Robot at corners.
     * @param elapsed Uses elapsed time for LERP calculations to smoothly rotate.
     */
    private void rotate(double elapsed) {
        if (isRotating) {
            double time = elapsed - rotationStartTime;
            float t = Math.max(0, Math.min((float)((time % rotationDuration)/rotationDuration),1));
            int prevPoint = (currentPoint == 0) ? rotationPoints.length - 1 : currentPoint - 1;
            rotateAngle = angleLerp(rotationPoints[prevPoint], rotationPoints[currentPoint],t);
            if (time >= rotationDuration) {
                isRotating = false;
                isTranslating = true;
                moveStartTime = elapsed;
                rotateAngle = rotationPoints[currentPoint];   
            }
            
        }
        
        robotRotateAll.setTransform(Mat4Transform.rotateAroundY(rotateAngle));
    }

    /**
     * Method, rotates and translates the spotlight light (position and direction) and the robotAntenna.
     * @param time Uses a time variable for smooth spotlight rotations
     */
    private void translateSpotlight(double time) {
        if (!isEnabled) {
            return;
        }
        
        float currentX = currentPosition.x;
        float currentY = currentPosition.y + 1.46f;
        float currentZ = currentPosition.z;

        Vec3 translation = new Vec3(currentX, currentY, currentZ);

        float rotation = (float)((time+spotlightElapsedOffset)* 2);

        float x = (float) Math.sin(rotation);
        float z = (float) Math.cos(rotation);

        Vec3 directionTranslation = new Vec3(x,-1.0f, z);

        light[1].setPosition(translation);
        light[1].setDirection(directionTranslation);

        antennaRotate = (rotation * (float)(180/Math.PI)+90) % 360.0f;
        robotRotateAntenna.setTransform(Mat4Transform.rotateAroundY(antennaRotate));

        
    }
    
    /**
     * Method, translates the robot between corner vectors
     * @param elapsed Time variable used for LERP calculations
     */
    private void translate(double elapsed) {
        if (isTranslating) {
            double time = elapsed - moveStartTime;
        
        if (time >= moveDuration) {
            time = 0.0;
            isTranslating = false;
            rotationStartTime = elapsed;
            isRotating = true;
            currentPoint = getNextPoint();

        }

        float t = (float)((time % moveDuration)/moveDuration);
        t = Math.max(0, Math.min(t,1));

        currentPosition = lerp(endPositions[currentPoint],endPositions[getNextPoint()],t);
        
        }
        
        robotTranslateX.setTransform(Mat4Transform.translate(currentPosition.x,currentPosition.y,currentPosition.z));
        
    }

    /**
     * Method, branch update handler for the various parts
     */
    private void updateBranches(double antennaTime) {
        double elapsedTime = getSeconds();
        
        translate(elapsedTime);
        rotate(elapsedTime);
        
        // double time = elapsedTime - pauseTime;
        translateSpotlight(antennaTime);
        
        robotRoot.update();
 

    }
    /**
     * Method, handles starting and stopping the robot.
     * Updates time variables for smooth start and stopping.
     */
    public void startStopRobot2() {
        double elapsed = getSeconds();
        if (isEnabled) {
            wasTranslating = isTranslating;
            if (isTranslating) {
                isTranslating = false;
            } else {
                isRotating = false;
            }
            pauseTime = elapsed;
            isEnabled = false;
        } else {
            isEnabled = true;
            if (wasTranslating) {
                moveStartTime += (elapsed - pauseTime);
                isTranslating = true;
            } else {
                rotationStartTime += (elapsed - pauseTime);
                isRotating = true;
            }

            if (pauseTime >0) {
                spotlightElapsedOffset += (elapsed - pauseTime);
            }
            
            
        }
    }

    /**
     * Method, gets the Robot current location for GLEventListener
     * @return Vector3 of the Robot's body current position.
     */
    public Vec3 getCurrentLocation() {
        return currentPosition;
    }


}

