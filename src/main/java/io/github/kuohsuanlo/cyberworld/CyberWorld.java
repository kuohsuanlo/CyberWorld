package io.github.kuohsuanlo.cyberworld;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

/* 0 = ground
 * 1 = road-NS
 * 2 = road-EW
 * <0= building
 * 
 * 
 * 
 */
public class CyberWorld extends JavaPlugin{
	private String version_string = "0.7";

    public static final String WINDOWS_PATH="./plugins/CyberWorld/schematics/";
    
    private Logger log = Logger.getLogger("Minecraft");
    private CyberWorldChunkGenerator c;
    private CityStreetGenerator cityg;
    public FileConfiguration config;
    
    /*Configuration*/
	public int BIOME_TYPES;
    public int BIOME_NUMBER_WITH_BUILDING;
    public int BIOME_OCTAVE;
    
    public int GROUND_LEVEL;
    public double SIGN_WALL_BLOCK_RATIO;
    public double SIGN_WALL_MINIMAL_WIDTH ;
    public double SIGN_WALL_COVERAGE_RATIO_MIN;
    public double SIGN_WALL_COVERAGE_RATIO_MAX;
    
    public int GROUND_SIGN_EXTRA_HEIGHT_MAX;
    public int GROUND_SIGN_CONTENT_HEIGHT_MAX;
    public int GROUND_SIGN_HEIGHT_BASE;
    public int GROUND_SIGN_BASE_HZ_SHIFT_MAX;
    
    
    public double HEIGHT_RAND_ODDS;
    public double HEIGHT_RAND_RATIO ;
    public int MAP_W;
    public int MAP_H;
    public int TERRAIN_OCTAVE;
    public int TERRAIN_HEIGHT ;
    public int SEA_LEVEL ;

    public int[] all_building_level;
    public int[] underground_building_level;
    public int L1_HEIGHT;
    public int L2_HEIGHT;
    public int L3_HEIGHT;
    public int[] LAYER_HEIGHT;
    

    public int sz_deco;
    public int sz_s;
    public int sz_m;
    public int sz_l;
    public int sz_block;

    
    public CyberWorld(){
    	readConfig();
        createBiomeFolder();
        //readCityStreetGenerator();
        
        c = new CyberWorldChunkGenerator(this,cityg);
    } 

    public void readConfig(){
    	config = this.getConfig();
    	config.addDefault("version",version_string);
    	config.addDefault("BIOME_TYPES",3);
    	config.addDefault("BIOME_NUMBER_WITH_BUILDING",4);
    	config.addDefault("BIOME_OCTAVE",5);
    	config.addDefault("GROUND_LEVEL",50);
    	
    	config.addDefault("SIGN_WALL_BLOCK_RATIO",0.4);
    	config.addDefault("SIGN_WALL_MINIMAL_WIDTH",12);
    	config.addDefault("SIGN_WALL_COVERAGE_RATIO_MIN",0.2);
    	config.addDefault("SIGN_WALL_COVERAGE_RATIO_MAX",0.8);
    	config.addDefault("HEIGHT_RAND_ODDS",0.5);
    	config.addDefault("HEIGHT_RAND_RATIO",1.5);
    	
    	config.addDefault("GROUND_SIGN_EXTRA_HEIGHT_MAX",20);
    	config.addDefault("GROUND_SIGN_CONTENT_HEIGHT_MAX",4);
    	config.addDefault("GROUND_SIGN_HEIGHT_BASE",10);
    	config.addDefault("GROUND_SIGN_BASE_HZ_SHIFT_MAX",4);

    	config.addDefault("MAP_W",1000);
    	config.addDefault("MAP_H",1000);
    	config.addDefault("TERRAIN_OCTAVE",8);
    	config.addDefault("TERRAIN_HEIGHT",100);
    	config.addDefault("SEA_LEVEL",45);
    	
    	config.addDefault("L1_HEIGHT",20);
    	config.addDefault("L2_HEIGHT",40);
    	config.addDefault("L3_HEIGHT",80);
    	

    	config.addDefault("SIZE_DECORATION",1);
    	config.addDefault("SIZE_SMALL",2);
    	config.addDefault("SIZE_MEDIUM",4);
    	config.addDefault("SIZE_LARGE",15);
    	config.addDefault("SIZE_BLOCK",20);

    	config.options().copyDefaults(true);
    	saveConfig();
 
    	BIOME_TYPES = config.getInt("BIOME_TYPES");
    	BIOME_NUMBER_WITH_BUILDING = config.getInt("BIOME_NUMBER_WITH_BUILDING")+1;
    	BIOME_OCTAVE = config.getInt("BIOME_OCTAVE");
    	GROUND_LEVEL = config.getInt("GROUND_LEVEL");
    	
    	SIGN_WALL_BLOCK_RATIO = config.getDouble("SIGN_WALL_BLOCK_RATIO");
    	SIGN_WALL_MINIMAL_WIDTH = config.getDouble("SIGN_WALL_MINIMAL_WIDTH");
    	SIGN_WALL_COVERAGE_RATIO_MIN = config.getDouble("SIGN_WALL_COVERAGE_RATIO_MIN");
    	SIGN_WALL_COVERAGE_RATIO_MAX = config.getDouble("SIGN_WALL_COVERAGE_RATIO_MAX");
    	HEIGHT_RAND_ODDS = config.getDouble("HEIGHT_RAND_ODDS");
    	HEIGHT_RAND_RATIO = config.getDouble("HEIGHT_RAND_RATIO");

    	GROUND_SIGN_EXTRA_HEIGHT_MAX = config.getInt("GROUND_SIGN_EXTRA_HEIGHT_MAX");
    	GROUND_SIGN_CONTENT_HEIGHT_MAX = config.getInt("GROUND_SIGN_CONTENT_HEIGHT_MAX");
    	GROUND_SIGN_HEIGHT_BASE = config.getInt("GROUND_SIGN_HEIGHT_BASE");
    	GROUND_SIGN_BASE_HZ_SHIFT_MAX = config.getInt("GROUND_SIGN_BASE_HZ_SHIFT_MAX");
    	
    	
    	MAP_W = config.getInt("MAP_W");
    	MAP_H = config.getInt("MAP_H");
    	TERRAIN_OCTAVE = config.getInt("TERRAIN_OCTAVE");
    	TERRAIN_HEIGHT = config.getInt("TERRAIN_HEIGHT");
    	SEA_LEVEL = config.getInt("SEA_LEVEL");
    	

    	L1_HEIGHT = config.getInt("L1_HEIGHT");
    	L2_HEIGHT = config.getInt("L2_HEIGHT");
    	L3_HEIGHT = config.getInt("L3_HEIGHT");

    	
    	sz_deco = config.getInt("SIZE_DECORATION");
    	sz_s = config.getInt("SIZE_SMALL");
    	sz_m = config.getInt("SIZE_MEDIUM");
    	sz_l = config.getInt("SIZE_LARGE");
    	sz_block = config.getInt("SIZE_BLOCK");
    	

        all_building_level = new int[3];
        all_building_level[0]=GROUND_LEVEL+3;
        all_building_level[1]=GROUND_LEVEL+3;
        all_building_level[2]=GROUND_LEVEL+3;
        
        underground_building_level = new int[3];
        underground_building_level[0]=3;
        underground_building_level[1]=3;
        underground_building_level[2]=3;
        
        LAYER_HEIGHT = new int[3];
        LAYER_HEIGHT[0]=GROUND_LEVEL+L1_HEIGHT;
        LAYER_HEIGHT[1]=GROUND_LEVEL+L2_HEIGHT;
        LAYER_HEIGHT[2]=GROUND_LEVEL+L3_HEIGHT;
        

    	config.options().copyDefaults(true);
    }
    public void onEnable(){
    	
        reportMessage(" enabled");
        
    }
    public void onDisable(){
    	//saveCityStreetGenerator();
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id){
        return c;
    }
    
    
    public String getPluginName() {
		return getDescription().getName();
	}
	private String getQuotedPluginName() {
		return "[" + getPluginName() + "]";
	}
	public void reportMessage(String message) {
		if (!message.startsWith("["))
			message = " " + message;
		log.info(getQuotedPluginName() + message);
	}
	public void reportMessage(String message1, String message2) {
		reportMessage(message1);
		log.info(" \\__" + message2);
	}

