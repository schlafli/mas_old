package env.mineworld.objects;

public class SelfPropObject extends RootObject {
	
	private Location target;
	private double distance;
	
	private double [] movementSpeedModifiers;
	private double baseSpeed;
	
	
	public SelfPropObject(String n, String t, Location l) {
		super(n, t, l);
	}
	
	
	public SelfPropObject(String n, String t, int x, int y) {
		super(n, t, x, y);
		
	}
	
	
	public double[] getMovementSpeedModifiers(){
		return movementSpeedModifiers;
	}
}
