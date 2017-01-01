package io.github.kuohsuanlo.cyberworld;

import static java.lang.System.arraycopy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.material.MaterialData;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;


public class CyberWorldObjectGenerator{
	private Random rng;
	private Logger log = Logger.getLogger("Minecraft");
    public CityStreetGenerator cg = null;
    private long testingSeed= 1205;
	private final static int schematicNumber = 44;
	private int sz_s=2;
	private int sz_m=3;
	private int sz_l=4;
	public CyberWorldObjectGenerator(){
		//generating the city layout
		rng = new Random();
		rng.setSeed(testingSeed);
		readSchematic();
		cg = new CityStreetGenerator(500,500,rng,4,cc_list_s.size(),cc_list_m.size(),cc_list_l.size(),sz_s,sz_m,sz_l,1,1,1);
		
	}

	
    public final static int DIR_EAST_WEST 		=1;
    public final static int DIR_NORTH_SOUTH		=2;
    public final static int DIR_INTERSECTION	=3;
    public final static int DIR_NOT_ROAD		=-1;
    public final static int DIR_BUILDING		=-2;
    public final static int DIR_S_BUILDING		=-3;
    public final static int DIR_M_BUILDING		=-4;
    public final static int DIR_L_BUILDING		=-5;
    public final static int DIR_NOT_DETERMINED  =0;

	//Paving Roads
    private static Material ROAD_SIDEWALK_MATERIAL_1 = Material.STEP;
    private static Material ROAD_MATERIAL_1 = Material.QUARTZ_BLOCK;


	
    //Paving High Roads
    private static int LAYER_1_HEIGHT = 64;
    private static int LAYER_2_HEIGHT = 94;
    private static int LAYER_3_HEIGHT = 124;
	
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
    
    
    public static final String WINDOWS_PATH="plugins\\CyberWorld\\schematics\\";
   
