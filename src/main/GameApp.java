package main;

/*
 * DANIEL XIAO ISU COMPUTER SCIENCE 2021
 * This is part of my ISU game
 * 
 * The purpose of this class is to bundle up all the stuff for game startup and running.
 */

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.niftygui.NiftyJmeDisplay;

import data.ModelLoader;
import data.chunks.ChunkObject;
import de.lessvoid.nifty.Nifty;
import gameobjects.ItemObject;
import scenes.GameScene;
import scenes.ParticleManager;
import scenes.controllers.SceneController;

public class GameApp extends SimpleApplication implements ActionListener  {
    
    GameScene gameScene;

    @Override
    public void simpleInitApp() {
    	
    	ModelLoader gameLoader = new ModelLoader();
    	gameLoader.init(assetManager);
    	
    	ChunkObject.initChunkModelPaths();
    	ItemObject.initItemModelPaths();
    	ParticleManager.initParticles(assetManager);
    	
    	gameScene = new GameScene(this);
    	
		NiftyJmeDisplay niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
		Nifty nifty = niftyDisplay.getNifty();
		nifty.fromXml("Gui/guis.xml", "start", new SceneController(this, gameScene));
		guiViewPort.addProcessor(niftyDisplay);
		
		gameScene.setNifty(nifty);
    	
        flyCam.setEnabled(false);
        this.setDisplayFps(false);
        this.setDisplayStatView(false);
        
        initKeys();
    }
    
    private void initKeys(){
    	inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
    	
    	addKeyMapping("a", KeyInput.KEY_A);
    	addKeyMapping("a", KeyInput.KEY_LEFT);
    	
    	addKeyMapping("d", KeyInput.KEY_D);
    	addKeyMapping("d", KeyInput.KEY_RIGHT);
    	
    	addKeyMapping("s", KeyInput.KEY_S);
    	addKeyMapping("s", KeyInput.KEY_RSHIFT);
    	addKeyMapping("s", KeyInput.KEY_LSHIFT);
    	
    	addKeyMapping("esc", KeyInput.KEY_ESCAPE);
    	addKeyMapping("space", KeyInput.KEY_SPACE);
    }
    
    private void addKeyMapping(String mappingName, int key) {
    	inputManager.addMapping(mappingName, new KeyTrigger(key));
    	inputManager.addListener(this, mappingName);
    }
    
    @Override
    public void onAction(String binding, boolean isPressed, float tpf) {
        gameScene.onKeyPress(binding, isPressed, tpf);
    }
    
    @Override
    public void simpleUpdate(float dt) {
        
        gameScene.update(dt);

    }
}