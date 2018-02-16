import java.io.*;

class Data{
   	private static float divAve, speedAve, divStd, spStd;
   	private static float tempDS, tempSS;
   	static int totProlif,totQui;

	static void initialize(){
		totProlif = totQui = 0;
	   	divAve = speedAve = divStd = spStd = 0;
	}

	/**************
	* CALCULATIONS
	**************/

	//adds up IMTs and speeds for averaging
	static void addAverages(float tDiv, float sTemp){
		divAve += tDiv;
		speedAve += sTemp;
	}

	//finds average IMT and speed by dividing by totals
	static void findTotals(int currentNumCells){
		divAve = divAve/(currentNumCells);
		speedAve = speedAve/currentNumCells;
	}

	//adds up contributions to standard deviations
	static void findStdDevs(float tDiv, float sp){
	    tempDS += (tDiv/(Pars.divConv)-divAve)*(tDiv/(Pars.divConv)-divAve);
		tempSS += (sp/Pars.speedConv-speedAve)*(sp/Pars.speedConv-speedAve);
	}

	//finalizes standard deviation calculation
	static void stdMore(int numCells){//change name?
		divStd = (float) (Math.sqrt(tempDS/numCells));
	    spStd = (float) (Math.sqrt(tempSS/numCells));
	}

	/***************
	* WRITE TO FILES
	****************/

	//writes time in hours, total number of cells, and number of cells proliferating
	static void writeDataPop(int numCells, int frameNum, int numP){
		try{
			BufferedWriter fout = new BufferedWriter(new FileWriter(Pars.outFile+"/data/popTime.txt",true));
			int getittime=frameNum*Pars.frameTime/60;
			fout.write(""+getittime+"	"+numCells+"   "+numP+"   "+"\n");
			fout.close();
		}
		catch(IOException e){
			System.out.println("There was a problem"+e);
		}
	}

	//writes time in hours, average intermitotic time in hours, and std dev of IMT in hours
    //writes time in hours, average speed in microns/hour, and std dev of speed in microns/hour
	static void writeDataAveStd(int frameNum){
		try{
			BufferedWriter fout0 = new BufferedWriter(new FileWriter(Pars.outFile+"/data/divAveStd.txt",true));
			BufferedWriter fout1 = new BufferedWriter(new FileWriter(Pars.outFile+"/data/spAveStd.txt",true));
            int getittime=frameNum*Pars.frameTime/60;
			fout0.write(""+getittime+"	"+divAve+"  "+divStd+"\n");
			fout1.write(""+getittime+"	"+speedAve+"  "+spStd+"\n");
			fout0.close();
			fout1.close();
		}
		catch(IOException e){
			System.out.println("There was a problem"+e);
		}
	}


}