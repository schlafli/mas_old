package env.mineworld.objects;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.logging.Level;
import java.util.logging.Logger;

import env.mineworld.tile.GridWorld;

public class Location {
	Point2D.Double point;

	public Location(double x, double y){
		point = new Point2D.Double(x, y);
	}
	
	public Location(int x, int y){
		if(GridWorld.getInstance()!=null){
			point = GridWorld.getInstance().getMapCoordFromTile(new Point(x,y));
		}else{
			Logger.getLogger("WorldLog").log(Level.WARNING, "World instance is null. Can't locate point on map without dimensions!");
		}
	}
	
	

	public void updateLocation(double x, double y){
		if(point!=null){
			point.x = x;
			point.y = y;
		}
	}
	
	public void updateLocation(int x, int y){
		if(GridWorld.getInstance()!=null){
			point = GridWorld.getInstance().getMapCoordFromTile(new Point(x,y));
		}else{
			Logger.getLogger("WorldLog").log(Level.WARNING, "World instance is null. Can't locate point on map without dimensions!");
		}

	}
	
	

}
