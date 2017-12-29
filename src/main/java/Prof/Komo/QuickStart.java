package Prof.Komo;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import java.util.ArrayList;

/**
 * Prompts the user for a shapefile and displays the contents on the screen in a map frame.
 * <p>
 * This is the GeoTools Quickstart application used in documentationa and tutorials.
 *
 * @version 0.1.0-alpha
 */
@SuppressWarnings("Duplicates")
public class QuickStart {

    private static final double AREACONVERSION = 3758.3057041374200;
    private static final double PERICONVERSION = 60.7035798453348;

    public static double Reock(MultiPolygon poly) {
        Coordinate[] coors = poly.convexHull().getCoordinates();
        double radius = Double.MAX_VALUE;

        for (int i = 0; i < coors.length; i++) {
            for (int j = i + 1; j < coors.length; j++) {
                ArrayList<Coordinate> list = new ArrayList<Coordinate>();
                list.add(coors[i]);
                list.add(coors[j]);

                Coordinate center = cocircular(list);

                boolean good = true;
                for (int m = 0; m < coors.length; m++) {
                    if (dist(center, coors[m]) > center.z + 0.001) good = false;
                }

                if (good && !Double.isNaN(center.z)) {
                    radius = Math.min(radius, center.z);
                }

            }
        }

        for (int i = 0; i < coors.length; i++) {
            for (int j = i + 1; j < coors.length; j++) {
                for (int k = j + 1; k < coors.length; k++) {
                    ArrayList<Coordinate> list = new ArrayList<Coordinate>();
                    list.add(coors[i]);
                    list.add(coors[j]);
                    list.add(coors[k]);

                    Coordinate center = cocircular(list);

                    boolean good = true;
                    for (int m = 0; m < coors.length; m++) {
                        if (dist(center, coors[m]) > center.z + 0.001) good = false;
                    }

                    if (good && !Double.isNaN(center.z)) {
                        radius = Math.min(radius, center.z);
                    }
                }
            }
        }

        radius *= PERICONVERSION;
        double circleArea = Math.PI * radius * radius;

        double area = getArea(poly);

        return area / circleArea;
    }

    public static Coordinate welzl(ArrayList<Coordinate> P, ArrayList<Coordinate> R) {
        if (P.size() == 0 || R.size() >= 3) {
            if (cocircular(R) != null) {
                Coordinate center = cocircular(R);

                return center;
            } else {
                return null;
            }
        }

        int index = (int) Math.floor(Math.random() * P.size());

        Coordinate p = P.get(index);

        P.remove(index);
        Coordinate c = welzl(P, R);

        if (c != null && dist(c, p) <= c.z) {
            System.out.println(p + " contained in " + c + " because r = " + dist(c, p));

            double radius = 0;
            for (int i = 0; i < R.size(); i++) radius = Math.max(radius, dist(R.get(i), c));
            for (int i = 0; i < R.size(); i++) if (Math.abs(dist(R.get(i), c) - radius) > 0.01) R.remove(i--);


            return c;
        } else {
            System.out.println(p + " added");
            R.add(p);

            return welzl(P, R);
        }
    }

    public static Coordinate cocircular(ArrayList<Coordinate> R) {
        if (R.size() == 0) return null;
        if (R.size() == 1) return new Coordinate(R.get(0).x, R.get(0).y, 0);
        if (R.size() == 2) {
            Coordinate mid = new Coordinate((R.get(0).x + R.get(1).x) / 2.0, (R.get(0).y + R.get(1).y) / 2.0, Double.NaN);
            return new Coordinate(mid.x, mid.y, dist(mid, R.get(0)));
        }

        double Ux = Double.NaN;
        double Uy = Double.NaN;

        for (int i = 0; i < R.size(); i++) {
            for (int j = i + 1; j < R.size(); j++) {
                for (int k = j + 1; k < R.size(); k++) {
                    Coordinate A = R.get(i);
                    Coordinate B = R.get(j);
                    Coordinate C = R.get(k);


                    double D = 2 * (A.x * (B.y - C.y) + B.x * (C.y - A.y) + C.x * (A.y - B.y));

                    double X = 1 / D * ((A.x * A.x + A.y * A.y) * (B.y - C.y) + (B.x * B.x + B.y * B.y) * (C.y - A.y) + (C.x * C.x + C.y * C.y) * (A.y - B.y));
                    double Y = -1 / D * ((A.x * A.x + A.y * A.y) * (B.x - C.x) + (B.x * B.x + B.y * B.y) * (C.x - A.x) + (C.x * C.x + C.y * C.y) * (A.x - B.x));

                    if (Double.isNaN(Ux) || Double.isNaN(Uy)) {
                        Ux = X;
                        Uy = Y;
                    } else if (Math.abs(Ux - X) > 0.01 || Math.abs(Uy - Y) > 0.01) {
                        return null;
                    }

                }
            }
        }


        return new Coordinate(Ux, Uy, dist(R.get(0), new Coordinate(Ux, Uy, Double.NaN)));
    }

    public static double Harris(MultiPolygon poly) {

        Coordinate[] coors = poly.getCoordinates();

        double maxLength = 0;
        Coordinate first = null;
        Coordinate second = null;

        for (int i = 0; i < coors.length; i++) {
            for (int j = i + 1; j < coors.length; j++) {
                double dist = dist(coors[i], coors[j]);
                if (dist > maxLength) {
                    first = coors[i];
                    second = coors[j];
                    maxLength = dist;
                }
            }
        }

        double dx = second.x - first.x;
        double dy = second.y - first.y;
        double mag = Math.sqrt(dx * dx + dy * dy);

        dx /= mag;
        dy /= mag;

        double maxWidth = 0;

        for (Coordinate coor : coors) {
            if (coor.equals2D(first))
                continue;

            double dx1 = coor.x - first.x;
            double dy1 = coor.y - first.y;

            double projx = (dx1 * dx + dy1 * dy) * dx;
            double projy = (dx1 * dx + dy1 * dy) * dy;

            double perpx = dx1 - projx;
            double perpy = dy1 - projy;

            double dist = Math.sqrt(perpx * perpx + perpy * perpy);

            if (dist > maxWidth) maxWidth = dist;
        }


        return maxWidth / maxLength;
    }

    public static double dist(Coordinate x, Coordinate y) {
        return Math.sqrt(Math.pow(x.x - y.x, 2) + Math.pow(x.y - y.y, 2));
    }

    public static double convexHull(MultiPolygon poly) {
        double area = getArea(poly);
        double hullArea = getArea((Polygon) poly.convexHull());

        return area / hullArea;
    }

    public static double PP(MultiPolygon poly) {
        double area = getArea(poly);
        double peri = getPeri(poly);

        double pp = 4 * Math.PI * area / peri / peri;
        return pp;
    }

    public static double Schwartzberg(MultiPolygon poly) {
        double radius = Math.pow(getArea(poly) / Math.PI, 0.5);
        double circumference = 2 * Math.PI * radius;
        double peri = getPeri(poly);

        return circumference / peri;
    }

    public static double getPeri(MultiPolygon poly) {
        double peri = poly.getLength();
        peri *= PERICONVERSION;
        return peri;
    }

    public static double getArea(Polygon poly) {
        double area = poly.getArea();
        area *= AREACONVERSION;
        return area;
    }

    public static double getArea(MultiPolygon poly) {
        double area = poly.getArea();
        area *= AREACONVERSION;
        return area;
    }

}