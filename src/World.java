package Drawing;
//package Detect;

import Jama.*;
//import processing.core*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

import org.openkinect.freenect.*;
import org.openkinect.freenect.processing.*;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import Blobscanner.*;
import codeanticode.gsvideo.*;
import Drawing.*;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;

import org.openkinect.freenect.processing.Kinect;

import WorldObject.*;
import Detect.*;

public class World extends PApplet{
	
	boolean firstRun = true;
	int w;
	int h;

	// Kinect stuff
	boolean depth = true;
	boolean rgb = false;
	boolean ir = true;

	// Homography stuff
	boolean estimate = false;
	boolean calibrated = false;
	Matrix homography;
	// camera points
	PVector []cam = new PVector[4];
	// projector points
	PVector []proj = new PVector[4];

	int Max_fingers; // = 4;
	ArrayList<float[]> fingers;  //x,y, dx, dy, time
	int thres = 15;        // threshold for determining how far a new point has to be from the current point in order to determine if the two points were from the same hand
	int idleTime = 0;
	PVector topLeft[];
	PVector bottomRight[];


	Detector detector;
    // int fps = 10;       // default = 15
	float deg = 15; // Start at 15 degrees

	PImage bg;
	boolean bgTaken = false;
	//track4 t4;
	final float MAX_SPEED 		= 	(float).1;
	final float MIN_SPEED 		= 	(float)-.1;
	final float MAX_SIZE 		= 	18;
	final float MAX_NEG_SIZE 	= 	-17;
	final float MIN_SIZE 		= 	5;
	final float MAX_BUB 		= 	275;
	final float MIN_BUB 		=	200;
	final int RADIUS_1           =   4;
	int neg = 1;
	Random ran = new Random();

	final int player_size = 20;
	final int player_dist_from_wall = 20;
	int BIGCNT = 0;

	// PVector[] Hom = new PVector[4];
	boolean HomFul = false;

	final int topL=1;
	final int topRight=2;
	final int bottomR=3;
	final int bottomLeft=4;
	int WHICHONE=1;

	Kinect kinect;

	ArrayList<bubble> bubbles = new ArrayList<bubble>();

	@Override
	public void setup() {
		size(640, 480);
		kinect = new Kinect(this);
		kinect.start();
		// kinect.enableRGB(true);
		kinect.enableIR(true);

		
		// cam = Hom;
		estimate = true;
		// w = 640;
		// h = 480;
		//size(640,480);
		homography = Matrix.identity(3, 3);
		// setting the four projector points
		proj[0] = new PVector(0, 0, 1);
		proj[1] = new PVector(width, 0, 1);
		proj[2] = new PVector(width, height, 1); 
		proj[3] = new PVector(0, height, 1);
		detector = new Detector( this, 0, 0, 640, 480, 255 );
		fingers = new ArrayList<float[]>();  //x,y, dx, dy, time        LIMIT TO 5??
		background(255);
		startWorld();
		smooth();
		//noStroke();
	}

	@Override
	public void draw() {
		if(HomFul){
			//fill(210, 100);
			//fill(0);
			//rect(0, 0, width+1, height+1);
			//image(bg,0,0);
			image(kinect.getVideoImage().get(),0,0);
			drawBubbles();
		}
		else 
		{
			//image(flip(kinect.getVideoImage().get()),0,0);
			image(kinect.getVideoImage().get(),0,0);
			//image(img,0,0);
		}
	}

