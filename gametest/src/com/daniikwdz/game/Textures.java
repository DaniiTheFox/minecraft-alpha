package com.daniikwdz.game;

import java.io.*;
import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class Textures {
	public int Texture_count;	// THIS IS TO PROVIDE ID
	public static ArrayList<String>  textures_names = new ArrayList<>();
	public static ArrayList<Texture> textures       = new ArrayList<>(); // THIS IS TO STORE THE TEXTURE
	
	public Textures () {
		System.out.println("<System> Texture System Has started!!!");
		Texture_count = 0; // Inicializa Texture_count a 0
	}

	public int PushTex(GL2 gl, String filename) {
	    try {
	        // Load from resources inside the JAR
	        InputStream textureStream = getClass().getClassLoader().getResourceAsStream("assets/" + filename);
	        if (textureStream == null) {
	            throw new FileNotFoundException("Texture not found: assets/" + filename);
	        }

	        Texture tmp_tex = TextureIO.newTexture(textureStream, true, TextureIO.PNG); // Adjust format if needed
	        
	        gl.glBindTexture(GL2.GL_TEXTURE_2D, tmp_tex.getTextureObject(gl));
	        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
	        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);

	        textures_names.add(filename);
	        textures.add(tmp_tex);
	        System.out.println("<System> Texture loaded: " + filename + " In pos: " + Texture_count);
	        return Texture_count++;
	    } catch (IOException | IllegalStateException e) {
	        System.out.println("<Texture error> " + e.getMessage());
	        e.printStackTrace();
	        return -1;
	    }
	}
	
	public void debug_list () {
		for (int i = 0; i < textures.size(); i++) {
	        Texture tex = textures.get(i);
	        String texName = textures_names.get(i);  // Nombre del archivo de la textura
	        if (tex != null) {
	            System.out.println("Texture " + texName + " (ID: " + tex.getTextureObject() + ") loaded successfully at index " + i);
	        } else {
	            System.out.println("Texture " + texName + " failed to load at index " + i);
	        }
	    }
	}

}
