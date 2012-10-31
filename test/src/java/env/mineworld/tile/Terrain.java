package env.mineworld.tile;

import java.awt.Color;


public class Terrain {
	
	private String name;
	private int ID;
	private Color color;
	
	public Terrain(String name, int ID){
		this.name = name;
		this.ID = ID;
	}
	
	public void setColor(int r, int g, int b){
		this.color = new Color(r,g,b);
	}
	
	public String getName(){
		return name;
	}
	public Color getColor(){
		return color;
	}
	
	public int getID(){
		return ID;
	}
	
	
}
