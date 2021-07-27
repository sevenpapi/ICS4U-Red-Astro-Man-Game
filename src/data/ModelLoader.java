package data;

/*
 * DANIEL XIAO ISU COMPUTER SCIENCE 2021
 * This is part of my ISU game
 * 
 * The purpose of this class is to load up all the 3d models and image texture assets.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.TangentBinormalGenerator;

public class ModelLoader {
	
	private static HashMap<String, Spatial> spatialMap; //stores all models loaded at start
	
	/*
	 * params: assetManager object from engine
	 * return: does something; void.
	 * purpose: to load up everything into spatial map at the start of the game.
	 */
	public void init(AssetManager assetManager) {
		spatialMap = new HashMap<String, Spatial>();
		initAssets(assetManager);
		
		String[] envItems = {"env_walls", "env_curb" ,"env_ground", "lamp_bulb", "lamp_tip", "lamp_body", "ceil", "ceil_lamp", "ceil_pipe"};
		insertNodeGroup("envNode", envItems);
	}
	
	/*
	 * params: assetManager object from engine
	 * return: does something; void.
	 * purpose: helper method for init -- loads everyhing from the assetmap txt files.
	 */
	private void initAssets(AssetManager assetManager) {
		
		// PLAYER TEXTURE
		String playerModelPath = "Models/";
		String playerModelTexPath = "Textures/player/";
		loadGroup(assetManager, "assets/Textures/assetmap_player.txt", playerModelTexPath, playerModelPath);

		// ENV OBJECT TEXTURES
		String envModelPath = "Models/env/";
		String envModelTexPath = "Textures/env/";
		loadGroup(assetManager, "assets/Textures/assetmap_env.txt", envModelTexPath, envModelPath);
		
		// CHUNK OBJECT TEXTURES
		String chunkModelPath = "Models/chunkObj/";
		String chunkModelTexPath = "Textures/chunkModel/";
		loadGroup(assetManager, "assets/Textures/assetmap_chunkobj.txt", chunkModelTexPath, chunkModelPath);
		
		// ITEM OBJECT TEXTURES
		String itemModelPath = "Models/items/";
		String itemModelTexPath = "Textures/items/";
		loadGroup(assetManager, "assets/Textures/assetmap_items.txt", itemModelTexPath, itemModelPath);
	}
	
	/*
	 * params: assetManager object from engine, path to look for assetmap file, path to look for textures, path to look for 3d models
	 * return: does something; void.
	 * purpose: load models from a specific assetmap text file.
	 */
	private void loadGroup(AssetManager assetManager, String dataTxt, String textureDirPath, String objDirPath) {
		try {
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new FileReader(dataTxt));
			String currentLine = "";
			HashMap<String, Texture> textureMap = new HashMap<String, Texture>();
			while ((currentLine = reader.readLine()) != null && !currentLine.equals("-spatialmaps")) { //tex mappings
				String[] texProperties = currentLine.split(",");
				if(currentLine.trim().equals("") || currentLine.indexOf("//") == 0)
					continue;
				if(texProperties.length > 2) {
					boolean isTiled = false;
					boolean useKey = false;
					for(int i = 2; i < texProperties.length; i++) {
						isTiled = texProperties[i].trim().equals("tiled") ;
						useKey = texProperties[i].trim().equals("key");
					}
					loadTexture(assetManager, textureMap, texProperties[0].trim(), textureDirPath + texProperties[1].trim(), isTiled, useKey);
				}
				else 
					loadTexture(assetManager, textureMap, texProperties[0].trim(), textureDirPath + texProperties[1].trim());
			}
			
			System.out.println("loaded textures");
			
			while ((currentLine = reader.readLine()) != null) { //spatial mappings
				if(currentLine.trim().equals("") || currentLine.indexOf("//") == 0)
					continue;
				HashMap<String, String> modelData = new HashMap<String, String>();
				String[] modelProperties = currentLine.split(",");
				for(String p : modelProperties) {
					String[] tmp = p.trim().split(":");
					if(tmp.length != 2)
						throw new NullPointerException("bad file format: " + currentLine);
					modelData.put(tmp[0].trim(), tmp[1].trim());
				}
				
				loadModelFromData(assetManager, textureMap, objDirPath, modelData);
			}
			
			System.out.println("loaded models");
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * params: asset manager obj, texturemap hashmap (loaded from above method), directory to look for model, hashmap w/ data abt the model (from assetmap file).
	 * return: does something; void.
	 * purpose: load a specific model
	 */
	private void loadModelFromData(AssetManager assetManager, HashMap<String, Texture> textureMap, String baseDir, HashMap<String, String> modelData) {
		
		if(!modelData.containsKey("model"))
			throw new NullPointerException("missing model key for line " + modelData);
		if(!modelData.containsKey("name"))
			throw new NullPointerException("missing name key in: " + modelData);
		if(!(modelData.containsKey("diffuse") ^ modelData.containsKey("color")))
			throw new NullPointerException("missing texture/color key in (OR ambiguous -- both are defined): " + modelData);

		boolean isLit = !modelData.containsKey("unlit");
		boolean isTextured = modelData.containsKey("diffuse");
    	Spatial model = assetManager.loadModel(baseDir + modelData.get("model"));
    	Material modelMat = isLit ? new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md") : 
    								new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    	
    	if(isTextured) {
    		Texture modelDiffuseLoad = textureMap.get(modelData.get("diffuse"));
        	modelMat.setTexture(isLit ? "DiffuseMap" : "ColorMap", modelDiffuseLoad);
        	
        	if(isLit) {
        		if(modelData.containsKey("normal")) {
        			TangentBinormalGenerator.generate(model);
        			modelMat.setTexture("NormalMap", textureMap.get(modelData.get("normal")));
        		}
        		if(modelData.containsKey("metalness")) 
        			modelMat.setTexture("SpecularMap", textureMap.get(modelData.get("metalness")));
        	}
    	}else {
    		ColorRGBA baseColor = colorParseUtil(modelData, "color");
    		modelMat.setBoolean("UseMaterialColors", true);
    		if(isLit) {
    			modelMat.setColor("Diffuse", baseColor); //light reflects this color (diffuse)
    			if(modelData.containsKey("ambient")) {
    				ColorRGBA emission = colorParseUtil(modelData, "emission");
    				modelMat.setColor("Ambient", emission); //the "emission" color
    			}else 
    				modelMat.setColor("Ambient", baseColor); //the "emission" color
    		}
    		else 
    			modelMat.setColor("Color", baseColor);
    	}
    	
    	model.setMaterial(modelMat);
    	spatialMap.put(modelData.get("name"), model);
	}
	
	// ========================= HELPER METHODS ========================= //
	
	private void loadTexture(AssetManager assetManager, HashMap<String, Texture> textureMap, String name, String filePath) {
		loadTexture(assetManager, textureMap, name, filePath, false, false);
	}
	private void loadTexture(AssetManager assetManager, HashMap<String, Texture> textureMap, String name, String filePath, boolean isTiled, boolean useTextureKey) {
		Texture texture = useTextureKey ? assetManager.loadTexture(new TextureKey(filePath, false)) : assetManager.loadTexture(filePath);
		if(isTiled)
			texture.setWrap(WrapMode.Repeat);
		textureMap.put(name, texture);
	}
	
	private ColorRGBA colorParseUtil(HashMap<String, String> modelData, String key){
		String[] colorData = modelData.get(key).trim().replaceAll("[()]", "").split(";");
		if(colorData.length != 4)
			throw new NumberFormatException("bad color data");
		
		float[] colorValues = new float[4];
		
		for(int i = 0; i < colorData.length; i++) {
			colorValues[i] = Float.parseFloat(colorData[i]); //let it throw exception if theres bad num
		}
		
		ColorRGBA baseColor = new ColorRGBA(colorValues[0], colorValues[1], colorValues[2], colorValues[3]);
		return baseColor;
	}
	
	public static Spatial getModel(String name) {
		if(name.indexOf("d-") == 0)
			name = name.substring(2);
		try {
			return spatialMap.get(name).clone();
		}catch(NullPointerException e) {
			throw new NullPointerException("null name: " + name);
		}
	}
	
	private void insertNodeGroup(String groupName, String[] items) {
		Node nodeGroup = new Node();
		for(String item : items)
			nodeGroup.attachChild(spatialMap.get(item));
		spatialMap.put(groupName, nodeGroup);
	}
	
}
