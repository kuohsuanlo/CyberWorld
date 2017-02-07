
package io.github.kuohsuanlo.cyberworld;

import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class CyberWorldBlockPopulator extends BlockPopulator
{
    byte[] layerDataValues;
    protected CyberWorldBlockPopulator(byte[] layerDataValues)
    {
        this.layerDataValues = layerDataValues;
    }

    public void populate(World world, Random random, Chunk chunk){
    	
		return;
		
    }
}