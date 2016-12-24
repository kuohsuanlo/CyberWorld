package io.github.kuohsuanlo.cyberworld;

import static java.lang.System.arraycopy;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;


public class CyberWorldObjectGenerator{
	private Random rng;
	private Logger log = Logger.getLogger("Minecraft");
    private double RNG_chkxz(int chkx,int chkz){
		return  Math.abs(Math.PI*(chkx+chkz)*10-Math.floor(Math.PI*(chkx+chkz)*10));
    }
    
    public static final int DIR_EAST_WEST 		=1;
    public static final int DIR_NORTH_SOUTH		=2;
    public static final int DIR_INTERSECTION	=0;
    public static final int DIR_NOT_ROAD		=-1;
    private long worldSeed;

	//Paving Roads
    private static Material ROAD_SIDEWALK_MATERIAL_1 = Material.STEP;
    private static Material ROAD_MATERIAL_1 = Material.QUARTZ_BLOCK;

    private static int ROAD_MOD= 5;
    private static int ROAD_R= 0;
    private static int SEWER_MOD= 5;
    private static int SEWER_R_1= 0;
    private static int SEWER_R_2= 2;
    private static int SEWER_R_3= 3;
    
    private static int SEWER_TUNNEL_MOD= 3;
    private static int SEWER_TUNNEL_R= 0;

    private static int SEWER_GATE_MOD= 10;
    private static int SEWER_GATE_R= SEWER_R_1;
	
    //Paving High Roads
    private static int LAYER_1_HEIGHT = 64;
    private static int LAYER_2_HEIGHT = 94;
    private static int LAYER_3_HEIGHT = 124;

    private static int LAYER_1_MOD= 10;
	private static int LAYER_1_R= 5;
	
	private static int LAYER_2_MOD= 10;
	private static int LAYER_2_R= 0;
	
	private static int LAYER_3_MOD= 10;
	private static int LAYER_3_R= 5;
	
	private static int LAYER_1_WIDTH = 10;
	private static int LAYER_2_WIDTH = 10;
	private static int LAYER_3_WIDTH = 10;
	private static int LAYER_1_SPACE = (16-LAYER_1_WIDTH)/2;
	private static int LAYER_2_SPACE = (16-LAYER_2_WIDTH)/2;
	private static int LAYER_3_SPACE = (16-LAYER_3_WIDTH)/2;
	private static int LAYER_1_SW_WD = 1;
	private static int LAYER_2_SW_WD = 1;
	private static int LAYER_3_SW_WD = 1;
	
	private static int LAYER_1_SRT = 0+LAYER_1_SPACE;
	private static int LAYER_1_END = 15-LAYER_1_SPACE;
	private static int LAYER_1_SW_MIN_END = LAYER_1_SRT+LAYER_1_SW_WD;
	private static int LAYER_1_SW_MAX_END = LAYER_1_END-LAYER_1_SW_WD;


	private static int LAYER_2_SRT = 0+LAYER_2_SPACE;
	private static int LAYER_2_END = 15-LAYER_2_SPACE;
	private static int LAYER_2_SW_MIN_END = LAYER_2_SRT+LAYER_2_SW_WD;
	private static int LAYER_2_SW_MAX_END = LAYER_2_END-LAYER_2_SW_WD;


	private static int LAYER_3_SRT = 0+LAYER_3_SPACE;
	private static int LAYER_3_END = 15-LAYER_3_SPACE;
	private static int LAYER_3_SW_MIN_END = LAYER_3_SRT+LAYER_3_SW_WD;
	private static int LAYER_3_SW_MAX_END = LAYER_3_END-LAYER_3_SW_WD;
    
    
    public static final String WINDOWS_PATH="plugins\\CyberpunkCityGenerator\\schematics\\";
    
    private short[] layer;
    private byte[] layerDataValues;

    public static final int MAX_SPACE_HEIGHT = 256; // 0-255

