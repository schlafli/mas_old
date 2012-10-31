#!/bin/bash

export CLASSPATH=$CLASSPATH:`./classpath.sh`


for (( c=1; c<=$1; c++ ))
do
	echo "Run $c of $1"
	java jason.mas2j.parser.mas2j /home/schlafli/workspaces/masworkspace/test/test.mas2j run > /dev/null
	sleep 2s
done



