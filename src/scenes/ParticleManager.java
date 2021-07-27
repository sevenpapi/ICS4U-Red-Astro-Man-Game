package scenes;

/*
 * DANIEL XIAO ISU COMPUTER SCIENCE 2021
 * This is part of my ISU game
 * 
 * The purpose of this class is to define particle system behavior.
 */

import java.util.HashMap;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

public class ParticleManager {
	
	private static final String particlePath = "Effects/Smoke/Smoke.png";
	
	private static HashMap<String, ParticleEmitter> particleMap;
	
	public final ParticleEmitter getParticleSystem(String particleName){
		return particleMap.get(particleName).clone();
	}

	public static void initParticles(AssetManager assetManager) {
		particleMap = new HashMap<String, ParticleEmitter>();
		
		ParticleEmitter playerFire = new ParticleEmitter("Emitter", Type.Triangle, 30);
	    Material mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
	    mat_red.setTexture("Texture", assetManager.loadTexture(particlePath));
	    playerFire.setMaterial(mat_red);
	    playerFire.setImagesX(15); playerFire.setImagesY(1);
	    playerFire.setEndColor(  new ColorRGBA(1f, 0f, 0f, 1f));
	    playerFire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f));
	    playerFire.getParticleInfluencer().setInitialVelocity(new Vector3f(0,0,400));
	    playerFire.setStartSize(10f);
	    playerFire.setEndSize(1f);
	    playerFire.setGravity(0,0,0);
	    playerFire.setLowLife(0.5f);
	    playerFire.setHighLife(1f);
//	    fire.getParticleInfluencer().setVelocityVariation(0.3f);
	    
	    ParticleEmitter playerBlue = playerFire.clone();
	    playerBlue.setNumParticles(50);
	    playerBlue.setEndColor(new ColorRGBA(0f, 0f, 1f, 1f));
	    playerBlue.setStartColor(new ColorRGBA(0f, 0f, 1f, 0.5f));
	    
	    ParticleEmitter playerYellow = playerFire.clone();
	    playerYellow.setEndColor(new ColorRGBA(1f, 1f, 0f, 1f));
	    playerYellow.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f));
	    
	    ParticleEmitter playerPurple = playerFire.clone();
	    playerPurple.getParticleInfluencer().setInitialVelocity(new Vector3f(0,0,900));
	    playerPurple.setEndColor(new ColorRGBA(1f, 0f, 1f, 1f));
	    playerPurple.setStartColor(new ColorRGBA(1f, 0f, 1f, 0.5f));
	    
	    ParticleEmitter playerRed = playerFire.clone();
	    playerRed.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f));
	    playerRed.setStartColor(new ColorRGBA(1f, 0f, 0f, 0.5f));
	    
	    ParticleEmitter playerGray = playerFire.clone();
	    playerGray.setEndColor(new ColorRGBA(1f, 1f, 1f, 0.5f));
	    playerGray.setStartColor(new ColorRGBA(0f, 0f, 0f, 1f));
	    
	    particleMap.put("playerFire", playerFire);
	    particleMap.put("playerBlue", playerBlue);
	    particleMap.put("playerYellow", playerYellow);
	    particleMap.put("playerPurple", playerPurple);
	    particleMap.put("playerRed", playerRed);
	    particleMap.put("playerGray", playerGray);
	    
	    ParticleEmitter blue = playerFire.clone();
	    blue.getParticleInfluencer().setInitialVelocity(new Vector3f(0,0,100));
	    blue.setNumParticles(50);
	    blue.setEndColor(new ColorRGBA(0f, 0f, 1f, 1f));
	    blue.setStartColor(new ColorRGBA(0f, 0f, 1f, 0.5f));
	    
	    ParticleEmitter yellow = blue.clone();
	    yellow.setEndColor(new ColorRGBA(1f, 1f, 0f, 1f));
	    yellow.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f));
	    
	    ParticleEmitter purple = blue.clone();
	    purple.setEndColor(new ColorRGBA(1f, 0f, 1f, 1f));
	    purple.setStartColor(new ColorRGBA(1f, 0f, 1f, 0.5f));
	    
	    ParticleEmitter red = blue.clone();
	    red.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f));
	    red.setStartColor(new ColorRGBA(1f, 0f, 0f, 0.5f));
	    
	    ParticleEmitter gray = blue.clone();
	    gray.setEndColor(new ColorRGBA(1f, 1f, 1f, 0.5f));
	    gray.setStartColor(new ColorRGBA(0f, 0f, 0f, 1f));
	    
	    particleMap.put("blue", blue);
	    particleMap.put("yellow", yellow);
	    particleMap.put("purple", purple);
	    particleMap.put("red", red);
	    particleMap.put("gray", gray);
	}
}
