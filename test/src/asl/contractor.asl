// Agent contractor in project test

/* Initial beliefs and rules */


plays(directory, contractorDF).

planID(0). 
running_plans(0).
limit(1).
maxInterations(200).

distinct([],[]).
distinct([H|T],C) :- .member(H,T) & distinct(T,C).
distinct([H|T],[H|C]) :- distinct(T,C).


intersection([X|Xt],Y,[X|Z]):- .member(X,Y) & intersection(Xt,Y,Z). 
intersection([X|Xt],Y,Z):- not .member(X,Y) & intersection(Xt,Y,Z). 
intersection([], Y, []).

//debug:-true.


getTasks(Plan, Tasks):- getTasks_1(Plan, [], TasksTmp) & reverse(TasksTmp, [], Tasks).

getTasks_1([], P, P).
getTasks_1([[_|[Task]]|T], Tmp, Tasks):- getTasks_1(T, [Task|Tmp], Tasks).


reverse([X|Y],Z,W) :- reverse(Y,[X|Z],W).
reverse([],X,X).


all_team_members_responded(PlanID)
:- 	.count(aok(PlanID,_),OK) &
	.count(problems(PlanID,_,_),Prob) &
	team_ass(PlanID, Team) &
	distinct(Team, Individuals) &
	.length(Individuals, Ln) &
	Ln = (OK+Prob).
	
/*
all_proposals_received(PlanID) 
  :- .count(introduction(participant,_),NP) &  	   // number of participants
     .count(propose(PlanID,_,_,_), NO) &           // number of proposes received
     .count(refuse(PlanID,_,_), NR) &              // number of refusals received
   	 plan(PlanID, Plan, _) &
   	 .length(Plan, Ln) &
     (NP * Ln) = (NO + NR ).
*/

all_proposals_received(PlanID):-
	.count(propose(PlanID,_,_,_), NO) &           // number of proposes received
    .count(refuse(PlanID,_,_), NR) &
     total_msgs(PlanID, Tot)  &
     Tot = (NO+NR).



!contractor_start.



/* Initial goals */

/* Plans */

+!contractor_start:
	true
	<-
	//************
	//FILE PREFIX
	//************
	
	env.start_log(def);
	
	
	?plays(directory, Dir);
	.my_name(Me);
	.send(Dir, tell, plays(contractor, Me));
	.at("now +5 seconds", { +!ttc}); //allow 5 seconds so everyone can register	
	.

+!ttc:
	true
	<-
	?planID(Pidc);
	?maxInterations(Ic);
	
	if(Pidc>=Ic){
	
	//************
	//TODO CHANGE IF MULTIPLE PLANS CAN BE CONTRACTED AT ONCE!!
	//************
		.print("Shutting Down!!!");
		
		.wait(2000);
		env.stop_log(exit_nice);
		.stopMAS;
	}else{
	
	?running_plans(Value);
	?limit(Lim);
	if(Value<Lim){
	
	!fill_plan("Move-crate",
	
	[
	[[cap(fly)],	[task(move)]],
//	[[cap(fly)],	[task(move2)]],
//	[[cap(drive)],	[task(move3)]],
//	[[cap(fly)],	[task(move4)]],
	[[cap(drive)],	[task(move5)]],
	[[cap(load)],	[task(lift)]],
	[[cap(unload)],	[task(lift2)]]
	]
	
	);	
	}
	
	.at("now +150 milliseconds", { +!ttc});
	
	}
	.


@ipidc [atomic]
+?newPlanId(ID): true
	<-
	?planID(N);
	-planID(N);
	ID = N + 1;
	+planID(ID);
	.
	
@fp[atomic]
+!fill_plan(PlanName, Plan)[source(A)]
	<-
	?newPlanId(NewID);
	.my_name(MyName);
	.concat("",PlanName,"_",MyName,"_", NewID, PlanID);
	if(debug){
		.print("Plan id = ", PlanID);
	}
	+tcnp_stage(PlanID, started);
	+plan_steps(PlanID, 0);
	?getTasks(Plan, Tasks);
	+plan(PlanID, Plan, Tasks);
	if(debug){
		.print("List of tasks: ", Tasks);
	}

	.length(Tasks, PlanLength);
	.findall(AName, introduction(participant,AName),ALP);	
	.length(ALP, NumberOfPeople);
	
	Tot = NumberOfPeople * PlanLength; 
	//.print(Tot);
	+total_msgs(PlanID, Tot);
	
	-tcnp_stage(PlanID, started);
	+tcnp_stage(PlanID, propose);

	
	
	!advertise_roles(Plan, PlanID);


	
	.at("now +200 milliseconds", { +!say_go(PlanID)});// tell the agents to start bidding in a second
	
	.at("now +10 seconds", { +!contract_roles(PlanID)});// Needed in 'real' situations in case some agents do not reply
	.

