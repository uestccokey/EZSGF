package com.toomasr.sgf4j.board;

public class Square {

    private StoneState mColor;
    public int x;
    public int y;

    public Square(StoneState color, int x, int y) {
        this.mColor = color;
        this.x = x;
        this.y = y;
    }

    public Square(char colorChar, int x, int y) {
        if ('-' == colorChar)
            this.mColor = StoneState.EMPTY;
        else if ('o' == colorChar)
            this.mColor = StoneState.WHITE;
        else if ('x' == colorChar)
            this.mColor = StoneState.BLACK;

        this.x = x;
        this.y = y;
    }

    public Square(int x, int y) {
        this(StoneState.EMPTY, x, y);
    }

    public String toString() {
        if (this.mColor.equals(StoneState.WHITE))
            return "o";
        else if (this.mColor.equals(StoneState.BLACK))
            return "x";
        else
            return "-";
    }

    public boolean isEmpty() {
        return this.mColor.equals(StoneState.EMPTY);
    }

    public boolean isOfColor(StoneState theColor) {
        return this.mColor.equals(theColor);
    }

    public StoneState getColor() {
        return this.mColor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mColor == null) ? 0 : mColor.hashCode());
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Square other = (Square) obj;
        if (mColor != other.mColor)
            return false;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        return true;
    }
}
