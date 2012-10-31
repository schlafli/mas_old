package env.mineworld.tile;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class TerrainManager {
	
	private Terrain [] terrains;
	private HashMap<String, Integer> nameMap;
	private HashMap<Color, Integer> colorMap;
	private HashMap<Integer, Integer> RGBcolorMap;
	
	
	private boolean loaded = false;

	@SuppressWarnings("unchecked")
	public boolean loadTerrain(String terrainDefURI){
		if(loaded) return loaded;
		
		File terrainFile = new File(terrainDefURI);
		if(!terrainFile.exists()){
			System.err.println("Cannot find terrain definition file");
			return false;
		}

		SAXBuilder builder = new SAXBuilder();

		try{

			Document document = (Document) builder.build(terrainFile);
			Element rootNode = document.getRootElement();
			List<Element> list = (List<Element>) rootNode.getChildren("terrain");
			int numberOfterrains = list.size();
			
			System.out.println("Number of terrains: "+ numberOfterrains);
			terrains = new Terrain[numberOfterrains+1];
			nameMap = new HashMap<String, Integer>(numberOfterrains);
			colorMap = new HashMap<Color, Integer>(numberOfterrains);
			RGBcolorMap = new HashMap<Integer, Integer>(numberOfterrains);
			
			for (int i=0; i< numberOfterrains; i++)
			{
				Element node = list.get(i);
				String name = node.getChildText("name");
				Terrain t = new Terrain(name, i);
				
				Element colour = node.getChild("colour");
				int r = Integer.parseInt(colour.getChildText("r"));
				int g = Integer.parseInt(colour.getChildText("g"));
				int b = Integer.parseInt(colour.getChildText("b"));
				
				t.setColor(r, g, b);
				terrains[i] = t;
				nameMap.put(t.getName(), i);
				colorMap.put(t.getColor(), i);
				RGBcolorMap.put(t.getColor().getRGB(), i);
			}
			Terrain tmp = new Terrain("unknown", numberOfterrains);
			tmp.setColor(255, 255, 255);
			terrains[numberOfterrains] = tmp;
			
			loaded = true;
			
		}catch(IOException io){
			System.out.println(io.getMessage());
		}catch(JDOMException jdomex){
			System.out.println(jdomex.getMessage());
		}


		return loaded;
	}
	
	
	private static class TerrainManagerHolder{
		private static final TerrainManager INSTANCE = new TerrainManager();
	}

	private TerrainManager(){

	}

	public int getTerrainID(Color c){
		Integer i = colorMap.get(c);
		if(i == null){
			return terrains.length-1;
		}else{
			return i.intValue();
		}
	}
	
	public int getTerrainID(String s){
		Integer i = nameMap.get(s);
		if(i == null){
			return terrains.length-1;
		}else{
			return i.intValue();
		}
	}
	
	public int getTerrainID(int rgb){
		Integer i = RGBcolorMap.get(rgb);
		if(i == null){
			return terrains.length-1;
		}else{
			return i.intValue();
		}
	}
	
	public String getNameFromID(int ID){
		if(ID>=terrains.length || ID < 0 ){
			return "Invalid ID";
		}else{
			return terrains[ID].getName();	
		}
	}
	
	
	public static TerrainManager getInstance(){
		return TerrainManagerHolder.INSTANCE;
	}

	
	public static boolean init(String terrainDefURI){
		return getInstance().loadTerrain(terrainDefURI);
	}
	
	public Terrain[] getTerrainArray(){
		return terrains;
	}
	

}
