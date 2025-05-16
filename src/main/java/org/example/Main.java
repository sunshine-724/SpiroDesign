package org.example;

import javax.swing.JFrame;

import org.example.model.Model;
import org.example.controller.Controller;
import org.example.view.View;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Spirograph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setVisible(true);
        
        Model model = new Model();
        View view = new View(model);
        Controller controller = new Controller(model, view);
    }
}