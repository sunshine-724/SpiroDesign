package org.example.view;

import javax.swing.JPanel;

import org.example.model.Model;

public class View extends JPanel {
    private Model model;

    public View(Model model) {
        // Initialize the view
        this.model = model;
    }
}
