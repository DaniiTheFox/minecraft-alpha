package blocks;

import com.daniikwdz.game.*;

public class water extends Fluid {
	public static int texture_id = 0; 	// By default blocks use the texture 0
	public static int durability = 0; 	// block durability on the world
	public static String name = "Water";
	
	public water (int _x, int _y, int _z) {
		super(_x,_y,_z);
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
