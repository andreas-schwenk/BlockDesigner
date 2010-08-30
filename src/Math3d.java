/*
 * BlockDesigner
 *
 * (c) 2010 Andreas Schwenk
 * Licensed under the MIT License
 */

public class Math3d {
    public static class Double2 {
        public double x, y;

        public Double2() {
            this(0.0, 0.0);
        }

        public Double2(double x, double y) {
            set(x, y);
        }

        public final void set(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void set(Double2 v) {
            x = v.x;
            y = v.y;
        }

        @Override
        public Double2 clone() {
            return new Double2(x, y);
        }
    }

    public static class Double3 {
        public double x, y, z;

        public Double3() {
            this(0.0, 0.0, 0.0);
        }

        public Double3(double x, double y, double z) {
            set(x, y, z);
        }

        public final void set(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public void set(Double3 v) {
            x = v.x;
            y = v.y;
            z = v.z;
        }

        public void normalize() {
            double length = Math.sqrt(x * x + y * y + z * z);
            x /= length;
            y /= length;
            z /= length;
        }

        public void add(Double3 v) {
            x += v.x;
            y += v.y;
            z += v.z;
        }

        public void add(Int3 v) {
            x += v.x;
            y += v.y;
            z += v.z;
        }

        public static Double3 add(Double3 v0, Double3 v1) {
            Double3 ret = new Double3();
            ret.x = v0.x + v1.x;
            ret.y = v0.y + v1.y;
            ret.z = v0.z + v1.z;
            return ret;
        }

        public void sub(Double3 v) {
            x -= v.x;
            y -= v.y;
            z -= v.z;
        }

        public void sub(Int3 v) {
            x -= v.x;
            y -= v.y;
            z -= v.z;
        }

        public void scalarMul(double scalar) {
            x *= scalar;
            y *= scalar;
            z *= scalar;
        }

        public static Double3 sub(Double3 minuend, Double3 subtrahend) {
            Double3 ret = new Double3();
            ret.x = minuend.x - subtrahend.x;
            ret.y = minuend.y - subtrahend.y;
            ret.z = minuend.z - subtrahend.z;
            return ret;
        }

        public static Double3 crossProduct(Double3 vec0, Double3 vec1) {
            Double3 ret = new Double3();
            ret.x = vec0.y * vec1.z - vec0.z * vec1.y;
            ret.y = vec0.z * vec1.x - vec0.x * vec1.z;
            ret.z = vec0.x * vec1.y - vec0.y * vec1.x;
            return ret;
        }

        public static double distance(Double3 vec0, double x, double y, double z) {
            return Math.sqrt(
                    (vec0.x - x) * (vec0.x - x)
                            + (vec0.y - y) * (vec0.y - y)
                            + (vec0.z - z) * (vec0.z - z));
        }

        public static double distance(Double3 vec0, Double3 vec1) {
            return Math.sqrt(
                    (vec0.x - vec1.x) * (vec0.x - vec1.x)
                            + (vec0.y - vec1.y) * (vec0.y - vec1.y)
                            + (vec0.z - vec1.z) * (vec0.z - vec1.z));
        }

        @Override
        public Double3 clone() {
            return new Double3(x, y, z);
        }
    }

    public static class Double4x4 {
        public double val[][] = new double[4][4];

        public Double4x4() {
            zero();
        }

        public final void zero() {
            int i, j;
            for (i = 0; i < 4; i++)
                for (j = 0; j < 4; j++)
                    val[i][j] = 0.0;
        }

        public void identity() {
            int i, j;
            for (i = 0; i < 4; i++)
                for (j = 0; j < 4; j++)
                    if (i == j)
                        val[i][j] = 1.0;
                    else
                        val[i][j] = 0.0;
        }

        public void rotX(double rad) {
            identity();
            val[1][1] = Math.cos(rad);
            val[1][2] = Math.sin(rad);
            val[2][1] = -Math.sin(rad);
            val[2][2] = Math.cos(rad);
        }

