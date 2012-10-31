package env;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;

public class start_log extends DefaultInternalAction {

	
	public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
		
		Atom prefix = (Atom)terms[0];
		LogOut.getInstance().openFile(prefix.toString());
		return true;
	
	}
}
