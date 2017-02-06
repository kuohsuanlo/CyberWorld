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
	private final int[][][] merged;
	private final int[][][] content;
    private Random rng;
    private final int minBW;
    private final int sign_interval =1;
    public final double SIGN_COVERAGE;
    public final int[] a_size ;
    public final int[] a_build_num ;
    private final int[] a_build_code;
    

    private int seed; 
    public final int OCTAVES;
    public final int BIOME_TYPES = 4;
    public final float ROUGHNESS ;
	private final float AMPLITUDE = 10;
    private int xOffset = 0;
    private int zOffset = 0;
    
	public SignGenerator(Random r, int total_num, int x, int y, int mmbw,int ss, int ms, int ls, double cover, int octaves,float rough) {
		this.x = x;
		this.y = y;
		merged = new int[this.x][this.y][total_num];
		content = new int[this.x][this.y][total_num];
		rng = r;
		minBW = mmbw;
		OCTAVES = octaves;
		this.seed = rng.nextInt(791205);
        
		a_size = new int[3];
		a_build_num = new int[3];
		a_build_code = new int[3];
		a_size[0]=Math.min(mmbw,ss);
		a_size[1]=Math.min(mmbw,ms);
		a_size[2]=Math.min(mmbw,ls);
		a_build_num[0] = 1;
		a_build_num[1] = 1;
		a_build_num[2] = 1;
		a_build_code[0]=CyberWorldObjectGenerator.DIR_S_BUILDING;
		a_build_code[1]=CyberWorldObjectGenerator.DIR_M_BUILDING;
		a_build_code[2]=CyberWorldObjectGenerator.DIR_L_BUILDING;

		SIGN_COVERAGE = cover;
		ROUGHNESS = rough;
		for(int i=0;i<total_num;i++){
			recursiveSplitting(0,0,x-1,y-1,1,i);
		}
		
	}
	public int getMerged(int rx, int ry,int l){
		int[] chunk_coor = {rx,ry};
		return merged[chunk_coor[0]][chunk_coor[1]][l];
	}

	public int getSignContent(int rx, int ry,int l){
		int[] chunk_coor = {rx,ry};
		return content[chunk_coor[0]][chunk_coor[1]][l];
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
							
							if((Math.min(i+width,point2x) - i)>=width  &&  (Math.min(j+height,point2y) - j)>=height  &&  rng.nextDouble()<SIGN_COVERAGE){
								hasPasted=true;
							
								
								int height_max = Math.min(j+height,point2y);
								int width_max = Math.min(i+width,point2x);
								
								int frame_type = Material.GLOWSTONE.getId();
								switch(rng.nextInt(4)){
									case 0:
										frame_type=Material.SEA_LANTERN.getId();
										break;
									case 1:
										frame_type=Material.GLOWSTONE.getId();
										break;
									case 2:
										frame_type=Material.IRON_FENCE.getId();
										break;
									case 3:
										frame_type=Material.FENCE.getId();
										break;
								}

								int start_shift_x = rng.nextInt(width/2);
								int start_shift_y = rng.nextInt(height/2);
								int i_start = i+start_shift_x;
								int j_start = j+start_shift_y;
								for(int s1=i_start;s1<width_max;s1++){
									for(int s2=j_start;s2<height_max;s2++){
										if((s2==j_start  ||  s2==height_max-1)  ||  (s1==i_start  ||  s1==width_max-1) ){
											
											merged[s1][s2][set_number]=frame_type;
											
										}
										else{
											merged[s1][s2][set_number]=Material.WOOL.getId();
											content[s1][s2][set_number]=this.generateType(s1, s2, false);
											
										}
										
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
			for (int j = 0; j < h; j++) {
				for (int i = 0; i < w; i++) {
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
		System.out.println("----------------------------------------");
		for(int l=0;l<set_number;l++){

			System.out.println("building_struct : "+l);
			for (int j = 0; j < h; j++) {
				for (int i = 0; i < w; i++) {
					int tmp  = this.getSignContent(i,j,l);
					//int tmp  = this.getBuildingStruct(i,j, l);
					
					if(tmp>=0){
						System.out.print(Integer.toHexString(tmp)+".");
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
		int ht =100;
		int wd =20;
		int set_number =1;
		Random rng = new Random();
		SignGenerator g = new SignGenerator(rng,set_number, wd,ht,wd,wd,wd,wd,0.7,1,1);
		g.displayGrid(wd,ht,set_number);
		
	}
	
    public float generateHeight(int x, int z, boolean transform) {
        float total = 0;
        int[] ans;
        if(transform){
        	ans = CityStreetGenerator.c2abs_transform(x, z,1000,1000);
        	x = ans[0];
        	z = ans[1];
        }
		
        
        float d = (float) Math.pow(2, OCTAVES-1);
        for(int i=0;i<OCTAVES;i++){
            float freq = (float) (Math.pow(2, i) / d);
            float amp = (float) Math.pow(ROUGHNESS, i) * AMPLITUDE;
            total += getInterpolatedNoise((x+xOffset)*freq, (z + zOffset)*freq) * amp;
        }
        return total;
    }

    public int generateType(int x, int z, boolean transform) {
    	int current_type=0;
    	int current_x=x;
    	int current_z=z;
    	
    	//No-hotspot
    	for(int i=0;i<this.BIOME_TYPES;i++){
    		if(this.generateHeight(current_x, current_z,transform)>0){
    			current_type+=Math.pow(2, i);
    		}
    		current_x+=79;
    		current_z+=125;
    	}
    	
    	
        return current_type;
    }
    private float getInterpolatedNoise(float x, float z){
        int intX = (int) x;
        int intZ = (int) z;
        float fracX = x - intX;
        float fracZ = z - intZ;
         
        float v1 = getSmoothNoise(intX, intZ);
        float v2 = getSmoothNoise(intX + 1, intZ);
        float v3 = getSmoothNoise(intX, intZ + 1);
        float v4 = getSmoothNoise(intX + 1, intZ + 1);
        float i1 = interpolate(v1, v2, fracX);
        float i2 = interpolate(v3, v4, fracX);
        return interpolate(i1, i2, fracZ);
    }
     
    private float interpolate(float a, float b, float blend){
        double theta = blend * Math.PI;
        float f = (float)(1f - Math.cos(theta)) * 0.5f;
        return a * (1f - f) + b * f;
    }
 
    private float getSmoothNoise(int x, int z) {
        float corners = (getNoise(x - 1, z - 1) + getNoise(x + 1, z - 1) + getNoise(x - 1, z + 1)
                + getNoise(x + 1, z + 1)) / 16f;
        float sides = (getNoise(x - 1, z) + getNoise(x + 1, z) + getNoise(x, z - 1)
                + getNoise(x, z + 1)) / 8f;
        float center = getNoise(x, z) / 4f;
        return corners + sides + center;
    }
 
    private float getNoise(int x, int z) {
        rng.setSeed(1205*x + 722*z  + seed);
        //random.setSeed(x * 91205 + z * 90722 + seed);
        return rng.nextFloat() * 2f - 1f;
    }
    
 
}