import java.io.*;

public class Main {
  private static Main shell = new Main();
  private static World noWorld = new World();

  public static void main(String[] args) {
    //set argument values to variables
    Pars.cellType=Integer.parseInt(args[0]);
    Pars.deathR = Integer.parseInt(args[1]);
    Pars.catR = Integer.parseInt(args[2]);

    System.out.println("initializing...");
    noWorld.setCells();//define initial cell position and attributes

    System.setProperty("java.awt.headless", "true");//no head

    //set up output directories
    Pars.outFile="../results"+Pars.cellType+Pars.deathR+Pars.catR;
    new File(Pars.outFile).mkdir();
    new File(Pars.outFile+"/data").mkdir();
    new File(Pars.outFile+"/movie").mkdir();

    shell.start();
  }

  private void start(){
    int frameNum = 0;
    //if cells don't max out or go to zero, run until final time set in Pars.java
    while(noWorld.cells.size()<Pars.MAX_CELLS && noWorld.cells.size()>0 && frameNum<=Pars.timeRun){
      noWorld.frameUpdate(frameNum);

      //write data
      if(frameNum%Pars.movFrames==0){
        Data.writeDataPop(noWorld.cells.size(), frameNum, noWorld.numPro);
        Data.writeDataAveStd(frameNum);
      }

      //draw graphics
      if(frameNum%Pars.movFrames==0){
        noWorld.writeGFile(frameNum);
      }

      frameNum++;
    } 
  }


}