package org.example.spiroIO;

import org.example.model.Model;
import org.example.model.Pen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public class SpiroIO {

    public void saveSpiro(File file, Model model, Pen pen) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(model);
            out.writeObject(pen);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Model loadSpiro(File file) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Model modelData = (Model) in.readObject();
            Pen penData = (Pen) in.readObject();
            return new Model(modelData, penData);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}