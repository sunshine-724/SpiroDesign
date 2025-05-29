package org.example.model;

import java.awt.geom.Point2D;

public class SpurGear extends SpiroGear {
    public SpurGear() {
        // Constructor logic here


    }

    public Point2D.Double getSpurPosition() {
        // Assuming spur gear position is calculated based on some logic
        // For now, returning a default position
        return new Point2D.Double(0, 0);
    }
}
