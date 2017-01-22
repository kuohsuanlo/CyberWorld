package io.github.kuohsuanlo.cyberworld;

import java.util.Random;

public class TerrainHeightGenerator {
 
    private final float AMPLITUDE;
    private final int OCTAVES ;
    private final float GROUND;
    private static final float ROUGHNESS = 0.3f;
 

    private Random random;
    private int seed;
    private int xOffset = 0;
    private int zOffset = 0;
    private int xOffset_n = 0;
    private int zOffset_n = 0;
 
    

			
    public TerrainHeightGenerator(Random rng, int maximum_height,int oct,int ground_level) {
    	this.OCTAVES = oct;
    	this.AMPLITUDE  = maximum_height*2;
    	this.GROUND = ground_level;
    	this.random = rng;
        this.seed = random.nextInt(900000000);

       
    }
    public int generateHeight(int x, int z, boolean transform) {
    	
        float ratio = 0;
        float total=0;
        
        int[] ans;
        if(transform){
        	ans = CityStreetGenerator.c2abs_transform(x, z);
        	x = ans[0];
        	z = ans[1];
        }
        else{
        	x +=16000;
        	z +=16000;
        }
        
        int RATIO_OCTAVES = Math.abs(OCTAVES-1);
        int HEIGHT_OCTAVES = OCTAVES;
        float d = (float) Math.pow(2, RATIO_OCTAVES-1);
        for(int i=0;i<RATIO_OCTAVES;i++){
            float freq = (float) (Math.pow(2, i) / d);
            float amp = (float) Math.pow(ROUGHNESS, i) * AMPLITUDE;
            ratio += getInterpolatedNoise((x+xOffset)*freq, (z + zOffset)*freq) * amp;
        }
        

        float d_n = (float) Math.pow(2, HEIGHT_OCTAVES-1);
        for(int i=0;i<HEIGHT_OCTAVES;i++){
            float freq = (float) (Math.pow(2, i) / d_n);
            float amp = (float) Math.pow(ROUGHNESS, i) * AMPLITUDE;
            total += getInterpolatedNoise((x+xOffset_n)*freq, (z + zOffset_n)*freq) * amp;
        }
    	
		
        float h_ratio = (AMPLITUDE/2-ratio)/(AMPLITUDE/2);
		
		
        total= total*h_ratio; 
		
		if(total<GROUND){
			total += (GROUND-total)/1.5;
		}
        return (int) Math.round(total);
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
        float theta = (float) (blend * Math.PI);
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
        random.setSeed(91205*x + 90722*z  + seed);
        //random.setSeed(x * 91205 + z * 90722 + seed);
        return random.nextFloat() * 2f - 1f;
    }
	public static void main(String[] args) {
		Random rng = new Random(1205);
		TerrainHeightGenerator h = new TerrainHeightGenerator(rng,80,5,60);

		for(int i=-25;i<25;i++){
			for(int j=-25;j<25;j++){

				int height = Math.round(h.generateHeight(i,j,true))/10;
				
				System.out.print(Integer.toHexString(height));
				
			}
			System.out.println();
		}
		/*
		for(int i=-10;i<10;i++){
			for(int j=-10;j<10;j++){

				rng.setSeed(91205*((i+1000)/3)+9722*((j+1000)/3));
				System.out.print(rng.nextInt(4));
			}
			System.out.println();
		}
		*/
		
		
	}
 
}