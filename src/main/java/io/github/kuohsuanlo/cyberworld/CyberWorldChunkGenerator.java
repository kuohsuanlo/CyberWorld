
package io.github.kuohsuanlo.cyberworld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;



public class CyberWorldChunkGenerator extends ChunkGenerator
{

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

    	
    	chunkdata = og.generateTerrain(chunkdata, random, chkx, chkz, biomes);
    	chunkdata = og.generateRoad(chunkdata, random, chkx, chkz, biomes);
    	chunkdata = og.generateSewer(chunkdata, random, chkx, chkz, biomes);
    	chunkdata = og.generateBuilding(chunkdata, random, chkx, chkz, biomes);
    	chunkdata = og.generateGroundDecoration(chunkdata, random, chkx, chkz, biomes);
    	//chunkdata = og.generateIllegalBuilding(chunkdata, random, chkx, chkz, biomes);
    	chunkdata = og.generateBuildingDecoration(chunkdata, random, chkx, chkz, biomes);
    	chunkdata = og.generateHighway(chunkdata, random, chkx, chkz, biomes);
    	//chunkdata = og.generateGroundSewer(chunkdata, random, chkx, chkz, biomes);
    	//chunkdata = og.generateCityWall(chunkdata, random, chkx, chkz, biomes);
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