package io.github.kuohsuanlo.cyberworld;

import java.util.Collections;
import java.util.Arrays;
 
/*
 * recursive backtracking algorithm
 * shamelessly borrowed from the ruby at
 * http://rosettacode.org/wiki/Maze_generation#Java
 * http://weblog.jamisbuck.org/2010/12/27/maze-generation-recursive-backtracking
 */
public class MazeGenerator {
	private final int x;
	private final int y;
	private final int[][] maze;
	private final int[][] grid;
 
	public MazeGenerator(int x, int y) {
		this.x = x;
		this.y = y;
		maze = new int[this.x][this.y];
		grid = new int[this.x*3][this.y*3];
		generateMaze(0, 0);
		transformToGrid();
	}
	private void transformToGrid(){
		for (int iy = 0; iy < y; iy++) {
			for (int ix = 0;ix < x; ix++) {
				
				int j = ix*3;
				int i = iy*3;
				switch(maze[ix][iy]) { 
	            case 0: 
	            	grid[j][i]  =1;grid[j+1][i]  =1;grid[j+2][i]  =1;
	            	grid[j][i+1]=1;grid[j+1][i+1]=0;grid[j+2][i+1]=1;
	            	grid[j][i+2]=1;grid[j+1][i+2]=1;grid[j+2][i+2]=1;
	            	break; 
	            case 1: 
	            	grid[j][i]  =1;grid[j+1][i]  =0;grid[j+2][i]  =1;
	            	grid[j][i+1]=1;grid[j+1][i+1]=0;grid[j+2][i+1]=1;
	            	grid[j][i+2]=1;grid[j+1][i+2]=1;grid[j+2][i+2]=1;
	                break; 
	            case 2: 
	            	grid[j][i]  =1;grid[j+1][i]  =1;grid[j+2][i]  =1;
	            	grid[j][i+1]=1;grid[j+1][i+1]=0;grid[j+2][i+1]=1;
	            	grid[j][i+2]=1;grid[j+1][i+2]=0;grid[j+2][i+2]=1;
	                break; 
	            case 3: 
	            	grid[j][i]  =1;grid[j+1][i]  =0;grid[j+2][i]  =1;
	            	grid[j][i+1]=1;grid[j+1][i+1]=0;grid[j+2][i+1]=1;
	            	grid[j][i+2]=1;grid[j+1][i+2]=0;grid[j+2][i+2]=1;
	                break; 
	            case 4: 
	            	grid[j][i]  =1;grid[j+1][i]  =1;grid[j+2][i]  =1;
	            	grid[j][i+1]=1;grid[j+1][i+1]=0;grid[j+2][i+1]=0;
	            	grid[j][i+2]=1;grid[j+1][i+2]=1;grid[j+2][i+2]=1;
	                break;  
	            case 5: 
	            	grid[j][i]  =1;grid[j+1][i]  =0;grid[j+2][i]  =1;
	            	grid[j][i+1]=1;grid[j+1][i+1]=0;grid[j+2][i+1]=0;
	            	grid[j][i+2]=1;grid[j+1][i+2]=1;grid[j+2][i+2]=1;
	                break;  
	            case 6: 
	            	grid[j][i]  =1;grid[j+1][i]  =1;grid[j+2][i]  =1;
	            	grid[j][i+1]=1;grid[j+1][i+1]=0;grid[j+2][i+1]=0;
	            	grid[j][i+2]=1;grid[j+1][i+2]=0;grid[j+2][i+2]=1;
	                break;  
	            case 7: 
	            	grid[j][i]  =1;grid[j+1][i]  =0;grid[j+2][i]  =1;
	            	grid[j][i+1]=1;grid[j+1][i+1]=0;grid[j+2][i+1]=0;
	            	grid[j][i+2]=1;grid[j+1][i+2]=0;grid[j+2][i+2]=1;
	                break;  
	            case 8: 
	            	grid[j][i]  =1;grid[j+1][i]  =1;grid[j+2][i]  =1;
	            	grid[j][i+1]=0;grid[j+1][i+1]=0;grid[j+2][i+1]=1;
	            	grid[j][i+2]=1;grid[j+1][i+2]=1;grid[j+2][i+2]=1;
	                break;  
	            case 9: 
	            	grid[j][i]  =1;grid[j+1][i]  =0;grid[j+2][i]  =1;
	            	grid[j][i+1]=0;grid[j+1][i+1]=0;grid[j+2][i+1]=1;
	            	grid[j][i+2]=1;grid[j+1][i+2]=1;grid[j+2][i+2]=1;
	                break;  
	            case 10: 
	            	grid[j][i]  =1;grid[j+1][i]  =1;grid[j+2][i]  =1;
	            	grid[j][i+1]=0;grid[j+1][i+1]=0;grid[j+2][i+1]=1;
	            	grid[j][i+2]=1;grid[j+1][i+2]=0;grid[j+2][i+2]=1;
	                break;  
	            case 11: 
	            	grid[j][i]  =1;grid[j+1][i]  =0;grid[j+2][i]  =1;
	            	grid[j][i+1]=0;grid[j+1][i+1]=0;grid[j+2][i+1]=1;
	            	grid[j][i+2]=1;grid[j+1][i+2]=0;grid[j+2][i+2]=1;
	                break;  
	            case 12: 
	            	grid[j][i]  =1;grid[j+1][i]  =1;grid[j+2][i]  =1;
	            	grid[j][i+1]=0;grid[j+1][i+1]=0;grid[j+2][i+1]=0;
	            	grid[j][i+2]=1;grid[j+1][i+2]=1;grid[j+2][i+2]=1;
	                break;  
	            case 13: 
	            	grid[j][i]  =1;grid[j+1][i]  =0;grid[j+2][i]  =1;
	            	grid[j][i+1]=0;grid[j+1][i+1]=0;grid[j+2][i+1]=0;
	            	grid[j][i+2]=1;grid[j+1][i+2]=1;grid[j+2][i+2]=1;
	                break;  
	            case 14:
	            	grid[j][i]  =1;grid[j+1][i]  =1;grid[j+2][i]  =1;
	            	grid[j][i+1]=0;grid[j+1][i+1]=0;grid[j+2][i+1]=0;
	            	grid[j][i+2]=1;grid[j+1][i+2]=0;grid[j+2][i+2]=1;
	                break;  
	            case 15: 
	            	grid[j][i]  =1;grid[j+1][i]  =0;grid[j+2][i]  =1;
	            	grid[j][i+1]=0;grid[j+1][i+1]=0;grid[j+2][i+1]=0;
	            	grid[j][i+2]=1;grid[j+1][i+2]=0;grid[j+2][i+2]=1;
	                break; 
	            default: 
	                
				}
				
			}
		}
	}
	private void displayGrid() {
		for (int i = 0; i < y*3; i++) {
			for (int j = 0; j < x*3; j++) {
				if(grid[j][i]==0){
					System.out.print("O");
				}
				else{
					System.out.print("@");
				}
			}
			System.out.println("");
		}
		
	}
	public void display() {
		for (int i = 0; i < y; i++) {
			// draw the north edge
			for (int j = 0; j < x; j++) {
				System.out.print(maze[j][i]+",");
			}

			System.out.print("\n");
		}
		
		
		for (int i = 0; i < y; i++) {
			// draw the north edge
			for (int j = 0; j < x; j++) {
				System.out.print((maze[j][i] & 1) == 0 ? "+---" : "+   ");
			}
			System.out.println("+");
			// draw the west edge
			for (int j = 0; j < x; j++) {
				System.out.print((maze[j][i] & 8) == 0 ? "|   " : "    ");
			}
			System.out.println("|");
		}
		// draw the bottom line
		for (int j = 0; j < x; j++) {
			System.out.print("+---");
		}
		System.out.println("+");
	}
	private void generateMaze(int cx, int cy) {
		DIR[] dirs = DIR.values();
		Collections.shuffle(Arrays.asList(dirs));
		for (DIR dir : dirs) {
			int nx = cx + dir.dx;
			int ny = cy + dir.dy;
			if (between(nx, x) && between(ny, y)
					&& (maze[nx][ny] == 0)) {
				maze[cx][cy] |= dir.bit;
				maze[nx][ny] |= dir.opposite.bit;
				generateMaze(nx, ny);
			}
		}
	}
 
	private static boolean between(int v, int upper) {
		return (v >= 0) && (v < upper);
	}
 
	private enum DIR {
		N(1, 0, -1), S(2, 0, 1), E(4, 1, 0), W(8, -1, 0);
		private final int bit;
		private final int dx;
		private final int dy;
		private DIR opposite;
 
		// use the static initializer to resolve forward references
		static {
			N.opposite = S;
			S.opposite = N;
			E.opposite = W;
			W.opposite = E;
		}
 
		private DIR(int bit, int dx, int dy) {
			this.bit = bit;
			this.dx = dx;
			this.dy = dy;
		}
	};
 
	public static void main(String[] args) {
		int x = args.length >= 1 ? (Integer.parseInt(args[0])) : 5	;
		int y = args.length == 2 ? (Integer.parseInt(args[1])) : 5;
		MazeGenerator g = new MazeGenerator(x, y);
		g.displayGrid();
		g.display();
	}
 
}