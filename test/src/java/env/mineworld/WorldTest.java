package env.mineworld;



import java.awt.AWTException;

import env.mineworld.gui.WorldView;

public class WorldTest {

	WorldModel model;
	WorldView view;
	
	String terrainDefURI = "resources/terrain_types.xml";
	String mapImageURI = "resources/maps/map1.png";
	String entityXMLURI = "resources/maps/map1.xml";
	
	
	
	public WorldTest(){
		model = new WorldModel();
		model.loadWorld(terrainDefURI, mapImageURI, entityXMLURI);
		
		view = new WorldView("View", model,800,800);
		
		if(model.getGridWorld() != null){
			System.out.println("World loaded successfully!!(I think)");
			//System.out.println(model.getGridWorld().toString());
			
			System.out.println("Loading GUI resources...");
			boolean done = view.loadAll();
			System.out.println("Done:" + done);
			view.repaint();
		
		}else{
			System.out.println("World loading failed");
		}
	}
	
	
	/**
	 * @param args
	 * @throws AWTException 
	 */
	public static void main(String[] args) throws AWTException {
		new WorldTest();
				
	}

}
