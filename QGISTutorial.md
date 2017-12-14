# A Tutorial on Convex Hull and QGIS

So you've succesfully gerrymandered or un-gerrymandered your state... now you want to get some analysis!

## Getting out of Dave's Redistricting

First, check that your map folllows all legal requirements for a district map. 
All of the precincts should be assigned to a district, and all the districts should be continuous. 
Export your map as a `.drf` file by going to `File > Save As` and choose `.drf`. 
This is a "Dave's Redistricting File", which can be used to save your map.
 Later on, if you want to edit your districts, you can import this file into a clean Dave's Redistricting window. If things begin to break when exporting, open a new instance of Dav'es Redistricting and import your saved file. 

## Calculating Convex Hull in QGIS

### Export to QGIS

From DRA, go to `File > Save VTD Info to CSV`, and save the resulting file somewhere. It contains information about how you drew your districts as well as vote and racial information for each precinct. 

### Merging Your Districts with the Geography

In order to calculate metrics like convex hull, you will have to combine your districts from DRA with the actual geography of your state. 
You will need a shapefile for your state, which can be obtained from this Google Drive [file](https://drive.google.com/folderview?id=0Bz_uFI8VY7xLZV9tTEFvc0hMdTg&usp=sharing).
After unzipping the directory, *do not* move any of the state files in or out of the directory, unless you do them as a whole. 
If you move, say the `North Carolina.shp` file out of 2010 DRA Shapefiles, then it will not be able to find any of the other files, and your shapefile will not display properly. 
 
Now, open a new project in QGIS. You should get a window looking something like this:

Open your state shapefile by pressing Ctrl-Shift-V or Cmd-Shift-V, or by going to `Layers > Add Layer > Add Vector Layer`. 
Choose `File` and `UTF-8` under Source, and select the shapefile in the `Dataset` field. 
Repeat the process to open your DRA-ecported CSV. 
In the bottom left corner, you should now see both the state shapefile and the csv. 
Additionally, you should see your state in the main window. 

Double-click the name of the state shapefile in that box, or right click it and choose `Properties`. 
Go to the `Joins` tab. 
Click the green plus button in the bottom left corner. 
You should see three dropdown menus that you'll have to select things for. 
The first one is the source file, which is the CSV file. 
The second is the join field, which should be `GeoId<number>` or something named similarly. 
The target field should be `GeoID<number>`. The other options don't matter. Click `OK`.

You should now see a new layer in the layer sidebar on the left. You can optionally save this file as a KML, if you want to have the precints displayed in Google Maps. 

### Dissolving into Districts

Currently, our map shows the precinct-level geography. 
We want the district-level geography. 
We're going to use an operation called `Dissolve`, which will remove all of the boundaries except for the ones that divide districts. 
Go to `Vector > Geoprocessing Tools > Dissolve`. 
