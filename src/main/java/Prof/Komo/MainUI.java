package Prof.Komo;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javafx.scene.control.TextArea;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.geotools.data.*;
import org.geotools.data.simple.*;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.event.MapLayerEvent;
import org.geotools.map.event.MapLayerListener;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.CursorTool;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.coordinate.LineString;

import javafx.scene.layout.VBox;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.io.File;

import javax.swing.*;

import org.geotools.swing.data.JFileDataStoreChooser;

/**
 * App front end
 * Sponsored by Seimens Foundation.
 */
public class MainUI extends Application
{

    private static StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

    /*
     * Convenient constants for the type of feature geometry in the shapefile
     */
    private enum GeomType { POINT, LINE, POLYGON };

    /*
     * Some default style variables
     */
    private static final Color LINE_COLOUR = Color.BLUE;
    private static final Color FILL_COLOUR = Color.CYAN;
    private static final Color SELECTED_COLOUR = Color.YELLOW;
    private static final float OPACITY = 1.0f;
    private static final float LINE_WIDTH = 1.0f;
    private static final float POINT_SIZE = 10.0f;

    private static JMapFrame mapFrame;
    private static SimpleFeatureSource featureSource;

    private static String geometryAttributeName;
    private static GeomType geometryType = GeomType.POLYGON;

    static double Reock = -1;
    static double Harr = -1;
    static double Convex = -1;
    static double PolsbyP = -1;
    static double Schwartz = -1;

    static TextArea textArea;
    
    public static void main( String[] args )
    {
        launch(args);
    }

