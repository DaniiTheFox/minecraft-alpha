package com.daniikwdz.game;

public class RayCast {
	private float x, y, z; // RAYCAST START POSITION
    private float yaw, pitch; // ANGLES FOR THE CHUNK DIRECTION
    
    public int Last_iChunk_X = 0; // LAST CHUNK DETECTED INSIDE A CHUNK
    public int Last_iChunk_Y = 0;
    
    public int Last_iMicro_X = 0;
    public int Last_iMicro_Y = 0; // LAST POSITION DETECTED INSIDE A CHUNK
    public int Last_iMicro_Z = 0;
    
    public int Current_Px = 0;
    public int Current_Py = 0; // CURRENT POSITION DETECTED INSIDE A WORLD
    public int Current_Pz = 0;
    
    public int preLast_iMicro_X = 0;
    public int preLast_iMicro_Y = 0; // THESE ARE THE PREVIOUS BLOCK USED FOR 
    public int preLast_iMicro_Z = 0; //   BUILDING WITHOUT THECHAOS 
    
    public Block Last_iBlock;
    
    private static final int TABLE_SIZE = 360;
    private static final float[] sinTable = new float[TABLE_SIZE];
    private static final float[] cosTable = new float[TABLE_SIZE];

    static {
        for (int i = 0; i < TABLE_SIZE; i++) {
            double radians = Math.toRadians(i);
            sinTable[i] = (float) Math.sin(radians);
            cosTable[i] = (float) Math.cos(radians);
        }
    }
    
    private float sinDeg(float degrees) {
        int index = ((int) degrees % TABLE_SIZE + TABLE_SIZE) % TABLE_SIZE; 
        return sinTable[index];
    }

    private float cosDeg(float degrees) {
        int index = ((int) degrees % TABLE_SIZE + TABLE_SIZE) % TABLE_SIZE;
        return cosTable[index];
    }
    
    public RayCast(float x, float y, float z, float yaw, float pitch) {
        this.x = x;			// 
        this.y = y;			//   | THE PARAMETERS PASSED TO THE RAYCASTER
        this.z = z;			// --| ARE THE DIRECTIONS, AND POSITIONS FOR THE RAYCAST
        this.yaw = yaw;     //   | IT IS LITERALLY A RAY THAT COMES FROM AN ANGLE AND POSITION
        this.pitch = pitch; //  
    }
    
    public void updateRay(float x, float y, float z, float yaw, float pitch) {
        this.x = x;			// 
        this.y = y;			//   | THE PARAMETERS PASSED TO THE RAYCASTER
        this.z = z;			// --| ARE THE DIRECTIONS, AND POSITIONS FOR THE RAYCAST
        this.yaw = yaw;     //   | IT IS LITERALLY A RAY THAT COMES FROM AN ANGLE AND POSITION
        this.pitch = pitch; //  
    }
    
    public int getWorldPosition (float realX, float realY, float realZ, World _w) {
    	
    	int chunkX = (int) (realX / 16);
    	int chunkY = (int) (realZ / 16);

    	int blockX = (int) (realX % 16);
    	int blockZ = (int) (realZ % 16);

    	int blockY = (int) realY; 

        
        String key = _w.toKey(chunkX, chunkY);
        Chunk chunk = _w._world.get(key);
        
        if (blockX < 0 || blockX > 16 || blockZ < 0 || blockZ > 16 ) {return 0;} 
        
        if (chunk != null) {
        	if (_w._world.get(_w.toKey(chunkX, chunkY))._data[blockX][blockY][blockZ] != null && 
                	!(_w._world.get(_w.toKey(chunkX, chunkY))._data[blockX][blockY][blockZ] instanceof Cross) && 
                	!(_w._world.get(_w.toKey(chunkX, chunkY))._data[blockX][blockY][blockZ] instanceof Fluid)
            ) {
                	// --------------------------------------------------------------------
                	Last_iChunk_X = chunkX; // IN THIS PART WE DETECTH IF THE BLOCK IS NOT NULL
                	Last_iChunk_Y = chunkY; // IT MEANS THERE IS SOMETHING STORED ON THE ARRAY THAT WE SPOTTED
                		
                	Last_iMicro_X = blockX;	  // 
                	Last_iMicro_Y = blockY;   // SO IN THIS CASE WE WILL STORE THE LAST INTERSECTION IN ORDER TO ACCESS
                	Last_iMicro_Z = blockZ;   // THE INFORMATION EASILY IN CASE IT IS REQUIRED BY ONE OF THE INSTANCES OF RAYCAST
                	// AND FINALLY WE STORE THE BLOCK THAT WE INTERSECTED (IF WE WANT TO KNOW LAST DETECTED BLOCK)
                	// ---------------------------------------------------------------------
                	Last_iBlock = _w._world.get(_w.toKey(chunkX, chunkY))._data[blockX][blockY][blockZ];
                	return 1; // THEN RETURN TRUE BECAUSE WE DETECTED A BLOCK
             } else if (_w._world.get(_w.toKey(chunkX, chunkY))._data[blockX][blockY][blockZ] != null && 
                 	(_w._world.get(_w.toKey(chunkX, chunkY))._data[blockX][blockY][blockZ] instanceof Fluid)) {
            	 	return 3;
             }else {
               		return 0; // FALSE BECAUSE WE DID NOT SPOTTED A BLOCK
             }
        }
        return 0;
    }
    // AND THIS IS THE RAY THAT WE ARE GOING TO TEST
    public boolean isIntersect (int distance, World _w) {
    	// FOR EACH SEGMENT IN THE RAY THAT WE ARE TESTING
    	// ----------------------------------------------------------
    	for (int i = 0; i < distance; i++) { 
    		float Ray_xPos = x + cosDeg(yaw) * i;
    		float Ray_yPos = y - (float) Math.tan(Math.toRadians(pitch)) * i;
    		float Ray_zPos = z - sinDeg(yaw) * i;
    		// IF THE RAY HAS SPOTTED SOMETHING THEN RETURN TRUE
    		// --------------------------------------------------------------------
    		if (getWorldPosition(Ray_xPos, Ray_yPos, Ray_zPos, _w)==1) {
    			
        		preLast_iMicro_X = (int) (x + cosDeg(yaw) * (i-1));
        		preLast_iMicro_Y = (int) (y - (float) Math.tan(Math.toRadians(pitch)) * (i-1));
        		preLast_iMicro_Z = (int) (z - sinDeg(yaw) * (i-1));
    			i = distance +1;
    			return true;
    		}
    	}
    	return false;
    }
    
    public void rayTracking(int distance) {
		Current_Px = (int) (x + cosDeg(yaw) * (distance-2));
		Current_Py = (int) (y - (float) Math.tan(Math.toRadians(pitch)) * (distance-2));
		Current_Pz = (int) (z - sinDeg(yaw) * (distance-2));
    }
}
