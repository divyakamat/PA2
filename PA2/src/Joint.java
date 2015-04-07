import java.util.ArrayList;
import java.util.List;

/**
 * Created by Divya on 3/24/2015.
 */
public class Joint {
    List<Joint> children;
    List<Joint> endEffectors;

    float distFromParent;
    float rotFromParent;
    float px,py;
    int numJoints;

    public Joint(){
        children = new ArrayList<Joint>();
        distFromParent = 0;
        rotFromParent = 0;
        px = py = 0;
        numJoints = 0;
        endEffectors = new ArrayList<Joint>();
    }
}
