import java.util.*;
import java.io.*;
import javax.imageio.*;
import java.awt.*;
import java.awt.image.BufferedImage; 

class World{
	private Random diceRoller = new Random();

    ArrayList<Cell> cells = new ArrayList<>();//cell list

	//Neighborhood grid
	private int nN = 60;//number of lattice points
	private int sN = Pars.sizeW/nN;//grid size in pixels
	private int maxO = 8*80;//number of cells in each lattice point
	private int[][][] gridN = new int[nN][nN][maxO];//stores ids of cells in lattice points
	private int[][] gridPop = new int[nN][nN];//stores number of cells in lattice points

	//Neighbors lists
    private int maxN=400;//max # of neighbors
	private int[] neighborList = new int[maxN];//list of neighbors
	private float[] neighborDist = new float[maxN];//distance to each neighbor
	private float[] neighborAng = new float[maxN];//angle to each neighbor

	//tracking & recording variables for output
    private int catTimer=0;//tracks time to next catastrophe
	private int numCD=225;//tracks number of cells that die in catastrophe - initial value assumes full of cells
	int numPro;//tracks number of cells actively proliferating
    private int neighborNum=0; //tracks number of neighbors
    private int newAngle;//new angle after division
    private float[] traitValues = new float[Pars.MAX_CELLS];//stores traits to record

    /*****************
     * CONSTRUCTOR:
     ****************/
    World() {
        //define initial cell position and attributes
        Cell cell = new Cell(Pars.sizeW/2, Pars.sizeH/2,Pars.cellType);
        cells.add(cell);
        cell.setCell();
    }

    /*******************
     * frame update
     *******************/

    void frameUpdate(int frameNum) {
        //if death rate is not 0, check for removal and for catastrophe
        if(Pars.deathR!=0){
            this.checkDeath();
            this.catast();
        }

        this.assignGrid();//populate grid with cells to check density

        for(int i = cells.size(); --i >= 0; ){//cell loop
            Cell cell = cells.get(i);

            setRandomDeath(cell);//random death

            newAngle=checkNeighbors(cell,i);//get new angle for division or go quiescent if no angles left (999)

            //quiescence if cell cycle is less than 90% & (either cell hits wall, space is full, or cell marked for death)
            if (cell.xDiv<0.9 && (cell.hitWall() || newAngle == 999 || cell.deathMeter<1500)){
                cell.quiResp();
            }
            else{
                cell.quiescence = false;
                cell.activate();//reactivate proliferation and migration in case previously quiescent

                if(cell.xDiv>=1){//cycle time is reached
                    division(cell,newAngle);//cell divides
                }
                else{
                    move(cell);//cell moves
                    cell.updatePers();//update persistence
                    cell.updatePos();//update position in cell cycle
                }
            } //end of pro
        }//end of cell loop

        this.collectDataAlways();
        if(frameNum%Pars.movFrames==0){
            this.collectDataSometimes();
        }
    }

	/***************
	* death functions
	***************/

    private void checkDeath(){
        for (int i = cells.size()-1; i>=0; i--) {
            Cell cell = cells.get(i);
            if(cell.deathMeter<1500){//by default set to 1500, if set for death, goes below - random
                cell.deathMeter-=1;
            }
            if(cell.deathMeter<=0){//remove at end of timer
                cells.remove(i);
            }
        }
    }

    private void catast(){
		if(catTimer>0){
			catTimer-=1;//count down to catastrophe
		}

		//if previous catastrophe had less deaths than necessary to meet death rate set to have a catastrophe
        //otherwise set a probability
		int thisDel = (numCD*Pars.deathR<(60*5*Pars.catR/100.f*cells.size())) ? 1
				: (int) (numCD*Pars.deathR/(60*5*Pars.catR/100.f*cells.size()));
		int dRR = (numCD==0) ? 0
				: (Pars.catR>0) ? diceRoller.nextInt(thisDel) : 0;

		//don't have a catastrophe before 400 cells
		if(dRR==0 && Pars.catR>0 && cells.size()>400 && catTimer==0){
			int inDist = (int) (2*Pars.sizeW/3+1000*Pars.micMToPix);
			//find cat location
			float catX=diceRoller.nextInt(inDist)+Pars.sizeW/6-500*Pars.micMToPix;
			float catY=diceRoller.nextInt(inDist)+Pars.sizeH/6-500*Pars.micMToPix;
			int root = (int) (Math.sqrt((catX-Pars.sizeW/2)*(catX-Pars.sizeW/2)+(catY-Pars.sizeH/2)*(catY-Pars.sizeH/2)));
			while(root>Pars.sizeW/3+300*Pars.micMToPix){
				catX=diceRoller.nextInt(inDist)+Pars.sizeW/6-500*Pars.micMToPix;
				catY=diceRoller.nextInt(inDist)+Pars.sizeH/6-500*Pars.micMToPix;
				root = (int) (Math.sqrt((catX-Pars.sizeW/2)*(catX-Pars.sizeW/2)+(catY-Pars.sizeH/2)*(catY-Pars.sizeH/2)));
			}

			numCD=0;//reset cell death count
			for (int i = cells.size()-1; i>=0; i--) {
	   			Cell cell = cells.get(i);
	   			int rootSmall = (int) (Math.sqrt((catX-cell.x)*(catX-cell.x)+(catY-cell.y)*(catY-cell.y)));
	   			if(rootSmall<250.f*Pars.micMToPix){//check if cell is in 500 micron diameter region
	   				cell.deathMeter=60*(6+diceRoller.nextInt(9));//apoptosis to take 6-15 hours - random
	   				numCD++;//count deaths
	   			}
	   		}
	   		catTimer=60*5;//wait at least 5 hours between catastrophes
        }
	}

