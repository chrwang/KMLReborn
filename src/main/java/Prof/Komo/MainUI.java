package Prof.Komo;

import com.vividsolutions.jts.geom.MultiPolygon;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.control.TextArea;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.*;
import org.geotools.styling.Stroke;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.CursorTool;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * App front end, using the one and only JavaFX (and some Swing, because Geotools.)
 * <p>
 * Acknowledgements to Professor Andrew R. M. Komo for technical, moral, and metaphorical support
 * Sponsored by Seimens Foundation.
 */
@SuppressWarnings("Duplicates")
public class MainUI extends Application {

    /*
     * Some default style variables
     */
    private static final Color LINE_COLOUR = Color.BLUE;
    private static final Color FILL_COLOUR = Color.CYAN;
    private static final Color SELECTED_COLOUR = Color.YELLOW;

    private static final float OPACITY = 1.0f;
    private static final float LINE_WIDTH = 1.0f;
    private static final float POINT_SIZE = 10.0f;
    private static StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    private static JMapFrame mapFrame;
    private static SimpleFeatureSource featureSource;
    private static String geometryAttributeName;
    private static GeomType geometryType = GeomType.POLYGON;
    private static double Reock = -1;
    private static double Harr = -1;
    private static double Convex = -1;
    private static double PolsbyP = -1;
    private static double Schwartz = -1;
    private File rawMap;
    private MapContent mc;
    private static TextArea logView = new TextArea("Begin KomoLog V0.0.1a");

    public static void main(String[] args) {
        launch(args);
    }

    public static void selectFeatures(MapMouseEvent ev) {

        /*
         * Construct a 5x5 pixel rectangle centred on the mouse click position
         */
        Point screenPos = (Point) ev.getPoint();
        Rectangle screenRect = new Rectangle(screenPos.x - 2, screenPos.y - 2, 5, 5);

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

            //System.out.println(selectedFeatures.size());

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
                    //System.out.println(feature.getID());
                    MultiPolygon poly = (MultiPolygon) feature.getAttribute("the_geom");

                    if (IDs.contains(feature.getIdentifier())) {
                        //Reock = QuickStart.Reock(poly);
                        Harr = QuickStart.Harris(poly);
                        Convex = QuickStart.convexHull(poly);
                        PolsbyP = QuickStart.PP(poly);
                        Schwartz = QuickStart.Schwartzberg(poly);
                        logView.setText(logView.getText() + "\n*****RESULTS*****\n"+"\nReock:\t\t\t" + Reock + "\nHarris:\t\t\t" + Harr + "\nConvex Hull:\t\t" + Convex
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
        Fill fill;
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

    @Override
    public void start(Stage ps) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        ps.setTitle("Prof. Andrew R. M. Komo, Inc.");

        GridPane gd = new GridPane();
        gd.setAlignment(Pos.TOP_CENTER);
        gd.setHgap(10);
        gd.setVgap(10);
        gd.setPadding(new Insets(25, 25, 25, 25));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(33);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(33);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(33);
        gd.getColumnConstraints().addAll(col1, col2, col3);

        Text scTitle = new Text("Welcome to Prof. Andrew R. M. Komo, Inc (R) Redistricting Maths App!");
        scTitle.setId("mainText");
        scTitle.setTextAlignment(TextAlignment.CENTER);
        VBox tv = new VBox();
        tv.getChildren().addAll(scTitle);
        tv.setAlignment(Pos.TOP_CENTER);
        gd.add(tv, 0, 0, 3, 1);

        Button btn = new Button("Exit");
        btn.setOnAction(event -> System.exit(0));
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.TOP_LEFT);
        hbBtn.getChildren().add(btn);
        gd.add(hbBtn, 0, 1);

        Button b2 = new Button("Load File");
        b2.setOnAction(event -> loadMap());
        HBox hb2 = new HBox(10);
        hb2.setAlignment(Pos.TOP_RIGHT);
        hb2.getChildren().add(b2);
        gd.add(hb2, 2, 1);

        Button b3 = new Button("Draw Map");
        b3.setOnAction(event -> {
            try {
                drawMap();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        HBox hb3 = new HBox(10);
        hb3.setAlignment(Pos.TOP_LEFT);
        hb3.getChildren().add(b3);
        gd.add(hb3, 1, 1);

        HBox hb4 = new HBox();
        hb4.setAlignment(Pos.TOP_LEFT);
        logView.setText(logView.getText() + " at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        logView.setEditable(false);
        logView.setStyle("-fx-font-family: \"VT323\",\"Courier New\", monospace; -fx-font-size: 16px;");
        hb4.getChildren().add(logView);
        gd.add(hb4, 0, 2, 4, 4);

        ps.setScene(new Scene(gd, 1200, 500));
        gd.getStylesheets().add(MainUI.class.getResource("main.css").toExternalForm());
        ps.show();
    }

    private void loadMap() {

        //textArea.setVisible(true);
        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file == null) {
            logView.setText(logView.getText() + "\nSomething had a sad. plz go away and try later.");
            return;
        }
        if (!file.getName().endsWith(".shp")) { // Checking magic numbers is for quiche eaters
            logView.setText(logView.getText() + "\nError! " + file.getName() + " is not a shapefile. Please try again.");
            return;
        } else {
            logView.setText(logView.getText() + "\nSuccess! Loaded " + file.getName() + ".");
            setRawMap(file);
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
            setMc(map);

        } catch (IOException e) {

        }

    }

    private void drawMap() throws IOException {
        if (getRawMap() == null){
            logView.setText(logView.getText()+"\nNo map loaded! Please load a map!");
        }
        FileDataStore store = FileDataStoreFinder.getDataStore(getRawMap());
        featureSource = store.getFeatureSource();
        SimpleFeatureCollection collection = featureSource.getFeatures();

        GeometryDescriptor geomDesc = featureSource.getSchema().getGeometryDescriptor();
        geometryAttributeName = geomDesc.getLocalName();

        SimpleFeatureIterator iter = collection.features();
        MapContent mapC = getMc();
        mapFrame = new JMapFrame(mapC);
        mapFrame.enableToolBar(true);
        mapFrame.enableStatusBar(true);
        mapFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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

        logView.setText(logView.getText() + "\nDraw complete.");
    }

    private File getRawMap() {
        return rawMap;
    }

    private void setRawMap(File rawMap) {
        this.rawMap = rawMap;
    }

    private MapContent getMc() {
        return mc;
    }

    private void setMc(MapContent mc) {
        this.mc = mc;
    }

    /*
     * Convenient constants for the type of feature geometry in the shapefile
     */
    private enum GeomType {
        POINT, LINE, POLYGON
    }
}
