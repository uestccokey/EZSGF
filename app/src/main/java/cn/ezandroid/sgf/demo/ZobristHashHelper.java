package cn.ezandroid.sgf.demo;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ZobristHash辅助类
 *
 * @author like
 * @date 2019-03-08
 */
public class ZobristHashHelper {

    private static final String MAGIC_HEAD = "ZH";
    private static final int MIN_BOARD_SIZE = 1;

    private static volatile ZobristHash COMMON;

    /**
     * 读取文件创建哈希表
     *
     * @param file
     * @return
     */
    public static ZobristHash create(File file) {
        try {
            return readZobristHash(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取通用哈希表
     *
     * @param context
     * @return
     */
    public static ZobristHash common(Context context) {
        if (COMMON == null) {
            synchronized (ZobristHash.class) {
                if (COMMON == null) {
                    COMMON = readZobristHash(new BufferedInputStream(context.getResources().openRawResource(R.raw.default_zobrist_hash)));
                }
            }
        }
        if (COMMON == null) {
            return new ZobristHash((byte) 19);
        } else {
            return new ZobristHash(COMMON.getBoardSize(), COMMON.getPassHash(), COMMON.getBoardHashTable());
        }
    }

    public static ZobristHash readZobristHash(InputStream fis) {
        ZobristHash hash = null;
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(fis);

            // 读取魔法字符
            String magicHead = String.valueOf(dis.readChar()) + String.valueOf(dis.readChar());
            System.out.println("魔法字符：" + magicHead);
            // 魔法字符不匹配说明不是合法的哈希表文件，直接返回null
            if (!MAGIC_HEAD.equals(magicHead.toUpperCase())) {
                return null;
            }
            // 读取格式版本
            System.out.println("格式版本：" + dis.readByte());
            // 读取棋盘尺寸
            byte boardSize = dis.readByte();
            int positionStateCount = ZobristHash.getPositionStateCount();
            int count = boardSize * boardSize * positionStateCount;
            System.out.println("棋盘尺寸：" + boardSize);
            // 棋盘尺寸超过限制说明不是合法的哈希表文件，直接返回null
            if (boardSize <= MIN_BOARD_SIZE) {
                return null;
            }
            // 读取其他信息
            System.out.println("其他信息：" + dis.readUTF());
            // 读取Pass的Hash值
            long passHash = dis.readLong();
            System.out.println("Pass Hash：" + passHash);
            // 读取棋盘的Hash表
            long[] table = new long[count];
            int index = 0;
            while (dis.available() > 0) {
                table[index] = dis.readLong();
                index++;
            }
            long[][][] boardHashTable = new long[positionStateCount][boardSize][boardSize];
            index = 0;
            for (int i = 0; i < boardSize; i++) {
                for (int j = 0; j < boardSize; j++) {
                    for (int state = 0; state < positionStateCount; state++) {
                        boardHashTable[state][i][j] = table[index];
                        index++;
                    }
                }
            }
            hash = new ZobristHash(boardSize, passHash, boardHashTable);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return hash;
    }

    public static void writeZobristHash(File file, ZobristHash hash) {
        FileOutputStream fos = null;
        DataOutputStream dos = null;
        try {
            byte boardSize = hash.getBoardSize();
            long passHash = hash.getPassHash();
            long[][][] boardHashTable = hash.getBoardHashTable();
            int positionStateCount = ZobristHash.getPositionStateCount();
            int count = boardSize * boardSize * positionStateCount;
            // 将3维数组转换为1维数组
            long[] table = new long[count];
            int index = 0;
            for (int i = 0; i < boardSize; i++) {
                for (int j = 0; j < boardSize; j++) {
                    for (int state = 0; state < positionStateCount; state++) {
                        table[index] = boardHashTable[state][i][j];
                        index++;
                    }
                }
            }

            fos = new FileOutputStream(file);
            dos = new DataOutputStream(fos);

            // 写入魔法字符
            dos.writeChar('Z');
            dos.writeChar('H');
            // 写入格式版本
            dos.writeByte(0);
            // 写入棋盘尺寸
            dos.writeByte(boardSize);
            // 写入其他信息 比如作者名称，文件版本，作者邮箱等
            dos.writeUTF("AhQGo");
            // 写入Pass的Hash值
            dos.writeLong(passHash);
            // 写入棋盘的Hash表
            for (long h : table) {
                dos.writeLong(h);
            }
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
