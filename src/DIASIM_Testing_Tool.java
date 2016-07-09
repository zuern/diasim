import quak.tests.TestingConfiguration;
import quak.tests.TestingTools;

import java.io.*;

/**
 * Run this class and use it on the command line to run tests of alignment on quak.corpus.TextCorpus corpora.
 * You can also use this class to create and parse corpora for syntax.
 */
public class DIASIM_Testing_Tool {

    /**
     * Enables reading System.in for user input.
     */
    public static final BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
    public static File corpusFile;

    public static void main(String args[]) {
        header("DIASIM Testing Tool");
        p("Written by Kevin Zuern (Queen's University at Kingston)");
        p("Makes it easy to use DIASIM to process text-based transcripts");
        p("Press enter to continue");
        WaitForEnter();
        Main_Menu();
    }

    private static void Main_Menu() {
        header("Main Menu");

        int selection = selectMenu(new String[]{
                "Select a corpus to work with",
                "Create Corpus from text files (Default)",
                "Parse a corpus (Generates syntax using standford parser",
                "Run a test on a loaded corpus",
                "Exit the program"
        });

        switch (selection){
            case 0:
                Menu_SelectCorpus();
                break;
            case 1:
                Menu_CreateCorpus();
                break;
            case 2:
                Menu_ParseCorpus();
                break;
            case 3:
                Menu_RunTest();
                break;
            case 4:
                System.exit(0);
            default:
                p("Invalid selection. Please try again");
                Main_Menu();
                return;
        }
    }

    private static void Menu_CreateCorpus() {
        File corpusFilePath;
        File textTranscriptsDir;

        header("Create a Corpus File");

        p("What format are the files?");
        p("[");

        corpusFilePath = new File(readString("Please enter the full path to where the corpus should be saved"));
        textTranscriptsDir = new File(readString("Please enter the folder path where the transcripts are located."));
        p("Please press enter to create your corpus.");
        WaitForEnter();
        TestingTools.CreateCorpus(textTranscriptsDir,corpusFilePath);
        p("Corpus created, and saved to " + corpusFilePath);


        p("Selecting this corpus in the menu.");
        corpusFile = corpusFilePath;

        p("Returning to main menu");
        Main_Menu();
    }

    private static void Menu_ParseCorpus() {
        header("Parse Corpus Syntax");

        ConfirmSelectedCorpus();
        p("Please press enter to parse the corpus (may take a while)");
        WaitForEnter();
        p("Parsing corpus now...");
        try {
            TestingTools.ParseCorpus(corpusFile);
        }
        catch (RuntimeException e) {
            System.err.println("Could not run the parser");
            p("An error occurred. This is usually caused by the stanford parser not being able to find the \"englishPCFG.ser.gz\" file.");
            p("An easy way to fix this issue is to make sure you have this file in /data/lib/stanford-parser/2010-08-20/englishPCFG.ser.gz");
            p("If you're still having this issue, you should make sure the working directory of this program is the root of the project");
            p("This means you should run this program in the parent folder of /lib/");
        }
        Main_Menu();
    }

    private static void Menu_SelectCorpus() {
        header("Select a corpus");
        corpusFile = new File(
                readString("Please type the full file path to the corpus (including the filename and extension)",true));
        if (!corpusFile.exists()) {
            p("The specified file does not exist. Did you mis-spell the file path?");
            p("Press enter to try again:");
            WaitForEnter();
            Menu_SelectCorpus();
            return;
        }
        p("Selected the corpus located at: " + corpusFile.getAbsolutePath());
        Main_Menu();
    }

