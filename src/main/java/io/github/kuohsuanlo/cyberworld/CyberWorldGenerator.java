
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

    public static final int MAX_BUILDING_WIDTH =4;
    public static final int MAX_DISTANCE_BETWEEN_BUILDING =2;
    public static final int MAX_ROAD_WIDTH =1;

    public void onEnable(){
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