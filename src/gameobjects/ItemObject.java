package gameobjects;

/*
 * DANIEL XIAO ISU COMPUTER SCIENCE 2021
 * This is part of my ISU game
 * 
 * The purpose of this class is to store data about items.
 */

import java.util.HashMap;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import data.ModelLoader;

public class ItemObject extends GameObject{
	private String name;
	
	private static HashMap<String, String> nameToModels;
	private static float rotationVel = FastMath.PI; //rps
	
	private Spatial model;
	private float rotation;
	
	public ItemObject(String name) {
		super();
		this.name = name;
		rotation = (float) (Math.random() * FastMath.PI * 2);
		super.init();
	}
	
	

	@Override
	protected void initModels() {
		model = ModelLoader.getModel(nameToModels.get(name));
		attachChild(model);
	}
	
	public static void initItemModelPaths() {
		nameToModels = new HashMap<String, String>();
		
		addNamedItemObject("zoom", "purpsmoothie");
		addNamedItemObject("time", "clock");
		addNamedItemObject("god", "yellowsmoothie");
		addNamedItemObject("points", "sammich");
		addNamedItemObject("1up", "heart");
		
	}
	
	public void onCollisionDestroy() {
		//animation here probably
		detachAllChildren();
	}
	
	private static void addNamedItemObject(String name, String models) {
		nameToModels.put(name, models);
	}
	
	public BoundingBox getBoundingBox(){
		return (BoundingBox) model.getWorldBound();
	}
	
	@Override
	public void update(float dt) {
		rotation += dt * (rotationVel);
		rotation %= 2.0f * FastMath.PI;
		Quaternion smallSpin = new Quaternion();
		smallSpin.fromAngleAxis(rotation, new Vector3f(0,1,0) );
		this.setLocalRotation(smallSpin);
	};
	
	@Override
	public String toString() {
		return name;
	};
	
}
