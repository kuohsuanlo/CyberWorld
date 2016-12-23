
package io.github.kuohsuanlo.cyberworld;

import org.bukkit.plugin.java.JavaPlugin;



import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;
import java.util.logging.Logger;
/* 0 = ground
 * 1 = road-NS
 * 2 = road-EW
 * <0= building
 * 
 * 
 * 
 */
public class CyberWorldGenerator extends JavaPlugin{
    private Logger log = Logger.getLogger("Minecraft");
    PluginDescriptionFile pluginDescriptionFile;

    public static final int CHUNK_RADIUS = 100;
    public static final int CHUNK_DIAMETER =2*CHUNK_RADIUS;
    public int[][] blueprint = new int[CHUNK_DIAMETER][CHUNK_DIAMETER];
    public Random rng;
	//Ground
	//  ----->i
	//  |
	//  V j
	//  0-100 -> -100~   0
	//101-199 -> +  1~+ 99
    
    public static final int MAX_BUILDING_WIDTH =4;
    public static final int MAX_DISTANCE_BETWEEN_BUILDING =2;
    public static final int MAX_ROAD_WIDTH =1;
    public void createBlueprint(){
    	//init
    	for(int i=0;i<CHUNK_DIAMETER;i++){
    		for(int j=0;j<CHUNK_DIAMETER;j++){
    			blueprint[i][j]=0;
    		}
    	}
    	
    	
    	
    }
    private void fillBuilding(){
    	
    }
    
    public void onEnable(){
    	rng = new Random();
        rng.setSeed(123456789);
         
        pluginDescriptionFile = getDescription();
        log.info("[CyberWorld] " + pluginDescriptionFile.getFullName() + " enabled");
        
       
    }

    public void onDisable(){
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id){
        return new CyberWorldChunkGenerator();
    }
}