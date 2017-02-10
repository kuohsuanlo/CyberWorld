
package io.github.kuohsuanlo.cyberworld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;


public class CyberWorldChunkGenerator extends ChunkGenerator{

    private CyberWorld plugin;
    public static final int MAX_SPACE_HEIGHT = 256; // 0-255

    private final CyberWorldObjectGenerator og;
    public CyberWorldObjectGenerator getOg() {
		return og;
	}
	private final CyberWorldBiomeGenerator bg;
    public CyberWorldChunkGenerator(CyberWorld p, CityStreetGenerator c){
    	plugin = p;
    	bg = new CyberWorldBiomeGenerator(plugin.BIOME_TYPES,plugin.BIOME_OCTAVE);
    	og = new CyberWorldObjectGenerator(plugin,plugin.BIOME_TYPES,bg,c);
    }
    
    @Override
    public ChunkData generateChunkData(World world, Random random, int chkx, int chkz, BiomeGrid biomes){
    	
    	ChunkData chunkdata = createChunkData(world);
    	int biome_type = bg.generateType(chkx, chkz,true);
		if(biome_type<plugin.BIOME_NUMBER_WITH_BUILDING){
        	chunkdata = og.generateBottom(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateCitySurface(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateCityRoad(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateCitySewer(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateGroundDecoration(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateBuilding(chunkdata, random, chkx, chkz,biome_type, biomes, 2, false);
        	chunkdata = og.generateGroundSign(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateUnderGroundBuilding(chunkdata, random, chkx, chkz,biome_type, biomes, 2);
        	chunkdata = og.generateHighway(chunkdata, random, chkx, chkz,biome_type, biomes);
    	}
    	else{
        	chunkdata = og.generateBottom(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateTerrain(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateFactoryGround(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateFactoryRoad(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateFactorySewer(chunkdata, random, chkx, chkz,biome_type, biomes);
    	}
		
        return chunkdata;
    } 
    /*
    @Override
    public List<BlockPopulator> getDefaultPopulators(World world)
    {
    	return new ArrayList<BlockPopulator>();
    	
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
    }*/
}