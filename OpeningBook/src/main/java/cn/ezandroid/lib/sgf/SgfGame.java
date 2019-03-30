package cn.ezandroid.lib.sgf;

import java.util.HashMap;
import java.util.Map;

/**
 * This class denotes a Go game.
 * <p>
 * It deals with loading the game and saving the game back to disk.
 */
public class SgfGame {

    private Map<String, String> mProperties = new HashMap<>();
    private SgfNode mRootNode;
    private int mMoveNos = 0;
    private int mNodeNos = 0;

//    // great for debugging
//    private String mOriginalSgf = null;

    public SgfGame() {
    }
//
//    public SgfGame(String originalSgf) {
//        this.mOriginalSgf = originalSgf;
//    }

    public void addProperty(String key, String value) {
        /*
         * Actually properties can be set multiple times and it seems based on
         * other software that the expectation is that everything is appended rather
         * than the last definition wins.
         */
        if (mProperties.containsKey(key)) {
            String current = mProperties.get(key);
            mProperties.put(key, current + "," + value);
        } else {
            mProperties.put(key, value);
        }
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
        return new HashMap<>(this.mProperties);
    }

    @Override
    public String toString() {
        return mProperties.toString();
    }

    public void setRootNode(SgfNode rootNode) {
        this.mRootNode = rootNode;
    }

    public SgfNode getRootNode() {
        return mRootNode;
    }

    public int getMoveNos() {
        return mMoveNos;
    }

    public void setMoveNos(int moveNos) {
        this.mMoveNos = moveNos;
    }

    public void postProcess() {
        // make sure we have a empty first node
        if (getRootNode().isMove()) {
            SgfNode oldRoot = getRootNode();
            SgfNode newRoot = new SgfNode(null);

            newRoot.addChild(oldRoot);
            setRootNode(newRoot);
        }

        // count the moves & nodes
        SgfNode node = getRootNode();
        do {
            if (node.isMove()) {
                mMoveNos++;
            }
            mNodeNos++;
        } while (((node = node.getNextNode()) != null));

        // number all the moves
        numberTheMoves(getRootNode(), 1, 0);

        // calculate the visual depth
        VisualDepthHelper helper = new VisualDepthHelper();
        helper.calculateVisualDepth(getLastMove(), 1);
    }

    private void numberTheMoves(SgfNode startNode, int moveNo, int nodeNo) {
        int nextMoveNo = moveNo;
        int nextNodeNo = nodeNo;

        if (startNode.isMove()) {
            startNode.setMoveNo(moveNo);
            nextMoveNo++;
        }

        startNode.setNodeNo(nodeNo);
        nextNodeNo++;

        if (startNode.getNextNode() != null) {
            numberTheMoves(startNode.getNextNode(), nextMoveNo, nextNodeNo);
        }

        if (startNode.hasChildren()) {
            for (SgfNode childNode : startNode.getChildren()) {
                numberTheMoves(childNode, nextMoveNo, nextNodeNo);
            }
        }
    }

    public int getNodeNos() {
        return mNodeNos;
    }

    public SgfNode getFirstMove() {
        SgfNode node = getRootNode();
        do {
            if (node.isMove()) {
                return node;
            }
        } while ((node = node.getNextNode()) != null);
        return null;
    }

