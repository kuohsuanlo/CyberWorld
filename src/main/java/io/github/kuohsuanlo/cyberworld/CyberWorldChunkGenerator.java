
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


public class CyberWorldChunkGenerator extends ChunkGenerator
{

    private byte[] layerDataValues;

    private CyberWorld plugin;
    public static final int MAX_SPACE_HEIGHT = 256; // 0-255
    public static final int BIOME_NUMBER_WITH_BUILDING =4;
    private final int BIOME_OCTAVE = 5;
    private final int BIOME_TYPES;
    private final int BIOME_NUMBERS;
    private final CyberWorldObjectGenerator og;
    public CyberWorldObjectGenerator getOg() {
		return og;
	}
	private final CyberWorldBiomeGenerator bg;
    public CyberWorldChunkGenerator(CyberWorld p, int biome_types, CityStreetGenerator c){
    	plugin = p;
    	BIOME_TYPES = biome_types;
    	BIOME_NUMBERS = (int) Math.round(Math.pow(2, BIOME_TYPES));
    	bg = new CyberWorldBiomeGenerator(BIOME_TYPES,BIOME_OCTAVE);
    	og = new CyberWorldObjectGenerator(BIOME_NUMBERS,bg,c);
    }
    
    @Override
    public ChunkData generateChunkData(World world, Random random, int chkx, int chkz, BiomeGrid biomes){
    	
    	ChunkData chunkdata = createChunkData(world);
    	//random.setSeed(world.getSeed()/2+1205*chkx+722*chkz);
    	int biome_type = bg.generateType(chkx, chkz,true);
		if(biome_type<=BIOME_NUMBER_WITH_BUILDING){
        	chunkdata = og.generateBottom(chunkdata, random, chkx, chkz,biome_type, biomes);
        	//chunkdata = og.generateTerrain(chunkdata, random, chkx, chkz, biomes);
        	chunkdata = og.generateCitySurface(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateCityRoad(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateCitySewer(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateBuilding(chunkdata, random, chkx, chkz,biome_type, biomes, 2, false);
        	chunkdata = og.generateUnderGroundBuilding(chunkdata, random, chkx, chkz,biome_type, biomes, 2);
        	chunkdata = og.generateGroundDecoration(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateHighway(chunkdata, random, chkx, chkz,biome_type, biomes);
    	}
    	else{
        	chunkdata = og.generateBottom(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateTerrain(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateFactoryGround(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateFactoryRoad(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateFactorySewer(chunkdata, random, chkx, chkz,biome_type, biomes);
    	}


		
		//update light
		/*
		for(int yi=0;yi<16;yi++){
			List<ChunkInfo> chkinfo = LightAPI.collectChunks(world,chkx*16+7,yi*16+7,chkz*16+7);
			for(int i=0;i<chkinfo.size();i++){
				LightAPI.updateChunk(chkinfo.get(i));
			}
		
		}*/
		
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