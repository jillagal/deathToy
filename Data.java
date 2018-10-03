import java.io.*;

class Data{
   	static float divAve, speedAve, divStd, spStd;
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
	static void stdMore(int numCells){
		divStd = (float) (Math.sqrt(tempDS/numCells));
	    spStd = (float) (Math.sqrt(tempSS/numCells));
	}


}