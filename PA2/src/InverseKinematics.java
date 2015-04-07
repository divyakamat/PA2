import org.ejml.factory.SingularMatrixException;
import org.ejml.simple.SimpleMatrix;
import org.lwjgl.util.vector.Vector3f;

/**
 * Created by Divya on 4/2/2015.
 */
public class InverseKinematics {

    private static SimpleMatrix calcJacobian(Joint root){
        SimpleMatrix Jacobian = new SimpleMatrix(3,(root.numJoints)+1);

        Joint temp = root;

        Vector3f s = new Vector3f();
        s.x=root.endEffectors.get(0).px;
        s.y=root.endEffectors.get(0).py;
        s.z=0;
        Vector3f e = new Vector3f();
        Vector3f v = new Vector3f(0,0,1);

        int count =0;

        while(temp != null)
        {
            Vector3f res = new Vector3f();
            Vector3f p = new Vector3f(temp.px,temp.py,0);

            Vector3f.cross(v,Vector3f.sub(s,p,null),res);

            Jacobian.set(0,count,res.x);
            Jacobian.set(1,count,res.y);


            Jacobian.set(2,count,res.z);

            count++;

            if(temp.children.size()!=0)
                temp = temp.children.get(0);
            else
                temp =null;
        }

        return Jacobian;
    }

    public static void getNewPosition(float mouseX, float mouseY, Joint root){
        Vector3f t = new Vector3f(mouseX,mouseY,0);
        Vector3f e = new Vector3f();

        Vector3f s = new Vector3f();
        s.x=root.endEffectors.get(0).px;
        s.y=root.endEffectors.get(0).py;
        s.z=0;

        Vector3f.sub(t,s,e);
        SimpleMatrix eM = new SimpleMatrix(3,1);
        eM.set(0,0,e.x);
        eM.set(1,0,e.y);
        eM.set(2,0,e.z);

        SimpleMatrix Jacobian = calcJacobian(root);
        SimpleMatrix trans = Jacobian.mult(Jacobian.transpose());
        SimpleMatrix iden = SimpleMatrix.identity(trans.numCols());

        float lambda = 50f;
        for(int i=0;i<trans.numCols();i++)
            iden.set(i,i,lambda);

        trans = trans.plus(iden);
        SimpleMatrix f = linearSolve(trans, eM);
        SimpleMatrix d_theta = Jacobian.transpose().mult(f);

        System.out.println(d_theta.numCols() + " " + d_theta.numRows());

        for(int i=0;i<d_theta.numRows();i++)
            System.out.println(d_theta.get(i,0));

        int cnt = 0;
        Joint temp = root.children.get(0);
        while(temp != null) {
            temp.rotFromParent -= (float)Math.toDegrees(d_theta.get(cnt,0));

            cnt++;
            if(temp.children.size()!=0)
                temp = temp.children.get(0);
            else
                temp = null;
        }

    }

    private static SimpleMatrix linearSolve(SimpleMatrix A, SimpleMatrix b) {
        SimpleMatrix res = null;
        try {
            SimpleMatrix x = A.solve(b);
            System.out.println("solution x = " + x);

            // verify the solution: Ax - b = 0
            res = A.mult(x).minus(b);
            System.out.println("residual = " + res);

            // due the the numerical error norm(Ax - b) might not be exact zero,
            // but it should be very small
            if ( res.normF() > 1E-8f ) {
                throw new RuntimeException("The linear solve has problems!");
            }
            return x;
        } catch ( SingularMatrixException e ) {
            throw new IllegalArgumentException("Singular matrix");
        }
    }


}
