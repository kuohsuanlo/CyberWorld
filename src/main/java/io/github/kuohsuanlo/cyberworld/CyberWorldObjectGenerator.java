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
    //private final static int schematicBlueprint = 4;
    private final static int schematicBlueprint = 124;
	private final static int schematicNumber = schematicBlueprint*1;
	private int sz_deco=1;
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

	/*    1
	 *  4   2
	 *    3
	 * 
	 * */
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

    public static final String WINDOWS_PATH="plugins\\CyberWorld\\schematics\\";
   
    public static final int MAX_SPACE_HEIGHT = 256; // 0-255
    
    
	private CuboidClipboard[] cc_list = new CuboidClipboard[schematicNumber];
	private ArrayList<CuboidClipboard> cc_list_deco = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_s = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_m = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_l = new ArrayList<CuboidClipboard>();
	private ArrayList<Material> cc_list_most_s = new ArrayList<Material>();
	private ArrayList<Material> cc_list_most_m = new ArrayList<Material>();
	private ArrayList<Material> cc_list_most_l = new ArrayList<Material>();
	

	private ArrayList<boolean[][][]> cc_list_filled_s = new ArrayList<boolean[][][]>();
	private ArrayList<boolean[][][]> cc_list_filled_m = new ArrayList<boolean[][][]>();
	private ArrayList<boolean[][][]> cc_list_filled_l = new ArrayList<boolean[][][]>();
	
	
	private void readSchematic(){
		for(int i =0;i<schematicBlueprint;i++){
			cc_list[i] = Schematic.getSchematic(i+".schematic",0);
			if(cc_list[i].getLength()<=sz_deco*16  && cc_list[i].getWidth()<=sz_deco*16){
				cc_list_deco.add(cc_list[i]);
			}
			
			if(cc_list[i].getLength()<=sz_s*16  && cc_list[i].getWidth()<=sz_s*16){
				cc_list_s.add(cc_list[i]);
				cc_list_filled_s.add(this.getfilledArea(cc_list[i]));
				cc_list_most_s.add(this.getMostMaterial(cc_list[i]));
			}
			else if(cc_list[i].getLength()<=sz_m*16  && cc_list[i].getWidth()<=sz_m*16){
				cc_list_m.add(cc_list[i]);
				cc_list_filled_m.add(this.getfilledArea(cc_list[i]));
				cc_list_most_m.add(this.getMostMaterial(cc_list[i]));
			}
			else if(cc_list[i].getLength()<=sz_l*16  && cc_list[i].getWidth()<=sz_l*16){
				cc_list_l.add(cc_list[i]);
				cc_list_filled_l.add(this.getfilledArea(cc_list[i]));
				cc_list_most_l.add(this.getMostMaterial(cc_list[i]));
			}
			else{
				System.out.print("[CyberWorld] : Error on schematic = "+i+"/ size too large : "+cc_list[i].getWidth()+","+cc_list[i].getLength());
			}
		}
		System.out.print("[CyberWorld] : Final numbers of read schematic(Deco/Small/Medium/Large) = "+cc_list_deco.size()+"/"+cc_list_s.size()+"/"+cc_list_m.size()+"/"+cc_list_l.size());
		cc_list = null;
		
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
	    			else if(y <=32  &&  y >=31){
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
        			else if((z == 5  ||  z==10)  &&  (x%8==3) ){
        				chunkdata.setBlock(x, y, z, Material.GLOWSTONE );
        			}
    			}
    			else if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
    				if((x == 5  ||  x==10)  &&  (z%8==1  ||  z%8==2) ){
    					chunkdata.setBlock(x, y, z, Material.STAINED_CLAY.getId(), (byte) 0x4 );
        			}
    				else if((x == 5  ||  x==10)    &&  (z%8==3) ){
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
    	for(int level=2;level>=0;level--){
    		int road_tube_width = 5;
    		int road_y_middle = LAYER_HEIGHT[level]+road_tube_width/2;
    		int road_tube_thick = 1;

    	    HIGHWAY_TUBES_MATERIAL = new MaterialData(Material.STAINED_GLASS.getId(), (byte)(Math.abs(chkx+chkz)%16)  );
    		for(int y=LAYER_HEIGHT[level];y<LAYER_HEIGHT[level]+road_tube_width*2;y++){
    	    	for(int x=0;x<16;x++){
    	    		for(int z=0;z<16;z++){

        				
    	    			//Road
        				if(y >=LAYER_HEIGHT[level] && y<LAYER_HEIGHT[level]+road_tube_width*2){
        					if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_EAST_WEST ){

              	    			if(chunkdata.getType(x, y, z)!=Material.AIR){
        							if(((z-7.5)*(z-7.5)+(y-road_y_middle)*(y-road_y_middle))<road_tube_width*road_tube_width){
        								chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);
        								if(((z-7.5)*(z-7.5)+(y-road_y_middle)*(y-road_y_middle))>=(road_tube_width-road_tube_thick)*(road_tube_width-road_tube_thick)      ){
            								chunkdata.setRegion(x,y,z,x+1,y+1,z+1,HIGHWAY_TUBES_MATERIAL);
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
            								chunkdata.setRegion(x,y,z,x+1,y+1,z+1,HIGHWAY_TUBES_MATERIAL);
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
            								chunkdata.setRegion(x,y,z,x+1,y+1,z+1,HIGHWAY_TUBES_MATERIAL);
        								}
        							}
              	    				if(((z-7.5)*(z-7.5)+(y-road_y_middle)*(y-road_y_middle))<road_tube_width*road_tube_width){
              	    					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);
              	    					if(((z-7.5)*(z-7.5)+(y-road_y_middle)*(y-road_y_middle))>=(road_tube_width-road_tube_thick)*(road_tube_width-road_tube_thick)      ){
            								chunkdata.setRegion(x,y,z,x+1,y+1,z+1,HIGHWAY_TUBES_MATERIAL);
        								}
        							}
        						}
              	    			//Remove 4 walls on intersection
    		        			if(((x-7.5)*(x-7.5)+(y-road_y_middle)*(y-road_y_middle))<(road_tube_width-road_tube_thick)*(road_tube_width-road_tube_thick)){
    		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);		        			
    		        			}
    		        			if(((z-7.5)*(z-7.5)+(y-road_y_middle)*(y-road_y_middle))<(road_tube_width-road_tube_thick)*(road_tube_width-road_tube_thick)){
    		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);		        			
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
	public ChunkData generateBuilding(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
    	//Building Generation
		int layer;
		int layer_ground=32;
		int layer_start = layer_ground+1;
		int layer_height = 256;
		
		int[] current_size = {cg.s_size,cg.m_size,cg.l_size};
		int[] building_type = {CyberWorldObjectGenerator.DIR_S_BUILDING,CyberWorldObjectGenerator.DIR_M_BUILDING,CyberWorldObjectGenerator.DIR_L_BUILDING};
		Object[] all_lists = {cc_list_s,cc_list_m,cc_list_l};
		Object[] all_filled_lists = {cc_list_filled_s,cc_list_filled_m,cc_list_filled_l};
		

		for(layer=0;layer<3;layer++){
			if(cg.getBuilding(chkx, chkz, layer)==building_type[layer]){	
				ArrayList<CuboidClipboard> current_list =  (ArrayList<CuboidClipboard>) all_lists[layer] ;
				
				int type = cg.getBuildingType(chkx,chkz,layer);
				int sx = (cg.getBuildingStruct(chkx, chkz, layer)-1)/current_size[layer];
				int sz = (cg.getBuildingStruct(chkx, chkz, layer)-1)%current_size[layer];
				int i_start = sx*16;
				int i_max = (sx+1)*16;
				int j_start = sz*16;
				int j_max = (sz+1)*16;
				int i_end = Math.min(current_list.get(type).getWidth(),i_max);
				int j_end = Math.min(current_list.get(type).getLength(),j_max);
				int k_end = Math.min(current_list.get(type).getHeight(),layer_height);
				
				boolean[][][] newIgnoringVoxel ;
				
				if(layer>=1){
					int last_type = cg.getBuildingType(chkx,chkz,layer-1);
					boolean [][][] new_filled = ((ArrayList<boolean[][][]>)all_filled_lists[layer]).get(type);
					boolean [][][] old_filled = ((ArrayList<boolean[][][]>)all_filled_lists[layer-1]).get(last_type);
					
					newIgnoringVoxel = new boolean [i_end][j_end][k_end] ;//returnOverlappingIgnoredVoxel(new_filled,old_filled);
				}
				else{
					newIgnoringVoxel = new boolean [i_end][j_end][k_end] ;
				}
				int block_id  = Material.AIR.getId();
	    		for(int i=i_start;i<i_end;i++){
	    			for(int j=j_start;j<j_end;j++){
		            	for(int k=k_end-1;k>=0;k--){
		    				int y = k+layer_start;
		    				int x = j-j_start;
		    				int z = i-i_start;
		    				if( chunkdata.getType(x, y, z)==Material.AIR){
		    					
		    					//true ==> paste the block
		    					if(!newIgnoringVoxel[i][j][k]){
		    						block_id = current_list.get(type).getBlock(new Vector(i,k,j)).getId();
			    					if(block_id!=Material.AIR.getId()){
			    						if((x%8==4  &&  z%8==4)  &&  y%8 ==0){
				    						switch(layer){
			    							case 0: 
												chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.JACK_O_LANTERN);
			    								break;
			    							case 1: 
												chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.GLOWSTONE);
			    								break;
			    							case 2: 
												chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.SEA_LANTERN);
			    								break;
			    							}
										}
				    					else{
				    						
				    						chunkdata.setBlock(x, y, z, new MaterialData(fixBannedBlock(block_id), (byte)current_list.get(type).getBlock(new Vector(i,k,j)).getData()));
				    							
				    					}
			    					}
		    					}
		    				}
		    			}
		    		}
		    	}
			}
		
		}
    return chunkdata;	
    }
	public ChunkData generateDecoration(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
		int layer_ground=32;
		int layer_start = layer_ground+1;
		int layer_height = 256;
		int sx=0;
		int sz=0;
		int j_start = sx*16;
		int j_max = (sx+1)*16;
		int i_start = sz*16;
		int i_max = (sz+1)*16;
		
		

		int type = rng.nextInt(cc_list_deco.size());
		CuboidClipboard object = cc_list_deco.get(type);
		object.rotate2D(rng.nextInt(4)*90);
		
		
		int j_end = Math.min(object.getWidth(),j_max);
		int i_end = Math.min(object.getLength(),i_max);
		int k_end = Math.min(object.getHeight(),layer_height);
		int block_id;
		
		boolean near_road=false;
		int near_distance =0;
		for(int cx = chkx-near_distance; cx<=chkx+near_distance;cx++){
			for(int cz = chkz-near_distance; cz<=chkz+near_distance;cz++){
				if(cg.getRoadType(cx,cz)==CyberWorldObjectGenerator.DIR_EAST_WEST ||
						cg.getRoadType(cx,cz)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ||
						cg.getRoadType(cx,cz)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
					
					near_road =true;
				}
			}
		}
		if(near_road){
			for(int j=j_start;j<j_end;j++){
	    		for(int i=i_start;i<i_end;i++){
	            	for(int k=k_end-1;k>=0;k--){
	            		int y = k+layer_start;
	    				int x = j-j_start;
	    				int z = i-i_start;


	            		block_id = object.getBlock(new Vector(j,k,i)).getId();
	            		if(block_id!=Material.AIR.getId()){
	            			chunkdata.setBlock(x, y, z, new MaterialData(fixBannedBlock(block_id), (byte)object.getBlock(new Vector(j,k,i)).getData()));
							
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
	            	for(int k=k_end-1;k>=0;k--){
	            		int y = k+layer_start;
	    				int x = j-j_start;
	    				int z = i-i_start;


	            		block_id = object.getBlock(new Vector(j,k,i)).getId();
	            		if(block_id!=Material.AIR.getId()){
	            			chunkdata.setBlock(x, y, z, new MaterialData(fixBannedBlock(block_id), (byte)object.getBlock(new Vector(j,k,i)).getData()));
							
	            		}
	            		
            		}
            	}
			}
		}
		



	    
        return chunkdata;
    	
    }
	private Material getMostMaterial(CuboidClipboard cc){

		int[] id_times = new int[500];
		int now_id =0;
		for(int y=0;y<cc.getHeight();y++){
			for(int x=0;x<cc.getWidth();x++){
				for(int z=0;z<cc.getLength();z++){
					now_id = cc.getBlock(new Vector(x,y,z)).getId();
					if(now_id<id_times.length  &&  now_id!=Material.AIR.getId()){
						id_times[now_id]++;
					}
					
				}
			}
		}
		
		int maxIndex = 0;
		int max=0;
		for (int i = 0; i < id_times.length; i++) {
		    if (id_times[i] > max) {
		        max = id_times[i];
		        maxIndex = i;
		    }
		}
		return Material.getMaterial(maxIndex);
		
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
		
		for(int y=0;y<max_y_old;y++){
			int z_s = 0;
			int z_e = 0;
			int x_s = 0;
			int x_e = 0;
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
			}
			
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
			}
			dia_tmp_x = new boolean[max_x_old][max_z_old];
			dia_tmp_z = new boolean[max_x_old][max_z_old];
			for(int x=0;x<max_x_old;x++){
				for(int z =z_s;z<=z_e;z++){
					dia_tmp_z[x][z]=true;
				}
			}
			for(int z=0;z<max_z_old;z++){
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


}
 		