    public static final int MAX_SPACE_HEIGHT = 256; // 0-255
    
    
	private CuboidClipboard[] cc_list ;
	private ArrayList<CuboidClipboard> cc_list_s = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_m = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_l = new ArrayList<CuboidClipboard>();
	private void readSchematic(){
		cc_list = new CuboidClipboard[schematicNumber];
		for(int i =0;i<schematicNumber;i++){
			cc_list[i] = Schematic.getSchematic(i+".schematic");
			if(cc_list[i].getLength()<=sz_s*16  && cc_list[i].getWidth()<=sz_s*16){
				cc_list_s.add(cc_list[i]);
			}
			else if(cc_list[i].getLength()<=sz_m*16  && cc_list[i].getWidth()<=sz_m*16){
				cc_list_m.add(cc_list[i]);
			}
			else if(cc_list[i].getLength()<=sz_l*16  && cc_list[i].getWidth()<=sz_l*16){
				cc_list_l.add(cc_list[i]);
			}
			else{
				System.out.print("[CyberWorld] : Error on schematic = "+i+"/ size too large : "+cc_list[i].getWidth()+","+cc_list[i].getLength());
			}
		}
		System.out.print("[CyberWorld] : Final numbers of read schematic(Small/Medium/Large) = "+cc_list_s.size()+"/"+cc_list_m.size()+"/"+cc_list_l.size());
		
		
	}
    public ChunkData generateTerrain(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
        for(int y=0;y<33;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			//Paving Ground
	    			if(y <1){
	    				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.BEDROCK);
		        	}
	    			else if(y <3){
	    				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.STONE);
		        	}
	    			else if(y ==32  ||  y ==31){
	    				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.STONE);
		        	}
	    		}
	    	}
	    }
        return chunkdata;
    }
    public ChunkData generateRoad(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
		//Paving Roads
		for(int y=33;y<34;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			//Here need to import the map so we could what direction to create the road.
	    			
	    			if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_EAST_WEST ){
	        			if(z<=2  ||  z>=13){
	        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
	        			}
	    			}
	    			else if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
	    				if(x<=2  ||  x>=13){
	        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
	        			}
	    			}
	    			else if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
	    				if((x<=2  ||  x>=13) && (z<=2  ||  z>=13)){
	        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
	        			}
	    			}
	    			else if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_NOT_ROAD ){
        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
	    			}
	    		}
	    		
	    	}
	    }
		
		//road line
		int y=32;
    	for(int x=0;x<16;x++){
    		for(int z=0;z<16;z++){
    			//Here need to import the map so we could what direction to create the road.
    			
    			if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_EAST_WEST ){
        			if((z == 5  ||  z==10)  &&  (x%4==1  ||  x%4==2) ){
        				chunkdata.setBlock(x, y, z, Material.STAINED_CLAY.getId(), (byte) 0x4 );
        			}
    			}
    			else if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
    				if((x == 5  ||  x==10)  &&  (z%4==1  ||  z%4==2) ){
    					chunkdata.setBlock(x, y, z, Material.STAINED_CLAY.getId(), (byte) 0x4 );
        			}
    			}
    			else if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
    				
    			}
    		}
    		
	    	
	    }
        return chunkdata;
    	
    }
    public ChunkData generateSewer(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
    	 //Building Sewer Layout

		int sewer_pipe_width = 5;
		int sewer_pipe_thick = 2;
		int sewer_pipe_height= 16;
		int pillar_width = 3;
	    for(int y=0;y<33;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			if(y >=2 && y <31){
	    				double d = rng.nextDouble();
	        			double r = rng.nextDouble();
	    				//Building Sewer Pipe, Sewer Ground
		        		if ( cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_EAST_WEST ){ //ROAD
		        			d = (d*Math.abs(z-7)/5);
		        			//Ground 0 1 2 //sewer road// 13 14 15
		        			if( y==2  && z>=5 && z<=9){
		        				if(r>0.5)
		        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.MOSSY_COBBLESTONE);
		        				else
		        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COBBLESTONE);
		        			}
		        			else if(y==2  &&  d<0.2){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.MOSSY_COBBLESTONE);
		        			}
		        			else if(y==2  &&  d>=0.2  &&  d<0.27){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COBBLESTONE);
		        			}
		        			else if(y==2  &&  d>=0.27  &&  d<0.3){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.OBSIDIAN);
		        			}
		        			else if(y==2  &&  d>=0.3 ){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.WATER);
		        			}
		            	}
		        		else if ( cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){ //ROAD
		        			d = (d*Math.abs(x-7)/5);
		        			//Ground 0 1 2 //sewer road// 13 14 15
		        			if(y==2  && x>=5 && x<=9){
		        				if(r>0.5)
		        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.MOSSY_COBBLESTONE);
		        				else
		        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COBBLESTONE);
		        			}
		        			else if(y==2  &&  d<0.2){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.MOSSY_COBBLESTONE);
		        			}
		        			else if(y==2  &&  d>=0.2  &&  d<0.27){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COBBLESTONE);
		        			}
		        			else if(y==2  &&  d>=0.27  &&  d<0.3){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.OBSIDIAN);
		        			}
		        			else if(y==2  &&  d>=0.3 ){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.WATER);
		        			}
		            	}
		        		else if ( cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION ){ //INTERSECTION
		        			//Ground 0 1 2 //sewer road// 13 14 15
		        			d = Math.max((d*Math.abs(z-7)/5),(d*Math.abs(x-7)/5));
		        			if(y==2  && ((x>=5 && x<=9) || (z>=5 && z<=9))){
		        				if(r>0.5)
		        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.MOSSY_COBBLESTONE);
		        				else
		        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COBBLESTONE);
		        			}
		        			else if(y==2  &&  d<0.2){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.MOSSY_COBBLESTONE);
		        			}
		        			else if(y==2  &&  d>=0.2  &&  d<0.27){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COBBLESTONE);
		        			}
		        			else if(y==2  &&  d>=0.27  &&  d<0.3){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.OBSIDIAN);
		        			}
		        			else if(y==2  &&  d>=0.3 ){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.WATER);
		        			}
		            	}
		        		//Building Pillar
		        		else if ( cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_BUILDING ){

		        			if((x<0+pillar_width ||  x>15-pillar_width)  &&  (z<0+pillar_width ||  z>15-pillar_width)){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.BRICK);
		        			}
		        			else{
			        			
			        			if(d<=0.05){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COBBLESTONE);
			        			}
			        			else if(d>0.05  &&  d<=0.1){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.MOSSY_COBBLESTONE);
			        			}
			        			else if(d>0.15  &&  d<=0.2){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COBBLESTONE_STAIRS);
			        			}
			        			else if(d>0.2  &&  d<=0.25){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COBBLE_WALL);
			        			}
			        			else if(d>0.25  &&  d<=0.30){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.STEP);
			        			}
			        			else{
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.STONE);
			        			}
			        			
		        			}

		        		}
		        		
		        		//building pipe
		        		
	    				if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH){
	    					
		        			//Pipe Shell
		        			if(y>=3  &&  ((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<sewer_pipe_width*sewer_pipe_width  && 
		        					((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))>=(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)      ){
		        				
		        				if(z==0  || z==15){
		        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.IRON_FENCE);
		        				}
		        				else if( d<0.02){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COAL_ORE);
			        			}
		        				else if( d>=0.02  &&  d<0.04){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.IRON_ORE);
			        			}
			        			else if( d>=0.04  &&  d<0.07){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.MOSSY_COBBLESTONE);
			        			}
			        			else if( d>=0.07  &&  d<=0.2){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COBBLESTONE);
			        			}
			        			else if( d>=0.2 ){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.STONE);
			        			}
		        			}
		        			//Outside the shell
		        			else if(y>=3  &&  ((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))>=sewer_pipe_width*sewer_pipe_width   ){
		        				if( d<0.02){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);
			        			}
		        			}

	    				}
	    				else if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_EAST_WEST){
	    					
		        			//Pipe Shell
		        			if(y>=3  &&  ((z-7.5)*(z-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<sewer_pipe_width*sewer_pipe_width  && 
		        					((z-7.5)*(z-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))>=(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)      ){
		        				
		        				if(x==0  || x==15){
		        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.IRON_FENCE);
		        				}
		        				else if( d<0.02){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COAL_ORE);
			        			}
		        				else if( d>=0.02  &&  d<0.04){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.IRON_ORE);
			        			}
			        			else if( d>=0.04  &&  d<0.07){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.MOSSY_COBBLESTONE);
			        			}
			        			else if( d>=0.07  &&  d<=0.2){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COBBLESTONE);
			        			}
			        			else if( d>=0.2 ){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.STONE);
			        			}
		        			}
		        			//Outside the shell
		        			else if(y>=3  &&  ((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))>=sewer_pipe_width*sewer_pipe_width   ){
		        				if( d<0.02){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);
			        			}
		        			}

	    				}
	    				else if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION){
	    					
		        			//Pipe Shell
		        			if(y>=3  &&  ((z-7.5)*(z-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<sewer_pipe_width*sewer_pipe_width  && 
		        					((z-7.5)*(z-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))>=(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)) {
		        				
		        				if(z==0  || z==15  ||  x==0  ||  x ==15){
		        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.IRON_FENCE);
		        				}
		        				else if( d<0.02){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COAL_ORE);
			        			}
		        				else if( d>=0.02  &&  d<0.04){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.IRON_ORE);
			        			}
			        			else if( d>=0.04  &&  d<0.07){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.MOSSY_COBBLESTONE);
			        			}
			        			else if( d>=0.07  &&  d<=0.2){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COBBLESTONE);
			        			}
			        			else if( d>=0.2 ){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.STONE);
			        			}
		        			}
		        			
		        			if(y>=3  &&  ((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<sewer_pipe_width*sewer_pipe_width  && 
		        					((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))>=(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)){
		        				if(z==0  || z==15  ||  x==0  ||  x ==15){
		        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.IRON_FENCE);
		        				}
		        				else if( d<0.02){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COAL_ORE);
			        			}
		        				else if( d>=0.02  &&  d<0.04){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.IRON_ORE);
			        			}
			        			else if( d>=0.04  &&  d<0.07){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.MOSSY_COBBLESTONE);
			        			}
			        			else if( d>=0.07  &&  d<=0.2){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COBBLESTONE);
			        			}
			        			else if( d>=0.2 ){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.STONE);
			        			}
		        			}
		        			
		        			
		        			//Remove 4 walls on intersection
		        			if(((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);		        			
		        			}
		        			if(((z-7.5)*(z-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);		        			
		        			}
		        			
		        			
		        			//Upward pipe
		        			if(y>=sewer_pipe_height+sewer_pipe_width-sewer_pipe_thick  && ((x-7.5)*(x-7.5)+(z-7.5)*(z-7.5))<sewer_pipe_width*sewer_pipe_width  && 
		        					((x-7.5)*(x-7.5)+(z-7.5)*(z-7.5))>=(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)      ){
		        				
		        				if( d<0.02){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COAL_ORE);
			        			}
		        				else if( d>=0.02  &&  d<0.04){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.IRON_ORE);
			        			}
			        			else if( d>=0.04  &&  d<0.07){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.MOSSY_COBBLESTONE);
			        			}
			        			else if( d>=0.07  &&  d<=0.2){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COBBLESTONE);
			        			}
			        			else if( d>=0.2 ){
			        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.STONE);
			        			}
		        			}
	    				}
	        		}
	    			

					//Upward pipe exit,entry to road
	    			
	    			//Cross 
	    			if(y==32  &&  cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION){
	    				//double d = rng.nextDouble();
	        			if(((x-7.5)*(x-7.5)+(z-7.5)*(z-7.5))<sewer_pipe_width*sewer_pipe_width  && 
	        					((x-7.5)*(x-7.5)+(z-7.5)*(z-7.5))>=(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)      ){
	        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COBBLESTONE);
	        			}
	        			else if(((x-7.5)*(x-7.5)+(z-7.5)*(z-7.5))<(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)){
	        				//chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.IRON_TRAPDOOR);
	        				chunkdata.setBlock(x, y, z, Material.IRON_TRAPDOOR.getId(), (byte) 0x8 );
	        			}
	    			}
	    			
	    			//upward
	    			if(y>sewer_pipe_height  &&  y<=31  &&  cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION){
	    				if(((x-7.5)*(x-7.5)+(z-7.5)*(z-7.5))<(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)){
	        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);
	        			}
	    			}
		        	
	    		}
	    	}
	    }    	
	    
	    
	    //Building Sewer Caves
		int cave_shift = (int) (Math.round( rng.nextDouble()*0)+8);
		int cave_height = (int) (Math.round( rng.nextDouble()*3)+4);
		int cave_width = (int) (Math.round( rng.nextDouble()*1)+3);
		
	    for(int y=3;y<31;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			double d =  rng.nextDouble();
	    			if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_BUILDING){
            			if(z>=0+cave_shift  &&  z<=cave_width+cave_shift){
            				if( ((y-(cave_height))* (y-(cave_height)) + (z-7.5)*(z-7.5))<  (cave_width)*(cave_width)){
            					if(d<=0.01){
        	        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.WEB);
            					}
            					else if(d>0.03  &&  d<=0.05){
        	        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.VINE);
            					}
            					else{
        	        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);
            					}
            				}
            			}
    				
            			if(x>=0+cave_shift  &&  x<=cave_width+cave_shift){
	        				if( ((y-(cave_height))* (y-(cave_height)) + (x-7.5)*(x-7.5))<  (cave_width)*(cave_width)){
	        					if(d<=0.01){
	    	        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.WEB);
	        					}
	        					else if(d>0.03  &&  d<=0.05){
	    	        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.VINE);
	        					}
	        					else{
	    	        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);
	        					}
	        				}
            			}
	    			}			
	    		}
	    	}
	    }    

	    
	    //Building Sewer gate
	    if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
		    for(int y=0;y<31;y++){
		    	for(int x=0;x<16;x++){
		    		for(int z=0;z<16;z++){
	    				if((y>=5  &&  y<31)  &&  ( (x<=0  ||  x>=15)  ||  (z<=0  ||  z>=15) )){
	    					double d = rng.nextDouble();
	    					if((x==0 || x==15) && (z==0  ||  z==15)  &&  d>=0.2){
	    						chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.MOSSY_COBBLESTONE);
	    					}
	    				}
	    			}
	    		}
	    	}
	    }    	
	    if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			double d = rng.nextDouble();
	    			if(d>=0.5){
	    				for(int y=0;y<31;y++){
	    					if((y>=5  &&  y<31)  &&  ( (x<=0  ||  x>=15)  ||  (z<=0  ||  z>=15) )){
	    						chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.IRON_FENCE);
	    					}
	    				}
    				}
    			}
    		}
    	}
	       	
	    
	    
     
        
        //Building Sewer-Road Entry (Should be after Road paving)
	    if ( (cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_EAST_WEST  ) ||
	    		(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH )  ){
	    	
			int x;
	 		int z;
		 	x=0;
	 		z=0;
	   		for(int y=3;y<33;y++){
	   			chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.LADDER);
	 		}
	   		chunkdata.setRegion(x,33,z,x+1,34,z+1,Material.TRAP_DOOR);

	 		x=15;
	 		z=15;
	   		for(int y=3;y<33;y++){
	   			chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.LADDER);
	 		}
	   		chunkdata.setRegion(x,33,z,x+1,34,z+1,Material.TRAP_DOOR);
		}
	       
	    
	    return chunkdata;
    	
    }  
    public ChunkData generateHighway(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
    	//Paving High Roads
	    for(int y=LAYER_1_HEIGHT;y<LAYER_1_HEIGHT+5;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){

	    			//Road
    				if(y >=LAYER_1_HEIGHT && y<LAYER_1_HEIGHT+2){
    					if(cg.getHighwayType(chkx,chkz,1)==CyberWorldObjectGenerator.DIR_EAST_WEST ){
    						if(z>=LAYER_1_SRT  &&  z<=LAYER_1_END){
        						chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_MATERIAL_1);
        					}
          	    		}
          	    		else if(cg.getHighwayType(chkx,chkz,1)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
          	    			if(x>=LAYER_1_SRT  &&  x<=LAYER_1_END){
        						chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_MATERIAL_1);
        					}
          	    		}
          	    		else if(cg.getHighwayType(chkx,chkz,1)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
          	    			if( (x>=LAYER_1_SRT  &&  x<=LAYER_1_END)  ||  (z>=LAYER_1_SRT  &&  z<=LAYER_1_END)){
        						chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_MATERIAL_1);
        					}
          	    		}
          	    		else if(cg.getHighwayType(chkx,chkz,1)==CyberWorldObjectGenerator.DIR_NOT_ROAD ){
          	    			
          	    		}
    					
    	    			
    	        	}
    				//Sidewalk
    				else if(y >=LAYER_1_HEIGHT+2 && y<LAYER_1_HEIGHT+3){
           	    		if(cg.getHighwayType(chkx,chkz,1)==CyberWorldObjectGenerator.DIR_EAST_WEST ){
	                			if((z<=LAYER_1_SW_MIN_END  &&  z>=LAYER_1_SRT)  ||  (z>=LAYER_1_SW_MAX_END  &&  z<=LAYER_1_END)){
	                				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
	                			}
          	    		}
          	    		else if(cg.getHighwayType(chkx,chkz,1)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
          	    			if((x<=LAYER_1_SW_MIN_END  &&  x>=LAYER_1_SRT)  ||  (x>=LAYER_1_SW_MAX_END  &&  x<=LAYER_1_END)){
	                    			chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
	                    		}
          	    		}
          	    		else if(cg.getHighwayType(chkx,chkz,1)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
	                			if(((x<=LAYER_1_SW_MIN_END )  ||  (x>=LAYER_1_SW_MAX_END )) && ((z<=LAYER_1_SW_MIN_END  )  ||  (z>=LAYER_1_SW_MAX_END ))  &&  !((x<LAYER_1_SRT  ||  x>LAYER_1_END)  &&  (z<LAYER_1_SRT  ||  z>LAYER_1_END))){
	                				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
	                			}

          	    		}
          	    		else if(cg.getHighwayType(chkx,chkz,1)==CyberWorldObjectGenerator.DIR_NOT_ROAD ){
          	    			
          	    		}
    				}
	    	    	


	    			
	    		}
	    	}
	    }       
	    for(int y=LAYER_2_HEIGHT;y<LAYER_2_HEIGHT+5;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
		    		//Layer_2
	    	    	
    	    		if(y >=LAYER_2_HEIGHT && y<LAYER_2_HEIGHT+2){
						if(cg.getHighwayType(chkx,chkz,2)==CyberWorldObjectGenerator.DIR_EAST_WEST ){
    						if(z>=LAYER_2_SRT  &&  z<=LAYER_2_END){
        						chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_MATERIAL_1);
        					}
          	    		}
          	    		else if(cg.getHighwayType(chkx,chkz,2)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
          	    			if(x>=LAYER_2_SRT  &&  x<=LAYER_2_END){
        						chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_MATERIAL_1);
        					}
          	    		}
          	    		else if(cg.getHighwayType(chkx,chkz,2)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
          	    			if( (x>=LAYER_2_SRT  &&  x<=LAYER_2_END)  ||  (z>=LAYER_2_SRT  &&  z<=LAYER_2_END)){
        						chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_MATERIAL_1);
        					}
          	    		}
          	    		else if(cg.getHighwayType(chkx,chkz,2)==CyberWorldObjectGenerator.DIR_NOT_ROAD ){
          	    			
          	    		}
    					
    	    			
    	        	}
    				//Sidewalk
    				else if(y >=LAYER_2_HEIGHT+2 && y<LAYER_2_HEIGHT+3){
    					if(cg.getHighwayType(chkx,chkz,2)==CyberWorldObjectGenerator.DIR_EAST_WEST ){
	                			if((z<=LAYER_2_SW_MIN_END  &&  z>=LAYER_2_SRT)  ||  (z>=LAYER_2_SW_MAX_END  &&  z<=LAYER_2_END)){
	                				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
	                			}
          	    		}
          	    		else if(cg.getHighwayType(chkx,chkz,2)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
          	    			if((x<=LAYER_2_SW_MIN_END  &&  x>=LAYER_2_SRT)  ||  (x>=LAYER_2_SW_MAX_END  &&  x<=LAYER_2_END)){
	                    			chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
	                    		}
          	    		}
          	    		else if(cg.getHighwayType(chkx,chkz,2)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
	                			if(((x<=LAYER_2_SW_MIN_END )  ||  (x>=LAYER_2_SW_MAX_END )) && ((z<=LAYER_2_SW_MIN_END  )  ||  (z>=LAYER_2_SW_MAX_END ))  &&  !((x<LAYER_2_SRT  ||  x>LAYER_2_END)  &&  (z<LAYER_2_SRT  ||  z>LAYER_2_END))){
	                				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
	                			}

          	    		}
          	    		else if(cg.getHighwayType(chkx,chkz,2)==CyberWorldObjectGenerator.DIR_NOT_ROAD ){
          	    			
          	    		}
    				}
    	    	


	    			
	    		}
	    	}
	    }       

	    for(int y=LAYER_3_HEIGHT;y<LAYER_3_HEIGHT+5;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    		
    	    		//Layer_3
    	    	
    	    		if(y >=LAYER_3_HEIGHT && y<LAYER_3_HEIGHT+2){
						if(cg.getHighwayType(chkx,chkz,3)==CyberWorldObjectGenerator.DIR_EAST_WEST ){
    						if(z>=LAYER_3_SRT  &&  z<=LAYER_3_END){
        						chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_MATERIAL_1);
        					}
          	    		}
          	    		else if(cg.getHighwayType(chkx,chkz,3)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
          	    			if(x>=LAYER_3_SRT  &&  x<=LAYER_3_END){
        						chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_MATERIAL_1);
        					}
          	    		}
          	    		else if(cg.getHighwayType(chkx,chkz,3)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
          	    			if( (x>=LAYER_3_SRT  &&  x<=LAYER_3_END)  ||  (z>=LAYER_3_SRT  &&  z<=LAYER_3_END)){
        						chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_MATERIAL_1);
        					}
          	    		}
          	    		else if(cg.getHighwayType(chkx,chkz,3)==CyberWorldObjectGenerator.DIR_NOT_ROAD ){
          	    			
          	    		}
    	    			
    	        	}
    				//Sidewalk
    				else if(y >=LAYER_3_HEIGHT+2 && y<LAYER_3_HEIGHT+3){
    					if(cg.getHighwayType(chkx,chkz,3)==CyberWorldObjectGenerator.DIR_EAST_WEST ){
	                			if((z<=LAYER_3_SW_MIN_END  &&  z>=LAYER_3_SRT)  ||  (z>=LAYER_3_SW_MAX_END  &&  z<=LAYER_3_END)){
	                				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
	                			}
          	    		}
          	    		else if(cg.getHighwayType(chkx,chkz,3)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
          	    			if((x<=LAYER_3_SW_MIN_END  &&  x>=LAYER_3_SRT)  ||  (x>=LAYER_3_SW_MAX_END  &&  x<=LAYER_3_END)){
	                    			chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
	                    		}
          	    		}
          	    		else if(cg.getHighwayType(chkx,chkz,3)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
	                			if(((x<=LAYER_3_SW_MIN_END )  ||  (x>=LAYER_3_SW_MAX_END )) && ((z<=LAYER_3_SW_MIN_END  )  ||  (z>=LAYER_3_SW_MAX_END ))  &&  !((x<LAYER_3_SRT  ||  x>LAYER_3_END)  &&  (z<LAYER_3_SRT  ||  z>LAYER_3_END))){
	                				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
	                			}

          	    		}
          	    		else if(cg.getHighwayType(chkx,chkz,3)==CyberWorldObjectGenerator.DIR_NOT_ROAD ){
          	    			
          	    		}
    				}
    	    	

	    			
	    		}
	    	}
	    }       

 		
         return chunkdata;
     	
     }
	public ChunkData generateBuilding(World world, ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
    	//Building Generation
		int layer;
		int layer_ground=32;
		int layer_height_1=256;
		int layer_height_2=256;
		int layer_height_3=256;
		
		int layer_start_1 = layer_ground+1;
		int layer_start_2 = layer_ground+1;
		int layer_start_3 = layer_ground+1;
		
		layer=1;
		if(cg.getBuilding(chkx, chkz, layer)==CyberWorldObjectGenerator.DIR_S_BUILDING){
			int type = cg.getBuildingType(chkx,chkz,layer);
			
			int sx = (cg.getBuildingStruct(chkx, chkz, layer)-1)%cg.s_size;
			int sz = (cg.getBuildingStruct(chkx, chkz, layer)-1)/cg.s_size;
			

			int j_start = sx*16;
			int j_max = (sx+1)*16;
			int i_start = sz*16;
			int i_max = (sz+1)*16;
			

			for(int j=j_start;j<Math.min(cc_list_s.get(type).getWidth(),j_max);j++){
        		for(int i=i_start;i<Math.min(cc_list_s.get(type).getLength(),i_max);i++){
                	for(int k=0;k<Math.min(cc_list_s.get(type).getHeight(),layer_height_1);k++){
        				int y = k+layer_start_1;
        				int x = j-j_start;
        				int z = i-i_start;
        				if(cc_list_s.get(type).getBlock(new Vector(j,k,i)).getId()!=Material.AIR.getId()){
        					if((x%8==0  &&  z%8==0)  &&  y%10 ==0)
								chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.JACK_O_LANTERN);
        					else
        						chunkdata.setRegion(x,y,z,x+1,y+1,z+1,cc_list_s.get(type).getBlock(new Vector(j,k,i)).getId());
        				}
        				
        			}
        		}
        	}
			
			
		}
		
		layer=2; // medium
		if(cg.getBuilding(chkx, chkz, layer)==CyberWorldObjectGenerator.DIR_M_BUILDING){
			int type = cg.getBuildingType(chkx,chkz,layer);
			
			int sx = (cg.getBuildingStruct(chkx, chkz, layer)-1)%cg.m_size;
			int sz = (cg.getBuildingStruct(chkx, chkz, layer)-1)/cg.m_size;
			

			int j_start = sx*16;
			int j_max = (sx+1)*16;
			int i_start = sz*16;
			int i_max = (sz+1)*16;
			for(int j=j_start;j<Math.min(cc_list_m.get(type).getWidth(),j_max);j++){
        		for(int i=i_start;i<Math.min(cc_list_m.get(type).getLength(),i_max);i++){
                	for(int k=0;k<Math.min(cc_list_m.get(type).getHeight(),layer_height_2);k++){
        				int y = k+layer_start_2;
        				int x = j-j_start;
        				int z = i-i_start;
        				if(cc_list_m.get(type).getBlock(new Vector(j,k,i)).getId()!=Material.AIR.getId()){
        					if((x%8==0  &&  z%8==0)  &&  y%10 ==0)
								chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.GLOWSTONE);
        					else
            					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,cc_list_m.get(type).getBlock(new Vector(j,k,i)).getId());
        				}
        				
        			}
        		}
        	}
			
		}	

		layer=3; // large
		if(cg.getBuilding(chkx, chkz, layer)==CyberWorldObjectGenerator.DIR_L_BUILDING){
			int type = cg.getBuildingType(chkx,chkz,layer);
			
			int sx = (cg.getBuildingStruct(chkx, chkz, layer)-1)%cg.l_size;
			int sz = (cg.getBuildingStruct(chkx, chkz, layer)-1)/cg.l_size;
			

			int j_start = sx*16;
			int j_max = (sx+1)*16;
			int i_start = sz*16;
			int i_max = (sz+1)*16;
			for(int j=j_start;j<Math.min(cc_list_l.get(type).getWidth(),j_max);j++){
        		for(int i=i_start;i<Math.min(cc_list_l.get(type).getLength(),i_max);i++){
                	for(int k=0;k<Math.min(cc_list_l.get(type).getHeight(),layer_height_3);k++){
        				int y = k+layer_start_3;
        				int x = j-j_start;
        				int z = i-i_start;
        				if(cc_list_l.get(type).getBlock(new Vector(j,k,i)).getId()!=Material.AIR.getId()){
        					if((x%8==0  &&  z%8==0)  &&  y%10 ==0)
								chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.SEA_LANTERN);
        					else
        						chunkdata.setRegion(x,y,z,x+1,y+1,z+1,cc_list_l.get(type).getBlock(new Vector(j,k,i)).getId());
        				}
        				
        			}
        		}
        	}
			
		}	
		
    return chunkdata;	
    }


}
 		