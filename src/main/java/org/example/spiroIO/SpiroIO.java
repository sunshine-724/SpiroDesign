package org.example.spiroIO;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.PortUnreachableException;

import javax.print.attribute.standard.PresentationDirection;

import java.io.IOException;

import java.awt.geom.Point2D;

public class SpiroIO {

    private Model model;
    private Pen pen;

    public SpiroIO(Model model, Pen pen) {
        this.model = model;
        this.pen = pen;
    }

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

            model.setFromSpiroData(modelData);
            pen.setFromSpiroData(penData);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return model;
    }
}