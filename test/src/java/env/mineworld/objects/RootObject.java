package env.mineworld.objects;

public class RootObject {
	
	private String name;
	private String type;
	private Location loc;
	
	public RootObject(String n, String t, Location l){
		name = n;
		type = t;
		loc = l;
	}
	
	public RootObject(String n, String t, int x, int y){
		name = n;
		type = t;
		loc = new Location(x, y);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}
	
	
	
}