	public void drawBubbles()
	{
		int sizeOF = bubbles.size();
		for(int cnt=0; cnt<sizeOF; cnt++)
		{
			// gets all the information about the update
			bubble dB = bubbles.get(cnt); 
			float size= abs(dB.getSIZE()); 
			float x = dB.getX();
			float y = dB.getY();

			float xD = dB.getSPEEDX();
			float yD = dB.getSPEEDY();

			// sets the information about the next move
			float newX = (dB.getX() + xD);
			float newY = (dB.getY() + yD);
			bubbles.get(cnt).setX(newX);
			bubbles.get(cnt).setY(newY);

			// deals with the collision with the wall
			if (newX < 0+(size/2))
			{
				bubbles.get(cnt).setSPEEDX(-1*xD);
				bubbles.get(cnt).setX(0+(size/2));
			}
			else if (newX > width-(size/2))
			{
				bubbles.get(cnt).setSPEEDX(-1*xD);
				bubbles.get(cnt).setX(width-(size/2));
			}

			if (newY < 0+(size/2))
			{
				bubbles.get(cnt).setSPEEDY(-1*yD);
				bubbles.get(cnt).setY(0+(size/2));

			}
			else if (newY > height-(size/2))
			{
				bubbles.get(cnt).setSPEEDY(-1*yD);
				bubbles.get(cnt).setY(height-(size/2));
			}

			if(dB.getPlayerID() == 1)		{ fill(240,128,100);	} // player 1 
			else if(dB.getPlayerID() == 2)  { fill(128,0,128);		} // player 2 
			else if(dB.getPlayerID() == 3)	{ fill(255,215,0);			} // player 3 
			else if(dB.getPlayerID() == 4)	{ fill(255,100,215);			} // player 4 
			else if (dB.getSIZE() > 0) 		{ fill(10,100,200);		} // + bubble //blue
			else 							{ fill(10,200,50);		} // - bubble //green
			ellipse(x,y,size,size); // draws bubble
			//checks for collision with other bubbles
			for(int cnt2=0; cnt2<sizeOF; cnt2++){
				if(cnt2!=cnt){		
					bubble dB2 = bubbles.get(cnt2); 
					float size1= abs(dB2.getSIZE()); 
					float x1 = dB2.getX();
					float y1 = dB2.getY();
					float d = sqrt(pow(y-y1,2)+pow(x-x1,2));
					float range = (size/2)+(size1/2); 

					if(d<=range){	
						float rawSize1 = dB.getSIZE();
						float rawSize2 = dB2.getSIZE();
						float area1 = (float)Math.PI*(rawSize1/2)*(rawSize1/2);
						float area2 = (float)Math.PI*(rawSize2/2)*(rawSize2/2); 

						int neg = 1;
						if (rawSize1 < 0){ area1 = -1*area1;} 
						if (rawSize2 < 0){ area2 = -1*area2;}
						if ((area1+area2) < 0) { neg = -1;}

						float newSize = neg*((float)Math.sqrt( abs(area1+area2) / Math.PI ));
						if (abs(dB.getSIZE()) >= abs(dB2.getSIZE())){	
							float rate = abs(area2)/abs(area1);
							dB.setSIZE(newSize*2);
							dB.setSPEEDX((dB.getSPEEDX()+rate*dB2.getSPEEDX())/(1+rate));
							dB.setSPEEDY((dB.getSPEEDY()+rate*dB2.getSPEEDY())/(1+rate));
							bubbles.remove(cnt2);
							sizeOF--;
							if(cnt>cnt2){
								cnt--;	
							}
							else{
								cnt2--;
							}
						}
						else if (abs(dB.getSIZE()) < abs(dB2.getSIZE()))
						{
							float rate = abs(area1)/abs(area1);
							dB2.setSIZE(newSize*2);
							dB2.setSPEEDX((dB2.getSPEEDX()+rate*dB.getSPEEDX())/(1+rate));
							dB2.setSPEEDY((dB2.getSPEEDY()+rate*dB.getSPEEDY())/(1+rate));
							bubbles.remove(cnt);
							sizeOF--;
							cnt--;
							break;
						}						
					}
				}
			}
		 // FIND	
			if (dB.getPlayerID() == 0)
			{
			// dB.setSIZE((random(98,102)/100)*dB.getSIZE());
			}
		}
		neg = -1*neg;
		// handles user imputs
		// PImage img = t4.getTracked();
		// image(img,0,0);
		ArrayList<float []> trackPoints = getTrackedPoints();
		//movement changes
		/*
		for (int incr = 0; incr < bubbles.ssssize(); incr++ ){
			PVector vector = bubbleMovements(incr);
	        bubbles.get(incr).setSPEEDX((float) (bubbles.get(incr).getSPEEDX()+.07*vector.x));
	        bubbles.get(incr).setSPEEDY((float) (bubbles.get(incr).getSPEEDY()+.07*vector.y));
		}
		*/
		if (!trackPoints.isEmpty())
		{
			for(float[] onePoint : trackPoints)
			{
				float tpX = onePoint[0];
				float tpY = onePoint[1]; 
				float ch_x = onePoint[2];
				float ch_y = onePoint[3];
				for(bubble bub: bubbles)
				{
					float bubX = bub.getX();
					float bubY = bub.getY();
					float d = sqrt(pow(tpX-bubX,2)+pow(tpY-bubY,2));
					float range = abs(bub.getSIZE())/2+20/2; 
					if(d<=range && abs(ch_x) < 20 && abs(ch_y) < 20){	
						if (abs(ch_x) < .0001 && abs(ch_y) < .0001)
						{
							bub.setSPEEDX(0);
							bub.setSPEEDY(0);
						}
						else 
						{
							bub.setSPEEDX(ch_x+bub.getSPEEDX());
							bub.setSPEEDY(ch_y+bub.getSPEEDY());
						}
						//bub.setSPEEDX(ch_x);
						//bub.setSPEEDY(ch_y);
					}
				}
			}
		}
	}
   //only if bubble.playerID ==0
   //move bubbles so they don't collide as much
	public PVector bubbleMovements(int in){
		PVector out;
		float aX = 0;
		float aY = 0;
		int n = 0;
		float x = bubbles.get(in).getX();
		float y = bubbles.get(in).getY();
		float sizeIn = bubbles.get(in).getSIZE();
		
		if (bubbles.get(in).getPlayerID() == 0){
			//don't modify movements of players	
			for (int incr = 0; incr < bubbles.size(); incr++){
				if (incr != in){
					float distX = (x)-(bubbles.get(incr).getX());
					float distY = (y)-(bubbles.get(incr).getY());
					float dist = sqrt(pow(distX,2)+pow(distY,2));
					float sizeIncr = bubbles.get(incr).getSIZE();
					float newdist = (float) (abs(sizeIn)+RADIUS_1);
					//repel from other non players
					if (dist < newdist) {
						aX += bubbles.get(incr).getX();
						aY += bubbles.get(incr).getY();
						n++;
					}
				}
			}
			if (n!= 0){
				out = new PVector(x-aX, y-aY, 0);
			}
			else{
				out = new PVector(0, 0, 0);
			}
		}
		else{
			out = new PVector(0, 0, 0);
		}
		out.normalize();
		return out;//return output PVector
	}
	
