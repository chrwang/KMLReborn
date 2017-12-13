# A Tutorial on Convex Hull and QGIS

So you've succesfully gerrymandered or un-gerrymandered your state... now you want to get some analysis!

## Getting out of Dav'es Redistricting

First, check that your map folllows all legal requirements for a district map. All of the precincts should be assigned to a district, and all the districts should be continuous. Export your map as a `.drf` file by going to `File > Save As` and choose `.drf`. This is a "Dave's Redistricting File", which can be used to save your map. Later on, if you want to edit your districts, you can import this file into a clean Dave's Redistricting window. If things begin to break when exporting, open a new instance of Dav'es Redistricting and import your saved file. 

## Export to QGIS

From DRA, go to `File > Save VTD Info to CSV`, and save the resulting file somewhere. It contains information about how you drew your districts as well as vote and racial information for each precinct. 
