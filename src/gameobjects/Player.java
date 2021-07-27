/*
 * DANIEL XIAO ISU COMPUTER SCIENCE 2021
 * This is part of my ISU game
 * 
 * The purpose of this class is to define the behavior for the player.
 */

package gameobjects;

import com.jme3.anim.AnimClip;
import com.jme3.anim.AnimComposer;
import com.jme3.anim.SkinningControl;
import com.jme3.anim.tween.action.Action;
import com.jme3.bounding.BoundingBox;
import com.jme3.collision.Collidable;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import data.ModelLoader;

public class Player extends GameObject implements Collidable{
    
    private AnimComposer animComposer;
    private static Action currentAction;
    
    private final float PLATFORM_W;
    
    private float[] bounds;
    
    private boolean isInvincible;
    private boolean isDead;
    
    private boolean moveRight;
    private boolean moveLeft;
    
	private static final int DEATH_THRESHOLD = 200;
    
    private boolean backMove;
    private float gameDZ;
    private float z_offset;
    private float x_pos;
    private float x_vel; //game units per second
    private float dx;
    private float x_accel; //game units per second squared
    
    private boolean isInRevive;
    
    public Player(float PLATFORM_W){
    	super();
        super.init();
        
        this.PLATFORM_W = PLATFORM_W * 1.85f;
        
        
    }
    
    /*
	 * params: none.
	 * return: does something; void
	 * purpose: reset player data either on death or on game start or on quit.
	 */
    public void setDefaults() {
    	isDead = false;
        x_pos = 0;
        gameDZ = 0;
        x_vel = 0;
        dx = 0;
        x_accel = 6.5f;
        z_offset = 0;
        backMove = false;
        
        moveLeft = false;
        moveRight = false;
    	
    	setLocalTranslation(new Vector3f(0,0,0));
    }
    
    /*
   	 * params: move player right or left or decelerate player, depending on what user is doing.
   	 * return: does something; void
   	 * purpose: move the player given a change in time between frames.
   	 */
    public void movePlayer(float dt) {
    	float offset_mag = x_pos * 2.0f;
    	
    	if(moveRight) //if R
    		x_vel += x_accel;
    	
    	if(moveLeft) //if L
    		x_vel -= x_accel;
    	
    	final float slowMul = 1.6f;
    	
    	if(!moveRight && !moveLeft) { //decel
    		if(x_vel < -x_accel) {
    			x_vel += x_accel * slowMul;
    		}else if(x_vel > x_accel) {
    			x_vel -= x_accel * slowMul;
    		}else if(x_accel != 0) {
    			x_vel = 0;
    		}
    	}
    	
    	if(offset_mag > PLATFORM_W) //on edge --> stop player
    		x_pos = PLATFORM_W / 2.0f;
    	else if(offset_mag < -PLATFORM_W)
    		x_pos = -PLATFORM_W / 2.0f;
    	
    	if(Math.abs(offset_mag) > PLATFORM_W || isDead)
    		x_vel = 0;
    	
    	dx = x_vel * dt;
    	
    	//rotation as a function of change in x
    	float rotationPerUnit = FastMath.PI / 5;
    	float rot = -rotationPerUnit * dx; 
    	
    	Quaternion rotateByY = new Quaternion();
    	float rotMag = Math.min(FastMath.PI / 2f, FastMath.abs(rot));
    	rotateByY.fromAngleAxis((rot < 0 ? -1 : 1) * rotMag, new Vector3f(0, 1, 0)); //rotate localrotation about y axis
    	
    	this.setLocalRotation(rotateByY);
    	
    }
    