        public void rotY(double rad) {
            identity();
            val[0][0] = Math.cos(rad);
            val[0][2] = -Math.sin(rad);
            val[2][0] = Math.sin(rad);
            val[2][2] = Math.cos(rad);
        }

        public void rotZ(double rad) {
            identity();
            val[0][0] = Math.cos(rad);
            val[0][1] = Math.sin(rad);
            val[1][0] = -Math.sin(rad);
            val[1][1] = Math.cos(rad);
        }

        public void rotXDeg(double val) {
            rotX(val / 360.0 * 2.0 * Math.PI);
        }

        public void rotYDeg(double val) {
            rotY(val / 360.0 * 2.0 * Math.PI);
        }

        public void rotZDeg(double val) {
            rotZ(val / 360.0 * 2.0 * Math.PI);
        }

        public void mulVec(Double3 vec) {
            double w;
            Double3 tmp = vec.clone();
            vec.x = tmp.x * val[0][0] + tmp.y * val[1][0] + tmp.z * val[2][0] + 1.0 * val[3][0];
            vec.y = tmp.x * val[0][1] + tmp.y * val[1][1] + tmp.z * val[2][1] + 1.0 * val[3][1];
            vec.z = tmp.x * val[0][2] + tmp.y * val[1][2] + tmp.z * val[2][2] + 1.0 * val[3][2];
            w = tmp.x * val[0][3] + tmp.y * val[1][3] + tmp.z * val[2][3] + 1.0 * val[3][3];
            vec.x /= w;
            vec.y /= w;
            vec.z /= w;
        }
    }

    public static class Int2 {
        public int x, y;

        public Int2() {
            this(0, 0);
        }

        public Int2(int x, int y) {
            set(x, y);
        }

        public final void set(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void set(Int2 v) {
            x = v.x;
            y = v.y;
        }

        @Override
        public Int2 clone() {
            return new Int2(x, y);
        }
    }

    public static class Int3 {
        public int x, y, z;

        public Int3() {
            this(0, 0, 0);
        }

        public Int3(int x, int y, int z) {
            set(x, y, z);
        }

        public final void set(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public void set(Int3 v) {
            x = v.x;
            y = v.y;
            z = v.z;
        }

        public void add(Int3 v) {
            x += v.x;
            y += v.y;
            z += v.z;
        }

        @Override
        public Int3 clone() {
            return new Int3(x, y, z);
        }
    }

    public static class AABB {
        public Double3 min, max;

        public AABB() {
            min = new Double3();
            max = new Double3();
        }

        @Override
        public AABB clone() {
            AABB aabb = new AABB();
            aabb.min = min.clone();
            aabb.max = max.clone();
            return aabb;
        }

        public boolean collision(AABB aabb) {
            if (min.x >= aabb.max.x)
                return false;
            if (min.y >= aabb.max.y)
                return false;
            if (min.z >= aabb.max.z)
                return false;
            if (max.x <= aabb.min.x)
                return false;
            if (max.y <= aabb.min.y)
                return false;
            if (max.z <= aabb.min.z)
                return false;
            return true;
        }

