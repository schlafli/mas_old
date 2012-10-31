package env;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;


import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;


import static choco.Choco.*;


public class get_team extends DefaultInternalAction {

	/*
	 * env.get_team(Tasks, Proposals, Dislikes, Team, LineUp ,Success); 
	 * 
	 * Tasks: 		[[task(t1_1),task(t1_2),..task(t1_n)],..] 
	 * Proposals: 	[propose(PlanID, Step, Agent, Cost)...]
	 * Dislikes: 	[dislike(Source,Target,[task(x1),task(x2)...task(xn)]),...]
	 * Team:		[Agent1, Agent2 ... Agentn]  (in order of tasks)
	 * LineUp:		[[agent1,[task(t1_1),task(t1_2),..task(t1_n)]],...] (Note: LineUp is ordered)
	 * PropsToDel: 	
	 * Success:		yes|no
	 */

	private static final int TASKS = 0;
	private static final int PROPOSALS = 1;
	private static final int DISLIKES = 2;

	private static final int TEAM = 3;
	private static final int LINEUP = 4;

	private static final int PROPSTODEL = 5;

	private static final int SUCCESS = 6;


	private boolean minimize = false;

	/**
	 * 
	 */
	private static final long serialVersionUID = -7657721811648578737L;

	@Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] terms) throws Exception {
		LogOut asdf = LogOut.getInstance();
		try {
			long stime = System.currentTimeMillis();

			ListTerm tasks = (ListTerm) terms[TASKS];
			ListTerm proposals = (ListTerm) terms[PROPOSALS];
			ListTerm dislikes = (ListTerm) terms[DISLIKES];



			HashMap<String, Integer> taskID = new HashMap<String, Integer>();

			ArrayList<ListTerm> taskLists = new ArrayList<ListTerm>();

			int[] tmap = new int[tasks.size()];
			int uniqueTID = 0;

			ArrayList<Integer> [] ppt = new ArrayList[tasks.size()];

			for(int i=0;i<ppt.length;i++){
				ppt[i] = new ArrayList<Integer>();

				ListTerm task = (ListTerm) tasks.get(i);

				String subT = getSubTasksAsString(task);

				if(!taskID.containsKey(subT)){
					taskID.put(subT,uniqueTID);					
					taskLists.add(task);

					uniqueTID++;
				}

				tmap[i] = taskID.get(subT);

			}

			HashMap<String, Integer> participants = new HashMap<String, Integer>();

			ArrayList<Atom> nameAtoms = new ArrayList<Atom>();

			int uniqueAID = 0;

			ArrayList<ArrayList<Integer>> isInTask = new ArrayList<ArrayList<Integer>>();

			for(int i=0;i<proposals.size();i++){

				Literal proposal = (Literal) proposals.get(i);
				List<Term> propTerms = proposal.getTerms();

				NumberTerm nt = (NumberTerm) propTerms.get(1);
				int taskIndex = (int) Math.round(nt.solve()) - 1;
				ppt[taskIndex].add(i);
				Atom name = (Atom) propTerms.get(2);
				String sname = name.toString();
				if(!participants.containsKey(sname)){
					participants.put(sname, uniqueAID);
					nameAtoms.add(name);
					isInTask.add(new ArrayList<Integer>());
					uniqueAID++;
				}

				isInTask.get(participants.get(sname)).add(taskIndex);
			}

			IntegerVariable[] cTasks = new IntegerVariable[tasks.size()];

			HashMap<Integer, Literal> [] proposalMap = new HashMap[cTasks.length];

			for(int i=0;i<cTasks.length;i++){

				proposalMap[i] = new HashMap<Integer, Literal>();

				int[] domain = new int[ppt[i].size()];
				for(int j=0;j<ppt[i].size();j++){
					Literal proposal = (Literal) proposals.get(ppt[i].get(j));

					String name = getNameFromProposal(proposal);
					domain[j] = participants.get(name);
					proposalMap[i].put(domain[j], proposal);

				}
				Arrays.sort(domain); //TODO do I need to sort the domain?
				cTasks[i] = makeIntVar("Task"+i, domain);
			}

			int [] costMin = new int[tasks.size()];
			int [] costMax = new int[tasks.size()];

			int tCostMin = 0;
			int tCostMax = 0;

			int [][] costs = new int[tasks.size()][participants.keySet().size()];

			for(int i=0;i<tasks.size();i++){
				for(int j=0;j<costs[i].length;j++){
					costs[i][j]=0;

				}

				ArrayList<Integer> pitt = ppt[i];
				boolean first=true;
				costMin[i]=0;
				costMax[i]=0;

				for(Integer index: pitt){
					Literal proposal = (Literal) proposals.get(index);
					List<Term> propTerms = proposal.getTerms();
					Atom name = (Atom) propTerms.get(2);
					String sname = name.toString();
					int agIn = participants.get(sname);
					NumberTerm cost = (NumberTerm) propTerms.get(3);
					costs[i][agIn] = (int)Math.round(cost.solve());
					if(first){
						costMin[i] = costs[i][agIn];
						costMax[i] = costs[i][agIn];
						first=false;
					}else{
						if(costs[i][agIn]>costMax[i]){
							costMax[i] = costs[i][agIn];
						}

						if(costs[i][agIn]<costMin[i]){
							costMin[i] = costs[i][agIn];
						}
					}
				}

				tCostMax += costMax[i];
				tCostMin += costMin[i];
			}

			ArrayList<HashMap<Integer, ArrayList<Integer>>> constraintMap = new ArrayList<HashMap<Integer,ArrayList<Integer>>>(participants.keySet().size());
			for(int i=0;i<participants.keySet().size();i++){
				constraintMap.add(new HashMap<Integer, ArrayList<Integer>>());
			}

			for(int i=0;i<dislikes.size();i++){
				Literal disl = (Literal) dislikes.get(i);
				String [] dislike = getDislikesAsString(disl);
				if(participants.containsKey(dislike[0]) && participants.containsKey(dislike[1]) && taskID.containsKey(dislike[2])){
					int ds = participants.get(dislike[0]);
					int dt = participants.get(dislike[1]);
					int tt = taskID.get(dislike[2]);

					if(!constraintMap.get(ds).containsKey(dt)){
						constraintMap.get(ds).put(dt, new ArrayList<Integer>());
					}
					constraintMap.get(ds).get(dt).add(tt);
				}
			}




			IntegerVariable [] Team = new IntegerVariable[tasks.size()];
			for(int i=0;i<Team.length;i++){
				Team[i] = makeIntVar(""+i+"th", costs[i]);
			}




			Model m = new CPModel();

			m.addVariables(cTasks);
			m.addVariables(Team);

			IntegerVariable total=null;
			
			if(minimize){
				total = makeIntVar("total", tCostMin, tCostMax);
				m.addVariable(total);
				m.addConstraint(eq(sum(Team),total));
			}
			
			for(int i=0;i<tasks.size();i++){
				m.addConstraint(nth(cTasks[i],costs[i],Team[i]));
			}

			int constraintCount = 0;
			//adds all dislikes as implication constraints
			for(int i=0;i<constraintMap.size();i++){
				HashMap<Integer, ArrayList<Integer>> disl = constraintMap.get(i);
				//if there are some complaints
				if(disl.keySet().size()!=0){
					//create the premesis of the implication
					Constraint implPrem;

					Constraint [] prems = new Constraint[isInTask.get(i).size()];
					for(int j=0;j<prems.length;j++){
						prems[j] = eq(i, cTasks[isInTask.get(i).get(j)]);
						constraintCount++;
					}
					if(prems.length==1){
						implPrem = prems[0];
					}else{
						implPrem = or(prems);
					}

					//create the implication
					Constraint implI;

					ArrayList<Constraint> implC = new ArrayList<Constraint>();
					for(Integer key: disl.keySet()){
						ArrayList<Integer> dit = disl.get(key);
						for(Integer intask: dit){
							implC.add(neq(key, cTasks[intask]));
						}
					}

					Constraint [] implCArr = new Constraint[implC.size()];

					for(int j=0;j<implC.size();j++){
						implCArr[j] = implC.get(j);
						constraintCount++;
					}

					if(implC.size()==1){
						implI = implCArr[0];
					}else{
						implI = and(implCArr);
					}	
					m.addConstraint(implies(implPrem, implI));
				}

			}



			Solver s = new CPSolver();
			s.read(m);
			boolean found;
			
			if(minimize){
				found = s.minimize(s.getVar(total), false);
			}else{
				found = s.solve();
			}
			
			Atom success;
			int cost = -1;

			if(found){
				ListTerm teamAss = new ListTermImpl();
				ListTerm teamLineUp = new ListTermImpl();

				ListTerm propsToRemove = new ListTermImpl();

				//for each task add a new atom to the list? or atom as listitem
				//To avoid creating atoms, Create hashmap of agentIds -> atoms done
				//Same^ but for taskId -> listTerms of the compound task
				//Actually do for all items 
				for(int i=0;i<cTasks.length;i++){
					Integer tmpid = s.getVar(cTasks[i]).getVal();

					Atom name = nameAtoms.get(tmpid);
					ListTerm task = taskLists.get(tmap[i]);

					//System.out.println(name + ":" + getSubTasksAsString(task));
					teamAss.add(name);
					ListTerm assignment = new ListTermImpl();

					assignment.add(name);
					assignment.add(task);

					teamLineUp.add(assignment);					

					propsToRemove.add(proposalMap[i].get(tmpid));

				}


				un.unifies(terms[TEAM], teamAss);
				un.unifies(terms[LINEUP], teamLineUp);
				un.unifies(terms[PROPSTODEL], propsToRemove);

				//unify results with team and lineup
				//create new variable for proposals to remove and preposals to add
				success = new Atom("yes");
				if(minimize) cost =  s.getVar(total).getVal();
				
				
			}else{
				un.unifies(terms[PROPSTODEL], new ListTermImpl());
				success = new Atom("no");
			}

			un.unifies(success, terms[SUCCESS]);


			//
			//			for(int i=0;i<cTasks.length;i++){
			//				int aidx = s.getVar(cTasks[i]).getVal();
			//				System.out.print(participantsR.get(aidx)+" ");
			//			}
			//			System.out.print("\t"+s.getVar(total).getVal());
			//			System.out.println();

			//			int t=1;
			//			for(ArrayList<Integer> a: ppt){
			//				System.out.println("Task"+t+" contains "+ a.size() +" proposals");
			//				
			//			}
			//			
			//			
			//			
			//			System.out.println("There are "+participants.keySet().size()+" unique participants");


			long etime = System.currentTimeMillis();
			long duration = etime - stime;
			System.out.println("get_team run:"+duration+"ms con:"+constraintCount +" cost:"+cost+" "+tCostMin+"-"+tCostMax);
			return true;

		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}

	private String[] getSubTasks(ListTerm task){
		int tcount = task.size();
		String [] ret = new String[tcount];

		for(int i=0;i<tcount;i++){
			Literal stask = (Literal) task.get(i);

			List<Term> taskTerms = stask.getTerms();
			Atom name = (Atom) taskTerms.get(0);
			ret[i] = name.toString();

		}

		return ret;
	}

	private String[] getDislikesAsString(Literal disl){

		List<Term> disTerms= disl.getTerms();

		Atom as = (Atom) disTerms.get(0);
		Atom at = (Atom) disTerms.get(1);
		ListTerm dt = (ListTerm)((ListTerm) disTerms.get(2)).get(0); //TODO At the moment, only sub lists will work

		String [] ret = new String[3];

		ret[0] = as.toString();
		ret[1] = at.toString();
		ret[2] = getSubTasksAsString(dt);

		return ret;
	}

	private String getSubTasksAsString(ListTerm task){
		String [] taskStrings = getSubTasks(task);
		String single = "";
		for(String s: taskStrings){
			single+=s;
		}
		return single;
	}

	private String getNameFromProposal(Literal proposal){
		List<Term> propTerms = proposal.getTerms();
		Atom name = (Atom) propTerms.get(2);
		String sname = name.toString();
		return sname;
	}
}
