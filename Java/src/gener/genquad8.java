package gener;

import fea.*;
import util.*;
import model.*;
import elem.*;

// Generate mesh of quadratic elements inside
//     a macroelement with 8 nodes.
// Input: modelName - name of the finite element model;
//  nh, nv - number of elements in local coordinate directions;
//  xyp - coordinates of 8 macroelement nodes (x1,y1,x2,y2 ..);
//  [res] - relative sizes of smallest elements on
//          macroelemet edges;
//  [mat] - material name.
public class genquad8 {

    private FeModel m;
    enum vars {
        nh, nv, res, xyp, mat, end
    }

    private vars name;
    private int nh, nv;
    String mat = "1";
    private double res[] = new double[4],
                   xp[] = new double[8], yp[] = new double[8];
    //  Nodes of parent element (coordinates xi, eta)
    double xip[] = {-1, 0, 1, 1, 1, 0,-1,-1},
           etp[] = {-1,-1,-1, 0, 1, 1, 1, 0};

    public genquad8() {

        String modelName = Jmgen.RD.next();
        Jmgen.PR.printf("GenQuad8: %s\n", modelName);
        readData();
        printData();
        m = new FeModel(Jmgen.RD, Jmgen.PR);
        generateMesh();
        Jmgen.blocks.put(modelName,m);
        Jmgen.PR.printf("Mesh " + modelName +
                ": nEl = %d  nNod = %d\n", m.nEl, m.nNod);
    }

    private void readData() {
        String varName, varname;

        while (Jmgen.RD.hasNext()) {
            varName = Jmgen.RD.next();
            varname = varName.toLowerCase();
            if (varName.equals("#")) {
                Jmgen.RD.nextLine(); continue;
            }
            try {
                name = vars.valueOf(varname);
            } catch (Exception e) {
                UTIL.errorMsg("Variable name is not found: "
                        + varName);
            }
            switch (name) {
            case nh:    nh = Jmgen.RD.readInt();
                break;
            case nv:    nv = Jmgen.RD.readInt();
                break;
            case res:
                for (int i = 0; i < 4; i++)
                    res[i] = Jmgen.RD.readDouble();
                break;
            case xyp:
                for (int i = 0; i < 8; i++) {
                    xp[i] = Jmgen.RD.readDouble();
                    yp[i] = Jmgen.RD.readDouble();
                }
                // Interpolation of macroelement midside
                // nodes if both coordinates are zeroes
                for (int i = 1; i < 8; i += 2)
                    if (xp[i] == 0.0 && yp[i] == 0.0) {
                         xp[i] = 0.5*(xp[i-1]+xp[(i+1)%8]);
                         yp[i] = 0.5*(yp[i-1]+yp[(i+1)%8]);
                    }
                break;
            case mat:    mat = Jmgen.RD.next();
                break;
            case end:
                return;
            }
        }
    }

    private void printData() {
        Jmgen.PR.printf(" nh =%5d\n", nh);
        Jmgen.PR.printf(" nv =%5d\n", nv);
        Jmgen.PR.printf(" res:  ");
        for (int i = 0; i < 4; i++)
            Jmgen.PR.printf("%7.3f", res[i]);
        Jmgen.PR.printf("\n xyp:  ");
        for (int i = 0; i < 8; i++) {
            Jmgen.PR.printf("%7.3f%7.3f", xp[i],yp[i]);
            if (i == 3) Jmgen.PR.printf("\n       ");
        }
        Jmgen.PR.printf("\n");
    }

    // Shift of midside nodes to have specified element size
    private void midsideNodeShift() {
        for (int edge = 0; edge < 4; edge++) {
            // minElem = relative size of the smallest element
            double minElem = res[edge];
            int sign = 1;
            if (minElem > 0.5) {
                minElem = 1.0 - minElem;
                sign = -1;
            }
            if (edge > 1) sign = -sign;
            if (minElem > 0.0) {
                // ne = number of elements on the elem edge
                double ne = (edge%2 == 0) ?
                        (double) nh : (double) nv;
                double ratio = 0.25*(minElem*ne*ne + (ne-2))
                               /(ne-1);
                double c = (-1.0 + ratio*2)*sign;
                if (edge%2 == 0) xip[2*edge + 1] = c;
                else             etp[2*edge + 1] = c;
            }
        }
    }

    // Quadratic 2D mapping.
    // z [8] - values at nodes,
    // returns interpolated z at point xi, et.
    private double quadraticTransform(double[] z,
                                      double xi, double et) {
        double x1 = 1 - xi;    double x2 = 1 + xi;
        double e1 = 1 - et;    double e2 = 1 + et;

        return -0.25*(x1*e1*(x2 + et)*z[0]
                    + x2*e1*(x1 + et)*z[2]
                    + x2*e2*(x1 - et)*z[4]
                    + x1*e2*(x2 - et)*z[6])
               + 0.5*(x1*x2*e1*z[1]
                    + e1*e2*x2*z[3]
                    + x1*x2*e2*z[5]
                    + e1*e2*x1*z[7]);
    }

    private void generateMesh() {
        int ind[] = new int[8];
        m.nDim = 2;

        // Element connectivities
        m.nEl = nh*nv;
        m.elems = new Element[m.nEl];
        int n = 0;

        for (int iv = 0; iv < nv; iv++) {
            for (int ih = 0; ih < nh; ih++) {
                m.elems[n] = Element.newElement("quad8");
                int in0 = iv*(3*nh+2) + 2*ih;
                ind[0] = in0 + 1;
                ind[1] = in0 + 2;
                ind[2] = in0 + 3;
                int in1 = iv*(3*nh+2) + 2*nh + ih + 2;
                ind[3] = in1 + 1;
                ind[7] = in1;
                int in2 = (iv+1)*(3*nh+2) + 2*ih;
                ind[4] = in2 + 3;
                ind[5] = in2 + 2;
                ind[6] = in2 + 1;
                m.elems[n].setElemConnectivities(ind);
                m.elems[n].setElemMaterial(mat);
                n++;
            }
        }

        // Shift of midside nodes for element in xi, eta
        midsideNodeShift();

        // Node coordinate array
        m.nNod = (3*nh+2)*nv+2*nh+1;
        m.newCoordArray();
        n = 0;
        double dxi = 1.0/nh;
        double det = 1.0/nv;
        for (int iv = 0; iv < 2*nv+1; iv++) {
            for (int ih = 0; ih < 2*nh+1; ih++) {
                if (iv%2 == 1 && ih%2 == 1) continue;
                double xi = -1.0 + dxi*ih;
                double et = -1.0 + det*iv;
                //  First quadratic transform: xi,et -> s,t
                double s,t;
                if (ih%2 == 0)
                    s = quadraticTransform(xip, xi, et);
                else
                    s = 0.5*(quadraticTransform(xip,xi-dxi,et)
                        + quadraticTransform(xip,xi+dxi,et));
                if (iv%2 == 0)
                    t = quadraticTransform(etp, xi, et);
                else
                    t = 0.5*(quadraticTransform(etp,xi,et-det)
                        + quadraticTransform(etp,xi,et+det));

                //  Second quadratic transform: s,t -> x,y
                m.setNodeCoord(n,0,quadraticTransform(xp,s,t));
                m.setNodeCoord(n,1,quadraticTransform(yp,s,t));
                n++;
            }
        }
    }

}
