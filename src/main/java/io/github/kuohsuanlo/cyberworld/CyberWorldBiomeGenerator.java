package io.github.kuohsuanlo.cyberworld;

import java.util.Random;

public class CyberWorldBiomeGenerator {
 
    private final float AMPLITUDE;
    private final int OCTAVES ;
    private static final float ROUGHNESS = 0.3f;
 

    private Random random;
    private int seed;
    private int xOffset = 0;
    private int zOffset = 0;
 
    public CyberWorldBiomeGenerator(Random rng, int biome_types,int oct) {
    	this.OCTAVES = oct;
    	this.AMPLITUDE  = (biome_types-1)*20;
    	this.random = rng;
        this.seed = random.nextInt(900000000);
    }
    
 
    public float generateHeight(int x, int z) {
        float total = 0;
        x = x+10000;
        z = z+10000;
        float d = (float) Math.pow(2, OCTAVES-1);
        for(int i=0;i<OCTAVES;i++){
            float freq = (float) (Math.pow(2, i) / d);
            float amp = (float) Math.pow(ROUGHNESS, i) * AMPLITUDE;
            total += getInterpolatedNoise((x+xOffset)*freq, (z + zOffset)*freq) * amp;
        }
        return total;
    }

    public int generateType(int x, int z) {
    	int height = Math.round(this.generateHeight(x, z));
    	
    	if(height<=0  &&  -1*height<=AMPLITUDE/6)
    		height=0;
    	
        return Math.round(height/10);
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
        random.setSeed(91205*x + 90722*z  + seed);
        //random.setSeed(x * 91205 + z * 90722 + seed);
        return random.nextFloat() * 2f - 1f;
    }
	public static void main(String[] args) {
		Random rng = new Random(1205);
		CyberWorldBiomeGenerator h = new CyberWorldBiomeGenerator(rng,12,5);

		for(int i=-50;i<50;i++){
			for(int j=-50;j<50;j++){

				rng.setSeed(1205*i+722*j);
				int biome_type = h.generateType(i,j);
				if(biome_type>=5){
					System.out.print("0");
		    	}
				else if(biome_type>=4){
					System.out.print("O");
		    	}
				else if(biome_type>=3){
					System.out.print("o");
		    	}
		    	else if(biome_type>=2){
					System.out.print("~");
		    	}
		    	else if(biome_type>=1){
					System.out.print("-");
		    	}
		    	else if(biome_type>=0){
		    		System.out.print(" ");
		    	}
		    	else{
		    		System.out.print("x");
		    	}
			}
			System.out.println();
		}
		
	}
 
}