package Prof.Komo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.geotools.data.*;
import org.geotools.data.simple.*;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Prompts the user for a shapefile and displays the contents on the screen in a map frame.
 * <p>
 * This is the GeoTools Quickstart application used in documentationa and tutorials.
 * @version 0.1.0-alpha
 */
public class QuickStart {

    private static final double AREACONVERSION = 3758.3057041374200;
    private static final double PERICONVERSION = 60.7035798453348;
    
    public static double Reock(MultiPolygon poly) {
        Coordinate[] coors = poly.getCoordinates();
        
        ArrayList<Coordinate> list = new ArrayList<Coordinate>();
        for(int i = 0; i < coors.length; i++) {
            list.add(coors[i]);
        }
        
        Coordinate circle = welzl(list, new ArrayList<Coordinate>());
        
        double radius = circle.z;
        
        double circleArea = Math.PI * radius * radius;
        
        double area = getArea(poly);
        
        return area / circleArea;
    }
    
    public static Coordinate welzl(ArrayList<Coordinate> P, ArrayList<Coordinate> R) {
        if(P.size() == 0 || R.size() >= 3) {
            if(cocircular(R) != null) {
                Coordinate center = cocircular(R);
                double radius = center.distance(R.get(0));
                
                center.z = radius;
                
                return center;
            } else {
                return null;
            }
        }
        
        int index = (int) Math.floor(Math.random() * P.size());
        
        Coordinate p = P.get(index);
        
        P.remove(index);
        Coordinate c = welzl(P, R);
        double radius = c.z;
        c.z = Double.NaN;
        
        if(c.distance(p) <= radius) {
            c.z = radius;
            return c;
        } else {
            R.add(p);
            return welzl(P,R);
        }
    }
    
    public static Coordinate cocircular(ArrayList<Coordinate> R) {
        double Ux = -1000;
        double Uy = -1000;
        
        for(int i = 0; i < R.size(); i++) {
            for(int j = i+1; j < R.size(); j++) {
                for(int k = j+1; k < R.size(); k++) {
                    Coordinate A = R.get(i);
                    Coordinate B = R.get(j);
                    Coordinate C = R.get(k);
                    
                    
                    double D = 2 * (A.x * (B.y - C.y) + B.x * (C.y - A.y) + C.x * (A.y - B.y));
                    
                    double X =   1 / D * ( (A.x * A.x + A.y * A.y) * (B.y - C.y) + (B.x * B.x + B.y * B.y) * (C.y - A.y) + (C.x * C.x + C.y * C.y) * (A.y - B.y) );
                    double Y = - 1 / D * ( (A.x * A.x + A.y * A.y) * (B.x - C.x) + (B.x * B.x + B.y * B.y) * (C.x - A.x) + (C.x * C.x + C.y * C.y) * (A.x - B.x) );
                    
                    if(Ux == -1000 && Uy == -1000) {
                        Ux = X;
                        Uy = Y;
                    } else if(Math.abs(Ux - X) > 0.001 || Math.abs(Uy - Y) > 0.001) {
                        return null;
                    }
                }
            }
        }
        
        return new Coordinate(Ux,Uy, Double.NaN);
    }
    
    public static double Harris(MultiPolygon poly) {
        
        Coordinate[] coors = poly.getCoordinates();
        
        double maxLength = 0;
        Coordinate first = null;
        Coordinate second = null;
        
        for(int i = 0; i < coors.length; i++) {
            for(int j = i+1; j < coors.length; j++) {
                double dist = coors[i].distance(coors[j]);
                if(dist > maxLength) {
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
        
        for(int i = 0; i < coors.length; i++) {
            if(coors[i].equals2D(first)) continue;
            
            double dx1 = coors[i].x - first.x;
            double dy1 = coors[i].y - first.y;
            
            double projx = (dx1 * dx + dy1 * dy) * dx1;
            double projy = (dx1 * dx + dy1 * dy) * dy1;
            
            double perpx = dx1 - projx;
            double perpy = dy1 - projy;
            
            double dist = Math.sqrt(perpx * perpx + perpy * perpy);
            
            if(dist > maxWidth) maxWidth = dist;
        }
        
        
        return maxWidth / maxLength;
    }
    
    
    public static double convexHull(MultiPolygon poly) {
        double area = getArea(poly);
        double hullArea = getArea((MultiPolygon) poly.convexHull());
        
        return hullArea / area;
    }
    
    public static double PP(MultiPolygon poly) {
        double area = getArea(poly);
        double peri = getPeri(poly);
        
        double pp = 4 * Math.PI * area / peri / peri;
        return pp;
    }
    
    public static double Schartzberg(MultiPolygon poly) {
        double radius = Math.pow(getArea(poly) / Math.PI, 0.5);
        double circumference = 2 * Math.PI * radius;
        double peri = getPeri(poly);
        
        return circumference/peri;
    }
    
    public static double getPeri(MultiPolygon poly) {
        double peri = poly.getLength();
        peri *= PERICONVERSION;
        return peri;
    }
    
    public static double getArea(MultiPolygon poly) {
        double area = poly.getArea();
        area *= AREACONVERSION;
        return area;
    }
    
}