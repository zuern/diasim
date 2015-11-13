#!/usr/bin/env bash

CP='bin'
for f in `find lib -name *jar`
do
    CP=$CP:$f
done
java -cp $CP qmul.corpus.UserTranscriptCorpus $1 $2 $3
