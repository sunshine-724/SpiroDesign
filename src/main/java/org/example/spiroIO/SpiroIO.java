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

    public void saveSpiro(File file, Model model, Pen pen) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(model); // modelをシリアライズ化して保存
            out.writeObject(pen); // penをシリアライズ化して保存
        } catch (IOException e) {
            e.printStackTrace(); // エラーが発生した場合の処理
        }
    }

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