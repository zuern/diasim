1. Import your corpus. Either:

   a) create a new subclass of qmul.corpus.DialogueCorpus. The constructor
   should read in the data from your external source, and use the existing
   addDialogue(), addTurn(), addSent() methods to build the corpus. The main()
   method should call this, and save the corpus to file using the existing
   saveToFile() method. See BNCCorpus, SwitchboardCorpus, DCPSECorpus for
   examples.

   b) put your external data into a simple text format like that used by
   qmul.corpus.TranscriptCorpus or qmul.corpus.SwitchboardTranscriptCorpus, and
   use that class to read it in (and save it to file, via main()). This only
   works for corpora without syntactic information - these text formats don't
   understand trees. TranscriptCorpus expects text files with one sentence per
   line in the following format:
   
     DialogueActTag SpeakerID_StartTime_EndTime [transcript]

   If you're doing it this way, you can run the TranscriptCorpus class from the
   command line:

     ./corpus.sh CORPUS_DATA_DIR CORPUS_NAME CORPUS_GENRE

2. If you are interested in syntactic similarity, but your corpus doesn't
   already contain syntax trees, you'll need to parse it. The
   qmul.corpus.CorpusParser class can do this, via the Stanford, C&C or RASP
   parsers. Its methods are static; see the main() method in
   qmul.corpus.BNCCorpus for an example of how to call it. Remember to save the
   corpus to file again once you've parsed it.

3. Decide on the similarity measure you want to use. The existing measures in
   qmul.align provide general lexical and syntactic similarity measures; if
   that's all you want, you don't need to do anything here except decide which
   of them to use. If you want something else, though, you'll need to define a
   new subclass of qmul.util.similarity.SimilarityMeasure.

4. Do you want to compare your data's observed similarity to a random baseline?
   We like to do this. If so, decide on the kind of randomisation of your corpus
   you want to compare to. The qmul.corpus.RandomCorpus class will do this for
   you, if you're happy with one of the randomisation methods it uses - see
   definitions and comments in that class. You can randomise the order of all
   utterances in the corpus; randomise the order of just one speaker's
   utterances; randomise pairing of speakers while keeping utterance order; and
   more. Again, remember to save the corpus to file once you've done this.

4. Now that you have an existing .corpus (or .corpus.gz) file containing your
   data, use the main() method in qmul.align.AlignmentTester to run a moving
   window across your data, calculating similarity between windows. You can
   define the size of your window; compare windows with previous windows for the
   same speaker; or with the equivalent window for the other speaker. Window
   units (for size and step) can be defined in either speaker turns or sentences
   (where a turn can consist of multiple sentences - this will depend on your
   data). You can set these options within the code or as command-line arguments
   - see the main() method and/or the run.sh script.
