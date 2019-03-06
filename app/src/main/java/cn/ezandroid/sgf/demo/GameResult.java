package cn.ezandroid.sgf.demo;

import java.io.Serializable;

/**
 * Contains the result of a game in the SGF Format (RE[] property).<p>
 * <p/>
 * "0" (zero) or "Draw" for a draw (jigo)<br/>
 * "B+" ["mScore"] for a black win<br/>
 * "W+" ["mScore"] for a white win<br/>
 * Score is optional (some games don't have a mScore e.g. chess).<br/>
 * If the mScore is given it has to be given as a real value, e.g. "B+0.5", "W+64", "B+12.5"<br/>
 * Use "B+R" or "B+Resign" and "W+R" or "W+Resign" for a win by resignation.
 * Applications must not write "Black resigns".<br/>
 * Use "B+T" or "B+Time" and "W+T" or "W+Time" for a win on time, "B+F" or "B+Forfeit" and "W+F"
 * or "W+Forfeit" for a win by forfeit<br/>
 * "Void" for no result or suspended play<br/>
 * "?" for an unknown result.
 */
public class GameResult implements Serializable {

    public static final long serialVersionUID = 42L;

    public static final char
            JIGO = '0', // 和棋
            BLACK = 'B',
            WHITE = 'W',
            VOID = 'V',
            UNKNOWN_WINNER = '?';

    public static final double
            RESIGN = -1,
            TIME = -2,
            FORFEIT = -3, // 犯规
            UNKNOWN_AMOUNT = -9;

    private char mWinner;
    private double mScore;

    public GameResult(char winner, double score) {
        this.mWinner = winner;
        this.mScore = score;
    }

    private GameResult(String result) {
        char c = result.toUpperCase().charAt(0);

        if (c == '0' || c == 'D' || c == 'J') {
            mWinner = JIGO;
        } else if (c == 'V') {
            mWinner = VOID;
        } else if (c == '?') {
            mWinner = UNKNOWN_WINNER;
        } else {
            if (c == 'B')
                mWinner = BLACK;
            else if (c == 'W')
                mWinner = WHITE;
            else
                mWinner = UNKNOWN_WINNER;

            mScore = UNKNOWN_AMOUNT;
            if (result.length() > 2) {
                c = result.charAt(2);
                if (c == 'R')
                    mScore = RESIGN;
                else if (c == 'T')
                    mScore = TIME;
                else if (c == 'F')
                    mScore = FORFEIT;
                else if (Character.isDigit(c)) {
                    try {
                        mScore = parseScore(result.substring(2), '.');
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
    }

    private float parseScore(String number, char decimalSeparator) {
        int dotPos = number.indexOf(decimalSeparator);
        if (dotPos < 0)
            return Integer.parseInt(number);
        int integerPart = Integer.parseInt(number.substring(0, dotPos));
        int decimalPart = Integer.parseInt(number.substring(dotPos + 1, dotPos + 2));
        return integerPart + (integerPart >= 0 ? 1f : -1f) * (decimalPart / 10f);
    }

    /**
     * Returns the result in a valid SGF property format (without the RE[] part).
     */
    @Override
    public String toString() {
        if (mWinner == JIGO)
            return "0";
        else if (mWinner == VOID)
            return "Void";
        else if (mWinner == UNKNOWN_WINNER)
            return "?";

        String result;
        if (mScore > 0)
            result = ((int) mScore) + "." + ((int) Math.round(mScore * 10) % 10);
        else if (mScore == RESIGN)
            result = "R";
        else if (mScore == TIME)
            result = "T";
        else if (mScore == FORFEIT)
            result = "F";
        else
            result = "";

        return mWinner + "+" + result;
    }

    /**
     * Returns the mWinner represented by this instance. It can be one of the following values (constants) :<br/>
     * JIGO, BLACK, WHITE, VOID, UNKNOWN_WINNER
     */
    public char getWinner() {
        return mWinner;
    }

    /**
     * Returns the mScore represented by this instance. It can be one of the following values (constants) :<br/>
     * RESIGN, TIME, FORFEIT, UNKNOWN_AMOUNT, or the result as a number.
     */
    public double getScore() {
        return mScore;
    }

    /**
     * Tries to parse the given SGF result. Returns null if it failed.
     */
    public static GameResult tryParse(String sgfResult) {
        try {
            return new GameResult(sgfResult);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
