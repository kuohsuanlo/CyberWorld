package io.github.kuohsuanlo.cyberworld;

import static java.lang.System.arraycopy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;

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
    private final CityStreetGenerator cg;
    private long testingSeed= 1205;
    
    private long schematicBlueprint = 0;
	private int sz_deco=1;
	private int sz_s=3;
	private int sz_m=6;
	private int sz_l=8;

    private final TerrainHeightGenerator hcg;
    private final static int GROUND_LEVEL = 60;
    private final static int[] all_building_level = {GROUND_LEVEL+3,GROUND_LEVEL+3,GROUND_LEVEL+3};
    private final static int[] underground_building_level = {3,3,3};
    private final static int[] LAYER_HEIGHT = {GROUND_LEVEL+20,GROUND_LEVEL+40,GROUND_LEVEL+80};
    private final int TERRAIN_OCTAVE = 6;
    private final int TERRAIN_HEIGHT = 80;
    
    public static final int CITY_X = 1000;
    public static final int CITY_Z = 1000;
    
	public CyberWorldObjectGenerator(CyberWorldBiomeGenerator b){
		rng = new Random();
		rng.setSeed(testingSeed);
		bm_rng = new Random();
		bm_rng.setSeed(testingSeed);

		readSchematic("default");
		readSchematic("import");
		readSchematic("underground");
		readSchematic("citysurface");
		cg = new CityStreetGenerator(b,CITY_X,CITY_Z,rng,sz_l,cc_list_s.size(),cc_list_m.size(),cc_list_l.size(),sz_s,sz_m,sz_l,1,1,1);
		hcg = new TerrainHeightGenerator(rng,TERRAIN_HEIGHT,TERRAIN_OCTAVE,GROUND_LEVEL);
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
    
	//Paving Roads
    private static Material ROAD_SIDEWALK_MATERIAL_1 = Material.STEP;
    private static Material HIGHWAY_MATERIAL = Material.QUARTZ_BLOCK;
    private static MaterialData ROAD_MATERIAL = new MaterialData(Material.STAINED_CLAY.getId(),(byte)0x9);

    //Paving High Roads

    private static Material HIGHWAY_FENCE = Material.IRON_FENCE;
    private static MaterialData HIGHWAY_TUBES_MATERIAL = null;
	
	private static int[] LAYER_WIDTH = {10,10,10};
	private static int[] LAYER_SPACE = {(16-LAYER_WIDTH[0])/2,(16-LAYER_WIDTH[1])/2,(16-LAYER_WIDTH[2])/2};
	private static int[] LAYER_SW_WD = {1,1,1};
	
	private static int[] LAYER_SRT = {0+LAYER_SPACE[0],0+LAYER_SPACE[1],0+LAYER_SPACE[2]};
	private static int[] LAYER_END = {15-LAYER_SPACE[0],15-LAYER_SPACE[1],15-LAYER_SPACE[2]};
	private static int[] LAYER_SW_MIN_END = {LAYER_SRT[0]+LAYER_SW_WD[0],LAYER_SRT[1]+LAYER_SW_WD[1],LAYER_SRT[2]+LAYER_SW_WD[2]};
	private static int[] LAYER_SW_MAX_END = {LAYER_END[0]-LAYER_SW_WD[0],LAYER_END[1]-LAYER_SW_WD[1],LAYER_END[2]-LAYER_SW_WD[2]};

    public static final String WINDOWS_PATH="./plugins/CyberWorld/schematics/";
   
    public static final int MAX_SPACE_HEIGHT = 256; // 0-255
    
    

	private CuboidClipboard cc_tmp = null;
	private CuboidClipboard cc_backup = null;
	private ArrayList<CuboidClipboard> cc_list_citysurface = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_citysurface_b = new ArrayList<CuboidClipboard>();
	
	private ArrayList<CuboidClipboard> cc_list_deco = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_s = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_m = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_l = new ArrayList<CuboidClipboard>();

	private ArrayList<CuboidClipboard> cc_list_deco_b = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_s_b = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_m_b = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_l_b = new ArrayList<CuboidClipboard>();
	
	private ArrayList<CuboidClipboard> cc_list_u_deco = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_u_s = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_u_m = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_u_l = new ArrayList<CuboidClipboard>();

	private ArrayList<CuboidClipboard> cc_list_u_deco_b = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_u_s_b = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_u_m_b = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_u_l_b = new ArrayList<CuboidClipboard>();

	private ArrayList<CuboidClipboard> deco =null;
	private ArrayList<CuboidClipboard> s =null;
	private ArrayList<CuboidClipboard> m =null;
	private ArrayList<CuboidClipboard> l =null;
	private ArrayList<CuboidClipboard> decob =null;
	private ArrayList<CuboidClipboard> sb =null;
	private ArrayList<CuboidClipboard> mb =null;
	private ArrayList<CuboidClipboard> lb =null;
	private void readSchematic(String building_type){

		if(building_type.equals("underground")){
			deco = cc_list_u_deco;
			s = cc_list_u_s;
			m = cc_list_u_m;
			l = cc_list_u_l;
			decob = cc_list_u_deco_b;
			sb = cc_list_u_s_b;
			mb = cc_list_u_m_b;
			lb = cc_list_u_l_b;
		}
		else if(building_type.equals("citysurface")){
			deco = cc_list_citysurface;
			decob = cc_list_citysurface_b;
		}
		else{
			deco = cc_list_deco;
			s = cc_list_s;
			m = cc_list_m;
			l = cc_list_l;
			decob = cc_list_deco_b;
			sb = cc_list_s_b;
			mb = cc_list_m_b;
			lb = cc_list_l_b;
		}
		try(Stream<Path> paths = Files.walk(Paths.get(CyberWorldObjectGenerator.WINDOWS_PATH+building_type))) {
		    paths.forEach(filePath -> {
		        if (Files.isRegularFile(filePath)) {
		            cc_tmp = Schematic.getSchematic(filePath.toString(),0);
					cc_backup = Schematic.getSchematic(filePath.toString(),0);
					if(cc_tmp.getLength()<=sz_deco*16  && cc_tmp.getWidth()<=sz_deco*16){
						deco.add(cc_tmp);
						decob.add(cc_backup);
					}
					else if(cc_tmp.getLength()<=sz_s*16  && cc_tmp.getWidth()<=sz_s*16){
						s.add(cc_tmp);
						sb.add(cc_backup);
					}
					else if(cc_tmp.getLength()<=sz_m*16  && cc_tmp.getWidth()<=sz_m*16){
						m.add(cc_tmp);
						mb.add(cc_backup);
					}
					else if(cc_tmp.getLength()<=sz_l*16  && cc_tmp.getWidth()<=sz_l*16){
						l.add(cc_tmp);
						lb.add(cc_backup);
					}
					else{
						System.out.print("[CyberWorld] : Error on "+building_type+" schematic = "+filePath.toString()+"/ size too large : "+cc_tmp.getWidth()+","+cc_tmp.getLength());
					}
		        }
		    });
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		System.out.print("[CyberWorld] : Final numbers of " + building_type + " read schematic(Deco/Small/Medium/Large) = "+deco.size()+"/"+s.size()+"/"+m.size()+"/"+l.size());
		
	}
	public ChunkData generateBottom(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
        for(int y=0;y<3;y++){
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
        for(int y=GROUND_LEVEL;y<=GROUND_LEVEL+3;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			//Paving Ground
	    			if(y  == GROUND_LEVEL+2){
	    				  int block_id = cc_list_citysurface.get(0).getBlock(new Vector(x,0,z)).getId();
	    				  int block_data = (byte) cc_list_citysurface.get(0).getBlock(new Vector(x,0,z)).getData();
	    				  chunkdata.setBlock(x, y, z,new MaterialData(block_id,(byte) block_data));
	    			}
	    			else if( y ==GROUND_LEVEL+1){
	    				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.STONE);
		        	}
	    			else if(y ==GROUND_LEVEL){
	    				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.DIRT);
		        	}
	    		}
	    	}
	    }
		
        return chunkdata;
    }
	public ChunkData generateFactoryRoad(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes, int road_layer){
		//Paving Roads
		int y;
    	if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_EAST_WEST ){
        	chunkdata.setRegion(0,GROUND_LEVEL,0,16,GROUND_LEVEL+3,16,ROAD_MATERIAL);
		}
		else if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
	    	chunkdata.setRegion(0,GROUND_LEVEL,0,16,GROUND_LEVEL+3,16,ROAD_MATERIAL);
		}
		else if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
	    	chunkdata.setRegion(0,GROUND_LEVEL,0,16,GROUND_LEVEL+3,16,ROAD_MATERIAL);
		}

    	y=GROUND_LEVEL+3;
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
	    
		
		//road line
		y=GROUND_LEVEL+2;
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
	public ChunkData generateCityRoad(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
		//Paving Roads
		int y;
    	if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_EAST_WEST ){
        	chunkdata.setRegion(0,GROUND_LEVEL,0,16,GROUND_LEVEL+3,16,ROAD_MATERIAL);
		}
		else if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
	    	chunkdata.setRegion(0,GROUND_LEVEL,0,16,GROUND_LEVEL+3,16,ROAD_MATERIAL);
		}
		else if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
	    	chunkdata.setRegion(0,GROUND_LEVEL,0,16,GROUND_LEVEL+3,16,ROAD_MATERIAL);
		}

    	y=GROUND_LEVEL+3;
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
	    
		
		//road line
		y=GROUND_LEVEL+2;
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
		int sewer_pipe_height= GROUND_LEVEL-16;
		int pillar_width = 3;
	    for(int y=0;y<GROUND_LEVEL+3;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			if(y >=2 && y <GROUND_LEVEL+1){
	    				//Building Sewer Pipe, Sewer Ground
		        		if ( cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_EAST_WEST ){ //ROAD

		    				double d = rng.nextDouble();
		        			double r = rng.nextDouble();
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

		    				double d = rng.nextDouble();
		        			double r = rng.nextDouble();
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

		    				double d = rng.nextDouble();
		        			double r = rng.nextDouble();
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
		        			
		    				double d = 0;//rng.nextDouble();
		        			if((x<0+pillar_width ||  x>15-pillar_width)  &&  (z<0+pillar_width ||  z>15-pillar_width)){
		        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.BRICK);
		        			}
		        			else{
			        			if(y>=GROUND_LEVEL-5){
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

		        		}
		        		
		        		//building pipe
		        		
	    				if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH){

		    				double d = rng.nextDouble();
		        			double r = rng.nextDouble();
	    					
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

		    				double d = rng.nextDouble();
		        			double r = rng.nextDouble();
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

		    				double d = rng.nextDouble();
		        			double r = rng.nextDouble();
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
	    			if(y==GROUND_LEVEL+2  &&  cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION){
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
	    			if(y>sewer_pipe_height  &&  y<=GROUND_LEVEL+1  &&  cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION){
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
		
	    for(int y=3;y<GROUND_LEVEL+1;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			//double d =  rng.nextDouble();
	    			if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_BUILDING){
            			if(z>=0+cave_shift  &&  z<=cave_width+cave_shift){
            				if( ((y-(cave_height))* (y-(cave_height)) + (z-7.5)*(z-7.5))<  (cave_width)*(cave_width)){
    	        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);	
            				}
            			}
    				
            			if(x>=0+cave_shift  &&  x<=cave_width+cave_shift){
	        				if( ((y-(cave_height))* (y-(cave_height)) + (x-7.5)*(x-7.5))<  (cave_width)*(cave_width)){
	        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.AIR);	
	        				}
            			}
	    			}			
	    		}
	    	}
	    }    

	    
	    //Building Sewer gate
	    if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
		    for(int y=0;y<GROUND_LEVEL+1;y++){
		    	for(int x=0;x<16;x++){
		    		for(int z=0;z<16;z++){
	    				if((y>=5  &&  y<GROUND_LEVEL+1)  &&  ( (x<=0  ||  x>=15)  ||  (z<=0  ||  z>=15) )){
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
	    				for(int y=0;y<GROUND_LEVEL+1;y++){
	    					if((y>=5  &&  y<GROUND_LEVEL+1)  &&  ( (x<=0  ||  x>=15)  ||  (z<=0  ||  z>=15) )){
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
	   		
	   		chunkdata.setRegion(x,3,z,x+1,GROUND_LEVEL+3,z+1,Material.LADDER);
	 		
	   		chunkdata.setRegion(x,GROUND_LEVEL+3,z,x+1,GROUND_LEVEL+1,z+1,Material.TRAP_DOOR);

	 		x=15;
	 		z=15;
	 		chunkdata.setRegion(x,3,z,x+1,GROUND_LEVEL+3,z+1,Material.LADDER);
	   		chunkdata.setRegion(x,GROUND_LEVEL+3,z,x+1,GROUND_LEVEL+4,z+1,Material.TRAP_DOOR);
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
    	    					chunkdata.setRegion(x,GROUND_LEVEL+3,z,x+1,LAYER_HEIGHT[level],z+1,HIGHWAY_MATERIAL);
    	        			}
    	  	    		}
    	  	    		else if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
    	  	    			if((z == 7  ||  z==8)  &&  (x==7  ||  x==8) ){
    	    					chunkdata.setRegion(x,GROUND_LEVEL+3,z,x+1,LAYER_HEIGHT[level],z+1,HIGHWAY_MATERIAL);
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
		int[] current_size = cg.a_size;
		
		int[] building_type = {CyberWorldObjectGenerator.DIR_S_BUILDING,CyberWorldObjectGenerator.DIR_M_BUILDING,CyberWorldObjectGenerator.DIR_L_BUILDING};
		Object[] all_lists = {cc_list_s,cc_list_m,cc_list_l};
		Object[] all_b_lists = {cc_list_s_b,cc_list_m_b,cc_list_l_b};
		
		int s_layer_nubmer=start_of_layer;
		for(layer=s_layer_nubmer;layer>=0;layer--){
			if(cg.getBuilding(chkx, chkz, layer)==building_type[layer]){	
				int layer_start = all_building_level[layer];
				int type = cg.getBuildingType(chkx,chkz,layer);
				int struct_type = cg.getBuildingStruct(chkx, chkz, layer);
				
				//fixing biome type
				if(struct_type>=1){
					int sx = (struct_type-1)/current_size[layer];
					int sz = (struct_type-1)%current_size[layer];
					int i_start = sx*16;
					int i_max = (sx+1)*16;
					int j_start = sz*16;
					int j_max = (sz+1)*16;

					int angle = cg.getBuildingRotation(chkx,chkz,layer)*90;
					//int angle = 0;
					
					ArrayList<CuboidClipboard> current_list =  (ArrayList<CuboidClipboard>) all_lists[layer] ;
					ArrayList<CuboidClipboard> current_b_list =  (ArrayList<CuboidClipboard>) all_b_lists[layer] ;

					long chunk_seed = cg.getBuildingSeed(chkx, chkz, layer);
					int block_id  = Material.AIR.getId();
					byte block_data  = 0;
					
					boolean[][][] fillingAirIndeces ;

					//rotating
					current_list.get(type).rotate2D(angle);

					int[] ori_idx_i = IntStream.range(0, current_list.get(type).getWidth()).toArray(); 
					int[] ori_idx_j = IntStream.range(0, current_list.get(type).getLength()).toArray();
					int r_i = (current_list.get(type).getWidth()%2);
					int r_j = (current_list.get(type).getLength()%2);
					int[] expended_idx_i = this.generateExpandedSequence(ori_idx_i, Math.min(4+4*layer+r_i,current_list.get(type).getWidth()/2+r_i), current_size[layer]*16);
					int[] expended_idx_j = this.generateExpandedSequence(ori_idx_j, Math.min(4+4*layer+r_j,current_list.get(type).getLength()/2+r_j), current_size[layer]*16);

					
					int i_end = Math.min(expended_idx_i.length,i_max);
					int j_end = Math.min(expended_idx_j.length,j_max);
					int k_end = current_list.get(type).getHeight();
					
					if(layer!=s_layer_nubmer){
						fillingAirIndeces = this.getfilledArea(current_list.get(type));
						//printMap(fillingAirIndeces);
					}
					else{
						fillingAirIndeces = new boolean [i_max][j_max][k_end] ;
					}
					
					
	            	for(int k=0;k<k_end;k++){
	    				int y = k+layer_start;
	    				for(int i=i_start;i<i_end;i++){
	    					for(int j=j_start;j<j_end;j++){
	    						int x = j-j_start;
	    						int z = i-i_start;
			    				
			    				block_id = current_list.get(type).getBlock(new Vector(expended_idx_i[i],k,expended_idx_j[j])).getId();
			            		block_data = (byte) current_list.get(type).getBlock(new Vector(expended_idx_i[i],k,expended_idx_j[j])).getData();
		    					//replacing illegal block, and light blocks
			            		boolean isLightSource=false;
			    				if(block_id!=Material.AIR.getId()){
		    						if((x%8==4  &&  z%8==4)  &&  y%8 ==0){
			    						switch(layer){
		    							case 0: 
		    								block_id = Material.JACK_O_LANTERN.getId();
		    								isLightSource=true;
		    								break;
		    							case 1: 
		    								block_id = Material.GLOWSTONE.getId();
		    								isLightSource=true;
		    								break;
		    							case 2: 
		    								block_id = Material.SEA_LANTERN.getId();
		    								isLightSource=true;
		    								break;
		    							}
									}
		    					}
			    				
			    				//clearing overlapping area
			    				if(fillingAirIndeces[expended_idx_i[i]][expended_idx_j[j]][k]){
			    					chunkdata.setBlock(x, y, z,Material.AIR);
			    				}
			    				if(block_id!=Material.AIR.getId()){
			    					int fixed_id = fixBannedBlock(block_id);
			    					if(fixed_id == block_id  &&  isLightSource==false){
			    						chunkdata.setBlock(x, y, z, getReplacedMaterial(bm_rng,block_id,block_data,chunk_seed ));
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

				}

			}
		}
		return chunkdata;	
    }
    /*
    public ChunkData generateBuilding(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes, int start_of_layer){
    	//Building Generation
		int layer;
		int[] current_size = cg.a_size;
		
		int[] building_type = {CyberWorldObjectGenerator.DIR_S_BUILDING,CyberWorldObjectGenerator.DIR_M_BUILDING,CyberWorldObjectGenerator.DIR_L_BUILDING};
		Object[] all_lists = {cc_list_s,cc_list_m,cc_list_l};
		Object[] all_b_lists = {cc_list_s_b,cc_list_m_b,cc_list_l_b};
		
		int s_layer_nubmer=start_of_layer;
		for(layer=s_layer_nubmer;layer>=0;layer--){
			if(cg.getBuilding(chkx, chkz, layer)==building_type[layer]){	
				int layer_start = all_building_level[layer];
				int type = cg.getBuildingType(chkx,chkz,layer);
				int struct_type = cg.getBuildingStruct(chkx, chkz, layer);
				
				//fixing biome type
				if(struct_type>=1){
					int sx = (struct_type-1)/current_size[layer];
					int sz = (struct_type-1)%current_size[layer];
					int i_start = sx*16;
					int i_max = (sx+1)*16;
					int j_start = sz*16;
					int j_max = (sz+1)*16;

					int angle = cg.getBuildingRotation(chkx,chkz,layer)*90;
					//int angle = 0;
					
					ArrayList<CuboidClipboard> current_list =  (ArrayList<CuboidClipboard>) all_lists[layer] ;
					ArrayList<CuboidClipboard> current_b_list =  (ArrayList<CuboidClipboard>) all_b_lists[layer] ;
					
					long chunk_seed = cg.getBuildingSeed(chkx, chkz, layer);
					int block_id  = Material.AIR.getId();
					byte block_data  = 0;
					
					boolean[][][] fillingAirIndeces ;

					//rotating
					current_list.get(type).rotate2D(angle);
					int i_end = Math.min(current_list.get(type).getWidth(),i_max);
					int j_end = Math.min(current_list.get(type).getLength(),j_max);
					int k_end = current_list.get(type).getHeight();
					
					if(layer!=s_layer_nubmer){
						fillingAirIndeces = this.getfilledArea(current_list.get(type));
						//printMap(fillingAirIndeces);
					}
					else{
						fillingAirIndeces = new boolean [i_end][j_end][k_end] ;
					}
					

	            	for(int k=0;k<k_end;k++){
	    				int y = k+layer_start;
	    				for(int i=i_start;i<i_end;i++){
	    					for(int j=j_start;j<j_end;j++){
	    						int x = j-j_start;
	    						int z = i-i_start;
			    				
			    				block_id = current_list.get(type).getBlock(new Vector(i,k,j)).getId();
			            		block_data = (byte) current_list.get(type).getBlock(new Vector(i,k,j)).getData();
		    					//replacing illegal block, and light blocks
			            		boolean isLightSource=false;
			    				if(block_id!=Material.AIR.getId()){
		    						if((x%8==4  &&  z%8==4)  &&  y%8 ==0){
			    						switch(layer){
		    							case 0: 
		    								block_id = Material.JACK_O_LANTERN.getId();
		    								isLightSource=true;
		    								break;
		    							case 1: 
		    								block_id = Material.GLOWSTONE.getId();
		    								isLightSource=true;
		    								break;
		    							case 2: 
		    								block_id = Material.SEA_LANTERN.getId();
		    								isLightSource=true;
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
			    					if(fixed_id == block_id  &&  isLightSource==false){
			    						chunkdata.setBlock(x, y, z, getReplacedMaterial(bm_rng,block_id,block_data,chunk_seed ));
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
				}
	    		
			}
		}
		return chunkdata;	
    }
    */
	
	public ChunkData generateUnderGroundBuilding(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes, int start_of_layer){
    	//Building Generation
		int layer;
		int[] current_size = cg.a_size;
		
		int[] building_type = {CyberWorldObjectGenerator.DIR_S_BUILDING,CyberWorldObjectGenerator.DIR_M_BUILDING,CyberWorldObjectGenerator.DIR_L_BUILDING};
		Object[] all_lists = {cc_list_u_s,cc_list_u_m,cc_list_u_l};
		Object[] all_b_lists = {cc_list_u_s_b,cc_list_u_m_b,cc_list_u_l_b};
		
		int s_layer_nubmer=start_of_layer;
		for(layer=s_layer_nubmer;layer>=0;layer--){
			if(cg.getBuilding(chkx, chkz, layer)==building_type[layer]){	
				int layer_start = underground_building_level[layer];
				int struct_type = cg.getBuildingStruct(chkx, chkz, layer);
				
				int tmp_size = ((ArrayList<CuboidClipboard>) all_lists[layer]).size();
				if(tmp_size>0){
					int type = cg.getBuildingType(chkx,chkz,layer)%(tmp_size);
					//fixing biome type
					if(struct_type>=1){
						int sx = (struct_type-1)/current_size[layer];
						int sz = (struct_type-1)%current_size[layer];
						int i_start = sx*16;
						int i_max = (sx+1)*16;
						int j_start = sz*16;
						int j_max = (sz+1)*16;

						int angle = cg.getBuildingRotation(chkx,chkz,layer)*90;
						//int angle = 0;
						
						ArrayList<CuboidClipboard> current_list =  (ArrayList<CuboidClipboard>) all_lists[layer] ;
						ArrayList<CuboidClipboard> current_b_list =  (ArrayList<CuboidClipboard>) all_b_lists[layer] ;
						

						
						
						long chunk_seed = cg.getBuildingSeed(chkx, chkz, layer);
						int block_id  = Material.AIR.getId();
						byte block_data  = 0;
						
						boolean[][][] fillingAirIndeces ;

						//rotating
						current_list.get(type).rotate2D(angle);
						int i_end = Math.min(current_list.get(type).getWidth(),i_max);
						int j_end = Math.min(current_list.get(type).getLength(),j_max);
						int k_end = current_list.get(type).getHeight();
						
						if(layer!=s_layer_nubmer){
							fillingAirIndeces = this.getfilledArea(current_list.get(type));
							//printMap(fillingAirIndeces);
						}
						else{
							fillingAirIndeces = new boolean [i_end][j_end][k_end] ;
						}
						
						
		            	for(int k=0;k<k_end;k++){
		            		int y = k+layer_start;
		            		if(y<Math.max(k+layer_start,GROUND_LEVEL)){
		            			for(int i=i_start;i<i_end;i++){
		            				for(int j=j_start;j<j_end;j++){
					    				int x = j-j_start;
					    				int z = i-i_start;
				            			block_id = current_list.get(type).getBlock(new Vector(i,k,j)).getId();
					            		block_data = (byte) current_list.get(type).getBlock(new Vector(i,k,j)).getData();
				    					//replacing illegal block, and light blocks
					            		boolean isLightSource=false;
					    				if(block_id!=Material.AIR.getId()){
				    						if((x%8==4  &&  z%8==4)  &&  y%8 ==0){
					    						switch(layer){
				    							case 0: 
				    								block_id = Material.JACK_O_LANTERN.getId();
				    								isLightSource=true;
				    								break;
				    							case 1: 
				    								block_id = Material.GLOWSTONE.getId();
				    								isLightSource=true;
				    								break;
				    							case 2: 
				    								block_id = Material.SEA_LANTERN.getId();
				    								isLightSource=true;
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
					    					if(fixed_id == block_id  &&  isLightSource==false){
					    						chunkdata.setBlock(x, y, z, getReplacedMaterial(bm_rng,block_id,block_data,chunk_seed ));
					    					}
					    					else{
					    						chunkdata.setBlock(x, y, z,new MaterialData(fixed_id));
					    					}
					    				}
				            		}
				    			}
				    		}
				    	}
			    		//rotating back
						current_list.get(type).rotate2D(360-angle);
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
		int layer_start = GROUND_LEVEL+3;
		int sx=0;
		int sz=0;
		int i_start = sx*16;
		int i_max = (sx+1)*16;
		int j_start = sz*16;
		int j_max = (sz+1)*16;
		
		if(cc_list_deco.size()>0){

			int type = rng.nextInt(cc_list_deco.size());
			CuboidClipboard object = cc_list_deco.get(type);
			CuboidClipboard object_b = cc_list_deco_b.get(type);
			int angle = rng.nextInt(4)*90;
			
			//replacing rng seed
			long chunk_seed = cg.getBuildingSeed(chkx, chkz, 0);
			
			int block_id  = Material.AIR.getId();
			byte block_data =0;
	
			
	
			
			//rotating
			object.rotate2D(angle);
			int i_end = i_end = Math.min(object.getWidth(),i_max);
			int j_end = j_end = Math.min(object.getLength(),j_max);
			int k_end = k_end = object.getHeight();
			
			
			boolean[][][] fillingAirIndeces = this.getfilledArea(object);
	
			
			
			
			
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
		    				int x = i-i_start;
		    				int z = j-j_start;

			            	for(int k=k_end-1;k>=0;k--){
			            		int y = k+layer_start;
	
			            		
			            		if(fillingAirIndeces[i][j][k]){
			    					chunkdata.setBlock(x, y, z,Material.AIR);
			    				}
			            		
			            		block_id = object.getBlock(new Vector(i,k,j)).getId();
			            		block_data = (byte) object.getBlock(new Vector(i,k,j)).getData();
			            		
			            		if(block_id!=Material.AIR.getId()){
			    					int fixed_id = fixBannedBlock(block_id);
			    					if(fixed_id == block_id){
			    						chunkdata.setBlock(x, y, z, getReplacedMaterial(bm_rng,block_id,block_data,chunk_seed ));
			    					}
			    					else{
			    						chunkdata.setBlock(x, y, z,new MaterialData(fixed_id));
			    					}
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
	
	
		            		
		            		
		            		
		            		if(fillingAirIndeces[i][j][k]){
		    					chunkdata.setBlock(x, y, z,Material.AIR);
		    				}
		    				//Bug: finding 3d volumn has bug in getfilledArea
		            		block_id = object.getBlock(new Vector(i,k,j)).getId();
		            		block_data = (byte) object.getBlock(new Vector(i,k,j)).getData();
		            		
		            		if(block_id!=Material.AIR.getId()){
		    					int fixed_id = fixBannedBlock(block_id);
		    					if(fixed_id == block_id){
		    						chunkdata.setBlock(x, y, z, getReplacedMaterial(bm_rng,block_id,block_data,chunk_seed ));
		    					}
		    					else{
		    						chunkdata.setBlock(x, y, z,new MaterialData(fixed_id));
		    					}
		    				} 
		            		
	            		}
	            	}
				}
			}
	
			//rotating back
			object.rotate2D(360-angle);
	
	
			//replacing back
			i_end = object.getWidth();
			j_end = object.getLength();
			k_end = object.getHeight();
			block_id  = Material.AIR.getId();
			
			for(int i=0;i<i_end;i++){
				for(int j=0;j<j_end;j++){
	            	for(int k=0;k<k_end;k++){
	            		block_id = object_b.getBlock(new Vector(i,k,j)).getId();
	            		object.setBlock(new Vector(i,k,j),new BaseBlock(block_id));
	            		
	            	}
				}
	    	}
			
		

		}
        return chunkdata;
    	
    }
	public ChunkData generateFactoryGround(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){

    	for(int x=0;x<16;x++){
    		for(int z=0;z<16;z++){
    			
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
	    
        return chunkdata;
    }
	public ChunkData generateTerrain(ChunkData chunkdata, Random random, int chkx, int chkz, BiomeGrid biomes){
		Material ground = null;

		
    	for(int x=0;x<16;x++){
    		for(int z=0;z<16;z++){
    			
    			int height = Math.round(hcg.generateHeight(chkx*16+x, chkz*16+z,false))+3;
    			if(height>=3){
        			chunkdata.setRegion(x,3,z,x+1,height-3,z+1,Material.STONE);
        			chunkdata.setRegion(x,height-3,z,x+1,height,z+1,Material.DIRT);		
        			if(biomes.getBiome(x, z).equals(Biome.FOREST)){
        				ground = Material.GRASS;
        			}
        			else if(biomes.getBiome(x, z).equals(Biome.PLAINS)){
        				ground = Material.DIRT;
        			}
        			else if(biomes.getBiome(x, z).equals(Biome.DESERT)){
        				ground = Material.SAND;
        			}
        			else if(biomes.getBiome(x, z).equals(Biome.SWAMPLAND)){
        				ground = Material.WATER;
        			}
        			else if(biomes.getBiome(x, z).equals(Biome.RIVER)){
        				ground = Material.WATER;
        			}
        			else {
        				ground = Material.CLAY;
        			}
        			switch(random.nextInt(3)){
	        			case 0:
	        				chunkdata.setRegion(x,height,z,x+1,height+1,z+1,Material.DIRT);
	        				break;
	        			case 1:
	        				chunkdata.setRegion(x,height,z,x+1,height+1,z+1,Material.GRAVEL);
	        				break;
	        			case 2:
	        				chunkdata.setRegion(x,height,z,x+1,height+1,z+1,ground);
	        				break;
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
		int sewer_pipe_height= GROUND_LEVEL-16;
	    for(int y=2;y<GROUND_LEVEL+3;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			if(y >=2 && y <GROUND_LEVEL+1){
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
	
	private static final int[] STAIRS_LIST = {67,108,109,114,128,134,135,136,156,163,164,180,203};
	private static final int[] SLABS_LIST = {44,126,205};
	private static final int[] FENCE_LIST = {85,113,188,189,190,191,192};
	private static final int[] BLOCKS_LIST = {1,4,5,17,24,43,45,82,87,88,98,121,125,155,162,168,179,181,201,202,204,206};
	private static final int[] BLOCKS_DMAX = {7,1,6, 4, 3, 8, 1, 1, 1, 1, 4,  1,  6,  3,  2,  3,  3,  1,  1,  1,  1,  1};
	private MaterialData getReplacedMaterial(Random replace_rng, int id,byte original_data,long seeds){
		if(id==Material.GLASS.getId()  ||   
				id==Material.THIN_GLASS.getId() ||  
				id==Material.AIR.getId()){
			return new MaterialData(id);
		}
		else if(id==Material.STAINED_GLASS.getId()  || 
				id==Material.STAINED_GLASS_PANE.getId()){
			replace_rng.setSeed(seeds+id*17);
			return new MaterialData(id,(byte)replace_rng.nextInt(16));
		}
		else if(id==Material.STAINED_CLAY.getId()  || 
				id==Material.HARD_CLAY.getId()){
			replace_rng.setSeed(seeds+id*17);
			return new MaterialData(id,(byte)replace_rng.nextInt(16));
		}
		else if(id==Material.WOOL.getId()){
			replace_rng.setSeed(seeds+id*17);
			return new MaterialData(id,(byte)replace_rng.nextInt(16));
		}
		//Stairs
		else if(this.contains(STAIRS_LIST, id)){
			replace_rng.setSeed(seeds+id*17);
			return new MaterialData(STAIRS_LIST[replace_rng.nextInt(STAIRS_LIST.length)],original_data);
		}
		//Slabs
		else if(this.contains(SLABS_LIST, id)){
			replace_rng.setSeed(seeds+id*17);
			return new MaterialData(SLABS_LIST[replace_rng.nextInt(SLABS_LIST.length)],original_data);
		}
		//Fences
		else if(this.contains(FENCE_LIST, id)){
			replace_rng.setSeed(seeds+id*17);
			return new MaterialData(FENCE_LIST[replace_rng.nextInt(FENCE_LIST.length)],original_data);
		}
		//Blocks
		else if(this.contains(BLOCKS_LIST, id)){
			replace_rng.setSeed(seeds+id*17);
			int new_block_idx = replace_rng.nextInt(BLOCKS_LIST.length);
			return new MaterialData(BLOCKS_LIST[new_block_idx],(byte)replace_rng.nextInt( BLOCKS_DMAX[new_block_idx]));
		}
		return new MaterialData(id);
		
	}
	private static boolean contains(final int[] arr, final int key) {
	    return Arrays.stream(arr).anyMatch(i -> i == key);
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
	
	/*
	public static void main(String[] args) {
		int[] s = IntStream.range(0,5).toArray(); 
		int[] ans = CyberWorldObjectGenerator.generateExpandedSequence(s,2, 16);
		for(int i=0;i<ans.length;i++){
			System.out.print(ans[i]+",");
		}

		System.out.print("\n"+ans.length);
		
	}*/
	private int[] generateExpandedSequence(int[] ori, int l, int max_size){
		int[] ans =null;
		int middle =0;
		int t  =0;
		int new_end  =0;
		int new_middle  =0;
		int current_size = ori.length;
		
		middle = ori.length/2;
		if(l%2==0){
			t = (l)/2;
		}
		else{
			t = (l+1)/2;
		}
		
		if( t==0 ||  l==current_size){
			return ori;
		}
		int ans_bound = current_size;
		while(ans_bound+2*t<=max_size){
			ans_bound+=2*t;
		}
		ans  = new int[ans_bound];
		while(current_size+2*t<=max_size){
			new_end = (ori.length+2*t);
			new_middle = (current_size+2*t)/2;
			//left
			for(int i=0;i<middle;i++){
				ans[i]=ori[i];
			}
			//left dup
			for(int i=0;i<t;i++){
				ans[middle+i]=ori[middle-t+i+1];
			}
			if(ori.length%2==1){
				//right
				for(int i=middle+1;i<ori.length;i++){
					ans[2*t+i]=ori[i];
				}
				//middle
				ans[new_middle] = ori[middle];
				//right dup
				for(int i=1;i<=t;i++){
					ans[new_middle+i]=ori[middle-t+i];
				}
			}
			else{
				//right
				for(int i=middle;i<ori.length;i++){
					ans[2*t+i]=ori[i];
				}
				//right dup
				for(int i=0;i<t;i++){
					ans[new_middle+i]=ori[middle-t+i];
				}
			}
			current_size+=2*t;
			ori = new int[current_size];
			for(int i=0;i<current_size;i++){
				ori[i]=ans[i];
			}
		}
		
		return ans;
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
 		