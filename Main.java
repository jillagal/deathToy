import java.io.*;

public class Main {
  private static Main shell = new Main();
  private static World world = new World();

  public static void main(String[] args) {
    //set argument values to variables
    Pars.cellType=Integer.parseInt(args[0]);
    Pars.deathR = Integer.parseInt(args[1]);
    Pars.catR = Integer.parseInt(args[2]);

    shell.initialize();
    shell.start();
  }

  private void start(){
    int frameNum = 0;
    //if cells don't max out or go to zero, run until final time set in Pars.java
    while(world.cells.size()<Pars.MAX_CELLS && world.cells.size()>0 && frameNum<=Pars.timeRun){
      world.frameUpdate(frameNum);

      if(frameNum%Pars.movFrames==0){
        //write data
        world.writeDFile(frameNum);

        //draw graphics
        world.writeGFile(frameNum);
      }

      frameNum++;
    } 
  }

  private void initialize(){
    String strCT=(Pars.cellType==0)?"OPEN":(Pars.cellType==1)?"CONVEX":"CONCAVE";
    System.out.println(".............");
    System.out.println("Initializing simulation with "+strCT+" tradeoff");
    System.out.println("1 in "+Pars.deathR+" deaths per cell per minute");
    System.out.println("and "+Pars.catR+"% catastrophic deaths");
    System.out.println(".............");
    System.out.println("time(days)  # cells");
    System.setProperty("java.awt.headless", "true");//no head

    //set up output directories
    Pars.outFile="../results"+Pars.cellType+Pars.deathR+Pars.catR;
    new File(Pars.outFile).mkdir();
    new File(Pars.outFile+"/data").mkdir();
    new File(Pars.outFile+"/movie").mkdir();
  }


}