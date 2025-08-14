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
 * This class handles Robot1
 * @author Keshav Joshi (kjoshi3@sheffield.ac.uk), design based on Steve Maddock's 7_3_robot Robot
 * I declare that this code is my own work
 */
public class Robot1 {
    private Camera camera;
    private Light[] light;
    private ModelMultipleLights sphere;
    private NameNode robotRoot;
    private TransformNode robotTranslate, robotRotate, upperLegRotate, bodyRotate, leftArmRotate, rightArmRotate, headRotate, rightEyeScale, leftEyeScale, leftEarRotate, rightEarRotate;

    private double startTime = 0;
    private double pauseDuration = 0;
    private double pauseStart = 0;
    private boolean isPaused = true;

    private float[] armAngles = {20,-20};
    private Vec3 posVec3 = new Vec3(-2.75f,.1f,-4);
    
    public void dispose(GL3 gl) {
        sphere.dispose(gl);
    }

    /**
     * Constructor, initialises the Robot.
     * @param gl GL3 package for OpenGL.
     * @param cameraIn Camera class for constructing ModelMultipleLights.
     * @param lightIn lights class for constructing ModelMultipleLights.
     * @param textures textures class for constructing ModelMultipleLights.
     */
    public Robot1(GL3 gl, Camera cameraIn, Light[] lightIn, TextureLibrary textures) {
        this.camera = cameraIn;
        this.light = lightIn;
        this.startTime = getSeconds();
        
        
        // cube = makeCube(gl);

        // base, bottomLeg, upperLeg, mainBody, leftArm, rightArm, head, leftEye, rightEye, rightEar, leftEar;

        float legHeight = 1.25f, legWidth = .25f, legLength = .25f;
        float baseheight = .3f;
        float bodyHeight = 3f, bodyWidth = .5f, bodyLength = .5f;
        float armWidth = .3f, armHeight = 1.5f, armLength = .3f;
        float headWidth = 1f, headHeight = .75f, headLength = .4f;

        Texture[] bodyTextures = {textures.get("robot1"), textures.get("pure_spec")};
        Texture[] eyeTextures = {textures.get("robot_eyes")};
        

        NameNode base = makePart(gl, "base", new Vec3(3,baseheight,3), bodyTextures);
        NameNode bottomLeg = makePart(gl, "bottom leg", new Vec3(legWidth, legHeight, legLength), bodyTextures);
        NameNode upperLeg = makePart(gl, "upper leg", new Vec3(legWidth, legHeight, legLength), bodyTextures);
        NameNode body = makePart(gl, "body", new Vec3(bodyWidth, bodyHeight, bodyLength), bodyTextures);
        NameNode leftArm = makePart(gl, "left arm", new Vec3(armWidth, armHeight, armLength), bodyTextures);
        NameNode rightArm = makePart(gl, "left arm", new Vec3(armWidth, armHeight, armLength), bodyTextures);
        NameNode head = makePart(gl, "head", new Vec3(headWidth, headHeight, headLength), bodyTextures);
        NameNode leftEye = makePart(gl, "eye", new Vec3(headWidth, headHeight, headLength), eyeTextures);
        NameNode rightEye = makePart(gl, "eye", new Vec3(headWidth, headHeight, headLength), eyeTextures);
        NameNode leftEar = makePart(gl, "eye", new Vec3(.2f, headHeight, .1f), eyeTextures);
        NameNode rightEar = makePart(gl, "eye", new Vec3(.2f, headHeight, .1f), eyeTextures);


        robotRoot = new NameNode("robotStack");

        robotTranslate = new TransformNode("robot transform", Mat4Transform.translate(posVec3));

        TransformNode bottomLegPivot = new TransformNode("bottom leg pivot", Mat4Transform.translate(0,baseheight-.15f,0));
        robotRotate = new TransformNode("robot rotate x", Mat4Transform.rotateAroundX(0));
        TransformNode bottomLegTranslate = new TransformNode("lower leg translate", Mat4Transform.translate(0, (legHeight/2), 0));

        TransformNode upperLegPivot = new TransformNode("upper leg pivot", Mat4Transform.translate(0,baseheight+.25f,0));
        upperLegRotate = new TransformNode("lower leg rx", Mat4Transform.rotateAroundX(0));
        TransformNode upperLegTranslate = new TransformNode("upper leg translate", Mat4Transform.translate(0, (legHeight/2),0));

        TransformNode bodyPivot = new TransformNode("body pivot", Mat4Transform.translate(0, baseheight+.2f, 0));
        bodyRotate = new TransformNode("rotation", Mat4Transform.rotateAroundX(0));
        TransformNode bodyTranslate = new TransformNode("body translate", Mat4Transform.translate(0,(bodyHeight/2),0));

        TransformNode leftarmPivot = new TransformNode("arm pivot", Mat4Transform.translate(bodyWidth/2-.05f, (bodyHeight / 2) - (armHeight /2), 0));
        TransformNode leftArmTranslate = new TransformNode("left arm translate", Mat4Transform.translate(0,-(armHeight/2),0));
        leftArmRotate = new TransformNode("left arm rotate", Mat4Transform.rotateAroundZ(armAngles[0]));
    
        TransformNode rightarmPivot = new TransformNode("arm pivot", Mat4Transform.translate(-bodyWidth/2+.05f, (bodyHeight / 2) - (armHeight /2), 0));
        TransformNode rightArmTranslate = new TransformNode("right arm translate", Mat4Transform.translate(0,-(armHeight/2),0));
        rightArmRotate = new TransformNode("right arm rotate", Mat4Transform.rotateAroundZ(armAngles[1]));

        TransformNode headPivot = new TransformNode("head pivot", Mat4Transform.translate(0,(bodyHeight/2)+(headHeight/2)-.4f,0));
        headRotate = new TransformNode("head rotate", Mat4Transform.rotateAroundY(0));
        TransformNode headTranslate = new TransformNode("head translate", Mat4Transform.translate(0, headHeight/2, 0));

        leftEyeScale = new TransformNode("left eye scale", Mat4Transform.scale(0.225f, 0.3f, 0.2f));
        TransformNode leftEyeTranslate = new TransformNode("left eye translate", Mat4Transform.translate(-.135f, 0, (headLength/2)-.005f));

        rightEyeScale = new TransformNode("right eye scale", Mat4Transform.scale(0.225f, 0.3f, 0.2f));
        TransformNode rightEyeTranslate = new TransformNode("left eye translate", Mat4Transform.translate(.135f, 0, (headLength/2)-.005f));

        TransformNode leftEarPivot = new TransformNode("left ear pivot",  Mat4Transform.translate(-0.2f,(0.5f/2)+(headHeight/2)-.2f,0));
        leftEarRotate = new TransformNode("left ear rotate", Mat4Transform.rotateAroundZ(35));
        TransformNode leftEarTranslate = new TransformNode("left ear translate", Mat4Transform.translate(0, (0.5f/2),0));

        TransformNode rightEarPivot = new TransformNode("right ear pivot",  Mat4Transform.translate(0.2f,(0.5f/2)+(headHeight/2)-.2f,0));
        rightEarRotate = new TransformNode("right ear rotate", Mat4Transform.rotateAroundZ(-35));
        TransformNode rightEarTranslate = new TransformNode("right ear translate", Mat4Transform.translate(0, (0.5f/2),0));

        robotRoot.addChild(robotTranslate);
            robotTranslate.addChild(base);
                base.addChild(bottomLegPivot);
                    bottomLegPivot.addChild(robotRotate);
                        robotRotate.addChild(bottomLegTranslate);
                            bottomLegTranslate.addChild(bottomLeg);
                                bottomLeg.addChild(upperLegPivot);
                                    upperLegPivot.addChild(upperLegRotate);
                                        upperLegRotate.addChild(upperLegTranslate);
                                            upperLegTranslate.addChild(upperLeg);
                                                upperLeg.addChild(bodyPivot);
                                                    bodyPivot.addChild(bodyRotate);
                                                        bodyRotate.addChild(bodyTranslate);
                                                            bodyTranslate.addChild(body);
                                                                body.addChild(leftarmPivot);
                                                                    leftarmPivot.addChild(leftArmRotate);
                                                                        leftArmRotate.addChild(leftArmTranslate);
                                                                            leftArmTranslate.addChild(leftArm);
                                                                body.addChild(rightarmPivot);
                                                                    rightarmPivot.addChild(rightArmRotate);
                                                                        rightArmRotate.addChild(rightArmTranslate);
                                                                            rightArmTranslate.addChild(rightArm);
                                                                body.addChild(headPivot);
                                                                    headPivot.addChild(headRotate);
                                                                        headRotate.addChild(headTranslate);
                                                                            headTranslate.addChild(head);
                                                                                head.addChild(leftEyeTranslate);
                                                                                    leftEyeTranslate.addChild(leftEyeScale);
                                                                                        leftEyeScale.addChild(leftEye);
                                                                                head.addChild(rightEyeTranslate);
                                                                                    rightEyeTranslate.addChild(rightEyeScale);
                                                                                        rightEyeScale.addChild(rightEye);
                                                                                head.addChild(leftEarPivot);
                                                                                    leftEarPivot.addChild(leftEarRotate);
                                                                                        leftEarRotate.addChild(leftEarTranslate);
                                                                                            leftEarTranslate.addChild(leftEar);
                                                                                head.addChild(rightEarPivot);
                                                                                    rightEarPivot.addChild(rightEarRotate);
                                                                                        rightEarRotate.addChild(rightEarTranslate);
                                                                                            rightEarTranslate.addChild(rightEar);





        robotRoot.update();
            

    }

