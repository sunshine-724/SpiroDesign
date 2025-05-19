package org.example.model;

import java.awt.geom.*;
import javax.swing.Timer;



public class Model {

    SpurGear spurGear;
    PinionGear pinionGear;

    SpiroIO spiroIO;

    // Point2D.Double spurGearPosition; //ref only
    // Point2D.Double pinionGearPosition; //ref only

    Timer timer;
    private long startTime;
    private long pauseTime;

    // Constants
    private static final int FRAME_PER_MILLISECOND = 1000 / 60; // 60 FPS
    private static final Point2D.Double PINIONGEAR_INIT_POS = new Point2D.Double(100, 0);


    public Model() {
        spurGear = new SpurGear();
        pinionGear = new PinionGear();

        spiroIO = new SpiroIO();

        pinionGear.setPosition(PINIONGEAR_INIT_POS);

        startTime = System.currentTimeMillis();
        pauseTime = 0;
        timer = new Timer(FRAME_PER_MILLISECOND, e -> {
			updateData(); // データ更新
		});
    }

    public void start() {
        System.out.println("start");
        timer.start();
        startTime = System.currentTimeMillis() - pauseTime;
    }

    public void stop() {
        System.out.println("stop");
        timer.stop();
        pauseTime = System.currentTimeMillis() - startTime;
    }

    private void updateData() {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - startTime;

        // Update the position of the pinion gear
        pinionGear.move((int) deltaTime);
    }

    public void changeSpeed(Double speed) {
        pinionGear.changeSpeed(speed);
    }

    public void setPinionGearPosition(Point2D.Double position) {
        pinionGear.setPosition(position);
    }

    public void changeSpurGeearRadius(Double radius) {
        spurGear.changeRadius(radius);
    }

    public void changePinionGearRadius(Double radius) {
        pinionGear.changeRadius(radius);
    }

    public Double getPinionGearRadius() {
        return pinionGear.getRadius();
    }

    public Double getSpurGearRadius() {
        return spurGear.getRadius();
    }

    public void loadData() {
        spiroIO.loadData();
    }

    public void saveData() {
        spiroIO.saveData();
    }
}