    public ChunkData generateTerrain(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
    	
        for(int y=0;y<33;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			//Paving Ground
	    			if(y <1){
	    				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.BEDROCK);
		        	}
	    			else if(y <33){
	    				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.STONE);
		        	}
	    		}
	    	}
	    }  
        
        return chunkdata;
    	
    }
    public ChunkData generateRoadBuilding(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
    	
		//Paving Roads
		for(int y=33;y<34;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			if(calculateRoadDirection(chkx,chkz,ROAD_MOD,ROAD_R)==this.DIR_EAST_WEST ){
	        			if(z<=2  ||  z>=13){
	        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
	        			}
	    			}
	    			else if(calculateRoadDirection(chkx,chkz,ROAD_MOD,ROAD_R)==this.DIR_NORTH_SOUTH ){
	    				if(x<=2  ||  x>=13){
	        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
	        			}
	    			}
	    			else if(calculateRoadDirection(chkx,chkz,ROAD_MOD,ROAD_R)==this.DIR_INTERSECTION ){
	    				if((x<=2  ||  x>=13) && (z<=2  ||  z>=13)){
	        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
	        			}
	    			}
	    			else if(calculateRoadDirection(chkx,chkz,ROAD_MOD,ROAD_R)==this.DIR_NOT_ROAD ){
	    				
	    			}
	    		}
	    		
	    	}
	    }
		
        return chunkdata;
    	
    }
    public void paste(String schematicName, Vector origin, World world) {
        try {
            File dir = new File(WINDOWS_PATH + schematicName);
            
            EditSession editSession = new EditSession(new BukkitWorld(world), 64*64*256);
            SchematicFormat schematic = SchematicFormat.getFormat(dir);
            CuboidClipboard clipboard = schematic.load(dir);
 
            clipboard.paste(editSession,origin, true);
            editSession.flushQueue();
        } catch (DataException | IOException ex) {
            ex.printStackTrace();
        } catch (MaxChangedBlocksException ex) {
            ex.printStackTrace();
        }
    }
    private void setBlock(short[][] result, int x, int y, int z, short blkid){
        if (result[y >> 4] == null) {
            result[y >> 4] = new short[4096];
        }
        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid;
    }
    private int calculateRoadDirection(int chkx, int chkz, int mod,int r){
    	int chkxr=chkx%mod;
    	int chkzr=chkz%mod;
    	if(chkxr==r  &&  chkzr==r){
    		return DIR_INTERSECTION;	
    	}
    	else if(chkxr==r  &&  chkzr!=r){
        		return DIR_NORTH_SOUTH;		
        }
    	else if(chkxr!=r  &&  chkzr==r){
    		return DIR_EAST_WEST ;				 
    	}
		return DIR_NOT_ROAD;					
    }
    /*
	public short[][] generateObject(World world, int chkx, int chkz, int mod,int r){
		
		int maxHeight = world.getMaxHeight();
        
        if (layer.length > maxHeight)
        {
            short[] newLayer = new short[maxHeight];
            arraycopy(layer, 0, newLayer, 0, maxHeight);
            layer = newLayer;
        }
		
		short[][] result = new short[maxHeight / 16][]; // 16x16x16 chunks
		
        layerDataValues = null;
        
        layer = new short[MAX_SPACE_HEIGHT];
        layer[0] = (short)Material.BEDROCK.getId();
        Arrays.fill(layer, 1, 2, (short)Material.STONE.getId());
        Arrays.fill(layer, 2, 33, (short)Material.STONE.getId());
        

		for(int y=0;y<33;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			//Paving Ground
	    			if(y >=31 && y <33){
	    				setBlock(result,x,y,z,layer[y]);
		        	}
	    		}
	    	}
	    }  
	    

		int tunnel_shift = (int) (Math.round( rng.nextDouble()*0)+8);
		int tunnel_height = (int) (Math.round( rng.nextDouble()*3)+4);
		int tunnel_width = (int) (Math.round( rng.nextDouble()*1)+3);
		
		int sewer_pipe_width = 5;
		int sewer_pipe_thick = 2;
		int sewer_pipe_height= 16;
		int pillar_width = 3;
	    //Building Sewer Layout
	    for(int y=0;y<33;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			if(y ==0){
	    				setBlock(result,x,y,z,layer[y]);
	    			}
	    			else if(y ==1){
	    				setBlock(result,x,y,z,layer[y]);
	    			}
	    			else if(y >=2 && y <31){
	    				
	    				//Building Sewer Pipe, Sewer Ground
		        		if ( (chkx%SEWER_MOD==SEWER_R_1  ) || (chkz%SEWER_MOD==SEWER_R_1  )  ||  (chkx%SEWER_MOD==SEWER_R_2  ) || (chkz%SEWER_MOD==SEWER_R_2  )  ||  
		        				 (chkx%SEWER_MOD==SEWER_R_3  ) || (chkz%SEWER_MOD==SEWER_R_3  )){
		        			double d = rng.nextDouble();
		        			
		        			//Ground
		        			if(y==2  &&  d<0.03){
		        				setBlock(result,x,y,z,(short)Material.LAVA.getId());
		        			}
		        			else if(y==2  &&  d>=0.03  &&  d<0.07){
		        				setBlock(result,x,y,z,(short)Material.COBBLESTONE.getId());
		        			}
		        			else if(y==2  &&  d>=0.07  &&  d<=0.2){
		        				setBlock(result,x,y,z,(short)Material.MOSSY_COBBLESTONE.getId());
		        			}
		        			else if(y==2  &&  d>=0.2 ){
		        				setBlock(result,x,y,z,(short)Material.WATER.getId());
		        			}
		        			
		        			
		        			
		            	}
		        		//Building Piller
		        		else{
		        			if((x<0+pillar_width ||  x>15-pillar_width)  &&  (z<0+pillar_width ||  z>15-pillar_width)){
		        				setBlock(result,x,y,z,(short)Material.BRICK.getId());
		        			}
		        			else{
			        			double d = rng.nextDouble();
			        			if(d<=0.05){
			        				setBlock(result,x,y,z,(short)Material.COBBLESTONE.getId());
			        			}
			        			else if(d>0.05  &&  d<=0.1){
			        				setBlock(result,x,y,z,(short)Material.MOSSY_COBBLESTONE.getId());
			        			}
			        			else if(d>0.15  &&  d<=0.2){
			        				setBlock(result,x,y,z,(short)Material.COBBLESTONE_STAIRS.getId());
			        			}
			        			else if(d>0.2  &&  d<=0.25){
			        				setBlock(result,x,y,z,(short)Material.COBBLE_WALL.getId());
			        			}
			        			else if(d>0.25  &&  d<=0.30){
			        				setBlock(result,x,y,z,(short)Material.STEP.getId());
			        			}
			        			else{
			        				setBlock(result,x,y,z,layer[y]);
			        			}
			        			
		        			}

		        		}
		        		
		        		//building pipe
		        		
	    				if(calculateRoadDirection(chkx,chkz,ROAD_MOD,ROAD_R)==this.DIR_NORTH_SOUTH){
	    					double d = rng.nextDouble();
		        			//Pipe Shell
		        			if(y>=3  &&  ((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<sewer_pipe_width*sewer_pipe_width  && 
		        					((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))>=(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)      ){
		        				
		        				if(z==0  || z==15){
		        					setBlock(result,x,y,z,(short)Material.IRON_FENCE.getId());
		        				}
		        				else if( d<0.02){
			        				setBlock(result,x,y,z,(short)Material.COAL_ORE.getId());
			        			}
		        				else if( d>=0.02  &&  d<0.04){
			        				setBlock(result,x,y,z,(short)Material.IRON_ORE.getId());
			        			}
			        			else if( d>=0.04  &&  d<0.07){
			        				setBlock(result,x,y,z,(short)Material.MOSSY_COBBLESTONE.getId());
			        			}
			        			else if( d>=0.07  &&  d<=0.2){
			        				setBlock(result,x,y,z,(short)Material.COBBLESTONE.getId());
			        			}
			        			else if( d>=0.2 ){
			        				setBlock(result,x,y,z,(short)Material.STONE.getId());
			        			}
		        			}
		        			//Outside the shell
		        			else if(y>=3  &&  ((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))>=sewer_pipe_width*sewer_pipe_width   ){
		        				if( d<0.02){
			        				setBlock(result,x,y,z,(short)Material.AIR.getId());
			        			}
		        			}

	    				}
	    				else if(calculateRoadDirection(chkx,chkz,ROAD_MOD,ROAD_R)==this.DIR_EAST_WEST){
	    					double d = rng.nextDouble();
		        			//Pipe Shell
		        			if(y>=3  &&  ((z-7.5)*(z-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<sewer_pipe_width*sewer_pipe_width  && 
		        					((z-7.5)*(z-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))>=(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)      ){
		        				
		        				if(x==0  || x==15){
		        					setBlock(result,x,y,z,(short)Material.IRON_FENCE.getId());
		        				}
		        				else if( d<0.02){
			        				setBlock(result,x,y,z,(short)Material.COAL_ORE.getId());
			        			}
		        				else if( d>=0.02  &&  d<0.04){
			        				setBlock(result,x,y,z,(short)Material.IRON_ORE.getId());
			        			}
			        			else if( d>=0.04  &&  d<0.07){
			        				setBlock(result,x,y,z,(short)Material.MOSSY_COBBLESTONE.getId());
			        			}
			        			else if( d>=0.07  &&  d<=0.2){
			        				setBlock(result,x,y,z,(short)Material.COBBLESTONE.getId());
			        			}
			        			else if( d>=0.2 ){
			        				setBlock(result,x,y,z,(short)Material.STONE.getId());
			        			}
		        			}
		        			//Outside the shell
		        			else if(y>=3  &&  ((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))>=sewer_pipe_width*sewer_pipe_width   ){
		        				if( d<0.02){
			        				setBlock(result,x,y,z,(short)Material.AIR.getId());
			        			}
		        			}

	    				}
	    				else if(calculateRoadDirection(chkx,chkz,ROAD_MOD,ROAD_R)==this.DIR_INTERSECTION){
	    					double d = rng.nextDouble();
		        			//Pipe Shell
		        			if(y>=3  &&  ((z-7.5)*(z-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<sewer_pipe_width*sewer_pipe_width  && 
		        					((z-7.5)*(z-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))>=(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)) {
		        				
		        				if(z==0  || z==15  ||  x==0  ||  x ==15){
		        					setBlock(result,x,y,z,(short)Material.IRON_FENCE.getId());
		        				}
		        				else if( d<0.02){
			        				setBlock(result,x,y,z,(short)Material.COAL_ORE.getId());
			        			}
		        				else if( d>=0.02  &&  d<0.04){
			        				setBlock(result,x,y,z,(short)Material.IRON_ORE.getId());
			        			}
			        			else if( d>=0.04  &&  d<0.07){
			        				setBlock(result,x,y,z,(short)Material.MOSSY_COBBLESTONE.getId());
			        			}
			        			else if( d>=0.07  &&  d<=0.2){
			        				setBlock(result,x,y,z,(short)Material.COBBLESTONE.getId());
			        			}
			        			else if( d>=0.2 ){
			        				setBlock(result,x,y,z,(short)Material.STONE.getId());
			        			}
		        			}
		        			
		        			if(y>=3  &&  ((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<sewer_pipe_width*sewer_pipe_width  && 
		        					((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))>=(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)){
		        				if(z==0  || z==15  ||  x==0  ||  x ==15){
		        					setBlock(result,x,y,z,(short)Material.IRON_FENCE.getId());
		        				}
		        				else if( d<0.02){
			        				setBlock(result,x,y,z,(short)Material.COAL_ORE.getId());
			        			}
		        				else if( d>=0.02  &&  d<0.04){
			        				setBlock(result,x,y,z,(short)Material.IRON_ORE.getId());
			        			}
			        			else if( d>=0.04  &&  d<0.07){
			        				setBlock(result,x,y,z,(short)Material.MOSSY_COBBLESTONE.getId());
			        			}
			        			else if( d>=0.07  &&  d<=0.2){
			        				setBlock(result,x,y,z,(short)Material.COBBLESTONE.getId());
			        			}
			        			else if( d>=0.2 ){
			        				setBlock(result,x,y,z,(short)Material.STONE.getId());
			        			}
		        			}
		        			
		        			
		        			//Remove 4 walls on intersection
		        			if(((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)){
		        				setBlock(result,x,y,z,(short)Material.AIR.getId());		        			
		        			}
		        			if(((z-7.5)*(z-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)){
		        				setBlock(result,x,y,z,(short)Material.AIR.getId());		        			
		        			}
		        			
		        			
		        			//Upward pipe
		        			if(y>=sewer_pipe_height+sewer_pipe_width-sewer_pipe_thick  && ((x-7.5)*(x-7.5)+(z-7.5)*(z-7.5))<sewer_pipe_width*sewer_pipe_width  && 
		        					((x-7.5)*(x-7.5)+(z-7.5)*(z-7.5))>=(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)      ){
		        				
		        				if( d<0.02){
			        				setBlock(result,x,y,z,(short)Material.COAL_ORE.getId());
			        			}
		        				else if( d>=0.02  &&  d<0.04){
			        				setBlock(result,x,y,z,(short)Material.IRON_ORE.getId());
			        			}
			        			else if( d>=0.04  &&  d<0.07){
			        				setBlock(result,x,y,z,(short)Material.MOSSY_COBBLESTONE.getId());
			        			}
			        			else if( d>=0.07  &&  d<=0.2){
			        				setBlock(result,x,y,z,(short)Material.COBBLESTONE.getId());
			        			}
			        			else if( d>=0.2 ){
			        				setBlock(result,x,y,z,(short)Material.STONE.getId());
			        			}
		        			}
	    				}
	        		}
	    			

					//Upward pipe exit,entry to road
	    			
	    			//Cross 
	    			if(y==32  &&  calculateRoadDirection(chkx,chkz,ROAD_MOD,ROAD_R)==this.DIR_INTERSECTION){
	    				double d = rng.nextDouble();
	        			if(((x-7.5)*(x-7.5)+(z-7.5)*(z-7.5))<sewer_pipe_width*sewer_pipe_width  && 
	        					((x-7.5)*(x-7.5)+(z-7.5)*(z-7.5))>=(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)      ){
	        				setBlock(result,x,y,z,(short)Material.STEP.getId());
	        			}
	        			else if(((x-7.5)*(x-7.5)+(z-7.5)*(z-7.5))<(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)){
	        				setBlock(result,x,y,z,(short)Material.IRON_TRAPDOOR.getId());
	        			}
	    			}
	    			
	    			//upward
	    			if(y>sewer_pipe_height  &&  y<=31  &&  calculateRoadDirection(chkx,chkz,ROAD_MOD,ROAD_R)==this.DIR_INTERSECTION){
	    				if(((x-7.5)*(x-7.5)+(z-7.5)*(z-7.5))<(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)){
	        				setBlock(result,x,y,z,(short)Material.AIR.getId());
	        			}
	    			}
		        	
	    		}
	    	}
	    }    	       

	    

	  //Building Sewer Tunnel
	    for(int y=3;y<31;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			

	    	        //Building Sewer Tunnel

	    			double d =  rng.nextDouble();
	    			if (! ((chkx%SEWER_MOD==SEWER_R_1  ) || (chkz%SEWER_MOD==SEWER_R_1  )  ||  (chkx%SEWER_MOD==SEWER_R_2  ) || (chkz%SEWER_MOD==SEWER_R_2  ) ||
	    					 (chkx%SEWER_MOD==SEWER_R_3  ) || (chkz%SEWER_MOD==SEWER_R_3  ))){
	    				//if(calculateRoadDirection(chkx,chkz,SEWER_MOD,SEWER_R_1)==this.DIR_NOT_ROAD){

	            			if(z>=0+tunnel_shift  &&  z<=tunnel_width+tunnel_shift){
	            				if( ((y-(tunnel_height))* (y-(tunnel_height)) + (z-7.5)*(z-7.5))<  (tunnel_width)*(tunnel_width)){
	            				//if(y>=tunnel_height-tunnel_width/2  &&  y<=tunnel_height+tunnel_width/2){
	            					if(d<=0.01){
	            						setBlock(result,x,y,z,(short)Material.WEB.getId());
	            					}
	            					else if(d>0.03  &&  d<=0.05){
	            						setBlock(result,x,y,z,(short)Material.VINE.getId());
	            					}
	            					else{
	            						setBlock(result,x,y,z,(short)Material.AIR.getId());
	            					}
	                				
	            				}
	            			}
	    				//}
	    			}
	        		if (! ((chkx%SEWER_MOD==SEWER_R_1  ) || (chkz%SEWER_MOD==SEWER_R_1  )  ||  (chkx%SEWER_MOD==SEWER_R_2  ) || (chkz%SEWER_MOD==SEWER_R_2  )
	        				||  (chkx%SEWER_MOD==SEWER_R_3  ) || (chkz%SEWER_MOD==SEWER_R_3  ))){
	    				//if( calculateRoadDirection(chkx,chkz,SEWER_MOD,SEWER_R_1)==this.DIR_NORTH_SOUTH){
		        			if(x>=0+tunnel_shift  &&  x<=tunnel_width+tunnel_shift){
	            				if( ((y-(tunnel_height))* (y-(tunnel_height)) + (x-7.5)*(x-7.5))<  (tunnel_width)*(tunnel_width)){
	            				//if(y>=tunnel_height-tunnel_width/2  &&  y<=tunnel_height+tunnel_width/2){
	            					if(d<=0.01){
	            						setBlock(result,x,y,z,(short)Material.WEB.getId());
	            					}
	            					else if(d>0.03  &&  d<=0.05){
	            						setBlock(result,x,y,z,(short)Material.VINE.getId());
	            					}
	            					else{
	            						setBlock(result,x,y,z,(short)Material.AIR.getId());
	            					}
	                				
	            				}
		        			}
	    				//}
	    			}
	        		
	    			
	    			
	    		}
	    	}
	    }    

		//Paving Roads
		for(int y=33;y<34;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			if(calculateRoadDirection(chkx,chkz,ROAD_MOD,ROAD_R)==this.DIR_EAST_WEST ){
	        			if(z<=2  ||  z>=13){
	        				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
	        			}
	    			}
	    			else if(calculateRoadDirection(chkx,chkz,ROAD_MOD,ROAD_R)==this.DIR_NORTH_SOUTH ){
	    				if(x<=2  ||  x>=13){
	        				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
	        			}
	    			}
	    			else if(calculateRoadDirection(chkx,chkz,ROAD_MOD,ROAD_R)==this.DIR_INTERSECTION ){
	    				if((x<=2  ||  x>=13) && (z<=2  ||  z>=13)){
	        				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
	        			}
	    			}
	    			else if(calculateRoadDirection(chkx,chkz,ROAD_MOD,ROAD_R)==this.DIR_NOT_ROAD ){
	    				
	    			}
	    		}
	    		
	    	}
	    }
		

	    //Building Sewer-Road Entry (Should be after Road paving)
		
	    if ( (calculateRoadDirection(chkx,chkz,SEWER_GATE_MOD,SEWER_R_1)==this.DIR_EAST_WEST  ) ||
	    		(calculateRoadDirection(chkx,chkz,SEWER_GATE_MOD,SEWER_R_1)==this.DIR_NORTH_SOUTH )  ){
	    	
			int x;
	 		int z;
		 	x=0;
	 		z=0;
	   		for(int y=3;y<33;y++){
	   			setBlock(result,x,y,z,(short)Material.LADDER.getId());
	 		}
	   		setBlock(result,x,33,z,(short)Material.TRAP_DOOR.getId());

	 		x=15;
	 		z=15;
	   		for(int y=3;y<33;y++){
	   			setBlock(result,x,y,z,(short)Material.LADDER.getId());
	 		}
	   		setBlock(result,x,33,z,(short)Material.TRAP_DOOR.getId());
		}
	        	        

	    //Building Sewer gate
	    for(int y=0;y<256;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    	
	        		if(calculateRoadDirection(chkx,chkz,SEWER_GATE_MOD,SEWER_GATE_R)==this.DIR_INTERSECTION ){
	    				if((y>=5  &&  y<31)  &&  ( (x<=0  ||  x>=15)  ||  (z<=0  ||  z>=15) )){
	    					double d = rng.nextDouble();
	    					
	    					if((x==0 || x==15) && (z==0  ||  z==15)  &&  d>=0.2){
	        					setBlock(result,x,y,z,(short)Material.MOSSY_COBBLESTONE.getId());
	    					}
	    					else if(d>=0.4){
	        					setBlock(result,x,y,z,(short)Material.IRON_FENCE.getId());
	    					}
	    				}
	    			}
	    		}
	    	}
	    }    	
	    

	    
	    //Paving High Roads
	    for(int y=0;y<256;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){

					//Layer_1
		    		if ( (chkx%LAYER_1_MOD==LAYER_1_R  ) || (chkz%LAYER_1_MOD==LAYER_1_R) ){
		    			//Road
	    				if(y >=LAYER_1_HEIGHT && y<LAYER_1_HEIGHT+2){
	    					if(calculateRoadDirection(chkx,chkz,LAYER_1_MOD,LAYER_1_R)==this.DIR_EAST_WEST ){
	    						if(z>=LAYER_1_SRT  &&  z<=LAYER_1_END){
	        						setBlock(result,x,y,z,ROAD_MATERIAL_1);
	        					}
	          	    		}
	          	    		else if(calculateRoadDirection(chkx,chkz,LAYER_1_MOD,LAYER_1_R)==this.DIR_NORTH_SOUTH ){
	          	    			if(x>=LAYER_1_SRT  &&  x<=LAYER_1_END){
	        						setBlock(result,x,y,z,ROAD_MATERIAL_1);
	        					}
	          	    		}
	          	    		else if(calculateRoadDirection(chkx,chkz,LAYER_1_MOD,LAYER_1_R)==this.DIR_INTERSECTION ){
	          	    			if( (x>=LAYER_1_SRT  &&  x<=LAYER_1_END)  ||  (z>=LAYER_1_SRT  &&  z<=LAYER_1_END)){
	        						setBlock(result,x,y,z,ROAD_MATERIAL_1);
	        					}
	          	    		}
	          	    		else if(calculateRoadDirection(chkx,chkz,LAYER_1_MOD,LAYER_1_R)==this.DIR_NOT_ROAD ){
	          	    			
	          	    		}
	    					
	    	    			
	    	        	}
	    				//Sidewalk
	    				else if(y >=LAYER_1_HEIGHT+2 && y<LAYER_1_HEIGHT+3){
	           	    		if(calculateRoadDirection(chkx,chkz,LAYER_1_MOD,LAYER_1_R)==this.DIR_EAST_WEST ){
		                			if((z<=LAYER_1_SW_MIN_END  &&  z>=LAYER_1_SRT)  ||  (z>=LAYER_1_SW_MAX_END  &&  z<=LAYER_1_END)){
		                				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
		                			}
	          	    		}
	          	    		else if(calculateRoadDirection(chkx,chkz,LAYER_1_MOD,LAYER_1_R)==this.DIR_NORTH_SOUTH ){
	          	    			if((x<=LAYER_1_SW_MIN_END  &&  x>=LAYER_1_SRT)  ||  (x>=LAYER_1_SW_MAX_END  &&  x<=LAYER_1_END)){
		                    			setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
		                    		}
	          	    		}
	          	    		else if(calculateRoadDirection(chkx,chkz,LAYER_1_MOD,LAYER_1_R)==this.DIR_INTERSECTION ){
		                			if(((x<=LAYER_1_SW_MIN_END )  ||  (x>=LAYER_1_SW_MAX_END )) && ((z<=LAYER_1_SW_MIN_END  )  ||  (z>=LAYER_1_SW_MAX_END ))  &&  !((x<LAYER_1_SRT  ||  x>LAYER_1_END)  &&  (z<LAYER_1_SRT  ||  z>LAYER_1_END))){
		                				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
		                			}

	          	    		}
	          	    		else if(calculateRoadDirection(chkx,chkz,LAYER_1_MOD,LAYER_1_R)==this.DIR_NOT_ROAD ){
	          	    			
	          	    		}
	    				}
	    	    	}
		    		//Layer_2
	    	    	if ( (chkx%LAYER_2_MOD==LAYER_2_R  ) || (chkz%LAYER_2_MOD==LAYER_2_R) ){

	    	    		if(y >=LAYER_2_HEIGHT && y<LAYER_2_HEIGHT+2){
							if(calculateRoadDirection(chkx,chkz,LAYER_2_MOD,LAYER_2_R)==this.DIR_EAST_WEST ){
	    						if(z>=LAYER_2_SRT  &&  z<=LAYER_2_END){
	        						setBlock(result,x,y,z,ROAD_MATERIAL_1);
	        					}
	          	    		}
	          	    		else if(calculateRoadDirection(chkx,chkz,LAYER_2_MOD,LAYER_2_R)==this.DIR_NORTH_SOUTH ){
	          	    			if(x>=LAYER_2_SRT  &&  x<=LAYER_2_END){
	        						setBlock(result,x,y,z,ROAD_MATERIAL_1);
	        					}
	          	    		}
	          	    		else if(calculateRoadDirection(chkx,chkz,LAYER_2_MOD,LAYER_2_R)==this.DIR_INTERSECTION ){
	          	    			if( (x>=LAYER_2_SRT  &&  x<=LAYER_2_END)  ||  (z>=LAYER_2_SRT  &&  z<=LAYER_2_END)){
	        						setBlock(result,x,y,z,ROAD_MATERIAL_1);
	        					}
	          	    		}
	          	    		else if(calculateRoadDirection(chkx,chkz,LAYER_2_MOD,LAYER_2_R)==this.DIR_NOT_ROAD ){
	          	    			
	          	    		}
	    					
	    	    			
	    	        	}
	    				//Sidewalk
	    				else if(y >=LAYER_2_HEIGHT+2 && y<LAYER_2_HEIGHT+3){
	    					if(calculateRoadDirection(chkx,chkz,LAYER_2_MOD,LAYER_2_R)==this.DIR_EAST_WEST ){
		                			if((z<=LAYER_2_SW_MIN_END  &&  z>=LAYER_2_SRT)  ||  (z>=LAYER_2_SW_MAX_END  &&  z<=LAYER_2_END)){
		                				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
		                			}
	          	    		}
	          	    		else if(calculateRoadDirection(chkx,chkz,LAYER_2_MOD,LAYER_2_R)==this.DIR_NORTH_SOUTH ){
	          	    			if((x<=LAYER_2_SW_MIN_END  &&  x>=LAYER_2_SRT)  ||  (x>=LAYER_2_SW_MAX_END  &&  x<=LAYER_2_END)){
		                    			setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
		                    		}
	          	    		}
	          	    		else if(calculateRoadDirection(chkx,chkz,LAYER_2_MOD,LAYER_2_R)==this.DIR_INTERSECTION ){
		                			if(((x<=LAYER_2_SW_MIN_END )  ||  (x>=LAYER_2_SW_MAX_END )) && ((z<=LAYER_2_SW_MIN_END  )  ||  (z>=LAYER_2_SW_MAX_END ))  &&  !((x<LAYER_2_SRT  ||  x>LAYER_2_END)  &&  (z<LAYER_2_SRT  ||  z>LAYER_2_END))){
		                				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
		                			}

	          	    		}
	          	    		else if(calculateRoadDirection(chkx,chkz,LAYER_2_MOD,LAYER_2_R)==this.DIR_NOT_ROAD ){
	          	    			
	          	    		}
	    				}
	    	    	}
		    		
		    		//Layer_3
	    	    	if ( (chkx%LAYER_3_MOD==LAYER_3_R  ) || (chkz%LAYER_3_MOD==LAYER_3_R) ){ 
	    	    		if(y >=LAYER_3_HEIGHT && y<LAYER_3_HEIGHT+2){
							if(calculateRoadDirection(chkx,chkz,LAYER_3_MOD,LAYER_3_R)==this.DIR_EAST_WEST ){
	    						if(z>=LAYER_3_SRT  &&  z<=LAYER_3_END){
	        						setBlock(result,x,y,z,ROAD_MATERIAL_1);
	        					}
	          	    		}
	          	    		else if(calculateRoadDirection(chkx,chkz,LAYER_3_MOD,LAYER_3_R)==this.DIR_NORTH_SOUTH ){
	          	    			if(x>=LAYER_3_SRT  &&  x<=LAYER_3_END){
	        						setBlock(result,x,y,z,ROAD_MATERIAL_1);
	        					}
	          	    		}
	          	    		else if(calculateRoadDirection(chkx,chkz,LAYER_3_MOD,LAYER_3_R)==this.DIR_INTERSECTION ){
	          	    			if( (x>=LAYER_3_SRT  &&  x<=LAYER_3_END)  ||  (z>=LAYER_3_SRT  &&  z<=LAYER_3_END)){
	        						setBlock(result,x,y,z,ROAD_MATERIAL_1);
	        					}
	          	    		}
	          	    		else if(calculateRoadDirection(chkx,chkz,LAYER_3_MOD,LAYER_3_R)==this.DIR_NOT_ROAD ){
	          	    			
	          	    		}
	    	    			
	    	        	}
	    				//Sidewalk
	    				else if(y >=LAYER_3_HEIGHT+2 && y<LAYER_3_HEIGHT+3){
	    					if(calculateRoadDirection(chkx,chkz,LAYER_3_MOD,LAYER_3_R)==this.DIR_EAST_WEST ){
		                			if((z<=LAYER_3_SW_MIN_END  &&  z>=LAYER_3_SRT)  ||  (z>=LAYER_3_SW_MAX_END  &&  z<=LAYER_3_END)){
		                				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
		                			}
	          	    		}
	          	    		else if(calculateRoadDirection(chkx,chkz,LAYER_3_MOD,LAYER_3_R)==this.DIR_NORTH_SOUTH ){
	          	    			if((x<=LAYER_3_SW_MIN_END  &&  x>=LAYER_3_SRT)  ||  (x>=LAYER_3_SW_MAX_END  &&  x<=LAYER_3_END)){
		                    			setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
		                    		}
	          	    		}
	          	    		else if(calculateRoadDirection(chkx,chkz,LAYER_3_MOD,LAYER_3_R)==this.DIR_INTERSECTION ){
		                			if(((x<=LAYER_3_SW_MIN_END )  ||  (x>=LAYER_3_SW_MAX_END )) && ((z<=LAYER_3_SW_MIN_END  )  ||  (z>=LAYER_3_SW_MAX_END ))  &&  !((x<LAYER_3_SRT  ||  x>LAYER_3_END)  &&  (z<LAYER_3_SRT  ||  z>LAYER_3_END))){
		                				setBlock(result,x,y,z,ROAD_SIDEWALK_MATERIAL_1);
		                			}

	          	    		}
	          	    		else if(calculateRoadDirection(chkx,chkz,LAYER_3_MOD,LAYER_3_R)==this.DIR_NOT_ROAD ){
	          	    			
	          	    		}
	    				}
	    	    	
	    	    	}
	    	    	//Layer other
	    	    	else{
		    		
	 	           
	    	    	}
		    	
	    			
	    		}
	    	}
	    }       

	    //Building Generation
		if (! ( (chkx%ROAD_MOD==ROAD_R  ) || (chkz%ROAD_MOD==ROAD_R) )){
			
			double d = RNG_chkxz(chkx/5,chkz/5);
			int building_max_height = (int) (Math.round( d*160)+40);
			int building_type = (int) (Math.round(d*4));
			int building_width = (int) (Math.round( d*8)+8);
			

			double d2= rng.nextDouble();
			int building_shift_left = (int) Math.round( d2 * (16-building_width)/2 ) ;
			int building_shift_right= (16-building_width)/2 - building_shift_left;
			
			int building_shift_up   = (int) Math.round( d2 * (16-building_width)/2 ) ;
			int building_shift_down = (16-building_width)/2 - building_shift_up;
			
			
			building_type=rng.nextInt(5); //exclusive 5
			switch(building_type){
				case 0:	
					for(int x=0+building_shift_up;x<16-building_shift_down;x++){
						for(int z=0+building_shift_left;z<16-building_shift_right;z++){
							for(int y=33;y<33+building_max_height;y++){
								if(z==0+building_shift_left  ||  z==16-building_shift_right-1  ||  x==0+building_shift_up  ||  x==16-building_shift_down-1||
										y==33+building_max_height-1){
									setBlock(result,x,y,z, (short)Material.NETHER_BRICK.getId());
								}
								
							}
						}
					}
					break;
				case 1:
					for(int x=0+building_shift_up;x<16-building_shift_down;x++){
						for(int z=0+building_shift_left;z<16-building_shift_right;z++){
							for(int y=33;y<33+building_max_height;y++){
								if(z==0+building_shift_left  ||  z==16-building_shift_right-1  ||  x==0+building_shift_up  ||  x==16-building_shift_down-1  ||
										y==33+building_max_height-1){
									setBlock(result,x,y,z, (short)Material.SMOOTH_BRICK.getId());
								}
							}
						}
					}
					break;
				case 2:
					for(int x=0+building_shift_up;x<16-building_shift_down;x++){
						for(int z=0+building_shift_left;z<16-building_shift_right;z++){
							for(int y=33;y<33+building_max_height;y++){
								if(z==0+building_shift_left  ||  z==16-building_shift_right-1  ||  x==0+building_shift_up  ||  x==16-building_shift_down-1  ||
										y==33+building_max_height-1){
									setBlock(result,x,y,z, (short)Material.BRICK.getId());
								}
							}
						}
					}
					break;
				case 3:
					for(int x=0+building_shift_up;x<16-building_shift_down;x++){
						for(int z=0+building_shift_left;z<16-building_shift_right;z++){
							for(int y=33;y<33+building_max_height;y++){

								if(z==0+building_shift_left  ||  z==16-building_shift_right-1  ||  x==0+building_shift_up  ||  x==16-building_shift_down-1  ||
										y==33+building_max_height-1){

									setBlock(result,x,y,z, (short)Material.SANDSTONE.getId());
								}
							}
						}
					}
					break;
				case 4:
					for(int x=0+building_shift_up;x<16-building_shift_down;x++){
						for(int z=0+building_shift_left;z<16-building_shift_right;z++){
							for(int y=33;y<33+building_max_height;y++){
								if(z==0+building_shift_left  ||  z==16-building_shift_right-1  ||  x==0+building_shift_up  ||  x==16-building_shift_down-1  ||
										y==33+building_max_height-1){

									setBlock(result,x,y,z, (short)Material.COBBLESTONE.getId());
								}
							}
						}
					}
					break;

				case 5:
					
					//Location loc = new Location(world,0,100,0);
					Vector origin  = new Vector(chkx ,100, chkz);
				
					
					int schematic_type = (int) (Math.round( rng.nextDouble()*20));
					schematic_type = 14;
					switch(schematic_type){
						case 0:
							paste("high_1.schematic",origin,world);
							break;
						case 1:
							paste("high_1.schematic",origin,world);
							break;
						case 2:
							paste("high_2.schematic",origin,world);
							break;
						case 3:
							paste("high_3.schematic",origin,world);
							break;
						case 4:
							paste("high_4.schematic",origin,world);
							break;
						case 5:
							paste("high_5.schematic",origin,world);
							break;
						case 6:
							paste("high_6.schematic",origin,world);
							break;
						case 7:
							paste("high_7.schematic",origin,world);
							break;
						case 8:
							paste("mid_1.schematic",origin,world);
							break;
						case 9:
							paste("mid_2.schematic",origin,world);
							break;
						case 10:
							paste("mid_3.schematic",origin,world);
							break;
						case 11:
							paste("mid_4.schematic",origin,world);
							break;
						case 12:
							paste("mid_5.schematic",origin,world);
							break;
						case 13:
							paste("mid_6.schematic",origin,world);
							break;
						case 14:
							paste("low_1.schematic",origin,world);
							break;
						case 15:
							paste("low_2.schematic",origin,world);
							break;
						case 16:
							paste("low_3.schematic",origin,world);
							break;
						case 17:
							paste("low_4.schematic",origin,world);
							break;
						case 18:
							paste("low_5.schematic",origin,world);
							break;
						case 19:
							paste("low_6.schematic",origin,world);
							break;
						default:
							break;
					}
					break;	  
				default:
					break;
			}
		}
		return result;	
	}
	*/
	
}
 		