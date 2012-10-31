



!start.



{include("participant.asl")}




@start[atomic]
+!start: true
	<-
		.random(X);
		//Some = X/2+0.5;
		Some = 1.0;
		
		Proob = 0.5;
		
		+say_yes(Some);
		.random(X2);
		if(X2 > Proob){
			+cap(drive);
		}
		
		.random(X3);
		if(X3 > Proob){
			+cap(load);
		}
		
		.random(X4);
		if(X4 > Proob){
			+cap(fly);
		}
		
		.random(X5);
		if(X5 > Proob){
			+cap(unload);
		}
		
		
		
		.


