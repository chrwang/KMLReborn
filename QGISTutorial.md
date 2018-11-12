# A Tutorial on QGIS and Professor Komo

So you've successfully gerrymandered or un-gerrymandered your state... now you want to get some analysis!

## Getting out of Dave's Redistricting

First, check that your map follows all legal requirements for a district map.
All of the precincts should be assigned to a district, and all the districts should be continuous.
Export your map as a `.drf` file by going to `File > Save As` and choose `.drf`.
This is a "Dave's Redistricting File", which can be used to save your map.
 Later on, if you want to edit your districts, you can import this file into a clean Dave's Redistricting window. If things begin to break when exporting, open a new instance of Dave's Redistricting and import your saved file.

## Export to QGIS

From DRA, go to `File > Save VTD Info to CSV`, and save the resulting file somewhere. It contains information about how you drew your districts as well as vote and racial information for each precinct.

## Merging Your Districts with the Geography

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

You should now see the join in the `Joins` tab. You can optionally save this file as a KML, if you want to have the precincts displayed in Google Maps.

## Dissolving into Districts

Currently, our map shows the precinct-level geography.
We want the district-level geography.
We are going to use an operation called `Dissolve`, which will remove all of the boundaries except for the ones that divide districts.
Go to `Vector > Geoprocessing Tools > Dissolve`:
![Dissolve Location](https://i.imgur.com/WMTobDd.png)

For input layer, select your CSV-Shapefile merged file.
Uncheck `Dissolve all`, and go down to the `Unique ID Fields` area.
In the list on the left, find the district geometry, which should be named `<name_of_CSV>_Dis`, or `<name_of_CSV>_District`.
Single-click it, and then click the single rightwards arrow to move it into the `Selected` field.
Leave everything else as it is:
![Dissolve Example](https://i.imgur.com/pjTX0KZ.png)

`Note: This menu might look slightly different depending on which version of QGIS you have installed (this tutorial made for QGIS 2.18.*). `

Click the `Run` button, and wait for the operation to complete.
This could take anywhere from 2 seconds to a minute, depending on how fast your computer is.

This will create a new layer called `Dissolved`.
Uncheck all other layers in the sidebar, and verify that the operation completed successfully.
The map should look like your districts.
If the screen is blank, repeat the dissolve procedure again, and make sure you are selecting the right layers.

Right click your new `Dissolved` in the `Layers Panel`, and hit `Save as`. Save the layer as a shape (`.shp`) layer

## Getting into Professor Komo

Download [Professor Komo](https://drive.google.com/open?id=1vvlv61xvNgekhqzbha7X3jEpWXqSoXxn), named after our the glorious '18 Andrew Komo after he won 1st place in Siemens. Once you have downloaded the `.jar` file, run it. You should arrive at the following screen:

![Professor Komo Startup Screen](https://imgur.com/Df5dB8v)

Click the `Start` button and a menu should pop up to allow you to select your saved `Dissolved.shp` file from above. *Note: As above, **do not** move the shape file by itself out of the directory. You must have all 6(ish) files present for this to work.* 

After selected your `.shp` file, a new window should open, and the original window changes format as follows:

![Professor Komo Initial Map Screen](https://imgur.com/ULnqlOk)

You should notice the following 6 buttons on the map screen:

![Menu Bar](https://imgur.com/rKLul1C)

The second and third can be used to zoom in an our, but the only really relevant ones are the rightmost two. The first option centers your map, and just gives you a good view of the entire map (very helpful if you resize the map window). The second one is our ticket to benchmark calculation. After clicking the `Select` button, simply click on one of your districts. You should see something like the following:

![Professor Komo Final Metrics Screen](https://imgur.com/If2ULBj)

It should be self-explanatory, but the scores on the first window show the 5 metrics for the highlighted yellow district. Selecting another district will make that district yellow, and update the scores. Simply record the values for each of your districts and go analyze your data!
