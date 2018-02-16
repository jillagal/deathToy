#!/usr/bin/env bash
[ -d ../classes ] && rm -r ../classes
[ -d ../results ] && rm -r ../results 


mkdir ../classes
javac Main.java -d ../classes
cd ../classes

cT=0; #tradeoff type (0=none, 1=convex, 2=concave)
dR=10000; #death rate; (0=no death or 1 death per dR cells per time frame)
cR=100; # catastrophe rate (% of deaths)

java Main $cT $dR $cR
			


