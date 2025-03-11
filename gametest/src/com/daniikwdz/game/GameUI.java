package com.daniikwdz.game;

import com.jogamp.opengl.GL2;

public class GameUI {
	public void set2DProjection(GL2 gl, int screenWidth, int screenHeight) {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glOrtho(0, screenWidth, 0, screenHeight, -1, 1);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();
    }

	public void restore3DProjection(GL2 gl) {
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPopMatrix();

        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }
	
	

}
