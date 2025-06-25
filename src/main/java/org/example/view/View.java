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
import java.awt.Cursor;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JLabel;
import java.util.Hashtable;
import java.text.DecimalFormat; // DecimalFormatをインポート

/**
 * スピログラフアプリケーションのViewクラス。
 */
public class View extends JPanel {

    /** Modelインスタンス */
    private Model model;

    /** メニュー表示用ポップアップ */
    public JPopupMenu MenuDisplay;
    /** 速度表示・入力欄 */
    public JTextField speedDisplay;
    /** カラーパレット (ダイアログとして使用) */
    public JColorChooser colorPalletDisplay;

    /** 拡大縮小率 */
    private double scale = 1.0;
    private static final double MIN_SCALE = 0.1; // 最小スケールをより小さく設定
    private static final double MAX_SCALE = 5.0; // 最大スケールをより大きく設定

    /** ビューのオフセット（パン用） */
    private Point2D.Double viewOffset = new Point2D.Double(0, 0);

    /** スパーギア定義中フラグ */
    private boolean isDefiningSpurGear = false;
    /** スパーギア中心（スクリーン座標） */
    private Point spurGearCenterScreen = null;
    /** スパーギアドラッグ点（スクリーン座標） */
    private Point currentDragPointScreen = null;

    /** ピニオンギア定義中フラグ */
    private boolean isDefiningPinionGear = false;
    /** ピニオンギア中心（スクリーン座標） */
    private Point pinionGearCenterScreen = null;
    /** ピニオンギアドラッグ点（スクリーン座標） */
    private Point currentDragPointScreenForPinion = null;

    /** ロードされた軌跡データ */
    private List<Point2D.Double> loadedLocusData = null;
    /** ロードされたペン色 */
    private Color loadedPenColor = null;
    /** ロードされたペンサイズ */
    private double loadedPenSize = -1.0;

    /** 保存メッセージ */
    private String saveMessage = null;
    /** メッセージ表示用タイマー */
    private Timer messageTimer;
    private static final int MESSAGE_DISPLAY_DURATION = 2000;

    /** メニューボタンリスナーインターフェース */
    public interface MenuButtonListener {
        void onMenuButtonClicked(String buttonName);
        void onColorSelected(Color color);
        void onSpeedSelected(double speed);
    }
    private MenuButtonListener menuButtonListener;

    /** ペン先表示フラグ */
    private boolean showPenTip = true;

    /** DecimalFormat for scale percentage display */
    private DecimalFormat percentFormat = new DecimalFormat("0.0%");

    /** メニューボタンリスナーを登録 */
    public void setMenuButtonListener(MenuButtonListener listener) {
        this.menuButtonListener = listener;
    }

