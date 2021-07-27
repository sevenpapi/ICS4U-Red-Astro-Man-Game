package gameobjects;

/*
 * DANIEL XIAO ISU COMPUTER SCIENCE 2021
 * This is part of my ISU game
 * 
 * The purpose of this class is to abstract the storage of data for all gameobjects.
 */

import com.jme3.scene.Node;

public abstract class GameObject extends Node{
	
    protected void init(){
        initModels();
    }
    
    protected abstract void initModels();
    public abstract void update(float dt);
    
}
