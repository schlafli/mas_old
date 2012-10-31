package env.mineworld.tile;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class GridWorld {
	
	private static GridWorld instance = null;
	
	private GridTile [][] map;
	private TerrainManager TM;
	
	private double wToHratio = 0.75;
	private int width;
	private int height;
	private boolean mouseMapLoaded = false;
	private BufferedImage mouseMap;	
	
	
	private Logger log = Logger.getLogger("WorldLog");
	
	
	
	private GridWorld(int width, int height){
		map = new GridTile[width][height];

		this.width = width;
		this.height = height;

		TM = TerrainManager.getInstance();

		try {
			mouseMap = ImageIO.read(new File("resources/mousemap.png"));
			mouseMapLoaded = true;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		instance = this;
	}
	
	
	public static GridWorld getInstance(){
		return instance;
	}

	/**
	 * Method to set a 
	 * 
	 * @param x	X coordinate
	 * @param y	Y coordinate
	 * @param id The id of the terrain. Warning: This will be truncated to a short!
	 */
	private void setTileTerrain(int x, int y, int id){
		map[x][y] = new GridTile((short)id);
	}

	private static GridWorld loadWorldFromImage(File mapImage){
		Logger log = Logger.getLogger("WorldLog");
		BufferedImage mapimg;
		try {
			mapimg = ImageIO.read(mapImage);
		} catch (IOException e) {
			log.severe("Cannot read from Image: "+ mapImage.toURI().getRawPath());
			return null;
		}
		int img_w = mapimg.getWidth();
		int img_h = mapimg.getHeight();

		GridWorld world = new GridWorld(img_w, img_h);
		TerrainManager tm = TerrainManager.getInstance();
		for(int x = 0; x<img_w; x++){
			for(int y = 0; y<img_h; y++){
				world.setTileTerrain(x, y, tm.getTerrainID(mapimg.getRGB(x, y)));
			}
		}

		return world;
	}

	public static GridWorld loadGridWorld(String terrainDefURI, String mapImageURI, String entityXMLURI){
		
		Logger log = Logger.getLogger("WorldLog");
		
		boolean loaded = false;

		TerrainManager.init(terrainDefURI);

		File mapImage = new File(mapImageURI);
		File entityXML = new File(entityXMLURI);

		if(!mapImage.exists()){
			log.severe("Cannot find map image file");
			return null;
		}
		if(!entityXML.exists()){
			log.severe("Cannot find map XML file");
			return null;
		}

		//load Map from picture first
		//then see whether XML width and height match (print warning or something)

		GridWorld world = loadWorldFromImage(mapImage);
		
		if(world == null){ 
			return null;
		}
		log.info("World Loaded.");

		int map_w;
		int map_h;

		SAXBuilder builder = new SAXBuilder();
		try{
			Document document = (Document) builder.build(entityXML);
			Element rootNode = document.getRootElement();

			Element sizeNode = rootNode.getChild("size");
			map_w = Integer.parseInt(sizeNode.getChildText("x"));
			map_h = Integer.parseInt(sizeNode.getChildText("y"));

			if(map_w!=world.width || map_h!=world.height){
				log.warning("Map image width and height do not match map XML file width and height");
			}
		
		}catch(IOException io){
			System.out.println(io.getMessage());
		}catch(JDOMException jdomex){
			System.out.println(jdomex.getMessage());
		}

		if(loaded){

		}
		return world;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("\tWorld\n\n");
		sb.append("------------------------\n");
		sb.append("Size :"+width+"x"+height+"\n");
		sb.append("------------------------\n");
		for(int y = 0; y<height; y++){
			sb.append("    ");
			for(int x = 0; x<width; x++){
				if(x%2==1){
					sb.append(TM.getNameFromID(map[x][y].getTerrainID()).charAt(0)+"       ");
				}
			}
			sb.append('\n');
			for(int x = 0; x<width; x++){
				if(x%2==0){
					sb.append(TM.getNameFromID(map[x][y].getTerrainID()).charAt(0)+"       ");
				}
			}
			sb.append('\n');
		}
		return sb.toString();


	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
	
	public short getTerrainTypeAt(int x, int y){
		if(x<map.length && y<map[x].length){
			return map[x][y].getTerrainID();
		}else{
			return -1;
		}
	}
	
	public double getWidthToHeightRatio(){
		return wToHratio;
	}
	
	public Point2D.Double getMapCoordFromTile(Point p){

		double x;
		double y;
		
		double th = 1 / (height+0.5);
		double tw = 1 / (width*1.0);
		
		if((p.x%2)==0){
			y = (p.y*th) + (th);
		}else{
			y = (p.y*th) + (0.5*th);
		}
		
		x = (tw * p.x ) + ((0.5*tw)/wToHratio);
		return new Point2D.Double(x, y);
	}
	
	public Point getTileFromMapCoord(Point2D.Double p){
		if(mouseMapLoaded){
			if(p.x<0||p.y<0||p.x>1||p.y>1){
				System.out.println("Clicked outside map");
				return null;
			}
			int regionX;
			int regionY;
			double regionSizeX;
			double regionSizeY = (height+0.5);

			if((width%2)==0){
				regionSizeX = 0.5 * (width+(1-wToHratio));
			}else{
				regionSizeX = 0.5 * (width+1+(1-wToHratio)); 
			}


			double pointInRegionX = p.x * regionSizeX;
			double pointInRegionY = p.y * regionSizeY;

			regionX = (int) pointInRegionX; 
			regionY = (int) pointInRegionY; 

			pointInRegionX -= regionX;
			pointInRegionY -= regionY;



			//System.out.println(""+regionX+","+regionY+" at: "+pointInRegionX+","+pointInRegionY+"");
		
			int clicked = mouseMap.getRGB((int) (mouseMap.getWidth() * pointInRegionX), (int) (mouseMap.getHeight() * pointInRegionY));

			int regionDX = 0;
			int regionDY = 0;
			
			switch ( clicked )
			{
			case 0xffffff00 : // yellow
			{      
				regionDX = 0;
				regionDY = -1;
				break;
			}
			case 0xffffffff : // white
			{
				regionDX = 1;
				regionDY = 0;
				break;
			}
			case 0xff00ff00 : // green
			{            
				regionDX = -1;
				regionDY = 0;
				break;
			}
			case 0xff0000ff : // blue
			{ 
				regionDX = 0;
				regionDY = 0;
				break;
			}
			}
			
			regionX *= 2;
			
			regionX += regionDX;
			regionY += regionDY;
			
			
			if(regionX<0||regionY<0||regionX>=width||regionY>=height){
				return null;
			}else{
				return new Point(regionX,regionY);
			}
			
		}
		return null;
	}
}
