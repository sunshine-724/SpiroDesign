package org.example.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.example.model.Model;
import java.io.File;
import javax.swing.Timer;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JPopupMenu;
import javax.swing.BoxLayout;

public class View extends JPanel {

    private Model model;

    public JPopupMenu MenuDisplay;
    public Map<String, JButton> subButton;
    public JTextField speedDisplay;
    public Map<String, JButton> penSizeDisplay;
    public JColorChooser colorPalletDisplay;

    private double scale = 1.0;
    private static final double MIN_SCALE = 0.5;
    private static final double MAX_SCALE = 2.0;

    private Point2D.Double viewOffset = new Point2D.Double(0, 0);

    private boolean isDefiningSpurGear = false;
    private Point spurGearCenterScreen = null;
    private Point currentDragPointScreen = null;

    private boolean isDefiningPinionGear = false;
    private Point pinionGearCenterScreen = null;
    private Point currentDragPointScreenForPinion = null;

    private List<Point2D.Double> loadedLocusData = null;
    private Color loadedPenColor = null;
    private double loadedPenSize = -1.0;

    private String saveMessage = null;
    private Timer messageTimer;
    private static final int MESSAGE_DISPLAY_DURATION = 2000;

    public View(Model model) {
        this.model = model;

        setLayout(null);
        setBackground(Color.WHITE);

        MenuDisplay = new JPopupMenu();
        JPanel menuContentPanel = new JPanel();
        menuContentPanel.setLayout(new BoxLayout(menuContentPanel, BoxLayout.Y_AXIS));

        subButton = new HashMap<>();
        penSizeDisplay = new HashMap<>();

        String[] buttonNames = { "Pen", "SpurGear", "PinionGear", "Start", "Stop", "Clear", "Save", "Load" };
        for (String name : buttonNames) {
            JButton button = new JButton(name);
            subButton.put(name, button);
            menuContentPanel.add(button);
        }

        String[] penSizes = { "Small", "Medium", "Large" };
        for (String size : penSizes) {
            JButton button = new JButton(size);
            penSizeDisplay.put(size, button);
            menuContentPanel.add(button);
        }

        speedDisplay = new JTextField("0.0");
        speedDisplay.setEditable(true);
        menuContentPanel.add(speedDisplay);

        colorPalletDisplay = new JColorChooser();
        menuContentPanel.add(colorPalletDisplay);

        MenuDisplay.add(menuContentPanel);

        setVisible(true);

        messageTimer = new Timer(MESSAGE_DISPLAY_DURATION, e -> {
            saveMessage = null;
            repaint();
            messageTimer.stop();
        });
        messageTimer.setRepeats(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (model == null) {
            return;
        }

        AffineTransform originalTransform = g2d.getTransform();

        g2d.translate(viewOffset.x, viewOffset.y);
        g2d.scale(scale, scale);

        Point2D.Double spurPosition = model.getSpurGearPosition();
        if (spurPosition != null) {
            displaySpur(g2d, spurPosition);
        }

        Point2D.Double pinionPosition = model.getPinionGearPosition();
        if (pinionPosition != null) {
            displayPinion(g2d, pinionPosition);
        }

        displaySpirographLocus(g2d);

        Point2D.Double penPosition = model.getPenPosition();
        Color penColor = model.getPenColor();
        if (penPosition != null) {
            displayDrawPen(g2d, penPosition, penColor);
        }

        // スパーギア定義中の仮描画
        if (isDefiningSpurGear && spurGearCenterScreen != null) {
            Point2D.Double centerWorld = screenToWorld(spurGearCenterScreen);
            double tempRadius = 0;
            if (currentDragPointScreen != null) {
                tempRadius = spurGearCenterScreen.distance(currentDragPointScreen) / scale;
            }

            g2d.setColor(Color.ORANGE);
            g2d.setStroke(new BasicStroke(1.5f));
            double x = centerWorld.x - tempRadius;
            double y = centerWorld.y - tempRadius;
            g2d.drawOval((int) x, (int) y, (int) (tempRadius * 2), (int) (tempRadius * 2));
        }

        // ピニオンギア定義中の仮描画
        if (isDefiningPinionGear && pinionGearCenterScreen != null) {
            Point2D.Double centerWorld = screenToWorld(pinionGearCenterScreen);
            double tempRadius = 0;
            if (currentDragPointScreenForPinion != null) {
                tempRadius = pinionGearCenterScreen.distance(currentDragPointScreenForPinion) / scale;
            }

            g2d.setColor(Color.MAGENTA);
            g2d.setStroke(new BasicStroke(1.5f));
            double x = centerWorld.x - tempRadius;
            double y = centerWorld.y - tempRadius;
            g2d.drawOval((int) x, (int) y, (int) (tempRadius * 2), (int) (tempRadius * 2));
        }

        g2d.setTransform(originalTransform);

        g2d.setColor(Color.BLACK);
        g2d.drawString("Scale: " + getScalePercent(), 10, getHeight() - 10);

        if (saveMessage != null) {
            drawSaveMessage(g2d);
        }
    }

    private void drawSaveMessage(Graphics2D g2d) {
        Font font = new Font("SansSerif", Font.BOLD, 24);
        g2d.setFont(font);
        FontMetrics metrics = g2d.getFontMetrics(font);
        int stringWidth = metrics.stringWidth(saveMessage);
        int stringHeight = metrics.getHeight();

        int padding = 20;
        int boxWidth = stringWidth + 2 * padding;
        int boxHeight = stringHeight + 2 * padding;

        int x = (getWidth() - boxWidth) / 2;
        int y = (getHeight() - boxHeight) / 2;

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(x, y, boxWidth, boxHeight, 20, 20);

        g2d.setColor(Color.WHITE);
        g2d.drawString(saveMessage, x + padding, y + padding + metrics.getAscent());
    }

    public void displayPinion(Graphics2D g, Point2D.Double position) {
        Color originalColor = g.getColor();
        java.awt.Stroke originalStroke = g.getStroke();

        g.setColor(Color.BLUE);
        g.setStroke(new BasicStroke(2.0f));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double radius = model.getPinionGearRadius();
        double x = position.x - radius;
        double y = position.y - radius;

        g.drawOval((int) x, (int) y, (int) (radius * 2), (int) (radius * 2));

        g.setColor(originalColor);
        g.setStroke(originalStroke);
    }

    public void displayMousePointer(Graphics2D g, Point2D.Double position, Color color) {
        // 未実装
    }

    public void displaySpur(Graphics2D g, Point2D.Double position) {
        Color originalColor = g.getColor();
        java.awt.Stroke originalStroke = g.getStroke();

        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(2.0f));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double radius = model.getSpurGearRadius();
        double x = position.x - radius;
        double y = position.y - radius;

        g.drawOval((int) x, (int) y, (int) (radius * 2), (int) (radius * 2));

        g.setColor(originalColor);
        g.setStroke(originalStroke);
    }

