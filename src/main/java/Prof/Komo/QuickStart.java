package Prof.Komo;

import java.io.File;
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
 * This is the GeoTools Quickstart application used in documentationa and tutorials. *
 */
public class QuickStart {

    /**
     * GeoTools Quickstart demo application. Prompts the user for a shapefile and displays its
     * contents on the screen in a map frame
     */
    public static void main(String[] args) throws Exception {
        // display a data store file chooser dialog for shapefiles
        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file == null) {
            return;
        }

        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = store.getFeatureSource();

        SimpleFeatureCollection collection = featureSource.getFeatures();
        
        SimpleFeatureIterator iter = collection.features();
        
        while(iter.hasNext()) {
            SimpleFeature feat = iter.next();
            System.out.println(feat.getName());
            System.out.println(feat.getType());
            System.out.println(feat.getValue());
            System.out.println(feat.getAttribute("the_geom"));
            
            MultiPolygon multiPoly = (MultiPolygon) feat.getAttribute("the_geom");
            
            System.out.println(multiPoly.getNumPoints());
            System.out.println(Arrays.toString(multiPoly.getCoordinates()));
            System.out.println(getArea(multiPoly.getCoordinates()));
            
            System.out.println();
        }
        
        
        // Create a map content and add our shapefile to it
        MapContent map = new MapContent();
        map.setTitle("Quickstart");
        
        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource, style);
        map.addLayer(layer);

        // Now display the map
        JMapFrame.showMap(map);
    }
    
    public static double getArea(Coordinate[] coors) {
        double area = 0;
        
        for(int i = 0; i < coors.length - 1; i++) {
            area += coors[i].x * coors[i+1].y - coors[i].y * coors[i+1].x;
        }
        
        area += coors[coors.length - 1].x * coors[0].y - coors[coors.length - 1].y * coors[0].x;
        
        return area;
    }
    
}