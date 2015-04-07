import java.nio.ByteBuffer;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import javax.imageio.ImageIO;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;

import static java.lang.Math.sqrt;


public class PA1 {

    String windowTitle = "3D Shapes";
    public boolean closeRequested = false;

    long lastFrameTime; // used to calculate delta

    float triangleAngle; // Angle of rotation for the triangles
    float quadAngle; // Angle of rotation for the quads

    Joint bot, arm;

    {
        bot = createBot();
        arm = createArm();
    }

    boolean mode = false;
    public void run() {

        createWindow();
        getDelta(); // Initialise delta timer
        initGL();
        Joint select = null;

        while (!closeRequested) {
            pollInput();
            updateLogic(getDelta());
            if(Mouse.isButtonDown(0))
                select = checkMousePosition();
            if(Keyboard.isKeyDown(Keyboard.KEY_M) && select != null)
                select.rotFromParent += 0.5;
            if(Keyboard.isKeyDown(Keyboard.KEY_N) && select != null)
                select.rotFromParent -= 0.5;
            if(mode){
                Vector3f v = getMouseOnPlaneZ(Mouse.getX(),Mouse.getY());
                InverseKinematics.getNewPosition(v.getX(),v.getY(),arm);
            }
            /*if (Mouse.isInsideWindow() && Mouse.isButtonDown(0)) {
                float mouseDX = Mouse.getDX();
                float mouseDY = -Mouse.getDY();

                float D = sqrt(mouseDX * mouseDX + mouseDY)
                select.rotFromParent = sqrt(mouseDX * mouseSensitivity * delta)
                //System.out.println("DX/Y: " + mouseDX + "  " + mouseDY);
                //rotation.y += mouseDX * mouseSensitivity * delta;
                //rotation.x += mouseDY * mouseSensitivity * delta;
                //rotation.x = Math.max(-maxLook, Math.min(maxLook, rotation.x));
            }*/

            if(Keyboard.isKeyDown(Keyboard.KEY_F))
                mode = false;
            if(Keyboard.isKeyDown(Keyboard.KEY_I))
                mode = true;
            if(!mode)
                renderGL();
            else
                renderGL_inverse();
            Display.update();
        }
        
        cleanup();
    }

    public Joint checkMousePosition(){
        Joint select;

        Queue check = new LinkedList();
        Joint temp;

        if(mode)
            temp = arm;
        else
            temp = bot;
        check.add(temp);

        while(!check.isEmpty()) {
            select = (Joint)check.poll();
            Vector3f v = getMouseOnPlaneZ(Mouse.getX(),Mouse.getY());
            if((int)select.px == (int)v.getX() && (int)select.py == (int)v.getY()) {
                return select;
            }

            else if ((int)select.px == (int)v.getX() && (int)select.py == (int)v.getY() && mode)
                return select;
            for (Joint element : select.children) {
                check.add(element);
                /*System.out.println(element.px + " " + element.py);
                System.out.println(v.getX() + " " + v.getY());*/
            }
        }
        return null;
    }

    static public Vector3f getMouseOnPlaneZ(int mouseX, int mouseY)
    {
        FloatBuffer modelview = BufferUtils.createFloatBuffer(16);
        FloatBuffer position = BufferUtils.createFloatBuffer(3);
        FloatBuffer position1 = BufferUtils.createFloatBuffer(3);
        FloatBuffer projection = BufferUtils.createFloatBuffer(16);
        IntBuffer viewport = BufferUtils.createIntBuffer(16);

        float winX, winY;

        GL11.glGetFloat( GL11.GL_MODELVIEW_MATRIX, modelview );
        GL11.glGetFloat( GL11.GL_PROJECTION_MATRIX, projection );
        GL11.glGetInteger( GL11.GL_VIEWPORT, viewport );

        winX = (float)mouseX;
        winY = (float)mouseY;

        GLU.gluUnProject(winX, winY, 0.0f, modelview, projection, viewport, position);
        GLU.gluUnProject(winX, winY, 1.0f, modelview, projection, viewport, position1);

        float zeropoint, zeroperc;
        double posXt, posYt, posZt;
        posXt = position.get(0) - position1.get(0);
        posYt = position.get(1) - position1.get(1);
        posZt = position.get(2) - position1.get(2);

        if ((position.get(2) < 0.0 && position1.get(2) < 0.0) || (position.get(2) > 0.0 && position1.get(2) > 0.0))
            return null;

        zeropoint = 0.0f - (float)position.get(2);

        //Find the percentage that this point is between them
        zeroperc = (zeropoint / (float)posZt);

        Vector3f v = new Vector3f((float)position.get(0) + (float)(posXt * zeroperc),(float)position.get(1) + (float)(posYt * zeroperc),(float)position.get(2) + (float)(posZt * zeroperc));
        return v ;

    }

