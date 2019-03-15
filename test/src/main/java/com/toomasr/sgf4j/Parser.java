package com.toomasr.sgf4j;

import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.GameNode;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private final String mOriginalGame;

    // http://www.red-bean.com/sgf/properties.html
    private static final Set<String> sGeneralProps = new HashSet<>();

    static {
        // Application used to generate the SGF
        sGeneralProps.add("AP");
        // Black's Rating
        sGeneralProps.add("BR");
        // White's Rating
        sGeneralProps.add("WR");
        // KOMI
        sGeneralProps.add("KM");
        // weird alternative KOMI
        sGeneralProps.add("GKM");
        // Black Player Extended information
        sGeneralProps.add("PBX");
        // Black Player name
        sGeneralProps.add("PB");
        // White Player name
        sGeneralProps.add("PW");
        // I think - Black Player name
        sGeneralProps.add("PX");
        // I think - White Player name
        sGeneralProps.add("PY");
        // Charset
        sGeneralProps.add("CA");
        // File format
        sGeneralProps.add("FF");
        // Game type - 1 means Go
        sGeneralProps.add("GM");
        // Size of the board
        sGeneralProps.add("SZ");
        // Annotator
        sGeneralProps.add("AN");
        // Name of the event
        sGeneralProps.add("EV");
        // Name of the event extended
        // Extended info about the event
        sGeneralProps.add("EVX");
        // Round number
        sGeneralProps.add("RO");
        // Rules
        sGeneralProps.add("RU");
        // Time limit in seconds
        sGeneralProps.add("TM");
        // How overtime is handled
        sGeneralProps.add("OT");
        // Date of the game
        sGeneralProps.add("DT");
        // Extended date
        sGeneralProps.add("DTX");
        // Place of the game
        sGeneralProps.add("PC");
        // Result of the game
        sGeneralProps.add("RE");
        // I think - Result of the game
        sGeneralProps.add("ER");
        // How to show comments
        sGeneralProps.add("ST");
        /*
         * Provides some extra information about the following game.
         * The intend of GC is to provide some background information
         * and/or to summarize the game itself.
         */
        sGeneralProps.add("GC");
        // Any copyright information
        sGeneralProps.add("CP");
        // Provides name of the source
        sGeneralProps.add("SO");
        // Name of the white team
        sGeneralProps.add("WT");
        // Name of the black team
        sGeneralProps.add("BT");
        // name of the user or program who entered the game
        sGeneralProps.add("US");
        // How to print move numbers
        sGeneralProps.add("PM");
        // Some more printing magic
        sGeneralProps.add("FG");
        // Name of the game
        sGeneralProps.add("GN");
        // Black territory or area
        sGeneralProps.add("TB");
        // White territory or area
        sGeneralProps.add("TW");
        // Sets the move number to the given value, i.e. a move
        // specified in this node has exactly this move-number. This
        // can be useful for variations or printing.
        // SGF4J doesn't honour this atm
        sGeneralProps.add("MN");
        // Handicap stones
        sGeneralProps.add("HA");
        // "AB": add black stones AB[point list]
        sGeneralProps.add("AB");
        // "AW": add white stones AW[point list]
        sGeneralProps.add("AW");
        // add empty = remove stones
        sGeneralProps.add("AE");
        // PL tells whose turn it is to play.
        sGeneralProps.add("PL");
        // KGSDE - kgs scoring - marks all prisoner stones
        // http://senseis.xmp.net/?CgobanProblemsAndSolutions
        sGeneralProps.add("KGSDE");
        // KGS - score white
        sGeneralProps.add("KGSSW");
        // KGS - score black
        sGeneralProps.add("KGSSB");
        // Checkmark - ignored in FF4
        // http://www.red-bean.com/sgf/ff1_3/ff3.html and http://www.red-bean.com/sgf/changes.html
        sGeneralProps.add("CH");
        // I think this is White Country
        sGeneralProps.add("WC");
        // "LT": enforces losing on time LT[]
        // http://www.red-bean.com/sgf/ff1_3/ff3.html
        // I don't get it but I'm parsing it
        sGeneralProps.add("LT");
        // I think this is Black Country
        sGeneralProps.add("BC");
        // I think this is just a game ID
        sGeneralProps.add("ID");
        // I have no idea what these properties means
        // but they are in many games of the collections
        // I've downloaded from the interwebs
        sGeneralProps.add("OH");
        sGeneralProps.add("LC");
        sGeneralProps.add("RD"); // maybe release date?
        sGeneralProps.add("TL"); // something to do with time
        sGeneralProps.add("GK"); // something to do with the game

        // These are also available for nodes!

        // time left for white
        sGeneralProps.add("WL");
        // time left for black
        sGeneralProps.add("BL");

        // Multigo specific properties
        sGeneralProps.add("MULTIGOGM");
        sGeneralProps.add("MULTIGOBM");
        // hotspot - no idea :)
        sGeneralProps.add("HO");
        // some go program info probably
        sGeneralProps.add("GOGGPFF");
        sGeneralProps.add("GOGGPAP");
        // these are actually node properties
        // but there are games where they are
        // part of the game properites - go figure!
        sGeneralProps.add("L");
        sGeneralProps.add("B");
        // see problematic-013.sgf - I think this is white score and black score
        sGeneralProps.add("BS");
        sGeneralProps.add("WS");
        // not sure what this is is but found it in a SGF file
        sGeneralProps.add("MU");
    }

    private static final Set<String> sNodeProps = new HashSet<>();

    static {
        // Move for Black
        sNodeProps.add("B");
        // Move for White
        sNodeProps.add("W");
        // marks given points with circle
        sNodeProps.add("CR");
        // marks given points with cross
        sNodeProps.add("MA");
        // marks given points with square
        sNodeProps.add("SQ");
        // selected points
        sNodeProps.add("SL");
        // labels on points
        sNodeProps.add("LB");
        // marks given points with triangle
        sNodeProps.add("TR");
        // Number of white stones to play in this byo-yomi period
        sNodeProps.add("OW");
        // Number of black stones to play in this byo-yomi period
        sNodeProps.add("OB");
        // time left for white
        sNodeProps.add("WL");
        // time left for black
        sNodeProps.add("BL");
        // Comment
        sNodeProps.add("C");
        /*
         * Provides a name for the node. For more info have a look at
         * the C-property.
         */
        sNodeProps.add("N");
        /*
         * List of points - http://www.red-bean.com/sgf/proplist_ff.html
         * Label the given points with uppercase letters. Not used in FF 3 and FF 4!
         *
         * Replaced by LB which defines the letters also:
         * Example: L[fg][es][jk] -> LB[fg:A][es:B][jk:C]
         */
        sNodeProps.add("L");

        // don't quite get it what it means
        // but lets parse this out
        sNodeProps.add("WV");
        // dimmed stones - see http://www.red-bean.com/sgf/DD_VW.html
        sNodeProps.add("VW");
        // Tesuji - don't know what to do with it though
        sNodeProps.add("TE");
    }

    private Stack<GameNode> mTreeStack = new Stack<>();

    public Parser(String game) {
        mOriginalGame = game;
    }

    public Game parse() {
        Game game = new Game(mOriginalGame);

        // the root node
        GameNode parentNode = null;
        // replace token delimiters

        int moveNo = 1;
        int id = 10000;

        for (int i = 0; i < mOriginalGame.length(); i++) {
            char chr = mOriginalGame.charAt(i);
            if (';' == chr && (i == 0 || mOriginalGame.charAt(i - 1) != '\\')) {
                String nodeContents = consumeUntil(mOriginalGame, i);
                i = i + nodeContents.length();

                GameNode node = parseToken(nodeContents, parentNode, game, id++);
                if (node.isMove()) {
                    node.setMoveNo(moveNo++);
                }

                if (parentNode == null) {
                    parentNode = node;
                    game.setRootNode(parentNode);
                } else if (!node.isEmpty()) {
                    parentNode.addChild(node);
                    parentNode = node;
                }
            } else if ('(' == chr && parentNode != null) {
                mTreeStack.push(parentNode);
            } else if (')' == chr) {
                if (mTreeStack.size() > 0) {
                    parentNode = mTreeStack.pop();
                    moveNo = parentNode.getMoveNo() + 1;
                }
            }
        }

        return game;
    }

    private String consumeUntil(String gameStr, int i) {
        StringBuilder rtrn = new StringBuilder();
        boolean insideComment = false;
        boolean insideValue = false;
        for (int j = i + 1; j < gameStr.length(); j++) {
            char chr = gameStr.charAt(j);
            if (insideComment) {
                if (']' == chr && gameStr.charAt(j - 1) != '\\') {
                    insideComment = false;
                }
                rtrn.append(chr);
            } else {
                if ('C' == chr && '[' == gameStr.charAt(j + 1)) {
                    insideComment = true;
                    rtrn.append(chr);
                } else if ('[' == chr) {
                    insideValue = true;
                    rtrn.append(chr);
                } else if (']' == chr) {
                    insideValue = false;
                    rtrn.append(chr);
                }
                // while inside the value lets consume everything -
                // even chars that otherwise would have special meaning
                // like ;()
                else if (insideValue) {
                    rtrn.append(chr);
                } else if ('\n' == chr) {
                    // skip newlines
                } else if ('\r' == chr) {
                    // skip newlines
                } else if (';' != chr && ')' != chr && '(' != chr) {
                    rtrn.append(chr);
                } else {
                    break;
                }
            }
        }
        return rtrn.toString().trim();
    }

    private GameNode parseToken(String token, final GameNode parentNode, Game game, int id) {
        GameNode rtrnNode = new GameNode(parentNode);
        rtrnNode.setId(id);
        // replace delimiters
        token = Parser.prepareToken("'" + token + "'");

        // lets find all the properties
        Pattern p = Pattern.compile("([a-zA-Z]{1,})((\\[[^\\]]*\\]){1,})");
        Matcher m = p.matcher(token);
        while (m.find()) {
            String group = m.group();
            if (group.length() == 0)
                continue;

            String key = m.group(1);
            String value = m.group(2);
            if (value.startsWith("[")) {
                value = value.substring(1, value.length() - 1);
            }

            value = Parser.normaliseToken(value);

            // these properties require some cleanup
            if ("AB".equals(key) || "AW".equals(key)) {
                // these come in as a list of coordinates while the first [ is cut off
                // and also the last ], easy to split by ][
                String[] list = value.split("\\]\\[");
                // if the parent node is null then these are
                // game properties, if not null then the node properties
                if (parentNode == null) {
                    game.addProperty(key, String.join(",", list));
                } else {
                    rtrnNode.addProperty(key, String.join(",", list));
                }
            } else if ("C".equals(key) || "N".equals(key)) {
                // nodes and the game can have a comment or name
                // if parent is null it is a game property
                if (parentNode == null) {
                    game.addProperty(key, value);
                } else {
                    rtrnNode.addProperty(key, value);
                }
            } else if (sGeneralProps.contains(key) || sNodeProps.contains(key)) {
                boolean addedToGame = false;
                if (sGeneralProps.contains(key) && parentNode == null) {
                    game.addProperty(key, cleanValue(value));
                    addedToGame = true;
                }

                if (sNodeProps.contains(key) && !addedToGame) {
                    rtrnNode.addProperty(key, cleanValue(value));
                }
            } else {
                // log.error("Not able to parse property '" + m.group(1) + "'=" + m.group(2) + ". Found it from " + m.group(0));
                throw new SgfParseException("Ignoring property '" + m.group(1) + "'=" + m.group(2) + " Found it from '" + m.group(0) + "'");
            }
        }

        return rtrnNode;
    }

    private String cleanValue(String value) {
        String cleaned = value.replaceAll("\\\\;", ";");
        return cleaned;
    }

    private static String prepareToken(String token) {
        token = token.replaceAll("\\\\\\[", "@@@@@");
        token = token.replaceAll("\\\\\\]", "#####");
        return token;
    }

    public static String normaliseToken(String token) {
        token = token.replaceAll("@@@@@", "\\\\\\[");
        token = token.replaceAll("#####", "\\\\\\]");
        return token;
    }
}
