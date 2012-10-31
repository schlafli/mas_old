package env.mineworld;

import java.util.logging.Level;
import java.util.logging.Logger;

import env.mineworld.tile.GridWorld;
import env.mineworld.tile.TerrainManager;

public class WorldModel {

	private GridWorld world;
	protected TerrainManager TM;
	private Logger log = Logger.getLogger("WorldLog"); 
	
	public WorldModel(){
		log.setLevel(Level.ALL);
	}
	
	public boolean loadWorld(String terrainDefURI, String mapImageURI, String entityXMLURI){
		

		
		world = GridWorld.loadGridWorld(terrainDefURI, mapImageURI, entityXMLURI);
		if(world==null) return false;
		
		TM = TerrainManager.getInstance();
		
		
		return true;
	}
	
	public void tick(){
		//Do everything (This is to do with updating the movement)
		
	}
	
	
	public GridWorld getGridWorld(){
		return world;
	}
}