    public Joint createBot(){
        Joint root = new Joint();
        Joint knee1 = new Joint();
        Joint knee2 = new Joint();
        Joint foot1 = new Joint();
        Joint foot2 = new Joint();
        Joint chest = new Joint();
        Joint elbow1 = new Joint();
        Joint elbow2 = new Joint();
        Joint hand1 = new Joint();
        Joint hand2 = new Joint();
        Joint head = new Joint();

        knee1.distFromParent = 1.5f;
        knee1.rotFromParent = 45;
        root.children.add(knee1);

        knee2.distFromParent = 1.5f;
        knee2.rotFromParent = 135;
        root.children.add(knee2);

        foot1.distFromParent = 1.5f;
        foot1.rotFromParent = 90;
        knee1.children.add(foot1);

        foot2.distFromParent = 1.5f;
        foot2.rotFromParent = 90;
        knee2.children.add(foot2);

        chest.distFromParent = -1.5f;
        chest.rotFromParent = 90;
        root.children.add(chest);

        elbow1.distFromParent = 1.5f;
        elbow1.rotFromParent = 30;
        chest.children.add(elbow1);

        hand1.distFromParent = 1.5f;
        hand1.rotFromParent = 45;
        elbow1.children.add(hand1);

        elbow2.distFromParent = 1.5f;
        elbow2.rotFromParent = 150;
        chest.children.add(elbow2);

        hand2.distFromParent = 1.5f;
        hand2.rotFromParent = 135;
        elbow2.children.add(hand2);

        /*head.distFromParent = -0.8f;
        head.rotFromParent = 90;
        chest.children.add(head);*/

        return root;
    }

    public Joint createArm(){

        Joint root = new Joint();

        Joint link1 = new Joint();
        link1.distFromParent = 1.8f;
        link1.rotFromParent = 30;
        root.children.add(link1);

        Joint link2 =new Joint();
        link2.distFromParent = 1.8f;
        link2.rotFromParent = -45;
        link1.children.add(link2);

        Joint link3 =new Joint();
        link3.distFromParent = 1.8f;
        link3.rotFromParent = -50;
        link2.children.add(link3);

        Joint link4 =new Joint();
        link4.distFromParent = 1.8f;
        link4.rotFromParent = 60;
        link3.children.add(link4);

        return root;
    }

