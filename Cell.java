import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.*;
/**
 * The dividing Cell.
 */
class Cell {
  private Random diceRoller = new Random();

  boolean quiescence;//quiescent state
  int tDiv;//records total time before division
  int prevDiv;//parental intermitotic time
  int walkTime;//persistence time
  int deathMeter;//time until death
  int family;//trade-off bounds
  float x, y;//cell position
  float angleInDegree;//cell movement angle
  float vDiv;//proliferation rate - rate at which cell moves through cell cycle
  float xDiv;//position in cell cycle (0-1) - at 1, cell can divide
  float speedX, speedY; //x and y speed
  float prevSp;//parental migration speed

  Color color;
  
  /*****************
  * CONSTRUCTOR: 
  ****************/
  Cell(float x, float y, int family) {
    this.x = x;
    this.y = y;
    this.family = family;
    this.quiescence = false;
    this.deathMeter = 1500;//if set below 1500, cell starts to countdown to die
  }
   
  /****************************
  * INTERACTIONS
  ***************************/

  //cell-cell collision
  boolean intersect(float otherX, float otherY, float otherSpX, float otherSpY) {
    float impactX = (this.x+this.speedX) - (otherX+otherSpX);
    float impactY = (this.y+this.speedY) - (otherY+otherSpY);
    float impsqx = (impactX)*(impactX);
    float impsqy = (impactY)*(impactY);

    boolean b = false;
    if(Math.sqrt(impsqx+impsqy)<(2*Pars.effRad)){
      b = true;
    }
    return b;
  }

  //cell-wall collision
  boolean hitWall() {
    float impactX = (this.x+this.speedX-Pars.sizeW/2);
    float impactY = (this.y+this.speedY-Pars.sizeH/2);
    float impsqx = (impactX)*(impactX);
    float impsqy = (impactY)*(impactY);

    boolean b = false;
    if(Math.sqrt(impsqx+impsqy)>(Pars.sizeW/3-2*Pars.effRad)){
      b = true;
    }
    return b;
  }

  //set cell quiescent
  void quiResp(){
    this.quiescence = true;
    this.tDiv += Pars.frameTime;
    this.speedX = 0;
    this.speedY = 0;
  }

  //reactivate quiescent cell
  void activate(){
      this.setSpeedXY();
      this.vDiv = (1.f/this.prevDiv);
  }

  /************************
  * Find available angles
  *************************/

  private int detAng;
  private ArrayList<Integer> finalBank = new ArrayList<>();
  private int[] angBank = new int[360];

  //find an available angle
  int getTheta(float[] nD, float[] nA, int nN){//imports distances, angles, and number of neighboring cells
    Random diceRoller = new Random();
    finalBank.clear();

    for(int i=0;i<360;i++) angBank[i] = i;//360 angles to start

    for(int q=0;q<nN;q++){
      float aCos = (float) Math.acos(nD[q]/(4.f*Pars.effRad));
      int angMin = (int) Math.floor(Math.toDegrees(nA[q]-aCos));
      int angMax = (int) Math.ceil(Math.toDegrees(nA[q]+aCos));

      popExs(angMin,angMax);//exclude angles that interfere with existing cell
    }

    for(int i=0;i<360;i++){
      if(angBank[i]!=999){
        finalBank.add(i);//add remaining angles to list
      }
    }
    //if no available angles, return 999, otherwise pick random from bank
    int thisSize = finalBank.size();
    if(thisSize==0){
      detAng=999;
    }
    else{
      detAng=finalBank.get(diceRoller.nextInt(thisSize));
    }
    return detAng;
  }

