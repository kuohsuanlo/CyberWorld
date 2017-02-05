package io.github.kuohsuanlo.cyberworld;

import java.util.Collections;
import java.util.Arrays;
import java.util.Random;

import org.bukkit.Material;
 
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

/*   1  2  3  4       1 2 3  	1 2
 *   5  6  7  8		  4 5 6	    3 4
 *   9 10 11 12	      7 8 9
 *  13 14 15 16
 *		Large		  Medium    Small
 *
 *  struct
 */
public class SignGenerator {
	private final int x;
	private final int y;
	private final int[][] city;
	private final int[][][] merged;
	private final int[][][] building_struct;
    private final Random rng;
    private final int minBW;
    private final int sign_interval =2;


    public final int[] a_size ;
    public final int[] a_build_num ;
    private final int[] a_build_code;
	
	public SignGenerator(int total_num, int x, int y, Random r, int mmbw,int s,int m,int l, int ss, int ms, int ls) {
		this.x = x;
		this.y = y;
		merged = new int[this.x][this.y][total_num];
		building_struct = new int[this.x][this.y][3];
		city = new int[this.x][this.y];
		rng = r;
		minBW = mmbw;
		
		a_size = new int[3];
		a_build_num = new int[3];
		a_build_code = new int[3];
		a_size[0]=Math.min(mmbw,ss);
		a_size[1]=Math.min(mmbw,ms);
		a_size[2]=Math.min(mmbw,ls);
		a_build_num[0] = s;
		a_build_num[1] = m;
		a_build_num[2] = l;
		a_build_code[0]=CyberWorldObjectGenerator.DIR_S_BUILDING;
		a_build_code[1]=CyberWorldObjectGenerator.DIR_M_BUILDING;
		a_build_code[2]=CyberWorldObjectGenerator.DIR_L_BUILDING;
		
		
		for(int i=0;i<total_num;i++){
			recursiveSplitting(0,0,x-1,y-1,1,i);
		}
		fillNotDeterminedRoad();
	}
	private void fillNotDeterminedRoad(){
		for(int i=0;i<x;i++){
			for(int j=0;j<y;j++){
				if(city[i][j]==CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
					city[i][j] = CyberWorldObjectGenerator.DIR_BUILDING;
				}
			}
		}
	}
	public int getRoadType(int rx, int rz){
		int[] chunk_coor = {rx,rz};
		return city[chunk_coor[0]][chunk_coor[1]];
	}
	public int getMerged(int rx, int rz,int l){
		int[] chunk_coor = {rx,rz};
		return merged[chunk_coor[0]][chunk_coor[1]][l];
	}
    void recursiveSplitting(int point1x, int point1y, int point2x, int point2y, int recursiveTimes, int set_number){
		if(Math.abs(point1x-point2x+1)<=minBW+2*sign_interval  ||  Math.abs(point1y-point2y+1)<=minBW+2*2*sign_interval){
			
			point2x= Math.min(point2x+1,x);
			point2y= Math.min(point2y+1,y);
			
			for(int l=2;l>=0;l--){
				int width = a_size[l]/2 +rng.nextInt(a_size[l]/2);
				int height = a_size[l]/2 +rng.nextInt(a_size[l]/2);
				
				if(a_build_num[l]>0){
				boolean hasPasted = false;
					for(int i=point1x;i<=point2x;i+=width+sign_interval){
						for(int j=point1y;j<=point2y;j+=height+sign_interval){
							
							if((Math.min(i+width,point2x) - i)>=width  &&  (Math.min(j+height,point2y) - j)>=height){
								hasPasted=true;
								int current_struct = 2;
							
								
								int height_max = Math.min(j+height,point2y);
								int width_max = Math.min(i+width,point2x);
								
								for(int s1=i;s1<width_max;s1++){
									for(int s2=j;s2<height_max;s2++){
										if((s2==j  ||  s2==height_max-1)  ||  (s1==i  ||  s1==width_max-1) ){
											building_struct[s1][s2][l]=1;
											merged[s1][s2][set_number]=Material.IRON_FENCE.getId();
										}
										else{
											building_struct[s1][s2][l]=current_struct;
											merged[s1][s2][set_number]=Material.WOOL.getId();
											current_struct++;
										}
										city[s1][s2] = CyberWorldObjectGenerator.DIR_BUILDING;
										
									}
								}
								
							}
						}
					}
					if(hasPasted){
						break;
					}
				}
			}
			
				
			
			return;
		}
		else{
			int x_shift = 0;
			int y_shift = 0;
			int x_margin = Math.abs(point1x-point2x+1)-(minBW);
			int y_margin = Math.abs(point1y-point2y+1)-(minBW);

			if(x_margin>0){
				x_shift = rng.nextInt(x_margin);
			}
			if(y_margin>0){
				y_shift = rng.nextInt(y_margin);
			}

			int intersectionX = point1x+minBW+x_shift;
			int intersectionY = point1y+minBW+y_shift;
			
			if(x_margin>0 && y_margin>0){
				for(int i=point1x;i<=point2x;i++){
					if(city[i][intersectionY] ==  CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
						city[i][intersectionY]=CyberWorldObjectGenerator.DIR_EAST_WEST;
					}
				}
				for(int i=point1y;i<=point2y;i++){
					if(city[intersectionX][i] ==  CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
						city[intersectionX][i]=CyberWorldObjectGenerator.DIR_NORTH_SOUTH;
					}
				}
				

				
				//starting point & end point
				if(recursiveTimes!=1){
					if(point1y-1>=0){
						city[intersectionX][point1y-1] = CyberWorldObjectGenerator.DIR_INTERSECTION;
					}
					if(point2y+1<y){
						city[intersectionX][point2y+1] = CyberWorldObjectGenerator.DIR_INTERSECTION;
					}
					if(point1x-1>=0){
						city[point1x-1][intersectionY] = CyberWorldObjectGenerator.DIR_INTERSECTION;
					}
					if(point2x+1<x){
						city[point2x+1][intersectionY] = CyberWorldObjectGenerator.DIR_INTERSECTION;
					}
				}
				//intersection
				city[intersectionX][intersectionY] = CyberWorldObjectGenerator.DIR_INTERSECTION;
				
				//System.out.println(recursiveTimes + " : intersection point"+ intersectionX+","+intersectionY);
				
				
				// left-up, left-down, right-up, right-down
				recursiveSplitting( point1x,  point1y,  intersectionX-1-sign_interval,  intersectionY-1-sign_interval,recursiveTimes+1,set_number);
				recursiveSplitting( point1x,  intersectionY+1+sign_interval,  intersectionX-1-sign_interval,  point2y,recursiveTimes+1,set_number);
				recursiveSplitting( intersectionX+1+sign_interval,  point1y,  point2x,  intersectionY-1-sign_interval,recursiveTimes+1,set_number);
				recursiveSplitting( intersectionX+1+sign_interval,  intersectionY+1+sign_interval,  point2x,  point2y,recursiveTimes+1,set_number);

			}
		}
	}
	private void displayGrid(int w, int h,int set_number) {
		
		System.out.println("----------------------------------------");
		for(int l=0;l<set_number;l++){

			System.out.println("building_struct : "+l);
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					int tmp  = this.getMerged(i,j,l);
					//int tmp  = this.getBuildingStruct(i,j, l);
					
					if(tmp>0){
						System.out.print(Integer.toHexString(tmp%16));
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
		int h =40;
		int set_number =1;
		Random rng = new Random();
		rng.setSeed(1205);
		SignGenerator g = new SignGenerator(set_number, w,h,rng,20,1,1,1,50,54,54);
		g.displayGrid(w,h,set_number);
		
	}
 
}