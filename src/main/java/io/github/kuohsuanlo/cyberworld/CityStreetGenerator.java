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

/*   1  2  3  4       1 2 3  	1
 *   5  6  7  8		  4 5 6	
 *   9 10 11 12	      7 8 9
 *  13 14 15 16
 *		Large		  Medium    Small
 *
 *  struct
 */
public class CityStreetGenerator {
	private final int x;
	private final int y;
	private final int[][] city;
	private final int[][][] building;
	private final int[][][] building_type;
	private final int[][][] building_struct;
	private final int[][][] hightway;
    private final Random rng;
    private final int minBW;
    private final int s_build_num;
    private final int m_build_num;
    private final int l_build_num;
    private final double s_rate;
    private final double m_rate;
    private final double l_rate;
    public final int s_size;
    public final int m_size;
    public final int l_size;
	public CityStreetGenerator(int x, int y, Random r, int mmbw,int s,int m,int l, int ss, int ms, int ls,double sr,double mr,double lr) {
		this.x = x;
		this.y = y;
		city = new int[this.x][this.y];
		hightway = new int[this.x][this.y][3];
		building = new int[this.x][this.y][3];
		building_type= new int[this.x][this.y][3];
		building_struct=new int[this.x][this.y][3];
		rng = r;
		minBW = mmbw;
		s_build_num=s;
		m_build_num=m;
		l_build_num=l;
		s_rate = sr;
		m_rate = mr;
		l_rate = lr;
		s_size = Math.min(mmbw,ss);
		m_size = Math.min(mmbw,ms);
		l_size = Math.min(mmbw,ls);
		recursiveSplitting(0,0,x-1,y-1,1);
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
		return hightway[rx%(this.x)][rz%(this.y)][layer];
	}
	public int getBuilding(int rx, int rz,int layer){
		rx+=x*0.5;
		rz+=y*0.5;
		if(rx<0){
			rx*=-1;
		}
		if(rz<0){
			rz*=-1;
		}
		return building[rx%(this.x)][rz%(this.y)][layer];
	}
	public int getBuildingType(int rx, int rz,int layer){
		rx+=x*0.5;
		rz+=y*0.5;
		if(rx<0){
			rx*=-1;
		}
		if(rz<0){
			rz*=-1;
		}
		return building_type[rx%(this.x)][rz%(this.y)][layer];
	}
	public int getBuildingStruct(int rx, int rz,int layer){
		rx+=x*0.5;
		rz+=y*0.5;
		if(rx<0){
			rx*=-1;
		}
		if(rz<0){
			rz*=-1;
		}
		return building_struct[rx%(this.x)][rz%(this.y)][layer];
	}	
    void recursiveSplitting(int point1x, int point1y, int point2x, int point2y, int recursiveTimes){
		if(Math.abs(point1x-point2x+1)<=minBW  ||  Math.abs(point1y-point2y+1)<=minBW){
			
			point2x= Math.min(point2x+1,x);
			point2y= Math.min(point2y+1,y);
			
			
			
			
			
			for(int l=0;l<3;l++){
				if(l==0  &&  rng.nextDouble()<s_rate){
					for(int i=point1x;i<=point2x;i+=s_size){
						for(int j=point1y;j<=point2y;j+=s_size){
							if((Math.min(i+s_size,point2x) - i)>=s_size  &&  (Math.min(j+s_size,point2y) - j)>=s_size){
								int s_type = rng.nextInt(s_build_num);
								int current_struct = 1;
								for(int s2=j;s2<Math.min(j+s_size,point2y);s2++){
									for(int s1=i;s1<Math.min(i+s_size,point2x);s1++){
										city[s1][s2] = CyberWorldObjectGenerator.DIR_BUILDING;
										building[s1][s2][l]=CyberWorldObjectGenerator.DIR_S_BUILDING;
										building_type[s1][s2][l]=s_type;
										building_struct[s1][s2][l]=current_struct;
										current_struct++;
									}
								}
								
							}
							
							
						}
					}
				}
				else if(l==1  &&  rng.nextDouble()<m_rate){
					for(int i=point1x;i<=point2x;i+=m_size){
						for(int j=point1y;j<=point2y;j+=m_size){
							int m_type = rng.nextInt(m_build_num);
							if((Math.min(i+m_size,point2x) - i)>=m_size  &&  (Math.min(j+m_size,point2y) - j)>=m_size){
								int current_struct = 1;
								for(int s2=j;s2<Math.min(j+m_size,point2y);s2++){
									for(int s1=i;s1<Math.min(i+m_size,point2x);s1++){
										city[s1][s2] = CyberWorldObjectGenerator.DIR_BUILDING;
										building[s1][s2][l]=CyberWorldObjectGenerator.DIR_M_BUILDING;
										building_type[s1][s2][l]=m_type;
										building_struct[s1][s2][l]=current_struct;
										current_struct++;
									}
								}
							}
						}
					}
				}
				else if(l==2  &&  rng.nextDouble()<l_rate){
					for(int i=point1x;i<=point2x;i+=l_size){
						for(int j=point1y;j<=point2y;j+=l_size){
							int l_type = rng.nextInt(l_build_num);
							if((Math.min(i+l_size,point2x) - i)>=l_size  &&  (Math.min(j+l_size,point2y) - j)>=l_size){
								int current_struct = 1;
								for(int s2=j;s2<Math.min(j+l_size,point2y);s2++){
									for(int s1=i;s1<Math.min(i+l_size,point2x);s1++){
										city[s1][s2] = CyberWorldObjectGenerator.DIR_BUILDING;
										building[s1][s2][l]=CyberWorldObjectGenerator.DIR_L_BUILDING;
										building_type[s1][s2][l]=l_type;
										building_struct[s1][s2][l]=current_struct;
										current_struct++;
									}
								}
							}
						}
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
			int x_highway_margin = Math.abs(point1x-point2x+1)-(minBW);
			int y_highway_margin = Math.abs(point1y-point2y+1)-(minBW);

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
					if(point2x+1<y){
						city[point2x+1][intersectionY] = CyberWorldObjectGenerator.DIR_INTERSECTION;
					}
				}
				//intersection
				city[intersectionX][intersectionY] = CyberWorldObjectGenerator.DIR_INTERSECTION;
				
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
							if(hightway[i][intersectionY][l] ==  CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
								hightway[i][intersectionY][l]=CyberWorldObjectGenerator.DIR_EAST_WEST;
							}
						}
					}
					if(highwayCutout_z){
						for(int i=point1y;i<=point2y;i++){
							if(hightway[intersectionX][i][l] ==  CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
								hightway[intersectionX][i][l]=CyberWorldObjectGenerator.DIR_NORTH_SOUTH;
							}
						}
					}
					//starting point & end point
					if(recursiveTimes!=1){
						if(highwayCutout_x){
							if(point1x-1>=0){
								hightway[point1x-1][intersectionY][l] = CyberWorldObjectGenerator.DIR_INTERSECTION;
							}
							if(point2x+1<y){
								hightway[point2x+1][intersectionY][l] = CyberWorldObjectGenerator.DIR_INTERSECTION;
							}
						}
						if(highwayCutout_z){
							if(point1y-1>=0){
								hightway[intersectionX][point1y-1][l] = CyberWorldObjectGenerator.DIR_INTERSECTION;
							}
							if(point2y+1<y){
								hightway[intersectionX][point2y+1][l] = CyberWorldObjectGenerator.DIR_INTERSECTION;
							}
						}
					}
					//intersection
					if(highwayCutout_x&&highwayCutout_z){
						hightway[intersectionX][intersectionY][l] = CyberWorldObjectGenerator.DIR_INTERSECTION;
					}
				}
			}
			
		}
	}
	private void displayGrid() {
		System.out.println("STREET");
		for (int i = 0; i < y; i++) {
			for (int j = 0; j < x; j++) {
				if(city[j][i]==CyberWorldObjectGenerator.DIR_NORTH_SOUTH  ||  city[j][i]==CyberWorldObjectGenerator.DIR_EAST_WEST  ||  city[j][i]==CyberWorldObjectGenerator.DIR_INTERSECTION){
					System.out.print("x");
				}
				else if(city[j][i]==CyberWorldObjectGenerator.DIR_NOT_DETERMINED ){
					System.out.print("*");
				}
				else  if(city[j][i]==CyberWorldObjectGenerator.DIR_BUILDING ){
					System.out.print(" ");
				}
			}
			System.out.println("");
		}
		
		for(int l=0;l<3;l++){

			System.out.println("HIGHWAY : "+l);
			for (int i = 0; i < y; i++) {
				for (int j = 0; j < x; j++) {
					if(hightway[j][i][l]>0){
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
		/*
		for(int l=0;l<3;l++){

			System.out.println("building : "+l);
			for (int i = 0; i < y; i++) {
				for (int j = 0; j < x; j++) {
					if(building[j][i][l]>0){
						System.out.print(building[j][i][l]%10);
						//System.out.print("x");
					}
					else if(building[j][i][l]==CyberWorldObjectGenerator.DIR_S_BUILDING){
						System.out.print(building[j][i][l]);
					}
					else if(building[j][i][l]==CyberWorldObjectGenerator.DIR_M_BUILDING){
						System.out.print(building[j][i][l]);
					}
					else if(building[j][i][l]==CyberWorldObjectGenerator.DIR_L_BUILDING){
						System.out.print(building[j][i][l]);
					}
					else{
						System.out.print(" ");
					}
				}
				System.out.println("");
			}
		}	
		*/
		/*
		for(int l=0;l<3;l++){

			System.out.println("building_type : "+l);
			for (int i = 0; i < y; i++) {
				for (int j = 0; j < x; j++) {
					if(building[j][i][l]>0){
						System.out.print(building_type[j][i][l]%10);
						//System.out.print("x");
					}
					else if(building[j][i][l]==CyberWorldObjectGenerator.DIR_S_BUILDING){
						System.out.print(building_type[j][i][l]);
					}
					else if(building[j][i][l]==CyberWorldObjectGenerator.DIR_M_BUILDING){
						System.out.print(building_type[j][i][l]);
					}
					else if(building[j][i][l]==CyberWorldObjectGenerator.DIR_L_BUILDING){
						System.out.print(building_type[j][i][l]);
					}
					else{
						System.out.print(" ");
					}
				}
				System.out.println("");
			}
		}	
		*/
		for(int l=0;l<3;l++){

			System.out.println("building_struct : "+l);
			for (int i = 0; i < y; i++) {
				for (int j = 0; j < x; j++) {
					if(building_struct[j][i][l]>0){
						System.out.print(building_struct[j][i][l]%10);
						//System.out.print("x");
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
		int w =30;
		int h =30;
		Random rng = new Random();
		rng.setSeed(6666);
		CityStreetGenerator g = new CityStreetGenerator(w, h,rng,4,10,10,10,2,3,4,1,1,1);
		g.displayGrid();
		
	}
 
}