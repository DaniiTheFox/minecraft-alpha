package blocks;

import com.daniikwdz.game.*;

public class grass extends Block {
	public static int texture_id = 0; 	// By default blocks use the texture 0
	
	public static int texture_idA = 0; 	// By default blocks use the texture 0
	public static int texture_idB = 0; 	// By default blocks use the texture 0
	public static int texture_idC = 0; 	// By default blocks use the texture 0
	
	public static int durability = 0; 	// block durability on the world
	public static String name = "Grass";
	
	public grass (int _x, int _y, int _z) {
		super(_x,_y,_z);
		this.has_many_textures = true;
	}
	
	@Override
	public int getTextureID_A () {
		return texture_idA;
	}
	
	@Override
	public int getTextureID_B () {
		return texture_idB;
	}
	
	@Override
	public int getTextureID_C () {
		return texture_idC;
	}
	
	@Override
    public int getTextureID() {
        return texture_id;
    }
	
	@Override
	public String getName() {
		return name;
	}
}
