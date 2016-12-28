package io.github.kuohsuanlo.cyberworld;

import java.util.Collections;
import java.util.Arrays;
import java.util.Random;
 
/*
 * recursive backtracking algorithm
 * shamelessly borrowed from the ruby at
 * http://rosettacode.org/wiki/Maze_generation#Java
 * http://weblog.jamisbuck.org/2010/12/27/maze-generation-recursive-backtracking
 *
 *
 *
 *
 */
public class CityStreetGenerator {
	private final int x;
	private final int y;
	private final int[][] city;
	private final int[][][] hightway;
    private final Random rng;
    private final int minBW;
	public CityStreetGenerator(int x, int y, Random r, int mmbw) {
		this.x = x;
		this.y = y;
		city = new int[this.x][this.y];
		hightway = new int[this.x][this.y][3];
		rng = r;
		minBW = mmbw;
		recursiveSplitting(0,0,x-1,y-1,1);
		determiningBuilding();
		determiningHighway();
	}
	public int getRoadType(int rx, int rz){
		rx+=x*0.5;
		rz+=y*0.5;
		if(rx<0){
			rx*=-1;
		}
		if(rz<0){
			rz*=-1;
		}
		return city[rx%(this.x)][rz%(this.y)];
	}
	public int getHighwayType(int rx, int rz,int layer){
		rx+=x*0.5;
		rz+=y*0.5;
		if(rx<0){
			rx*=-1;
		}
		if(rz<0){
			rz*=-1;
		}
		return hightway[rx%(this.x)][rz%(this.y)][layer-1];
	}
	private void determiningBuilding(){
		for(int i=0;i<x;i++){
			for(int j=0;j<y;j++){
				if(city[i][j] ==CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
					city[i][j] = CyberWorldObjectGenerator.DIR_BUILDING;
				} 
			}
		}
	}
	private void determiningHighway(){
		for(int l=0;l<3;l++){
			for(int i=0;i<x;i++){
				for(int j=0;j<y;j++){
					if(hightway[i][j][l] ==CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
						hightway[i][j][l] = CyberWorldObjectGenerator.DIR_BUILDING;
					} 
				}
			}
		}

	}
	private void recursiveSplitting(int point1x, int point1y, int point2x, int point2y, int recursiveTimes){
		//System.out.println(recursiveTimes + " : "+ point1x+","+point1y+"/"+point2x+","+point2y);
		if(Math.abs(point1x-point2x)<minBW  ||  Math.abs(point1y-point2y)<minBW){
			return;
		}
		else{
			int x_shift = -1;
			int y_shift = -1;
			int x_margin = Math.abs(point1x-point2x+1)-(minBW);
			int y_margin = Math.abs(point1y-point2y+1)-(minBW);
			int x_highway_margin = Math.abs(point1x-point2x+1)-(minBW);
			int y_highway_margin = Math.abs(point1y-point2y+1)-(minBW);
			if(x_margin>0){
				x_shift = rng.nextInt(x_margin);
			}
			if(y_margin>0){
				y_shift = rng.nextInt(y_margin);
			}
			if(x_margin>0 && y_margin>0){
				for(int i=point1x;i<=point2x;i++){
					if(city[i][point1y+minBW+y_shift] ==  CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
						city[i][point1y+minBW+y_shift]=CyberWorldObjectGenerator.DIR_EAST_WEST;
					}
				}
				for(int i=point1y;i<=point2y;i++){
					if(city[point1x+minBW+x_shift][i] ==  CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
						city[point1x+minBW+x_shift][i]=CyberWorldObjectGenerator.DIR_NORTH_SOUTH;
					}
				}
				//starting point & end point
				if(recursiveTimes!=1){
					if(point1y-1>=0){
						city[point1x+minBW+x_shift][point1y-1] = CyberWorldObjectGenerator.DIR_INTERSECTION;
					}
					if(point2y+1<y){
						city[point1x+minBW+x_shift][point2y+1] = CyberWorldObjectGenerator.DIR_INTERSECTION;
					}
					if(point1x-1>=0){
						city[point1x-1][point1y+minBW+y_shift] = CyberWorldObjectGenerator.DIR_INTERSECTION;
					}
					if(point2x+1<y){
						city[point2x+1][point1y+minBW+y_shift] = CyberWorldObjectGenerator.DIR_INTERSECTION;
					}
				}
				//intersection
				city[point1x+minBW+x_shift][point1y+minBW+y_shift] = CyberWorldObjectGenerator.DIR_INTERSECTION;
				
				int intersectionX = point1x+minBW+x_shift;
				int intersectionY = point1y+minBW+y_shift;
				//System.out.println(recursiveTimes + " : intersection point"+ intersectionX+","+intersectionY);
				
				// left-up, left-down, right-up, right-down
				recursiveSplitting( point1x,  point1y,  intersectionX-1,  intersectionY-1,recursiveTimes+1);
				recursiveSplitting( point1x,  intersectionY+1,  intersectionX-1,  point2y,recursiveTimes+1);
				recursiveSplitting( intersectionX+1,  point1y,  point2x,  intersectionY-1,recursiveTimes+1);
				recursiveSplitting( intersectionX+1,  intersectionY+1,  point2x,  point2y,recursiveTimes+1);
			}

			if(x_highway_margin>0 && y_highway_margin>0){
				for(int l=0;l<3;l++){
					boolean highwayCutout_x = rng.nextDouble()>0.66;
					boolean highwayCutout_z = rng.nextDouble()>0.66;
					if(highwayCutout_x){
						for(int i=point1x;i<=point2x;i++){
							if(hightway[i][point1y+minBW+y_shift][l] ==  CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
								hightway[i][point1y+minBW+y_shift][l]=CyberWorldObjectGenerator.DIR_EAST_WEST;
							}
						}
					}
					if(highwayCutout_z){
						for(int i=point1y;i<=point2y;i++){
							if(hightway[point1x+minBW+x_shift][i][l] ==  CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
								hightway[point1x+minBW+x_shift][i][l]=CyberWorldObjectGenerator.DIR_NORTH_SOUTH;
							}
						}
					}
					//starting point & end point
					if(recursiveTimes!=1){
						if(highwayCutout_x){
							if(point1x-1>=0){
								hightway[point1x-1][point1y+minBW+y_shift][l] = CyberWorldObjectGenerator.DIR_INTERSECTION;
							}
							if(point2x+1<y){
								hightway[point2x+1][point1y+minBW+y_shift][l] = CyberWorldObjectGenerator.DIR_INTERSECTION;
							}
						}
						if(highwayCutout_z){
							if(point1y-1>=0){
								hightway[point1x+minBW+x_shift][point1y-1][l] = CyberWorldObjectGenerator.DIR_INTERSECTION;
							}
							if(point2y+1<y){
								hightway[point1x+minBW+x_shift][point2y+1][l] = CyberWorldObjectGenerator.DIR_INTERSECTION;
							}
						}
					}
					//intersection
					if(highwayCutout_x&&highwayCutout_z){
						hightway[point1x+minBW+x_shift][point1y+minBW+y_shift][l] = CyberWorldObjectGenerator.DIR_INTERSECTION;
					}
				}
				
				
			}
			
		}
	}
	private void displayGrid() {
		System.out.println("STREET");
		for (int i = 0; i < y; i++) {
			for (int j = 0; j < x; j++) {
				if(city[j][i]>=0){
					//System.out.print(city[j][i]);
					System.out.print("x");
				}
				else{
					System.out.print(" ");
				}
			}
			System.out.println("");
		}
		for(int l=0;l<3;l++){

			System.out.println("HIGHWAY : "+l);
			for (int i = 0; i < y; i++) {
				for (int j = 0; j < x; j++) {
					if(hightway[j][i][l]>=0){
						//System.out.print(city[j][i]);
						System.out.print("x");
					}
					else{
						System.out.print(" ");
					}
				}
				System.out.println("");
			}
		}		
	}
	public static void main(String[] args) {
		int w =50;
		int h =50;
		Random rng = new Random();
		rng.setSeed(9888);
		CityStreetGenerator g = new CityStreetGenerator(w, h,rng,2);
		g.displayGrid();
		
	}
 
}