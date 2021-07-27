package scenes.controllers;

/*
 * DANIEL XIAO ISU COMPUTER SCIENCE 2021
 * This is part of my ISU game
 * 
 * The purpose of this class is to connect the gui definition xml document to the java code and have it communicate w the game scene.
 */

import com.jme3.app.SimpleApplication;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import scenes.GameScene;

public class SceneController implements ScreenController {
	
	Nifty nifty;
	SimpleApplication application;
	GameScene gameScene;
	Screen screen;
	
	public SceneController(SimpleApplication application, GameScene gs) {
		this.application = application;
		gameScene = gs;
	}

	public void navTo(String nextScreen) {
		nifty.gotoScreen(nextScreen);
	}
	
	public void seeScores() {
		gameClear();
		navTo("scores");
		gameScene.showScores();
	}
	
	public void gameClear() {
		gameScene.clearGui();
	}
	
	public void resumeGame() {
		navTo("hud");
		gameScene.unpause();
	}
	
	public void exitGame() {
		navTo("start");
		gameScene.quitGame();
	}
	
	public void restartGame() {
		startGame();
		gameScene.doChunkInit();
	}
	
	public void startGame() {
		navTo("hud");
		gameScene.startGame();
	}

	public void quitGame() {
		if(application != null) {
			application.stop();
		}
	}

	@Override
	public void bind(Nifty arg0, Screen arg1) {
		nifty = arg0;
		this.screen = arg1;
	}
	
	@Override
	public void onEndScreen() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartScreen() {
		// TODO Auto-generated method stub
		
	}


}
