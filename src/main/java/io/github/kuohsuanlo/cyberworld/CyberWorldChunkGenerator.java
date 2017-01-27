
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

import ru.beykerykt.lightapi.LightAPI;
import ru.beykerykt.lightapi.chunks.ChunkInfo;


public class CyberWorldChunkGenerator extends ChunkGenerator
{

    private byte[] layerDataValues;

    public static final int MAX_SPACE_HEIGHT = 256; // 0-255
    private final int BIOME_OCTAVE = 5;
    private final int BIOME_TYPES;
    private final int BIOME_NUMBERS;
    private final CyberWorldObjectGenerator og;
    private final CyberWorldBiomeGenerator bg;
    public CyberWorldChunkGenerator(int biome_types){
    	BIOME_TYPES = biome_types;
    	BIOME_NUMBERS = (int) Math.round(Math.pow(2, biome_types));
    	bg = new CyberWorldBiomeGenerator(biome_types,BIOME_OCTAVE);
    	og = new CyberWorldObjectGenerator(BIOME_NUMBERS,bg);
    }
    public static final int CLASS_0_AREA=0;
    public static final int CLASS_1_AREA=1;
    public static final int CLASS_2_AREA=2;
    public static final int CLASS_3_AREA=3;
    public static final int CLASS_4_AREA=4;
    public static final int CLASS_5_AREA=5;
    public static final int CLASS_6_AREA=6;

    @Override
    public ChunkData generateChunkData(World world, Random random, int chkx, int chkz, BiomeGrid biomes){
    	ChunkData chunkdata = createChunkData(world);
    	//random.setSeed(world.getSeed()/2+1205*chkx+722*chkz);
    	int biome_type = bg.generateType(chkx, chkz,true);
		if(biome_type==CLASS_0_AREA){
        	chunkdata = og.generateBottom(chunkdata, random, chkx, chkz,biome_type, biomes);
        	//chunkdata = og.generateTerrain(chunkdata, random, chkx, chkz, biomes);
        	chunkdata = og.generateCitySurface(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateCityRoad(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateCitySewer(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateBuilding(chunkdata, random, chkx, chkz,biome_type, biomes, 2);
        	chunkdata = og.generateUnderGroundBuilding(chunkdata, random, chkx, chkz,biome_type, biomes, 2);
        	chunkdata = og.generateGroundDecoration(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateHighway(chunkdata, random, chkx, chkz,biome_type, biomes);
    	}
		else if(biome_type==CLASS_1_AREA){
        	chunkdata = og.generateBottom(chunkdata, random, chkx, chkz,biome_type, biomes);
        	//chunkdata = og.generateTerrain(chunkdata, random, chkx, chkz, biomes);
        	chunkdata = og.generateCitySurface(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateCityRoad(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateCitySewer(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateBuilding(chunkdata, random, chkx, chkz,biome_type, biomes, 2);
        	chunkdata = og.generateUnderGroundBuilding(chunkdata, random, chkx, chkz,biome_type, biomes, 2);
        	chunkdata = og.generateGroundDecoration(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateHighway(chunkdata, random, chkx, chkz,biome_type, biomes);
    	}
		else if(biome_type==CLASS_2_AREA){
        	chunkdata = og.generateBottom(chunkdata, random, chkx, chkz,biome_type, biomes);
        	//chunkdata = og.generateTerrain(chunkdata, random, chkx, chkz, biomes);
        	chunkdata = og.generateCitySurface(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateCityRoad(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateCitySewer(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateBuilding(chunkdata, random, chkx, chkz,biome_type, biomes, 1);
        	chunkdata = og.generateUnderGroundBuilding(chunkdata, random, chkx, chkz,biome_type, biomes, 1);
        	chunkdata = og.generateHighway(chunkdata, random, chkx, chkz,biome_type, biomes);
    	}
		else if(biome_type==CLASS_3_AREA){
        	chunkdata = og.generateBottom(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateTerrain(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateCitySurface(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateCityRoad(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateCitySewer(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateBuilding(chunkdata, random, chkx, chkz,biome_type, biomes, 1);
        	chunkdata = og.generateUnderGroundBuilding(chunkdata, random, chkx, chkz,biome_type, biomes, 1);
    	}
		else if(biome_type==CLASS_4_AREA){
        	chunkdata = og.generateBottom(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateTerrain(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateFactoryRoad(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateFactorySewer(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateUnderGroundBuilding(chunkdata, random, chkx, chkz,biome_type, biomes, 0);
    	}
		else if(biome_type==CLASS_5_AREA){
        	chunkdata = og.generateBottom(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateTerrain(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateFactoryGround(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateFactoryRoad(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateFactorySewer(chunkdata, random, chkx, chkz,biome_type, biomes);
        	chunkdata = og.generateUnderGroundBuilding(chunkdata, random, chkx, chkz,biome_type, biomes, 0);
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