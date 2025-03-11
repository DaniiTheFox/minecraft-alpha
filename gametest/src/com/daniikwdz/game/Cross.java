package com.daniikwdz.game;

import com.jogamp.opengl.GL2;

public abstract class Cross extends Block{
	public int x,y,z;		  	// Block position required by the game
	public String name = "Generic block"; // block name on inventory
	
	public Cross (int _x, int _y, int _z) {
		// The basic block setup
		super(_x,_y,_z);
	}
	
	public void onActivate () {
		// By default the block does nothing
	}
	
	public void onDeactivate () {
		// by default the block deos nothing
	}
	
	public abstract String getName();
	
	public abstract int getTextureID(); // Cada bloque lo define
}