    private static void Menu_RunTest() {
        header("Run a Test");
        p("If you are unsure of some of the settings, please refer to the documentation");

        ConfirmSelectedCorpus();

        // Set the base directory for the test (where the corpus is)
        String baseDir    = corpusFile.getParent() + "\\";
        // Set the file name (excluding the file extension) of the corpus.
        String corpusRoot = corpusFile.getName().substring(0,corpusFile.getName().lastIndexOf('.'));

        //
        // BEGIN GETTING PARAMETERS FOR THE TEST
        //
        String randtype,simtype,wintype,unittype;
        int montecarlo = -1;
        boolean xlsoutput,plotgraphs;

        // Randomization
        header("Randomization Options");
        int randomType = selectMenu(new String[] {
                "Random1",
                "Random2",
                "Random3",
                "Random4",
                "Random5",
                "Random_Same",
                "Random_S2me",
        });

        switch (randomType)
        {
            case 1:
                randtype = TestingConfiguration.RAND_Random1;
                break;
            case 2:
                randtype = TestingConfiguration.RAND_Random2;
                break;
            case 3:
                randtype = TestingConfiguration.RAND_Random3;
                break;
            case 4:
                randtype = TestingConfiguration.RAND_Random4;
                break;
            case 5:
                randtype = TestingConfiguration.RAND_Random5;
                break;
            case 6:
                randtype = TestingConfiguration.RAND_Random_Same;
                break;
            case 7:
                randtype = TestingConfiguration.RAND_Random_S2me;
                break;
            default:
                p("Invalid selection made. Returning to main menu.");
                Main_Menu();
                return;
        }

        // Similarity
        header("Similarity Measures");
        int simType = selectMenu(new String[] {
                "Sentence Lexical",
                "Sentence Lexical Token",
                "Sentence Syntactic",
                "Sentence Last Construction",
        });

        switch(simType) {
            case 0:
                simtype = TestingConfiguration.SIM_SENTENCE_LEXICAL;
                break;
            case 1:
                simtype = TestingConfiguration.SIM_SENTENCE_LEXICAL_TOKEN;
                break;
            case 2:
                simtype = TestingConfiguration.SIM_SENTENCE_SYNTACTIC;
                break;
            case 3:
                simtype = TestingConfiguration.SIM_SENTENCE_LAST_CONSTRUCTION;
                break;
            default:
                p("Invalid selection made. Returning to main menu");
                Main_Menu();
                return;
        }

        // Windowing
        header("Windower Types");
        int windowType = selectMenu(new String[] {
                "Other Speaker Turn Windower",
                "Same  Speaker Turn Windower",
                "Other Speaker All Other Turn Windower",
                "Same  Speaker All Other Turn Windower",
                "Sentence Windower",
        });

        switch (windowType) {
            case 0:
                wintype = TestingConfiguration.WIN_USE_OtherSpeakerTurnWindower;
                break;
            case 1:
                wintype = TestingConfiguration.WIN_USE_SameSpeakerTurnWindower;
                break;
            case 2:
                wintype = TestingConfiguration.WIN_USE_OtherSpeakerAllOtherTurnWindower;
                break;
            case 3:
                wintype = TestingConfiguration.WIN_USE_SameSpeakerAllOtherTurnWindower;
                break;
            case 4:
                wintype = TestingConfiguration.WIN_USE_SentenceWindower;
                break;
            default:
                p("Invalid Selection made. Returning to main menu");
                Main_Menu();
                return;
        }

        // Units
        header("Unit Type");
        int unitType = selectMenu(new String[] {
                "Measure Units in Sentences",
                "Measure Units in Turns (TurnAverageSimiliarityMeasure)",
                "Measure Units in Turns (TurnConcatSimiliarityMeasure",
        });
        switch(unitType) {
            case 0:
                unittype = TestingConfiguration.UNIT_USE_SENTENCES;
                break;
            case 1:
                unittype = TestingConfiguration.UNIT_USE_TurnAverageSimilarityMeasure;
                break;
            case 2:
                unittype = TestingConfiguration.UNIT_USE_TurnConcatSimilarityMeasure;
                break;
            default:
                p("Invalid selection made. Returning to main menu");
                Main_Menu();
                return;
        }

        // Monte Carlo
        header("Monte Carlo");
        while (montecarlo < 0)
            montecarlo = readInt("Please enter a Natural Number for Monte Carlo (0,1,2,...)");

        header("Results Format");

        // Excel
        if (yesno("Print Results to an Excel 2013 Spreadsheet? (*.xlsx)"))
            xlsoutput = true;
        else
            xlsoutput = false;

        // Graphs
        if (yesno("Display graphs upon completion of experiment?"))
            plotgraphs = true;
        else
            plotgraphs = false;

        //
        // BEGIN CONFIRMATION AND RUN EXPERIMENT
        //

        header("Ready to Begin Experiment");
        p("We are ready to run the experiment with the following settings:");
        System.out.println(String.format(
                "Random Type:         %s\n" +
                "Similiarity Measure: %s\n" +
                "Windower:            %s\n" +
                "Unit Type:           %s\n" +
                "Monte Carlo:         %d\n" +
                "Output Excel:        %s\n" +
                "Plot Graphs:         %s",randtype,simtype,wintype,unittype,montecarlo,xlsoutput,plotgraphs));

        header("Run Experiment");
        if (yesno("Ready to run experiment?"))
            TestingTools.runTest(
                TestingConfiguration.create(
                        baseDir,
                        corpusRoot,
                        randtype,
                        simtype,
                        unittype,
                        wintype,
                        montecarlo,
                        xlsoutput,
                        plotgraphs
                ));

        header("Experiment Complete");
        if (xlsoutput)
            p("Your results are saved in excel files here: \"" + System.getProperty("user.dir") + "\"");
        Main_Menu();
    }


