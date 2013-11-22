SimpleSplitPS3 (formerly MiniSplit)
===================================

Overview
--------

SimpleSplitPS3 is a tool for splitting large PS3 game files ( > 4294967295 bytes ) into .666## files so you can transfer them to a FAT32 formatted device (e.g. an external hard drive). It can also join .666## files back into their original file.

**Java Runtime Environment 7 (1.7) is required for SimpleSplitPS3 to run.**

Usage
-----

1. Run SimpleSplitPS3 by double clicking on SimpleSplitPS3.jar.
2. Select Split or Join and check/uncheck each checkbox depending on your preferences.
3. Press Search For Files to pick a folder to check recursively for large files if you want to split or .666## files if you want to join.
4. If you checked the Change checkbox, choose a destination folder for your split/joined files.
5. Wait for SimpleSplitPS3 to finish splitting/joining files.

Features
--------

* Split large files into 4294967295 byte pieces (the max file size for FAT32).
* Join .666## files back into their original file.
* Delete source files after splitting or joining files.
* Rename game folders by adding an _ to the game folder once large files have been split or removing an _ from the game folder if split files have been joined.
* Change the destination directory. Useful if you want to keep the split/joined files separate from the source files or if you want to speed up the splitting/joining process by reading the source files from one storage device and writing the resulting files to another storage device.
* Recreate the folder structure of the source folder inside your chosen destination folder. Only applies when you've checked the Change checkbox. Lets you easily see where the newly split/joined files are supposed to go in the original game folder.
* Display per file and total progress with progress bars.
* Split/Join files from multiple game folders at one time.

Todo
----

* Comment code.

Screenshots
-----------

![Screenshot-1](/screenshots/Screenshot-1.png?raw=true)