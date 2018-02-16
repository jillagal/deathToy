class Pars {
	//defined in bash arguments in exe1.sh
   	static int cellType = 1;
    static int deathR = 0;
    static int catR = 0;
   
    /********************************************************************************************
    * MAIN OPTIONS    **************************************************************************
    ********************************************************************************************/
    static String outFile = "../results";	//output directory

    //timing
    static int frameTime = 1; //frame time in minutes
    static int movFrames=24*60; //length of movie frame in minutes
    static int timeRun = 400*24*60; //run time in minutes

    //layout
    static final int sizeW = 1800; //width in pixels (window size)
    static final int sizeH = sizeW; //height
    static float dishDiamMicm = 4000; //size of circular domain in microns
    
    //conversions
    static float micMToPix = (float) (sizeW/(dishDiamMicm)); //convert microns to pixels
    static int divConv = 60/frameTime;
    static float speedConv = Pars.micMToPix*Pars.frameTime/60; //to get pixels per frame

    //cell/population properties
    private static final float cellDiam = 20; //cell diameter in microns
    static float rad= (float) (cellDiam*micMToPix/2);//cell radius in pixels
    static float effRad = rad*0.9f; //effective cell radius (less than 1 gives some overlap)
    static final int MAX_CELLS = 30000; // Max number cells allowed

    //inheritance
    static int drift = 3; //how much drift possible at division
    static int numBins = 20; //number of bins for phenotypes
   
    //division range properties (in hours)
    static int dMax = 40;//maximum intermitotic time
    static int dMin = 10;//minimum intermitotic time
    static float epD = (dMax-dMin)/numBins;//unit of drift in intermitotic time
    //converted to frame time
    static int divMax = dMax*divConv;
    static int divMin = dMin*divConv;

    //speed range properties (microns/h)
    static float sMax = 20f;//maximum speed
    static float sMin = 0.000f;//minimum speed
    static float epS = (sMax-sMin)/numBins;//unit of drift in speed
    //converted to pixels/frame
    static float spMax = sMax*speedConv;
    static float spMin = sMin*speedConv;

    //persistence
    static int persMax = 220/frameTime; //persistence time in minutes converted - max
    static int persMin = 0/frameTime;//min
    static int persMean = 80/frameTime;//mean
    static int persError = 40/frameTime;//std dev
	    
}
