package com.daniikwdz.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.jogamp.opengl.GL2;

public class World {
    public static int Global_CX = 0;
    public static int Global_CY = 0;

    public static int w_x = 5;
    public static int w_y = 5;
    public static int draw_dist = 25;

    public static final Map<String, Chunk> _world = new HashMap<>();
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public static String toKey(int x, int y) {
        return x + "," + y;
    }

    public World(int startX, int startY) {
        System.out.println("Generating level...");
        for (int x = startX; x < startX + w_x; x++) {
            for (int y = startY; y < startY + w_y; y++) {
                String key = toKey(x, y);
                Chunk chunk = new Chunk(x, y);
                chunk.chunkGenerate();
                chunk.chunkUpdate();
                _world.put(key, chunk);
            }
        }
        System.out.println("World complete!");
    }

    public static void worldDraw(int realX, int realY, GL2 gl) {
       for (Chunk chunk : _world.values()) {
            chunk.chunkDraw(gl);
        }
    }

    private void modifyBlock(float realX, float realY, float realZ, Block block) {
        int chunkX = (int) (realX / 16);
        int chunkY = (int) (realZ / 16);
        
        float chunkLocalX = (realX / 16.0f) - chunkX;
        float chunkLocalZ = (realZ / 16.0f) - chunkY;
        
        int blockX = (int) (chunkLocalX * 16);
        int blockZ = (int) (chunkLocalZ * 16);
        
        int blockY = (int) realY;
        
        String key = toKey(chunkX, chunkY);
        Chunk chunk = _world.get(key);
        if (chunk != null) {
            chunk._data[blockX][blockY][blockZ] = block;
            chunk.chunkUpdate();
            chunk.needsUpdate = true;
        }
    }

    public void setBlock(float realX, float realY, float realZ, Block block) {
        modifyBlock(realX, realY, realZ, block);
    }

    public void killBlock(float realX, float realY, float realZ) {
        modifyBlock(realX, realY, realZ, null);
    }

    public void WorldUpdate(int realX, int realZ) {
        Set<String> activeChunks = new HashSet<>();
        int half_wx = w_x / 2;
        int half_wy = w_y / 2;

        for (int x = (realX / 16) - half_wx; x <= (realX / 16) + half_wx; x++) {
            for (int y = (realZ / 16) - half_wy; y <= (realZ / 16) + half_wy; y++) {
                String key = toKey(x, y);
                activeChunks.add(key);
                if (!_world.containsKey(key)) {
                    generateChunkAsync(x, y);
                }
            }
        }

        _world.entrySet().removeIf(entry -> !activeChunks.contains(entry.getKey()));
    }

    private void generateChunkAsync(int chunkX, int chunkY) {
        String key = toKey(chunkX, chunkY);
        Chunk chunk = new Chunk(chunkX, chunkY);
        chunk.chunkGenerate();
        chunk.chunkUpdate();
        chunk.needsUpdate = true;
        _world.put(key, chunk);
    }
}