	void setRandomDeath(Cell cell){
        if(Pars.deathR!=0 && Pars.catR<100.f){
            int dR = diceRoller.nextInt((int) (Pars.deathR/(1.f-Pars.catR/100.f)));
            if(dR==0){
                cell.deathMeter=60*(6+diceRoller.nextInt(9));//apoptosis to take 6-15 hours - random
            }
        }
    }


	/*************
	*Neighborhoods
	***********/

    private void assignGrid(){
        //reset grid
        for(int i=0;i<nN;i++){
            for(int j=0;j<nN;j++){
                gridPop[i][j]=0;
                for(int k=0;k<maxO;k++){
                    gridN[i][j][k]=-1;//default no cell value
                }
            }
        }
        for(int i = cells.size(); --i >= 0; ){
            Cell cell = cells.get(i);
            int gIndX = Math.round(cell.x/sN);
            int gIndY = Math.round(cell.y/sN);
            int gP = gridPop[gIndX][gIndY];
            gridN[gIndX][gIndY][gP]=i;//record index
            gridPop[gIndX][gIndY]++;//update number of cells in grid
        }
    }

    int checkNeighbors(Cell cell, int i){
        //reset neighbors
        neighborNum=0;
        for(int n=0;n<maxN;n++){
            neighborList[n]=0;
            neighborDist[n]=0;
            neighborAng[n]=0;
        }

        //find cell grid point
        int cellIndX = Math.round(cell.x/sN);
        int cellIndY = Math.round(cell.y/sN);
        cellIndX = (cellIndX>=nN) ? nN-1 : cellIndX;
        cellIndY = (cellIndY>=nN) ? nN-1 : cellIndY;
        //lists neighboring grid points
        Integer nXT[]={cellIndX,cellIndX,cellIndX,cellIndX+1,cellIndX+1,cellIndX+1,cellIndX-1,cellIndX-1,cellIndX-1};
        Integer nYT[]={cellIndY,cellIndY-1,cellIndY+1,cellIndY,cellIndY-1,cellIndY+1,cellIndY,cellIndY+1,cellIndY-1};

        //checks current grid and neighboring grids for neighbors
        for (int j=0;j<9;j++){
            int tBx=nXT[j];
            int tBy=nYT[j];
            for (int k=0;k<gridPop[tBx][tBy];k++){

                if(tBx>=0 && tBy>=0 && tBx<nN && tBy<nN){
                    int eI=gridN[tBx][tBy][k];
                    if(eI!=i){//current cell is not its own neighbor
                        Cell cellOther = cells.get(eI);
                        float xDist = cell.x-cellOther.x;
                        float yDist = cell.y-cellOther.y;
                        float rDist = (float) (Math.sqrt(xDist*xDist+yDist*yDist));
                        if(rDist<=4*Pars.effRad){//only get neighbors within 4 cell radii
                            neighborList[neighborNum]=eI;//find nieghbor id
                            neighborDist[neighborNum]=rDist;//find distance to neighbor
                            float theta = (float) (Math.atan2(-yDist, xDist)+1.f*Math.PI);//find neighbor angle
                            neighborAng[neighborNum]= (theta<0) ? (float) (theta+Math.PI+Math.PI) : theta;
                            neighborNum=neighborNum+1;//count neighbors
                        }
                    }
                }

            }
        }
        newAngle = diceRoller.nextInt(360);//if no neighbors, random angle chosen
        if(neighborNum>0){
            newAngle = cell.getTheta(neighborDist, neighborAng, neighborNum);//get an open angle to divide in
        }
        return newAngle;
    }

    /******************
    * Move and Divide
     * ***************/

    void division(Cell cell, int findAngle){//divide into an open space
        float angRad = (float) Math.toRadians(findAngle);

        float xPos =(float) (cell.x+2*Pars.effRad*(Math.cos(angRad)));
        float yPos = (float) (cell.y+2*Pars.effRad*(-Math.sin(angRad)));
        Cell child = new Cell(xPos,yPos,cell.family);
        cells.add(child);
        cell.divNewParams(findAngle, 0,cell.prevDiv,cell.prevSp);
        child.divNewParams(findAngle, 1,cell.prevDiv,cell.prevSp);
    }

