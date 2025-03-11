package com.daniikwdz.game;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.*;

public class Main implements GLEventListener, KeyListener, MouseMotionListener, MouseListener, MouseWheelListener {
    public static GL2 gl;
    public static GLU glu;
    public static JFrame frame;
    public Textures texture_mgr;                  // Store all ingame textures
    public static Block[] Game_Blocks;            // Store all ingame blocks
    public static Player user = new Player(4000, 80, 4000, 0, 0);
    public static World overworld;
    private long lastTime = System.nanoTime();
    private float deltaTime = 0;
    private float timeOfDay = 0.25f; 
    private final float dayLength = 10 * 60.0f; 
    private final float nightLength = 10 * 60.0f; 

    private boolean wPressed = false;
    private boolean sPressed = false;
    private boolean aPressed = false;
    private boolean dPressed = false;
    private boolean spacePressed = false;

    public static void main(String[] args) {
        frame = new JFrame("Cave game multiplayer");
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        GLCanvas canvas = new GLCanvas(capabilities);

        Main renderer = new Main();
        canvas.addGLEventListener(renderer);
        canvas.addKeyListener(renderer);
        canvas.addMouseMotionListener(renderer);
        canvas.addMouseWheelListener(renderer);
        canvas.addMouseListener(renderer);

        canvas.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB),
                new Point(0, 0),
                "blank_cursor"
        ));

        frame.add(canvas);
        frame.setSize(640, 480);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        FPSAnimator animator = new FPSAnimator(canvas, 60);
        animator.start();
    }

    @Override
    public void display(GLAutoDrawable arg0) {
    	processInput(deltaTime);
        gl = arg0.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        // Update player and world
        user.calculate_player(glu);
        user.indicatorDraw(gl);
        user.gravity(deltaTime, overworld);

        gl.glPushMatrix();
        overworld.WorldUpdate((int) user.x, (int) user.z);
        World.worldDraw((int) user.x, (int) user.z, gl);
        gl.glPopMatrix();

        // Update delta time
        long now = System.nanoTime();
        deltaTime = (now - lastTime) / 1_000_000_000.0f;
        lastTime = now;
        
        float cycleLength = dayLength + nightLength;  
        timeOfDay += deltaTime / cycleLength; 

        if (timeOfDay > 1.0f) {
            timeOfDay -= 1.0f; 
        }
        float timeIn24H = timeOfDay * 24.0f;

        setupLighting(gl, timeIn24H);
    }

    @Override
    public void dispose(GLAutoDrawable arg0) {
        // Cleanup resources if needed
    }
    
    public void setupLighting(GL2 gl, float timeOfDay) {
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_LIGHT1);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);
        gl.glShadeModel(GL2.GL_SMOOTH);

        float sunAngle = (timeOfDay / 24.0f) * 360.0f;
        float sunX = (float) Math.cos(Math.toRadians(sunAngle));
        float sunY = (float) Math.sin(Math.toRadians(sunAngle));

        float brightness = Math.max(0.1f, sunY);

        float skyR, skyG, skyB;
        float lightR, lightG, lightB;
        float fogR, fogG, fogB;

        float[] daySky = {0.75f, 0.85f, 0.95f};
        float[] dayLight = {1.0f, 1.0f, 1.0f};
        float[] dayFog = {1.0f, 1.0f, 1.0f};

        float[] sunsetSky = {0.9f, 0.5f, 0.4f};
        float[] sunsetLight = {1.0f, 0.8f, 0.6f};
        float[] sunsetFog = {0.8f, 0.6f, 0.5f};

        float[] nightSky = {0.05f, 0.02f, 0.1f};
        float[] nightLight = {0.1f, 0.2f, 0.5f};
        float[] nightFog = {0.1f, 0.1f, 0.2f};

        float[] dawnSky = {0.05f, 0.07f, 0.15f};
        float[] dawnLight = {0.4f, 0.5f, 0.8f};
        float[] dawnFog = {0.2f, 0.2f, 0.3f};

        if (brightness > 0.8f) {  
            skyR = daySky[0]; skyG = daySky[1]; skyB = daySky[2];
            lightR = dayLight[0]; lightG = dayLight[1]; lightB = dayLight[2];
            fogR = dayFog[0]; fogG = dayFog[1]; fogB = dayFog[2];
        } else if (brightness > 0.4f) {  
            float factor = (brightness - 0.4f) / 0.4f;
            skyR = lerp(sunsetSky[0], daySky[0], factor);
            skyG = lerp(sunsetSky[1], daySky[1], factor);
            skyB = lerp(sunsetSky[2], daySky[2], factor);
            lightR = lerp(sunsetLight[0], dayLight[0], factor);
            lightG = lerp(sunsetLight[1], dayLight[1], factor);
            lightB = lerp(sunsetLight[2], dayLight[2], factor);
            fogR = lerp(sunsetFog[0], dayFog[0], factor);
            fogG = lerp(sunsetFog[1], dayFog[1], factor);
            fogB = lerp(sunsetFog[2], dayFog[2], factor);
        } else if (brightness > 0.2f) {  
            float factor = (brightness - 0.2f) / 0.2f;
            skyR = lerp(nightSky[0], sunsetSky[0], factor);
            skyG = lerp(nightSky[1], sunsetSky[1], factor);
            skyB = lerp(nightSky[2], sunsetSky[2], factor);
            lightR = lerp(nightLight[0], sunsetLight[0], factor);
            lightG = lerp(nightLight[1], sunsetLight[1], factor);
            lightB = lerp(nightLight[2], sunsetLight[2], factor);
            fogR = lerp(nightFog[0], sunsetFog[0], factor);
            fogG = lerp(nightFog[1], sunsetFog[1], factor);
            fogB = lerp(nightFog[2], sunsetFog[2], factor);
        } else {  
            float factor = brightness / 0.2f;
            skyR = lerp(dawnSky[0], nightSky[0], factor);
            skyG = lerp(dawnSky[1], nightSky[1], factor);
            skyB = lerp(dawnSky[2], nightSky[2], factor);
            lightR = lerp(dawnLight[0], nightLight[0], factor);
            lightG = lerp(dawnLight[1], nightLight[1], factor);
            lightB = lerp(dawnLight[2], nightLight[2], factor);
            fogR = lerp(dawnFog[0], nightFog[0], factor);
            fogG = lerp(dawnFog[1], nightFog[1], factor);
            fogB = lerp(dawnFog[2], nightFog[2], factor);
        }

        
        gl.glClearColor(skyR, skyG, skyB, 1.0f);

        float[] light_position = {sunX, sunY, 1.0f, 0.0f};
        float[] light_diffuse = {lightR, lightG, lightB, 1.0f};
        float[] light_specular = {lightR, lightG, lightB, 1.0f};
        float[] light_ambient = {lightR * 0.6f, lightG * 0.6f, lightB * 0.6f, 1.0f};

        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light_position, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, light_diffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, light_specular, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, light_ambient, 0);

        float ao_intensity = Math.max(0.1f, 0.4f - brightness);
        float[] ao_light_position = {-sunX, -0.2f, -1.0f, 0.0f};
        float[] ao_light_diffuse = {ao_intensity, ao_intensity, ao_intensity, 1.0f};
        float[] ao_light_ambient = {ao_intensity * 0.5f, ao_intensity * 0.5f, ao_intensity * 0.5f, 1.0f};

        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, ao_light_position, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, ao_light_diffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, ao_light_ambient, 0);

        if (user.isWatered(overworld)) {
        	ambient_fog (gl, 0.0f, 0.0f, 1.0f , 0.2f);
        	gl.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
        }else {
        	ambient_fog (gl, fogR, fogG, fogB, 0.03f);
        }
    }
    
    public void ambient_fog (GL2 gl, float _fogR, float _fogG,float _fogB, float _dens) {
    	gl.glEnable(GL2.GL_FOG);
        gl.glFogi(GL2.GL_FOG_MODE, GL2.GL_EXP2);
        float[] fogColor = {_fogR, _fogG, _fogB, 1.0f};
        gl.glFogfv(GL2.GL_FOG_COLOR, fogColor, 0);
        gl.glFogf(GL2.GL_FOG_DENSITY, _dens);
        gl.glFogf(GL2.GL_FOG_START, 15.0f);
        gl.glFogf(GL2.GL_FOG_END, 250.0f);
    }

    
    private float lerp(float start, float end, float factor) {
        return start + (end - start) * factor;
    }
    @Override
    public void init(GLAutoDrawable arg0) {
        gl = arg0.getGL().getGL2();
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_TEXTURE_2D);
        
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glCullFace(GL2.GL_BACK);
        
        setupLighting(gl, 12.0f);
        
        // Load textures and initialize world
        texture_mgr = new Textures();
        create_textures();
        overworld = new World((int) user.x, (int) user.z);
    }

    public void create_textures () {
		blocks.cobblestone.texture_id = texture_mgr.PushTex(gl, "cobblestone.png");
		blocks.grass.texture_id       = texture_mgr.PushTex(gl, "grass_block.png");
		blocks.leaves.texture_id      = texture_mgr.PushTex(gl, "leaves.png");
		blocks.stone.texture_id       = texture_mgr.PushTex(gl, "stone.png");
		blocks.sand.texture_id        = texture_mgr.PushTex(gl, "sand.png");
		blocks.dirt.texture_id        = texture_mgr.PushTex(gl, "dirt.png");
		blocks.wood.texture_id        = texture_mgr.PushTex(gl, "wood.png");
		blocks.weeds.texture_id 	  = texture_mgr.PushTex(gl, "weeds.png");
		blocks.flower.texture_id 	  = texture_mgr.PushTex(gl, "flower.png");
		blocks.rose.texture_id 	      = texture_mgr.PushTex(gl, "rose.png");
		blocks.bricks.texture_id 	  = texture_mgr.PushTex(gl, "bricks.png");
		blocks.gravel.texture_id 	  = texture_mgr.PushTex(gl, "gravel.png");
		
		// -- SPECIAL 3 TEXTURE BLOCKS ARE HANDLED HERE --
		blocks.grass.texture_idA       = texture_mgr.PushTex(gl, "grass_side.png");
		blocks.grass.texture_idB       = texture_mgr.PushTex(gl, "dirt.png");
		blocks.grass.texture_idC       = texture_mgr.PushTex(gl, "grass_block.png");
		// --- CUSTOM FLUID TEXTURES GO IN THIS PART  ---
		blocks.water.texture_id 	   = texture_mgr.PushTex(gl, "water.png");
		blocks.lava.texture_id 	       = texture_mgr.PushTex(gl, "lava.png");
		
		texture_mgr.debug_list();
	}
    
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu = new GLU();
        glu.gluPerspective(45.0, (double) width / height, 0.1, 3000.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                wPressed = true;
                break;
            case KeyEvent.VK_S:
                sPressed = true;
                break;
            case KeyEvent.VK_A:
                aPressed = true;
                break;
            case KeyEvent.VK_D:
                dPressed = true;
                break;
            case KeyEvent.VK_SPACE:
                spacePressed = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                wPressed = false;
                break;
            case KeyEvent.VK_S:
                sPressed = false;
                break;
            case KeyEvent.VK_A:
                aPressed = false;
                break;
            case KeyEvent.VK_D:
                dPressed = false;
                break;
            case KeyEvent.VK_SPACE:
                spacePressed = false;
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
       
    }

    public void processInput(float deltaTime) {
        if (wPressed) {
            user.move_forward(0.215f);
        }
        if (sPressed) {
            user.move_backward(0.213f);
        }
        if (aPressed) {
            user.move_left(0.214f);
        }
        if (dPressed) {
            user.move_right(0.214f);
        }
        if (spacePressed) {
            user.jump(deltaTime, overworld);
        }
    }

    @Override
	public void mousePressed(MouseEvent arg0) {
		if (SwingUtilities.isLeftMouseButton(arg0)) {
			user.onRClick(overworld);
        } else if (SwingUtilities.isRightMouseButton(arg0)) {
        	user.onClick(overworld);
        }
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	    int centerX = frame.getX() + frame.getWidth() / 2;
	    int centerY = frame.getY() + frame.getHeight() / 2;
	    
	    
	    Point p = MouseInfo.getPointerInfo().getLocation();
	    int dx = p.x - centerX;
	    int dy = p.y - centerY;
	    
	    
	    float sensitivity = 0.05f;
	    user.a -= dx * sensitivity;
	    user.b += dy * sensitivity; 
	    
	    
	    if (user.b > 90) user.b = 90;
	    if (user.b < -90) user.b = -90;
	    
	    
	    try {
	        Robot robot = new Robot();
	        robot.mouseMove(centerX, centerY);
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub
		int notches = arg0.getWheelRotation();

        if (notches < 0) {
           user.BuildDistance++;
        } else {
           user.BuildDistance--;
        }
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}