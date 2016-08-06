
package io.github.kuohsuanlo.cyberpunkcity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import static java.lang.System.arraycopy;
import static java.lang.System.inheritedChannel;

public class CyberpunkCityChunkGenerator extends ChunkGenerator
{
    private Logger log = Logger.getLogger("Minecraft");
    private short[] layer;
    private byte[] layerDataValues;
    public static final int MAX_SPACE_HEIGHT = 256; // 0-255
    public static final int DIR_EAST_WEST 		=1;
    public static final int DIR_NORTH_SOUTH		=2;
    public static final int DIR_INTERSECTION	=0;
    public static final int DIR_NOT_ROAD		=-1;
    private long worldSeed;
    private Random rng;

    public CyberpunkCityChunkGenerator(){
        layerDataValues = null;
        layer = new short[MAX_SPACE_HEIGHT];
        layer[0] = (short)Material.BEDROCK.getId();
        Arrays.fill(layer, 1, 2, (short)Material.STONE.getId());
        Arrays.fill(layer, 2, 32, (short)Material.STONE.getId());
        Arrays.fill(layer, 32, 33, (short)Material.STONE.getId());
    }
    private int calculateRoadDirection(int chkx, int chkz, int mod){
    	int chkxr=chkx%mod;
    	int chkzr=chkz%mod;
    	if(chkxr==0  &&  chkzr==0){
    		return DIR_INTERSECTION;	
    	}
    	else if(chkxr==0  &&  chkzr!=0){
        		return DIR_NORTH_SOUTH;		
        }
    	else if(chkxr!=0  &&  chkzr==0){
    		return DIR_EAST_WEST ;				 
    	}
		return DIR_NOT_ROAD;					
    }
    @Override
    public short[][] generateExtBlockSections(World world, Random random, int chkx, int chkz, BiomeGrid biomes){
        int maxHeight = world.getMaxHeight();
        //worldSeed = world.getSeed();
        //rng = new Random(worldSeed);
        rng = new Random();
        if (layer.length > maxHeight)
        {
            log.warning("[CleanroomGenerator] Error, chunk height " + layer.length + " is greater than the world max height (" + maxHeight + "). Trimming to world max height.");
            short[] newLayer = new short[maxHeight];
            arraycopy(layer, 0, newLayer, 0, maxHeight);
            layer = newLayer;
        }
        short[][] result = new short[maxHeight / 16][]; // 16x16x16 chunks
        
        
        //Layout constructing
        for(int y=0;y<256;y++){
        	for(int x=0;x<16;x++){
        		for(int z=0;z<16;z++){
        			if(y ==0){
        				setBlock(result,x,y,z,layer[y]);
        			}
        			else if(y ==1){
        				setBlock(result,x,y,z,layer[y]);
        			}
        			else if(y >=2 && y <32){
		        		if ( (chkx%3==0  ) || (chkz%3==0  )){
		        			if(y==2){
		        				setBlock(result,x,y,z,(short)Material.WATER.getId());
		        			}
		        			else{
		        				setBlock(result,x,y,z,(short)Material.AIR.getId());
		        			}
		        			
		            	}
		        		else{
		        			setBlock(result,x,y,z,layer[y]);
		        		}
		        	}
        			//Paving Ground
        			else if(y >=32 && y <33){
        				setBlock(result,x,y,z,layer[y]);
		        	}
		        	
        		}
        	}
        }
        
        //Building Generation
		if (! ( (chkx%4==0  ) || (chkz%4==0) )){
			int building_max_height = (int) (Math.round( rng.nextDouble()*160)+40);
			int building_type = (int) (Math.round( rng.nextDouble()*4));
			
			int building_width = (int) (Math.round( rng.nextDouble()*8)+8);
			
			
			int building_shift_left = (int) Math.round( rng.nextDouble() * (16-building_width)/2 ) ;
			int building_shift_right= (16-building_width)/2 - building_shift_left;
			
			int building_shift_up   = (int) Math.round( rng.nextDouble() * (16-building_width)/2 ) ;
			int building_shift_down = (16-building_width)/2 - building_shift_up;
			
			switch(building_type){
				case 0:	
					for(int x=0+building_shift_up;x<16-building_shift_down;x++){
						for(int z=0+building_shift_left;z<16-building_shift_right;z++){
							for(int y=33;y<33+building_max_height;y++){
								setBlock(result,x,y,z, (short)Material.NETHER_BRICK.getId());
							}
						}
    				}
					break;
				case 1:
					for(int x=0+building_shift_up;x<16-building_shift_down;x++){
						for(int z=0+building_shift_left;z<16-building_shift_right;z++){
							for(int y=33;y<33+building_max_height;y++){
								setBlock(result,x,y,z, (short)Material.SMOOTH_BRICK.getId());
							}
						}
    				}
					break;
				case 2:
					for(int x=0+building_shift_up;x<16-building_shift_down;x++){
						for(int z=0+building_shift_left;z<16-building_shift_right;z++){
							for(int y=33;y<33+building_max_height;y++){
								setBlock(result,x,y,z, (short)Material.BRICK.getId());
							}
						}
    				}
					break;
				case 3:
					for(int x=0+building_shift_up;x<16-building_shift_down;x++){
						for(int z=0+building_shift_left;z<16-building_shift_right;z++){
							for(int y=33;y<33+building_max_height;y++){
								setBlock(result,x,y,z, (short)Material.SANDSTONE.getId());
							}
						}
    				}
					break;
				case 4:
					for(int x=0+building_shift_up;x<16-building_shift_down;x++){
						for(int z=0+building_shift_left;z<16-building_shift_right;z++){
							for(int y=33;y<33+building_max_height;y++){
								setBlock(result,x,y,z, (short)Material.COBBLESTONE.getId());
							}
						}
    				}
    				break;
				default:
					break;
    		}
    	}
        
		 //Paving Roads
        short ROAD_SIDEWALK_MATERIAL_1 = (short)Material.STEP.getId();
        short ROAD_MATERIAL_1 = (short)Material.QUARTZ_BLOCK.getId();
        
		for(int y=33;y<34;y++){
        	for(int x=0;x<16;x++){
        		for(int z=0;z<16;z++){
        			if(calculateRoadDirection(chkx,chkz,4)==this.DIR_EAST_WEST ){
            			if(z<=2  ||  z>=13){
            				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
            			}
        			}
        			else if(calculateRoadDirection(chkx,chkz,4)==this.DIR_NORTH_SOUTH ){
        				if(x<=2  ||  x>=13){
            				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
            			}
        			}
        			else if(calculateRoadDirection(chkx,chkz,4)==this.DIR_INTERSECTION ){
        				if((x<=2  ||  x>=13) && (z<=2  ||  z>=13)){
            				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
            			}
        			}
        			else if(calculateRoadDirection(chkx,chkz,4)==this.DIR_NOT_ROAD ){
        				
        			}
        		}
        		
        	}
        }
		
		
        //Paving High Roads
		int LAYER_1_HEIGHT = 80;
		int LAYER_2_HEIGHT = 160;
		int LAYER_3_HEIGHT = 240;

		int LAYER_1_MOD= 4;
		int LAYER_2_MOD= 8;
		int LAYER_3_MOD= 8;
        for(int y=0;y<256;y++){
        	for(int x=0;x<16;x++){
        		for(int z=0;z<16;z++){

    				//Layer_1
    	    		if ( (chkx%LAYER_1_MOD==0  ) || (chkz%LAYER_1_MOD==0) ){
    	    			//Road
        				if(y >=LAYER_1_HEIGHT && y<LAYER_1_HEIGHT+2){
        					if(calculateRoadDirection(chkx,chkz,LAYER_1_MOD)==this.DIR_EAST_WEST ){
        						if(z>=2  &&  z<=13){
            						setBlock(result,x,y,z,ROAD_MATERIAL_1);
            					}
              	    		}
              	    		else if(calculateRoadDirection(chkx,chkz,LAYER_1_MOD)==this.DIR_NORTH_SOUTH ){
              	    			if(x>=2  &&  x<=13){
            						setBlock(result,x,y,z,ROAD_MATERIAL_1);
            					}
              	    		}
              	    		else if(calculateRoadDirection(chkx,chkz,LAYER_1_MOD)==this.DIR_INTERSECTION ){
              	    			if( (x>=2  &&  x<=13)  ||  (z>=2  &&  z<=13)){
            						setBlock(result,x,y,z,ROAD_MATERIAL_1);
            					}
              	    		}
              	    		else if(calculateRoadDirection(chkx,chkz,LAYER_1_MOD)==this.DIR_NOT_ROAD ){
              	    			
              	    		}
        					
        	    			
        	        	}
        				//Sidewalk
        				else if(y >=LAYER_1_HEIGHT+2 && y<LAYER_1_HEIGHT+3){
               	    		if(calculateRoadDirection(chkx,chkz,LAYER_1_MOD)==this.DIR_EAST_WEST ){
  	                			if((z<=3  &&  z>=2)  ||  (z>=12  &&  z<=13)){
  	                				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
  	                			}
              	    		}
              	    		else if(calculateRoadDirection(chkx,chkz,LAYER_1_MOD)==this.DIR_NORTH_SOUTH ){
              	    			if((x<=3  &&  x>=2)  ||  (x>=12  &&  x<=13)){
  	                    			setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
  	                    		}
              	    		}
              	    		else if(calculateRoadDirection(chkx,chkz,LAYER_1_MOD)==this.DIR_INTERSECTION ){
  	                			if(((x<=3 )  ||  (x>=12 )) && ((z<=3  )  ||  (z>=12 ))  &&  !((x<2  ||  x>13)  &&  (z<2  ||  z>13))){
  	                				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
  	                			}

              	    		}
              	    		else if(calculateRoadDirection(chkx,chkz,LAYER_1_MOD)==this.DIR_NOT_ROAD ){
              	    			
              	    		}
        				}
        	    	}
    	    		//Layer_2
        	    	if ( (chkx%LAYER_2_MOD==0  ) || (chkz%LAYER_2_MOD==0) ){

        	    		if(y >=LAYER_2_HEIGHT && y<LAYER_2_HEIGHT+2){
        	    			if(calculateRoadDirection(chkx,chkz,LAYER_2_MOD)==this.DIR_EAST_WEST ){
        						if(z>=2  &&  z<=13){
            						setBlock(result,x,y,z,ROAD_MATERIAL_1);
            					}
              	    		}
              	    		else if(calculateRoadDirection(chkx,chkz,LAYER_2_MOD)==this.DIR_NORTH_SOUTH ){
              	    			if(x>=2  &&  x<=13){
            						setBlock(result,x,y,z,ROAD_MATERIAL_1);
            					}
              	    		}
              	    		else if(calculateRoadDirection(chkx,chkz,LAYER_2_MOD)==this.DIR_INTERSECTION ){
              	    			if( (x>=2  &&  x<=13)  ||  (z>=2  &&  z<=13)){
            						setBlock(result,x,y,z,ROAD_MATERIAL_1);
            					}
              	    		}
              	    		else if(calculateRoadDirection(chkx,chkz,LAYER_2_MOD)==this.DIR_NOT_ROAD ){
              	    			
              	    		}
        					
        	    			
        	        	}
        				//Sidewalk
        				else if(y >=LAYER_2_HEIGHT+2 && y<LAYER_2_HEIGHT+3){
               	    		if(calculateRoadDirection(chkx,chkz,LAYER_2_MOD)==this.DIR_EAST_WEST ){
  	                			if((z<=3  &&  z>=2)  ||  (z>=12  &&  z<=13)){
  	                				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
  	                			}
              	    		}
              	    		else if(calculateRoadDirection(chkx,chkz,LAYER_2_MOD)==this.DIR_NORTH_SOUTH ){
              	    			if((x<=3  &&  x>=2)  ||  (x>=12  &&  x<=13)){
  	                    				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
  	                    			}
              	    		}
              	    		else if(calculateRoadDirection(chkx,chkz,LAYER_2_MOD)==this.DIR_INTERSECTION ){
  	                			if(((x<=3 )  ||  (x>=12 )) && ((z<=3  )  ||  (z>=12 ))  &&  !((x<2  ||  x>13)  &&  (z<2  ||  z>13))){
  	                				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
  	                			}

              	    		}
              	    		else if(calculateRoadDirection(chkx,chkz,LAYER_2_MOD)==this.DIR_NOT_ROAD ){
              	    			
              	    		}
        				}
        	    	}
    	    		
    	    		//Layer_3
        	    	if ( (chkx%LAYER_3_MOD==0  ) || (chkz%LAYER_3_MOD==0) ){ 
        	    		if(y >=LAYER_3_HEIGHT && y<LAYER_3_HEIGHT+2){
        	    			if(calculateRoadDirection(chkx,chkz,LAYER_3_MOD)==this.DIR_EAST_WEST ){
        						if(z>=2  &&  z<=13){
            						setBlock(result,x,y,z,ROAD_MATERIAL_1);
            					}
              	    		}
              	    		else if(calculateRoadDirection(chkx,chkz,LAYER_3_MOD)==this.DIR_NORTH_SOUTH ){
              	    			if(x>=2  &&  x<=13){
            						setBlock(result,x,y,z,ROAD_MATERIAL_1);
            					}
              	    		}
              	    		else if(calculateRoadDirection(chkx,chkz,LAYER_3_MOD)==this.DIR_INTERSECTION ){
              	    			if( (x>=2  &&  x<=13)  ||  (z>=2  &&  z<=13)){
            						setBlock(result,x,y,z,ROAD_MATERIAL_1);
            					}
              	    		}
              	    		else if(calculateRoadDirection(chkx,chkz,LAYER_3_MOD)==this.DIR_NOT_ROAD ){
              	    			
              	    		}
        					
        	    			
        	        	}
        				//Sidewalk
        				else if(y >=LAYER_3_HEIGHT+2 && y<LAYER_3_HEIGHT+3){
               	    		if(calculateRoadDirection(chkx,chkz,LAYER_3_MOD)==this.DIR_EAST_WEST ){
  	                			if((z<=3  &&  z>=2)  ||  (z>=12  &&  z<=13)){
  	                				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
  	                			}
              	    		}
              	    		else if(calculateRoadDirection(chkx,chkz,LAYER_3_MOD)==this.DIR_NORTH_SOUTH ){
              	    			if((x<=3  &&  x>=2)  ||  (x>=12  &&  x<=13)){
  	                    				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
  	                    			}
              	    		}
              	    		else if(calculateRoadDirection(chkx,chkz,LAYER_3_MOD)==this.DIR_INTERSECTION ){
  	                			if(((x<=3 )  ||  (x>=12 )) && ((z<=3  )  ||  (z>=12 ))  &&  !((x<2  ||  x>13)  &&  (z<2  ||  z>13))){
  	                				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
  	                			}
              	    		}
              	    		else if(calculateRoadDirection(chkx,chkz,LAYER_3_MOD)==this.DIR_NOT_ROAD ){
              	    			
              	    		}
        				}
        	    	
        	    	}
        	    	//Layer other
        	    	else{
    	    		
     	           
        	    	}
    	    	
        			
        		}
        	}
        }       


        return result;
    }
    private void setBlock(short[][] result, int x, int y, int z, short blkid){
        if (result[y >> 4] == null) {
            result[y >> 4] = new short[4096];
        }
        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid;
    }
    @Override
    public List<BlockPopulator> getDefaultPopulators(World world)
    {
        if (layerDataValues != null)
        {
            return Arrays.asList((BlockPopulator)new CyberpunkCityBlockPopulator(layerDataValues));
        } else
        {
            // This is the default, but just in case default populators change to stock minecraft populators by default...
            return new ArrayList<BlockPopulator>();
        }
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random)
    {
        if (!world.isChunkLoaded(0, 0))
        {
            world.loadChunk(0, 0);
        }

        if ((world.getHighestBlockYAt(0, 0) <= 0) && (world.getBlockAt(0, 0, 0).getType() == Material.AIR)) // SPACE!
        {
            return new Location(world, 0, 64, 0); // Lets allow people to drop a little before hitting the void then shall we?
        }

        return new Location(world, 0, world.getHighestBlockYAt(0, 0), 0);
    }
}