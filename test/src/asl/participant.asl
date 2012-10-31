

plays(directory, contractorDF).
//debug:-true.

answer :-
	.random(X) &
	answer(X).
	
answer(X) :-
	say_yes(Y) &
	Y > X.



have_caps([]) :- true.
have_caps([cap(X)|T]) :-
	cap(X) &
	have_caps(T).
		
		
		
random_price(X) :- .random(R) & X = (20*R)+10.
		



/* Goals */
//plays(initiator,contractor).
//!printname.
//!respond2.
//!haveCaps.


!startedd.

/* Plans */



+!startedd: 
	true 
	<-
	.wait(2000);
	?plays(directory, Dir);
	.send(Dir, askOne, who(contractor, X), who(contractor, A) );
	+plays(contractor, A);
	.
	


+plays(contractor,In)
   :  .my_name(Me)
   
   <- .send(In,tell,introduction(participant,Me)).
   

+!respond : 
	.random(Ass) &
	answer(Ass)
	<-
	.print("Yarp").	
	
+!respond : true <- .print("narp").


+say(X)[source(A)]: true 
	<-
	.print("Agent: ",A, " told me to say: ", X);
	-say(X).
	

+!respond2: true
<-
	if(answer){
		.print("Yes")
	}else{
		.print("no")
	}.
	
	
+!printname : true 
	<-
		.my_name(X);
		?say_yes(Var);
		.print("My name is: ",X, " I will say yes with a prob of:",Var).


+?price(Task, X):
	cost(Task,_)
	<-
	?cost(Task,X).

+?price(Task, X):
	not cost(Task, _)
	<-
	?random_price(X);
	//+cost(Task, X)
	.
	

		

+?total_price([],0).
+?total_price([Task|Tasks], Total):
	true
	<-
	?total_price(Tasks, Tot);
	?price(Task, Offer);
	Total = Offer + Tot;
	.

+startBid(PlanID)[source(A)]:
	true
	<-
	.findall(RoleID, cfp(PlanID, RoleID, _, _), Roles);
	!refresh_cfp(PlanID, Roles);
	.


+!refresh_cfp(PlanID, [RoleID|T]):
	true
	<-
	?cfp(PlanID, RoleID, Cap, Task)[source(X)];
	-cfp(PlanID, RoleID, Cap, Task)[source(X)];
	+cfp(PlanID, RoleID, Cap, Task)[source(X)];
	!refresh_cfp(PlanID, T)
	.
	
+!refresh_cfp(PlanID, []).

+cfp(PlanID, RoleID, Caps, Tasks)[source(A)] : 
	startBid(PlanID)[source(A)] &
	have_caps(Caps) &
	answer
<- 
	if(debug){
		.print("Tasks: ", Tasks);
	}
	.my_name(Name);
	if(answer){
		.concat(PlanID, " ", RoleID, Task);
		?total_price(Tasks, Offer);
		+proposal(PlanID, RoleID, Offer);
		.send(A, tell, propose(PlanID, RoleID, Name, Offer));
		//Respone yes
	} else {
		//Respond no
		.send(A, tell, refuse(PlanID, RoleID, Name));
		!remove_cfp(PlanID,RoleID);
	}.
	
+cfp(PlanID, RoleID, Caps, Tasks)[source(A)]:	
	startBid(PlanID)[source(A)]
	<-
	//Respond no
	.my_name(Name);
	.send(A, tell, refuse(PlanID, RoleID, Name));
	!remove_cfp(PlanID,RoleID);
	.
	
+cfp(PlanID, RoleID, Caps, Tasks)[source(A)]:
	not startBid(PlanID)[source(A)]
	<-
	//.print("waiting");
	true
	.


+gtl(PlanID, LineUp)[source(A)] :
	true
	<-
	?complaints(LineUp, [], Complaints);
	.my_name(Name);
	if(.length(Complaints,0)){
		.send(A, tell, aok(PlanID,Name));
	}else{
		.send(A, tell, problems(PlanID,Name,Complaints));
	}
	-gtl(PlanID, LineUp)[source(A)];
	.


+?complaints([[Member|Task]| LineUp], Temp, Complaints):
	true
	<-
	?check_member(Member, Task, Comp);
	if(.length(Comp, 0)){
		?complaints(LineUp, Temp, Complaints);
	}else{
		?complaints(LineUp, [Comp|Temp], Complaints);
	}

	.
+?complaints([], X, X).



+?check_member(Member, _, Complaint):
		.my_name(Member)
	<-
		Complaint = [].

+?check_member(Member, Task, Complaint):
		trust(Member, Task, X)
	<-
		if(X == no){
			Complaint = [Member, Task];
		}else{
			Complaint = [];
		}
		.
		
+?check_member(Member, Task, Complaint):	
		not trust(Member, Task, _)
	<-
		.random(X);
		if(X>0.40){
			+trust(Member, Task, yes);
			Complaint = [];
		}else{
			+trust(Member, Task, no);
			Complaint = [Member, Task];
		}.


+won_role(PlanID, RoleID, Tasks)[source(A)]:
	true
	<-
	-won_role(PlanID, RoleID, Tasks)[source(A)];
	+have_role(PlanID, RoleID, Tasks, A);
	if(debug){
		.my_name(Name);
		.print("I, ", Name," have role: ", RoleID," in plan: ", PlanID," consisting of tasks:",Tasks);
	}
	!remove_cfp(PlanID, RoleID);
	.

+run_step(PlanID, Step):
	have_role(PlanID, Step, Tasks, _)
	<-
	
	
	//RUN TASKS
	
	//CLEANUP
	-run_step(PlanID, Step)[source(_)];
	-have_role(PlanID, Step, Tasks, _);
	-proposal(PlanID, Step, _);
	//RESPOND DONE/FAIL
	.
	
+run_step(PlanID, Step):
	true
	<-
	.print("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
	//.wait(10000);
	.

+!remove_cfp(PlanID,RoleID):
	true
	<-
	-cfp(PlanID,RoleID,_,_)[source(_)];
	.

+reject_proposal(PlanID, RoleID)[source(A)]:
	true
	<-
	//.print("I lost")
	-reject_proposal(PlanID, RoleID)[source(A)];
	-proposal(PlanID, RoleID, _);
	!remove_cfp(PlanID,RoleID);
	.
	