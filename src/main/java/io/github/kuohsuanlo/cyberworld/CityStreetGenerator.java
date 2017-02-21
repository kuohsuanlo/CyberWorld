package io.github.kuohsuanlo.cyberworld;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
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

/*   1  2  3  4       1 2 3  	1 2
 *   5  6  7  8		  4 5 6	    3 4
 *   9 10 11 12	      7 8 9
 *  13 14 15 16
 *		Large		  Medium    Small
 *
 *  struct
 */
public class CityStreetGenerator implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int x;
	private final int y;
	private final int[][] city;
	private final int[][] sign;
	private final int[][][] building;
	private final int[][][] building_type;
	private final int[][][] building_rotation;
	private final long[][][] building_rng_seeds;
	private final int[][][] building_struct;
	private final int[][][] highway;
    private final Random rng;
    private final int minBW;
    private final CyberWorldBiomeGenerator bg;


    public final int[] a_size ;
    public final double[] a_rate ;
    public final int[] a_build_num ;
    private final int[] a_build_code;
	
	public CityStreetGenerator(CyberWorldBiomeGenerator b, int x, int y, Random r, int mmbw,int s,int m,int l, int ss, int ms, int ls,double sr,double mr,double lr) {
		bg =b;
		this.x = x;
		this.y = y;
		city = new int[this.x][this.y];
		sign = new int[this.x][this.y];
		highway = new int[this.x][this.y][3];
		building = new int[this.x][this.y][3];
		building_type= new int[this.x][this.y][3];
		building_rotation= new int[this.x][this.y][3];
		building_rng_seeds= new long[this.x][this.y][3];
		building_struct=new int[this.x][this.y][3];
		rng = r;
		minBW = mmbw;
		
		a_size = new int[3];
		a_rate = new double[3];
		a_build_num = new int[3];
		a_build_code = new int[3];
		a_size[0]=Math.min(mmbw,ss);
		a_size[1]=Math.min(mmbw,ms);
		a_size[2]=Math.min(mmbw,ls);
		a_rate[0]=sr;
		a_rate[1]=mr;
		a_rate[2]=lr;
		a_build_num[0] = s;
		a_build_num[1] = m;
		a_build_num[2] = l;
		a_build_code[0]=CyberWorldObjectGenerator.DIR_S_BUILDING;
		a_build_code[1]=CyberWorldObjectGenerator.DIR_M_BUILDING;
		a_build_code[2]=CyberWorldObjectGenerator.DIR_L_BUILDING;
		
		
   		recursiveSplitting(0,0,x-1,y-1,1);
   		fillNotDeterminedRoad();


		
		
	}
	public static int[] c2abs_transform(int rx, int rz,int bound_x, int bound_z){
		rx+=bound_x*0.5;
		rz+=bound_z*0.5;
		if(rx<0){
			rx*=-1;
			//rx--;
		}
		if(rz<0){
			rz*=-1;
			//rz--;
		}
		int[] ans =  {rx%(bound_x),rz%(bound_z)};
		return ans;
	}
	private void fillNotDeterminedRoad(){
		for(int i=0;i<x;i++){
			for(int j=0;j<y;j++){
				if(city[i][j]==CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
					city[i][j] = CyberWorldObjectGenerator.DIR_BUILDING;
				}

				if(sign[i][j]==CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
					sign[i][j] = CyberWorldObjectGenerator.DIR_BUILDING;
				}
				for(int l=0;l<3;l++){
					if(highway[i][j][l]==CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
						highway[i][j][l] = CyberWorldObjectGenerator.DIR_BUILDING;
					}
				}
			}
		}
		
		
		for(int i=0+1;i<x-1;i++){
			for(int j=0+1;j<y-1;j++){
				if(city[i][j]!=CyberWorldObjectGenerator.DIR_BUILDING){
					city[i][j]=0;
					if(	city[i+1][j]!=CyberWorldObjectGenerator.DIR_BUILDING){
						city[i][j] +=1;
					}
					if(	city[i-1][j]!=CyberWorldObjectGenerator.DIR_BUILDING){
						city[i][j] +=2;
					}
					if(	city[i][j+1]!=CyberWorldObjectGenerator.DIR_BUILDING){
						city[i][j] +=4;
					}
					if(	city[i][j-1]!=CyberWorldObjectGenerator.DIR_BUILDING){
						city[i][j] +=8;
					}
				}
			}
		}
		
	}
	public int getRoadType(int rx, int rz){
		int[] chunk_coor = c2abs_transform(rx,rz,this.x,this.y);
		return city[chunk_coor[0]][chunk_coor[1]];
	}
	public int getSignType(int rx, int rz){
		int[] chunk_coor = c2abs_transform(rx,rz,this.x,this.y);
		return sign[chunk_coor[0]][chunk_coor[1]];
	}
	public int getHighwayType(int rx, int rz,int layer){
		int[] chunk_coor = c2abs_transform(rx+722*(layer+1),rz+1205*(layer+1),this.x,this.y);
		return highway[chunk_coor[0]][chunk_coor[1]][layer];
	}
	public int getBuilding(int rx, int rz,int layer){
		int[] chunk_coor = c2abs_transform(rx,rz,this.x,this.y);
		return building[chunk_coor[0]][chunk_coor[1]][layer];
	}
	public int getBuildingType(int rx, int rz,int layer){
		int[] chunk_coor = c2abs_transform(rx,rz,this.x,this.y);
		return building_type[chunk_coor[0]][chunk_coor[1]][layer];
	}
	public int getBuildingRotation(int rx, int rz,int layer){
		int[] chunk_coor = c2abs_transform(rx,rz,this.x,this.y);
		return building_rotation[chunk_coor[0]][chunk_coor[1]][layer];
	}
	public long getBuildingSeed(int rx, int rz,int layer){
		int[] chunk_coor = c2abs_transform(rx,rz,this.x,this.y);
		return building_rng_seeds[chunk_coor[0]][chunk_coor[1]][layer];
	}	
	public int getBuildingStruct(int rx, int rz,int layer){
		int[] chunk_coor = c2abs_transform(rx,rz,this.x,this.y);
		return building_struct[chunk_coor[0]][chunk_coor[1]][layer];
	}	
    void recursiveSplitting(int point1x, int point1y, int point2x, int point2y, int recursiveTimes){
		if(Math.abs(point1x-point2x+1)<=minBW  ||  Math.abs(point1y-point2y+1)<=minBW){
			
			point2x= Math.min(point2x+1,x);
			point2y= Math.min(point2y+1,y);
			
			for(int l=0;l<3;l++){

				//System.out.println(l+","+a_build_num[l]);
				if( rng.nextDouble()<a_rate[l]  &&  a_build_num[l]>0){
					
					for(int i=point1x;i<=point2x;i+=a_size[l]){
						for(int j=point1y;j<=point2y;j+=a_size[l]){
							if((Math.min(i+a_size[l],point2x) - i)>=a_size[l]  &&  (Math.min(j+a_size[l],point2y) - j)>=a_size[l]){
								int s_type = rng.nextInt(a_build_num[l]);
								int angle = rng.nextInt(4);
								long seed = rng.nextLong();
								int current_struct = 1;
								
								int isComplete = 0;
								int last_type =bg.generateType(i, j, false);
								//Still have cut structures..
								for(int s2=j;s2<Math.min(j+a_size[l],point2y);s2++){
									for(int s1=i;s1<Math.min(i+a_size[l],point2x);s1++){
										if(bg.generateType(s1, s2, false)==last_type){
											isComplete++;
										}
									}
								}
								if(isComplete==a_size[l]*a_size[l]){
									for(int s2=j;s2<Math.min(j+a_size[l],point2y);s2++){
										for(int s1=i;s1<Math.min(i+a_size[l],point2x);s1++){
											city[s1][s2] = CyberWorldObjectGenerator.DIR_BUILDING;
											building[s1][s2][l]=a_build_code[l];
											building_type[s1][s2][l]=s_type;
											building_rotation[s1][s2][l]=angle;
											building_rng_seeds[s1][s2][l]=seed;
											building_struct[s1][s2][l]=current_struct;
											current_struct++;
										}
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

					if(sign[i][intersectionY] ==  CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
						if(i>1 && i< x-1){
							sign[i][intersectionY-1]=CyberWorldObjectGenerator.SIGN_LEFT;
							sign[i][intersectionY]=CyberWorldObjectGenerator.DIR_EAST_WEST;
							sign[i][intersectionY+1]=CyberWorldObjectGenerator.SIGN_RIGHT;
						}
					}
					
				}
				for(int i=point1y;i<=point2y;i++){
					if(city[intersectionX][i] ==  CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
						city[intersectionX][i]=CyberWorldObjectGenerator.DIR_NORTH_SOUTH;	
					}

					if(sign[intersectionX][i] ==  CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
						if(i>1 && i< y-1){
							sign[intersectionX-1][i]=CyberWorldObjectGenerator.SIGN_UP;
							sign[intersectionX][i]=CyberWorldObjectGenerator.DIR_NORTH_SOUTH;
							sign[intersectionX+1][i]=CyberWorldObjectGenerator.SIGN_DOWN;
						}
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

			for(int l=0;l<3;l++){

				int x_highway_margin = Math.abs(point1x-point2x+1)-(minBW*(l+1));
				int y_highway_margin = Math.abs(point1y-point2y+1)-(minBW*(l+1));
				if(x_highway_margin>0 || y_highway_margin>0){
					
					boolean highwayCutout_x = rng.nextDouble()>0.33;
					boolean highwayCutout_z = rng.nextDouble()>0.33;
					if(highwayCutout_x){
						for(int i=point1x;i<=point2x;i++){
							if(highway[i][intersectionY][l] ==  CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
								highway[i][intersectionY][l]=CyberWorldObjectGenerator.DIR_EAST_WEST;
							}
						}
					}
					if(highwayCutout_z){
						for(int i=point1y;i<=point2y;i++){
							if(highway[intersectionX][i][l] ==  CyberWorldObjectGenerator.DIR_NOT_DETERMINED){
								highway[intersectionX][i][l]=CyberWorldObjectGenerator.DIR_NORTH_SOUTH;
							}
						}
					}
					//starting point & end point
					if(recursiveTimes!=1){
						if(highwayCutout_x){
							if(point1x-1>=0){
								highway[point1x-1][intersectionY][l] = CyberWorldObjectGenerator.DIR_INTERSECTION;
							}
							if(point2x+1<x){
								highway[point2x+1][intersectionY][l] = CyberWorldObjectGenerator.DIR_INTERSECTION;
							}
						}
						if(highwayCutout_z){
							if(point1y-1>=0){
								highway[intersectionX][point1y-1][l] = CyberWorldObjectGenerator.DIR_INTERSECTION;
							}
							if(point2y+1<y){
								highway[intersectionX][point2y+1][l] = CyberWorldObjectGenerator.DIR_INTERSECTION;
							}
						}
					}
					//intersection
					if(highwayCutout_x&&highwayCutout_z){
						highway[intersectionX][intersectionY][l] = CyberWorldObjectGenerator.DIR_INTERSECTION;
					}
				}
			}
			
		}
	}
	private void displayGrid() {
		System.out.println("----------------------------------------");
		for(int l=0;l<3;l++){
			for (int i = -100; i < 100; i++) {
				for (int j = -100; j < 100; j++) {
					//int tmp  = this.getBuildingStruct(i,j, l);
					
					//int tmp  = this.getBuildingType(i,j,l);
					int tmp  = this.getRoadType(i,j);
					if(tmp>0){
						System.out.print(Integer.toHexString(tmp));
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
		int w =200;
		int h =200;
		Random rng = new Random();
		rng.setSeed(1205);
		CyberWorldBiomeGenerator b = new CyberWorldBiomeGenerator(3,6);
		CityStreetGenerator g = new CityStreetGenerator(b,w, h,rng,20,3,3,3,2,4,18,1,1,1);
		g.displayGrid();
		
	}
 
}