    public SgfNode getLastMove() {
        SgfNode node = getRootNode();
        SgfNode rtrn = null;
        do {
            if (node.isMove()) {
                rtrn = node;
            }
        } while ((node = node.getNextNode()) != null);
        return rtrn;
    }

//    public void saveToFile(File path) {
//        Sgf.writeToFile(this, path);
//    }
//
//    public boolean isSameGame(SgfGame otherGame) {
//        return isSameGame(otherGame, false);
//    }
//
//    public boolean isSameGame(SgfGame otherGame, boolean verbose) {
//        if (this.equals(otherGame)) {
//            if (verbose) {
//                System.out.println("The very same game object - returning true");
//            }
//            return true;
//        }
//
//        // all root level properties have to match
//        Map<String, String> reReadProps = otherGame.getProperties();
//        if (mProperties.size() != reReadProps.size()) {
//            if (verbose) {
//                System.out.printf("Properties mismatch %s %s\n", mProperties.size(), otherGame.getProperties().size());
//            }
//            return false;
//        }
//
//        for (Map.Entry<String, String> entry : mProperties.entrySet()) {
//            if (!entry.getValue().equals(reReadProps.get(entry.getKey()))) {
//                if (verbose) {
//                    System.out.printf("Property mismatch %s='%s' '%s'", entry.getKey(), entry.getValue(), reReadProps.get(entry.getKey()));
//                }
//                return false;
//            }
//        }
//
//        // same number of nodes?
//        if (this.getNodeNos() != otherGame.getNodeNos()) {
//            if (verbose) {
//                System.out.printf("Games have different no of nodes old=%s new=%s", this.getNodeNos(), otherGame.getNodeNos());
//            }
//            return false;
//        }
//
//        // same number of moves?
//        if (this.getMoveNos() != otherGame.getMoveNos()) {
//            if (verbose)
//                System.out.println("Games have different number of moves " + this.getMoveNos() + " " + otherGame.getMoveNos());
//            return false;
//        } else if (verbose) {
//            System.out.println("Games have same number of moves " + this.getMoveNos());
//        }
//
//        // alrighty, lets check alllllll the moves
//        if (!doAllNodesEqual(this, this.getRootNode(), otherGame, otherGame.getRootNode(), verbose)) {
//            if (verbose)
//                System.out.println("Some nodes don't equal");
//            return false;
//        }
//
//        return true;
//    }
//
//    private boolean doAllNodesEqual(SgfGame game, SgfNode node, SgfGame otherGame, SgfNode otherNode, boolean verbose) {
//        if (!node.isSameNode(otherNode)) {
//            if (verbose) {
//                System.out.println("Nodes don't equal a=" + node + "\nb=" + otherGame);
//            }
//            return false;
//        }
//
//        // First let's check the nextNode
//        SgfNode nextNode = node.getNextNode();
//        SgfNode nextOtherNode = otherNode.getNextNode();
//
//        if (nextNode != null) {
//            if (!nextNode.isSameNode(nextOtherNode)) {
//                if (verbose) {
//                    System.out.println("Nodes don't equal");
//                    System.out.println(nextNode);
//                    System.out.println(nextOtherNode);
//                    System.out.println();
//                }
//                return false;
//            }
//
//            if (!doAllNodesEqual(game, nextNode, otherGame, nextOtherNode, verbose)) {
//                return false;
//            }
//        } else if (nextOtherNode != null) {
//            if (verbose) {
//                System.out.println("Nodes don't equal node=" + nextNode + " otherNode=" + nextOtherNode);
//            }
//            return false;
//        }
//
//        // Secondly let's check the children nodes
//        Set<SgfNode> children = node.getChildren();
//        Set<SgfNode> otherChildren = otherNode.getChildren();
//
//        if (children.size() != otherChildren.size()) {
//            if (verbose) {
//                System.out.println("Size of children don't equal node=" + children + " otherNode=" + otherChildren);
//            }
//            return false;
//        }
//
//        for (SgfNode gameNode : children) {
//            boolean found = false;
//            for (SgfNode gameNode2 : otherChildren) {
//                if (gameNode.isSameNode(gameNode2))
//                    found = true;
//            }
//            if (!found) {
//                if (verbose) {
//                    System.out.println("Children don't equal node=" + children + " otherNode=" + otherChildren);
//                }
//                return false;
//            }
//        }
//
//        Iterator<SgfNode> ite = children.iterator();
//        Iterator<SgfNode> otherIte = otherChildren.iterator();
//        for (; ite.hasNext(); ) {
//            SgfNode childNode = ite.next();
//            SgfNode otherChildNode = otherIte.next();
//            if (!doAllNodesEqual(game, childNode, otherGame, otherChildNode, verbose)) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    public String getOriginalSgf() {
//        return mOriginalSgf;
//    }
//
//    public void setOriginalSgf(String originalSgf) {
//        this.mOriginalSgf = originalSgf;
//    }

    public String getGeneratedSgf() {
        StringBuilder rtrn = new StringBuilder();
        rtrn.append("(");

        // lets write all the root node properties
        Map<String, String> props = getProperties();
        if (props.size() > 0) {
            rtrn.append(";");
        }

        for (Map.Entry<String, String> entry : props.entrySet()) {
            rtrn.append(entry.getKey()).append("[").append(entry.getValue()).append("]");
        }

        populateSgf(getRootNode(), rtrn);

        rtrn.append(")");
        return rtrn.toString();
    }

    private void populateSgf(SgfNode node, StringBuilder sgfString) {
        // print out the node
        sgfString.append(";");
        for (Map.Entry<String, String> entry : node.getProperties().entrySet()) {
            sgfString.append(entry.getKey()).append("[").append(entry.getValue()).append("]");
        }
        sgfString.append("\n");

        // if we have children then first print out the
        // getNextNode() and then the rest of the children
        if (node.hasChildren()) {
            sgfString.append("(");
            populateSgf(node.getNextNode(), sgfString);
            sgfString.append(")");
            sgfString.append("\n");

            for (SgfNode childNode : node.getChildren()) {
                sgfString.append("(");
                populateSgf(childNode, sgfString);
                sgfString.append(")");
                sgfString.append("\n");
            }
        }
        // we can just continue with the next elem
        else if (node.getNextNode() != null) {
            populateSgf(node.getNextNode(), sgfString);
        }
    }
}
