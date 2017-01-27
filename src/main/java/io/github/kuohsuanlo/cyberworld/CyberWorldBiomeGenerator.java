package io.github.kuohsuanlo.cyberworld;

import java.util.Random;

public class CyberWorldBiomeGenerator {
 
    private final float AMPLITUDE = 10;
    private final int BIOME_TYPES;
    private final int OCTAVES ;
    private static final float ROUGHNESS = 0.3f;
 

    private Random rng;
    private long testingSeed= 791205;
    private int seed;
    private int xOffset = 0;
    private int zOffset = 0;
 
    public CyberWorldBiomeGenerator(int biome_types,int oct) {
    	this.BIOME_TYPES = biome_types;
    	this.OCTAVES = oct;
    	rng = new Random();
		rng.setSeed(testingSeed);
        this.seed = rng.nextInt(900000000);
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
    	
    	//hotspot version.
    	/*
    	for(int i=0;i<Math.pow(2,this.BIOME_TYPES);i++){
    		if(this.generateHeight(current_x, current_z,transform)>0){
    			current_type++;
    		}
    		current_x+=79;
    		current_z+=80;
    	}*/
    	
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
        rng.setSeed(91205*x + 90722*z  + seed);
        //random.setSeed(x * 91205 + z * 90722 + seed);
        return rng.nextFloat() * 2f - 1f;
    }
	public static void main(String[] args) {

		CyberWorldBiomeGenerator h = new CyberWorldBiomeGenerator(3,3);

		for(int i=-100;i<25;i++){
			for(int j=-100;j<25;j++){
				int biome_type = h.generateType(i,j,true);
				
				System.out.print(Integer.toHexString(biome_type)+",");
		    	
			}
			System.out.println();
		}
		
		
	}
 
}