    /*
   	 * params: bounding box that defines an object
   	 * return: int -- 0 - no coll; 1 - stop player move; 2 - move player back OR if items, just destroy the item if not return 0.
   	 * purpose: to move the player upon a collision.
   	 */
    public int checkCollisions(BoundingBox objectBounds) { //implement w generated bounding box
    	
		Vector3f playerPos = this.getWorldTranslation();
		Vector3f objPos = objectBounds.getCenter();

		float objR = objPos.x + objectBounds.getXExtent();
		float objL = objPos.x - objectBounds.getXExtent();
		float objFar = objPos.z - objectBounds.getZExtent();
		float objNear = objPos.z + objectBounds.getZExtent();

		float plrR = playerPos.x + bounds[3];
		float plrL = playerPos.x + bounds[2];
		float plrFar = playerPos.z + bounds[1];
		float plrNear = playerPos.z + bounds[0];

		boolean playerR = objL < plrR; // right player coll
		boolean playerL = objR > plrL; // left player coll

		boolean playerFar = objFar < plrNear;
		boolean playerClose = objNear > plrFar;

		if (playerFar && playerClose && playerL && playerR) {

			if (playerL && objR < plrR && x_vel < 0 && !isInvincible) {
				x_vel = 0;

				return 2; // nudge right
			}

			if (playerR && objL > plrL && x_vel > 0 && !isInvincible) {
				x_vel = 0;

				return 2; // nudge left
			}

			if (objL < plrL && objR > plrR && !isInvincible)
				z_offset += gameDZ;
//    			System.out.println("headcoll " + gameVel + " zoff:" + z_offset);

			return 1; // drag thy player to brazil
		}
		
		return 0;
    }
    
    /*
   	 * params: none
   	 * return: does something; void
   	 * purpose: get model from modelloader at the start of the game.
   	 */
    @Override
    protected void initModels() {
    	
        Spatial amogus = ModelLoader.getModel("player");
        
        amogus.setCullHint(CullHint.Never);
        
        BoundingBox bounding = (BoundingBox) amogus.getWorldBound();
		
		float xExt = bounding.getXExtent();
		float zExt = bounding.getXExtent();
		
		bounds = new float[4];
		bounds[0] = zExt; //closeZ
		bounds[1] = -zExt; //farZ
		bounds[2] = -xExt; //leftX
		bounds[3] = xExt; //rightX
		
        initAnimation(amogus);
        attachChild(amogus);
    }
    
    /*
   	 * params: spatial that defines the model (also has the animations)
   	 * return: does something; void
   	 * purpose: to initialize the animation data for the model. there is only one clip (walk), so that is the one that is loaded in.
   	 */
    private void initAnimation(Spatial amogus){
        animComposer = amogus.getControl(AnimComposer.class);
        for (AnimClip animClip : animComposer.getAnimClips()) {
            Action action = animComposer.action(animClip.getName());
            animComposer.addAction(animClip.getName(), action);
        }
        currentAction = animComposer.setCurrentAction("Walk");
        setWalkSpeed(2f);
        
        SkinningControl skinningControl = amogus.getControl(SkinningControl.class);
        skinningControl.setHardwareSkinningPreferred(false);
    }
    
    /*
   	 * params: float -- target walk speed
   	 * return: does something; void
   	 * purpose: set the animation speed of the player
   	 */
    public void setWalkSpeed(float walkSpeed){ //for anim
        currentAction.setSpeed(walkSpeed);
    }

    /*
   	 * params: change in time between last frame
   	 * return: does something; void
   	 * purpose: to update player data every frame (pos, vel, is dead, etc)
   	 */
	@Override
	public void update(float dt) {
		// TODO Auto-generated method stub
    	x_pos += dx;
    	if(!backMove) {
    		float fallForwardMul = 1.07f;
	    	if(z_offset > fallForwardMul * gameDZ)
	    		z_offset -= fallForwardMul * (isDead ? 0 : gameDZ);
	    	else if(z_offset != 0)
	    		z_offset = 0;
    	}
//    	System.out.println(backMove);
    	backMove = false;
    	setLocalTranslation(x_pos, 0, z_offset);
    	if(z_offset > DEATH_THRESHOLD)
    		isDead = true;
	}
	
	// ========== HELPER METHODS ========== //
	
	public float [] getBounds() {
		return bounds;
	}

	public void setMoveLeft(boolean moveLeft) {
		this.moveLeft = moveLeft;
	}

	public void setMoveRight(boolean moveRight) {
		this.moveRight = moveRight;
	}

	public boolean isDead() {
		return isDead;
	}
	
	public boolean isHalfDead() {
		return z_offset > 0;
	}
	
	public final void setBackMove(boolean backMove) {
		this.backMove = backMove;
	}
	
    public void setGameDZ(float gameVel) {
    	this.gameDZ = gameVel;
    }

	public boolean isInvincible() {
		return isInvincible;
	}

	public void setInvincible(boolean isInvincible) {
		this.isInvincible = isInvincible;
	}

	public final boolean isInRevive() {
		return isInRevive;
	}

	public final void setInRevive(boolean isInRevive) {
		this.isInRevive = isInRevive;
	}

	public final void setDead(boolean isDead) {
		this.isDead = isDead;
	}

}