    /**
     * Viewのコンストラクタ
     * @param model Modelインスタンス
     */
    public View(Model model) {
        this.model = model;

        setLayout(null);
        setBackground(Color.WHITE);

        MenuDisplay = new JPopupMenu();

        // MenuButtonListenerを介してアクションを処理する共通のActionListener
        ActionListener commonMenuListener = e -> {
            if (menuButtonListener != null) {
                JMenuItem source = (JMenuItem) e.getSource();
                String command = source.getText();
                menuButtonListener.onMenuButtonClicked(command);
            }
        };

        // --- メインボタン群をJMenuItemとしてJPopupMenuに追加 ---
        String[] mainButtonNames = { "Start", "Stop", "Clear", "Save", "Load" };
        for (String name : mainButtonNames) {
            JMenuItem item = new JMenuItem(name);
            item.addActionListener(commonMenuListener);
            MenuDisplay.add(item);
        }

        MenuDisplay.addSeparator(); // 区切り線を追加

        // --- ペンサイズをPenSizeサブメニューにまとめる ---
        JMenu penSizeMenu = new JMenu("PenSize");
        String[] penSizes = { "Small", "Medium", "Large" };
        for (String size : penSizes) {
            JMenuItem item = new JMenuItem(size);
            item.addActionListener(commonMenuListener);
            penSizeMenu.add(item);
        }
        MenuDisplay.add(penSizeMenu);

        // --- カラーパレット表示メニュー項目を追加 ---
        JMenuItem chooseColorItem = new JMenuItem("色を選択...");
        chooseColorItem.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(View.this, "色を選択", model.getPenColor());
            if (newColor != null) {
                if (menuButtonListener != null) {
                    menuButtonListener.onColorSelected(newColor); // 新しい色をリスナーに通知
                }
            }
        });
        MenuDisplay.add(chooseColorItem);

        // --- スピード選択用JSliderを追加 ---
        MenuDisplay.addSeparator(); // 区切り線を追加

        // スライダーとラベルを格納するためのパネル
        JPanel speedPanel = new JPanel();
        speedPanel.setLayout(new BoxLayout(speedPanel, BoxLayout.Y_AXIS)); // 垂直方向に要素を配置

        JLabel speedLabel = new JLabel("スピード:");
        speedLabel.setAlignmentX(CENTER_ALIGNMENT); // パネル内で中央揃え

        int initialSpeed = (int) model.getPinionGearSpeed(); // Modelから実際の速度を取得
        if (initialSpeed < 1) initialSpeed = 1;
        if (initialSpeed > 100) initialSpeed = 100;


        JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, initialSpeed);
        speedSlider.setPaintTicks(true);
        speedSlider.setSnapToTicks(true);

        // カスタムラベルテーブルを作成
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(1, new JLabel("1"));
        labelTable.put(100, new JLabel("100"));
        speedSlider.setLabelTable(labelTable);
        speedSlider.setPaintLabels(true);

        // スライダーの値が変更されたときのリスナー
        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!speedSlider.getValueIsAdjusting()) {
                    if (menuButtonListener != null) {
                        menuButtonListener.onSpeedSelected(speedSlider.getValue());
                    }
                }
            }
        });

        speedPanel.add(speedLabel);
        speedPanel.add(speedSlider);

        MenuDisplay.add(speedPanel); // パネルをポップアップメニューに追加

        speedDisplay = new JTextField("0.0");
        speedDisplay.setEditable(true);

        colorPalletDisplay = new JColorChooser();

        setVisible(true);

        messageTimer = new Timer(MESSAGE_DISPLAY_DURATION, e -> {
            saveMessage = null;
            repaint();
            messageTimer.stop();
        });
        messageTimer.setRepeats(false);
    }

    /**
     * 描画処理
     * @param g グラフィックス
     */
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

        // ズームとパンを適用
        g2d.translate(viewOffset.x, viewOffset.y);
        g2d.scale(scale, scale);

        // --- 中心点の描画（r=2） ---
        // スパーギア中心
        Point2D.Double spurPosition = model.getSpurGearPosition();
        if (spurPosition != null) {
            g2d.setColor(Color.RED);
            int r = 2;
            g2d.fillOval((int)(spurPosition.x - r), (int)(spurPosition.y - r), r * 2, r * 2);
            displaySpur(g2d, spurPosition);
        }

        // ピニオンギア中心
        Point2D.Double pinionPosition = model.getPinionGearPosition();
        if (pinionPosition != null) {
            g2d.setColor(Color.BLUE);
            int r = 2;
            g2d.fillOval((int)(pinionPosition.x - r), (int)(pinionPosition.y - r), r * 2, r * 2);
            displayPinion(g2d, pinionPosition);
        }

        displaySpirographLocus(g2d);

        Point2D.Double penPosition = model.getPenPosition();
        Color penColor = model.getPenColor();
        if (penPosition != null && showPenTip) {
            displayDrawPen(g2d, penPosition, penColor);
        }

        // スパーギア定義中の仮描画
        if (isDefiningSpurGear && spurGearCenterScreen != null) {
            Point2D.Double centerWorld = screenToWorld(spurGearCenterScreen);
            double tempRadius = 0;
            if (currentDragPointScreen != null) {
                // スケールを考慮して半径を計算
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
                // スケールを考慮して半径を計算
                tempRadius = pinionGearCenterScreen.distance(currentDragPointScreenForPinion) / scale;
            }

            g2d.setColor(Color.MAGENTA);
            g2d.setStroke(new BasicStroke(1.5f));
            double x = centerWorld.x - tempRadius;
            double y = centerWorld.y - tempRadius;
            g2d.drawOval((int) x, (int) y, (int) (tempRadius * 2), (int) (tempRadius * 2));
        }

        // 元のTransformに戻す
        g2d.setTransform(originalTransform);

        // スケール表示
        g2d.setColor(Color.BLACK);
        g2d.drawString("Scale: " + getScalePercent(), 10, getHeight() - 10);

        // 保存メッセージ表示
        if (saveMessage != null) {
            drawSaveMessage(g2d);
        }
    }

    /**
     * 保存メッセージを描画
     * @param g2d グラフィックス2D
     */
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

    /**
     * ピニオンギアを描画
     * @param g グラフィックス2D
     * @param position ギア中心座標
     */
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

    /**
     * マウスポインタを描画（未実装）
     * @param g グラフィックス2D
     * @param position 座標
     * @param color 色
     */
    public void displayMousePointer(Graphics2D g, Point2D.Double position, Color color) {
        // 未実装
    }

    /**
     * スパーギアを描画
     * @param g グラフィックス2D
     * @param position ギア中心座標
     */
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

    /**
     * ペンを描画
     * @param g グラフィックス2D
     * @param position ペン座標
     * @param color ペン色
     */
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

    /**
     * スピログラフの軌跡を描画
     * @param g2d グラフィックス2D
     */
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

    /**
     * スパーギアとピニオンギアの半径を連動して変更する
     * @param newSpurRadius 新しいスパーギア半径
     */
    public void changeSpurAndPinionRadius(double newSpurRadius) {
        double oldSpurRadius = model.getSpurGearRadius();
        double oldPinionRadius = model.getPinionGearRadius();
        if (oldSpurRadius == 0) return; // 0除算防止
        double ratio = newSpurRadius / oldSpurRadius;
        double newPinionRadius = oldPinionRadius * ratio;
        model.setSpurRadius(newSpurRadius);
        model.changePinionGearRadius(newPinionRadius);
        repaint();
    }

    /**
     * マウスカーソル位置を中心に拡大縮小する
     * @param screenPoint スクリーン座標でのズーム中心点
     * @param zoomFactor ズーム倍率
     */
    public void zoomAt(Point screenPoint, double zoomFactor) {
        // 現在のスクリーン座標での中心点をワールド座標に変換
        Point2D.Double worldPointBeforeZoom = screenToWorld(screenPoint);

        // 新しいスケールを計算し、範囲内に収める
        double newScale = scale * zoomFactor;
        if (newScale < MIN_SCALE) {
            newScale = MIN_SCALE;
        } else if (newScale > MAX_SCALE) {
            newScale = MAX_SCALE;
        }

        // スケールが変更されない場合は何もしない
        if (newScale == scale) {
            return;
        }

        // 新しいスケールを設定
        scale = newScale;

        // ズーム後のスクリーン座標での中心点
        Point2D.Double screenPointAfterZoom = new Point2D.Double(
            worldPointBeforeZoom.x * scale + viewOffset.x,
            worldPointBeforeZoom.y * scale + viewOffset.y
        );

        // ズーム後のオフセットを計算
        viewOffset.x += (screenPoint.x - screenPointAfterZoom.x);
        viewOffset.y += (screenPoint.y - screenPointAfterZoom.y);

        repaint();
    }


    /**
     * 現在のスケール値を取得
     * @return スケール値
     */
    public double getScale() {
        return scale;
    }

    /**
     * スケール値を設定
     * @param newScale 新しいスケール
     */
    public void setScale(double newScale) {
        if (newScale >= MIN_SCALE && newScale <= MAX_SCALE) {
            scale = newScale;
            repaint();
        }
    }

    /**
     * スケール値をパーセント表記で取得
     * @return 例: "100.0%"
     */
    public String getScalePercent() {
        return percentFormat.format(scale);
    }

    /**
     * 保存ファイル選択ダイアログ
     * @return 選択ファイル
     */
    public File chooseSaveFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    /**
     * 読込ファイル選択ダイアログ
     * @return 選択ファイル
     */
    public File chooseLoadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    /**
     * スクリーン座標をワールド座標に変換
     * @param screenPoint スクリーン座標
     * @return ワールド座標
     */
    public Point2D.Double screenToWorld(Point screenPoint) {
        double worldX = (screenPoint.getX() - viewOffset.x) / scale;
        double worldY = (screenPoint.getY() - viewOffset.y) / scale;
        return new Point2D.Double(worldX, worldY);
    }

    /**
     * スパーギア定義モード設定
     * @param defining trueで定義中
     */
    public void setDefiningSpurGear(boolean defining) {
        this.isDefiningSpurGear = defining;
        if (!defining) {
            clearSpurGearDefinition();
        }
        repaint();
    }

    /**
     * スパーギア中心（スクリーン座標）設定
     * @param p スクリーン座標
     */
    public void setSpurGearCenterScreen(Point p) {
        this.spurGearCenterScreen = p;
        this.currentDragPointScreen = p;
        repaint();
    }

    /**
     * スパーギアドラッグ点（スクリーン座標）設定
     * @param p スクリーン座標
     */
    public void setCurrentDragPointScreen(Point p) {
        this.currentDragPointScreen = p;
        repaint();
    }

    /**
     * スパーギア定義情報クリア
     */
    public void clearSpurGearDefinition() {
        this.isDefiningSpurGear = false;
        this.spurGearCenterScreen = null;
        this.currentDragPointScreen = null;
        repaint();
    }

    /**
     * ピニオンギア定義モード設定
     * @param defining trueで定義中
     */
    public void setDefiningPinionGear(boolean defining) {
        this.isDefiningPinionGear = defining;
        if (!defining) {
            clearPinionGearDefinition();
        }
        repaint();
    }

    /**
     * ピニオンギア中心（スクリーン座標）設定
     * @param p スクリーン座標
     */
    public void setPinionGearCenterScreen(Point p) {
        this.pinionGearCenterScreen = p;
        this.currentDragPointScreenForPinion = p;
        repaint();
    }

    /**
     * ピニオンギアドラッグ点（スクリーン座標）設定
     * @param p スクリーン座標
     */
    public void setCurrentDragPointScreenForPinion(Point p) {
        this.currentDragPointScreenForPinion = p;
        repaint();
    }

    /**
     * ピニオンギア定義情報クリア
     */
    public void clearPinionGearDefinition() {
        this.isDefiningPinionGear = false;
        this.pinionGearCenterScreen = null;
        this.currentDragPointScreenForPinion = null;
        repaint();
    }

    /**
     * ビューオフセット取得
     * @return オフセット
     */
    public Point2D.Double getViewOffset() {
        return viewOffset;
    }

    /**
     * ビューオフセット設定
     * @param offset 新しいオフセット
     */
    public void setViewOffset(Point2D.Double offset) {
        this.viewOffset = offset;
        repaint();
    }

    /**
     * 軌跡データ・ペン情報をViewにセット
     * @param locus 軌跡
     * @param penColor ペン色
     * @param penSize ペンサイズ
     */
    public void setLocusData(List<Point2D.Double> locus, Color penColor, double penSize) {
        this.loadedLocusData = locus;
        this.loadedPenColor = penColor;
        this.loadedPenSize = penSize; // ここを修正：-1.0ではなくpenSizeをセット
        repaint();
    }

    /**
     * ロード済み軌跡データをクリア
     */
    public void clearLoadedLocusData() {
        this.loadedLocusData = null;
        this.loadedPenColor = null;
        this.loadedPenSize = -1.0;
        repaint();
    }

    /**
     * 保存成功メッセージを表示
     * @param message メッセージ
     */
    public void displaySaveSuccessMessage(String message) {
        this.saveMessage = message;
        repaint();
        if (messageTimer.isRunning()) {
            messageTimer.restart();
        } else {
            messageTimer.start();
        }
    }

    /**
     * メニューを表示
     * @param x X座標
     * @param y Y座標
     */
    public void showMenu(int x, int y) {
        MenuDisplay.show(this, x, y);
    }

    /**
     * 画面パン（移動）
     * @param dx X方向移動量
     * @param dy Y方向移動量
     */
    public void pan(int dx, int dy) {
        viewOffset.x += dx;
        viewOffset.y += dy;
        repaint();
    }

    /**
     * マウス位置に応じてカーソル形状を更新する
     * @param mousePoint スクリーン座標でのマウス位置
     */
    public void updateCursor(Point mousePoint) {
        Point2D world = screenToWorld(mousePoint);

        // スパーギア中心
        Point2D spurCenter = model.getSpurGearPosition();
        double spurRadius = model.getSpurGearRadius();
        // ピニオンギア中心
        Point2D pinionCenter = model.getPinionGearPosition();
        double pinionRadius = model.getPinionGearRadius();

        if (spurCenter != null && world.distance(spurCenter) < 10 / scale) { // スケールを考慮
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        } else if (spurCenter != null && Math.abs(world.distance(spurCenter) - spurRadius) < 10 / scale) { // スケールを考慮
            setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
        } else if (pinionCenter != null && world.distance(pinionCenter) < 10 / scale) { // スケールを考慮
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /** ペン先を非表示にする */
    public void hidePenTip() {
        showPenTip = false;
        repaint();
    }

    /** ペン先を表示する */
    public void showPenTip() {
        showPenTip = true;
        repaint();
    }
}
