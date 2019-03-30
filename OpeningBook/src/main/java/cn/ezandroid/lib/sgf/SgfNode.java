package cn.ezandroid.lib.sgf;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * SgfNode is any node of a game.
 * <p>
 * This can be any SGF node, whether it is an actual move, markers, placement of stones etc.
 * More information at https://www.red-bean.com/sgf/sgf4.html
 */
public class SgfNode implements Comparable<SgfNode>, Cloneable {

    public static final int PASS_POS = -1;

    private static final Map<String, Integer> ALPHA_TO_COORD = new HashMap<String, Integer>() {{
        put("a", 0);
        put("b", 1);
        put("c", 2);
        put("d", 3);
        put("e", 4);
        put("f", 5);
        put("g", 6);
        put("h", 7);
        put("i", 8);
        put("j", 9);
        put("k", 10);
        put("l", 11);
        put("m", 12);
        put("n", 13);
        put("o", 14);
        put("p", 15);
        put("q", 16);
        put("r", 17);
        put("s", 18);
        put("t", 19);
        put("u", 20);
        put("v", 21);
        put("w", 22);
        put("x", 23);
        put("y", 24);
        put("z", 25);
    }};

    private final Set<SgfNode> mChildren = new TreeSet<>();
    private final Map<String, String> mProperties = new HashMap<>();
    private final Map<String, String> mUpdatedProperties = new HashMap<>();

    private int mMoveNo = -1;
    private int mNodeNo = -1;
    private int mVisualDepth = -1;

    private SgfNode mParentNode;
    private SgfNode mNextNode;
    private SgfNode mPrevNode;
//    private int mId;

    /**
     * Constructs a new node with the argument as the parent node. Besides a
     * parent node each node also has possibly a previous and next node. Branching
     * is achieved by also having children nodes. See the following for a short
     * overview.
     * <p>
     * getNextNode is the node next on the same line of play. If this is null
     * then the line does not have any more moves.
     * <p>
     * getPrevNode is the previous node on the same line of play. If this is null
     * then this line does not have any previous moves.
     * <p>
     * If node hasChildren() is true then this node has child nodes and not just
     * a nextNode. In that case the getNextNode will be part of the getChildren().
     *
     * @param parentNode node to be the parent of the just created node.
     */
    public SgfNode(SgfNode parentNode) {
        this.mParentNode = parentNode;
    }

    public void addChild(SgfNode node) {
        if (mNextNode == null) {
            mNextNode = node;
            mNextNode.setVisualDepth(0);
            node.setPrevNode(this);
            return;
        }

        if (mChildren.contains(node)) {
            throw new RuntimeException("Node '" + node + "' already exists for " + this);
        }

        mChildren.add(node);
    }

    public void setNextNode(SgfNode nextNode) {
        this.mNextNode = nextNode;
    }

    public SgfNode getNextNode() {
        return mNextNode;
    }

    public void setPrevNode(SgfNode node) {
        this.mPrevNode = node;
    }

    public SgfNode getPrevNode() {
        return mPrevNode;
    }

    public boolean isRootNode() {
        return mParentNode == null;
    }

    public SgfNode getParentNode() {
        return mParentNode;
    }

    public void setParentNode(SgfNode node) {
        mParentNode = node;
    }

    public void addProperty(String key, String value) {
        mProperties.put(key, value);
    }

    public String getUpdatedProperty(String key) {
        return mUpdatedProperties.get(key);
    }

    public void addUpdatedProperty(String key, String value) {
        mUpdatedProperties.put(key, value);
    }

    public boolean containsProperty(String key) {
        return mProperties.containsKey(key);
    }

    public String getProperty(String key) {
        return getProperty(key, "");
    }

    public String getProperty(String key, String defaultValue) {
        if (!mProperties.containsKey(key)) {
            return defaultValue;
        } else {
            return mProperties.get(key);
        }
    }

    public Map<String, String> getProperties() {
        return mProperties;
    }

    public boolean isMove() {
        return mProperties.containsKey("W") || mProperties.containsKey("B");
    }

    public String getMoveString() {
        if (mProperties.containsKey("W")) {
            return mProperties.get("W");
        } else if (mProperties.containsKey("B")) {
            return mProperties.get("B");
        } else {
            return null;
        }
    }

    public static int[] getCoords(String move) {
        int x = PASS_POS;
        int y = PASS_POS;
        if (move != null && move.length() >= 2) {
            Integer xs = ALPHA_TO_COORD.get(move.charAt(0) + "");
            Integer ys = ALPHA_TO_COORD.get(move.charAt(1) + "");
            if (xs != null) {
                x = xs;
            }
            if (ys != null) {
                y = ys;
            }
        }
        return new int[]{x, y};
    }

    public int[] getCoords() {
        String move = getMoveString();
        return getCoords(move);
    }

