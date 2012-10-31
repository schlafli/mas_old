package env;

import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import static choco.Choco.*;

public class SimpleCSPTest2 {
	
	
	public SimpleCSPTest2(){
		
		
//		Model m = new CPModel();
//		Solver s = new CPSolver();
//		IntegerVariable nvar = makeIntVar("v1", 2, 3);
//		IntegerVariable[] vars = makeIntVarArray("var", 10, 0, 10);
//		SetVariable values = makeSetVar("s", 2, 6);
//		m.addConstraint(among(nvar, vars, values));
//		s.read(m);
//		s.solve();
//		while(s.nextSolution()){
//		System.out.println(s.getVar(values));
//		System.out.println(s.getVar(nvar));
//		for(IntDomainVar v:s.getVar(vars)){
//			System.out.println(v);
//		}
//		}
		
		
		

		Model m = new CPModel();
		
		
		IntegerVariable ag1 = constant(1);
		IntegerVariable ag2 = constant(2);
		IntegerVariable ag3 = constant(3);
		IntegerVariable ag4 = constant(4);
		
		SetVariable t1 = makeSetVar("task1", new int[]{1,2,4});
		
		
		
		
	}

	
	public static void main(String[] args) {
		new SimpleCSPTest2();
	}
}
