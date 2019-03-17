package cn.ezandroid.lib.sgf.parser;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import cn.ezandroid.lib.sgf.board.StoneState;

/**
 * GameNode is any node of a game. This can be any SGF node, whether it is an actual
 * move, markers, placement of stones etc. More information at https://www.red-bean.com/sgf/sgf4.html
 */
public class GameNode implements Comparable<GameNode>, Cloneable {

    private final Set<GameNode> mChildren = new TreeSet<>();
    private final Map<String, String> mProperties = new HashMap<>();
    private final Map<String, String> mUpdatedProperties = new HashMap<>();

    private int mMoveNo = -1;
    private int mNodeNo = -1;
    private int mVisualDepth = -1;

    private GameNode mParentNode;
    private GameNode mNextNode = null;
    private GameNode mPrevNode = null;
    private int mId;

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
    public GameNode(GameNode parentNode) {
        this.mParentNode = parentNode;
    }

    public void addChild(GameNode node) {
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

    public void setNextNode(GameNode nextNode) {
        this.mNextNode = nextNode;
    }

    public GameNode getNextNode() {
        return mNextNode;
    }

    public void setPrevNode(GameNode node) {
        this.mPrevNode = node;
    }

    public GameNode getPrevNode() {
        return mPrevNode;
    }

    public GameNode getParentNode() {
        return mParentNode;
    }

    public void setParentNode(GameNode node) {
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

    public String getProperty(String key) {
        return mProperties.get(key);
    }

    public String getProperty(String key, String defaultValue) {
        if (mProperties.get(key) == null)
            return defaultValue;
        else
            return mProperties.get(key);
    }

    public Map<String, String> getProperties() {
        return mProperties;
    }

    public boolean isMove() {
        return mProperties.get("W") != null || mProperties.get("B") != null;
    }

    public String getMoveString() {
        if (mProperties.get("W") != null) {
            return mProperties.get("W");
        } else if (mProperties.get("B") != null) {
            return mProperties.get("B");
        } else {
            return null;
        }
    }

    public int[] getCoords() {
        String moveStr = getMoveString();
        return Util.alphaToCoords(moveStr);
    }

    public boolean isWhite() {
        return mProperties.get("W") != null;
    }

    public boolean isBlack() {
        return mProperties.get("B") != null;
    }

    public String getColor() {
        if (mProperties.get("W") != null)
            return "W";
        return "B";
    }

    public StoneState getColorAsEnum() {
        if (mProperties.get("W") != null)
            return StoneState.WHITE;
        return StoneState.BLACK;
    }

    public boolean hasChildren() {
        return mChildren.size() > 0;
    }

    public Set<GameNode> getChildren() {
        return mChildren;
    }

    public void setMoveNo(int i) {
        this.mMoveNo = i;
    }

    public int getMoveNo() {
        return mMoveNo;
    }

    public boolean isEmpty() {
        return mProperties.isEmpty() && mChildren.size() == 0;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Similar to equals but doesn't include the ID fiel which is auto-assigned
     * during parsing and can be system and time dependent. Method is meant
     * to compare nodes for all the properties worth while equals method
     * is good to compare objects.
     *
     * @param otherNode
     * @return
     */
    public boolean isSameNode(GameNode otherNode) {
        if (this == otherNode)
            return true;
        if (otherNode == null)
            return false;
        if (getClass() != otherNode.getClass())
            return false;
        GameNode other = otherNode;

        for (GameNode gameNode : mChildren) {
            boolean found = false;
            for (GameNode gameNode2 : mChildren) {
                if (gameNode.isSameNode(gameNode2)) {
                    found = true;
                    break;
                }
            }
            if (!found)
                return false;
        }

        if (mMoveNo != other.mMoveNo)
            return false;
        if (mParentNode == null) {
            if (other.mParentNode != null)
                return false;
        } else if (!mParentNode.isSameNode(other.mParentNode))
            return false;
        if (!mProperties.equals(other.mProperties))
            return false;
        if (mVisualDepth != other.mVisualDepth)
            return false;

        return true;
    }

    @Override
    public int compareTo(GameNode o) {
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
        // tt means a pass and actually an empty [] also
        // but right now not handling that because I don't know
        // how exactly it looks like in a SGF
        return !isPlacementMove() && "tt".equals(getMoveString());
    }

    /**
     * There are moves that actually don't place a stone of a
     * move but rather a new added position. I call this a placementMove
     *
     * @return true if this is a placement move and not a game move
     */
    public boolean isPlacementMove() {
        return mProperties.get("W") == null && mProperties.get("B") == null
                && (mProperties.get("AB") != null || mProperties.get("AW") != null);
    }

    public void setNodeNo(int nodeNo) {
        this.mNodeNo = nodeNo;
    }

    public int getNodeNo() {
        return this.mNodeNo;
    }

    public String getSgfComment() {
        String comment = mProperties.get("C");
        if (!TextUtils.isEmpty(comment)) {
            return comment;
        } else {
            return "";
        }
    }

    public void setId(int id) {
        this.mId = id;
    }

    public int getId() {
        return this.mId;
    }

    @Override
    public String toString() {
        return "Props: keys=" + mProperties.keySet().toString() + " all=" + mProperties.toString() + " moveNo: " + mMoveNo + " children: " + mChildren.size() + " vdepth: " + mVisualDepth + " parentNode: " + getParentNode().hashCode();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mChildren.hashCode();
        result = prime * result + mMoveNo;
        result = prime * result + mId;
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
        GameNode other = (GameNode) obj;
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
        if (mId != other.mId)
            return false;
        return true;
    }
}