    // HELPER FUNCTIONS

    /**
     * Print something to the console.
     */
    private static void p(String s) {
        System.out.println(s);
    }
    /**
     * Print a header to the console
     * @param s
     */
    private static void header(String s) {
        // What the borders are made of
        String borderChar = "-";
        // How long you want the border to be
        int l = 30;

        int pad = (l-s.length())/2;

        String border = "";
        for (int i = 0; i < l+1; i++)
            border = border + borderChar;

        String spaces = ""; //
        for (int i = 0; i < pad; i++)
            spaces = spaces + " ";

        p(border + "\n" + borderChar + spaces + s + spaces + borderChar+ "\n" + border);
    }
    /**
     * Get an input string from the user
     * @param message Gets displayed to the user
     * @param confirm If true, will confirm the user's input before returning
     * @return
     */
    private static String readString(String message, boolean confirm) {
        try {
            p(message);
            String input = r.readLine();
            if (confirm) {
                if (!yesno("Is this correct: \"" + input + ""))
                    return readString(message,confirm);
            }
            return input;
        }
        catch (IOException ex) {
            p("Invalid input");
            return readString(message,confirm);
        }
    }
    /**
     * Get an input string from the user (no confirmation)
     * @param message Gets displayed to the user
     * @return
     */
    private static String readString(String message) { return readString(message,false); }
    /**
     * Ask the user to input an integer.
     * @param message Gets displayed to the user
     * @return
     */
    private static Integer readInt(String message) {
        String s = readString(message);
        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e)
        {
            p("Invalid integer. Please try again.");
            return readInt(message);
        }
    }
    /**
     * Ask the user a yes/no question. Defaults to yes
     * @param Message What you want to ask the user
     * @return True if the user replied yes (or default)
     */
    private static boolean yesno(String Message) {
        String decision = readString(Message + " (yes/no) Default: yes");
        if (decision.equals("yes") || decision.equals(""))
            return true;
        else
            return false;
    }
    /**
     * Ask the user a yes/no question.
     * @param Message What you want to ask the user
     * @param defaultToNo If true, will make question default to no.
     * @return True if the user replied yes
     */
    private static boolean yesno(String Message, boolean defaultToNo) {
        if (!defaultToNo)
            return yesno(Message);
        else
        {
            String decision = readString(Message + " (yes/no) Default: no");
            if (decision == "yes")
                return true;
            else
                return false;
        }
    }
    /**
     * Simply waits for the user to press enter to continue.
     */
    private static void WaitForEnter() {
        try {
            r.readLine();
        }
        catch (IOException ex) {
            WaitForEnter();
        }
    }
    /**
     * Make the user pick from a range of values (inclusive)
     * @param min The smallest number the user can choose
     * @param max The largest number the user can choose
     * @return The integer that the user chose.
     */
    private static int getSelection(int min, int max) {
        System.out.print("Please make a selection (Default = 0): ");
        Integer selection = null;
        try {
            String s = r.readLine();
            if (!s.equals(""))
                selection = Integer.parseInt(s);
            else
                selection = 0;
        }
        catch(IOException ex) {
            p("IOException occurred reading your input. Please only type a number and press Enter.");
            selection = getSelection(min,max);
        }
        catch (NumberFormatException ex) {
            p("Could not parse that input. Please make sure to type only a number and then press enter.");
            selection = getSelection(min,max);
        }

        if (selection < min || selection > max) {
            p("Number entered is out of the range [" + min + ", " + max + "]");
            selection = getSelection(min,max);
        }

        return selection;
    }
    /**
     * Gives the user an opportunity to change corpora or confirm they want to use the currently selected one.
     */
    private static void ConfirmSelectedCorpus() {
        if (corpusFile == null) {
            p("No corpus has been selected. Press enter to go to Select Corpus Menu");
            WaitForEnter();
            Menu_SelectCorpus();
        }
        else if (corpusFile.exists()) {
            if (yesno("Currently \"" + corpusFile.getName() + "\" is selected. Use another?",true))
                corpusFile = new File(readString("Please enter the full path to the corpus file you wish to parse"));
            else
                p("Using \"" + corpusFile.getName() + "\".");
        }
    }

    /**
     * Output a selection to the console. Collects user selection and returns zero-indexed selection number
     * @param messages An array of options to display in order.
     * @return A zero-index value specifying the selected message.
     */
    private static int selectMenu(String[] messages) {
        int counter = 0;
        for (String message : messages) {
            p(String.format("[%d] %s", counter, message));
            counter++;
        }

        return getSelection(0, counter);

    }
}
