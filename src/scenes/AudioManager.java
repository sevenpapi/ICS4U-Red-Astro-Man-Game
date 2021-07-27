package scenes;

/*
 * DANIEL XIAO ISU COMPUTER SCIENCE 2021
 * This is part of my ISU game
 * 
 * The purpose of this class is to define behavior for the audio in the game scene.
 */

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;

public class AudioManager {
	
	private AssetManager assetManager;
	
	private int playMode;
	private static float MAX_VOL = 10;
	private float volume;
	
	private int crossFadeTarget;
	private int crossFadeOriginal;
	
	private AudioNode menuMusic;
	private AudioNode scoreMusic;
	
	private AudioNode normalMusic;
	private AudioNode dripMusic;
	private AudioNode drownMusic;
	
	private AudioNode errorSound;
	private AudioNode itemUse;
	private AudioNode itemPickup;
	
	private AudioNode alarm;
	private AudioNode oneup;
	
	private boolean isAlarmOn;
	
	public AudioManager(SimpleApplication app) {
		crossFadeOriginal = -1;
		crossFadeTarget = -1;
		assetManager = app.getAssetManager();
		playMode = 4;
		volume = MAX_VOL;
		isAlarmOn = false;
		
		initFiles();
	}
	
	private void initFiles() {
		normalMusic = new AudioNode(assetManager, getSound("normal"), DataType.Buffer);
		dripMusic = new AudioNode(assetManager,  getSound("drip"), DataType.Buffer);
		drownMusic = new AudioNode(assetManager, getSound("drowned"), DataType.Buffer);
		alarm = new AudioNode(assetManager, getSound("alarm"), DataType.Buffer);
		
		menuMusic = new AudioNode(assetManager, getSound("menu"), DataType.Buffer);
		scoreMusic = new AudioNode(assetManager, getSound("score"), DataType.Buffer);
		
		itemUse = new AudioNode(assetManager, getSound("itemuse"), DataType.Buffer);
		itemPickup = new AudioNode(assetManager, getSound("itemsound"), DataType.Buffer);
		errorSound = new AudioNode(assetManager, getSound("err"), DataType.Buffer);
		oneup = new AudioNode(assetManager, getSound("1up"), DataType.Buffer);
		
		alarm.setLooping(true);
		alarm.setVolume(MAX_VOL * 1.5f);
	}
	
	public void playItemUse() {
		playSound(itemUse);
	}
	
	public void playErr() {
		playSound(errorSound);
	}
	
	public void playRes() {
		playSound(oneup);
	}
	
	public void playItemPickup() {
		playSound(itemPickup);
	}
	
	private void playSound(AudioNode n) {
		n.setTimeOffset(0);
		n.setVolume(MAX_VOL);
		n.play();
	}
	
	public void startAlarm() {
		if(!isAlarmOn) {
			alarm.play();
			isAlarmOn =! isAlarmOn;
		}
	}
	
	public void stopAlarm() {
		if(isAlarmOn) {
			alarm.stop();
			isAlarmOn =! isAlarmOn;
		}
	}
	
	public void playGameMusic() {
		
		menuMusic.stop();
		scoreMusic.stop();
		setPlayMode(1, true);
		
		normalMusic.setVolume(volume);
		dripMusic.setVolume(0);
		drownMusic.setVolume(0);
		
		normalMusic.play();
		dripMusic.play();
		drownMusic.play();
		
		normalMusic.setLooping(true);
		dripMusic.setLooping(true);
		drownMusic.setLooping(true);
	}
	
	private void resetCrossFade() {
		crossFadeTarget = -1;
		crossFadeOriginal = -1;
	}
	
	public void setToMenu() {
		System.out.println("Menuset");
		setPlayMode(4, true);

		menuMusic.setVolume(MAX_VOL);
		scoreMusic.setVolume(0);
		
		menuMusic.setTimeOffset(0);
		scoreMusic.setTimeOffset(0);
		
		menuMusic.play();
		scoreMusic.play();
		
		normalMusic.stop();
		dripMusic.stop();
		drownMusic.stop();
		
		normalMusic.setTimeOffset(0);
		dripMusic.setTimeOffset(0);
		drownMusic.setTimeOffset(0);
	}
	
	private String getSound(String sFile) {
		return "Sounds/" + sFile + ".wav";
	}

	public void update(float dt) {
		if(playMode == 0)
			return;
		
//		System.out.println("music: " + playMode);
//		timer += dt;
		
		if(crossFadeTarget != -1) {
			AudioNode originalNode = getNodeByID(crossFadeOriginal);
//			System.out.println("FADING -- ORIG.: " + crossFadeOriginal + " TARG. : " + crossFadeTarget + " orig vol: " + originalNode.getVolume());
			
			final float dVol = dt * 5;
			
			if(originalNode.getVolume() < dVol) {
				originalNode.setVolume(0);
				resetCrossFade();
			}else if (crossFadeTarget != crossFadeOriginal){
				changeVolumeOfNodeBy(originalNode, -dVol);
				changeVolumeOfNodeBy(getNodeByID(crossFadeTarget), dVol);
			}
		}
		
//		if(timer > audioDuration) {
//			timer = 0;
//			playGameMusic();
//		}
	}
	
	public void showScores() {
		scoreMusic.play();
		setPlayMode(5, true);
		
	}
	
	private void changeVolumeOfNodeBy(AudioNode node, float dvol) {
		node.setVolume(node.getVolume() + dvol);
	}
	
	private AudioNode getNodeByID(int id) {
		switch(id) {
		case 1:
			return normalMusic;
		case 2:
			return dripMusic;
		case 3:
			return drownMusic;
		case 4:
			return menuMusic;
		case 5:
			return scoreMusic;
		}
		return null;
	}
	
	public void setPlayMode(int p, boolean doCrossFade) {
		if(crossFadeTarget != -1) {
			AudioNode originalNode = getNodeByID(crossFadeOriginal);
			originalNode.setVolume(0);
			getNodeByID(crossFadeTarget).setVolume(MAX_VOL);
		}
		
		if(doCrossFade) {
			crossFadeTarget = p;
			crossFadeOriginal = playMode;
			if (crossFadeTarget == crossFadeOriginal) {
				getNodeByID(crossFadeOriginal).setVolume(MAX_VOL);
				crossFadeTarget = -1;
				crossFadeOriginal = -1;
			}
		}
		playMode = p;
	}
	
	public int getPlayMode() {
		return playMode;
	}
	
}
