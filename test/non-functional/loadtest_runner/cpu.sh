#!/bin/bash
# $1 - log path
# $2 - host to monitor (ssh into) 

while true; do
	cpu=`ssh $2 -f top -b -n 1 | grep java | head -n 1 | awk {'print $9'}`
	load=`ssh $2 -f w | grep "load" | grep -v "grep" | awk {'print $10 '}`
	
	t=`date` 

	echo "${t};${cpu};${load}" >> $1
	sleep 2
done
