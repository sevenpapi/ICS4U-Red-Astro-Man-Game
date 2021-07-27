package main;

/*
 * DANIEL XIAO ISU COMPUTER SCIENCE 2021
 * This is the main class for my isu game.
 */

import com.jme3.system.AppSettings;


public class Main {
	
	public static final int WINDOW_WIDTH = 1280;
	public static final int WINDOW_HEIGHT = 720;
    
    public static void main(String[] args) {
    	GameApp app = new GameApp();
    	
    	app.setShowSettings(false);

    	AppSettings settings = new AppSettings(true);

    	settings.put("Width", WINDOW_WIDTH);

    	settings.put("Height", WINDOW_HEIGHT);

    	settings.put("Title", "amogus game");

    	settings.put("Samples", 4);
    	

    	app.setSettings(settings);
    	
    	
        app.start();
    }
}