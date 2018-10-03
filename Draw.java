import java.util.*; 
import java.awt.*;

class Draw{

	static void background(Graphics g1, int sW, int sH){
		g1.setColor(Color.white);
	  	g1.fillRect(0,0,sW,sH);
	}

	//draw scale line
	static void scale(Graphics g1){
		g1.setColor(Color.black);
	 	g1.drawLine((int) ((Pars.dishDiamMicm/2-500)*Pars.micMToPix),60,(int) ((Pars.dishDiamMicm/2+500)*Pars.micMToPix), 60);
        g1.drawLine((int) ((Pars.dishDiamMicm/2-500)*Pars.micMToPix),70,(int) ((Pars.dishDiamMicm/2-500)*Pars.micMToPix), 50);
        g1.drawLine((int) ((Pars.dishDiamMicm/2+500)*Pars.micMToPix),70,(int) ((Pars.dishDiamMicm/2+500)*Pars.micMToPix), 50);
        g1.setFont(new Font("Dialogue", Font.ITALIC, 11));
  	 	g1.drawString("1mm", (int) ((Pars.dishDiamMicm/2+350)*Pars.micMToPix), 50);
	}

	//draw cells
	static void renderCells(Graphics g1, ArrayList cells, int trait){
		for (int i = cells.size(); --i >=0; ) {
           	Cell cell = (Cell) cells.get(i);
          	float tempT = 1.f/(cell.vDiv);
          	tempT = (tempT>Pars.divMax) ? Pars.divMax : (tempT<Pars.divMin) ? Pars.divMin : tempT;

            //colored according to phenotype combination
          	if(trait==1){
          		cell.color = colorSet2DP(tempT, Pars.divMax,Pars.divMin,cell.prevSp, Pars.spMax,Pars.spMin);
          	}
          	else{//colored according to proliferation status
          		if(cell.deathMeter<1500){
      				cell.color = Color.red;
    			}
    			else if(cell.xDiv>0.9){
      				cell.color = Color.black;    			}
    			else{
      				if(cell.quiescence){
        				cell.color = Color.yellow;
      				}
      				else{
        				cell.color = Color.green;
      				}
    			}          	
    		}
    		cell.draw(g1);
        }
	}

	//display number of cells proliferating, quiescent, and total
	static void dispNumCells(Graphics g1, int cS, int nP){
		g1.setFont(new Font("Dialogue", Font.BOLD, 20));
        g1.setColor(Color.black); //black
		g1.drawString("Pro "+nP+" ", Pars.sizeW-120, Pars.sizeH/30);
		g1.drawString("Qui "+(cS-nP)+" ", Pars.sizeW-120, Pars.sizeH/30+20);
		g1.drawString("Tot "+(cS)+" ", Pars.sizeW-120, Pars.sizeH/30+40);
	}

	//display time in d:h:m
	static void time(Graphics g1, int fN){
		int fontAt = Pars.sizeW/50; 
		int fontAt2 = Pars.sizeH/22; 
	    g1.setColor(Color.black);
	    g1.setFont(new Font("Dialogue", Font.BOLD, 24));
	    float timeNow = fN*Pars.frameTime; //in minutes
	    int days = (int)(timeNow/(24*60));
	    int hours = (int)((timeNow-days*24*60)/60);
	    int min = (int)(timeNow-days*24*60-hours*60);
	    g1.drawString(" d : h : m ",fontAt,fontAt2+40);
	    g1.drawString(" "+Integer.toString(days)+" : "+Integer.toString(hours)+" : "+Integer.toString(min), fontAt, fontAt2+80);
	}

	/********************
	* COLOR SET
	********************/

	//defines color map for phenotype combination
	static private Color colorSet2DP(float current1, float max1, float min1, float current2, float max2, float min2){
		Color color;
		int angSect;
		float thetaCol;
		float xCol = 2*(current1-(max1-min1)/2-min1)/(max1-min1);
		float yCol = 2*(current2-max2/2-min2)/(max2-min2);
		float colDist = (float) (1.1f*Math.sqrt(xCol*xCol+yCol*yCol));
		colDist=(colDist>1) ? 1.f : colDist; 
		thetaCol=(float) ((180.f/Math.PI)*Math.atan2(yCol,xCol));
		thetaCol=(thetaCol>360) ? thetaCol-360.f : (thetaCol<0) ? thetaCol+360.f: thetaCol;
		if(colDist<0.0){
			angSect=0;
		}
		else if (thetaCol>0 && thetaCol<=30){
			angSect=1;
		}
		else if(thetaCol>30 && thetaCol<=90){
			angSect=2;
		}	
		else if(thetaCol>90 && thetaCol<=150){
			angSect=3;
		}
		else if(thetaCol>150 && thetaCol<=210){
			angSect=4;
		}
		else if(thetaCol>210 && thetaCol<=270){
			angSect=5;
		}
		else if(thetaCol>270 && thetaCol<330){
			angSect=6; 
		}
		else{
			angSect=1;
		}
		float p1;
		switch (angSect){
			case 0: {
				color = new Color(1-colDist/.5f,1-colDist/.5f,1-colDist/.5f);
				break;}
			case 1: {
				if(thetaCol<180){
					p1=0.5f+(thetaCol)/60.f;
					color = new Color(0,colDist,colDist*(1-p1));
				}
				else{
					p1=(thetaCol-330)/60.f;
					color = new Color(0,colDist,colDist*(1-p1));
				}	
	  			break;}
	  		case 2:{
	  			p1=(thetaCol-30.f)/60.f;
	  			color = new Color(colDist*(p1),colDist,0);
	  			break;
	  		}
	  		case 3:{
	  			p1=(thetaCol-90.f)/60.f;
	  			color = new Color(colDist,colDist*(1-p1),0);	  				  			
	  			break;
	  		}
	  		case 4:{
	  			p1=(thetaCol-150.f)/60.f;
	  			color = new Color(colDist,0,colDist*(p1));
	  			break;
	  		}
	  		case 5:{
	  			if(thetaCol<=230){ 
	  				color=new Color(colDist,0,colDist);
	  			}
	  			else{
	  				p1=(thetaCol-230.f)/40.f; 
	  				color = new Color(colDist*(1-p1),0,colDist);
	  			}
	  			break;
	  		}
	  		case 6:{
	  			if(thetaCol<=310){ 
	  				p1=(thetaCol-270.f)/40.f; 
	  				color=new Color(0,colDist*(p1),colDist);
	  			}
	  			else{
	  				
	  				color = new Color(0,colDist,colDist);
	  			}
	  			break;
	  		}
	  		default:{
	  			color = new Color(0,0,0);
	  		}
	  	}
		return color;
	}


}