	/*
	public ArrayList<float[]> getTrackedPoints()
	{	
		ArrayList<float[]> tps = new ArrayList<float[]>();
		int points = (int)random(3, 10);
		for(int cnt=0; cnt< points;cnt++)
		{
			int	x = (int)random(0,width);
			int	y = (int)random(0,height);
	
			float xD = 0;
			float yD = 0;
			while( xD == 0 && yD == 0)
			{
				//xD= random((float)-.5,(float).5);
				//yD= random((float)-.5,(float).5);
				xD= random((float)-3,(float)3);
				yD= random((float)-3,(float)3);
			}
			float[] b = {x,y,xD,yD};  
			tps.add(b);
		}
		if (BIGCNT == 100) // keep creating
		{
			//startWorld();
			BIGCNT=0;
		}
		BIGCNT++;
		return tps;
	
	}
	*/
	public void startWorld()
	{
		bubbles.add(new bubble(player_dist_from_wall,
				player_dist_from_wall, 
				player_size, 0, 0, 1));
		bubbles.add(new bubble(player_dist_from_wall,
				height-player_dist_from_wall, 
				player_size, 0, 0, 2));
		bubbles.add(new bubble(width-player_dist_from_wall,
				player_dist_from_wall, 
				player_size, 0, 0, 3));
		bubbles.add(new bubble(width-player_dist_from_wall, 
				height-player_dist_from_wall,
				player_size, 0, 0, 4));

		int radius_cnt = player_size*4;
		int number_of_bubbles = (int)random(MIN_BUB, MAX_BUB);
		for (int cnt = 0; (cnt <= number_of_bubbles); cnt++) //&& abs(radius_cnt) < (height-MAX_SIZE-player_size))
		{
			float size = 0;
			while (abs(size) < MIN_SIZE)
			{
				size = random(MAX_NEG_SIZE,MAX_SIZE);
			}
			radius_cnt+=size;		

			//5 buffer so init doesnt hit player
			int	x = (int)random(size+45,width-size-45);
			int	y = (int)random(size+45,height-size-45);
			float xD = 0;
			float yD = 0;
			while( xD == 0 && yD == 0)
			{
				xD= random(MIN_SPEED,MAX_SPEED);
				yD= random(MIN_SPEED,MAX_SPEED);
			}
			bubble b = new bubble(x,y,size,xD,yD);	
			bubbles.add(b);
		}
	}