        /**
         * returns distance; no intersection, if distance < 0
         */
        public double rayIntersection(Double3 rayOrig, Double3 rayDir) {
            boolean inside = true;
            Int3 quadrant = new Int3(); // 0=left; 1=right; 2=middle
            int whichPlane;
            Double3 maxT = new Double3();
            Double3 candidatePlane = new Double3();
            Double3 coord = new Double3();
            double tmp = 0.0;
            // ** find candidate planes **
            // x
            if (rayOrig.x < min.x) {
                quadrant.x = 0;
                candidatePlane.x = min.x;
                inside = false;
            } else if (rayOrig.x > max.x) {
                quadrant.x = 1;
                candidatePlane.x = max.x;
                inside = false;
            } else
                quadrant.x = 2;
            // y
            if (rayOrig.y < min.y) {
                quadrant.y = 0;
                candidatePlane.y = min.y;
                inside = false;
            } else if (rayOrig.y > max.y) {
                quadrant.y = 1;
                candidatePlane.y = max.y;
                inside = false;
            } else
                quadrant.y = 2;
            // z
            if (rayOrig.z < min.z) {
                quadrant.z = 0;
                candidatePlane.z = min.z;
                inside = false;
            } else if (rayOrig.z > max.z) {
                quadrant.z = 1;
                candidatePlane.z = max.z;
                inside = false;
            } else
                quadrant.z = 2;
            // ** ray origin inside AABB ? **
            if (inside) {
                coord.set(rayOrig);
                return 0.0;
            }
            // ** calculate T distances to candidate planes **
            // x
            if (quadrant.x != 2 && rayDir.x != 0.) // 2: middle
                maxT.x = (candidatePlane.x - rayOrig.x) / rayDir.x;
            else
                maxT.x = -1.;
            // y
            if (quadrant.y != 2 && rayDir.y != 0.) // 2: middle
                maxT.y = (candidatePlane.y - rayOrig.y) / rayDir.y;
            else
                maxT.y = -1.;
            // z
            if (quadrant.z != 2 && rayDir.z != 0.) // 2: middle
                maxT.z = (candidatePlane.z - rayOrig.z) / rayDir.z;
            else
                maxT.z = -1.;
            // ** get largest maxT-component for the final choice of intersection **
            whichPlane = 0;
            if (maxT.x > maxT.y && maxT.x > maxT.z)
                whichPlane = 0;
            else if (maxT.y > maxT.x && maxT.y > maxT.z)
                whichPlane = 1;
            else if (maxT.z > maxT.x && maxT.z > maxT.y)
                whichPlane = 2;
            // ** check if final candidate is inside box **
            switch (whichPlane) {
                case 0:
                    tmp = maxT.x;
                    if (maxT.x < 0.)
                        return -1;
                    break;
                case 1:
                    tmp = maxT.y;
                    if (maxT.y < 0.)
                        return -1;
                    break;
                case 2:
                    tmp = maxT.z;
                    if (maxT.z < 0.)
                        return -1;
                    break;
            }
            // x
            if (whichPlane != 0) {
                coord.x = rayOrig.x + tmp * rayDir.x;
                if (coord.x < min.x || coord.x > max.x)
                    return -1;
            } else
                coord.x = candidatePlane.x;
            // y
            if (whichPlane != 1) {
                coord.y = rayOrig.y + tmp * rayDir.y;
                if (coord.y < min.y || coord.y > max.y)
                    return -1;
            } else
                coord.y = candidatePlane.y;
            // z
            if (whichPlane != 2) {
                coord.z = rayOrig.z + tmp * rayDir.z;
                if (coord.z < min.z || coord.z > max.z)
                    return -1;
            } else
                coord.z = candidatePlane.z;
            return tmp;
        }
    }

    public static double getIntersection(
            Math3d.Double3 eyePos, Math3d.Double3 rayDir, Math3d.Int3 dest, double y) {
        double lambda, x, z;
        // plane: n=[0 1 0] d=y
        lambda = (y - 1.0 * eyePos.y) / (1.0 * rayDir.y);
        x = eyePos.x + lambda * rayDir.x;
        z = eyePos.z + lambda * rayDir.z;
        if (x >= 0)
            dest.x = (int) x;
        else
            dest.x = (int) x - 1;
        if (y >= 0)
            dest.y = (int) y;
        else
            dest.y = (int) y - 1;
        if (z >= 0)
            dest.z = (int) z;
        else
            dest.z = (int) z - 1;
        return Math3d.Double3.distance(eyePos, x, y, z);
    }

    public static double getIntersection(
            Math3d.Double3 eyePos, Math3d.Double3 rayDir, Math3d.Double3 dest, double y) {
        // plane: n=[0 1 0] d=y
        double lambda = (y - 1.0 * eyePos.y) / (1.0 * rayDir.y);
        dest.x = eyePos.x + lambda * rayDir.x;
        dest.y = eyePos.y + lambda * rayDir.y;
        dest.z = eyePos.z + lambda * rayDir.z;
        return Math3d.Double3.distance(eyePos, dest);
    }
}
