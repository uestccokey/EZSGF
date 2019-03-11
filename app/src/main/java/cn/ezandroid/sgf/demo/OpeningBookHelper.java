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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * OpeningBook辅助类
 *
 * @author like
 * @date 2019-03-08
 */
public class OpeningBookHelper {

    private static final String MAGIC_HEAD = "OB";
    private static final int MIN_BOARD_SIZE = 1;

    private static volatile OpeningBook COMMON;

    public static OpeningBook create(File file) {
        try {
            return OpeningBookHelper.readOpeningBook(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static OpeningBook common(Context context) {
        if (COMMON == null) {
            synchronized (OpeningBook.class) {
                if (COMMON == null) {
                    COMMON = readOpeningBook(new BufferedInputStream(context.getResources().openRawResource(R.raw.opening_book_106182)));
                }
            }
        }
        if (COMMON == null) {
            return new OpeningBook((byte) 19);
        } else {
            return new OpeningBook(COMMON.getBoardSize(), COMMON.getBookTable());
        }
    }

    public static OpeningBook readOpeningBook(InputStream fis) {
        OpeningBook book = null;
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(fis);

            // 读取魔法字符
            String magicHead = String.valueOf(dis.readChar()) + String.valueOf(dis.readChar());
            System.out.println("魔法字符：" + magicHead);
            // 魔法字符不匹配说明不是合法的开局库文件，直接返回null
            if (!MAGIC_HEAD.equals(magicHead.toUpperCase())) {
                return null;
            }
            // 读取格式版本
            System.out.println("格式版本：" + dis.readByte());
            // 读取棋盘尺寸
            byte boardSize = dis.readByte();
            System.out.println("棋盘尺寸：" + boardSize);
            // 棋盘尺寸超过限制说明不是合法的开局库文件，直接返回null
            if (boardSize <= MIN_BOARD_SIZE) {
                return null;
            }
            // 读取其他信息
            System.out.println("其他信息：" + dis.readUTF());
            // 读取局面的预测图
            book = new OpeningBook(boardSize);
            int bookSize = dis.readInt();
            for (int i = 0; i < bookSize; i++) {
                long hash = dis.readLong();
                byte forecastSize = dis.readByte();
                for (int j = 0; j < forecastSize; j++) {
                    OpeningBook.Forecast forecast = new OpeningBook.Forecast(dis.readShort(), dis.readUTF());
                    book.add(hash, forecast);
                }
            }
            System.out.println("总局面数：" + bookSize);
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
        return book;
    }

    public static void writeOpeningBook(File file, OpeningBook book) {
        FileOutputStream fos = null;
        DataOutputStream dos = null;
        try {
            fos = new FileOutputStream(file);
            dos = new DataOutputStream(fos);

            // 写入魔法字符
            dos.writeChar('O');
            dos.writeChar('B');
            // 写入格式版本
            dos.writeByte(0);
            // 写入棋盘尺寸
            dos.writeByte(book.getBoardSize());
            // 写入其他信息 比如作者名称，文件版本，作者邮箱等
            dos.writeUTF("AhQGo");
            // 写入局面的预测图
            dos.writeInt(book.size());
            Set<Map.Entry<Long, List<OpeningBook.Forecast>>> entrySet = book.getBookTable().entrySet();
            for (Map.Entry<Long, List<OpeningBook.Forecast>> entry : entrySet) {
                Long key = entry.getKey();
                List<OpeningBook.Forecast> forecasts = entry.getValue();
                dos.writeLong(key);
                dos.writeByte(forecasts.size()); // 对一个局面的预测落子应该不超过127个
                for (OpeningBook.Forecast forecast : forecasts) {
                    dos.writeShort(forecast.getPosition());
                    dos.writeUTF(forecast.getInfo());
                }
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
