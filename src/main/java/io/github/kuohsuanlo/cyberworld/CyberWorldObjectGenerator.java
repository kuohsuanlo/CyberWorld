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
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.material.MaterialData;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;


public class CyberWorldObjectGenerator{
	private final Random rng;
	private final Random bm_rng;
	private Logger log = Logger.getLogger("Minecraft");
    public CityStreetGenerator cg = null;
    private long testingSeed= 1205;
    private final static int schematicBlueprint = 4;
    //private final static int schematicBlueprint = 124;
	private final static int schematicNumber = schematicBlueprint*1;
	private int sz_deco=1;
	private int sz_s=2;
	private int sz_m=3;
	private int sz_l=4;

    private final TerrainHeightGenerator hcg;
    private final int FACTORY_TERRAIN_OCTAVE = 5;
    private final int FACTORY_TERRAIN_HEIGHT = 40;
    
	public CyberWorldObjectGenerator(){
		rng = new Random();
		rng.setSeed(testingSeed);
		bm_rng = new Random();
		bm_rng.setSeed(testingSeed);
		
		readSchematic();
		cg = new CityStreetGenerator(500,500,rng,4,cc_list_s.size(),cc_list_m.size(),cc_list_l.size(),sz_s,sz_m,sz_l,1,1,1);
		hcg = new TerrainHeightGenerator(rng,FACTORY_TERRAIN_HEIGHT,FACTORY_TERRAIN_OCTAVE);
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

    public final static int MAX_MOST_MATERIAL =5;
    private final static int[] POSSIBLE_MATERIAL = {1,2,3,4,17,20,24,35,43,95,98,159,179,201};
    
	//Paving Roads
    private static Material ROAD_SIDEWALK_MATERIAL_1 = Material.STEP;
    private static Material HIGHWAY_MATERIAL = Material.QUARTZ_BLOCK;
    private static MaterialData ROAD_MATERIAL = new MaterialData(Material.STAINED_CLAY.getId(),(byte)0x9);

    //Paving High Roads

    private static Material HIGHWAY_FENCE = Material.IRON_FENCE;
    private static MaterialData HIGHWAY_TUBES_MATERIAL = null;
    private static int[] LAYER_HEIGHT = {48,58,68};
	
	private static int[] LAYER_WIDTH = {10,10,10};
	private static int[] LAYER_SPACE = {(16-LAYER_WIDTH[0])/2,(16-LAYER_WIDTH[1])/2,(16-LAYER_WIDTH[2])/2};
	private static int[] LAYER_SW_WD = {1,1,1};
	
	private static int[] LAYER_SRT = {0+LAYER_SPACE[0],0+LAYER_SPACE[1],0+LAYER_SPACE[2]};
	private static int[] LAYER_END = {15-LAYER_SPACE[0],15-LAYER_SPACE[1],15-LAYER_SPACE[2]};
	private static int[] LAYER_SW_MIN_END = {LAYER_SRT[0]+LAYER_SW_WD[0],LAYER_SRT[1]+LAYER_SW_WD[1],LAYER_SRT[2]+LAYER_SW_WD[2]};
	private static int[] LAYER_SW_MAX_END = {LAYER_END[0]-LAYER_SW_WD[0],LAYER_END[1]-LAYER_SW_WD[1],LAYER_END[2]-LAYER_SW_WD[2]};

    public static final String WINDOWS_PATH="./plugins/CyberWorld/schematics/";
   
    public static final int MAX_SPACE_HEIGHT = 256; // 0-255
    
    
	private CuboidClipboard[] cc_list = new CuboidClipboard[schematicNumber];
	private ArrayList<CuboidClipboard> cc_list_deco = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_s = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_m = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_l = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_s_b = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_m_b = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_l_b = new ArrayList<CuboidClipboard>();
	private ArrayList<int[]> cc_list_most_s = new ArrayList<int[]>();
	private ArrayList<int[]> cc_list_most_m = new ArrayList<int[]>();
	private ArrayList<int[]> cc_list_most_l = new ArrayList<int[]>();
	

	
	private void readSchematic(){
		for(int i =0;i<schematicBlueprint;i++){
			cc_list[i] = Schematic.getSchematic(i+".schematic",0);
			if(cc_list[i].getLength()<=sz_deco*16  && cc_list[i].getWidth()<=sz_deco*16){
				cc_list_deco.add(cc_list[i]);
			}
			
			if(cc_list[i].getLength()<=sz_s*16  && cc_list[i].getWidth()<=sz_s*16){
				cc_list_s.add(cc_list[i]);
				cc_list_s_b.add(cc_list[i]);
				cc_list_most_s.add(this.getMostMaterial(cc_list[i]));
			}
			else if(cc_list[i].getLength()<=sz_m*16  && cc_list[i].getWidth()<=sz_m*16){
				cc_list_m.add(cc_list[i]);
				cc_list_m_b.add(cc_list[i]);
				cc_list_most_m.add(this.getMostMaterial(cc_list[i]));
			}
			else if(cc_list[i].getLength()<=sz_l*16  && cc_list[i].getWidth()<=sz_l*16){
				cc_list_l.add(cc_list[i]);
				cc_list_l_b.add(cc_list[i]);
				cc_list_most_l.add(this.getMostMaterial(cc_list[i]));
			}
			else{
				System.out.print("[CyberWorld] : Error on schematic = "+i+"/ size too large : "+cc_list[i].getWidth()+","+cc_list[i].getLength());
			}
			
			//System.out.println("FRAME_AREA : "+i);
			//printMap(this.getFrameArea(cc_list[i]));
			//System.out.println("FILLED_AREA : "+i);
			//printMap(this.getfilledArea(cc_list[i]));
		}
		System.out.print("[CyberWorld] : Final numbers of read schematic(Deco/Small/Medium/Large) = "+cc_list_deco.size()+"/"+cc_list_s.size()+"/"+cc_list_m.size()+"/"+cc_list_l.size());
		cc_list = null;
		
	}
    

	public ChunkData generateBottom(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
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
	    		}
	    	}
	    }
        return chunkdata;
    }
	public ChunkData generateCitySurface(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
        for(int y=30;y<=33;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			//Paving Ground
	    			if(y <=32  &&  y >=31){
	    				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.STONE);
		        	}
	    			else if(y ==30){
	    				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.DIRT);
		        	}
	    		}
	    	}
	    }
		
        return chunkdata;
    }
	public ChunkData generateRoad(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
		//Paving Roads
		
    	if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_EAST_WEST ){
        	chunkdata.setRegion(0,30,0,16,33,16,ROAD_MATERIAL);
		}
		else if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
	    	chunkdata.setRegion(0,30,0,16,33,16,ROAD_MATERIAL);
		}
		else if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
	    	chunkdata.setRegion(0,30,0,16,33,16,ROAD_MATERIAL);
		}

    	
		for(int y=33;y<34;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			
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
        			if((z == 5  ||  z==10)  &&  (x%8==1  ||  x%8==2) ){
        				chunkdata.setBlock(x, y, z, Material.STAINED_CLAY.getId(), (byte) 0x4 );
        			}
        			else if((z == 5  ||  z==10)  &&  (x%8==3  ||  x%8==0) ){
        				chunkdata.setBlock(x, y, z, Material.GLOWSTONE );
        			}
    			}
    			else if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
    				if((x == 5  ||  x==10)  &&  (z%8==1  ||  z%8==2) ){
    					chunkdata.setBlock(x, y, z, Material.STAINED_CLAY.getId(), (byte) 0x4 );
        			}
    				else if((x == 5  ||  x==10)    &&  (z%8==3  ||  z%8==0) ){
        				chunkdata.setBlock(x, y, z, Material.GLOWSTONE );
        			}
    			}
    			else if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
    				if((x ==4  ||  x==6  ||  x==9  ||  x==11)  &&  (z<3  ||  z>=13) ){
    					chunkdata.setBlock(x, y, z, Material.QUARTZ_BLOCK);
        			}
    				else if((z ==4  || z==6  ||  z==9  ||  z==11)  &&  (x<3  || x>=13) ){
    					chunkdata.setBlock(x, y, z, Material.QUARTZ_BLOCK);
        			}
    			}
    		}
    		
	    	
	    }
        return chunkdata;
    	
    }
    public ChunkData generateCitySewer(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
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
		        			//d = (d*Math.abs(z-7)/5);
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
		        			//d = (d*Math.abs(x-7)/5);
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
		        			//d = Math.max((d*Math.abs(z-7)/5),(d*Math.abs(x-7)/5));
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
		        			
		        			
		        			//Downward foundation
		        			if(y<=sewer_pipe_height-sewer_pipe_width  && ((x-7.5)*(x-7.5)+(z-7.5)*(z-7.5))<sewer_pipe_width*sewer_pipe_width    ){
		        				
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
    	for(int level=2;level>=0;level--){
    		int road_tube_width = 5;
    		int road_y_middle = LAYER_HEIGHT[level]+road_tube_width/2;
    		int road_tube_thick = 1;

    	    HIGHWAY_TUBES_MATERIAL = new MaterialData(Material.STAINED_GLASS.getId(), (byte)(Math.abs(chkx+chkz)%16)  );
    		for(int y=LAYER_HEIGHT[level];y<LAYER_HEIGHT[level]+road_tube_width*2;y++){
    			boolean EW_tunnel = false;
    			boolean NS_tunnel = false;
    			boolean IT_tunnel = false;
    	    	for(int x=0;x<16;x++){
    	    		for(int z=0;z<16;z++){

        				
    	    			//Road & tube checking
        				if(y >=LAYER_HEIGHT[level] && y<LAYER_HEIGHT[level]+road_tube_width*2){
        					if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_EAST_WEST ){
              	    			if(chunkdata.getType(x, y, z)!=Material.AIR){
        							if(((z-7.5)*(z-7.5)+(y-road_y_middle)*(y-road_y_middle))<road_tube_width*road_tube_width){
        								chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);
        								if(((z-7.5)*(z-7.5)+(y-road_y_middle)*(y-road_y_middle))>=(road_tube_width-road_tube_thick)*(road_tube_width-road_tube_thick)      ){
        									EW_tunnel = true;
        								}
        							}
        						}
              	    			
        						if(z>=LAYER_SRT[level]  &&  z<=LAYER_END[level]  &&  y<LAYER_HEIGHT[level]+2){
            						chunkdata.setRegion(x,y,z,x+1,y+1,z+1,HIGHWAY_MATERIAL);
            					}
              	    		}
              	    		else if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
              	    			
              	    			if(chunkdata.getType(x, y, z)!=Material.AIR){
        							if(((x-7.5)*(x-7.5)+(y-road_y_middle)*(y-road_y_middle))<road_tube_width*road_tube_width){
        								chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);
        								if(((x-7.5)*(x-7.5)+(y-road_y_middle)*(y-road_y_middle))>=(road_tube_width-road_tube_thick)*(road_tube_width-road_tube_thick)  ){
        									NS_tunnel = true;
        								}
        								
        							}
        						}
              	    			if(x>=LAYER_SRT[level]  &&  x<=LAYER_END[level]  &&  y<LAYER_HEIGHT[level]+2){
            						chunkdata.setRegion(x,y,z,x+1,y+1,z+1,HIGHWAY_MATERIAL);
            					}
              	    		}
              	    		else if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
              	    			if(chunkdata.getType(x, y, z)!=Material.AIR){
              	    				if(((x-7.5)*(x-7.5)+(y-road_y_middle)*(y-road_y_middle))<road_tube_width*road_tube_width){
              	    					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);
              	    					if(((x-7.5)*(x-7.5)+(y-road_y_middle)*(y-road_y_middle))>=(road_tube_width-road_tube_thick)*(road_tube_width-road_tube_thick)  ){
              	    						IT_tunnel = true;
        								}
        							}
              	    				if(((z-7.5)*(z-7.5)+(y-road_y_middle)*(y-road_y_middle))<road_tube_width*road_tube_width){
              	    					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);
              	    					if(((z-7.5)*(z-7.5)+(y-road_y_middle)*(y-road_y_middle))>=(road_tube_width-road_tube_thick)*(road_tube_width-road_tube_thick)      ){
              	    						IT_tunnel = true;
        								}
        							}
        						}
              	    			if( ((x>=LAYER_SRT[level]  &&  x<=LAYER_END[level])  ||  (z>=LAYER_SRT[level]  &&  z<=LAYER_END[level]))  &&  y<LAYER_HEIGHT[level]+2){
            						chunkdata.setRegion(x,y,z,x+1,y+1,z+1,HIGHWAY_MATERIAL);
            					}
            					
              	    		}
              	    		else if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_NOT_ROAD ){
              	    			
              	    		}
        					
        	    			
        	        	}

        				//Fence
        				if(y >=LAYER_HEIGHT[level]+2 && y<LAYER_HEIGHT[level]+3){
               	    		if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_EAST_WEST ){
    	                			if((z<=LAYER_SW_MIN_END[level]  &&  z>=LAYER_SRT[level])  ||  (z>=LAYER_SW_MAX_END[level]  &&  z<=LAYER_END[level])){
    	                				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,HIGHWAY_FENCE);
    	                			}
              	    		}
              	    		else if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
              	    			if((x<=LAYER_SW_MIN_END[level]  &&  x>=LAYER_SRT[level])  ||  (x>=LAYER_SW_MAX_END[level]  &&  x<=LAYER_END[level])){
    	                    			chunkdata.setRegion(x,y,z,x+1,y+1,z+1,HIGHWAY_FENCE);
    	                    		}
              	    		}
              	    		else if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_INTERSECTION ){

              	    			
                    			if(((x<=LAYER_SW_MIN_END[level] )  ||  (x>=LAYER_SW_MAX_END[level] )) && ((z<=LAYER_SW_MIN_END[level]  )  ||  (z>=LAYER_SW_MAX_END[level] ))  &&  !((x<LAYER_SRT[level]  ||  x>LAYER_END[level])  &&  (z<LAYER_SRT[level]  ||  z>LAYER_END[level]))){
                    				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,HIGHWAY_FENCE);
                    			}

              	    		}
              	    		else if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_NOT_ROAD ){
              	    			
              	    		}
        				}
    	    		}
    	    	}
    	    	
    	    	//tube constructing
    	    	for(int x=0;x<16;x++){
    	    		for(int z=0;z<16;z++){
      	    			if(EW_tunnel){
      	    				if(((z-7.5)*(z-7.5)+(y-road_y_middle)*(y-road_y_middle))<road_tube_width*road_tube_width){
    							if(((z-7.5)*(z-7.5)+(y-road_y_middle)*(y-road_y_middle))>=(road_tube_width-road_tube_thick)*(road_tube_width-road_tube_thick)      ){
    								chunkdata.setRegion(x,y,z,x+1,y+1,z+1,HIGHWAY_TUBES_MATERIAL);
    							}
      	    				}
      	    			}
      	    			else if(NS_tunnel){
      	    				if(((x-7.5)*(x-7.5)+(y-road_y_middle)*(y-road_y_middle))<road_tube_width*road_tube_width){
      	    					if(((x-7.5)*(x-7.5)+(y-road_y_middle)*(y-road_y_middle))>=(road_tube_width-road_tube_thick)*(road_tube_width-road_tube_thick)  ){
    								chunkdata.setRegion(x,y,z,x+1,y+1,z+1,HIGHWAY_TUBES_MATERIAL);
    							}
      	    				}
      	    			}
      	    			else if(IT_tunnel){
      	    				if(((x-7.5)*(x-7.5)+(y-road_y_middle)*(y-road_y_middle))<road_tube_width*road_tube_width){
      	    					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);
      	    					if(((x-7.5)*(x-7.5)+(y-road_y_middle)*(y-road_y_middle))>=(road_tube_width-road_tube_thick)*(road_tube_width-road_tube_thick)  ){
    								chunkdata.setRegion(x,y,z,x+1,y+1,z+1,HIGHWAY_TUBES_MATERIAL);
								}
							}
      	    				if(((z-7.5)*(z-7.5)+(y-road_y_middle)*(y-road_y_middle))<road_tube_width*road_tube_width){
      	    					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);
      	    					if(((z-7.5)*(z-7.5)+(y-road_y_middle)*(y-road_y_middle))>=(road_tube_width-road_tube_thick)*(road_tube_width-road_tube_thick)      ){
    								chunkdata.setRegion(x,y,z,x+1,y+1,z+1,HIGHWAY_TUBES_MATERIAL);
								}
							}
      	    				//Remove 4 walls on intersection
      	    				
    	        			if(y>=road_y_middle  &&  ((x-7.5)*(x-7.5)+(y-road_y_middle)*(y-road_y_middle))<(road_tube_width-road_tube_thick)*(road_tube_width-road_tube_thick)){
    	        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);		        			
    	        			}
    	        			if(y>=road_y_middle  &&  ((z-7.5)*(z-7.5)+(y-road_y_middle)*(y-road_y_middle))<(road_tube_width-road_tube_thick)*(road_tube_width-road_tube_thick)){
    	        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);		        			
    	        			}
          	    			if( ((x>=LAYER_SRT[level]  &&  x<=LAYER_END[level])  ||  (z>=LAYER_SRT[level]  &&  z<=LAYER_END[level]))  &&  y<LAYER_HEIGHT[level]+2){
        						chunkdata.setRegion(x,y,z,x+1,y+1,z+1,HIGHWAY_MATERIAL);
        					}
      	    			}


      	    			
      	    			
    	    		}
    	    	}
    	    }   

    		// construct pillar for highway
    		
			boolean has_thing_underneath=false;
			/*
			if(level>=1){
				for(int l=0;l < level;l++){
					if(cg.getHighwayType(chkx,chkz,l)==CyberWorldObjectGenerator.DIR_BUILDING){
						has_thing_underneath =true;
					}
				}
			}
			if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_BUILDING){
				has_thing_underneath = true;
			}
			*/
			if((chkx+chkz)%4==0  &&  (!has_thing_underneath) ){
				for(int x=0;x<16;x++){
    	    		for(int z=0;z<16;z++){
    	    			if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_EAST_WEST ){
    	    				if((z == 7  ||  z==8)  &&  (x==7  ||  x==8) ){
    	    					chunkdata.setRegion(x,33,z,x+1,LAYER_HEIGHT[level],z+1,HIGHWAY_MATERIAL);
    	        			}
    	  	    		}
    	  	    		else if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
    	  	    			if((z == 7  ||  z==8)  &&  (x==7  ||  x==8) ){
    	    					chunkdata.setRegion(x,33,z,x+1,LAYER_HEIGHT[level],z+1,HIGHWAY_MATERIAL);
    	        			}
    	  	    		}
    	  	    		else if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
    	  	    			
    	  	    		}
    	  	    		else if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_NOT_ROAD ){
    	  	    			
    	  	    		}
    	    		}
        		}
				
			}
    		
    	}
	      
         return chunkdata;
     	
     }
	public ChunkData generateBuilding(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes, int start_of_layer){
    	//Building Generation
		int layer;
		int layer_ground=32;
		int layer_start = layer_ground+1;
		
		int[] current_size = {cg.s_size,cg.m_size,cg.l_size};
		int[] building_type = {CyberWorldObjectGenerator.DIR_S_BUILDING,CyberWorldObjectGenerator.DIR_M_BUILDING,CyberWorldObjectGenerator.DIR_L_BUILDING};
		Object[] all_lists = {cc_list_s,cc_list_m,cc_list_l};
		Object[] all_b_lists = {cc_list_s_b,cc_list_m_b,cc_list_l_b};
		Object[] all_m_lists = {cc_list_most_s,cc_list_most_m,cc_list_most_l};
		
		int s_layer_nubmer=start_of_layer;
		for(layer=s_layer_nubmer;layer>=0;layer--){
			if(cg.getBuilding(chkx, chkz, layer)==building_type[layer]){	

				int type = cg.getBuildingType(chkx,chkz,layer);
				int sx = (cg.getBuildingStruct(chkx, chkz, layer)-1)/current_size[layer];
				int sz = (cg.getBuildingStruct(chkx, chkz, layer)-1)%current_size[layer];
				int i_start = sx*16;
				int i_max = (sx+1)*16;
				int j_start = sz*16;
				int j_max = (sz+1)*16;


				
				int angle = cg.getBuildingRotation(chkx,chkz,layer)*90;
				//int angle = 0;
				
				ArrayList<CuboidClipboard> current_list =  (ArrayList<CuboidClipboard>) all_lists[layer] ;
				ArrayList<CuboidClipboard> current_b_list =  (ArrayList<CuboidClipboard>) all_b_lists[layer] ;
				ArrayList<int[]> current_m_list=  (ArrayList<int[]>) all_m_lists[layer] ;
				

				//replace bulding blocks rng
				int[] used_material = new int[MAX_MOST_MATERIAL];
				bm_rng.setSeed(cg.getBuildingSeed(chkx, chkz, layer));
				for(int i=0;i<MAX_MOST_MATERIAL;i++){
					//if(bm_rng.nextBoolean()){
						//System.out.println(current_m_list.get(type)[i] +"->"+POSSIBLE_MATERIAL[bm_rng.nextInt(POSSIBLE_MATERIAL.length)]);
						used_material[i] = POSSIBLE_MATERIAL[bm_rng.nextInt(POSSIBLE_MATERIAL.length)];
					//}
					//else{
					//	used_material[i] = current_m_list.get(type)[i];
					//}
					
				}
				
				
				//replacing block
				int i_end = Math.min(current_list.get(type).getWidth(),i_max);
				int j_end = Math.min(current_list.get(type).getLength(),j_max);
				int k_end = current_list.get(type).getHeight();
				int block_id  = Material.AIR.getId();
				
				for(int i=i_start;i<i_end;i++){
	    			for(int j=j_start;j<j_end;j++){
		            	for(int k=0;k<k_end;k++){
		            		block_id = current_list.get(type).getBlock(new Vector(i,k,j)).getId();
		            		for(int m=0;m<MAX_MOST_MATERIAL;m++){
		            			if(block_id == current_m_list.get(type)[m]){
		            				current_list.get(type).setBlock(new Vector(i,k,j),new BaseBlock(used_material[m]));
		            				break;
		            			}
		            		}
		            		
		            	}
	    			}
            	}
				//rotating
				current_list.get(type).rotate2D(angle);
				i_end = Math.min(current_list.get(type).getWidth(),i_max);
				j_end = Math.min(current_list.get(type).getLength(),j_max);
				k_end = current_list.get(type).getHeight();
				
				

				
				
				boolean[][][] fillingAirIndeces ;
				
				if(layer!=s_layer_nubmer){
					fillingAirIndeces = this.getfilledArea(current_list.get(type));
					//printMap(fillingAirIndeces);
				}
				else{
					fillingAirIndeces = new boolean [i_end][j_end][k_end] ;
				}
				
				
	    		for(int i=i_start;i<i_end;i++){
	    			for(int j=j_start;j<j_end;j++){
		            	for(int k=0;k<k_end;k++){
		    				int y = k+layer_start;
		    				int x = j-j_start;
		    				int z = i-i_start;
		    				
		    				
		    				block_id = current_list.get(type).getBlock(new Vector(i,k,j)).getId();
	    					//replacing illegal block, and light blocks
		    				if(block_id!=Material.AIR.getId()){
	    						if((x%8==4  &&  z%8==4)  &&  y%8 ==0){
		    						switch(layer){
	    							case 0: 
	    								block_id = Material.JACK_O_LANTERN.getId();
	    								break;
	    							case 1: 
	    								block_id = Material.GLOWSTONE.getId();
	    								break;
	    							case 2: 
	    								block_id = Material.SEA_LANTERN.getId();
	    								break;
	    							}
								}
	    					}
		    				
		    				//clearing overlapping area
		    				if(fillingAirIndeces[i][j][k]){
		    					chunkdata.setBlock(x, y, z,Material.AIR);
		    				}
		    				
		    				if(block_id!=Material.AIR.getId()){
		    					
		    					int fixed_id = fixBannedBlock(block_id);
		    					if(fixed_id == block_id){
		    						chunkdata.setBlock(x, y, z,new MaterialData(fixed_id, (byte)current_list.get(type).getBlock(new Vector(i,k,j)).getData()));
		    					}
		    					else{
		    						chunkdata.setBlock(x, y, z,new MaterialData(fixed_id));
		    					}
		    				} 
		    			}
		    		}
		    	}

	    		
	    		//rotating back
				current_list.get(type).rotate2D(360-angle);
				
				//replacing back
				i_end = Math.min(current_list.get(type).getWidth(),i_max);
				j_end = Math.min(current_list.get(type).getLength(),j_max);
				k_end = current_list.get(type).getHeight();
				
				for(int i=i_start;i<i_end;i++){
	    			for(int j=j_start;j<j_end;j++){
		            	for(int k=0;k<k_end;k++){
		            		block_id = current_b_list.get(type).getBlock(new Vector(i,k,j)).getId();
            				current_list.get(type).setBlock(new Vector(i,k,j),new BaseBlock(block_id));
		            	}
	    			}
            	}
				
			}
		
		}
		return chunkdata;	
    }
	public ChunkData generateBuildingDecoration(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
		//on top, on group, on side
    	//Building Generation
		
		return chunkdata;
    	
    }
	public ChunkData generateGroundDecoration(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
		int layer_ground=32;
		int layer_start = layer_ground+1;
		int layer_height = 256;
		int sx=0;
		int sz=0;
		int i_start = sx*16;
		int i_max = (sx+1)*16;
		int j_start = sz*16;
		int j_max = (sz+1)*16;
		
		
		int type = rng.nextInt(cc_list_deco.size());
		CuboidClipboard object = cc_list_deco.get(type);
		int angle = rng.nextInt(4)*90;
		object.rotate2D(angle);
		boolean[][][] fillingAirIndeces = this.getfilledArea(object);
		
		


		int i_end = Math.min(object.getWidth(),i_max);
		int j_end = Math.min(object.getLength(),j_max);
		int k_end = object.getHeight();
		int block_id;
		
		boolean near_road=false;
		int near_distance;
		

		
		
		if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_EAST_WEST ||
				cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH  ||  cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
			//on road object
			
		}
		else{
			//near road object
			
			near_distance =1;
			for(int cx = chkx-near_distance; cx<=chkx+near_distance;cx++){
				for(int cz = chkz-near_distance; cz<=chkz+near_distance;cz++){
					if( cg.getRoadType(cx,cz)==CyberWorldObjectGenerator.DIR_EAST_WEST ||
							cg.getRoadType(cx,cz)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH  ||  cg.getRoadType(cx,cz)==CyberWorldObjectGenerator.DIR_INTERSECTION  ){
						
						near_road =true;
					}
				}
			}
			if(near_road){
				for(int j=j_start;j<j_end;j++){
		    		for(int i=i_start;i<i_end;i++){
		            	for(int k=k_end-1;k>=0;k--){
		            		int y = k+layer_start;
		    				int x = i-i_start;
		    				int z = j-j_start;


		            		block_id = object.getBlock(new Vector(i,k,j)).getId();
		            		if(block_id!=Material.AIR.getId()){
		            			chunkdata.setBlock(x, y, z, new MaterialData(fixBannedBlock(block_id), (byte)object.getBlock(new Vector(i,k,j)).getData()));
								
		            		}
		            		
	            		}
	            	}
				}
			}
		}

			
		boolean less_building_area=false;
		int[][] less_building_struct = {{4},{6,8,9},{8,11,14,12,15,16}};
		for(int layer=0;layer<3;layer++){
			for(int ls_area_idx = 0;ls_area_idx< less_building_struct[layer].length;ls_area_idx++){
				if(cg.getBuildingStruct(chkx, chkz, layer)==less_building_struct[layer][ls_area_idx]){	
					less_building_area =true;
				}
			}
		}
		
		
		if(less_building_area){
			for(int j=j_start;j<j_end;j++){
	    		for(int i=i_start;i<i_end;i++){
	            	for(int k=0;k<k_end;k++){
	            		int y = k+layer_start;
	    				int z = j-j_start;
	    				int x = i-i_start;


	            		block_id = object.getBlock(new Vector(i,k,j)).getId();
	            		
	            		
	            		if(fillingAirIndeces[i][j][k]){
	    					chunkdata.setBlock(x, y, z,Material.AIR);
	    				}
	    				//Bug: finding 3d volumn has bug in getfilledArea
	    				if(block_id!=Material.AIR.getId()){
	    					int fixed_id = fixBannedBlock(block_id);
	    					if(fixed_id == block_id){
	    						chunkdata.setBlock(x, y, z,new MaterialData(fixed_id, (byte)object.getBlock(new Vector(i,k,j)).getData()));
	    					}
	    					else{
	    						chunkdata.setBlock(x, y, z,new MaterialData(fixed_id));
	    					}
	    				} 
	            		
	            		if(block_id!=Material.AIR.getId()){
	            			chunkdata.setBlock(x, y, z, new MaterialData(fixBannedBlock(block_id), (byte)object.getBlock(new Vector(i,k,j)).getData()));
							
	            		}
	            		
            		}
            	}
			}
		}

		object.rotate2D(360-angle);



	    
        return chunkdata;
    	
    }
	public ChunkData generateFactoryTerrain(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){

    	for(int x=0;x<16;x++){
    		for(int z=0;z<16;z++){
    			int height = Math.round(hcg.generateHeight(chkx*16+x, chkz*16+z))+3;
    			if(height>=3){
        			chunkdata.setRegion(x,3,z,x+1,height,z+1,Material.STONE);
        			chunkdata.setRegion(x,height,z,x+1,height+1,z+1,Material.DIRT);
    			}
    			else{
    				double d = rng.nextDouble();
					int y=2;
					if ( d<0.2){
        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.MOSSY_COBBLESTONE);
        			}
        			else if( d>=0.2  &&  d<0.27){
        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.COBBLESTONE);
        			}
        			else if( d>=0.27  &&  d<0.3){
        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.OBSIDIAN);
        			}
        			else if( d>=0.3 ){
        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.WATER);
        			}
	            	
    			}
    			
    		}
    	}
	    
        return chunkdata;
    }
	public ChunkData generateFactorySewer(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
   	 //Building Sewer Layout

		int sewer_pipe_width = 5;
		int sewer_pipe_thick = 2;
		int sewer_pipe_height= 16;
	    for(int y=2;y<33;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			if(y >=2 && y <31){
	    				double d = rng.nextDouble();
	    				//Building Sewer Pipe, Sewer Ground
		        		if ( cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_EAST_WEST ){ //ROAD
		        			//d = (d*Math.abs(z-7)/5);
		        			//Ground 0 1 2 //sewer road// 13 14 15
		        			if(y==2  &&  d<0.2){
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
		        			//d = (d*Math.abs(x-7)/5);
		        			//Ground 0 1 2 //sewer road// 13 14 15
		        			if(y==2  &&  d<0.2){
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
		        			if(y==2  &&  d<0.2){
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
		        			else if((-1)*(y-sewer_pipe_height) == sewer_pipe_width-sewer_pipe_thick-1  &&  ((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)      ){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.WATER);
		        			}
		        			else if(y>=3 &&  ((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)      ){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);
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
		        			else if((-1)*(y-sewer_pipe_height) == sewer_pipe_width-sewer_pipe_thick-1  &&  ((z-7.5)*(z-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)      ){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.WATER);
		        			}
		        			else if(y>=3 &&  ((z-7.5)*(z-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)      ){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);
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
		        			
		        			//pipe water
		        			if((-1)*(y-sewer_pipe_height) == sewer_pipe_width-sewer_pipe_thick-1  &&  ((z-7.5)*(z-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)      ){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.WATER);
		        			}
		        			if((-1)*(y-sewer_pipe_height) == sewer_pipe_width-sewer_pipe_thick-1  &&  ((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)      ){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.WATER);
		        			}
		        			
		        			//Downward foundation
		        			if(y<=sewer_pipe_height-sewer_pipe_width  && ((x-7.5)*(x-7.5)+(z-7.5)*(z-7.5))<sewer_pipe_width*sewer_pipe_width    ){
		        				
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
	    			

	    			
	    			//Pipe Entry
	    			if(y>sewer_pipe_height  &&  y<=21  &&  cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION){
	    				if(((x-7.5)*(x-7.5)+(z-7.5)*(z-7.5))<(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)){
	        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);
	        			}
	    			}
	    			
	    			if(y==20  &&  cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION){
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
	    			
	    			
		        	
	    		}
	    	}
	    }    	
	    
	    return chunkdata;
   	
	}
	private int[] getMostMaterial(CuboidClipboard cc){

		int[] id_times = new int[500];
		int now_id =0;
		for(int y=0;y<cc.getHeight();y++){
			for(int x=0;x<cc.getWidth();x++){
				for(int z=0;z<cc.getLength();z++){
					now_id = cc.getBlock(new Vector(x,y,z)).getId();
					if(now_id<id_times.length  &&  
							now_id!=Material.AIR.getId()     &&  
							now_id!=Material.GLASS.getId()   &&  
							now_id!=Material.STAINED_GLASS.getId()   &&  
							now_id!=Material.THIN_GLASS.getId()   && 
							now_id!=Material.STAINED_GLASS_PANE.getId()   && 
							now_id!=Material.LAVA.getId()   &&  
							now_id!=Material.WATER.getId()){
						id_times[now_id]++;
					}
					
				}
			}
		}

	    int large[] = new int[MAX_MOST_MATERIAL];
	    int max = 0, index;
		for (int j = 0; j < MAX_MOST_MATERIAL; j++) {
	        max = id_times[0];
	        index = 0;
	        for (int i = 1; i < id_times.length; i++) {
	            if (max < id_times[i]) {
	                max = id_times[i];
	                index = i;
	            }
	        }
	        large[j] = index;
	        id_times[index] = Integer.MIN_VALUE;

	        //System.out.println("Largest " + index );
	    }
		
		return large;
		
	}
	private boolean[][][] returnOverlappingIgnoredVoxel(boolean[][][] new_cc,boolean[][][] old_cc){

		int max_x_old = Math.max(new_cc.length , old_cc.length);
		int max_z_old = Math.max(new_cc[0].length , old_cc[0].length);
		int max_y_old = Math.max(new_cc[0][0].length , old_cc[0][0].length);

		boolean[][][] overlap_area = new boolean[max_x_old][max_z_old][max_y_old];
		
		
		max_x_old = Math.min(new_cc.length , old_cc.length);
		max_z_old = Math.min(new_cc[0].length , old_cc[0].length);
		max_y_old = Math.min(new_cc[0][0].length , old_cc[0][0].length);

		
		for(int y=0;y<max_y_old;y++){
			for(int x=0;x<max_x_old;x++){
				for(int z=0;z<max_z_old;z++){
					if(new_cc[x][z][y]  &&  old_cc[x][z][y]){
						overlap_area[x][z][y]=true;
					}
				}
			}
		}
		
		

		return overlap_area;
		
	}
	private boolean[][][] getFrameArea(CuboidClipboard cc){
		
		int max_x_old = cc.getWidth();
		int max_z_old = cc.getLength();
		int max_y_old = cc.getHeight();
		

		boolean[][][] filled = this.getfilledArea(cc);
		boolean[][][] frame = new boolean[max_x_old][max_z_old][max_y_old];
		
		for(int y=0;y<max_y_old;y++){
			//bindind z
			for(int x=0;x<max_x_old;x++){

				for(int z=0;z<max_z_old;z++){
					if(filled[x][z][y]){
						frame[x][z][y]=true;
						break;
					}
				}
				for(int z=max_z_old-1;z>=0;z--){
					if(filled[x][z][y]){
						frame[x][z][y]=true;
						break;
					}
				}
			}
			
			//bindind x
			for(int z=0;z<max_z_old;z++){	
				for(int x=0;x<max_x_old;x++){
					if(filled[x][z][y]){
						frame[x][z][y]=true;
						break;
					}
				}
				for(int x=max_x_old-1;x>=0;x--){
					if(filled[x][z][y]){
						frame[x][z][y]=true;
						break;
					}
				}
			}
			
		}
		
		
		return frame;
		
		
	}	
	
	private boolean[][][] getfilledArea(CuboidClipboard cc){
		boolean[][] dia_tmp_x =null;
		boolean[][] dia_tmp_z =null;
		
		int max_x_old = cc.getWidth();
		int max_z_old = cc.getLength();
		int max_y_old = cc.getHeight();
		

		boolean[][][] area = new boolean[max_x_old][max_z_old][max_y_old];
		boolean[][][] filled = new boolean[max_x_old][max_z_old][max_y_old];
		
		for(int y=0;y<max_y_old;y++){
			for(int x=0;x<max_x_old;x++){
				for(int z=0;z<max_z_old;z++){
					if(cc.getBlock(new Vector(x,y,z)).getId()!=Material.AIR.getId()){
						area[x][z][y]=true;
					}
				}
			}
		}
		
		//System.out.println("area");
		//this.printMap(area);
		
		for(int y=0;y<max_y_old;y++){

			dia_tmp_x = new boolean[max_x_old][max_z_old];
			dia_tmp_z = new boolean[max_x_old][max_z_old];
			int z_s = 0;
			int z_e = 0;
			int x_s = 0;
			int x_e = 0;
			
			//bindind z
			for(int x=0;x<max_x_old;x++){
				
				for(int z=0;z<max_z_old;z++){
					if(area[x][z][y]){
						z_s=z;
						break;
					}
				}
				for(int z=max_z_old-1;z>=0;z--){
					if(area[x][z][y]){
						z_e = z;
						break;
					}
				}
				for(int z =z_s;z<=z_e;z++){
					dia_tmp_z[x][z]=true;
				}
			}
			

			//bindind x
			for(int z=0;z<max_z_old;z++){	
				for(int x=0;x<max_x_old;x++){
					if(area[x][z][y]){
						x_s = x;
						break;
					}
				}
				for(int x=max_x_old-1;x>=0;x--){
					if(area[x][z][y]){
						x_e = x;
						break;
					}
				}
				for(int x =x_s;x<=x_e;x++){
					dia_tmp_x[x][z]=true;
				}
			}
			
			
			for(int x=0;x<max_x_old;x++){
				for(int z=0;z<max_z_old;z++){
					if(dia_tmp_x[x][z]  &&  dia_tmp_z[x][z]){
						filled[x][z][y]=true;
					}
				}
			}
				
			
		}
		
		
		return filled;
		
		
	}
	private int fixBannedBlock(int block_id){
		if(block_id==Material.GOLD_BLOCK.getId()  || 
				block_id==Material.GOLD_ORE.getId()  ||  
				block_id==Material.DIAMOND_BLOCK.getId()  || 	
				block_id==Material.DIAMOND_ORE.getId()  ||    
				block_id==Material.IRON_BLOCK.getId()  ||    
				block_id==Material.IRON_ORE.getId()  ||    
				block_id==Material.COAL_BLOCK.getId()  ||      
				block_id==Material.COAL_ORE.getId()  ||     
				block_id==Material.LAPIS_BLOCK.getId()  ||   
				block_id==Material.LAPIS_ORE.getId()  ||     
				block_id==Material.EMERALD_BLOCK.getId()  || 
				block_id==Material.EMERALD_ORE.getId()  || 
				block_id==Material.BEDROCK.getId()  ||     
				block_id==Material.BEACON.getId()  ||     
				block_id==Material.TNT.getId()  ||      
				block_id==Material.COMMAND.getId()  ||    
				block_id==Material.COMMAND_CHAIN.getId()  ||    
				block_id==Material.COMMAND_MINECART.getId()  ||    
				block_id==Material.COMMAND_REPEATING.getId()  ||   
				block_id==Material.REDSTONE_BLOCK.getId()  ||  
				block_id==Material.REDSTONE_ORE.getId()  ||  
				block_id==Material.GRASS.getId() ||
				block_id==Material.LAVA.getId() ){
				
				block_id = Material.COBBLESTONE.getId();
		}
		return block_id;
	}
	private void printMap(boolean[][][] fillingAirIndeces){
		String buffer ="";
		System.out.println("-------------------------------------");
		buffer ="";
		for(int y=0;y<fillingAirIndeces[0][0].length;y++){
			System.out.println("y = "+y);
			for(int x=0;x<fillingAirIndeces.length;x++){
				for(int z=0;z<fillingAirIndeces[0].length;z++){
					if( fillingAirIndeces[x][z][y]){
						buffer +="@";
					}
					else{
						buffer +=".";
					}
					
				}
				System.out.println(buffer);
				buffer="";
			}
		}
		
	}

}
 		