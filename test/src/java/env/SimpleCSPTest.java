package env;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

import static choco.Choco.*;


public class SimpleCSPTest {

	public SimpleCSPTest(){
		Model m = new CPModel();
		
		//int Tn = 5;
		
		IntegerVariable[] Tasks = new IntegerVariable[4];
		Tasks[0] = makeIntVar("Task0", new int[]{0,1,3});
		Tasks[1] = makeIntVar("Task1", new int[]{1,2,3});
		Tasks[2] = makeIntVar("Task2", new int[]{1,2});
		Tasks[3] = makeIntVar("Task3", new int[]{0,1,2});
		
		int[][] costs = new int[4][];
		costs[0] = new int[]{2,2,0,1};
		costs[1] = new int[]{0,2,4,1};
		costs[2] = new int[]{0,2,1,0};
		costs[3] = new int[]{3,7,1,0};
		
		IntegerVariable [] Team = new IntegerVariable[4];
		Team[0] = makeIntVar("1st", 0, 10000);
		Team[1] = makeIntVar("2nd", 0, 10000);
		Team[2] = makeIntVar("3rd", 0, 10000);
		Team[3] = makeIntVar("4th", 0, 10000);
		
		IntegerVariable total = makeIntVar("Total", 0, 1000000);
		m.addConstraint(eq(sum(Team),total));
		
		for(int i=0;i<4;i++){
			m.addConstraint(nth(Tasks[i],costs[i],Team[i]));
		}
		
		//if 1 has task 0 or 3 then (2 cannot do task 0 and 3 cannot do task 3)
		m.addConstraint(implies(or(eq(1, Tasks[0]), eq(1,Tasks[3])), and(neq(2, Tasks[0]), neq(3, Tasks[3]))));
		m.addConstraint(implies(or(eq(2, Tasks[0]), eq(2,Tasks[1]), eq(2, Tasks[2]),eq(2, Tasks[3])), neq(1, Tasks[3])));		
		m.addConstraint(implies(or(eq(4, Tasks[0]), eq(4,Tasks[1])), and(neq(1, Tasks[0]), neq(2, Tasks[1]))));
		
		Solver s = new CPSolver();
		s.read(m);
		System.out.println("Starting solving...");
		
		if(s.minimize(s.getVar(total), false)){
		//if(s.solve()){
					
			int aa=0;
			for(int i=0;i<4;i++){
				System.out.print(s.getVar(Tasks[i]).getVal()+1);
			}
			System.out.print("\t"+s.getVar(total).getVal());
			System.out.println();
			
			while(s.nextSolution()){
				for(int i=0;i<4;i++){
					System.out.print(s.getVar(Tasks[i]).getVal()+1);
				}
				System.out.print("\t"+s.getVar(total).getVal());
				System.out.println();
				aa++;
				if(aa>100)break;
			}
		}
		
		
	}

	
	public static void main(String[] args) {
		new SimpleCSPTest();
	}

}
