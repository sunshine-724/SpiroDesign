package org.example.controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.example.model.Model;
import org.example.view.View;

public class Controller extends MouseAdapter {
    private Model model;
    private View view;

    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Handle mouse click event
        int x = e.getX();
        int y = e.getY();
    }
}