    /**
     * Method, modified version of makePart from Robot in 7_3_robot.
     * Creates a Name, Transform and ModelNode of the robot.
     * @param gl GL3 package for OpenGL
     * @param name String name for the nodes.
     * @param size Vector3 size for scaling.
     * @param textures Textures for ModelMultipleLights construction in makeSphere()
     * @return NameNode that has a TransformNode and ModelNode
     */
    private NameNode makePart(GL3 gl, String name, Vec3 size, Texture[] textures) {
        sphere = makeSphere(gl, textures);
        NameNode part = new NameNode(name);
        Mat4 m = Mat4Transform.scale(size);
        TransformNode partTransform = new TransformNode(name + " transform", m);
        ModelNode bodyShape = new ModelNode("object("+name+")", sphere);
        part.addChild(partTransform);
        partTransform.addChild(bodyShape);
        return part;
    }

    /**
     * Method, creates a sphere using ModelMultipleLights for makePart
     * @param gl GL3 package for OpenGL
     * @param textures Texture class for ModelMultipleLights
     * @return a ModelMultipleLights object
     */
    private ModelMultipleLights makeSphere(GL3 gl, Texture[] textures) {
        String name = "sphere";
        Mesh mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
        Shader shader;
        Material material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
        if (textures.length == 2) {
            shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_standard_m_2t.txt");
            return new ModelMultipleLights(name, mesh, new Mat4(1), shader, material, light, camera, textures[0], textures[1]);
        } else if (textures.length > 0) {
            shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_standard_m_1t.txt");
            return new ModelMultipleLights(name, mesh, new Mat4(1), shader, material, light, camera, textures[0]);
        }
        shader = new Shader(gl, "assets/shaders/vs_standard.txt", "assets/shaders/fs_standard_m_0t.txt");
        return new ModelMultipleLights(name, mesh, new Mat4(1), shader, material, light, camera);
        
    }

