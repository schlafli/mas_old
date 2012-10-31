package env.mineworld.tile;


public class GridTile {
	private short terrainID;
	
	public GridTile(short id){
		this.terrainID = id;
	}
	
	public short getTerrainID(){
		return terrainID;
	}
	
}