+!say_go(PlanID):
	true
	<-
	.findall(Name, introduction(participant,Name),LP);
	.send(LP, tell, startBid(PlanID));
	.
	

+propose(PlanID, RoleID, Name, Bid)
	<-
	if(debug){
		.print("Agent: {",Name ,"} can do task {", RoleID, "} in plan {", PlanID,"} bid {",Bid,"}");
	}
	if(all_proposals_received(PlanID)){
		!contract_roles(PlanID);
	}
	.

	

+refuse(PlanID, RoleID, Name)
	<-
	if(debug){
		.print("Agent: {",Name ,"} can't do task {", RoleID, "} in plan {", PlanID,"}");
	}
	
	if(all_proposals_received(PlanID)){
		!contract_roles(PlanID);
	}
	.


+!advertise_roles([], PlanID).

+!advertise_roles([[Caps, Steps]|T], PlanID) 
	<- 
	?plan_steps(PlanID, StepID);
	-plan_steps(PlanID, StepID);
	NextStep = StepID + 1;
	+plan_steps(PlanID, NextStep);
	.findall(Name, introduction(participant,Name),LP);
	.send(LP, tell, cfp(PlanID, NextStep, Caps, Steps));
	!advertise_roles(T, PlanID).
	
	
@lc1[atomic]
+!contract_roles(PlanID):
	tcnp_stage(PlanID, propose)
	<-
	-tcnp_stage(PlanID, propose);
	+tcnp_stage(PlanID, contract);
	.at("now +100 milliseconds", { +!contract_roles2(PlanID)});
	.

@lc2[atomic]
+!contract_roles(PlanID):
	
	not tcnp_stage(PlanID, propose)
	<-
	//.print("hmm");
	true;
	.
	
	
+!contract_roles2(PlanID):
	tcnp_stage(PlanID, contract)
	<-
	.findall(X, propose(PlanID, X, _, _), L);
	?distinct(L, Uni);
	if(debug){
		.print("Before:",L," After:",Uni);
	}
	?plan_steps(PlanID, Steps);
	.length(Uni, Len);
	if(Len==Steps){
		if(debug){
			.print("Have all!!");
		}
		!start_team_val(PlanID);
	}else{
		if(debug){
			.print("Can't do the task!!");
		}
		-tcnp_stage(PlanID, contract);
		!reject_all(PlanID);
		!remove_refusals(PlanID);
		+tcnp_stage(PlanID, failed);
	}
	.

	
+!remove_refusals(PlanID):
	not refuse(PlanID, _, _)
	<-
	true.	
	
+!remove_refusals(PlanID):
	refuse(PlanID, RoleID, Name)
	<-
	-refuse(PlanID, RoleID, Name)[source(_)];
	!remove_refusals(PlanID);
	.

+!reject_all(PlanID):
	not propose(PlanID, _, _, _)
	<-
	true.
	
+!reject_all(PlanID):
	 propose(PlanID, TaskID, Name, _)
	<-
	.send(Name, tell, reject_proposal(PlanID, TaskID));
 	-propose(PlanID, TaskID, Name, _)[source(_)]; //could be kept for record
 	!reject_all(PlanID);
	.
	
+!reject_not_in_team(PlanID):
	true
	<-
	!reject_all(PlanID);
	!remove_refusals(PlanID);
	.



+!create_lists(PlanID):
	plan_steps(PlanID, Steps)
	<-
	!create_list(PlanID, Steps);
	.

+!create_list(_, 0).
+!create_list(PlanID, Step):
	true
	<-
	.findall(Offer, propose(PlanID, Step, _, Offer), AllBids);
	.sort(AllBids, Sorted);

	!create_list(PlanID, Step-1);
	.
	

