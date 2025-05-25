package G1.SpiroDesign.src.main.java.org.example.spiroIO;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;

import java.util.List;

public class SpiroIO {

    private Model model;

    public SpiroIO(Model model) {
        this.model = model;
    }

    public void saveSpiro(File file) {
        SpurGear spurGear = model.getSpurGear();
        PinionGear pinionGear = model.getPinionGear();
        Pen pen = model.getPen();
        List<Point> locus = model.getLocus();
        long time = model.getTime();

        SpiroData data = new SpiroData(spurGear, pinionGear, pen, locus, time);

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadSpiro(File file) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            SpiroData data = (SpiroData) in.readObject();
            model.setFromSpiroData(data);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
