package data.chunks;

/*
 * DANIEL XIAO ISU COMPUTER SCIENCE 2021
 * This is part of my ISU game
 * 
 * The purpose of this class is to deal with the chunks that the player spawns inside.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.jme3.effect.ParticleEmitter;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import data.ModelLoader;
import gameobjects.GameObject;
import gameobjects.ItemObject;
import scenes.ParticleManager;

public class Chunk extends Node{
	
	private static int nextItem = -1;
	private static final int minItemDelay = 10; //min chunks passed for item spawn
	private static final int maxItemDelay = 25; //max chunks passed for item spawn
	
	private ParticleManager particleManager; //for particles
	
	private ArrayList<ChunkObject> chunkObjects;
	private ArrayList<ItemObject> itemObjects;
	private int[] configuration;
	private HashMap<Integer, Integer> configMap;
	
	private boolean hasItem;
	
	private BigMachineSide bigMachineSide; //does have machine? if so, which side?
	private enum BigMachineSide {LEFT, RIGHT, NONE};
	
	//<constructors>
	
	public Chunk(ParticleManager pm, ArrayList<Chunk> activeChunks, float platLength) {
		this(pm, activeChunks, platLength, false);
	}
	
	public Chunk(ParticleManager pm, ArrayList<Chunk> activeChunks, float platLength, boolean isEnvChunk) {
		super();
		
		particleManager = pm;
		
		chunkObjects = new ArrayList<ChunkObject>();
		itemObjects = new ArrayList<ItemObject>();
		attachChild(ModelLoader.getModel("envNode"));
    	
    	bigMachineSide = BigMachineSide.NONE;
    	
    	if(isEnvChunk || activeChunks.isEmpty())
    		return;
    	
    	Random random = new Random();
    	Chunk lastChunk = activeChunks.get(activeChunks.size() - 1);
    	
    	nextItem--;
    	hasItem = nextItem == 0;
    	if(hasItem) {
    		nextItem = random.nextInt(maxItemDelay - minItemDelay + 1) + minItemDelay;
    	}
    	else if(nextItem < -1) {
    		nextItem = random.nextInt(maxItemDelay - minItemDelay + 1) + minItemDelay + 5;
    	}
    	
    	configuration = new int[5];

		generateChunk(lastChunk, platLength, random);
		
	}
	
	//</constructors>
	
	/*
	 * params: the prev. chunk generated (null if none), length of platform player is on, random object
	 * return: does something; void
	 * purpose: called in constructor; helps set variables. generates a random chunk with random stuff and possubly items.
	 */
	private void generateChunk(Chunk lastChunk, float platLength, Random random) {
		boolean bigEdgeGenerate = weightedCoin(25, random);
		if(bigEdgeGenerate) {
			boolean isLeft = false;
			
			if(lastChunk.bigMachineSide == BigMachineSide.NONE) {
				isLeft = random.nextBoolean();
			}else {
				isLeft = lastChunk.bigMachineSide != BigMachineSide.LEFT;
			}
			
			bigMachineSide = isLeft ? BigMachineSide.LEFT : BigMachineSide.RIGHT;
			int lTmp = isLeft ? 1 : 0;
			int notlTmp = isLeft ? 0 : 1;
			configuration[0] = lTmp;
			configuration[1] = lTmp;
			configuration[3] = notlTmp;
			configuration[4] = notlTmp;
			
			
			configuration[isLeft ? (random.nextInt(2) + 3) : (random.nextInt(2))] = 1;
			
			clearBadTriples(random);
			
		}else {
			int[] indexes = lastChunk.bigMachineSide == BigMachineSide.NONE ?
					uniqueGen(3, 0, 4, random) : (lastChunk.bigMachineSide == BigMachineSide.LEFT ? 
							uniqueGen(3, 1, 4, random) : uniqueGen(3, 0, 3, random));
							//last was left              //last was right
			randomFill(indexes, random);
			bigMachineSide = BigMachineSide.NONE;
			generateConfigMap();
		}
		if(hasItem) {
			ArrayList<Integer> possibleSpots = new ArrayList<Integer>();
			for(int i = 0; i < configuration.length; i++) {
				if(configuration[i] == 0)
					possibleSpots.add(i);
			}
			if(possibleSpots.size() != 0)
				configuration[possibleSpots.get(random.nextInt(possibleSpots.size()))] = random.nextInt(5) + 2; //0 - z00m, 1- time, 2- invincib, 3 - points, 4 - 1up
			
			for(int i = 0; i < configuration.length; i++) {
				if(configuration[i] > 1) {
					addItemObjAtIndex(i, configuration[i], platLength, random);
				}
			}
		}
		
		fillChunk(platLength, random);
	}
	
	/*
	 * params: platform length that the player is on, random object
	 * return: does something; void
	 * purpose: given a randomly generated chunk permutation, this method fills it up with 3d models that were defined in ModelLoader at the start.
	 */
	private void fillChunk(float platLength, Random random) {
		Quaternion halfspin = new Quaternion();
		halfspin.fromAngleAxis( FastMath.PI , new Vector3f(0,1,0) );
		
		switch(bigMachineSide) {
		
		case NONE:
			for(Integer index : configMap.keySet()) {
				int count = configMap.get(index);
				
				if(index == 0 && count == 2) {
					//left 2blk
					ChunkObject generatedLeft2bk = ChunkObject.generateRandomChunkObject(ChunkObjectType.EDGE_TWO, random);
					generatedLeft2bk.setLocalRotation(halfspin);
					addChunkObj(generatedLeft2bk);
				}
				else if(index == 3 && count == 2) {
					//right 2b
					ChunkObject generatedRight2bk = ChunkObject.generateRandomChunkObject(ChunkObjectType.EDGE_TWO, random);
					addChunkObj(generatedRight2bk);
				}
				else if(index == 1 && count == 3) {
					ChunkObject generatedMid3 = ChunkObject.generateRandomChunkObject(ChunkObjectType.BIG_MID, random);
					addChunkObj(generatedMid3);
				}
				else if(count == 2) {
					Quaternion randomRotation = new Quaternion();
					final int MAXANGLE = 20;
					randomRotation.fromAngleAxis( FastMath.PI * MAXANGLE * random.nextFloat() / 180 , new Vector3f(0,1,0) );
					
					ChunkObject generated2obj = ChunkObject.generateRandomChunkObject(ChunkObjectType.TWO, random);
					generated2obj.setLocalRotation(randomRotation);
					addChunkObj(generated2obj);
					float increment = platLength / 5;
					
					float offsetSpread = increment / 3;
					float offsetNoise = random.nextFloat() * (offsetSpread * 2 + 1) - offsetSpread;
					
					int relativePos = index + 1;
					float distFromLeft = increment * relativePos;
					generated2obj.move(new Vector3f(-platLength / 2 + distFromLeft + offsetNoise, 0, 0));
				}
				else if(count == 1) {
					addIndividualObjAtIndex(index, platLength, random);
				}else {
					System.out.println("Not supposed to be here!");
				}
			}
			return;			
		case LEFT:
			ChunkObject generatedLeftMachine = ChunkObject.generateRandomChunkObject(ChunkObjectType.BIG_EDGE, random);
			generatedLeftMachine.setLocalRotation(halfspin);
			
			addChunkObj(generatedLeftMachine);
			for(int i = 2; i < 5; i++)
				if(configuration[i] == 1) {addIndividualObjAtIndex(i, platLength, random); break;}
			break;
		case RIGHT:
			ChunkObject generatedRightMachine = ChunkObject.generateRandomChunkObject(ChunkObjectType.BIG_EDGE, random);
			addChunkObj(generatedRightMachine);
			for(int i = 0; i < 3; i++)
				if(configuration[i] == 1) {addIndividualObjAtIndex(i, platLength, random); break;}
			break;
		default:
			System.out.println("this should not happen");
			break;
		}
		
	}
	
	/*
	 * params: index -- where within the chunk the indiv. obj is at; length of platform the player is on; random obj
	 * return: does something; void
	 * purpose: to add an object that takes up a space of 1 unit to  the scene.
	 */
	private void addIndividualObjAtIndex(int index, float platLength, Random random) {
		Quaternion randomRotation = new Quaternion();
		final int MAXANGLE = 45;
		randomRotation.fromAngleAxis(FastMath.PI * MAXANGLE * random.nextFloat() / 180 , new Vector3f(0,1,0)); //random rotation on add
		
		ChunkObject generated1obj = ChunkObject.generateRandomChunkObject(ChunkObjectType.ONE, random);
		generated1obj.setLocalRotation(randomRotation);
		addChunkObj(generated1obj);
		offsetHelper(index, generated1obj, platLength, random);
	}
	
	// ====================== HELPER METHODS  ====================== //
	//(im very sorry i know there r a lot of them but most of them just do math or help with random number generation please dont dock marks)
	
	private void offsetHelper(int index, GameObject gameObject, float platLength, Random random) {
		float increment = platLength / 5;
		
		float offsetSpread = increment / 6;
		float offsetNoise = random.nextFloat() * (offsetSpread * 2 + 1) - offsetSpread;
		
		float distFromLeft = increment / 2 + increment * index;
		gameObject.move(new Vector3f(-platLength / 2 + distFromLeft + offsetNoise, 0, 0));
	}
	
	private void addItemObjAtIndex(int index, int itemID, float platLength, Random random) {
		String itemToAdd = "";
		String particlesToAdd = "";
		
		switch(itemID) {
		case 2:
			itemToAdd = "zoom";
			particlesToAdd = "purple";
			break;
		case 3:
			itemToAdd = "time";
			particlesToAdd = "blue";
			break;
		case 4:
			itemToAdd = "god";
			particlesToAdd = "yellow";
			break;
		case 5:
			itemToAdd = "points";
			particlesToAdd = "gray";
			break;
		case 6:
			itemToAdd = "1up";
			particlesToAdd = "red";
			break;
		}
		
		ItemObject item = new ItemObject(itemToAdd);
		addItemObj(item, particlesToAdd);
		offsetHelper(index, item, platLength, random);
	}
	
	private boolean weightedCoin(int chanceTrue, Random rand) {
		return rand.nextInt(100) < chanceTrue;
	}
	
	private void randomFill(int[] indexes, Random random) {
		for(int i = 0; i < indexes.length; i++) 
			configuration[indexes[i]] = 1;
		
		clearBadTriples(random);
	}
	
	private void clearBadTriples(Random random) {
		if(configuration[0] == 1 && configuration[1] == 1 && configuration[2] == 1) {
			configuration[0] = 0;
			configuration[4] = 1;
		}else if(configuration[2] == 1  && configuration[3] == 1  && configuration[4] == 1) {
			configuration[4] = 0;
			configuration[0] = 1;
		}
	}
	
	private void generateConfigMap() {
		configMap = new HashMap<Integer, Integer>();
		int current = 0;
		for(int i = 0; i < configuration.length; i++) {
			if(configuration[i] == 1)
				configMap.put(current, configMap.containsKey(current) ? configMap.get(current) + 1 : 1);
			else
				current = i + 1;
		}
	}
	
	private int[] uniqueGen(int arrSize, int lower, int upper, Random random) {
		
		if(upper - lower < arrSize || upper < lower) {
			return null;
		}
		
		HashSet<Integer> tmp = new HashSet<Integer>();
		while(tmp.size() < arrSize) 
			tmp.add(random.nextInt(upper - lower + 1) + lower);
		
		int[] out = new int[arrSize];
		int i = 0;
		for(Integer val: tmp) 
			out[i++] = val;
		return out;
	}
	
	private void addChunkObj(ChunkObject obj) {
		attachChild(obj);
		chunkObjects.add(obj);
	}
	
	private void addItemObj(ItemObject obj, String particlesToAdd) {
		attachChild(obj);
		ParticleEmitter ps = particleManager.getParticleSystem(particlesToAdd);
		obj.attachChild(ps);
		ps.move(0, 50, 0);
		itemObjects.add(obj);
	}
	
	public ArrayList<ChunkObject> getChunkObjects(){
		return chunkObjects;
	}
	
	public ArrayList<ItemObject> getChunkItems(){
		return itemObjects;
	}
	
}
