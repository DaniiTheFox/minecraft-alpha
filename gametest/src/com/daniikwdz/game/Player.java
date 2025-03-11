package com.daniikwdz.game;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

import blocks.*;

public class Player {
    String username = "daniikwdz";
    public float x, y, z, a, b;        // Camera X, Y, Z
    public float cam_y, cam_p, cam_r;  // Camera YAW, PITCH ROLL

    public int BuildDistance = 5;

    public float velocityY = 0;
    private float GRAVITY = -19.8f; 
    private final float TERMINAL_VELOCITY = -15.0f;
    private boolean isJumping = false;
    private float jumpDuration = 0.005f;
    private float jumpTimer = 0;
    public boolean isFalling = true;

    public List<double[]> Vertex = new ArrayList<>(); // each player has a model
    public List<double[]> uvmaps = new ArrayList<>(); // an OBJ model reason why we use this
    public int trit_num = 0;                          // store how many triangles does the model has
    public int texture_val = 0;                       // store the texture ID

    public RayCast raycaster = new RayCast(x, y, z, a, b);

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

    public Player(float _x, float _y, float _z, float _a, float _b) {
        x = _x;
        y = _y;
        z = _z;
        cam_y = _a;
        cam_p = _b;
    }

    private float sinDeg(float degrees) {
        int index = ((int) degrees % TABLE_SIZE + TABLE_SIZE) % TABLE_SIZE;
        return sinTable[index];
    }

    private float cosDeg(float degrees) {
        int index = ((int) degrees % TABLE_SIZE + TABLE_SIZE) % TABLE_SIZE;
        return cosTable[index];
    }

    public void calculate_player(GLU glu) {
        cam_y = x + cosDeg(a) * 1;
        cam_p = y - (float) Math.tan(Math.toRadians(b)) * 1;
        cam_r = z - sinDeg(a) * 1;

        glu.gluLookAt(x, y, z, cam_y, cam_p, cam_r, 0, 1, 0);
        raycaster.updateRay(x, y, z, a, b);
    }

    public void move_forward(float speed) {
    	x += cosDeg(a) * speed;
        z -= sinDeg(a) * speed;
    }

    public void move_backward(float speed) {
        x -= cosDeg(a) * speed;
        z += sinDeg(a) * speed;
    }

    public void move_left(float speed) {
        x += cosDeg(a + 90) * speed;
        z -= sinDeg(a + 90) * speed;
    }

    public void move_right(float speed) {
        x += cosDeg(a - 90) * speed;
        z -= sinDeg(a - 90) * speed;
    }

    public void die() {
        System.exit(0);
    }
    
    public boolean isWatered (World _w) {
    	if (raycaster.getWorldPosition(x, y, z, _w)==3) { 
    		return true;
    	}
    	return false;
    }

    public void gravity(float deltaTime, World _w) {
        float currentGravity = isWatered(_w) ? -2.8f : -9.8f;

        velocityY += currentGravity * deltaTime;

        if (velocityY < TERMINAL_VELOCITY) {
            velocityY = TERMINAL_VELOCITY;
        }

        float newY = y + velocityY * deltaTime;
        
        if (raycaster.getWorldPosition(x, newY - 2, z, _w) == 1) {
            y = (float) (Math.floor(newY) + 1);
            velocityY = 0;
            isFalling = false;
        } else {
            y = newY;
            isFalling = true;
        }
        if (isJumping) {
            jumpTimer += deltaTime;
            if (jumpTimer >= jumpDuration) {
                isJumping = false;
            }
        }

        if (y < 1) {
            y = 150;
        }
        
        if (raycaster.getWorldPosition(x, newY - 1, z, _w) == 1) {
        	isFalling = false;
        	this.jump(deltaTime, _w);
        }
    }

    public void jump(float deltaTime, World _w) {
    	boolean iwat = isWatered(_w);
    	if (iwat) {
    		isFalling = false;
    	}
    	
        if (!isJumping && !isFalling) {
            isJumping = true;
            jumpTimer = 0;

            if (iwat) {
                velocityY = 2.0f;
            } else {
                velocityY = 6.0f;
            }
        }
    }

    public void onClick(World _w) {
        _w.setBlock(raycaster.Current_Px, raycaster.Current_Py, raycaster.Current_Pz,
                new cobblestone(raycaster.Current_Px, raycaster.Current_Py, raycaster.Current_Pz));
    }

    public void onRClick(World _w) {
        _w.killBlock(raycaster.Current_Px, raycaster.Current_Py, raycaster.Current_Pz);
    }

    public void indicatorDraw(GL2 gl) {
        raycaster.rayTracking(BuildDistance);
        gl.glPushMatrix();
        gl.glTranslatef(raycaster.Current_Px + 0.5f, raycaster.Current_Py + 0.5f, raycaster.Current_Pz + 0.5f);
        gl.glLineWidth(2.0f);
        float half = 1 / 2.0f;
        gl.glDisable(gl.GL_TEXTURE_2D);
        gl.glDisable(gl.GL_LIGHT0);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glColor3f(0, 0, 0);
        gl.glBegin(GL2.GL_LINE_LOOP);

        // Cara frontal
        gl.glVertex3f(-half, -half, half);
        gl.glVertex3f(half, -half, half);
        gl.glVertex3f(half, half, half);
        gl.glVertex3f(-half, half, half);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINE_LOOP);
        // Cara trasera
        gl.glVertex3f(-half, -half, -half);
        gl.glVertex3f(half, -half, -half);
        gl.glVertex3f(half, half, -half);
        gl.glVertex3f(-half, half, -half);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        // Conectar caras frontal y trasera
        gl.glVertex3f(-half, -half, half);
        gl.glVertex3f(-half, -half, -half);

        gl.glVertex3f(half, -half, half);
        gl.glVertex3f(half, -half, -half);

        gl.glVertex3f(half, half, half);
        gl.glVertex3f(half, half, -half);

        gl.glVertex3f(-half, half, half);
        gl.glVertex3f(-half, half, -half);
        gl.glEnd();
        gl.glEnable(gl.GL_TEXTURE_2D);
        gl.glEnable(gl.GL_LIGHT0);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glLineWidth(1.0f);
        gl.glPopMatrix();
    }
}