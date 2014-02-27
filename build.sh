#!/usr/bin/env bash

mkdir bin
CP='src:bin'
for f in `find lib -name *jar`
do
    CP=$CP:$f
done
echo $CP
for f in `find src -name *java`
do
    javac -d bin -cp $CP $f
done