@stv1
+!start_team_val(PlanID):
	tcnp_stage(PlanID, contract)
	<-
	-tcnp_stage(PlanID, contract);
	+tcnp_stage(PlanID, selection);
	//!create_lists(PlanID);
	!get_team(PlanID, Team, LineUp, Success);
	if(Success=yes){
		+team_ass(PlanID, Team); //add the list of agents(so the contractor can check)
		!reject_not_in_team(PlanID);
		!remove_refusals(PlanID);
		.send(Team, tell, gtl(PlanID, LineUp));
	}else{
		!reject_all(PlanID);
		!remove_refusals(PlanID);
		.print("Plan not possible!");
		
		.wait(2000);
		env.stop_log(exit_plan_not_possible);
		.stopMAS;		


	}
	.

@stv2
+!start_team_val(_).	

	
+!get_team(PlanID, Team,  LineUp, Success):
	true
	<-
	?plan_steps(PlanID, Steps);
	!recfill(PlanID, Steps, [], Team, [], LineUp);
	Success=yes;
	!check_known_dislikes(Team, LineUp, Dislikes);
	
	.
	
	

+!recfill(PlanID, 0, A, A, B, B).
+!recfill(PlanID, Step, Team, Ret, LineUp, RetA):
	true
	<-
	.findall(offer(O,A), propose(PlanID, Step, A, O), L);
	.min(L, offer(Wo, Wa)); 
	-propose(PlanID, Step, Wa, _)[source(_)];
	+team_member(PlanID, Step, Wa, Wo);
	?plan(PlanID, _, Tasks);
	.nth(Step-1, Tasks, Task);
	!recfill(PlanID, Step-1, [Wa|Team], Ret, [[Wa|Task]|LineUp], RetA);
	.
	
	
 +aok(PlanID,Name)
 	<-
 	if(all_team_members_responded(PlanID)){
		!finalize_assignment(PlanID);
	}
	.
	

+problems(PlanID,Name,Complaints)
	<-
	!rec_add_dislikes(Name, Complaints);
	if(all_team_members_responded(PlanID)){
		!finalize_assignment(PlanID);
	}
	.	
	
//@fin1[atomic]	
+!finalize_assignment(PlanID):
	tcnp_stage(PlanID, selection)
	<-
	-tcnp_stage(PlanID, selection);
	+tcnp_stage(PlanID, finalize);
	.count(problems(PlanID,_,_), Probs);
	if(Probs>0){
		!team_complaints(PlanID);
	}else{
		!accept_winners(PlanID);
		!remove_aok(PlanID);
		!execute_plan(PlanID);
	}
	.
	
@fin2
+!finalize_assignment(_).


@tc1
+!team_complaints(PlanID):
	tcnp_stage(PlanID, finalize)
	<-
	-tcnp_stage(PlanID, finalize);
	+tcnp_stage(PlanID, failed);
	!reject_winners(PlanID);
	-team_ass(PlanID, _);
	!remove_prob(PlanID);
	!remove_aok(PlanID);
	.
	
@tc2
+!team_complaints(PlanID).
	
	

+!rec_add_dislikes(Dis, []).
+!rec_add_dislikes(Dis, [[A|Tasks]|T]):
	true
	<-
	!rec_add_dislikes2(Dis, A, Tasks);
	!rec_add_dislikes(Dis, T);
	.
	
+!rec_add_dislikes2(Dis, A, []).
+!rec_add_dislikes2(Dis, A, [Task|T]):
	true
	<-
	+dislikes(Dis, A, Task);
	!rec_add_dislikes2(Dis, A, T);
	.


+!accept_winners(PlanID):
	true
	<-
	.findall(ms(RoleID,A), team_member(PlanID, RoleID, A, _), L);
	!rec_accept(PlanID, L);
	.

+!rec_accept(PlanID, []).
+!rec_accept(PlanID, [ms(RoleID,A)|T]):
	true
	<-
	
	?plan(PlanID, _, Tasks);
	.nth(RoleID-1, Tasks, Task);
	.send(A, tell, won_role(PlanID, RoleID, Task));
	!rec_accept(PlanID, T);
	.



+!reject_winners(PlanID):
	true
	<-
	.findall(ms(RoleID,A), team_member(PlanID, RoleID, A, _), L);
	!rec_reject(PlanID, L);
	.

+!rec_reject(PlanID, []).
+!rec_reject(PlanID, [ms(RoleID,A)|T]):
	true
	<-
	-team_member(PlanID, RoleID, A, _);
	.send(A, tell, reject_proposal(PlanID, RoleID));
	!rec_reject(PlanID, T);
	.


+!remove_aok(PlanID):
	not aok(PlanID, _)
	<-
	true.
	
+!remove_aok(PlanID):
	 aok(PlanID, Name)[source(A)]
	<-
 	-aok(PlanID, Name)[source(A)];
	!remove_aok(PlanID);
	.
	