    public boolean isWhite() {
        return mProperties.containsKey("W");
    }

    public boolean isBlack() {
        return mProperties.containsKey("B");
    }

    public String getColor() {
        if (mProperties.containsKey("W")) {
            return "W";
        } else {
            return "B";
        }
    }
//
//    public StoneState getColorAsEnum() {
//        if (mProperties.get("W") != null)
//            return StoneState.WHITE;
//        return StoneState.BLACK;
//    }

    public boolean hasPrev() {
        return mPrevNode != null;
    }

    public boolean hasNext() {
        return mNextNode != null;
    }

    public boolean hasChildren() {
        return mChildren.size() > 0;
    }

    public Set<SgfNode> getChildren() {
        return mChildren;
    }

    public void setMoveNo(int i) {
        this.mMoveNo = i;
    }

    public int getMoveNo() {
        return mMoveNo;
    }

    public boolean isEmpty() {
        return mProperties.isEmpty() && mChildren.isEmpty();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

//
//    /**
//     * Similar to equals but doesn't include the ID fiel which is auto-assigned
//     * during parsing and can be system and time dependent. Method is meant
//     * to compare nodes for all the properties worth while equals method
//     * is good to compare objects.
//     *
//     * @param otherNode
//     * @return
//     */
//    public boolean isSameNode(SgfNode otherNode) {
//        if (this == otherNode)
//            return true;
//        if (otherNode == null)
//            return false;
//        if (getClass() != otherNode.getClass())
//            return false;
//        SgfNode other = otherNode;
//
//        for (SgfNode gameNode : mChildren) {
//            boolean found = false;
//            for (SgfNode gameNode2 : mChildren) {
//                if (gameNode.isSameNode(gameNode2)) {
//                    found = true;
//                    break;
//                }
//            }
//            if (!found)
//                return false;
//        }
//
//        if (mMoveNo != other.mMoveNo)
//            return false;
//        if (mParentNode == null) {
//            if (other.mParentNode != null)
//                return false;
//        } else if (!mParentNode.isSameNode(other.mParentNode))
//            return false;
//        if (!mProperties.equals(other.mProperties))
//            return false;
//        if (mVisualDepth != other.mVisualDepth)
//            return false;
//
//        return true;
//    }

    @Override
    public int compareTo(SgfNode o) {
        if (this.mVisualDepth < o.mVisualDepth)
            return -1;

        if (this.mVisualDepth > o.mVisualDepth)
            return 1;

        if (this.mMoveNo < o.mMoveNo)
            return -1;

        if (this.mMoveNo > o.mMoveNo)
            return 1;

        // so the move no is the same and the depth is the same
        if (this.hashCode() < o.hashCode())
            return -1;
        else if (this.hashCode() > o.hashCode())
            return 1;

        return 0;
    }

    public void setVisualDepth(int visualDepth) {
        this.mVisualDepth = visualDepth;
    }

    public int getVisualDepth() {
        return mVisualDepth;
    }

    public boolean isPass() {
        return !isPlacementMove() && ("tt".equals(getMoveString()) || "".equals(getMoveString()));
    }

    /**
     * There are moves that actually don't place a stone of a
     * move but rather a new added position. I call this a placementMove
     *
     * @return true if this is a placement move and not a game move
     */
    public boolean isPlacementMove() {
        return !mProperties.containsKey("W") && !mProperties.containsKey("B")
                && (mProperties.containsKey("AW") || mProperties.containsKey("AB") || mProperties.containsKey("AE"));
    }

    public void setNodeNo(int nodeNo) {
        this.mNodeNo = nodeNo;
    }

    public int getNodeNo() {
        return this.mNodeNo;
    }

    public String getSgfComment() {
        return getProperty("C");
    }

    public void setSgfComment(String comment) {
        addProperty("C", comment);
    }

    @Override
    public String toString() {
        return "SgfNode:" + mProperties.toString() + " MoveNo:" + mMoveNo + " Children:" + mChildren.size() + " Depth:" + mVisualDepth;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mChildren.hashCode();
        result = prime * result + mMoveNo;
        result = prime * result + ((mParentNode == null) ? 0 : mParentNode.mProperties.hashCode());
        result = prime * result + mProperties.hashCode();
        result = prime * result + mVisualDepth;
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
        SgfNode other = (SgfNode) obj;
        if (!mChildren.equals(other.mChildren))
            return false;
        if (mMoveNo != other.mMoveNo)
            return false;
        if (mParentNode == null) {
            if (other.mParentNode != null)
                return false;
        } else if (!mParentNode.equals(other.mParentNode))
            return false;
        if (!mProperties.equals(other.mProperties))
            return false;
        if (mVisualDepth != other.mVisualDepth)
            return false;
        return true;
    }
}