    private void createBiomeFolder(){
    	if(createFolder("./plugins/CyberWorld")){
    		
        }
    	
        if(createFolder("./plugins/CyberWorld/citymap_pregen")){
        	
        }
        if(createFolder("./plugins/CyberWorld/schematics")){
        	
        }
        
        if(createFolder("./plugins/CyberWorld/schematics/"+"/citysurface")){
        	
        }
        if(createFolder("./plugins/CyberWorld/schematics/"+"/highway")){
        	
        }
        if(createFolder("./plugins/CyberWorld/schematics/"+"/underground")){
        	
        }
        if(createFolder("./plugins/CyberWorld/schematics/"+"/import")){
        	
        }
        for(int i=0;i< BIOME_NUMBER_WITH_BUILDING;i++){
            if(createFolder("./plugins/CyberWorld/schematics/"+i+"/citysurface")){
            	
            }
            if(createFolder("./plugins/CyberWorld/schematics/"+i+"/highway")){
            	
            }
            if(createFolder("./plugins/CyberWorld/schematics/"+i+"/underground")){
            	
            }
            if(createFolder("./plugins/CyberWorld/schematics/"+i+"/import")){
            	
            }
        }
        
		
        
    }
    private static boolean createFolder(String path){
    	 
  	   	File file = new File(path);
  	   	if(!file.exists()){
  	   		new File(path).mkdirs();
  	   		return true;
  	   	}
  	   	return false;

    }    
    private void saveCityStreetGenerator(){
    	try {
        	FileOutputStream fos;
			fos = new FileOutputStream("./plugins/CyberWorld/citymap_pregen/"+"default.cityobj");
        	ObjectOutputStream oos = new ObjectOutputStream(fos);
        	oos.writeObject(c.getOg().getCg());
        	oos.close();
		} catch (Exception e  ) {
			// TODO Auto-generated catch block
			System.out.print("[CyberWorld] : City Map saving error. It will regenerate next time.");
			e.printStackTrace();
		} 
    }
    private void readCityStreetGenerator(){
    	//Creating
		try {
	  	   	String path = "./plugins/CyberWorld/citymap_pregen/"+"default.cityobj";
	  	   	File file = new File(path);
	  	   	
	  	   	
	  	   	if(file.exists()){
	  	   		System.out.print("[CyberWorld] : Loading City Map... Please wait.");
	  	   		System.out.println("[CyberWorld] : successfully load from "+"./plugins/CyberWorld/citymap_pregen/"+"default.cityobj");	
	  	   		
				FileInputStream fis;
				fis = new FileInputStream("./plugins/CyberWorld/citymap_pregen/"+"default.cityobj");
		    	ObjectInputStream ois = new ObjectInputStream(fis);
		    	cityg = (CityStreetGenerator) ois.readObject();
		    	ois.close();
	  	   		System.out.print("[CyberWorld] : City Map loaded.");
	  	   	}
	  	   	else{
	  	   		cityg =null;
	  	   	}
		} catch (Exception e  ) {
  	   		cityg =null;
  	   		System.out.print("[CyberWorld] : City Map loading error, regenerating...");
			e.printStackTrace();
		}
    			
    }


}