package com.daniikwdz.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.daniikwdz.game.*;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class Chunk {
	public int Global_X = 0;
	public int Global_Y = 0;
	Block[][][] _data = new Block[16][255][16];
	TerrainGen _noiser = new TerrainGen();
	public int seaLevel = 48;
	public int beachThreshold = 1;
	Random random = new Random();
	float debug_r, debug_g, debug_b;
	
	public List<float[]> Chunk_Vertex = new ArrayList<>(); // each player has a model
 	public List<float[]> Chunk_Uvmaps = new ArrayList<>(); // an OBJ model reason why we use this
 	public List<Integer> Chunk_Blocks = new ArrayList<>(); // Texture drawing stack
 	public List<float[]> Chunk_Normals = new ArrayList<>(); // Normals for lighting
 	
 	public boolean needsUpdate = true;
	
	public Chunk (int x, int y) {
		Global_X = x;
		Global_Y = y;
        debug_r = 0.5f + random.nextFloat() * 0.5f;
        debug_g = 0.5f + random.nextFloat() * 0.5f;
        debug_b = 0.5f + random.nextFloat() * 0.5f;
	}
	
	public void chunkGenerate() {
	    double scale = 0.12;
	    double caveScale = 0.1;

	    for (int x = 0; x < 16; x++) {
	        for (int z = 0; z < 16; z++) {
	            double worldX = ((Global_X * 16) + x) * scale;
	            double worldZ = ((Global_Y * 16) + z) * scale;

	            int height = _noiser.generateTerrainHeight_Generic(worldX, worldZ);

	            // Generate terrain blocks
	            for (int y = 0; y <= height; y++) {
	                double caveDensity = PerlinNoise3D.noise(worldX, y * caveScale, worldZ);
	                if (caveDensity >= 0.4) continue; // Skip if caveDensity is too high

	                if (caveDensity > 0.11 && caveDensity < 0.3) {
	                    _data[x][y][z] = (height <= seaLevel + beachThreshold)
	                        ? new blocks.gravel(x, y, z)
	                        : new blocks.dirt(x, y, z);
	                } else {
	                    _data[x][y][z] = (y < height - 2)
	                        ? new blocks.stone(x, y, z)
	                        : (height <= seaLevel + beachThreshold)
	                            ? new blocks.sand(x, y, z)
	                            : new blocks.dirt(x, y, z);
	                }
	            }

	            // Surface layer adjustment
	            if (height > seaLevel + beachThreshold) {
	                _data[x][height][z] = new blocks.grass(x, height, z);
	            }

	            // Water filling
	            for (int y = height; y <= seaLevel; y++) {
	                if (_data[x][y][z] == null || _data[x][y][z] instanceof blocks.sand) {
	                    _data[x][y][z] = new blocks.water(x, y, z);
	                }
	            }

	            // Vegetation generation
	            if (shouldGenerateVegetation(x, height, z)) {
	                int vegetationType = random.nextInt(10);
	                switch (vegetationType) {
	                    case 0 -> _data[x][height + 1][z] = new blocks.weeds(x, height + 1, z);
	                    case 1 -> _data[x][height + 1][z] = new blocks.flower(x, height + 1, z);
	                    case 2 -> _data[x][height + 1][z] = new blocks.rose(x, height + 1, z);
	                }
	            }

	            // Structure placement
	            if (shouldPlaceStructure(x, height, z)) {
	                int structureID = random.nextInt(50);
	                if (structureID < _noiser.Loaded_Structures) {
	                    _noiser.pasteStructure(this, x, height, z, structureID);
	                }
	            }
	        }
	    }
	}

	private boolean shouldGenerateVegetation(int x, int height, int z) {
	    return height > seaLevel + beachThreshold
	        && _data[x][height][z] instanceof blocks.grass
	        && !(_data[x][height - 1][z] instanceof com.daniikwdz.game.Fluid)
	        && !(_data[x][height - 1][z] instanceof blocks.sand);
	}

	private boolean shouldPlaceStructure(int x, int height, int z) {
	    return x > 4 && x < 14 && z > 4 && z < 14
	        && _data[x][height - 1][z] != null
	        && !(_data[x][height - 1][z] instanceof com.daniikwdz.game.Fluid)
	        && !(_data[x][height - 1][z] instanceof blocks.sand);
	}


	public void makeUV(int faceType,int x,int y,int z, boolean mTex) {
		int textureID = _data[x][y][z].getTextureID();
		
		if (mTex) {
		    switch (faceType) {
		        case 0: // LEFT
		        case 1: // RIGHT
		        case 2: // BACK
		        case 3: // FRONT
		            textureID = _data[x][y][z].getTextureID_A(); 
		            break;
		        case 4: // DOWN
		            textureID = _data[x][y][z].getTextureID_B();
		            break;
		        case 5: // UP
		            textureID = _data[x][y][z].getTextureID_C();
		            break;
		        default:
		            return;
		    }
		}
		
	    float[] uv;
	    switch (faceType) {
	        case 0: // LEFT
	        case 1: // RIGHT
	        case 2: // BACK
	        case 3: // FRONT
	        	uv = new float[]{
	        		    1f, 0f,  1f, 1f,  0f, 1f,  
	        		    1f, 0f,  0f, 1f,  0f, 0f  
	        	};
	            break;
	        case 4: // DOWN
	        case 5: // UP
	            uv = new float[]{
	                0f, 0f,  1f, 0f,  1f, 1f,
	                0f, 0f,  1f, 1f,  0f, 1f
	            };
	            break;
	        default:
	            return;
	    }
	    Chunk_Uvmaps.add(uv);
	    //Chunk_Blocks.add(_data[x][y][z].getTextureID());
	    Chunk_Blocks.add(textureID);
	    
	    float[] normal;
	    switch (faceType) {
	        case 0: normal = new float[]{-1, 0, 0}; break; // LEFT
	        case 1: normal = new float[]{1, 0, 0}; break; // RIGHT
	        case 2: normal = new float[]{0, 0, -1}; break; // BACK
	        case 3: normal = new float[]{0, 0, 1}; break; // FRONT
	        case 4: normal = new float[]{0, -1, 0}; break; // DOWN
	        case 5: normal = new float[]{0, 1, 0}; break; // UP
	        default: return;
	    }
	    Chunk_Normals.add(normal);
	}


	
	// -------------------------------------------------------------------------------------------------
	// - IN THIS SEGMENT WE GENERATE THE MESH USED TO DRAW OUR CHUNK IN ORDER TO REDUCE OPERATION -
	// --------------------------------------------------------------------------------------------
	public void chunkUpdate() {
		if (!needsUpdate) return;
		
	    // Limpiar datos previos
	    Chunk_Vertex.clear();
	    Chunk_Uvmaps.clear();
	    Chunk_Blocks.clear();
	    Chunk_Normals.clear();
	    
	    for (int x = 0; x < 16; x++) {
	        for (int y = 0; y < 255; y++) {
	            for (int z = 0; z < 16; z++) {
	                Block block = _data[x][y][z];
	                if (block == null) continue;
	                
	                // CROSS RENDERING
	                if (block instanceof Cross) {
	                    addCross(x, y, z);
	                    continue;
	                }
	                
	                if (block instanceof Fluid) {
	                	// STANDARD BLOCK RENDERING
	                	if (y == 0 || _data[x][y - 1][z] == null || _data[x][y - 1][z] instanceof Cross) {addFace(4, x, y, z);}
		                if (y == 254 || _data[x][y + 1][z] == null || _data[x][y + 1][z] instanceof Cross) {addFace(5, x, y, z);}
	                	if (x==0||x==15||z==0||z==15) {continue;}
		                if (x == 0 || _data[x - 1][y][z] == null || _data[x - 1][y][z] instanceof Cross ) {addFace(0, x, y, z);}
		                if (x == 15 || _data[x + 1][y][z] == null || _data[x + 1][y][z] instanceof Cross) {addFace(1, x, y, z);}
		                if (z == 0 || _data[x][y][z - 1] == null || _data[x][y][z - 1] instanceof Cross) {addFace(2, x, y, z);}
		                if (z == 15 || _data[x][y][z + 1] == null || _data[x][y][z + 1] instanceof Cross) {addFace(3, x, y, z);}
		                continue;
	                }
	                // STANDARD BLOCK RENDERING
	                if (x == 0 || _data[x - 1][y][z] == null || _data[x - 1][y][z] instanceof Cross ||  _data[x - 1][y][z] instanceof Fluid) {addFace(0, x, y, z);}
	                if (x == 15 || _data[x + 1][y][z] == null || _data[x + 1][y][z] instanceof Cross||  _data[x + 1][y][z] instanceof Fluid) {addFace(1, x, y, z);}
	                if (z == 0 || _data[x][y][z - 1] == null || _data[x][y][z - 1] instanceof Cross||  _data[x][y][z -1] instanceof Fluid) {addFace(2, x, y, z);}
	                if (z == 15 || _data[x][y][z + 1] == null || _data[x][y][z + 1] instanceof Cross||  _data[x][y][z + 1] instanceof Fluid) {addFace(3, x, y, z);}
	                if (y == 0 || _data[x][y - 1][z] == null || _data[x][y - 1][z] instanceof Cross||  _data[x][y-1][z] instanceof Fluid) {addFace(4, x, y, z);}
	                if (y == 254 || _data[x][y + 1][z] == null || _data[x][y + 1][z] instanceof Cross||  _data[x][y+1][z] instanceof Fluid) {addFace(5, x, y, z);}
	            }
	        }
	    }
	    
	    needsUpdate = false;
	}

	private void addFace(int face, int x, int y, int z) {
	    float[] vertices; 
	    
	    switch (face) {
        	case 0: vertices = new float[]{ x, y, z + 1, x, y + 1, z + 1, x, y + 1, z, x, y, z + 1, x, y + 1, z,  x, y, z }; break;
        	case 1: vertices = new float[]{ x + 1, y, z, x + 1, y + 1, z, x + 1, y + 1, z + 1, x + 1, y, z, x + 1, y + 1, z + 1, x + 1, y, z + 1}; break;
        	case 2: vertices = new float[]{ x, y, z, x, y + 1, z, x + 1, y + 1, z, x, y, z, x + 1, y + 1, z, x + 1, y, z}; break;
        	case 3: vertices = new float[]{ x + 1, y, z + 1, x + 1, y + 1, z + 1, x, y + 1, z + 1, x + 1, y, z + 1, x, y + 1, z + 1, x, y, z + 1}; break;
        	case 4: vertices = new float[]{ x, y, z, x + 1, y, z, x + 1, y, z + 1,x, y, z, x + 1, y, z + 1, x, y, z + 1 }; break;
        	case 5: vertices = new float[]{ x, y + 1, z + 1, x + 1, y + 1, z + 1, x + 1, y + 1, z, x, y + 1, z + 1, x + 1, y + 1, z, x, y + 1, z };break;
        	default:vertices = new float[]{};break;
	    }
	    
	    Chunk_Vertex.add(vertices);
	    makeUV(face, x, y, z, _data[x][y][z].has_many_textures);
	}

	private void addCross(int x, int y, int z) {
	    float cx = x + 0.5f; 
	    float cy = y + 0.5f;
	    float cz = z + 0.5f;

	    // Panel 1 (front and back)
	    float[] panel1FrontVertices = {
	        cx - 0.5f, cy + 0.5f, cz,  // Top-left
	        cx + 0.5f, cy + 0.5f, cz,  // Top-right
	        cx + 0.5f, cy - 0.5f, cz,  // Bottom-right
	        cx - 0.5f, cy + 0.5f, cz,  // Top-left
	        cx + 0.5f, cy - 0.5f, cz,  // Bottom-right
	        cx - 0.5f, cy - 0.5f, cz   // Bottom-left
	    };

	    float[] panel1BackVertices = {
	        cx + 0.5f, cy + 0.5f, cz,  // Bottom-left
	        cx - 0.5f, cy + 0.5f, cz,   // Bottom-right
	        cx - 0.5f, cy - 0.5f, cz,   // Top-right
	        cx + 0.5f, cy + 0.5f, cz,   // Bottom-left
	        cx - 0.5f, cy - 0.5f, cz,   // Top-right
	        cx + 0.5f, cy - 0.5f, cz    // Top-left
	    };

	    // Panel 2 (front and back)
	    float[] panel2FrontVertices = {
	        cx, cy + 0.5f, cz - 0.5f,  // Top-left
	        cx, cy + 0.5f, cz + 0.5f,   // Top-right
	        cx, cy - 0.5f, cz + 0.5f,   // Bottom-right
	        cx, cy + 0.5f, cz - 0.5f,   // Top-left
	        cx, cy - 0.5f, cz + 0.5f,   // Bottom-right
	        cx, cy - 0.5f, cz - 0.5f    // Bottom-left
	    };

	    float[] panel2BackVertices = {
	        cx, cy + 0.5f, cz + 0.5f,   // Bottom-left
	        cx, cy + 0.5f, cz - 0.5f,    // Bottom-right
	        cx, cy - 0.5f, cz - 0.5f,    // Top-right
	        cx, cy + 0.5f, cz + 0.5f,    // Bottom-left
	        cx, cy - 0.5f, cz - 0.5f,    // Top-right
	        cx, cy - 0.5f, cz + 0.5f     // Top-left
	    };

	    // Add all vertices to the Chunk_Vertex list
	    Chunk_Vertex.add(panel1FrontVertices);
	    Chunk_Vertex.add(panel1BackVertices);
	    Chunk_Vertex.add(panel2FrontVertices);
	    Chunk_Vertex.add(panel2BackVertices);

	    // Generate UVs for each panel (front and back)
	    makeUV(3, x, y, z, _data[x][y][z].has_many_textures); // Panel 1 front
	    makeUV(3, x, y, z, _data[x][y][z].has_many_textures); // Panel 1 back
	    makeUV(3, x, y, z, _data[x][y][z].has_many_textures); // Panel 2 front
	    makeUV(3, x, y, z, _data[x][y][z].has_many_textures); // Panel 2 back
	}

	
	// -----------------------------------------------------------------------------------
	public void chunkDraw (GL2 gl) {
		chunkUpdate();
		gl.glPushMatrix();
		  gl.glTranslatef(Global_X*16, 0, Global_Y*16);
		  
		  for (int i = 0; i < Chunk_Vertex.size(); i++) {
			gl.glEnable(GL2.GL_ALPHA_TEST);
			gl.glAlphaFunc(GL2.GL_GREATER, 0.5f); 

			  
			gl.glEnable(GL2.GL_BLEND);
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

			  
			gl.glBindTexture(gl.GL_TEXTURE_2D, Chunk_Blocks.get(i)+1);
			  
			gl.glBegin(gl.GL_TRIANGLES);
		      float[] face = Chunk_Vertex.get(i);
		      float[] uv = Chunk_Uvmaps.get(i); 
		      float[] normal = Chunk_Normals.get(i); 

		      gl.glColor3f(1,1,1);
		      gl.glNormal3f(normal[0], normal[1], normal[2]);
		      // First triangle
		      gl.glTexCoord2f(uv[0], uv[1]); gl.glVertex3f(face[0], face[1], face[2]);
		      gl.glTexCoord2f(uv[2], uv[3]); gl.glVertex3f(face[3], face[4], face[5]);
		      gl.glTexCoord2f(uv[4], uv[5]); gl.glVertex3f(face[6], face[7], face[8]);

		      gl.glTexCoord2f(uv[6], uv[7]); gl.glVertex3f(face[9], face[10], face[11]);
		      gl.glTexCoord2f(uv[8], uv[9]); gl.glVertex3f(face[12], face[13], face[14]);
		      gl.glTexCoord2f(uv[10], uv[11]); gl.glVertex3f(face[15], face[16], face[17]);
		    gl.glEnd();
		  }
		  
		  
		gl.glPopMatrix();
	}
}
