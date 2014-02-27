#!/usr/bin/env bash

CP='bin'
for f in `find lib -name *jar`
do
    CP=$CP:$f
done
java -cp $CP qmul.align.AlignmentTester -Cbnc -Rrandom1 -Slex -Utuco -Woth -M9
