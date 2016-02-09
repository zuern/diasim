package quak.util.FileFormatting;

/**
 * The type of regular expression replacement to perform.
 * ReplaceFIRST will replace only the first match of the regex in each line of the file.
 * ReplaceALL will replace all occurrences of the regex in each line of the file.
 */
public enum ReplaceStyle {
    ReplaceFIRST,
    ReplaceALL
}
