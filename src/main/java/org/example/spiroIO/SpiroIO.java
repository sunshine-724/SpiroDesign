package org.example.spiroIO;

import org.example.model.Model;
import org.example.model.Pen;
import org.example.lib.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public class SpiroIO {
    /**
     * SpiroIOクラスは、Spiroのモデルとペンの状態をファイルに保存および読み込みするためのクラスです。
     * 
     * @param file
     * @param model
     * @param pen
     */

    /**
     * saveSpiroメソッドは、指定されたファイルにSpiroのモデルとペンの状態を保存します。
     * 
     * @param file
     * @param model
     * @param pen
     */
    public void saveSpiro(File file, Model model, Pen pen) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(model); // modelをシリアライズ化して保存
            out.writeObject(pen); // penをシリアライズ化して保存
        } catch (IOException e) {
            e.printStackTrace(); // エラーが発生した場合の処理
        }
    }

    /**
     * loadSpiroメソッドは、指定されたファイルからSpiroのモデルとペンの状態を読み込みます。
     * ファイルから読み込まれたデータは、ModelとPenのオブジェクトとして返されます。
     * 
     * @param file 読み込むファイル
     * @return モデルとペンのデータを含むPairオブジェクト。読み込みに失敗した場合はnullを返す。
     */
    public Pair<Model, Pen> loadSpiro(File file) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Model modelData = (Model) in.readObject(); // modelをデシリアライズ化して取得
            Pen penData = (Pen) in.readObject(); // penをデシリアライズ化して取得
            return new Pair<Model, Pen>(modelData, penData); // モデルデータとペンデータをペアで返す
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace(); // エラーが発生した場合の処理
            return null; // エラーが発生した場合はnullを返す
        }
    }
}