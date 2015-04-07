import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Sphere;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Created by Divya on 3/24/2015.
 */


public class DrawCharacter {

    private static int count = 0;
    //private static List<Joint> endEff = new ArrayList<Joint>();

    public static List<Joint> draw(Joint root,boolean mode){

        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        drawSphere(0.3f);

        List<Joint> endEff = new ArrayList<Joint>();

        count++;
        FloatBuffer model = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, model);
        if(mode)
            root.px = model.get(4*3)+1.8f;
        else
            root.px = model.get(4*3);
        root.py = model.get(4*3+1);
        if(root.children.size() == 0) {
            endEff.add(root);
            return endEff;
        }
        for (Joint element : root.children) {
            GL11.glPushMatrix();
            float xmax = element.distFromParent *(float) cos(element.rotFromParent * 3.1416 / 180);
            float ymax = element.distFromParent * (float)sin(element.rotFromParent * 3.1416 / 180);
            GL11.glPushMatrix();
            GL11.glRotatef(90,1,0,0);
            GL11.glRotatef(90-element.rotFromParent,0,1,0);
            drawCube(element.distFromParent);
            GL11.glPopMatrix();
            GL11.glTranslatef(xmax,-ymax, 0);
            endEff.addAll(draw(element,mode));
            GL11.glPopMatrix();
        }

        return endEff;
    }

    public static int getCount(){
        return count;
    }

    public static void resetCount() {
        count =0;
    }

    /*public static List<Joint> getEndEffectors(){
        System.out.println(endEff.size());
        return endEff;
    }

    public static void resetEndEffectors(){
        endEff.remove(0);
    }*/
    private static void drawSphere(float rad){
        Sphere sphere = new Sphere();

        sphere.draw(rad, 16, 16);
    }

    private static void drawCube(float d) {

        GL11.glBegin(GL11.GL_QUADS); // Start Drawing The Cube
        GL11.glColor3f(1.0f, 0.0f, 0.0f); // Set The Color To Green
        GL11.glVertex3f(0.2f, 0.2f, 0); // Top Right Of The Quad (Top)
        GL11.glVertex3f(-0.2f, 0.2f, 0); // Top Left Of The Quad (Top)
        GL11.glVertex3f(-0.2f, 0.2f, d); // Bottom Left Of The Quad (Top)
        GL11.glVertex3f(0.2f, 0.2f, d); // Bottom Right Of The Quad (Top)

        //GL11.glColor3f(1.0f, 0.5f, 0.0f); // Set The Color To Orange
        GL11.glVertex3f(0.2f, -0.2f, d); // Top Right Of The Quad (Bottom)
        GL11.glVertex3f(-0.2f, -0.2f, d); // Top Left Of The Quad (Bottom)
        GL11.glVertex3f(-0.2f, -0.2f, 0); // Bottom Left Of The Quad (Bottom)
        GL11.glVertex3f(0.2f, -0.2f, 0); // Bottom Right Of The Quad (Bottom)

       /* GL11.glColor3f(1.0f, 0.0f, 0.0f); // Set The Color To Red
        GL11.glVertex3f(1.0f, 1.0f, 1.0f); // Top Right Of The Quad (Front)
        GL11.glVertex3f(-1.0f, 1.0f, 1.0f); // Top Left Of The Quad (Front)
        GL11.glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Left Of The Quad (Front)
        GL11.glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Right Of The Quad (Front)

        GL11.glColor3f(1.0f, 1.0f, 0.0f); // Set The Color To Yellow
        GL11.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Left Of The Quad (Back)
        GL11.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Right Of The Quad (Back)
        GL11.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Right Of The Quad (Back)
        GL11.glVertex3f(1.0f, 1.0f, -1.0f); // Top Left Of The Quad (Back)

        GL11.glColor3f(0.0f, 0.0f, 1.0f); // Set The Color To Blue
        GL11.glVertex3f(-1.0f, 1.0f, 1.0f); // Top Right Of The Quad (Left)
        GL11.glVertex3f(-1.0f, 1.0f, -1.0f); // Top Left Of The Quad (Left)
        GL11.glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Left Of The Quad (Left)
        GL11.glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Right Of The Quad (Left)

        GL11.glColor3f(1.0f, 0.0f, 1.0f); // Set The Color To Violet
        GL11.glVertex3f(1.0f, 1.0f, -1.0f); // Top Right Of The Quad (Right)
        GL11.glVertex3f(1.0f, 1.0f, 1.0f); // Top Left Of The Quad (Right)
        GL11.glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Left Of The Quad (Right)
        GL11.glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Right Of The Quad (Right)*/
        GL11.glEnd(); // Done Drawing The Quad
    }
}