    private void initGL() {

        /* OpenGL */
        int width = Display.getDisplayMode().getWidth();
        int height = Display.getDisplayMode().getHeight();

        GL11.glViewport(0, 0, width, height); // Reset The Current Viewport
        GL11.glMatrixMode(GL11.GL_PROJECTION); // Select The Projection Matrix
        GL11.glLoadIdentity(); // Reset The Projection Matrix
        GLU.gluPerspective(80.0f, ((float) width / (float) height), 0.1f, 100.0f); // Calculate The Aspect Ratio Of The Window
        GL11.glMatrixMode(GL11.GL_MODELVIEW); // Select The Modelview Matrix
        GL11.glLoadIdentity(); // Reset The Modelview Matrix

        GL11.glShadeModel(GL11.GL_SMOOTH); // Enables Smooth Shading
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Black Background
        GL11.glClearDepth(1.0f); // Depth Buffer Setup
        GL11.glEnable(GL11.GL_DEPTH_TEST); // Enables Depth Testing
        GL11.glDepthFunc(GL11.GL_LEQUAL); // The Type Of Depth Test To Do
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST); // Really Nice Perspective Calculations
        Camera.create();        
    }
    
    private void updateLogic(int delta) {
        triangleAngle += 0.1f * delta; // Increase The Rotation Variable For The Triangles
        quadAngle -= 0.05f * delta; // Decrease The Rotation Variable For The Quads
    }


    private void renderGL() {

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // Clear The Screen And The Depth Buffer
        GL11.glLoadIdentity(); // Reset The View
        GL11.glTranslatef(0.0f, 0.0f, -7.0f); // Move Right And Into The Screen

        Camera.apply();
        DrawCharacter.draw(bot,mode);
    }

    private void renderGL_inverse() {

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // Clear The Screen And The Depth Buffer
        GL11.glLoadIdentity(); // Reset The View
        GL11.glTranslatef(0.0f, 0.0f, -7.0f); // Move Right And Into The Screen

        Camera.apply();
        GL11.glTranslatef(-1.8f, 0, 0);
        DrawCharacter.resetCount();
        arm.endEffectors=DrawCharacter.draw(arm,mode);
        arm.numJoints = DrawCharacter.getCount();

        /*System.out.println(arm.endEffectors.size());
        System.out.println(arm.children.size());
        System.out.println(arm.numJoints);*/
    }

    /**
     * Poll Input
     */
    public void pollInput() {
        Camera.acceptInput(getDelta());
        // scroll through key events
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE)
                    closeRequested = true;
                else if (Keyboard.getEventKey() == Keyboard.KEY_P)
                    snapshot();
            }
        }

        if (Display.isCloseRequested()) {
            closeRequested = true;
        }
    }

    public void snapshot() {
        System.out.println("Taking a snapshot ... snapshot.png");

        GL11.glReadBuffer(GL11.GL_FRONT);

        int width = Display.getDisplayMode().getWidth();
        int height= Display.getDisplayMode().getHeight();
        int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer );

        File file = new File("snapshot.png"); // The file to save to.
        String format = "PNG"; // Example: "PNG" or "JPG"
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
   
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                int i = (x + (width * y)) * bpp;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }
           
        try {
            ImageIO.write(image, format, file);
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    /** 
     * Calculate how many milliseconds have passed 
     * since last frame.
     * 
     * @return milliseconds passed since last frame 
     */
    public int getDelta() {
        long time = (Sys.getTime() * 1000) / Sys.getTimerResolution();
        int delta = (int) (time - lastFrameTime);
        lastFrameTime = time;
     
        return delta;
    }

    private void createWindow() {
        try {
            Display.setDisplayMode(new DisplayMode(840, 720));
            Display.setVSyncEnabled(true);
            Display.setTitle(windowTitle);
            Display.create();
        } catch (LWJGLException e) {
            Sys.alert("Error", "Initialization failed!\n\n" + e.getMessage());
            System.exit(0);
        }
    }
    
    /**
     * Destroy and clean up resources
     */
    private void cleanup() {
        Display.destroy();
    }
    
    public static void main(String[] args) {
        new PA1().run();
    }
    
    public static class Camera {
        public static float moveSpeed = 0.5f;

        private static float maxLook = 85;

        private static float mouseSensitivity = 0.05f;

        private static Vector3f pos;
        private static Vector3f rotation;

        public static void create() {
            pos = new Vector3f(0, 0, 0);
            rotation = new Vector3f(0, 0, 0);
        }

        public static void apply() {
            if (rotation.y / 360 > 1) {
                rotation.y -= 360;
            } else if (rotation.y / 360 < -1) {
                rotation.y += 360;
            }

            //System.out.println(rotation);
            GL11.glRotatef(rotation.x, 1, 0, 0);
            GL11.glRotatef(rotation.y, 0, 1, 0);
            GL11.glRotatef(rotation.z, 0, 0, 1);
            GL11.glTranslatef(-pos.x, -pos.y, -pos.z);
        }

        public static void acceptInput(float delta) {
            //System.out.println("delta="+delta);
            acceptInputRotate(delta);
            acceptInputMove(delta);
        }

        public static void acceptInputRotate(float delta) {
            if (Mouse.isInsideWindow() && Mouse.isButtonDown(0)) {
                float mouseDX = Mouse.getDX();
                float mouseDY = -Mouse.getDY();
                //System.out.println("DX/Y: " + mouseDX + "  " + mouseDY);
                rotation.y += mouseDX * mouseSensitivity * delta;
                rotation.x += mouseDY * mouseSensitivity * delta;
                rotation.x = Math.max(-maxLook, Math.min(maxLook, rotation.x));
            }
        }

        public static void acceptInputMove(float delta) {
            boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_W);
            boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_S);
            boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_D);
            boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_A);
            boolean keyFast = Keyboard.isKeyDown(Keyboard.KEY_Q);
            boolean keySlow = Keyboard.isKeyDown(Keyboard.KEY_E);
            boolean keyFlyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
            boolean keyFlyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

            float speed;

            if (keyFast) {
                speed = moveSpeed * 5;
            } else if (keySlow) {
                speed = moveSpeed / 2;
            } else {
                speed = moveSpeed;
            }

            speed *= delta;

            if (keyFlyUp) {
                pos.y += speed;
            }
            if (keyFlyDown) {
                pos.y -= speed;
            }

            if (keyDown) {
                pos.x -= Math.sin(Math.toRadians(rotation.y)) * speed;
                pos.z += Math.cos(Math.toRadians(rotation.y)) * speed;
            }
            if (keyUp) {
                pos.x += Math.sin(Math.toRadians(rotation.y)) * speed;
                pos.z -= Math.cos(Math.toRadians(rotation.y)) * speed;
            }
            if (keyLeft) {
                pos.x += Math.sin(Math.toRadians(rotation.y - 90)) * speed;
                pos.z -= Math.cos(Math.toRadians(rotation.y - 90)) * speed;
            }
            if (keyRight) {
                pos.x += Math.sin(Math.toRadians(rotation.y + 90)) * speed;
                pos.z -= Math.cos(Math.toRadians(rotation.y + 90)) * speed;
            }
        }

        public static void setSpeed(float speed) {
            moveSpeed = speed;
        }

        public static void setPos(Vector3f pos) {
            Camera.pos = pos;
        }

        public static Vector3f getPos() {
            return pos;
        }

        public static void setX(float x) {
            pos.x = x;
        }

        public static float getX() {
            return pos.x;
        }

        public static void addToX(float x) {
            pos.x += x;
        }

        public static void setY(float y) {
            pos.y = y;
        }

        public static float getY() {
            return pos.y;
        }

        public static void addToY(float y) {
            pos.y += y;
        }

        public static void setZ(float z) {
            pos.z = z;
        }

        public static float getZ() {
            return pos.z;
        }

        public static void addToZ(float z) {
            pos.z += z;
        }

        public static void setRotation(Vector3f rotation) {
            Camera.rotation = rotation;
        }

        public static Vector3f getRotation() {
            return rotation;
        }

        public static void setRotationX(float x) {
            rotation.x = x;
        }

        public static float getRotationX() {
            return rotation.x;
        }

        public static void addToRotationX(float x) {
            rotation.x += x;
        }

        public static void setRotationY(float y) {
            rotation.y = y;
        }

        public static float getRotationY() {
            return rotation.y;
        }

        public static void addToRotationY(float y) {
            rotation.y += y;
        }

        public static void setRotationZ(float z) {
            rotation.z = z;
        }

        public static float getRotationZ() {
            return rotation.z;
        }

        public static void addToRotationZ(float z) {
            rotation.z += z;
        }

        public static void setMaxLook(float maxLook) {
            Camera.maxLook = maxLook;
        }

        public static float getMaxLook() {
            return maxLook;
        }

        public static void setMouseSensitivity(float mouseSensitivity) {
            Camera.mouseSensitivity = mouseSensitivity;
        }

        public static float getMouseSensitivity() {
            return mouseSensitivity;
        }
    }
}