    void move(Cell cell){
        // Check collision between neighboring cells
        for (int j=0;j<neighborNum;j++){
            Cell cellOther = cells.get(neighborList[j]);
            boolean collision = cell.intersect(cellOther.x, cellOther.y, cellOther.speedX, cellOther.speedY);
            if(collision){//if cell contacts another, reset persistence, angle, and speed for both cells
                cell.walkTime = (int) (Functions.gaussSample(Pars.persMean, Pars.persError, Pars.persMin, Pars.persMax));
                cell.angleInDegree = diceRoller.nextInt(360);
                cell.setSpeedXY();

                cellOther.walkTime = (int) (Functions.gaussSample(Pars.persMean, Pars.persError, Pars.persMin, Pars.persMax));
                cellOther.angleInDegree = diceRoller.nextInt(360);
                cellOther.setSpeedXY();
            }
        }
    }

    /******************
     * Data & output
     ******************/

    //data collected at every frame
    private void collectDataAlways(){
		Data.initialize();
		numPro = 0;

		for (int i = cells.size(); --i >= 0; ) {
			Cell cell = cells.get(i);
			numPro+=(!cell.quiescence) ? 1 : 0;//count proliferating cells

			float tempD = (1.f/Pars.divConv*cell.prevDiv);
			tempD=Pars.numBins*(tempD-Pars.dMin)/(Pars.dMax-Pars.dMin);
			int tempDa =(tempD>Pars.numBins) ? Pars.numBins-1 : (tempD<0) ? 0 : (int) tempD;

			float tempS = cell.prevSp/Pars.speedConv;//getSpeed()
			tempS = Pars.numBins*(tempS-Pars.sMin)/(Pars.sMax-Pars.sMin);
			int tempSa = (tempS>Pars.numBins) ? Pars.numBins-1 : (tempS<0) ? 0 : (int) tempS;

			Data.addAverages(tempDa*(Pars.dMax-Pars.dMin)/Pars.numBins+Pars.dMin,tempSa*(Pars.sMax-Pars.sMin)/Pars.numBins+Pars.sMin);
		}
		Data.findTotals(cells.size());

	}

	//data collected less often - to be written to files
	private void collectDataSometimes(){
		for (int i = cells.size(); --i >= 0; ) {
			Cell cell = cells.get(i);
			numPro+=(!cell.quiescence) ? 1 : 0;
		}

		for (int i = cells.size(); --i >=0; ){
			Cell cell = cells.get(i);
	    	Data.findStdDevs(cell.prevDiv, cell.prevSp);
	    }
	    Data.stdMore(cells.size());

	}

    //graphics
	void writeGFile(int frameNum){
        int fNN=frameNum/60;

        BufferedImage bi1=drawIt(1,frameNum);
        File f1 = new File(Pars.outFile+"/movie/"+fNN+".gif");
        try {ImageIO.write(bi1, "gif", f1);}
        catch (IOException ex) {ex.printStackTrace();}
    }

    void writeDFile(int frameNum){
        int getTime=frameNum*Pars.frameTime/60;
        System.out.println(getTime/24+" "+cells.size());

        String str = Pars.outFile+"/data/popTime.txt";
        int[] vectorI = {getTime, cells.size(), numPro};
        Functions.writeIntVector(str, vectorI);

        str = Pars.outFile+"/data/divAveStd.txt";
        float[] vectorF1 = {getTime, Data.divAve, Data.divStd};
        Functions.writeFloatVector(str, vectorF1);

        str = Pars.outFile+"/data/spAveStd.txt";
        float[] vectorF2 = {getTime, Data.speedAve, Data.spStd};
        Functions.writeFloatVector(str, vectorF2);


        if(getTime==2400){
            str=Pars.outFile+"/data/divs.txt";
            for(int i=0;i<Pars.MAX_CELLS;i++) {
                if(i<cells.size()){
                    Cell cell = (Cell) cells.get(i);
                    traitValues[i]=1.f/(cell.vDiv*Pars.divConv);
                }
                else{
                    traitValues[i]=99;//use 99 for N/A
                }

            }
            Functions.writeFloatVector(str, traitValues);

            str=Pars.outFile+"/data/spees.txt";
            for(int i=0;i<Pars.MAX_CELLS;i++) {
                if(i<cells.size()){
                    Cell cell = (Cell) cells.get(i);
                    traitValues[i]=1.f*cell.prevSp/Pars.speedConv;
                }
                else{
                    traitValues[i]=99;//use 99 for N/A
                }

            }
            Functions.writeFloatVector(str, traitValues);
        }


    }

	/********************
	* Graphics
	*********************/

    BufferedImage drawIt(int trait, int frameNum){
		BufferedImage bi = new BufferedImage(Pars.sizeW, Pars.sizeH, BufferedImage.TYPE_INT_RGB);
	  	Graphics2D g0 = bi.createGraphics();

	  	//main drawing
	  	Draw.background(g0,Pars.sizeW,Pars.sizeH);
	  	Draw.renderCells(g0,cells,trait);

	  	//display info
	  	Draw.scale(g0);
 		Draw.dispNumCells(g0,cells.size(),numPro);
 		Draw.time(g0,frameNum);

	    return bi;
	}

	
}