	public void keyPressed()
	{
		if (key == 's')
		{
			bubbles = new ArrayList();
			startWorld();
			background(255);
		}
	}
	public void mousePressed() {
		if(HomFul==false)
		{
			if (WHICHONE==topL) {
				cam[0] = new PVector(mouseX, mouseY, 1);
				WHICHONE=2;
			}
			else if (WHICHONE==topRight) {
				cam[1] = new PVector(mouseX, mouseY, 1);
				WHICHONE=3;
			}
			else if (WHICHONE==bottomR) {
				cam[2] = new PVector(mouseX, mouseY, 1);
				WHICHONE=4;
			}
			else if (WHICHONE==bottomLeft) {
				cam[3] = new PVector(mouseX, mouseY, 1);
				WHICHONE=1;
				HomFul = true;
				//t4 = new track4(Hom, kinect);
				this.kinect.enableRGB(false);
				this.kinect.enableDepth(true);
				this.kinect.enableIR(true);
			}
		}
	}
	//-------------------------------------------------------------------
	boolean isValid(int i){
		if ( topLeft[i].x > 0 && topLeft[i].y > 0 && bottomRight[i].x < width && bottomRight[i].y < height &&
				abs(bottomRight[i].x - topLeft[i].x) > 2 && abs(bottomRight[i].x - topLeft[i].x) < 40 
				&& abs(bottomRight[i].y - topLeft[i].y) > 2 && abs(bottomRight[i].y - topLeft[i].y) < 40) 
		{
			return true;
		}
		else{
			return false;
		}
	}

