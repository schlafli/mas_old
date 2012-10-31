// Agent contractorDF in project test

/* Initial beliefs and rules */

/* Initial goals */



+?who(Role, L):
	true
	<-
	.findall(X, plays(Role, X), L);
	.
	

	
	