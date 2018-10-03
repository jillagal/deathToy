# deathToy

This is the code used to produce the results in ["The dynamic tumor ecosystem: how cell turnover and trade-offs affect cancer evolution"](https://www.biorxiv.org/content/early/2018/02/23/270900) and the [interactive website](http://www.imomodelview.com/Publications/Gallaher/Death_Toy/Gallaher_et_al_2018.html) with representative movies from this code. In this work, we ask: How do proliferation-migration tradeoffs affect phenotypic evolution? In this off-lattice agent-based simulation the user can vary proliferation-migration trade-offs, death rate, and whether death occurs in response to demographic or environmental stochasticity. The output is a movie folder (spatial layout of cells over time) and a data folder (population number, averages and standard deviations of proliferation and migration rates over time).

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites
You will need Java to run this program. It is known to work with:

```
Java(TM) SE Runtime Environment (build 1.7.0_79-b15)
Command line or other IDE software (we use IntelliJ IDEA)
```

### Install & Run

First download a ZIP file of the repository contents to your local machine. Double-click to unZIP. 

We will show here two ways to get the program up and running.

#### 1) Command line

Go to the directory folder from the command line:

```
cd local-directory/deathToy-master/
```

Run the shell script*:

```
./exe1.sh
```

*You may need to change the file permissions before running the script in order to make it executable:

```
chmod +x exe1.sh
```

#### 2) IntelliJ IDEA

From IntelliJ, open the program

```
Click Open and select the local deathToy-master folder.
```

Ensure that the BashSupport plugin is installed. 

```
Click on the IntelliJ IDEA tab and select Preferences.
Go to the Plugins tab and click on Plugins from the menu on the left.
Click the Browse repositories button toward the bottom of the panel.
Type BashSupport into the search bar. 
Click INSTALL, then click Restart IntelliJ IDEA.
```

Set up the bash script to run the program.

```
Click the Run tab, and click Edit Configurations. 
Click the + sign, and select Bash.
Fill out the following run configurations.
  Name: any name
  Script: find and select the local exe1.sh file
  Interpreter path: you may need edit this to find your Java interpreter, we used /usr/bin/env
  Interpreter options: bash
  Working directory: browse to and select local-directory/deathToy-master/
Click OK.
```

It should now be set up, and will run when you click the green arrow.

### Changing The Input Variables
The trade-off type, the death rate, and the catastrophic rate can be changed to reproduce the results from the paper.

```
Edit the exe1.sh file. The numbers used in the paper are given.
```
## Basic Code Structure
This section describes the general code structure to get you oriented, and each class file is expanded upon below.

This code uses a bash script (exe1.sh) to compile and execute the main java program (Main.java). The Main class initializes the program and creates an instance of the World class where the arrayList of cells (instances of the Cell class) is stored, the frameUpdate is defined, and the interactions between cells occur. The Cell class defines attributes and functions for a cell. The Pars class defines the parameters used in the simulation. The Data class contains variables and calculations for the data output, and the Draw class contains functions needed to produce the graphical output. The Functions class contains several generic functions used in the code.

### exe1.sh
This is the bash script that deletes any old directories from previous runs, makes new directories, compiles the java files, and passes the arguments to run the java code.

### Main.java
This is the main source file that sets the variables passed from the bash script, and initializes and runs the simulation. The output directories are created here, and the frame update is called with conditions for ending the simulation. The functions to write the data and graphics are also called from this class.

### Pars.java
This file contains the parameters for the simulation.

### World.java
This file contains most of the functions for updating the simulation. 
#### World()
The constructor defines the initial cell position and attributes.
#### frameUpdate()
This function is called in the Main class and proceeds as follows:
1) At the top of the frame, we remove dead cells, countdown the timers set for cells that are set to die, and check for catastrophic deaths.
2) Then, assignGrid() is called that records information about cells within a gridded structure to be accessed later. For each nieghborhood, a list of cell indexes and the total cell population is recorded.
3) The cell loop is called that goes through all cells checking for random death, checking for quiesence due to lack of space from neighbors, and if not quiescent, will divide if gone through the cell cycle (xDiv=1), or move and update if not (xDiv<1).
4) Metrics are updated and recorded periodically.
#### checkNeighbors()
This function finds only the nearest neighbors of a cell by checking its neighborhood and the surrounding neighborhoods for cells and records their ID, their distance away from the cell, and the angle at which it resides from the cell. If the cell has neighbors, it calls the getTheta() function in the Cell class to find an empty angle to divide into or returns 999 if there are no empty angles where a cell can fit without overlap.
#### division()
This function uses the angle found from CheckNeighbors() to create a new cell at the angle given from the parental cell. It also assigns new trait values and resets other variables using the divNewParams() function found in the Cell class.
#### move()
This function checks all neighbors from its neighborhood and the surrounding neighborhoods for collisions defined by the function intersect() defined in the Cell class. If a collision occurs, the persistence and angle of movement are reset.

### Cell.java
This file contains specific functions and attributes for the Cell class.
#### intersect()
This function checks whether two cells will intersect paths and defines a collision if they are within a diameter apart.
#### hitWall()
This function defines a similar interaction to intersect() except with a cell to the bounds of the circular domain.
#### getTheta()
This function records open angles into which a cell can divide in finalBank. It starts with 360 angles available and excludes angles due to the presence of neighboring cells using the popExs() function and setting those angles to 999 depending on its angle and distance away. Any angle not excluded will be added to the list finalBank, and the angle for a new cell will be randomly chosen from this list.
#### updatePers()
Persistence times are sampled from a Gaussian distribution. At each frame the counter counts down until the persistence time is reached. When time is up, the persistence time and angle for movement are reset.
#### updatePos()
New positions due to movement are found, the cell cycle is updated, and the total time in the cell cycle is incremented.
#### setCell()
This sets the initial attributes for the starting cell.
#### divNewParams()
This function resets attributes for a newly divided cell as well as calling the function inheritDriftBounded() in the Functions class that defines the inherited traits for each given tradeoff boundary.

### Data.java
This file contains variables and functions for calculating averages and standard deviations for traits output into the data folder.

### Draw.java
The functions for creating the graphical output in the movie folder are contained here. This includes the color map for the cells to display their trait combination.

### Functions.java
Several generic functions are stored here for writing to files and sampling from distributions. The inheritance function for each of the tradeoffs is also here.
#### inheritDriftBounded()
This function defines new trait values for newly divided cells. It first samples new trait values within range of the previous trait values, then checks whether the new trait values are within the bounds of the tradeoff. If not, it resamples until the new values lie within the bounds.

### deathToyClean.iml
This is a module file that saves settings from IntelliJ IDEA.

## Contributions & Feedback
Please contact me if you have any suggestions for improvement.

## Authors
Jill Gallaher - Code, Investigation, Analysis, & Visualization
Joel Brown and Alexander R. A. Anderson - Conceptualization, Methodology, Writing, & Editing