+!remove_prob(PlanID):
	not problems(PlanID,_,_)
	<-
	true.
	
+!remove_prob(PlanID):
	problems(PlanID,Name,_)[source(A)]
	<-
 	-problems(PlanID,Name,_)[source(A)];
	!remove_prob(PlanID);
	.
	
+!plan_run_step(PlanID, Step, Step).
+!plan_run_step(PlanID, Step, Total):
	true
	<-
	NStep = Step+1;
	//.print("At step:",NStep," of:",Total);
	?team_member(PlanID, NStep, Ag, _);
	.send(Ag, tell, run_step(PlanID, NStep));
	.wait(50);
	!plan_run_step(PlanID, NStep, Total);
	.
	
@ex1
+!execute_plan(PlanID):
	tcnp_stage(PlanID, finalize)
	<-
	-tcnp_stage(PlanID, finalize);
	+tcnp_stage(PlanID, running);
	
	//.print("Running");
	?plan_steps(PlanID, Total);
	
	.wait(500);
	!plan_run_step(PlanID, 0, Total);
	.wait(500);
	
	//remove refs
		
	-tcnp_stage(PlanID, running);
	+tcnp_stage(PlanID, done);
	.

@ex2
+!execute_plan(_).


+!remove_member_ref(PlanID):
	not team_member(PlanID, _,_,_)
	<-
	true.
	
+!remove_member_ref(PlanID):
	team_member(PlanID,Step,_,_)
	<-
 	-team_member(PlanID,Step,_,_);
	!remove_member_ref(PlanID);
	.
	
+tcnp_stage(PlanID, State):
	true
	<-
	if(State == done){
		.print("Finished:\"",PlanID,"\"");
		!dec_counter(Count);
		!cleanup(PlanID);
		-tcnp_stage(PlanID,_);
		-total_msgs(PlanID, _);
		env.add_result(yes);
	}
	if(State == failed){
		//.print("Failed:\"",PlanID,"\"");
		!dec_counter(Count);
		!cleanup(PlanID);
		-tcnp_stage(PlanID,_);
		-total_msgs(PlanID, _);
		env.add_result(no);
	}
	if(State == started){
		!inc_counter(Count);
	}
	.
	
@inc1[atomic]
+!inc_counter(Count):
	true
	<-
	?running_plans(Value);
	-running_plans(Value);
	Count = Value+1;
	+running_plans(Count);
	.
	
@dec1[atomic]
+!dec_counter(Count):
	true
	<-
	?running_plans(Value);
	-running_plans(Value);
	Count = Value-1;
	+running_plans(Count);
	.
	
	
+!cleanup(PlanID):
	true
	<-
	!remove_member_ref(PlanID);
	
	-plan(PlanID,_,_);
	-plan_steps(PlanID,_);
	-team_ass(PlanID,_);
	.
	

+!get_team_2(PlanID, Team, LineUp, Success):
	true
	<-
	
	.findall(propose(PlanID,S,C,P), propose(PlanID,S,C,P), Proposals);
	.findall(dislikes(A,B,T), dislikes(A,B,T), Dislikes);
	?plan(PlanID, Plan, Tasks);
	
	env.get_team(Tasks, Proposals, Dislikes, Team, LineUp, PropsToDel, Success);
	
	!swap_props(PropsToDel);
	.

+!swap_props([]).
+!swap_props([propose(PlanID, Step, Agent, Offer)|T]):
	true
	<-
	-propose(PlanID, Step, Agent, Offer)[source(_)];
	+team_member(PlanID, Step, Agent, Offer);
	!swap_props(T);
	
	.
	


+!check_known_dislikes(Team, [], []).
+!check_known_dislikes(Team, [[Agent|Task]|LineUp], Dis):
	true
	<-
	!check_known_dislikes(Team, LineUp, Dislikes);
	
	!get_dislikers(Agent, Task, Team, Dislikers);
	
	if(.length(Dislikers, 0)){
		Dis = Dislikes;
	}else{
		Temp = [Dislikers, Agent, Task];
		if(.length(Dislikes,0)){
			Dis = [Temp];
		}else{
			Dis = [Temp,Dislikes];
		}
	}
	.
	

	
+!get_dislikers(Agent, Task, Team, Dislikers):
	true
	<-
	.findall(X, dislikes(X,Agent,Task), Dis);
	?intersection(Dis, Team, Dislikers);
	.


