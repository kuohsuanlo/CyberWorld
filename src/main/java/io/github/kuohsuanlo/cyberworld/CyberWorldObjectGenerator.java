package io.github.kuohsuanlo.cyberworld;

import static java.lang.System.arraycopy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
	private final Random ed_rng;
	private final int BIOME_NUMBERS;
	private Logger log = Logger.getLogger("Minecraft");
    private CityStreetGenerator cg;
    public CityStreetGenerator getCg() {
		return cg;
	}
	private long testingSeed= 1205;
    
	private int sz_deco=1;
	private int sz_s=2;
	private int sz_m=4;
	private int sz_l=15;
	private int sz_block=20;

    private final TerrainHeightGenerator hcg;
    private final static int GROUND_LEVEL = 50;
    private final static double HEIGHT_RAND_ODDS = 0.5;
    private final static double HEIGHT_RAND_RATIO = 1.5;
    private final static int[] all_building_level = {GROUND_LEVEL+3,GROUND_LEVEL+3,GROUND_LEVEL+3};
    private final static int[] underground_building_level = {3,3,3};
    private final static int[] LAYER_HEIGHT = {GROUND_LEVEL+20,GROUND_LEVEL+40,GROUND_LEVEL+80};
    private final int TERRAIN_OCTAVE = 8;
    private final int TERRAIN_HEIGHT = 100;
    private final int SEA_LEVEL = 45;
    
    
	public CyberWorldObjectGenerator(int biome_numbers, CyberWorldBiomeGenerator b, CityStreetGenerator c){
		BIOME_NUMBERS = biome_numbers;
		rng = new Random();
		rng.setSeed(testingSeed);
		bm_rng = new Random();
		bm_rng.setSeed(testingSeed);
		ed_rng = new Random();
		ed_rng.setSeed(testingSeed);

		readSchematic();
		if(c==null){
			System.out.print("[CyberWorld] : Generating City Map... Please wait.");
  	   		cg = new CityStreetGenerator(b,1000,1000,rng,sz_block,cc_list_s.size(),cc_list_m.size(),cc_list_l.size(),sz_s,sz_m,sz_l,1,1,1);
  	   		System.out.print("[CyberWorld] : City Map generation done.");
		}
		else{
			cg = c;
		}
		hcg = new TerrainHeightGenerator(rng,TERRAIN_HEIGHT,TERRAIN_OCTAVE,GROUND_LEVEL);

	}

    public final static int SIGN_LEFT 		=4;
    public final static int SIGN_RIGHT		=5;
    public final static int SIGN_UP 		=6;
    public final static int SIGN_DOWN		=7;
    public final static int DIR_EAST_WEST 		=1;
    public final static int DIR_NORTH_SOUTH		=2;
    public final static int DIR_INTERSECTION	=3;
    public final static int DIR_NOT_ROAD		=-1;
    public final static int DIR_BUILDING		=-2;
    public final static int DIR_S_BUILDING		=-3;
    public final static int DIR_M_BUILDING		=-4;
    public final static int DIR_L_BUILDING		=-5;
    public final static int DIR_U_BUILDING		=-6;
    public final static int DIR_NOT_DETERMINED  =0;

    public final static int MAX_MOST_MATERIAL =5;
    
	//Paving Roads
    private static Material ROAD_SIDEWALK_MATERIAL_1 = Material.STEP;
    private static Material HIGHWAY_MATERIAL = Material.QUARTZ_BLOCK;
    private static MaterialData ROAD_MATERIAL = new MaterialData(Material.STAINED_CLAY.getId(),(byte)0x9);
    private static MaterialData ROAD_OUTSIDE_MATERIAL = new MaterialData(Material.STONE);

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
	

	private ArrayList<CuboidClipboard> cc_list_highway = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_citysurface = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_deco = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_s = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_m = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_l = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_u_deco = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_u_s = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_u_m = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_u_l = new ArrayList<CuboidClipboard>();

	private ArrayList<CuboidClipboard> cc_list_highway_b = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_citysurface_b = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_deco_b = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_s_b = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_m_b = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_l_b = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_u_deco_b = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_u_s_b = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_u_m_b = new ArrayList<CuboidClipboard>();
	private ArrayList<CuboidClipboard> cc_list_u_l_b = new ArrayList<CuboidClipboard>();
	
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_highway = new ArrayList<ArrayList<CuboidClipboard>>();
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_citysurface = new ArrayList<ArrayList<CuboidClipboard>>();
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_deco = new ArrayList<ArrayList<CuboidClipboard>>();
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_s = new ArrayList<ArrayList<CuboidClipboard>>();
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_m = new ArrayList<ArrayList<CuboidClipboard>>();
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_l = new ArrayList<ArrayList<CuboidClipboard>>();
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_u_deco = new ArrayList<ArrayList<CuboidClipboard>>();
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_u_s = new ArrayList<ArrayList<CuboidClipboard>>();
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_u_m = new ArrayList<ArrayList<CuboidClipboard>>();
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_u_l = new ArrayList<ArrayList<CuboidClipboard>>();
	
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_highway_b = new ArrayList<ArrayList<CuboidClipboard>>();
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_citysurface_b = new ArrayList<ArrayList<CuboidClipboard>>();
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_deco_b = new ArrayList<ArrayList<CuboidClipboard>>();
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_s_b = new ArrayList<ArrayList<CuboidClipboard>>();
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_m_b = new ArrayList<ArrayList<CuboidClipboard>>();
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_l_b = new ArrayList<ArrayList<CuboidClipboard>>();
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_u_deco_b = new ArrayList<ArrayList<CuboidClipboard>>();
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_u_s_b = new ArrayList<ArrayList<CuboidClipboard>>();
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_u_m_b = new ArrayList<ArrayList<CuboidClipboard>>();
	private ArrayList<ArrayList<CuboidClipboard>> biome_cc_list_u_l_b = new ArrayList<ArrayList<CuboidClipboard>>();


	private ArrayList<CuboidClipboard> deco =null;
	private ArrayList<CuboidClipboard> s =null;
	private ArrayList<CuboidClipboard> m =null;
	private ArrayList<CuboidClipboard> l =null;
	private ArrayList<CuboidClipboard> decob =null;
	private ArrayList<CuboidClipboard> sb =null;
	private ArrayList<CuboidClipboard> mb =null;
	private ArrayList<CuboidClipboard> lb =null;
	
	@SuppressWarnings("deprecation")
	private String current_reading_folder;
	private void readSchematic(){
		String[] folder_name = {"underground","citysurface","highway","import"};

			
		int n=0;
		
		//read default schematics
		for(n=0;n<folder_name.length;n++){
			current_reading_folder = folder_name[n];
			if(n==0){
				deco = cc_list_u_deco;
				s = cc_list_u_s;
				m = cc_list_u_m;
				l = cc_list_u_l;
				decob = cc_list_u_deco_b;
				sb = cc_list_u_s_b;
				mb = cc_list_u_m_b;
				lb = cc_list_u_l_b;
			}
			else if(n==1){
				deco = cc_list_citysurface;
				decob = cc_list_citysurface_b;
			}
			else if(n==2){
				deco = cc_list_highway;
				decob = cc_list_highway_b;
			}
			else if(n==3){
				deco = cc_list_deco;
				s = cc_list_s;
				m = cc_list_m;
				l = cc_list_l;
				decob = cc_list_deco_b;
				sb = cc_list_s_b;
				mb = cc_list_m_b;
				lb = cc_list_l_b;
			}
			
			try(Stream<Path> paths = Files.walk(Paths.get(CyberWorldObjectGenerator.WINDOWS_PATH+current_reading_folder))) {
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
							System.out.print("[CyberWorld] : Error on default"+current_reading_folder+" schematic = "+filePath.toString()+"/ size too large : "+cc_tmp.getWidth()+","+cc_tmp.getLength());
						}
			        }
			    });
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			System.out.print("[CyberWorld] : Final numbers of default's " + folder_name[n] + " read schematic("+sz_deco+"/"+sz_s+"/"+sz_m+"/"+sz_l+") = "+deco.size()+"/"+s.size()+"/"+m.size()+"/"+l.size());
			
		}
		
		
		
		//read import schematics
		
		for(int biome_number=0;biome_number<BIOME_NUMBERS;biome_number++){
			
			
			
			for(n=0;n<folder_name.length;n++){
				current_reading_folder = folder_name[n];

				deco =new ArrayList<CuboidClipboard>();
				s  =new ArrayList<CuboidClipboard>();
				m  =new ArrayList<CuboidClipboard>();
				l  =new ArrayList<CuboidClipboard>();
				decob =new ArrayList<CuboidClipboard>();
				sb  =new ArrayList<CuboidClipboard>();
				mb  =new ArrayList<CuboidClipboard>();
				lb  =new ArrayList<CuboidClipboard>();
				
				try(Stream<Path> paths = Files.walk(Paths.get(CyberWorldObjectGenerator.WINDOWS_PATH+"/"+biome_number+"/"+current_reading_folder))) {
				    
					paths.forEach(filePath -> {
				        if (Files.isRegularFile(filePath)) {
				        	System.out.println("Now reading schematic : "+filePath.toString());
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
								System.out.print("[CyberWorld] : Error on "+current_reading_folder+" schematic = "+filePath.toString()+"/ size too large : "+cc_tmp.getWidth()+","+cc_tmp.getLength());
							}
				        }
				    });
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
				
				// replace empty
				// imported schematic doesn't exists
				if(folder_name[n].equals("underground")){
					if(deco.size()==0){
						deco = cc_list_u_deco;
						decob = cc_list_u_deco_b;
						System.out.print("[CyberWorld] : no imported schematic for underground deco on biome "+biome_number+", replaced with default schematics");
						
					}
					if(s.size()==0){
						s = cc_list_u_s;
						sb = cc_list_u_s_b;
						System.out.print("[CyberWorld] : no imported schematic for underground s on biome "+biome_number+", replaced with default schematics");
					}
					if(m.size()==0){
						m = cc_list_u_m;
						mb = cc_list_u_m_b;
						System.out.print("[CyberWorld] : no imported schematic for underground m on biome "+biome_number+", replaced with default schematics");
					}
					if(l.size()==0){
						l = cc_list_u_l;
						lb = cc_list_u_l_b;
						System.out.print("[CyberWorld] : no imported schematic for underground l on biome "+biome_number+", replaced with default schematics");
					}
				}
				else if(folder_name[n].equals("citysurface")){
					if(deco.size()==0){
						deco = cc_list_citysurface;
						decob = cc_list_citysurface_b;
						System.out.print("[CyberWorld] : no imported schematic for citysurface deco on biome "+biome_number+", replaced with default schematics");
					}
				}
				else if(folder_name[n].equals("highway")){
					if(deco.size()==0){
						deco = cc_list_highway;
						decob = cc_list_highway_b;
						System.out.print("[CyberWorld] : no imported schematic for highway deco on biome "+biome_number+", replaced with default schematics");
					}
				}
				else if(folder_name[n].equals("import")){
					
					if(deco.size()==0){

						for(int i=0;i<cc_list_deco.size();i++){
							if(biome_number==0){
								deco = cc_list_deco;
								decob = cc_list_deco_b;
							}
							else if( i%CyberWorldChunkGenerator.BIOME_NUMBER_WITH_BUILDING==biome_number  ||  rng.nextInt(5)==0){
								deco.add(cc_list_deco.get(i));
								decob.add(cc_list_deco_b.get(i));
							}
						}
						System.out.print("[CyberWorld] : no imported schematic for import deco on biome "+biome_number+", replaced with default schematics");
					}
					if(s.size()==0){
						for(int i=0;i<cc_list_s.size();i++){
							if(biome_number==0){
								s = cc_list_s;
								sb = cc_list_s_b;
							}
							else if( i%CyberWorldChunkGenerator.BIOME_NUMBER_WITH_BUILDING==biome_number  ||  rng.nextInt(5)==0){
								s.add(cc_list_s.get(i));
								sb.add(cc_list_s_b.get(i));
							}
						}
						System.out.print("[CyberWorld] : no imported schematic for import s on biome "+biome_number+", replaced with default schematics");
					}
					if(m.size()==0){
						for(int i=0;i<cc_list_m.size();i++){
							if(biome_number==0){
								m = cc_list_m;
								mb = cc_list_m_b;
							}
							else if( i%CyberWorldChunkGenerator.BIOME_NUMBER_WITH_BUILDING==biome_number  ||  rng.nextInt(5)==0 ){
								m.add(cc_list_m.get(i));
								mb.add(cc_list_m_b.get(i));
							}
						}
						System.out.print("[CyberWorld] : no imported schematic for import m on biome "+biome_number+", replaced with default schematics");
					}
					if(l.size()==0){
						for(int i=0;i<cc_list_l.size();i++){
							if(biome_number==0){
								l = cc_list_l;
								lb = cc_list_l_b;
							}
							else if( i%CyberWorldChunkGenerator.BIOME_NUMBER_WITH_BUILDING==biome_number  ||  rng.nextInt(5)==0){
								
								l.add(cc_list_l.get(i));
								lb.add(cc_list_l_b.get(i));
							}
						}
						System.out.print("[CyberWorld] : no imported schematic for import l on biome "+biome_number+", replaced with default schematics");
					}
				}
				
				System.out.print("[CyberWorld] : Final numbers of "+ "biome type : "+biome_number +"'s "+ folder_name[n] + " read schematic("+sz_deco+"/"+sz_s+"/"+sz_m+"/"+sz_l+") = "+deco.size()+"/"+s.size()+"/"+m.size()+"/"+l.size());
				
				
				//copy 
				if(n==0){
					biome_cc_list_u_deco.add( (ArrayList<CuboidClipboard>) deco);
					biome_cc_list_u_s.add( (ArrayList<CuboidClipboard>) s);
					biome_cc_list_u_m.add( (ArrayList<CuboidClipboard>) m);
					biome_cc_list_u_l.add( (ArrayList<CuboidClipboard>) l);
					biome_cc_list_u_deco_b.add( (ArrayList<CuboidClipboard>) decob);
					biome_cc_list_u_s_b.add( (ArrayList<CuboidClipboard>) sb);
					biome_cc_list_u_m_b.add( (ArrayList<CuboidClipboard>) mb);
					biome_cc_list_u_l_b.add( (ArrayList<CuboidClipboard>) lb);
					
				}
				else if(n==1){
					biome_cc_list_citysurface.add( (ArrayList<CuboidClipboard>) deco);
					biome_cc_list_citysurface_b.add( (ArrayList<CuboidClipboard>) decob);
				}
				else if(n==2){
					biome_cc_list_highway.add( (ArrayList<CuboidClipboard>) deco);
					biome_cc_list_highway_b.add( (ArrayList<CuboidClipboard>) decob);
				}
				else if(n==3){
					biome_cc_list_deco.add( (ArrayList<CuboidClipboard>) deco);
					biome_cc_list_s.add( (ArrayList<CuboidClipboard>) s);
					biome_cc_list_m.add( (ArrayList<CuboidClipboard>) m);
					biome_cc_list_l.add( (ArrayList<CuboidClipboard>) l);
					biome_cc_list_deco_b.add( (ArrayList<CuboidClipboard>) decob);
					biome_cc_list_s_b.add( (ArrayList<CuboidClipboard>) sb);
					biome_cc_list_m_b.add( (ArrayList<CuboidClipboard>) mb);
					biome_cc_list_l_b.add( (ArrayList<CuboidClipboard>) lb);
				}
			}
				
			
		}

	}
	public ChunkData generateBottom(ChunkData chunkdata, Random random, int chkx, int chkz, int biome_number,BiomeGrid biomes){
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
	public ChunkData generateCitySurface(ChunkData chunkdata, Random random, int chkx, int chkz, int biome_number, BiomeGrid biomes){
        for(int y=GROUND_LEVEL;y<=GROUND_LEVEL+3;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			//Paving Ground
	    			if(y  == GROUND_LEVEL+2){
	    				  int block_id = biome_cc_list_citysurface.get(biome_number).get(0).getBlock(new Vector(x,0,z)).getId();
	    				  int block_data = (byte)  biome_cc_list_citysurface.get(biome_number).get(0).getBlock(new Vector(x,0,z)).getData();
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
	public ChunkData generateFactoryRoad(ChunkData chunkdata, Random random, int chkx, int chkz, int biome_number,BiomeGrid biomes){
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
	public ChunkData generateCityRoad(ChunkData chunkdata, Random random, int chkx, int chkz,int biome_type, BiomeGrid biomes){
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
    				chunkdata.setRegion(0,GROUND_LEVEL,0,16,GROUND_LEVEL+3,1,ROAD_OUTSIDE_MATERIAL);
    				chunkdata.setRegion(0,GROUND_LEVEL,15,16,GROUND_LEVEL+3,16,ROAD_OUTSIDE_MATERIAL);
    				
    				
        			if( (z>=1&&z<=2)  ||  (z>=13&&z<=14)){
        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
        			}
        			
        			if((z==1 &&  x==7)  ||  (z==14  && x==7)){
        				chunkdata.setRegion(x,y,z,x+1,y+5,z+1,Material.FENCE);
        				chunkdata.setRegion(x,y+5,z,x+1,y+6,z+1,Material.GLOWSTONE);
        			}
    			}
    			else if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
    				chunkdata.setRegion(0,GROUND_LEVEL,0,1,GROUND_LEVEL+3,16,ROAD_OUTSIDE_MATERIAL);
    				chunkdata.setRegion(15,GROUND_LEVEL,0,16,GROUND_LEVEL+3,16,ROAD_OUTSIDE_MATERIAL);
    		    	
    				if( (x>=1&&x<=2)  ||  (x>=13&&x<=14)){
        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
        			}

        			if((x==1 &&  z==7)  ||  (x==14  && z==7)){
        				chunkdata.setRegion(x,y,z,x+1,y+5,z+1,Material.FENCE);
        				chunkdata.setRegion(x,y+5,z,x+1,y+6,z+1,Material.GLOWSTONE);
        			}
    			}
    			else if(cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION ){
    				chunkdata.setRegion(0,GROUND_LEVEL,0,1,GROUND_LEVEL+3,1,ROAD_OUTSIDE_MATERIAL);
    				chunkdata.setRegion(15,GROUND_LEVEL,0,16,GROUND_LEVEL+3,1,ROAD_OUTSIDE_MATERIAL);
    				chunkdata.setRegion(0,GROUND_LEVEL,15,1,GROUND_LEVEL+3,16,ROAD_OUTSIDE_MATERIAL);
    				chunkdata.setRegion(15,GROUND_LEVEL,15,16,GROUND_LEVEL+3,16,ROAD_OUTSIDE_MATERIAL);
    				
    				if( (x<=2  ||  x>=13)  && (z<=2  ||  z>=13)){
        				chunkdata.setRegion(x,y,z,x+1,y+1,z+1,ROAD_SIDEWALK_MATERIAL_1);
        			}

    				chunkdata.setRegion(0,y,0,1,y+1,1,Material.AIR);
    				chunkdata.setRegion(15,y,0,16,y+1,1,Material.AIR);
    				chunkdata.setRegion(0,y,15,1,y+1,16,Material.AIR);
    				chunkdata.setRegion(15,y,15,16,y+1,16,Material.AIR);
    				
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
    public ChunkData generateCitySewer(ChunkData chunkdata, Random random, int chkx, int chkz, int biome_number,BiomeGrid biomes){
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
		        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.GLASS);
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
		        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.GLASS);
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
		        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.GLASS);
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
		        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.GLASS);
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
	    						chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.GLASS);
	    					}
	    				}
    				}
    			}
    		}
    	}
	       	
	    
	    
	    return chunkdata;
    	
    }  
    public ChunkData generateHighway(ChunkData chunkdata, Random random, int chkx, int chkz, int biome_number,BiomeGrid biomes){
    	//Paving High Roads
    	for(int level=2;level>=0;level--){
    		int road_tube_width = 7;
    		int road_y_middle = LAYER_HEIGHT[level]+road_tube_width/2;
    		int road_tube_thick = 1;
    	    //HIGHWAY_TUBES_MATERIAL = new MaterialData(Material.STAINED_GLASS.getId(), (byte)(Math.abs(chkx+chkz)%16)  );
    	    HIGHWAY_TUBES_MATERIAL = new MaterialData(Material.GLASS.getId()  );
    	    
    	    
			boolean EW_tunnel = false;
			boolean NS_tunnel = false;
			boolean IT_tunnel = false;
    		for(int y=LAYER_HEIGHT[level]-1;y<LAYER_HEIGHT[level]+road_tube_width*2;y++){
    	    	for(int x=0;x<16;x++){
    	    		for(int z=0;z<16;z++){
    	    			//Road & tube checking
        				if(y >=LAYER_HEIGHT[level]-1 && y<LAYER_HEIGHT[level]+road_tube_width*2){
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
    		}

    		for(int y=LAYER_HEIGHT[level];y<LAYER_HEIGHT[level]+road_tube_width*2;y++){
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
			if((chkx+chkz)%2==0  &&  (!has_thing_underneath) ){
				for(int x=0;x<16;x++){
    	    		for(int z=0;z<16;z++){
    	    			if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_EAST_WEST ){
    	    				if((z >= 6  &&  z<=9)  &&  (x==7  ||  x==8) ){
    	    					chunkdata.setRegion(x,GROUND_LEVEL+3,z,x+1,LAYER_HEIGHT[level],z+1,HIGHWAY_MATERIAL);
    	        			}
    	  	    		}
    	  	    		else if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){
    	  	    			if((x >= 6  &&  x<=9)  &&  (z==7  ||  z==8) ){
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


    	    //Pasting Struct
    	    if(cc_list_highway.size()>0){
    	    	if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH){

    	    		int highway_struct_height = biome_cc_list_highway.get(biome_number).get(0).getHeight();
    	    		int highway_struct_width = biome_cc_list_highway.get(biome_number).get(0).getWidth();
    	    		int highway_struct_length = biome_cc_list_highway.get(biome_number).get(0).getLength();
    				for(int y=LAYER_HEIGHT[level];y<LAYER_HEIGHT[level]+highway_struct_height;y++){
    		    		int k=y-LAYER_HEIGHT[level];
    	    			for(int x=0;x<highway_struct_width;x++){
    	    	    		for(int z=0;z<highway_struct_length;z++){
    	    	    			int block_id = biome_cc_list_highway.get(biome_number).get(0).getBlock(new Vector(x,k,z)).getId();
    	    				    int block_data = biome_cc_list_highway.get(biome_number).get(0).getBlock(new Vector(x,k,z)).getData();
    	    				    if(block_id!=Material.AIR.getId())
    	    				    	chunkdata.setBlock(x, y-3, z,new MaterialData(block_id,(byte) block_data));
    	    	    		}
        	    		}
    	    		}
        		}
        		else if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_EAST_WEST  ){
        			//rotating
        			biome_cc_list_highway.get(biome_number).get(0).rotate2D(90);

            		int highway_struct_height = biome_cc_list_highway.get(biome_number).get(0).getHeight();
            		int highway_struct_width = biome_cc_list_highway.get(biome_number).get(0).getWidth();
            		int highway_struct_length = biome_cc_list_highway.get(biome_number).get(0).getLength();
    				for(int y=LAYER_HEIGHT[level];y<LAYER_HEIGHT[level]+highway_struct_height;y++){
    		    		int k=y-LAYER_HEIGHT[level];
    	    			for(int x=0;x<highway_struct_width;x++){
    	    	    		for(int z=0;z<highway_struct_length;z++){
    	    	    			int block_id = biome_cc_list_highway.get(biome_number).get(0).getBlock(new Vector(x,k,z)).getId();
    	    				    int block_data =  biome_cc_list_highway.get(biome_number).get(0).getBlock(new Vector(x,k,z)).getData();
    	    				    if(block_id!=Material.AIR.getId())
    	    				    	chunkdata.setBlock(x, y-3, z,new MaterialData(block_id,(byte) block_data));
    	    	    		}
        	    		}
    	    		}
    				biome_cc_list_highway.get(biome_number).get(0).rotate2D(270);
        		}
        		else if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_INTERSECTION ){

            		int highway_struct_height = biome_cc_list_highway.get(biome_number).get(0).getHeight();
            		int highway_struct_width = biome_cc_list_highway.get(biome_number).get(0).getWidth();
            		int highway_struct_length = biome_cc_list_highway.get(biome_number).get(0).getLength();
    				for(int y=LAYER_HEIGHT[level];y<LAYER_HEIGHT[level]+highway_struct_height;y++){
    		    		int k=y-LAYER_HEIGHT[level];
    	    			for(int x=0;x<highway_struct_width;x++){
    	    	    		for(int z=0;z<highway_struct_length;z++){
    	    	    			int block_id = biome_cc_list_highway.get(biome_number).get(0).getBlock(new Vector(x,k,z)).getId();
    	    				    int block_data =  biome_cc_list_highway.get(biome_number).get(0).getBlock(new Vector(x,k,z)).getData();
    	    				    if(block_id!=Material.AIR.getId())
    	    				    	chunkdata.setBlock(x, y-3, z,new MaterialData(block_id,(byte) block_data));
    	    	    		}
        	    		}
    	    		}
        			//rotating
    				biome_cc_list_highway.get(biome_number).get(0).rotate2D(90);

            		highway_struct_height = biome_cc_list_highway.get(biome_number).get(0).getHeight();
            		highway_struct_width = biome_cc_list_highway.get(biome_number).get(0).getWidth();
            		highway_struct_length = biome_cc_list_highway.get(biome_number).get(0).getLength();
            		
    				for(int y=LAYER_HEIGHT[level];y<LAYER_HEIGHT[level]+highway_struct_height;y++){
    		    		int k=y-LAYER_HEIGHT[level];
    	    			for(int x=0;x<highway_struct_width;x++){
    	    	    		for(int z=0;z<highway_struct_length;z++){
    	    	    			int block_id = biome_cc_list_highway.get(biome_number).get(0).getBlock(new Vector(x,k,z)).getId();
    	    				    int block_data =  biome_cc_list_highway.get(biome_number).get(0).getBlock(new Vector(x,k,z)).getData();
    	    				    if(block_id!=Material.AIR.getId())
    	    				    	chunkdata.setBlock(x, y-3, z,new MaterialData(block_id,(byte) block_data));
    	    	    		}
        	    		}
    	    		}
    				biome_cc_list_highway.get(biome_number).get(0).rotate2D(270);
    				
        		}
        		else if(cg.getHighwayType(chkx,chkz,level)==CyberWorldObjectGenerator.DIR_NOT_ROAD ){
        			
        		}
    	    }
    	
    	
    	}
	      
        return chunkdata;
     	
    }
    public ChunkData generateBuilding(ChunkData chunkdata, Random random, int chkx, int chkz,int biome_type, BiomeGrid biomes, int start_of_layer, boolean only_shell){
    	//Building Generation
		int layer;
		int[] current_size = cg.a_size;
		
		int[] building_type = {CyberWorldObjectGenerator.DIR_S_BUILDING,CyberWorldObjectGenerator.DIR_M_BUILDING,CyberWorldObjectGenerator.DIR_L_BUILDING};
		Object[] all_lists = {biome_cc_list_s.get(biome_type),biome_cc_list_m.get(biome_type),biome_cc_list_l.get(biome_type)};
		Object[] all_b_lists = {biome_cc_list_s_b.get(biome_type),biome_cc_list_m_b.get(biome_type),biome_cc_list_l_b.get(biome_type)};
		
		int s_layer_nubmer=start_of_layer;
		for(layer=s_layer_nubmer;layer>=0;layer--){
			if(cg.getBuilding(chkx, chkz, layer)==building_type[layer]){	
				int layer_start = all_building_level[layer];
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

					if(current_list.size()>0){
						int type = cg.getBuildingType(chkx,chkz,layer)%current_list.size();
						
						long chunk_seed = cg.getBuildingSeed(chkx, chkz, layer);
						int block_id  = Material.AIR.getId();
						int block_data  = 0;
						
						boolean[][][] fillingAirIndeces ;
						boolean[][][] frameIndeces = null;
						//rotating
						current_list.get(type).rotate2D(angle);

						int[] ori_idx_i = IntStream.range(0, current_list.get(type).getWidth()).toArray(); 
						int[] ori_idx_j = IntStream.range(0, current_list.get(type).getLength()).toArray();
						int[] ori_idx_k = IntStream.range(0, current_list.get(type).getHeight()).toArray();
						int r_i = (current_list.get(type).getWidth()%2);
						int r_j = (current_list.get(type).getLength()%2);
						
						ed_rng.setSeed(cg.getBuildingSeed(chkx, chkz, layer)*722);
						int i_rand = ed_rng.nextInt(current_size[layer]*16/4)*2+(current_size[layer]*16/4)*2;
						int j_rand = ed_rng.nextInt(current_size[layer]*16/4)*2+(current_size[layer]*16/4)*2;
						int[] expended_idx_i = this.generateExpandedSequence(ori_idx_i, Math.min(4+4*layer+r_i,current_list.get(type).getWidth()/2+r_i), Math.max(current_list.get(type).getWidth()+1,i_rand+r_i));
						int[] expended_idx_j = this.generateExpandedSequence(ori_idx_j, Math.min(4+4*layer+r_j,current_list.get(type).getLength()/2+r_j),Math.max(current_list.get(type).getLength()+1,j_rand+r_j));
						
						
						int new_height = current_list.get(type).getHeight();
						if(ed_rng.nextDouble()<HEIGHT_RAND_ODDS){
							new_height+= ed_rng.nextInt((int)(current_list.get(type).getHeight()*(HEIGHT_RAND_RATIO-1)));
							
						}
						
						if(new_height>this.MAX_SPACE_HEIGHT-this.SEA_LEVEL){
							new_height = current_list.get(type).getHeight();
						}
						int[] expended_idx_k = this.generateExpandedHeightSequence(ori_idx_k,new_height);


						int i_end = Math.min(expended_idx_i.length,i_max);
						int j_end = Math.min(expended_idx_j.length,j_max);
						int k_end = expended_idx_k.length;
						
						if(layer!=s_layer_nubmer){
							fillingAirIndeces = this.getfilledArea(current_list.get(type));
							//printMap(fillingAirIndeces);
						}
						else{
							fillingAirIndeces = new boolean [i_max][j_max][k_end] ;
						}
						
						if(only_shell){
							frameIndeces = this.getFrameArea(current_list.get(type));
						}
						else{
							frameIndeces = new boolean [i_max][j_max][k_end] ;
						}
		            	for(int k=0;k<k_end;k++){
		    				int y = k+layer_start;
		    				for(int i=i_start;i<i_end;i++){
		    					for(int j=j_start;j<j_end;j++){
		    						int x = j-j_start;
		    						int z = i-i_start;
				    				
				    				block_id = current_list.get(type).getBlock(new Vector(expended_idx_i[i],expended_idx_k[k],expended_idx_j[j])).getId();
				            		block_data = current_list.get(type).getBlock(new Vector(expended_idx_i[i],expended_idx_k[k],expended_idx_j[j])).getData();
				            		
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
				    				if(fillingAirIndeces[expended_idx_i[i]][expended_idx_j[j]][expended_idx_k[k]]){
				    					chunkdata.setBlock(x, y, z,Material.AIR);
				    				}
				    				if(block_id!=Material.AIR.getId()  &&  ( !only_shell || frameIndeces[expended_idx_i[i]][expended_idx_j[j]][expended_idx_k[k]]  )){
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
		}
		return chunkdata;	
    }
	public ChunkData generateUnderGroundBuilding(ChunkData chunkdata, Random random, int chkx, int chkz, int biome_number,BiomeGrid biomes, int start_of_layer){
    	//Building Generation
		int layer;
		int[] current_size = cg.a_size;
		
		int[] building_type = {CyberWorldObjectGenerator.DIR_S_BUILDING,CyberWorldObjectGenerator.DIR_M_BUILDING,CyberWorldObjectGenerator.DIR_L_BUILDING};
		Object[] all_lists = {biome_cc_list_u_s.get(biome_number),biome_cc_list_u_m.get(biome_number),biome_cc_list_u_l.get(biome_number)};
		Object[] all_b_lists = {biome_cc_list_u_s_b.get(biome_number),biome_cc_list_u_m_b.get(biome_number),biome_cc_list_u_l_b.get(biome_number)};
		
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
						int block_data  = 0;
						
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
					            		block_data =  current_list.get(type).getBlock(new Vector(i,k,j)).getData();
					            		
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
    public ChunkData generateBuildingDecoration(ChunkData chunkdata, Random random, int chkx, int chkz,int biome_type, BiomeGrid biomes){
		//on top, on group, on side
    	//Building Generation
		
		return chunkdata;
    	
    }
	public ChunkData generateGroundDecoration(ChunkData chunkdata, Random random, int chkx, int chkz,int biome_type, BiomeGrid biomes){
		int layer_start = GROUND_LEVEL+3;
		int sx=0;
		int sz=0;
		int i_start = sx*16;
		int i_max = (sx+1)*16;
		int j_start = sz*16;
		int j_max = (sz+1)*16;
		
		if(biome_cc_list_deco.get(biome_type).size()>0){

			
			
			int type = rng.nextInt(biome_cc_list_deco.get(biome_type).size());
			CuboidClipboard object = biome_cc_list_deco.get(biome_type).get(type);
			CuboidClipboard object_b = biome_cc_list_deco.get(biome_type).get(type);
			int angle = rng.nextInt(4)*90;
			
			//replacing rng seed
			long chunk_seed = cg.getBuildingSeed(chkx, chkz, 0);
			
			int block_id  = Material.AIR.getId();
			int block_data =0;
	
			
	
			
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
			            		block_data =  object.getBlock(new Vector(i,k,j)).getData();
			            		
			            		
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
		            		block_data =  (object.getBlock(new Vector(i,k,j)).getData()%128);
		            		
		            		
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
	public ChunkData generateFactoryGround(ChunkData chunkdata, Random random, int chkx, int chkz,int biome_type, BiomeGrid biomes){

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
	public ChunkData generateTerrain(ChunkData chunkdata, Random random, int chkx, int chkz,int biome_type, BiomeGrid biomes){
		Material ground = null;
		int heightRevisedRatio = 5;
		
		
    	for(int x=0;x<16;x++){
    		for(int z=0;z<16;z++){
    			
    			int height = Math.round(hcg.generateHeight(chkx*16+x, chkz*16+z,false))+3;
    			if(height>=3){

    				
        			//SEA
        			if(biomes.getBiome(x, z).equals(Biome.OCEAN) ||
    					biomes.getBiome(x, z).equals(Biome.FROZEN_OCEAN)  ||
    					biomes.getBiome(x, z).equals(Biome.DEEP_OCEAN)){
        				
        				ground = Material.CLAY;
        				height = (int) ((SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio)*0.5);
        				/*
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
        				}*/
        				chunkdata.setRegion(x,height,z,x+1,height+1,z+1,ground);
        				
	    				chunkdata.setRegion(x,3,z,x+1,height-3,z+1,Material.STONE);
	        			chunkdata.setRegion(x,height-3,z,x+1,height,z+1,Material.DIRT);		
	        			
		        		chunkdata.setRegion(x,height,z,x+1,SEA_LEVEL+1,z+1,Material.WATER);
		        		
	        			
        			}
        			else{
        				if(biomes.getBiome(x, z).equals(Biome.FOREST)){
            				ground = Material.GRASS;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			else if(biomes.getBiome(x, z).equals(Biome.BIRCH_FOREST)){
            				ground = Material.GRASS;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			else if(biomes.getBiome(x, z).equals(Biome.MUTATED_BIRCH_FOREST)){
            				ground = Material.GRASS;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			else if(biomes.getBiome(x, z).equals(Biome.MUTATED_FOREST)){
            				ground = Material.GRASS;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			else if(biomes.getBiome(x, z).equals(Biome.ROOFED_FOREST)){
            				ground = Material.GRASS;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			else if(biomes.getBiome(x, z).equals(Biome.JUNGLE)){
            				ground = Material.GRASS;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			//Hills
            			else if(biomes.getBiome(x, z).equals(Biome.BIRCH_FOREST_HILLS) ||
            					biomes.getBiome(x, z).equals(Biome.DESERT_HILLS) ||
            					biomes.getBiome(x, z).equals(Biome.FOREST_HILLS) ||
            					biomes.getBiome(x, z).equals(Biome.JUNGLE_HILLS) ||
            					biomes.getBiome(x, z).equals(Biome.MUTATED_BIRCH_FOREST_HILLS) ||
            					biomes.getBiome(x, z).equals(Biome.MUTATED_REDWOOD_TAIGA_HILLS) ||
            					biomes.getBiome(x, z).equals(Biome.REDWOOD_TAIGA_HILLS) ||
            					biomes.getBiome(x, z).equals(Biome.TAIGA_COLD_HILLS) ||
            					biomes.getBiome(x, z).equals(Biome.TAIGA_HILLS) ||
            					biomes.getBiome(x, z).equals(Biome.BIRCH_FOREST_HILLS) ){
            				ground = Material.GRASS;
            				height = (int) (SEA_LEVEL+Math.abs(Math.round((height-SEA_LEVEL)*1.15)));
            			}
            			//Exterme hills
            			else if(biomes.getBiome(x, z).equals(Biome.EXTREME_HILLS) ||
            					biomes.getBiome(x, z).equals(Biome.EXTREME_HILLS_WITH_TREES) ||
            					biomes.getBiome(x, z).equals(Biome.MUTATED_EXTREME_HILLS) ||
            					biomes.getBiome(x, z).equals(Biome.MUTATED_EXTREME_HILLS_WITH_TREES) ||
            					biomes.getBiome(x, z).equals(Biome.SMALLER_EXTREME_HILLS) ){
            				ground = Material.GRASS;
            				height = (int) (SEA_LEVEL+Math.abs(Math.round((height-SEA_LEVEL)*1.25)));
            			}
            			//
            			
            			//Forest
            			else if(biomes.getBiome(x, z).equals(Biome.BIRCH_FOREST)){
            				ground = Material.GRASS;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			else if(biomes.getBiome(x, z).equals(Biome.MUTATED_BIRCH_FOREST)){
            				ground = Material.GRASS;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			else if(biomes.getBiome(x, z).equals(Biome.MUTATED_FOREST)){
            				ground = Material.GRASS;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			else if(biomes.getBiome(x, z).equals(Biome.ROOFED_FOREST)){
            				ground = Material.GRASS;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			//Plain
            			else if(biomes.getBiome(x, z).equals(Biome.PLAINS)){
            				ground = Material.GRASS;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			else if(biomes.getBiome(x, z).equals(Biome.MUTATED_PLAINS)){
            				ground = Material.GRASS;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			else if(biomes.getBiome(x, z).equals(Biome.SAVANNA)){
            				ground = Material.GRASS;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			//Desert
            			else if(biomes.getBiome(x, z).equals(Biome.DESERT)){
            				ground = Material.SAND;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			else if(biomes.getBiome(x, z).equals(Biome.MUTATED_DESERT)){
            				ground = Material.SAND;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			
            			//Swamp
            			else if(biomes.getBiome(x, z).equals(Biome.MUTATED_SWAMPLAND)){
            				ground = Material.WATER;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			else if(biomes.getBiome(x, z).equals(Biome.SWAMPLAND)){
            				ground = Material.WATER;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			
            			else if(biomes.getBiome(x, z).equals(Biome.RIVER)){
            				ground = Material.WATER;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            				chunkdata.setRegion(x,(int) Math.round(height*0.95),z,x+1,height+1,z+1,Material.WATER);
            				height*=0.95;
            			}
            			
            			//BEACHES
            			else if(biomes.getBiome(x, z).equals(Biome.BEACHES)){
            				ground = Material.SAND;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			else if(biomes.getBiome(x, z).equals(Biome.COLD_BEACH)){
            				ground = Material.SNOW_BLOCK;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			else if(biomes.getBiome(x, z).equals(Biome.STONE_BEACH)){
            				ground = Material.STONE;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			
            			//MESA
            			else if(biomes.getBiome(x, z).equals(Biome.MESA)){
            				ground = Material.RED_SANDSTONE;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
            			
            			
            			else {
            				ground = Material.CLAY;
            				height = (int) (SEA_LEVEL+Math.abs(height-SEA_LEVEL)/heightRevisedRatio);
            			}
        				chunkdata.setRegion(x,3,z,x+1,height-3,z+1,Material.STONE);
            			chunkdata.setRegion(x,height-3,z,x+1,height,z+1,Material.DIRT);		
            			/*
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
        				}*/
        				chunkdata.setRegion(x,height,z,x+1,height+1,z+1,ground);
        			}
    			}
    			
    		}
    	}
	    
        return chunkdata;
    }

	public ChunkData generateFactorySewer(ChunkData chunkdata, Random random, int chkx, int chkz,int biome_type, BiomeGrid biomes){
   	 //Building Sewer Layout

		int sewer_pipe_width = 5;
		int sewer_pipe_thick = 2;
		int sewer_pipe_height= GROUND_LEVEL-16;
		boolean ew = false;
		boolean ns = false;
	    for(int y=2;y<GROUND_LEVEL+3;y++){
	    	for(int x=0;x<16;x++){
	    		for(int z=0;z<16;z++){
	    			if(y >=2 && y <GROUND_LEVEL+1){
	    				double d = rng.nextDouble();
	    				//Building Sewer Pipe, Sewer Ground
	    				if ( cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_EAST_WEST ||
    						 cg.getHighwayType(chkx,chkz,0)==CyberWorldObjectGenerator.DIR_EAST_WEST ||	
    						 cg.getHighwayType(chkx,chkz,1)==CyberWorldObjectGenerator.DIR_EAST_WEST ||
    						 cg.getHighwayType(chkx,chkz,2)==CyberWorldObjectGenerator.DIR_EAST_WEST ){ //ROAD
	    					ew=true;
	    					
	    				}
	    				
	    				if ( cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ||
	    						 cg.getHighwayType(chkx,chkz,0)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ||	
	    						 cg.getHighwayType(chkx,chkz,1)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ||
	    						 cg.getHighwayType(chkx,chkz,2)==CyberWorldObjectGenerator.DIR_NORTH_SOUTH ){ //ROAD
	    					ns=true;
	    				}
	    				

	    				if ( cg.getRoadType(chkx,chkz)==CyberWorldObjectGenerator.DIR_INTERSECTION ||
	    						 cg.getHighwayType(chkx,chkz,0)==CyberWorldObjectGenerator.DIR_INTERSECTION ||	
	    						 cg.getHighwayType(chkx,chkz,1)==CyberWorldObjectGenerator.DIR_INTERSECTION ||
	    						 cg.getHighwayType(chkx,chkz,2)==CyberWorldObjectGenerator.DIR_INTERSECTION ){ //ROAD
	    					ns=true;
	    					ew=true;
	    				}
	    				
	    				
	    				if (ew == true &&  ns == false){ //ROAD
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
		        		else if(ns == true &&  ew == false){ //ROAD
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
		        		else if (ns == true &&  ew == true){ //ROAD
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
	    				if(ns == true &&  ew == false){
	    					
		        			//Pipe Shell
		        			if(y>=3  &&  ((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<sewer_pipe_width*sewer_pipe_width  && 
		        					((x-7.5)*(x-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))>=(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)      ){
		        				
		        				if(z==0  || z==15){
		        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.GLASS);
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
	    				else if(ew == true &&  ns == false){
	    					
		        			//Pipe Shell
		        			if(y>=3  &&  ((z-7.5)*(z-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<sewer_pipe_width*sewer_pipe_width  && 
		        					((z-7.5)*(z-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))>=(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)      ){
		        				
		        				if(x==0  || x==15){
		        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.GLASS);
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
	    				else if(ew == true &&  ns == true){
	    					
		        			//Pipe Shell
		        			if(y>=3  &&  ((z-7.5)*(z-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))<sewer_pipe_width*sewer_pipe_width  && 
		        					((z-7.5)*(z-7.5)+(y-sewer_pipe_height)*(y-sewer_pipe_height))>=(sewer_pipe_width-sewer_pipe_thick)*(sewer_pipe_width-sewer_pipe_thick)) {
		        				
		        				if(z==0  || z==15  ||  x==0  ||  x ==15){
		        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.GLASS);
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
		        					chunkdata.setRegion(x,y,z,x+1,y+1,z+1,Material.GLASS);
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
	    }    	
	    
	    return chunkdata;
   	
	}
	
	private static final int[] STAIRS_LIST = {67,108,109,114,128,134,135,136,156,163,164,180,203};
	private static final int[] SLABS_LIST = {44,126,205};
	private static final int[] FENCE_LIST = {85,113,188,189,190,191,192};
	private static final int[] BLOCKS_LIST = {1,4,5,17,24,43,45,82,87,88,98,121,125,155,162,168,172,179,181,201,202,204,206};
	private static final int[] BLOCKS_DMAX = {7,1,6, 4, 3, 8, 1, 1, 1, 1, 4,  1,  6,  3,  2,  3,  1,  3,  1,  1,  1,  1,  1};
	private MaterialData getReplacedMaterial(Random replace_rng, int id,int original_data_int,long seeds){
		byte original_data = (byte) original_data_int;
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
		else if(this.contains(BLOCKS_LIST, id)  ||  id==Material.STAINED_CLAY.getId()  ||  id==Material.WOOL.getId() ){
			replace_rng.setSeed(seeds+id*17);
			int all_block_idx = replace_rng.nextInt(BLOCKS_LIST.length+32);
			
			if(all_block_idx<16  &&  all_block_idx>=0){
				replace_rng.setSeed(seeds+id*17);
				return new MaterialData(Material.WOOL.getId(),(byte)replace_rng.nextInt(16));
			}
			else if(all_block_idx<32  &&  all_block_idx>=16){
				replace_rng.setSeed(seeds+id*17);
				return new MaterialData(Material.STAINED_CLAY.getId(),(byte)replace_rng.nextInt(16));
			}
			else{
				int new_block_idx = all_block_idx-32;
				return new MaterialData(BLOCKS_LIST[new_block_idx],(byte)replace_rng.nextInt( BLOCKS_DMAX[new_block_idx]));
			}
			
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
		

		boolean[][][] area = new boolean[max_x_old][max_z_old][max_y_old];

		for(int y=0;y<max_y_old;y++){
			for(int x=0;x<max_x_old;x++){
				for(int z=0;z<max_z_old;z++){
					if(cc.getBlock(new Vector(x,y,z)).getId()!=Material.AIR.getId()){
						area[x][z][y]=true;
					}
				}
			}
		}
		boolean[][][] frame = new boolean[max_x_old][max_z_old][max_y_old];
		boolean[][][] frame_x = new boolean[max_x_old][max_z_old][max_y_old];
		boolean[][][] frame_y = new boolean[max_x_old][max_z_old][max_y_old];
		boolean[][][] frame_z = new boolean[max_x_old][max_z_old][max_y_old];

		int current_thickness;
		int thickness_max=1;
		int last_index = -1;
		for(int y=0;y<max_y_old;y++){
			//bindind z
			for(int x=0;x<max_x_old;x++){
				current_thickness =thickness_max;
				for(int z=0;z<max_z_old;z++){
					if(area[x][z][y] ){
						frame_y[x][z][y]=true;
						current_thickness--;
						last_index = z;
						if(current_thickness<=0){
							break;
						}
					}
				}
				current_thickness =thickness_max;
				for(int z=max_z_old-1;z>=0;z--){
					if(area[x][z][y] ){
						frame_y[x][z][y]=true;
						current_thickness--;
						if(current_thickness<=0){
							break;
						}
					}
				}
			}
			
			//bindind x
			for(int z=0;z<max_z_old;z++){
				current_thickness =thickness_max;
				for(int x=0;x<max_x_old;x++){
					if(area[x][z][y]){
						frame_y[x][z][y]=true;
						current_thickness--;
						if(current_thickness<=0){
							break;
						}
					}
				}
				current_thickness =thickness_max;
				for(int x=max_x_old-1;x>=0;x--){
					if(area[x][z][y] ){
						frame_y[x][z][y]=true;
						current_thickness--;
						if(current_thickness<=0){
							break;
						}
					}
				}
			}
			
		}
		
		for(int x=0;x<max_x_old;x++){
			//bindind y
			for(int y=0;y<max_y_old;y++){

				current_thickness =thickness_max;
				for(int z=0;z<max_z_old;z++){
					if(area[x][z][y]){
						frame_x[x][z][y]=true;
						current_thickness--;
						if(current_thickness<=0){
							break;
						}
					}
				}
				current_thickness =thickness_max;
				for(int z=max_z_old-1;z>=0;z--){
					if(area[x][z][y]){
						frame_x[x][z][y]=true;
						current_thickness--;
						if(current_thickness<=0){
							break;
						}
					}
				}
			}
			
			//bindind z
			for(int z=0;z<max_z_old;z++){	
				current_thickness =thickness_max;
				for(int y=0;y<max_y_old;y++){
					if(area[x][z][y]){
						frame_x[x][z][y]=true;
						current_thickness--;
						if(current_thickness<=0){
							break;
						}
					}
				}
				current_thickness =thickness_max;
				for(int y=max_y_old-1;y>=0;y--){
					if(area[x][z][y]){
						frame_x[x][z][y]=true;
						current_thickness--;
						if(current_thickness<=0){
							break;
						}
					}
				}
			}
			
		}
		
		for(int z=0;z<max_z_old;z++){
			//bindind z
			for(int y=0;y<max_y_old;y++){

				current_thickness =thickness_max;
				for(int x=0;x<max_x_old;x++){
					if(area[x][z][y]){
						frame_z[x][z][y]=true;
						current_thickness--;
						if(current_thickness<=0){
							break;
						}
					}
				}
				current_thickness =thickness_max;
				for(int x=max_x_old-1;x>=0;x--){
					if(area[x][z][y]){
						frame_z[x][z][y]=true;
						current_thickness--;
						if(current_thickness<=0){
							break;
						}
					}
				}
			}
			
			//bindind x
			for(int x=0;x<max_x_old;x++){	
				current_thickness =thickness_max;
				for(int y=0;y<max_y_old;y++){
					if(area[x][z][y]){
						frame_z[x][z][y]=true;
						current_thickness--;
						if(current_thickness<=0){
							break;
						}
					}
				}
				current_thickness =thickness_max;
				for(int y=max_y_old-1;y>=0;y--){
					if(area[x][z][y]){
						frame_z[x][z][y]=true;
						current_thickness--;
						if(current_thickness<=0){
							break;
						}
					}
				}
			}
			
		}
		
		
		for(int x=0;x<max_x_old;x++){	
			for(int y=0;y<max_y_old;y++){
				for(int z=0;z<max_z_old;z++){	
					if(frame_x[x][z][y] || frame_y[x][z][y] || frame_z[x][z][y]){
						frame[x][z][y]=true;
					}
				}
			}
		}
		return frame;
		
		
	}
	private boolean[][][] getfilledArea(CuboidClipboard cc){
		boolean[][] dia_tmp_x =null;
		boolean[][] dia_tmp_y =null;
		boolean[][] dia_tmp_z =null;
		
		int max_x_old = cc.getWidth();
		int max_z_old = cc.getLength();
		int max_y_old = cc.getHeight();
		

		boolean[][][] area = new boolean[max_x_old][max_z_old][max_y_old];
		boolean[][][] filled = new boolean[max_x_old][max_z_old][max_y_old];
		boolean[][][] filled_x = new boolean[max_x_old][max_z_old][max_y_old];
		boolean[][][] filled_y = new boolean[max_x_old][max_z_old][max_y_old];
		boolean[][][] filled_z = new boolean[max_x_old][max_z_old][max_y_old];
		
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
						filled_y[x][z][y]=true;
					}
				}
			}
				
			
		}
		
		
		
		for(int x=0;x<max_x_old;x++){

			dia_tmp_y = new boolean[max_y_old][max_z_old];
			dia_tmp_z = new boolean[max_y_old][max_z_old];
			int z_s = 0;
			int z_e = 0;
			int x_s = 0;
			int x_e = 0;
			int y_s = 0;
			int y_e = 0;
			
			//bindind z
			for(int y=0;y<max_y_old;y++){
				
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
					dia_tmp_z[y][z]=true;
				}
			}
			

			//bindind y
			for(int z=0;z<max_z_old;z++){	
				for(int y=0;y<max_y_old;y++){
					if(area[x][z][y]){
						y_s = y;
						break;
					}
				}
				for(int y=max_y_old-1;y>=0;y--){
					if(area[x][z][y]){
						y_e = y;
						break;
					}
				}
				for(int y =y_s;y<=y_e;y++){
					dia_tmp_y[y][z]=true;
				}
			}
			
			
			for(int y=0;y<max_y_old;y++){
				for(int z=0;z<max_z_old;z++){
					if(dia_tmp_y[y][z]  &&  dia_tmp_z[y][z]){
						filled_x[x][z][y]=true;
					}
				}
			}
				
			
		}
		
		for(int z=0;z<max_z_old;z++){

			dia_tmp_x = new boolean[max_x_old][max_y_old];
			dia_tmp_y = new boolean[max_x_old][max_y_old];
			int z_s = 0;
			int z_e = 0;
			int x_s = 0;
			int x_e = 0;
			int y_s = 0;
			int y_e = 0;
			
			//bindind x
			for(int y=0;y<max_y_old;y++){
				
				for(int x=0;x<max_x_old;x++){
					if(area[x][z][y]){
						x_s=x;
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
					dia_tmp_x[x][y]=true;
				}
			}
			

			//bindind y
			for(int x=0;x<max_x_old;x++){	
				for(int y=0;y<max_y_old;y++){
					if(area[x][z][y]){
						y_s = y;
						break;
					}
				}
				for(int y=max_y_old-1;y>=0;y--){
					if(area[x][z][y]){
						y_e = y;
						break;
					}
				}
				for(int y =y_s;y<=y_e;y++){
					dia_tmp_y[x][y]=true;
				}
			}
			
			
			for(int y=0;y<max_y_old;y++){
				for(int x=0;x<max_x_old;x++){
					if(dia_tmp_x[x][y]  &&  dia_tmp_y[x][y]){
						filled_z[x][z][y]=true;
					}
				}
			}
				
			
		}
		
		for(int x=0;x<max_x_old;x++){
			for(int y=0;y<max_y_old;y++){
				for(int z=0;z<max_z_old;z++){
					if(filled_x[x][z][y]  ||  filled_y[x][z][y]  ||  filled_z[x][z][y]){
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
		else if(block_id==Material.LEAVES.getId()  ||  
				block_id==Material.LEAVES_2.getId() ){
				
				block_id = Material.AIR.getId();
		}
		return block_id;
	}
	
	
	public static void main(String[] args) {
		int size = 17;
		int[] s = IntStream.range(0,size).toArray(); 
		
		//int[] ans = generateExpandedHeightSequence(s,size*2);
		int[] ans = CyberWorldObjectGenerator.generateExpandedSequence(s,5, 34);
		for(int i=0;i<ans.length;i++){
			System.out.print(ans[i]+",");
		}

		System.out.print("\n"+ans.length);
		
	}
	private static int[] generateExpandedHeightSequence(int[] ori, int max_size){

		int ori_size = ori.length;
		
		if(max_size>ori_size){
			int ro_number = max_size - ori_size;
			int[] repeated_idx = new int[(int) Math.ceil(ro_number)];
			int[] ans = new int[max_size];
			
			for(int i=0;i<ro_number;i++){
				double new_idx = i*((double)ori_size/ro_number);
				if(new_idx<ori_size){
					repeated_idx[i] = (int) Math.floor(new_idx);
				}
			}
			
			System.arraycopy(ori, 0, ans, 0, ori.length);
			System.arraycopy(repeated_idx, 0, ans, ori.length, repeated_idx.length);
			Arrays.sort(ans);
			return ans;
		}
		else{
			return ori;
		}

		
		
		
	}
	private static int[] generateExpandedSequence(int[] ori, int l, int max_size){
		int[] ans =null;
		int middle =0;
		int end = 0;
		int t  =0;
		int new_end  =0;
		int new_middle  =0;
		int current_size = ori.length;
		int size_inc = 0;
		int offset = 0;
		
		middle = ori.length/2;
		end = ori.length;
		if(l%2==0){
			t = (l)/2;
		}
		else{
			t = (l+1)/2;
		}
		
		if( t==0 ||  l==current_size  ||  current_size+4*t>max_size){
			return ori;
		}
		int ans_bound = current_size;
		while(ans_bound+4*t-offset<=max_size){
			ans_bound+=4*t-offset;
		}
		ans  = new int[ans_bound];
		
		while(current_size+4*t-offset<=max_size){
			middle = ori.length/2;
			end = ori.length;
			new_end = (ori.length+4*t);
			new_middle = (new_end)/2;
			
			//left
			for(int i=0;i<middle;i++){
				ans[size_inc]=ori[i]; // 0 ~ middle -1
				size_inc++;
			}
			//left dup decend
			for(int i=0;i<t;i++){
				ans[size_inc]=ori[middle-i];// middle ~  middle + t-1
				//System.out.print(ans[middle+i]+",");
				size_inc++;
			}
			//left dup ascend
			for(int i=0;i<t;i++){
				ans[size_inc]=ori[middle+i-t];// middle + t ~ middle +2*t-1
				//System.out.print(ans[middle+i+t]+",");
				size_inc++;
			}
			
			ans[size_inc] = ori[middle]; // middle + 2*t
			size_inc++;
			
			//right dup decend
			for(int i=0;i<t;i++){
				ans[size_inc]=ori[middle+i+1];// middle +2*t  ~ middle + 3*t -1
				//System.out.print(ans[new_middle+i]+",");
				size_inc++;
			}
			//right dup ascend
			for(int i=0;i<t;i++){
				ans[size_inc]=ori[middle-i+t+1];// middle + 3*t ~ middle + 4*t -1  
				//System.out.print(ans[new_middle+t+i]+",");
				size_inc++;
			}
			//right
			for(int i=middle+1;i<end;i++){
				ans[size_inc]=ori[i]; // middle + 1 ~ end  
				//System.out.print(ans[i+4*t]+",");
				size_inc++;
			}
			
			
			current_size += 4*t-offset;
			ori = new int[current_size];
			for(int i=0;i<current_size;i++){
				ori[i]=ans[i];
			}
			size_inc=0;
			
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
 		