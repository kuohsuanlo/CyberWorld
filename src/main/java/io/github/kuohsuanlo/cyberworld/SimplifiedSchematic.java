
package io.github.kuohsuanlo.cyberworld;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BlockData;



public class SimplifiedSchematic {

   private int [][][] blocks_id;
   private byte [][][] blocks_data;
   private Vector origin;
   private Vector offset;
   private Vector size;
   private int angle;
   public SimplifiedSchematic(CuboidClipboard cc){
	   int X = cc.getWidth();
	   int Y = cc.getHeight();
	   int Z = cc.getLength();
	   
	   blocks_id = new int[X][Y][Z];
	   blocks_data = new byte[X][Y][Z];
	   
	   for(int x=0;x<X;x++){
		   for(int y=0;y<Y;y++){
			   for(int z=0;z<Z;z++){
				   int b = cc.getBlock( new Vector(x,y,z)).getId();
				   byte d = (byte)cc.getBlock( new Vector(x,y,z)).getData();   
				   if(b!=0){
					   blocks_id[x][y][z] = b;
					   if(d!=0){
						   blocks_data[x][y][z]  = d;
					   }
				   }
				  
			   }
		   }
	   }
	   
	   cc= null;
	   size = new Vector(X,Y,Z);
	   origin = new Vector(0,0,0);
	   offset = new Vector(0,0,0);
   }
   
   
   public void rotate(int angle) {
       angle = angle % 360;
       if (angle % 90 != 0) { // Can only rotate 90 degrees at the moment
           return;
       }
       
       if(angle%360==0){
    	   return;
       }
       
       final int width = getWidth();
       final int length = getLength();
       final int height = getHeight();
       final Vector sizeRotated = size.transform2D(angle, 0, 0, 0, 0);
       final int shiftX = sizeRotated.getX() < 0 ? -sizeRotated.getBlockX() - 1 : 0;
       final int shiftZ = sizeRotated.getZ() < 0 ? -sizeRotated.getBlockZ() - 1 : 0;

       final int[][][] new_ids = new int
               [Math.abs(sizeRotated.getBlockX())]
               [Math.abs(sizeRotated.getBlockY())]
               [Math.abs(sizeRotated.getBlockZ())];
       final byte[][][] new_datas = new byte
               [Math.abs(sizeRotated.getBlockX())]
               [Math.abs(sizeRotated.getBlockY())]
               [Math.abs(sizeRotated.getBlockZ())];

       for (int x = 0; x < width; x++) {
           for (int z = 0; z < length; z++) {
               final Vector2D v = new Vector2D(x, z).transform2D(angle, 0, 0, shiftX, shiftZ);
               final int newX = v.getBlockX();
               final int newZ = v.getBlockZ();
               for (int y = 0; y < height; y++) {
                   final int new_id = blocks_id[x][y][z];
                   final byte new_data = blocks_data[x][y][z];
                   new_ids[newX][y][newZ] = new_id;
                   new_datas[newX][y][newZ] = new_data;
                  
                   
               }
           }
       }

       blocks_id = new_ids;
       blocks_data = new_datas;
       
       size = new Vector(Math.abs(sizeRotated.getBlockX()),
                         Math.abs(sizeRotated.getBlockY()),
                         Math.abs(sizeRotated.getBlockZ()));
       offset = offset.transform2D(angle, 0, 0, 0, 0)
               .subtract(shiftX, 0, shiftZ);
   }

   
   public int getWidth() {
       return size.getBlockX();
   }
   public int getLength() {
       return size.getBlockZ();
   }
   public int getHeight() {
       return size.getBlockY();
   }

   public int getBlockId(int x, int y,int z){

	   return blocks_id[x][y][z];
	   
	   
   }
   public byte getBlockData(int x, int y,int z){
   
	   return blocks_data[x][y][z];
	   
	   
   }
}