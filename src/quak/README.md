Queen's University at Kingston Ontario
Speech Lab - Department of Psychology
Author: Kevin Zuern
--------------------------------------
# HOW TO USE THIS MODULE
--------------------------------------
This module creates a really simple way of utilizing the incredibly powerful DIASIM library - specifically using it to
run AlignmentTests.

1.
    You need your data to be recorded in text file transcripts. The files must be using the format shown in
    SampleTranscript.txt.
    You can use quak.FileFormatter to help you format your files.
2.
    Once you have formatted your files, you have to place them all in one directory.
    Run quak.tests.TestingTools.createCorpus, and specify the File where you want your corpus to be saved.

    It should be saved as "SomeCorpusName.corpus"
3.
    If you're interested in syntactic similarity, use the quak.tests.TestingTools.parseCorpus method to parse the syntax
    in your corpus using the Stanford Parser. The Stanford Parser will use default settings to do the parsing.

4.
    Once you are ready to run a test, use quak.tests.TestingConfiguration.create to create an object to hold the parameters
    for the test you want to run. Pass this in to quak.tests.TestingTools.runSingleTest to run your test, or alternately you
    can create an array of TestConfigurations and run multiple tests in a batch.

    Note that the available parameters for the TestConfiguration are stored as static final variables in the
    TestConfiguration class.


If you have any trouble using these classes, please refer to the DocStrings in the code, or generate a JavaDoc to view
details.