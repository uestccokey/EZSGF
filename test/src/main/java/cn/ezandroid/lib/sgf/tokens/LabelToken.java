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
import java.util.HashMap;
import java.util.Map;

import cn.ezandroid.lib.sgf.Point;

/**
 * 标签标记
 */
public class LabelToken extends MarkupToken {

    private Map<Point, String> mLabelMap = new HashMap<>();

    public LabelToken() {}

    protected boolean parsePoint(StreamTokenizer st)
            throws IOException {
        int token = st.nextToken();

        if ((token == (int) ']') || (token != StreamTokenizer.TT_WORD))
            return true;

        byte xCoord = -1;
        byte yCoord = -1;
        try {
            int xChar = st.sval.charAt(0);
            int yChar = st.sval.charAt(1);
            xCoord = coordFromChar(xChar);
            yCoord = coordFromChar(yChar);
            setX(xCoord);
            setY(yCoord);
            if (st.sval.length() > 3) {
                char split = st.sval.charAt(2);
                if (':' == split) {
                    mLabelMap.put(new Point(xCoord, yCoord), st.sval.substring(3));
                }
            }
        } catch (Exception e) {
            System.out.println("Error at row=" + yCoord + " col=" + xCoord);
            e.printStackTrace();
        }

        // Read the closing parenthesis; we're done.
        //
        return (st.nextToken() == (int) ']');
    }

    public String getLabel(Point point) {
        return mLabelMap.get(point);
    }
}

