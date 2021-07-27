package data.chunks;

/*
 * DANIEL XIAO ISU COMPUTER SCIENCE 2021
 * This is part of my ISU game
 * 
 * The purpose of this class is to store data about the stuff that is spawned inside chunks.
 */

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Random;

import com.jme3.bounding.BoundingBox;
import com.jme3.scene.Spatial;

import data.ModelLoader;
import gameobjects.GameObject;

public class ChunkObject extends GameObject {
	
	private String name;
	private ArrayList<Spatial> collidableModels;
	
	private static HashMap<String, String> nameToModels;
	
	private static ArrayList<String> bigEdgeMachines, // names of big edge machines
	bigCentralObjs, // names of big central 3block objects
	twoEdgeOccs, // names of 2 space R/L edge occupants
	twoOccs, // names of 2 space occupants
	oneOccs; // names of 1 space occupants
	
	protected ChunkObject(String name) {
		super();
		this.name = name;
		collidableModels = new ArrayList<Spatial>();
		super.init();
	}
	
	public static ChunkObject generateRandomChunkObject(ChunkObjectType type, Random random) {
		
		switch(type) {
		case BIG_EDGE:
			return genReturnHelper(bigEdgeMachines, random);
		case BIG_MID:
			return genReturnHelper(bigCentralObjs, random);
		case EDGE_TWO:
			return genReturnHelper(twoEdgeOccs, random);
		case TWO:
			return genReturnHelper(twoOccs, random);
		case ONE:
			return genReturnHelper(oneOccs, random);
		}
		
		throw new NullPointerException("ur not supposed to be here");
	}

	@Override
	protected void initModels() {
		String[] models = nameToModels.get(name).split(", ");
		for(String modelName : models) {
//			Spatial model = modelLoader.getModel(modelName);
			Spatial model = ModelLoader.getModel(modelName);
			attachChild(model);
			if(modelName.indexOf("d-") != 0)
				collidableModels.add(model);
		}
	}; 
	
	public static void initChunkModelPaths() {
		nameToModels = new HashMap<String, String>();
		bigEdgeMachines = new ArrayList<String>();
		bigCentralObjs = new ArrayList<String>();
		twoEdgeOccs = new ArrayList<String>();
		twoOccs = new ArrayList<String>();
		oneOccs = new ArrayList<String>();
		
		//d- for decor (not collidable)
		addNamedChunkObject(bigEdgeMachines, "BigPipe", "pipe1, pipe1_p");
		addNamedChunkObject(bigEdgeMachines, "BigPipe2", "pipe2, pipe2_p");
		
		addNamedChunkObject(bigCentralObjs, "CenterChair", "centerChair, d-centerChair_screen");
		addNamedChunkObject(bigCentralObjs, "ElectricalBox", "electricalbox");
		
		addNamedChunkObject(twoEdgeOccs, "BigBoiler", "bigBoiler");
		addNamedChunkObject(twoEdgeOccs, "Beds", "beds");
		addNamedChunkObject(twoEdgeOccs, "SecurityDesk", "securitydesk");
		addNamedChunkObject(twoEdgeOccs, "EdgeSeat1", "edgeseat1, edgeseat1_chair");
		addNamedChunkObject(twoEdgeOccs, "EdgeSeat2", "edgeseat2, edgeseat2_chair");
		addNamedChunkObject(twoEdgeOccs, "Medscanner", "medbody, medplat, d-medwires, d-monitorL, d-monitorR, monitorTable");
		
		addNamedChunkObject(twoOccs, "RadioPlayer", "radioplayer");
		addNamedChunkObject(twoOccs, "Piano", "piano");
		addNamedChunkObject(twoOccs, "Forklift1", "forklift1");
		addNamedChunkObject(twoOccs, "Forklift2", "forklift2");
		addNamedChunkObject(twoOccs, "Gastank1", "gastank1");
		addNamedChunkObject(twoOccs, "Gastank2", "gastank2");
		addNamedChunkObject(twoOccs, "Gastank3", "gastank3");
		
		addNamedChunkObject(oneOccs, "Incubator", "incubator");
		addNamedChunkObject(oneOccs, "BarrelGreen", "greenbarrel");
		addNamedChunkObject(oneOccs, "BarrelNavy", "navybarrel");
		addNamedChunkObject(oneOccs, "BarrelBlue", "bluebarrel");
		addNamedChunkObject(oneOccs, "BarrelYellow", "yellowbarrel");
		addNamedChunkObject(oneOccs, "LeatherSeat", "leatherseat");
		addNamedChunkObject(oneOccs, "WoodTbl1", "woodtable1");
		addNamedChunkObject(oneOccs, "WoodTbl2", "woodtable2");
		addNamedChunkObject(oneOccs, "WoodStool", "woodstool");
		addNamedChunkObject(oneOccs, "Toilet", "toilet");
	}
	
	private static void addNamedChunkObject(ArrayList<String> targetType, String name, String models) {
		nameToModels.put(name, models);
		targetType.add(name);
	}
	
	public ArrayList<BoundingBox> getBoundingBoxes(){
		ArrayList<BoundingBox> out = new ArrayList<BoundingBox>();
		for(Spatial collidableModel : collidableModels) 
			out.add((BoundingBox) collidableModel.getWorldBound());
		return out;
	}
	
	@Override
	public void update(float dt) {
		//nothin...
	};
	
	@Override
	public String toString() {
		return name;
	};
	
	private static ChunkObject genReturnHelper(ArrayList<String> trg, Random random) {
		return new ChunkObject(trg.get(random.nextInt(trg.size())));
	}
	
}
