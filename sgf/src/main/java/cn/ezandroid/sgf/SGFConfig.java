package cn.ezandroid.sgf;

public class SGFConfig {

    private static boolean gLastVarIsMain = true;

    public static boolean isLastVarIsMain() {
        return gLastVarIsMain;
    }

    public static void setLastVarIsMain(boolean lastVarIsMain) {
        gLastVarIsMain = lastVarIsMain;
    }
}