	public void stop() {
		kinect.quit();
		//mm.finish();
		super.stop();
	}
	/*
	Calculates the homography matrix by using the reference points 
	gathered during the mouse pressed
	*/
	public void estimateHomography() {
		// Creates an array of two times the size of the cam[] array 
		double[][] a = new double[2*cam.length][];
		// Creates the estimation matrix
		for (int i = 0; i < cam.length; i++) {
			double l1 [] = {
					cam[i].x, cam[i].y, cam[i].z, 0, 0, 0, -cam[i].x*proj[i].x, -cam[i].y*proj[i].x, -proj[i].x
			};
			double l2 [] = {
					0, 0, 0, cam[i].x, cam[i].y, cam[i].z, -cam[i].x*proj[i].y, -cam[i].y*proj[i].y, -proj[i].y
			};
			a[2*i] = l1;
			a[2*i+1] = l2;
		}
		Matrix A = new Matrix(a);
		Matrix T = A.transpose();
		Matrix X = T.times(A);
		EigenvalueDecomposition E = X.eig();
		// Find the eigenvalues 
		double[] eigenvalues = E.getRealEigenvalues();
		// grab the first eigenvalue from the eigenvalues []
		double w = eigenvalues[0];
		int r = 0;
		// Find the minimun eigenvalue
		
		for (int i= 0; i< eigenvalues.length; i++) {
			if (eigenvalues[i] <= w) {
				w = eigenvalues[i];
				r = i;
			}
		}
		// find the corresponding eigenvector
		Matrix v = E.getV();  
		// create the homography matrix from the eigenvector v
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				homography.set(i, j, v.get(i*3+j, r));
			}
		}
		println("Estimated H");
		homography.print(3, 3);
	}
	PVector applyTransformation(Matrix h, PVector v) {
		Matrix u  = new Matrix(3, 1);
		u.set(0, 0, v.x);
		u.set(1, 0, v.y);
		u.set(2, 0, 1);
		Matrix t = h.times(u);
		return new PVector((float)(t.get(0, 0)/t.get(2, 0)), (float)(t.get(1, 0)/ (t.get(2, 0))));
	} 
	// method for background subtraction
	PImage subtracted (PImage bgImg, PImage currImg) {
		bgImg.loadPixels(); 
		currImg.loadPixels();
		PImage result = createImage(bgImg.width,bgImg.height,RGB);
		for (int x = 0; x <bgImg.width;x++) {
			for (int y = 0; y <bgImg.height; y++) {
				int currColor = (currImg.pixels[x + y*bgImg.width]);
				int bgColor = (bgImg.pixels[x + y*bgImg.width]);

				int currR = (currColor >> 16) & 0xff;
				int currG = (currColor >> 8) & 0xff;
				int currB = currColor & 0xff;

				int bgR = (bgColor >> 16) & 0xff;
				int bgG = (bgColor >> 8) & 0xff;
				int bgB = bgColor & 0xff;
				// bgImg.width = x
				result.set(x,y, color(abs(currR-bgR),abs(currG-bgG), abs(currB-bgB)));          // flip image horizontally to make sure the image is read exactly as shown by the monitor
			}
		}
		return result;
	}

	public ArrayList<float[]> getTrackedPoints(){
		
        if (bgTaken == false) {              // to take the first background image
			delay(1000);
			//bg = flip(kinect.getVideoImage().get());
			bg = kinect.getVideoImage().get();
			bgTaken = true;
		}
		else
		{
		//	bg = new PImage(640,480);
		}

		if (estimate) {                  // calculate homography once the 4 bounding points are selected
			estimateHomography();
			println("inside homom");
			estimate=false;
			calibrated = true;
			// saveHomography();
		}
		PImage img = kinect.getVideoImage().get();
		//image(img,640,0);   // raw depth image from Kinect. Contains noise (background subtraction not done yet)
		img = subtracted(bg,img);
		img.filter(THRESHOLD, (float)0.5);
		//image(img,0,0);
    	//loadPixels();
		img.loadPixels();
		
		//image(img, 0, 0);
		

		detector.findBlobs(img.pixels, img.width, img.height);
		detector.loadBlobsFeatures();// to call always before to use a method returning or processing a blob feature

		topLeft =  detector.getA();
		bottomRight = detector.getD();
		if (calibrated){             // when homography has been calculated, we can apply homography transformation of points
			for (int i = 0; i < topLeft.length; i++){
				topLeft[i] = applyTransformation(homography,topLeft[i]);
				bottomRight[i] = applyTransformation(homography,bottomRight[i]);      
			}
		}

		int totBlobs = detector.getBlobsNumber();
		Max_fingers = fingers.size();

		if ( totBlobs > 0 && Max_fingers > 0 ){
			float distArray[][] = new float[Max_fingers][totBlobs];
			for (int i = 0; i < totBlobs; i++) {
				if ( isValid(i)){  
					for(int f = 0; f < Max_fingers; f++){
						if (fingers.size()>1){
						}
						float[] finCenter = fingers.get(f);
						distArray[f][i] = abs(finCenter[0] - (topLeft[i].x + bottomRight[i].x)/2) + abs(finCenter[1] - (topLeft[i].y + bottomRight[i].y)/2);
					}
				}
			}

			int f = 0;
			while (f < fingers.size()){    
				float distance = 70000;
				int closestBlobIndex = 0;
				for (int b = 0; b < totBlobs; b++){
					if (topLeft[b].x > 0 && topLeft[b].y > 0 && bottomRight[b].x < width && bottomRight[b].y < height && distArray[f][b] < distance){
						distance = distArray[f][b];
						closestBlobIndex = b;
					}
				}
				if (distance < thres){
					float [] prev = fingers.get(f);
					float newX = (topLeft[closestBlobIndex].x + bottomRight[closestBlobIndex].x) / 2;
					float newY = (topLeft[closestBlobIndex].y + bottomRight[closestBlobIndex].y) / 2;
					float dx = newX - prev[0];
					float dy = newY - prev[1];
					float ans [] = {newX,newY,dx,dy, 0};
					fingers.set(f, ans);
					// make sure that this 'b'th point isnt tracked anymore
					topLeft[closestBlobIndex].x = -10000;
					f++;
				}
				else{
					float curr[] = fingers.get(f);
					curr[4]++;
					if (curr[4] > idleTime){
						fingers.remove(f);
					}
					else{
						fingers.set(f, curr);
						f++;
					}
				}
			}
			for (int b = 0; b < totBlobs && isValid(b) ; b++) {
				float ans[]  = { (topLeft[b].x + bottomRight[b].x)/2, (topLeft[b].y + bottomRight[b].y)/2, 0, 0, 0 };
				fingers.add( ans );
			}
		}
		else if (totBlobs > 0){
			for (int i = 0; i < totBlobs; i++) {
				if ( isValid(i)){   
					float ans [] = {(topLeft[i].x + bottomRight[i].x)/2,  (topLeft[i].y + bottomRight[i].y)/2, 0, 0, 0 };
					fingers.add(ans );
				}
			}    
		}

		else {           // not using any time limit to allow fingerprints to stay
			int i = 0;
			while (i < fingers.size()){
				float curr[] = fingers.get(i);
				curr[4]++;
				if (curr[4] > idleTime){
					fingers.remove(i);
				}
				else{
					fingers.set(i,curr);
					i++;
				}
			}
		}

		fill(220,0,0);
		if (calibrated){
			for (int i = 0; i < cam.length; i++){ // extra REMOVE 
				fill(255,0,0);
				//ellipse (cam[i].x,cam[i].y,5,5);
				
			}
			for (int i = 0; i < fingers.size();i++){
					ellipse(fingers.get(i)[0],fingers.get(i)[1],20,20);
			}
		}
	
	return fingers;
}

	public PImage flip(PImage img)
	{
	  PImage flipped = createImage(img.width, img.height, RGB);
	  img.loadPixels();
	  flipped.loadPixels();
	  for (int y = 0; y < img.height; y++) { 
	    for (int x = img.width; x > 0; x--) { 
	      int pos = (y*img.width + (x-1));
	      float r = red(img.pixels[pos]);
	      float g = green(img.pixels[pos]);
	      float b = blue(img.pixels[pos]);
	      flipped.pixels[y*img.width + (img.width-x)] = color(r, g, b);
	    }
	  }
	  return flipped;
	}
}

