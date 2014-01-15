import java.util.regex.*;

/**
 * The poll array folds the options of the array and provides helper methods
 */
public class PollArray {
    private int[] array;

    /**
     * constructs an empty results array of size <b>size</b>
     *
     * @param size
     */
    public PollArray(int size) {
        array = new int[size];
    }

    /**
     * Constructs an array of poll results from the array
     *
     * @param arr
     */
    public PollArray(int[] arr) {
        this.array = new int[arr.length];
        System.arraycopy(arr, 0, this.array, 0, arr.length);
    }

    /**
     * returns the size of the array
     *
     * @return
     */
    public int getSize() {
        return this.array.length;
    }

    /**
     * Gets the total number of votes
     *
     * @return
     */
    public int getVotes() {
        int sumVotes = 0;
        for (int i = 0; i < this.array.length; i++) {
            sumVotes += this.array[i];
        }
        return sumVotes;
    }

    /**
     * adds 1 to the vote count of <b>option</b>
     *
     * @param option
     */
    public void addVote(int option) {
        this.array[option] = this.array[option] + 1;
    }

    /**
     * prints the array as numbers separated by semi-colon ";"
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.array.length; i++) {
            sb.append(this.array[i] + ";");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * parses a string to answers
     *
     * @param input
     * @return
     */
    public static PollArray parsePollArray(String input) {
        int[] result = null;
        try {
            Pattern pattern = Pattern.compile("[()\"\']");
            input = input.replaceAll(pattern.toString(), "");
            String[] arr = input.split(";");
            result = new int[arr.length];

            for (int i = 0; i < arr.length; i++) {
                result[i] = Integer.parseInt(arr[i]);
            }
        } catch (NumberFormatException e) {
            System.err.println(e);
            return null;
        }
        return new PollArray(result);
    }

    /**
     * Returns the number of votes for a specific option
     *
     * @param option
     * @return
     */
    public int getVoteForOption(int option) {
        return this.array[option];
    }
}
