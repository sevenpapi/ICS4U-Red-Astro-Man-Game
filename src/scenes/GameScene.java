package scenes;

/*
 * DANIEL XIAO ISU COMPUTER SCIENCE 2021
 * This is part of my ISU game
 * 
 * The purpose of this class is to handle stuff for the main game loop.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.effect.ParticleEmitter;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;

import data.ModelLoader;
import data.chunks.Chunk;
import data.chunks.ChunkObject;
import de.lessvoid.nifty.Nifty;
import gameobjects.ItemObject;
import gameobjects.Player;
import main.Main;

public class GameScene{
	
	private boolean running;
	
	private Node message;
	private float messageTimer;
	private boolean doMoveMessage;
	private static final float MAX_MESSAGE_HOVERTIME = 4; //time the message hovers in the middle
	private static final float MESSAGE_Y_POS = 21;
	private static final float MSG_W = 800;
	private static final float MSG_H = 75;
	
	private static final String SCORE_FONT_FILE_NAME = "Gui/fonts/banhaus_32.fnt";
	private static final String WHITE_FONT_FILE_NAME = "Gui/fonts/banhaus_32_white.fnt";
	BitmapFont scoreFont;
	
	private Picture messageBoxImage;
	private BitmapText messageText;
	private BitmapFont messageFont;
	
	private Player player;
	private Nifty nifty;
	private AudioManager audioManager;
	
	private ParticleManager particleManager;
	private ParticleEmitter currentParticles;
	
	private static final float BASE_SCORE_PER_SECOND = 50;
	private static final float MULTIPLIER_INCREMENT_DELAY = 30; //increase multiplier every 30 seconds
	private float multiplierIncrementCounter;
	private float score;
	private int scoreMultiplier;
	
	private SimpleApplication application;
	private Node rootNode;

	private BitmapText scoreText;
	private BitmapText multiplierText;
	
	private ItemMode itemMode;
	
	private ArrayList<Chunk> activeChunks; //chunks currently on screen
	private Node gameNode;
	
	private float tmpItemVelocity;
	private int itemTmpMul;
	private boolean doItemDecel;
	private boolean doDecelSpeedUp;
	
	private float gameSpeed, chunkOffset, gameTime; //gamespeed measured in chunks/min
	private float gameAccel; //increases w/ time (magnitude bounded by asymptote of rational function)
	
	Picture itemFrameImage;
	Picture fillRectImage;
	Picture itemBoxImage;

	private static final int INITIAL_GAMESPEED = 60;
	private static final float ACCEL_LIMIT = 0.35f;
	private static final float ITEM_ACCEL_LIMIT = 35f;
	
	private static final int CHUNK_COUNT = 30;
	private static final int CHUNKS_BEHIND = 5;
	private static final int INITIAL_CHUNK_BUFFER = 9;
	private static final int MIN_ITEM_BAR_WIDTH = 200;
	
	private float lastItemDuration;
	private float itemTimer;
	private String inventoryItem;
	
	private final float PLATFORM_Z_LENGTH;
	private final float PLATFORM_X_EXTENT;
	
	private boolean isPaused;
	private boolean isOver;
	
	//constructor -- initializes default data.
	public GameScene(SimpleApplication app) {
		this.application = app;
		this.rootNode = application.getRootNode();
		this.message = new Node();
		this.particleManager = new ParticleManager();
		
		// ---------------- DATA ---------------- //
		
		running = false;
		
		messageFont = application.getAssetManager().loadFont(WHITE_FONT_FILE_NAME);
		scoreFont = application.getAssetManager().loadFont(SCORE_FONT_FILE_NAME);
        messageText = new BitmapText(messageFont, false);
		
        messageBoxImage = new Picture("msgBox");
        
        itemTmpMul = 0;
		messageTimer = 0;
		doMoveMessage = false;
		
        activeChunks = new ArrayList<Chunk>();
		
		gameNode = new Node();
		
		fillRectImage = new Picture("fillRectImage");
		itemFrameImage = new Picture("ItemImage");
		itemBoxImage = new Picture("ItemBox");
		
		final int imageSz = 250;
		itemFrameImage.setPosition(0, Main.WINDOW_HEIGHT - imageSz);
		itemFrameImage.setWidth(imageSz);
		itemFrameImage.setHeight(imageSz);
		
		final int imageh = 250;
		fillRectImage.setLocalTranslation(0, Main.WINDOW_HEIGHT - imageh, -1);
		fillRectImage.setHeight(imageh);
		
		application.getGuiNode().attachChild(fillRectImage);
		application.getGuiNode().attachChild(itemBoxImage);
		application.getGuiNode().attachChild(itemFrameImage);
		application.getGuiNode().attachChild(message);
		
		audioManager = new AudioManager(app);
		audioManager.setToMenu();
		
		BoundingBox groundBounds = (BoundingBox) ModelLoader.getModel("env_ground").getWorldBound();
		
		PLATFORM_Z_LENGTH = groundBounds.getZExtent() * 2.0f; //fwd
		PLATFORM_X_EXTENT = groundBounds.getXExtent(); //side to side
		
		player = new Player(PLATFORM_X_EXTENT);
		player.setDefaults();
		rootNode.attachChild(player);
		
		// ---------------- LIGHTS ---------------- //
		AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.4f));
        rootNode.addLight(al);
        
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(0f,-.5f,-.5f).normalizeLocal());
        rootNode.addLight(sun);
        
        setGameScene();
        isPaused = true;
		isOver = false;
        
		
        rootNode.attachChild(gameNode);
        
        
        initFonts(app);
        app.getGuiNode().attachChild(scoreText);
        app.getGuiNode().attachChild(multiplierText);
        
        initCamera();
	}
	
	/*
   	 * params: message to display, whether it is an error message or not
   	 * return: does something; void
   	 * purpose: to display a little pop up message for the player after something happens
   	 */
	private void displayMessage(String message, boolean isError) {
		messageTimer = 0;
		doMoveMessage = true;
		//instantiate message
		this.message.detachAllChildren();
		if(isError) {
			audioManager.playErr();
		}
		
		messageBoxImage.setImage(application.getAssetManager(), "Gui/hud/" + (isError ? "msg_error" : "msg_normal") + ".png", true); //get image
		messageBoxImage.setWidth(MSG_W);
		messageBoxImage.setHeight(MSG_H);
		messageBoxImage.setLocalTranslation(Main.WINDOW_WIDTH / 2 - MSG_W / 2, -MSG_H, 0); //hide below screen
		
        messageText.setSize(messageFont.getCharSet().getRenderedSize());
        messageText.setColor(ColorRGBA.White); //set color
        messageText.setText(message);
        
        anchorMessageText();
        
		this.message.attachChild(messageBoxImage);
		this.message.attachChild(messageText);
	}
	
	/*
   	 * params: none, uses globals
   	 * return: does something; void
   	 * purpose: to show the list of high scores
   	 */
	public void showScores() {
		audioManager.showScores();
		BitmapText scoreNameText = new BitmapText(messageFont);
		BitmapText scoreNumText = new BitmapText(messageFont);
		
		String scoreFile = "scores.txt";
		String nameTxtOut = "";
		String numTxtOut = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(scoreFile));
			if (reader != null) {
				String currentLine;
				while ((currentLine = reader.readLine()) != null) { // read in a line as long as there exists a line to be
					String [] rawData = currentLine.split(" ");
					nameTxtOut += rawData[0] + '\n';
					numTxtOut += rawData[1] + '\n';
				}
				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			//shouldnt be here...
		}
		
		// ------ GUI STUFF ----- //
		
		float sty = Main.WINDOW_HEIGHT * 2 / 3;
		scoreNameText.setText(nameTxtOut);
		float stx1 = (Main.WINDOW_WIDTH - scoreNameText.getLineWidth()) * 2 / 5;
		scoreNameText.setLocalTranslation(stx1, sty, 0);
		
		scoreNumText.setText(numTxtOut);
		float stx2 = (Main.WINDOW_WIDTH - scoreNumText.getLineWidth()) * 3 / 5;
		scoreNumText.setLocalTranslation(stx2, sty, 0);
		
		application.getGuiNode().attachChild(scoreNameText);
		application.getGuiNode().attachChild(scoreNumText);
	}
	
	/*
   	 * params: the change in time between the last frame
   	 * return: does something; void
   	 * purpose: called on every frame (main game loop to update everything)
   	 */
	public void update(float dt) {
		
		if(doMoveMessage) { //if there is a message to be displayed
			messageTimer += dt;
			
			if(messageBoxImage.getWorldTranslation().y < MESSAGE_Y_POS) 
				messageBoxImage.move(0, 1, 0);
			
			if(messageTimer > MAX_MESSAGE_HOVERTIME) {
				messageBoxImage.move(0, -2, 0);
				if(messageBoxImage.getWorldTranslation().y <= -MSG_H) {
					messageTimer = 0;
					doMoveMessage = false;
				}
			}
			anchorMessageText();
		}

		if(!isPaused) { //if the game is not paused
//			application.getCamera().lookAt(player.getWorldTranslation(), Vector3f.UNIT_Y);
			anchorMultiplier();
			player.movePlayer(dt);
			
			if (chunkOffset > PLATFORM_Z_LENGTH){ //move chunks back to base pos if offset exceeds length
				chunkOffset = 0;
				activeChunks.remove(0).removeFromParent();
				
				generateChunk();
			}
			
			boolean doTimeAlter = itemMode == ItemMode.ZOOM || itemMode == ItemMode.TIME;
			float dChunkZ = PLATFORM_Z_LENGTH * (doTimeAlter || doItemDecel ? tmpItemVelocity : gameSpeed) / 60 * dt;
			player.setGameDZ(dChunkZ);
			
			int i = CHUNKS_BEHIND;
			for(int j = 0 ; j < activeChunks.size(); j++) {
				Chunk activeChunk = activeChunks.get(j);
				ArrayList<ItemObject> chunkItems = activeChunk.getChunkItems();
	        	
				if (j > CHUNKS_BEHIND - 1 && j < CHUNKS_BEHIND + 2) { //if the chunk is the one w the player
					ArrayList<ChunkObject> chunkObjs = activeChunk.getChunkObjects();
		        	int cCode = 0;
					for (ChunkObject chunkObj : chunkObjs) {
						ArrayList<BoundingBox> objColliders = chunkObj.getBoundingBoxes();
						for (BoundingBox boundingBox : objColliders) {
							cCode = player.checkCollisions(boundingBox);
							if (cCode == 1 && !player.isInvincible())
								player.setBackMove(true);
						}
	
					}
					
					int itemCode = 0;
					for(ItemObject item: chunkItems) { //item collisions, if any
						BoundingBox itemCollider = item.getBoundingBox();
						itemCode = player.checkCollisions(itemCollider);
						if(itemCode > 0) {
							inventoryItem = item.toString();
							audioManager.playItemPickup();
							setInventoryItem();
							item.onCollisionDestroy();
							chunkItems.clear();
							break;
						}
					}
				}
	        	
				for(ItemObject item: chunkItems) {
					item.update(dt);
				}
	        	
	        	float chZ = i * PLATFORM_Z_LENGTH + chunkOffset;
	        	activeChunk.setLocalTranslation(new Vector3f(0, 0, chZ));
	        	i--;
	        }
	        
			chunkOffset += dChunkZ;
			player.update(dt);
			
			if(itemTimer > dt) { //if there is an item active
				itemTimer -= dt;
				doItemAccel(dt);
				depleteItemBar();
			} else { //if there is not an item active
				player.setInRevive(false);
				if(player.hasChild(currentParticles))
					player.detachChild(currentParticles);
				lastItemDuration = 0;
//				setInventoryItem();
				itemTimer = 0;
				doItemDecel = doTimeAlter;
				doDecelSpeedUp = doItemDecel && itemMode == ItemMode.TIME;
				if(audioManager.getPlayMode() != 1 && running) {
					audioManager.setPlayMode(1, true);
				}
				if(!doItemDecel) {
					itemMode = ItemMode.NONE;
					player.setInvincible(false);
				}
			}
			doItemDecel(dt, doDecelSpeedUp);
			
			if(player.isHalfDead() && !player.isDead()) //alarm if player is not dead and is hitting a thing
				audioManager.startAlarm();
			else 
				audioManager.stopAlarm();
			
			if(!isOver && player.isDead()) { //if is not game over and is dead, do revive
				if(player.isInRevive()) {
					player.setInvincible(true);
					float targetRevivalVelocity = Math.max(gameSpeed * 0.6f, INITIAL_GAMESPEED);
					if(decel(dt, targetRevivalVelocity)) {
						player.setInRevive(false);
						player.setInvincible(false);
						player.setDead(false);
						player.setDefaults();
						resetItems();
						resetBar();
						displayMessage("You have been revived!", false);
						audioManager.playRes();
						System.out.println("decel done @ pos " + player.getLocalTranslation());
					}else { //fix decel
						float ratio = 1 - targetRevivalVelocity / gameSpeed;
						float dx = player.getLocalTranslation().x * ratio;
						float dz = player.getLocalTranslation().z * ratio; 
						player.setLocalTranslation (dx, 0, dz);
					}
				}else {//stop alarm and die
					audioManager.stopAlarm();
					die(dt);
				}
				
			} else if (player.isDead()) {//decelerate if dead
				decel(dt, 0);
			} else {//add scores and update if player is not dead 
				gameTime += dt;
				updateAccel(dt);
				gameSpeed += gameAccel;
				player.setWalkSpeed((doTimeAlter || doItemDecel ? tmpItemVelocity : gameSpeed) / 30.0f);
				updateScore(dt);
			}
						
				
		}
		audioManager.update(dt);
		
	}
	/*
   	 * params: time between frames
   	 * return: does something; void
   	 * purpose: defines behavior on the player's death
   	 */
	private void die(float dt) {
		resetGameData();
		running = false;
		
		try {
			String scoreFile = "scores.txt";
			BufferedReader reader = new BufferedReader(new FileReader(scoreFile));
			String currentLine;
			ArrayList<String[]> scores = new ArrayList<String[]>();
			
			if (reader != null) { //read scrores file and get player name if high score
				boolean doCompare = true;
				while ((currentLine = reader.readLine()) != null) { // read in a line as long as there exists a line to be
					String[] curScoreData = currentLine.split(" ");
					int curScore = Integer.parseInt(curScoreData[1]);
					if(score > curScore && doCompare) {
						String nameInput = "";
						boolean validName = false;
						do {
							nameInput = JOptionPane.showInputDialog(null, "New high score! What is your name (no spaces)?");
							validName = nameInput != null && nameInput.length() < 20 && !nameInput.equals("") && !nameInput.contains(" ");
							if(!validName) {
								JOptionPane.showMessageDialog(null, "Enter a proper name below length 30 (make sure to select 'ok' and not 'cancel').\n"
										+ " Name must:\n"
										+ " - Be less than 20 characters in length\n - Not be blank\n - Not contain any spaces","Alert",JOptionPane.WARNING_MESSAGE);     
							}
						}while(!validName);
						doCompare = false;
						String[] playerInsert = {nameInput, String.format("%.0f", score)};
						scores.add(playerInsert);
					}
					scores.add(curScoreData);
				}
				reader.close();
			}
			
			for(String[] s : scores) {
				System.out.println(s[0] + s[1]);
			}
			
			PrintWriter writer = new PrintWriter(scoreFile, "UTF-8");
			for(int i = 0; i < 10; i++) {
				String[] scoreSet = scores.get(i);
				String sOut = scoreSet[0] + " " + scoreSet[1];
				writer.println(sOut);
			}
			writer.close();
			
		} catch (IOException e) {
			System.out.println("file not found");
		}
		
		application.getGuiNode().detachAllChildren();
		
		isOver = true;
	
		nifty.gotoScreen("over");
		
		BitmapText deathText = new BitmapText(messageFont, false);
        deathText.setSize(scoreFont.getCharSet().getRenderedSize());
        deathText.setColor(ColorRGBA.White);
        deathText.setLocalTranslation(Main.WINDOW_WIDTH - 600, Main.WINDOW_HEIGHT * 3 / 5, 0);
        
        deathText.setText("Score: " + String.format("%.0f", score));
        
        application.getGuiNode().attachChild(deathText);
        
	}
	
	/*
   	 * params: time between frames
   	 * return: does something; void
   	 * purpose: updates scores every frame and chenges text on screen for scores and multiplier
   	 */
	private void updateScore(float dt) {
		int mul = itemMode != ItemMode.NONE  ? itemTmpMul : scoreMultiplier;
		String scoreStr = String.format("%.0f", score);
		scoreText.setText("Score: " + scoreStr + "\nSpeed: " + String.format("%.1f", itemMode == ItemMode.ZOOM || itemMode == ItemMode.TIME ? tmpItemVelocity : gameSpeed) + " b/s");
		multiplierText.setText((itemMode != ItemMode.NONE && scoreMultiplier != itemTmpMul ? "+" + itemTmpMul + "x\n" + scoreMultiplier : scoreMultiplier) + " x");
		
		multiplierIncrementCounter += dt;
		
		if(multiplierIncrementCounter - MULTIPLIER_INCREMENT_DELAY > 0) {
			scoreMultiplier++;
			displayMessage("Score multiplier increased to " + scoreMultiplier + ".", false);
			multiplierIncrementCounter = 0;
		}
			
		score += dt * BASE_SCORE_PER_SECOND * mul;
	}
	
	/*
   	 * params: none.
   	 * return: does something; void
   	 * purpose: defines behavior for when player uses an item
   	 */
	private void useItem() {
		final String itemError = "No item in slot";
		if(itemMode != ItemMode.NONE) {
			displayMessage(inventoryItem.equals("") ? itemError :"You can't use an item while another one is active!", true);
			return;
		}
		
		itemTmpMul = scoreMultiplier;
		switch(inventoryItem) {
		case "zoom":
			setPlayerParticles("playerPurple");
			itemTimer = 6;
			displayMessage("Speed boost activated for " + String.format("%.0f", itemTimer) + "s.", false);
			for(Chunk c: activeChunks) {
				ArrayList<ItemObject> itemObjs = c.getChunkItems();
				for(ItemObject item: itemObjs) {
					item.onCollisionDestroy();
				}
				itemObjs.clear();
			}
			
			audioManager.setPlayMode(2, true);
			itemMode = ItemMode.ZOOM;
			itemTmpMul = Math.max(50, 2 * scoreMultiplier);
			tmpItemVelocity = gameSpeed;
			player.setInvincible(true);
			break;
		case "time":
			setPlayerParticles("playerBlue");
			itemTimer = 10;
			displayMessage("Slow mode activated for " + String.format("%.0f", itemTimer) + "s.", false);
			audioManager.setPlayMode(3, true);
			itemMode = ItemMode.TIME;
			itemTmpMul = Math.max(50, 2 * scoreMultiplier);
			tmpItemVelocity = gameSpeed;
			break;
		case "god":
			setPlayerParticles("playerFire");
			itemTimer = 10;
			displayMessage("Invincibility activated for " + String.format("%.0f", itemTimer) + "s.", false);
			audioManager.setPlayMode(2, true);
			itemMode = ItemMode.GOD;
			itemTmpMul = Math.max(50, 2 * scoreMultiplier);
			player.setInvincible(true);
			break;
		case "points":
			setPlayerParticles("playerGray");
			itemTimer = 16;
			displayMessage("Point boost activated for " + String.format("%.0f", itemTimer) + "s.", false);
			audioManager.setPlayMode(2, true);
			itemMode = ItemMode.POINT;
			itemTmpMul = Math.max(100, 2 * scoreMultiplier);
			break;
		case "1up":
			setPlayerParticles("playerRed");
			itemTimer = 30;
			displayMessage("Revival activated for " + String.format("%.0f", itemTimer) + "s.", false);
			player.setInRevive(true);
			itemMode = ItemMode.LIFE;
			break;
		default:
			displayMessage(itemError, true);
			return;
		}
		
		audioManager.playItemUse();
	
		inventoryItem = "";
		
		lastItemDuration = itemTimer;
		final int imagew = 700;
		final int imageh = 250;
		fillRectImage.setLocalTranslation(0, Main.WINDOW_HEIGHT - imageh, -1);
		fillRectImage.setWidth(imagew);
		setInventoryItem();
	}
	
	/*
   	 * params: which key, whether its key down or up, and time per frame (same as dt earlier)
   	 * return: does something; void
   	 * purpose: defines behavior for user keyboard input
   	 */
	public void onKeyPress(String binding, boolean isPressed, float tpf) {
		
		if(!running)
			return;
		
		if (binding.equals("d")) {
			player.setMoveRight(isPressed);
		} else if (binding.equals("a")) {
			player.setMoveLeft(isPressed);
		} else if(binding.equals("esc") && isPressed && running) {
			isPaused = !isPaused;
			if(isPaused) {
				nifty.gotoScreen("pause");
				application.getGuiNode().detachAllChildren();
			} else {
				nifty.gotoScreen("hud");
				onStartAttachGui();
			}
		} else if(binding.equals("s") && isPressed) {
			useItem();
		}
	}
	
	/*
   	 * params: none
   	 * return: does something; void
   	 * purpose: places camera at the start of the game
   	 */
	private void initCamera(){
		Camera cam = application.getCamera();
		cam.setFrustumFar(10000);
		cam.setFrustumNear(1f);
		
		float fov = 50;
		float aspect = (float)cam.getWidth() / (float)cam.getHeight(); 
		cam.setFrustumPerspective(fov, aspect, cam.getFrustumNear(), cam.getFrustumFar());
		
		cam.update();
        
        float hDst = 419f;
        float vDst = 210f;
        
        Node horizonNode = new Node();
        horizonNode.setLocalTranslation(new Vector3f(0,0,-600));

		cam.setLocation(player.localToWorld(new Vector3f(0, vDst, hDst), null));
		cam.lookAt(horizonNode.getWorldTranslation(), Vector3f.UNIT_Y);
	}
	
	// ==================================== HELPER METHODS  ==================================== //
	// (i know there r a lot of them but most of these just reset like 5 variables or calls other methods above)
	// (it would be a pain to explain them all in depth w/ full method comments)
	
	public void clearGui() {
		application.getGuiNode().detachAllChildren();
	}
	
	private void setPlayerParticles(String particles) {
		currentParticles = particleManager.getParticleSystem(particles);
		player.attachChild(currentParticles);
		currentParticles.setLocalTranslation(0, 25, 10);
	}
	
	private void onStartAttachGui() {
		application.getGuiNode().attachChild(fillRectImage);
		application.getGuiNode().attachChild(itemBoxImage);
		application.getGuiNode().attachChild(itemFrameImage);
		application.getGuiNode().attachChild(message);
		application.getGuiNode().attachChild(scoreText);
		application.getGuiNode().attachChild(multiplierText);
		messageBoxImage.setLocalTranslation(Main.WINDOW_WIDTH / 2 - MSG_W / 2, -MSG_H, 0);
	}
	
	public void startGame() {
		player.setDefaults();
		application.getGuiNode().detachAllChildren();
		
		audioManager.setPlayMode(1, false);
		audioManager.playGameMusic();
		
		running = true;
		
		onSetSceneResetValues();
		onStartAttachGui();
		
		gameSpeed = INITIAL_GAMESPEED;
		isPaused = false;
		
		multiplierIncrementCounter = 0;
		score = 0;
		scoreMultiplier = 1;
		
		final int imageh = 250;
		itemBoxImage.setPosition(0, Main.WINDOW_HEIGHT - imageh);
		itemBoxImage.setWidth(imageh);
		itemBoxImage.setHeight(imageh);
	}
	
	private void anchorMessageText() {

		float textX = messageBoxImage.getLocalTranslation().x + MSG_W / 2 - messageText.getLineWidth() / 2;
		float textY = messageBoxImage.getLocalTranslation().y + MSG_H / 2 + messageText.getLineHeight() / 2;

		messageText.setLocalTranslation(textX, textY, 0);
	}
	
	private void resetGameData() {
		isPaused = false;
		
		resetItems();
		inventoryItem = "";
		
		player.setInRevive(false);
	}
	
	private void resetItems() {
		itemMode = ItemMode.NONE;
		itemTimer = 0;
		doItemDecel = false;
		doDecelSpeedUp = false;
		tmpItemVelocity = 0;
		itemTmpMul = 0;
	}
	
	private void onSetSceneResetValues() {
		resetGameData();
		
		isOver = false;
		
		chunkOffset = 0;
		gameTime = 0;
		gameSpeed = 0; //chunks/min
		gameAccel = 0;
		
		multiplierIncrementCounter = 0;
		score = 0;
		scoreMultiplier = 1;
		
		itemFrameImage.setImage(application.getAssetManager(), "Gui/hud/items/blank.png", true);
		fillRectImage.setImage(application.getAssetManager(), "Gui/hud/fillrect.png", true);
		itemBoxImage.setImage(application.getAssetManager(), "Gui/hud/itembox.png", true);
		resetBar();
		
	}
	
	private void resetBar() {
		fillRectImage.setWidth(MIN_ITEM_BAR_WIDTH/700.0f);
	}
	
	private void setGameScene() {
		player.setDefaults();
		onSetSceneResetValues();
		doChunkInit();
	}
	
	public void setInventoryItem() {
		
		switch(inventoryItem) {
		case "zoom":
			imgHelper("purple.png");
			break;
		case "time":
			imgHelper("clock.png");
			break;
		case "god":
			imgHelper("yellows.png");
			break;
		case "points":
			imgHelper("sandwich.png");
			break;
		case "1up":
			imgHelper("heart.png");
			break;
		default:
			imgHelper("blank.png");
		}
	}
	
	private void updateAccel(float dt) {
		float xSqueeze = 0.2f;
		gameAccel = (float) ((1 / (-(xSqueeze * gameTime + (1 / ACCEL_LIMIT)))) + ACCEL_LIMIT) * dt;
	}
	
	private void generateChunk() {
		Chunk generatedChunk = new Chunk(particleManager, activeChunks, PLATFORM_X_EXTENT * 2);
		activeChunks.add(generatedChunk);
		gameNode.attachChild(generatedChunk);
	}
	
	private void generateFirstChunks(int i) {
		Chunk generatedChunk = i < INITIAL_CHUNK_BUFFER ? new Chunk(particleManager, activeChunks, PLATFORM_X_EXTENT * 2, true) : new Chunk(particleManager, activeChunks, PLATFORM_X_EXTENT * 2);
		activeChunks.add(generatedChunk);
		gameNode.attachChild(generatedChunk);
	}
	
	private void doItemAccel(float dt) {
		if(itemMode == ItemMode.TIME) {
			if(tmpItemVelocity > INITIAL_GAMESPEED / 2.0f)
				tmpItemVelocity -= ITEM_ACCEL_LIMIT * dt;
		}else if(itemMode == ItemMode.ZOOM) {
			tmpItemVelocity += ITEM_ACCEL_LIMIT * dt;
		}
	}
	
	public void doChunkInit() {
		// ---------------- SCENE ---------------- //
		activeChunks.clear();
		gameNode.detachAllChildren();

		for (int i = 0; i < CHUNK_COUNT; i++)
			generateFirstChunks(i);

		// ---------------- SET SCENE ---------------- //

		int i = CHUNKS_BEHIND;
		for (Chunk ch : activeChunks) {
			float chZ = i * PLATFORM_Z_LENGTH;
			gameNode.attachChild(ch);
			ch.setLocalTranslation(new Vector3f(0, 0, chZ));
			i--;
		}
	}
	
	public void quitGame() {
		clearGui();
		audioManager.setToMenu();
		running = false;
	}
	
	private void doItemDecel(float dt, boolean doSpeedUp) { //decel phases?
		if(!doItemDecel)
			return;
		if(doSpeedUp) {
			if(tmpItemVelocity < gameSpeed) {tmpItemVelocity += ITEM_ACCEL_LIMIT * 2 * dt; return;}
		}else {
			if(tmpItemVelocity > gameSpeed) {tmpItemVelocity -= ITEM_ACCEL_LIMIT * 2 * dt; return;}
		}
		doItemDecel = false;
		tmpItemVelocity = 0;
		itemMode = ItemMode.NONE;
		player.setInvincible(false);
	}

	private boolean decel(float dt, float targetVel) {
		if(gameSpeed > ACCEL_LIMIT + targetVel) {
			gameSpeed -= ACCEL_LIMIT * 100 * dt;
		}
		else {
			gameSpeed = targetVel;
		}
		return(!(gameSpeed > ACCEL_LIMIT + targetVel));
	}
	
	private void depleteItemBar() { //fix this
		final double imagew = 700.0;
		final float m = (float) (MIN_ITEM_BAR_WIDTH / imagew);
		final float num = (1 - m) * itemTimer + m * lastItemDuration;
		fillRectImage.setWidth((float) ((num/lastItemDuration) * imagew));
	}
	
	private void imgHelper(String imgPath) {
		itemFrameImage.setImage(application.getAssetManager(), "Gui/hud/items/" + imgPath, true);
	}
	
	private void initFonts(SimpleApplication app) {
        scoreText = new BitmapText(scoreFont, false);
        scoreText.setSize(scoreFont.getCharSet().getRenderedSize());
        scoreText.setColor(ColorRGBA.Black);
        scoreText.setLocalTranslation(Main.WINDOW_WIDTH - 400, Main.WINDOW_HEIGHT - 40, 0);
        
        multiplierText = new BitmapText(scoreFont, false);
        multiplierText.setSize(scoreFont.getCharSet().getRenderedSize());
        multiplierText.setColor(ColorRGBA.Black);
        
        anchorMultiplier();
	}
	
	private void anchorMultiplier() {
		
        final float mulL = Main.WINDOW_WIDTH - 150;
        final float mulR = Main.WINDOW_WIDTH - 70;
        final float mulTT = Main.WINDOW_HEIGHT - 75;
        final float mulTB = Main.WINDOW_HEIGHT - 100;
        
        float xPos = mulL + (mulR - mulL) / 2 - multiplierText.getLineWidth() / 2;
        float yPos = mulTB + (mulTT - mulTB) / 2 + multiplierText.getHeight() / 2;
        
        multiplierText.setLocalTranslation(xPos, yPos, 0);
	}
	
	public void setNifty(Nifty n) {
		this.nifty = n;
	}
	public void unpause() {
		this.isPaused = false;
	}
	
	public String getInventoryItem() {
		return inventoryItem;
	}

}
