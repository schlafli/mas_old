// Agent testagent in project test

/* Initial beliefs and rules */


/* Initial goals */




distinct([],[]).
distinct([H|T],C) :- .member(H,T) & distinct(T,C).
distinct([H|T],[H|C]) :- distinct(T,C).

!start.


/* Plans */

+!start : true <- ?distinct([a,b,c,b],X);  .print("Yup.", X).
+!start : .print("Nope.").


+?unique_items(X , Y ): 
	true 
	<- 
	if(.list(X)){
		N = 1;
		.length(X, Len);
		.sort(X, Xsort);
		.nth(0,X,Last);
		.concat([Last], Y);
		
		while(N<Len){
			.print("Got here");
			.nth(N,X,Last);
			.print("Got here");
			if(.member(Last, Y)){
				
			}else{
				.concat([Last], Y);
			}
			N = N+1;
			
		}
	}
	.