  //EXCLUSION function - finds which angles occupied by cell
  private void popExs(int aMin, int aMax){
    if(aMin <= 0 && aMax>0){
      for (int y = 0; y < aMax; y++){
        angBank[y]=999;
      }
      for (int y = aMin+360; y<360; y++){
        angBank[y]=999;
      }
    }
    else if(aMin<=0 && aMax<=0){//new
      for (int y = aMin+360; y<aMax+360; y++){
        angBank[y]=999;
      }
    }
    else if(aMax >= 360 && aMin<360) {
      for (int y = aMin; y < 360; y++){
        angBank[y]=999;
      }
      for (int y = 0; y<aMax-360; y++){
        angBank[y]=999;
      }
    }
    else if(aMax >= 360 && aMin>=360) {
      for (int y = aMin-360; y<aMax-360; y++){
        angBank[y]=999;
      }
    }
    else{
      for (int y = aMin; y <= aMax; y++){
        angBank[y]=999;
      }
    }
  }

  /************************************
  * Update persistence and position
  ************************************/

  void updatePers() {
    if(this.walkTime<=0){//reset persistence and angle if walk is done
      this.walkTime = (int) (Functions.gaussSample(Pars.persMean, Pars.persError, Pars.persMin, Pars.persMax));
      this.angleInDegree = Functions.rangeSample(0, 360);
      this.setSpeedXY();
    }
    else{//count down walk time
      this.walkTime-=Pars.frameTime;
    }

  }

  void updatePos() {
    //move
    this.x += this.speedX*Pars.frameTime;
    this.y += this.speedY*Pars.frameTime;

    //move through cell cycle
    this.xDiv += this.vDiv*Pars.frameTime;
    //keep track of total time in cycle
    this.tDiv += Pars.frameTime;
  }

  /*********************
  * Speed conversion
  *********************/
  //set the XY speed from the previous absolute
  void setSpeedXY(){
    float angRad = (float) (Math.toRadians(this.angleInDegree));
    this.speedX = (float) (this.prevSp*(Math.cos(angRad)));
    this.speedY = (float) (-this.prevSp*(Math.sin(angRad)));
  }

    /****************
    *SET CELL
    *****************/
    void setCell(){
        //start with least aggressive cell
        int dT = Pars.divMax;
        float iSp = Pars.spMin;

        this.prevDiv = dT;//set initial intermitotic time
        this.xDiv = (diceRoller.nextInt(60)+0.f)/60;//random position in cell cycle
        this.tDiv = (int) (dT *this.xDiv);//corresponding time in cell cycle
        this.vDiv = 1.f/(dT);//proliferation rate
        this.angleInDegree = diceRoller.nextInt(360);//set initial angle of movement
        this.prevSp = iSp;//set initial speed
        this.setSpeedXY();//x and y speed from speed

        //get persistence time from Gaussian
        this.walkTime = (int) (Functions.gaussSample(Pars.persMean, Pars.persError, Pars.persMin, Pars.persMax));
	}

	void divNewParams(int findAngle, int identify,int pDiv, float pSp) {
        this.prevDiv = pDiv;
        this.prevSp = pSp;
        //set cells to move in opposite directions upon division
        this.angleInDegree = (identify==1) ? findAngle : (findAngle>=180) ? findAngle-180 : findAngle+180;

        //get new traits allowing drift
        float[] traits = Functions.inheritDriftBounded(this.prevDiv,this.prevSp,this.family,Pars.drift);

  	    int dSt = (int) traits[0];
        this.prevDiv = dSt;//inherited intermitotic time
        this.xDiv = 0;//reset cell cycle position
        this.vDiv = 1.f/dSt;//reset proliferation rate
        this.tDiv = 0;//reset time in cycle

        this.prevSp = traits[1];//inherited speed
        this.setSpeedXY();

        //reset persistence time
        this.walkTime =(int) (Functions.gaussSample(Pars.persMean, Pars.persError, Pars.persMin, Pars.persMax));
  }
   
  /***************************
  * GRAPHICS
  ******************************/

  void draw(Graphics g) {
    //inside
    g.setColor(color);
    g.fillOval((int)(x - Pars.rad), (int)(y - Pars.rad), (int)(2 * Pars.rad),(int)(2 * Pars.rad));

    //outside
    g.setColor(Color.black);
    g.drawOval((int)(x - Pars.rad), (int)(y - Pars.rad), (int)(2 * Pars.rad),(int)(2 * Pars.rad));

  }

}