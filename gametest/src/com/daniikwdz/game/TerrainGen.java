package com.daniikwdz.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class TerrainGen {
	public int TG_MOUNTAIN_HG = 60;
	
	public int TG_BIOME_DESERT = 0;
	public int TG_BIOME_VALLEY = 1;
	public int TG_BIOME_MOUNTN = 2;
	
	private static final String ASSETS_PATH = "structures/";
    static int Loaded_Structures = 0;

    public static List<List<Object[]>> loadStructures() {
        List<List<Object[]>> structures = new ArrayList<>();
        System.out.println("Loading all BMC Structures....");

        try {
            ClassLoader classLoader = TerrainGen.class.getClassLoader();
            URL resource = classLoader.getResource(ASSETS_PATH);

            if (resource == null) {
                System.err.println("JAR File is incomplete. Missing: " + ASSETS_PATH);
                return structures;
            }

            // Caso 1: Ejecutando desde una carpeta normal
            if (resource.getProtocol().equals("file")) {
                File folder = new File(resource.toURI());
                if (folder.exists() && folder.isDirectory()) {
                    for (File file : Objects.requireNonNull(folder.listFiles())) {
                        if (file.getName().endsWith(".bmc")) {
                            structures.add(loadStructure(file.getName()));
                        }
                    }
                }
            } 
            // Caso 2: Ejecutando desde un JAR (No podemos usar File)
            else if (resource.getProtocol().equals("jar")) {
                String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!")); 
                try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().startsWith(ASSETS_PATH) && entry.getName().endsWith(".bmc")) {
                            String fileName = entry.getName().substring(ASSETS_PATH.length());
                            structures.add(loadStructure(fileName));
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return structures;
    }

    private static List<Object[]> loadStructure(String filename) {
        List<Object[]> structure = new ArrayList<>();

        try (InputStream inputStream = TerrainGen.class.getClassLoader().getResourceAsStream(ASSETS_PATH + filename);
             BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 4) {
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    int z = Integer.parseInt(parts[2]);
                    String blockName = parts[3];

                    structure.add(new Object[]{x, y, z, blockName});
                }
            }

            Loaded_Structures++;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return structure;
    }

    static List<List<Object[]>> World_Structures = loadStructures();
    
	// Now this is cursed shit get the name of the block class and then sample it
	// inside the world, this is done like this in order to avoid all the process of parsing
	// block ID's from the JSON files
	public static Block createBlockFromName(String blockName, int x, int y, int z) {
	    try {
	        Class<?> clazz = Class.forName("blocks." + blockName);
	        if (Block.class.isAssignableFrom(clazz)) {
	            return (Block) clazz.getConstructor(int.class, int.class, int.class).newInstance(x, y, z);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	public TerrainGen () {}
	
	public void setTerrainGen(int _mh, int _cl, int _dmin, int _dmax) {
		TG_MOUNTAIN_HG = 10;
	}
	
	public int generateTerrainHeight_Generic (double worldX, double worldZ) {
	    double baseHeight = PerlinNoise3D.noise(worldX * 0.11, 0, worldZ * 0.11) * TG_MOUNTAIN_HG + 63;

	    double detail1 = PerlinNoise3D.noise(worldX * 0.11, 100, worldZ * 0.11) * 10;
	    double detail2 = PerlinNoise3D.noise(worldX * 0.12, 200, worldZ * 0.12) * 5;

	    double height = baseHeight + detail1 + detail2;

	    return Math.max(1, Math.min(255, (int) height));
	}
	
	public static void pasteStructure(Chunk _world, int baseX, int baseY, int baseZ, int structureID) {
	    List<Object[]> structure = World_Structures.get(structureID);
	    
	    if (structure == null) {
	        System.err.println("Structure ID " + structureID + " not found!");
	        return;
	    }

	    for (Object[] data : structure) {
	        int x = (int) data[0];
	        int y = (int) data[1];
	        int z = (int) data[2];
	        String blockName = (String) data[3];

	        Block block = createBlockFromName(blockName, baseX + x, baseY + y, baseZ + z);
	        
	        if (block != null
	        		&& (baseX + x) > 0 && (baseX + x) < 16  //
	        		&& (baseY + y) > 0 && (baseY + y) < 255 // - VERIFICATION THAT STRUCTURE IS INSIDE THE CHUNK
	        		&& (baseZ + z) > 0 && (baseZ + z) < 16  //
	        ) {
	           _world._data[baseX + x][baseY + y][baseZ + z] = block;
	        }
	    }
	}

	
}