    private double getSeconds() {
        return System.currentTimeMillis()/1000.0;
    }

    
    /**
     * Method for handling movement of the Robot. Uses sine of time variable with variation for dance-like smooth movement.
     * @param time
     */
    private void bodyMovement(double time) {
        
        float speed = 3;
        float amplifier = 15;
        float lowCalculation = (float)Math.sin(time*speed/2)*amplifier;
        float variation = (float)Math.sin(time)*5;
        
        float robotZRotation = (float)(Math.sin(time*speed))*35;
        float robotYRotation = (float)((Math.sin(time*speed))*35)+variation;
        float robotXRotation = (float)(-lowCalculation);

        float upperZRotation = -(robotZRotation)+(float)(Math.sin(time*speed/2)*amplifier*4)+variation;
        float upperYRotation = -(robotYRotation);
        float upperXRotation = -(robotXRotation);

        Mat4 legCalculation = Mat4.multiply(Mat4Transform.rotateAroundY(upperYRotation), Mat4Transform.rotateAroundZ(upperZRotation));
        legCalculation = Mat4.multiply(legCalculation, Mat4Transform.rotateAroundX(upperXRotation));

        Mat4 robotRotation = Mat4.multiply(Mat4Transform.rotateAroundZ(robotZRotation), Mat4Transform.rotateAroundY(robotYRotation));
        robotRotation = Mat4.multiply(robotRotation, Mat4Transform.rotateAroundX(robotXRotation));
        robotRotate.setTransform(robotRotation);
        upperLegRotate.setTransform(Mat4.multiply(legCalculation, Mat4Transform.rotateAroundY(upperYRotation)));

        
        float bodyCounterRotationZ = -(upperZRotation);
        float bodyCounterRotationY = -(upperYRotation);
        float bodyCounterRotationX = -(upperXRotation);

        Mat4 bodyCounterRotation = Mat4.multiply(Mat4Transform.rotateAroundZ(bodyCounterRotationZ), Mat4Transform.rotateAroundY(bodyCounterRotationY));
        bodyRotate.setTransform(Mat4.multiply(bodyCounterRotation, Mat4Transform.rotateAroundX(bodyCounterRotationX-lowCalculation-variation))); //-lowCalculation-variation
        headRotate.setTransform(Mat4.multiply(Mat4Transform.rotateAroundY((float)(Math.sin(time*speed)*amplifier*3)), Mat4Transform.rotateAroundX((float)(Math.sin(time)*amplifier*3))));

        float earRotation = (float)Math.max(0, Math.min((Math.sin(time*1.5)*amplifier*6),65));

        leftEarRotate.setTransform(Mat4Transform.rotateAroundZ(earRotation));
        rightEarRotate.setTransform(Mat4Transform.rotateAroundZ(-earRotation));
        

        float armAngle = (float)(Math.sin(time*speed)+1)/2 *(150-20)+20;

        leftArmRotate.setTransform(Mat4Transform.rotateAroundZ(armAngle));
        rightArmRotate.setTransform(Mat4Transform.rotateAroundZ(-armAngle));

        robotRoot.update();
    }

    /*
     * Method for toggling the Robot's movement.
     */
    public boolean toggleAwake() {
        double time = getSeconds();
        if (isPaused) {
            
            startTime += time - pauseStart;
            isPaused = false;
        } else {
            pauseStart = time;
            isPaused = true;
        }
        return isPaused;
    }

    /**
     * Method for getting the robot's move state.
     * @return Boolean value for if the robot is Paused.
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Method for rendering robot and the movement.
     * @param gl GL3 package for OpenGL
     */
    public void render(GL3 gl) {
        double elapsedTime = this.getSeconds() - startTime - pauseDuration;

        if (!isPaused) {
            float value = (float)Math.max(0.05,Math.min((float)-Math.sin(elapsedTime*1),.3));
            rightEyeScale.setTransform(Mat4Transform.scale(0.225f, value, 0.2f));
            leftEyeScale.setTransform(Mat4Transform.scale(0.225f, value, 0.2f));
            // animateArms(elapsedTime + pauseDuration);
            bodyMovement(elapsedTime);
            robotRoot.update();
        }
        robotRoot.draw(gl);
    }

    /**
     * Method for getting the Robot's base current position.
     * @return Vec3 position of base.
     */
    public Vec3 getPosition() {
        return posVec3;
    }


}

