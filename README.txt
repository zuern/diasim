# DIASIM - Dialogue Similiarity
This library offers an extensive API for linguistic testing of similiarity. In order to use this, you have two options
described below. For information on any parts of DIASIM, please refer to the javadoc found in /doc/index.html.

## Option 1: Command Line App & quak.tests
---
This option is the simplest for getting your feet wet with this library. Researchers from [Queen's University at Kingston](http://www.queensu.ca/psychology/speech-perception-and-production-lab)
have developed a command line app and testing toolset that allows you to interact with DIASIM with minimal programming. You can import a directory of text-based transcripts of conversation, parse it, and run some tests on your data using a moving window.

To use the command line app, run the `main()` method in `DIASIM_Testing_Tool`.

### 1. Import a Corpus
You'll need to import your data into a "_.corpus_ " file in order to do anything with DIASIM. You can easily do this using the command line app. Simply put all of your dialogues into seperate text files using the following format:
1. One text file per dialog
2. One line per turn in each text file
2. Each line begins with the timestamp, the SpeakerID, a colon, and the transcript

See `/quak/SampleTranscripts.txt` for an example.

__NOTE: If you have a speaker who appears in multiple dialogs, make sure his/her SpeakerID matches in each of the text files.__

Once this is done, use the Import Corpus option in the menu to import your dialogues. Provide the full path to the directory which your dialogues are in. For example: `C:\Users\TestUser\Data\Dialogues\`.

### 2. Select a Corpus
You must tell the app which corpus to work with. Using the Select Corpus menu, provide the full file path to the corpus. Don't forget to include the file extension.
Example: `C:\Users\TestUser\Data\sample.corpus`

### 3. Running a Test

When running an test you will be presented with many options for configuration. To understand these options (esp. the similiarity measures) and what they do please refer to the javadoc (`/doc/index.html`) for more information.

### Programming Your Own Tests
The command line app uses the testing tool `quak.tests.TestingTools` to run tests. This provides a simple interface to interact with DIASIM. We have also created `quak.tests.TestingConfiguration` to easily create test configs for `quak.tests.TestingTools.runTest()`.

If you want to know what any of the parameters in `TestingConfiguration` do, please refer to their corresponding classes detailed in the code.

###### TestingConfiguration.java

    69: // Selects the windower type found in qmul.window
    70: public static final String WIN_USE_OtherSpeakerTurnWindower         = "oth";

## Option 2: Direct API Programming
---
This option is considerably more advanced, however this method will give you the greatest flexibility and power.

A bit about the source code structure:
* `qmul` = Queen Mary University of London. This is where the majority of DIASIM resides.
* `quak` = Queen's University at Kingston.

#### 1. Import your corpus. Either:
- create a new subclass of `qmul.corpus.DialogueCorpus`. The constructor should read in the data from your external source, and use the existing `addDialogue()`, `addTurn()`, `addSent()` methods to build the corpus. The `main()` method should call this, and save the corpus to file using the existing `saveToFile()` method. See `BNCCorpus`, `SwitchboardCorpus`, `DCPSECorpus` for examples.

- put your external data into a simple text format like that used by `qmul.corpus.TranscriptCorpus` or `qmul.corpus.SwitchboardTranscriptCorpus`, and use that class to read it in (and save it to file, via `main()`). This only works for corpora without syntactic information - these text formats don't understand trees. TranscriptCorpus expects text files with one sentence per line in the following format:

        DialogueActTag SpeakerID_StartTime_EndTime [transcript]

    If you're doing it this way, you can run the TranscriptCorpus class from the command line:

         ./corpus.sh CORPUS_DATA_DIR CORPUS_NAME CORPUS_GENRE

#### 2. Parse your corpus (Optional)
If you are interested in syntactic similarity, but your corpus doesn't already contain syntax trees, you'll need to parse it. The `qmul.corpus.CorpusParser` class can do this, via the Stanford, C&C or RASP parsers. Its methods are static; see the `main()` method in `qmul.corpus.BNCCorpus` for an example of how to call it. Remember to save the corpus to file again once you've parsed it.

#### 3. Pick a Similarity Measure
Decide on the similarity measure you want to use. The existing measures in `qmul.align` provide general lexical and syntactic similarity measures; if that's all you want, you don't need to do anything here except decide which of them to use. If you want something else, though, you'll need to define a new subclass of `qmul.util.similarity.SimilarityMeasure`.

#### 4. Create Random Baseline (Optional)
Do you want to compare your data's observed similarity to a random baseline?
We like to do this. If so, decide on the kind of randomisation of your corpus you want to compare to. The `qmul.corpus.RandomCorpus` class will do this for you, if you're happy with one of the randomisation methods it uses - see definitions and comments in that class. You can randomise the order of all utterances in the corpus; randomise the order of just one speaker's utterances; randomise pairing of speakers while keeping utterance order; and more. Again, remember to save the corpus to file once you've done this.

#### 5. Run your Tests
Now that you have an existing .corpus (or .corpus.gz) file containing your data, use the `main()` method in `qmul.align.AlignmentTester` to run a moving window across your data, calculating similarity between windows. You can define the size of your window; compare windows with previous windows for the same speaker; or with the equivalent window for the other speaker. Window units (for size and step) can be defined in either speaker turns or sentences (where a turn can consist of multiple sentences - this will depend on your data).
You can set these options within the code or as command-line arguments (see the `main()` method and/or the run.sh script).