    @Override
    public void start(Stage ps) throws Exception {
        ps.setTitle("Prof. Andrew R. M. Komo, Inc.");



        GridPane gd = new GridPane();
        gd.setAlignment(Pos.CENTER);
        gd.setHgap(10);
        gd.setVgap(10);
        gd.setPadding(new Insets(25, 25, 25, 25));

        Text scTitle = new Text("Welcome to Prof. Andrew R. M. Komo, Inc (R) Redistricting Maths App!");
        gd.add(scTitle, 0,0,3,1);

        Button btn = new Button("Exit");
        btn.setOnAction(event -> System.exit(0));
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        gd.add(hbBtn, 0, 4);

        Button b2 = new Button("Start");
        HBox hb2 = new HBox(10);
        hb2.setAlignment(Pos.BOTTOM_RIGHT);
        hb2.getChildren().add(b2);
        gd.add(hb2, 2, 4);

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setVisible(false);
        gd.add(textArea, 1, 2);



        textArea.setText("Reock:\t\t\t" + Reock + "\nHarris:\t\t\t" + Harr + "\nConvex Hull:\t\t" + Convex 
                + "\nPolsby-Popper:\t" + PolsbyP + "\nSchwartzberg:\t\t" + Schwartz);

        b2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                textArea.setVisible(true);
                File file = null;
                while(file == null) {
                    file = JFileDataStoreChooser.showOpenFile("shp", null);
                }

                try {
                    FileDataStore store = FileDataStoreFinder.getDataStore(file);
                    featureSource = store.getFeatureSource();
                    SimpleFeatureCollection collection = featureSource.getFeatures();

                    GeometryDescriptor geomDesc = featureSource.getSchema().getGeometryDescriptor();
                    geometryAttributeName = geomDesc.getLocalName();
                    
                    SimpleFeatureIterator iter = collection.features();


                    MapContent map = new MapContent();
                    map.setTitle("Professor Komo");
                    Layer layer = new FeatureLayer(featureSource, createDefaultStyle());
                    map.addLayer(layer);
                    mapFrame = new JMapFrame(map);
                    mapFrame.enableToolBar(true);
                    mapFrame.enableStatusBar(true);

                    /*
                     * Before making the map frame visible we add a new button to its
                     * toolbar for our custom feature selection tool
                     */
                    JToolBar toolBar = mapFrame.getToolBar();
                    JButton btn = new JButton("Select");
                    toolBar.addSeparator();
                    toolBar.add(btn);


                    btn.addActionListener(e -> mapFrame.getMapPane().setCursorTool(
                            new CursorTool() {

                                @Override
                                public void onMouseClicked(MapMouseEvent ev) {
                                    selectFeatures(ev);
                                }
                            }));
                    mapFrame.setSize(500, 500);
                    mapFrame.setVisible(true);
                    
                } catch(IOException e) {

                }


            }
        });

        ps.setScene(new Scene(gd, 500, 500));
        ps.show();
    }

    public static void selectFeatures(MapMouseEvent ev) {

        /*
         * Construct a 5x5 pixel rectangle centred on the mouse click position
         */
        Point screenPos = (Point) ev.getPoint();
        Rectangle screenRect = new Rectangle(screenPos.x-2, screenPos.y-2, 5, 5);

        /*
         * Transform the screen rectangle into bounding box in the coordinate
         * reference system of our map context. Note: we are using a naive method
         * here but GeoTools also offers other, more accurate methods.
         */
        AffineTransform screenToWorld = mapFrame.getMapPane().getScreenToWorldTransform();
        Rectangle2D worldRect = screenToWorld.createTransformedShape(screenRect).getBounds2D();
        ReferencedEnvelope bbox = new ReferencedEnvelope(
                worldRect,
                mapFrame.getMapContent().getCoordinateReferenceSystem());

        /*
         * Create a Filter to select features that intersect with
         * the bounding box
         */
        Filter filter = ff.intersects(ff.property(geometryAttributeName), ff.literal(bbox));

        /*
         * Use the filter to identify the selected features
         */
        try {
            SimpleFeatureCollection selectedFeatures =
                    featureSource.getFeatures(filter);

            Set<FeatureId> IDs = new HashSet<>();
            try (SimpleFeatureIterator iter = selectedFeatures.features()) {
                while (iter.hasNext()) {
                    SimpleFeature feature = iter.next();
                    IDs.add(feature.getIdentifier());

                    //System.out.println("   " + feature.getIdentifier());
                }

            }

            if (IDs.isEmpty()) {
                //System.out.println("   no feature selected");
            }

            displaySelectedFeatures(IDs);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Sets the display to paint selected features yellow and
     * unselected features in the default style.
     *
     * @param IDs identifiers of currently selected features
     */
    public static void displaySelectedFeatures(Set<FeatureId> IDs) {
        Style style;

        if (IDs.isEmpty()) {
            style = createDefaultStyle();

        } else {
            style = createSelectedStyle(IDs);
        }

        Layer layer = mapFrame.getMapContent().layers().get(0);
        ((FeatureLayer) layer).setStyle(style);
        mapFrame.getMapPane().repaint();
        
        
        
        try {
            SimpleFeatureCollection selectedFeatures = featureSource.getFeatures();

            try (SimpleFeatureIterator iter = selectedFeatures.features()) {
                
                while (iter.hasNext()) {
                    SimpleFeature feature = iter.next();
                    System.out.println(feature.getID());
                    MultiPolygon poly = (MultiPolygon) feature.getAttribute("the_geom");
                    
                    if(IDs.contains(feature.getIdentifier())) {
                        Reock = QuickStart.Reock(poly);
                        Harr = QuickStart.Harris(poly);
                        Convex = QuickStart.convexHull(poly);
                        PolsbyP = QuickStart.PP(poly);
                        Schwartz = QuickStart.Schwartzberg(poly);
                        
                        textArea.setText("Reock:\t\t\t" + Reock + "\nHarris:\t\t\t" + Harr + "\nConvex Hull:\t\t" + Convex 
                                + "\nPolsby-Popper:\t" + PolsbyP + "\nSchwartzberg:\t\t" + Schwartz);
                        break;
                    }
                     
                }

            }

            if (IDs.isEmpty()) {
                System.out.println("   no feature selected");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Create a default Style for feature display
     */
    private static Style createDefaultStyle() {
        Rule rule = createRule(LINE_COLOUR, FILL_COLOUR);

        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(rule);

        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);
        return style;
    }

    /**
     * Create a Style where features with given IDs are painted
     * yellow, while others are painted with the default colors.
     */
    private static Style createSelectedStyle(Set<FeatureId> IDs) {
        Rule selectedRule = createRule(SELECTED_COLOUR, SELECTED_COLOUR);
        selectedRule.setFilter(ff.id(IDs));

        Rule otherRule = createRule(LINE_COLOUR, FILL_COLOUR);
        otherRule.setElseFilter(true);

        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(selectedRule);
        fts.rules().add(otherRule);

        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);
        return style;
    }

    /**
     * Helper for createXXXStyle methods. Creates a new Rule containing
     * a Symbolizer tailored to the geometry type of the features that
     * we are displaying.
     */
    private static Rule createRule(Color outlineColor, Color fillColor) {
        Symbolizer symbolizer = null;
        Fill fill = null;
        Stroke stroke = sf.createStroke(ff.literal(outlineColor), ff.literal(LINE_WIDTH));

        switch (geometryType) {
        case POLYGON:
            fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));
            symbolizer = sf.createPolygonSymbolizer(stroke, fill, geometryAttributeName);
            break;

        case LINE:
            symbolizer = sf.createLineSymbolizer(stroke, geometryAttributeName);
            break;

        case POINT:
            fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));

            Mark mark = sf.getCircleMark();
            mark.setFill(fill);
            mark.setStroke(stroke);

            Graphic graphic = sf.createDefaultGraphic();
            graphic.graphicalSymbols().clear();
            graphic.graphicalSymbols().add(mark);
            graphic.setSize(ff.literal(POINT_SIZE));

            symbolizer = sf.createPointSymbolizer(graphic, geometryAttributeName);
        }

        Rule rule = sf.createRule();
        rule.symbolizers().add(symbolizer);
        return rule;
    }

}