    public void displayDrawPen(Graphics2D g, Point2D.Double position, Color color) {
        Color originalColor = g.getColor();
        java.awt.Stroke originalStroke = g.getStroke();

        g.setColor(color != null ? color : Color.GREEN);
        double penSize = model.getPenSize();
        g.setStroke(new BasicStroke((float) penSize));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.fillOval((int) (position.x - penSize / 2), (int) (position.y - penSize / 2), (int) penSize, (int) penSize);

        g.setColor(originalColor);
        g.setStroke(originalStroke);
    }

    private void displaySpirographLocus(Graphics2D g2d) {
        Color originalColor = g2d.getColor();
        java.awt.Stroke originalStroke = g2d.getStroke();

        List<Point2D.Double> locusToDraw;
        Color penColorToUse;
        double penSizeToUse;

        if (loadedLocusData != null && !loadedLocusData.isEmpty() && loadedPenColor != null && loadedPenSize != -1.0) {
            locusToDraw = loadedLocusData;
            penColorToUse = loadedPenColor;
            penSizeToUse = loadedPenSize;
        } else {
            locusToDraw = model.getLocus();
            penColorToUse = model.getPenColor();
            penSizeToUse = model.getPenSize();
        }

        g2d.setColor(penColorToUse);
        g2d.setStroke(new BasicStroke((float) penSizeToUse));

        if (locusToDraw != null && locusToDraw.size() > 1) {
            for (int i = 0; i < locusToDraw.size() - 1; i++) {
                Point2D.Double p1 = locusToDraw.get(i);
                Point2D.Double p2 = locusToDraw.get(i + 1);
                g2d.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
            }
        }

        g2d.setColor(originalColor);
        g2d.setStroke(originalStroke);
    }

