package env.mineworld.objects;


public class ObjectPropertyManager {

	private static class ObjectPropertyManagerHolder{
		private static final ObjectPropertyManager INSTANCE = new ObjectPropertyManager();
	}
	
	private ObjectPropertyManager(){
		//this class must load an Ontology
		
	}
	
	public ObjectPropertyManager getInstance(){
		return ObjectPropertyManagerHolder.INSTANCE;
	}
	
	
}
