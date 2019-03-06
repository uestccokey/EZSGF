/*
 * Copyright (C) 2001 by Dave Jarvis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * Online at: http://www.gnu.org/copyleft/gpl.html
 */

package cn.ezandroid.lib.sgf.tokens;

import java.io.IOException;
import java.io.StreamTokenizer;

import cn.ezandroid.lib.sgf.SGFException;

/**
 * 对局结果 (jigo, winner: score, resignation, time).
 */
public class ResultToken extends TextToken implements InfoToken {

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

    public ResultToken() { }

    /**
     * The following conventions are used to denote the game result:
     * <p>
     * <PRE>
     * "0" (zero) or "Draw" for a draw (jigo),
     * "B+score" for black win
     * "W+score" for white win
     * e.g. "B+0.5", "W+64", "B+12.5"
     * <p>
     * "B+R"  | "B+Resign",  "W+R" | "W+Resign" for win by resignation
     * "B+T"  | "B+Time",    "W+T" | "W+Time" for win by time
     * "B+F"  | "B+Forfeit", "W+F" | "W+Forfeit" for win by forfeit
     * "Void" | "?" for no result/suspended play/unknown result
     * </PRE>
     */
    protected boolean parseContent(StreamTokenizer st)
            throws IOException, SGFException {
        if (!super.parseContent(st))
            return false;

        String result = getText();
        if (result.isEmpty()) {
            return false;
        }

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

        return true;
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

//    /**
//     * Returns the result in a valid SGF property format (without the RE[] part).
//     */
//    @Override
//    public String toString() {
//        if (mWinner == JIGO)
//            return "0";
//        else if (mWinner == VOID)
//            return "Void";
//        else if (mWinner == UNKNOWN_WINNER)
//            return "?";
//
//        String result;
//        if (mScore > 0)
//            result = ((int) mScore) + "." + ((int) Math.round(mScore * 10) % 10);
//        else if (mScore == RESIGN)
//            result = "R";
//        else if (mScore == TIME)
//            result = "T";
//        else if (mScore == FORFEIT)
//            result = "F";
//        else
//            result = "";
//
//        return mWinner + "+" + result;
//    }
}