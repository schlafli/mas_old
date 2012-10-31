package env;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;


public class add_result extends DefaultInternalAction {
	
	@Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
		Atom success = (Atom)terms[0];
		
		
		if(success.toString().equals("yes")){
			LogOut.getInstance().log("y");
		}else{
			LogOut.getInstance().log("n");			
		}
		return true;
	}

}
