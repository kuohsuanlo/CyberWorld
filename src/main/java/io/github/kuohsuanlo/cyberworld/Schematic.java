package io.github.kuohsuanlo.cyberworld;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.CuboidClipboard.FlipDirection;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.session.ClipboardHolder;

@SuppressWarnings("deprecation")
public class Schematic {
	SchematicFormat schematic = null;
	CuboidClipboard clipboard = null;
    public CuboidClipboard getSchematic(String schematicName) {
        File dir = new File(schematicName);

        SchematicFormat schematic = SchematicFormat.getFormat(dir);
      
		try {
			clipboard = schematic.load(dir);
			
		} catch (DataException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dir = null;
		schematic = null;
        return clipboard;

    }

}