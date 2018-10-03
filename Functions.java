import java.util.Random;
import java.io.*;

class Functions {
	static private Random diceRoller = new Random();

	//write integer vector to files
    static void writeIntVector(String strF, int[] vector){
        try{
            BufferedWriter fout0 = new BufferedWriter(new FileWriter(strF,true));
            for (int aVector : vector) {
                fout0.write(aVector + " ");
            }
            fout0.write("\n");
            fout0.close();
        }
        catch(IOException e){
            System.out.println("There was a problem"+e);
        }
    }

    //write float vector to files
    static void writeFloatVector(String strF, float[] vector){
        try{
            BufferedWriter fout0 = new BufferedWriter(new FileWriter(strF,true));
            for (float aVector : vector) {
                fout0.write(aVector + " ");
            }
            fout0.write("\n");
            fout0.close();
        }
        catch(IOException e){
            System.out.println("There was a problem"+e);
        }
    }

	//sample from a bounded Gaussian distribution
	static float gaussSample(float mean, float error, float min, float max){
		float gauss = (float) diceRoller.nextGaussian();
		float trait = error*gauss+mean;
		while(trait <= min|| trait>= max){
		  	gauss = (float) diceRoller.nextGaussian();
		  	trait = error*gauss+mean;
		}
		return trait;
	}

	//sample within a range
	static int rangeSample(int min, int max){
        return diceRoller.nextInt(max-min+1)+min;
	}

	//inherit from parental phenotype according to trade-off bounds and drift rate
	static float[] inheritDriftBounded(float prevD, float prevS, int fam, int drift){
		float[] inTraits = {0,0};

		float rangeD=Pars.divMax-Pars.divMin;
		float rangeS=Pars.spMax-Pars.spMin;

		//find drift vales for IMT and speed
		float rand = rangeSample(-drift, drift)*Pars.divConv*Pars.epD;
		float rand2 = rangeSample(-drift, drift)*Pars.speedConv*Pars.epS;

		//add to parental values
		float newD = prevD+rand;
		float newS = prevS+rand2;
		if(fam==0){//open trade-off - just fit within bounds
			inTraits[0] = (newD>Pars.divMax) ? Pars.divMax : (newD<Pars.divMin) ? Pars.divMin : newD;
        	inTraits[1] = (newS>Pars.spMax) ? Pars.spMax : (newS<Pars.spMin) ? Pars.spMin : newS;
		}
		else{//convex (fam=1) and concave (fam=2) trade-offs, resample if out of bounds
			boolean checkFit = (fam==1) ?
				1>=(newD-Pars.divMax)*(newD-Pars.divMax)/(rangeD*rangeD)+(newS-Pars.spMin)*(newS-Pars.spMin)/(rangeS*rangeS):
				1<=(newD-Pars.divMin)*(newD-Pars.divMin)/(rangeD*rangeD)+(newS-Pars.spMax)*(newS-Pars.spMax)/(rangeS*rangeS);
			while(!checkFit){
				rand = rangeSample(-drift, drift)*Pars.divConv*Pars.epD;
				rand2 = rangeSample(-drift, drift)*Pars.speedConv*Pars.epS;
				newD = prevD+rand;
				newS = prevS+rand2;
				checkFit = (fam==1) ?
						1>=(newD-Pars.divMax)*(newD-Pars.divMax)/(rangeD*rangeD)+(newS-Pars.spMin)*(newS-Pars.spMin)/(rangeS*rangeS):
						1<=(newD-Pars.divMin)*(newD-Pars.divMin)/(rangeD*rangeD)+(newS-Pars.spMax)*(newS-Pars.spMax)/(rangeS*rangeS);
			}
			inTraits[0] = (newD>Pars.divMax) ? Pars.divMax : (newD<Pars.divMin) ? Pars.divMin : newD;
        	inTraits[1] = (newS>Pars.spMax) ? Pars.spMax : (newS<Pars.spMin) ? Pars.spMin : newS;
		}

        return inTraits;
	}

}