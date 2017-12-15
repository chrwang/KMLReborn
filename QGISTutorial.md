# A Tutorial on Convex Hull and QGIS

So you've successfully gerrymandered or un-gerrymandered your state... now you want to get some analysis!

## Getting out of Dave's Redistricting

First, check that your map follows all legal requirements for a district map.
All of the precincts should be assigned to a district, and all the districts should be continuous.
Export your map as a `.drf` file by going to `File > Save As` and choose `.drf`.
This is a "Dave's Redistricting File", which can be used to save your map.
 Later on, if you want to edit your districts, you can import this file into a clean Dave's Redistricting window. If things begin to break when exporting, open a new instance of Dave's Redistricting and import your saved file.

## Calculating Convex Hull in QGIS

### Export to QGIS

From DRA, go to `File > Save VTD Info to CSV`, and save the resulting file somewhere. It contains information about how you drew your districts as well as vote and racial information for each precinct.

### Merging Your Districts with the Geography

In order to calculate metrics like convex hull, you will have to combine your districts from DRA with the actual geography of your state.
You will need a shapefile for your state, which can be obtained from this Google Drive [file](https://drive.google.com/folderview?id=0Bz_uFI8VY7xLZV9tTEFvc0hMdTg&usp=sharing).
After unzipping the directory, *do not* move any of the state files in or out of the directory, unless you do them as a whole.
If you move, say the `North Carolina.shp` file out of 2010 DRA Shapefiles, then it will not be able to find any of the other files, and your shapefile will not display properly.

Now, open a new project in QGIS. You should get a window looking something like this:
![Blank QGIS Image](https://i.imgur.com/2dHeMKX.png)

Open your state shapefile by pressing <kbd>⌃</kbd> <kbd>⇧</kbd> <kbd>V</kbd> or <kbd>⌘</kbd> <kbd>⇧</kbd> <kbd>V</kbd>, or by going to `Layers > Add Layer > Add Vector Layer`.
Choose `File` and `UTF-8` under Source, and select the shapefile in the `Dataset` field.
Repeat the process to open your DRA-exported CSV.
In the bottom left corner, you should now see both the state shapefile and the `csv`.
Additionally, you should see your state in the main window.

Double-click the name of the state shapefile in that box, or right click it and choose `Properties`.
Go to the `Joins` tab.
Click the green plus button in the bottom left corner.
You should see three dropdown menus that you will have to select things for.
The first one is the source file, which is the CSV file.
The second is the join field, which should be `GeoId<number>` or something named similarly.
The target field should be `GeoID<number>`. The other options do not matter. Click `OK`.

You should now see a new layer in the layer sidebar on the left. You can optionally save this file as a KML, if you want to have the precincts displayed in Google Maps.

### Dissolving into Districts

Currently, our map shows the precinct-level geography.
We want the district-level geography.
We are going to use an operation called `Dissolve`, which will remove all of the boundaries except for the ones that divide districts.
Go to `Vector > Geoprocessing Tools > Dissolve`.
For input layer, select your CSV-Shapefile merged file.
Uncheck `Dissolve all`, and go down to the `Unique ID Fields` area.
In the list on the left, find the district geometry, which should be named `<name_of_CSV>_Dis`.
Single-click it, and then click the single rightwards arrow to move it into the `Selected` field.
Leave everything else as it is.
Click the `Run` button, and wait for the operation to complete.
This could take anywhere from 2 seconds to a minute, depending on how fast your computer is.

This will create a new layer called `Dissolved`.
Uncheck all other layers in the sidebar, and verify that the operation completed successfully.
The map should look like your districts.
If the screen is blank, repeat the dissolve procedure again, and make sure you are selecting the right layers.

### Calculating Convex Hull
We first need to name each of the districts that have just been created.
Right click the `Dissolved` layer, and select `Open Attribute Table`.
You should see a blank column called `Name` or something similar.
The number of rows should be equal to the number of districts you have, and the nth row corresponds to the nth district in DRA.
In the Attribute Table, click the Pencil icon
Put some text in that field for each district.
You can put whatever you want, but each box should be unique.
We need this field in order for QGIS to do each district individually.

Now, go to `Vector > Geoprocessing Tools > Convex Hull`.
A new window will open.
Choose your `Dissolved` layer as the `Input Layer`.
For `Field`, choose the `Name` field that we created earlier.
For `Method`, choose `Create convex hulls based on field`.
Click run, and wait for the operation to finish.

Once this completes, you should see a convex hull created for each district in your state.
Right click the `Convex Hull` in the `Layers Panel`, and click `Save as`.
Save the layer as a KML file.

Go to [Google MyMaps](https://www.google.com/mymaps/) and create a new map.
In the menu on the left, click `Add layer`, which will add an `Untitled Layer` in the menu.
Name the layer whatever you want.
Under the layer name, click `Import`, and add your KML file.
Once the map loads, you should see the convex hulls on the map.
If you click a hull, you'll see values for both the perimeter and the area. 