    public void scaling(boolean zoomIn) {
        double scaleChange = zoomIn ? 0.1 : -0.1;
        double newScale = scale + scaleChange;

        if (newScale >= MIN_SCALE && newScale <= MAX_SCALE) {
            scale = newScale;
            repaint();
        }
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double newScale) {
        if (newScale >= MIN_SCALE && newScale <= MAX_SCALE) {
            scale = newScale;
            repaint();
        }
    }

    public String getScalePercent() {
        return (int) (scale * 100) + "%";
    }

    public File chooseSaveFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    public File chooseLoadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    public Point2D.Double screenToWorld(Point screenPoint) {
        double worldX = (screenPoint.getX() / scale) - (viewOffset.x / scale);
        double worldY = (screenPoint.getY() / scale) - (viewOffset.y / scale);
        return new Point2D.Double(worldX, worldY);
    }

    public void setDefiningSpurGear(boolean defining) {
        this.isDefiningSpurGear = defining;
        if (!defining) {
            clearSpurGearDefinition();
        }
        repaint();
    }

    public void setSpurGearCenterScreen(Point p) {
        this.spurGearCenterScreen = p;
        this.currentDragPointScreen = p;
        repaint();
    }

    public void setCurrentDragPointScreen(Point p) {
        this.currentDragPointScreen = p;
        repaint();
    }

    public void clearSpurGearDefinition() {
        this.isDefiningSpurGear = false;
        this.spurGearCenterScreen = null;
        this.currentDragPointScreen = null;
        repaint();
    }

    public void setDefiningPinionGear(boolean defining) {
        this.isDefiningPinionGear = defining;
        if (!defining) {
            clearPinionGearDefinition();
        }
        repaint();
    }

    public void setPinionGearCenterScreen(Point p) {
        this.pinionGearCenterScreen = p;
        this.currentDragPointScreenForPinion = p;
        repaint();
    }

    public void setCurrentDragPointScreenForPinion(Point p) {
        this.currentDragPointScreenForPinion = p;
        repaint();
    }

    public void clearPinionGearDefinition() {
        this.isDefiningPinionGear = false;
        this.pinionGearCenterScreen = null;
        this.currentDragPointScreenForPinion = null;
        repaint();
    }

    public Point2D.Double getViewOffset() {
        return viewOffset;
    }

    public void setViewOffset(Point2D.Double offset) {
        this.viewOffset = offset;
        repaint();
    }

    public void setLocusData(List<Point2D.Double> locus, Color penColor, double penSize) {
        this.loadedLocusData = locus;
        this.loadedPenColor = penColor;
        this.loadedPenSize = penSize;
        repaint();
    }

    public void clearLoadedLocusData() {
        this.loadedLocusData = null;
        this.loadedPenColor = null;
        this.loadedPenSize = -1.0;
        repaint();
    }

    public void displaySaveSuccessMessage(String message) {
        this.saveMessage = message;
        repaint();
        if (messageTimer.isRunning()) {
            messageTimer.restart();
        } else {
            messageTimer.start();
        }
    }

    public void showMenu(int x, int y) {
        MenuDisplay.show(this, x, y);
    }

    // Controllerからの画面移動(pan)操作に対応
    public void pan(int dx, int dy) {
        viewOffset.x += dx;
        viewOffset.y += dy;
        repaint();
    }
}
