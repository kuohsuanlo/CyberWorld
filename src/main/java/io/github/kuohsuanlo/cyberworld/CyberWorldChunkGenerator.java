
package io.github.kuohsuanlo.cyberworld;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;
import com.sk89q.worldedit.schematic.SchematicFormat;

import static java.lang.System.arraycopy;
import static java.lang.System.inheritedChannel;

public class CyberWorldChunkGenerator extends ChunkGenerator
{
    private Logger log = Logger.getLogger("Minecraft");
    private short[] layer;
    private byte[] layerDataValues;

    public static final int MAX_SPACE_HEIGHT = 256; // 0-255
    private CyberWorldObjectGenerator og=null;

    public CyberWorldChunkGenerator(){
    	og = new CyberWorldObjectGenerator();
    }


    @Override
    public ChunkData generateChunkData(World world, Random random, int chkx, int chkz, BiomeGrid biomes){
    	ChunkData chunkdata = createChunkData(world);
    	
    	
    	random.setSeed(world.getSeed()/2+chkx+chkz);
    	//log.warning("[CyberWorld] : "+chkx+" "+chkz+" "+ random.nextInt());
    	//log.warning("[CyberWorld] : "+chkx+" "+chkz+" "+ random.nextInt());
    	//random.setSeed(world.getSeed()/2+chkx+chkz);
    	//log.warning("[CyberWorld] : "+chkx+" "+chkz+" "+ random.nextInt());
    	//log.warning("[CyberWorld] : "+chkx+" "+chkz+" "+ random.nextInt());
        
    	
    	chunkdata = og.generateTerrain(chunkdata, random, chkx, chkz, biomes);
    	chunkdata = og.generateRoadBuilding(chunkdata, random, chkx, chkz, biomes);
        //og.generateSewer(world, random, chkx, chkz, biomes);
        //og.generateDecoration(world, random, chkx, chkz, biomes);
        //og.generateMines(world, random, chkx, chkz, biomes);
        return chunkdata;
    	
    }
    
    @Override
    public List<BlockPopulator> getDefaultPopulators(World world)
    {
        if (layerDataValues != null)
        {
            return Arrays.asList((BlockPopulator)new CyberWorldBlockPopulator(layerDataValues));
        } else
        {
            // This is the default, but just in case default populators change to stock minecraft populators by default...
            return new ArrayList<BlockPopulator>();
        }
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random)
    {
        if (!world.isChunkLoaded(0, 0))
        {
            world.loadChunk(0, 0);
        }

        if ((world.getHighestBlockYAt(0, 0) <= 0) && (world.getBlockAt(0, 0, 0).getType() == Material.AIR)) // SPACE!
        {
            return new Location(world, 0, 64, 0); // Lets allow people to drop a little before hitting the void then shall we?
        }

        return new Location(world, 0, world.getHighestBlockYAt(0, 0), 0);
    }
}