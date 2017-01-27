package io.github.kuohsuanlo.cyberworld;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    private PluginDescriptionFile pluginDescriptionFile;
    private CyberWorldChunkGenerator c ;
    private final int BIOME_TYPES = 3;
    private final int BIOME_NUMBERS = (int) Math.round( Math.pow(2, BIOME_TYPES));
    public void onEnable(){
    	
        pluginDescriptionFile = getDescription();
        log.info("[CyberWorld] " + pluginDescriptionFile.getFullName() + " enabled");
        
        if(this.createFolder("./plugins/CyberWorld")){
        	
        }
        if(this.createFolder("./plugins/CyberWorld/schematics")){
        	
        }
        
        if(this.createFolder("./plugins/CyberWorld/schematics/"+"/citysurface")){
        	
        }
        if(this.createFolder("./plugins/CyberWorld/schematics/"+"/highway")){
        	
        }
        if(this.createFolder("./plugins/CyberWorld/schematics/"+"/underground")){
        	
        }
        if(this.createFolder("./plugins/CyberWorld/schematics/"+"/import")){
        	
        }
        for(int i=0;i< BIOME_NUMBERS;i++){
            if(this.createFolder("./plugins/CyberWorld/schematics/"+i+"/citysurface")){
            	
            }
            if(this.createFolder("./plugins/CyberWorld/schematics/"+i+"/highway")){
            	
            }
            if(this.createFolder("./plugins/CyberWorld/schematics/"+i+"/underground")){
            	
            }
            if(this.createFolder("./plugins/CyberWorld/schematics/"+i+"/import")){
            	
            }
        }
        
        c = new CyberWorldChunkGenerator(3);
        
    }

    public void onDisable(){
    	
    }

    private boolean createFolder(String path){
 
  	   	File file = new File(path);
  	   	if(!file.exists()){
  	   		new File(path).mkdirs();
  	   		return true;
  	   	}
  	   	return false;

    }
    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id){
        return c;
    }
}