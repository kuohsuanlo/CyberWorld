# CyberWorld  (English version)

CyberWorld is a minecraft spigot plugin project. The goal is 
  - Procedurally generating complicated cyberpunk city in minecraft 
  - Automatically merging buildings into mega-structure
  - Generating the building with sub-structure such as signs, illegal building, etc.

ScreenShot : 

bird view rendering (without shader)
![ScreenShot](http://i.imgur.com/88ZMhRM.png)
In game rendering (with shader)
![ScreenShot](http://i.imgur.com/PtRndwI.png)
![ScreenShot](http://i.imgur.com/JBseZLc.png)
![ScreenShot](http://i.imgur.com/wz9SNm7.png)

For more images : 
http://imgur.com/a/xoJft

A short 1minute demo video : 
https://www.youtube.com/watch?v=NIHs_7g5-e4

###############################################################################

# Dependency : 
1. Multiverse 
2. WorldEdit

# How to use ?
1. Run the plugin once. Let it generate ./plugins/Cyberworld/schematics/ ... etc
2. Stop the server.
3. Put the schematics files from world edit. Otherwise, the map would be empty because of no schematics read.
  You could get my version from https://github.com/kuohsuanlo/CyberWorld/blob/master/schematics/schematics.zip
  Just decompress and paste them.
4. Run the server again.
5. Generate the world with Multiverse commands and specify its world generator with *-g* : 
   /mv create YOUR_WORLD_NAME normal -g CyberWorld

###############################################################################

# Features
1. Up to three types of bulding size, creating a crowded city view.
2. Building's block will be automatically changed into other building material.
3. Building's width and height would be automatically stretched to increase the variety.
4. Automatically find wall or building that are suitable for advertisement sign(TV Wall) .
5. Underground buildings and sewage pipes entagled to meet the cyberpunk's choatic settings.

###############################################################################

# Configuration
BIOME_TYPES: 3
  - Number of "City biomes", which is different from biome of minecraft.
  - Total number of city biome would be 2^K. K here is 3 by default.

BIOME_NUMBER_WITH_BUILDING: 4
  - Say your BIOME_TYPES is 3. There would be 8 biome (0~7), this is the number of biome that contains buildings, the rest of the biome would be nature biome. So biome *(0~4)* would be city, *(5~7)* would be nature.

BIOME_OCTAVE: 5
   - The city biomes' size, the larger it is , the larger each city biome would be.

##################

SIGN_WALL_BLOCK_RATIO: 0.4
  - ![ScreenShot](http://i.imgur.com/8Jkr4HA.png)
  - The walls automatically get hanged with sign should should have over *40%* blocks that is not air.

SIGN_WALL_MINIMAL_WIDTH: 12
  - The minimal size of the sign

SIGN_WALL_COVERAGE_RATIO_MIN: 0.2
SIGN_WALL_COVERAGE_RATIO_MAX: 0.8
  - ![ScreenShot](http://i.imgur.com/DB5187B.png)

##################

HEIGHT_RAND_ODDS: 0.5
HEIGHT_RAND_RATIO: 1.5

##################
MAP_W: 1000
MAP_H: 1000
TERRAIN_OCTAVE: 8
TERRAIN_HEIGHT: 100
##################

SEA_LEVEL: 45
L1_HEIGHT: 20
L2_HEIGHT: 40
L3_HEIGHT: 80
GROUND_LEVEL: 50
  - The ground level of city biome.
  - ![ScreenShot](http://i.imgur.com/ALiR2YC.png)

##################
SIZE_DECORATION: 1
SIZE_SMALL: 2
SIZE_MEDIUM: 4
SIZE_LARGE: 15
SIZE_BLOCK: 20
  - The unit of size here is "chunk width", meaning that if your schematic is 30x30, it would be classified as 2x2 --mall
  - There are three types of building, from small to large. 
  - The plugin paste from large - medium - small.
