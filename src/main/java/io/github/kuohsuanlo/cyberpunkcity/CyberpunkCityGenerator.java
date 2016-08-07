
package io.github.kuohsuanlo.cyberpunkcity;




import org.bukkit.plugin.java.JavaPlugin;



import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class CyberpunkCityGenerator extends JavaPlugin
{
    private Logger log = Logger.getLogger("Minecraft");
    PluginDescriptionFile pluginDescriptionFile;

    public void onEnable()
    {
        pluginDescriptionFile = getDescription();
        log.info("[CyberpunkCityPlugin] " + pluginDescriptionFile.getFullName() + " enabled");
        
       
    }

    public void onDisable()
    {
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)
    {
        return new CyberpunkCityChunkGenerator();
    }
}