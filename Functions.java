import java.util.Random;

class Functions {
	private Random diceRoller = new Random();

	//sample from a bounded Gaussian distribution
	float gaussSample(float mean, float error, float min, float max){
		float gauss = (float) diceRoller.nextGaussian();
		float trait = error*gauss+mean;
		while(trait <= min|| trait>= max){
		  	gauss = (float) diceRoller.nextGaussian();
		  	trait = error*gauss+mean;
		}
		return trait;
	}

	//sample within a range
	int rangeSample(int min, int max){
        return diceRoller.nextInt(max-min+1)+min;
	}

	//inherit from parental phenotype according to trade-off bounds and drift rate
	float[] inheritDriftBounded(float prevD, float prevS, int fam, int drift){
		float[] inTraits = {0,0};//check divMin instances!!! and speMax

		float minD = Pars.divMin;
		float maxS = Pars.spMax;

		//find drift vales for IMT and speed
		float rand = rangeSample(-drift, drift)*Pars.divConv*Pars.epD;
		float rand2 = rangeSample(-drift, drift)*Pars.speedConv*Pars.epS;

		//add to parental values
		float newD = prevD+rand;
		float newS = prevS+rand2;
		if(fam==0){//open trade-off - just fit within bounds
			inTraits[0] = (newD>Pars.divMax) ? Pars.divMax : (newD<minD) ? minD : newD;
        	inTraits[1] = (newS>maxS) ? maxS : (newS<Pars.spMin) ? Pars.spMin : newS;
		}
		else{//convex (fam=1) and concave (fam=2) trade-offs, resample if out of bounds
			boolean checkFit = (fam==1) ?
				1>=(newD-Pars.divMax)*(newD-Pars.divMax)/((Pars.divMax-minD)*(Pars.divMax-minD))+(newS-Pars.spMin)*(newS-Pars.spMin)/((maxS-Pars.spMin)*(maxS-Pars.spMin)) :
				1<=(newD-Pars.divMin)*(newD-Pars.divMin)/((Pars.divMax-minD)*(Pars.divMax-minD))+(newS-Pars.spMax)*(newS-Pars.spMax)/((maxS-Pars.spMin)*(maxS-Pars.spMin));
			while(!checkFit){
				rand = rangeSample(-drift, drift)*Pars.divConv*Pars.epD;
				rand2 = rangeSample(-drift, drift)*Pars.speedConv*Pars.epS;
				newD = prevD+rand;
				newS = prevS+rand2;
				checkFit = (fam==1) ?
						1>=(newD-Pars.divMax)*(newD-Pars.divMax)/((Pars.divMax-minD)*(Pars.divMax-minD))+(newS-Pars.spMin)*(newS-Pars.spMin)/((maxS-Pars.spMin)*(maxS-Pars.spMin)) :
						1<=(newD-Pars.divMin)*(newD-Pars.divMin)/((Pars.divMax-minD)*(Pars.divMax-minD))+(newS-Pars.spMax)*(newS-Pars.spMax)/((maxS-Pars.spMin)*(maxS-Pars.spMin));
			}
			inTraits[0] = (newD>Pars.divMax) ? Pars.divMax : (newD<minD) ? minD : newD;
        	inTraits[1] = (newS>maxS) ? maxS : (newS<Pars.spMin) ? Pars.spMin : newS;
		}

        return inTraits;
	}

}