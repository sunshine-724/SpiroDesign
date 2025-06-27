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
import java.text.DecimalFormat;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.example.lib.PathSegment;

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
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 5.0;

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

    /** ロードされた軌跡データをPathSegmentのリストで管理 */
    private List<PathSegment> loadedPathSegments = null;
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

    /** ファイル拡張子定義 */
    private static final String SPIRO_EXTENSION = "spiro";

    /** ファイルチューザー */
    private JFileChooser fileChooser;

    /**
     * メニューボタンリスナーを登録
     * @param listener リスナー
     */
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

        // ファイルチューザーの初期化
        fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Spiro Files (*.spiro)", SPIRO_EXTENSION);
        fileChooser.setFileFilter(filter);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home"))); // デフォルトディレクトリ設定

        // MenuButtonListenerを介してアクションを処理する共通のActionListener
        ActionListener commonMenuListener = e -> {
            if (menuButtonListener != null) {
                JMenuItem source = (JMenuItem) e.getSource();
                String command = source.getText();
                menuButtonListener.onMenuButtonClicked(command);
            }
        };

        // メインボタン群をJMenuItemとしてJPopupMenuに追加
        String[] mainButtonNames = { "Start", "Stop", "Clear", "Save", "Load" };
        for (String name : mainButtonNames) {
            JMenuItem item = new JMenuItem(name);
            item.addActionListener(commonMenuListener);
            MenuDisplay.add(item);
        }

        MenuDisplay.addSeparator();

        // ペンサイズをPenSizeサブメニューにまとめる
        JMenu penSizeMenu = new JMenu("PenSize");
        String[] penSizes = { "Small", "Medium", "Large" };
        for (String size : penSizes) {
            JMenuItem item = new JMenuItem(size);
            item.addActionListener(commonMenuListener);
            penSizeMenu.add(item);
        }
        MenuDisplay.add(penSizeMenu);

        // カラーパレット表示メニュー項目を追加
        JMenuItem chooseColorItem = new JMenuItem("色を選択...");
        chooseColorItem.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(View.this, "色を選択", model.getPenColor());
            if (newColor != null) {
                if (menuButtonListener != null) {
                    menuButtonListener.onColorSelected(newColor);
                }
            }
        });
        MenuDisplay.add(chooseColorItem);

        // スピード選択用JSliderを追加
        MenuDisplay.addSeparator();

        JPanel speedPanel = new JPanel();
        speedPanel.setLayout(new BoxLayout(speedPanel, BoxLayout.Y_AXIS));
        JLabel speedLabel = new JLabel("スピード:");
        speedLabel.setAlignmentX(CENTER_ALIGNMENT);

        // スライダーの最小値を1、最大値を10に変更し、現在のモデルの速度を新しいスケールに合わせる
        // model.getPinionGearSpeed()は実際の速度 (0.1〜1.0) を返すため、スライダーの表示値 (1〜10) に変換
        int initialSliderValue = (int) (model.getPinionGearSpeed() * 10.0);
        // 新しい最小値と最大値に合わせてクランプ
        if (initialSliderValue < 1) initialSliderValue = 1;
        if (initialSliderValue > 10) initialSliderValue = 10;
        JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, initialSliderValue); // スライダーの範囲を1〜10に
        speedSlider.setPaintTicks(true);
        speedSlider.setSnapToTicks(true);
        speedSlider.setMajorTickSpacing(1); // 目盛り間隔を1に
        speedSlider.setMinorTickSpacing(1); // 小目盛り間隔を1に
        speedSlider.setPaintLabels(true);

        // カスタムラベルテーブルを作成 (1から10まで)
        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        for (int i = 1; i <= 10; i++) {
            labels.put(i, new JLabel(String.valueOf(i)));
        }
        speedSlider.setLabelTable(labels);

        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // スライダーが調整中ではない場合のみイベントを通知
                if (!speedSlider.getValueIsAdjusting()) {
                    if (menuButtonListener != null) {
                        menuButtonListener.onSpeedSelected(speedSlider.getValue());
                    }
                }
            }
        });
        speedPanel.add(speedLabel);
        speedPanel.add(speedSlider);
        MenuDisplay.add(speedPanel);

        setComponentPopupMenu(MenuDisplay); // 右クリックでメニュー表示

        // メッセージ表示用タイマーの設定
        messageTimer = new Timer(MESSAGE_DISPLAY_DURATION, e -> {
            saveMessage = null;
            repaint(); // メッセージ非表示後に再描画
        });
        messageTimer.setRepeats(false); // 一度だけ実行
    }

    /**
     * 描画処理
     * @param g グラフィックス
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // アンチエイリアシングを有効にする
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // 背景をクリア
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (model == null) {
            return;
        }

        // ビューの拡大縮小とパンを適用
        AffineTransform originalTransform = g2d.getTransform();
        AffineTransform transform = new AffineTransform();
        transform.translate(viewOffset.x, viewOffset.y);
        transform.scale(scale, scale);
        g2d.transform(transform);

        // Modelからギアと軌跡のデータを取得して描画
        Point2D.Double spurGearPos = model.getSpurGearPosition();
        double spurGearRadius = model.getSpurGearRadius();
        Color spurGearColor = model.getSpurGearColor();

        Point2D.Double pinionGearPos = model.getPinionGearPosition();
        double pinionGearRadius = model.getPinionGearRadius();
        Color pinionGearColor = model.getPinionGearColor();

        double penSize = model.getPenSize();
        Color penColor = model.getPenColor();
        Point2D.Double penPos = model.getPenPosition();

        // 描画ロジック (変更なし)
        // 軌跡の描画
        List<PathSegment> allPathSegments = model.getPathSegments();
        if (allPathSegments != null) {
            for (PathSegment segment : allPathSegments) {
                g2d.setColor(segment.getColor());
                List<Point2D.Double> points = segment.getPoints();
                if (points.size() > 1) {
                    g2d.setStroke(new BasicStroke((float) penSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    for (int i = 0; i < points.size() - 1; i++) {
                        Point2D.Double p1 = points.get(i);
                        Point2D.Double p2 = points.get(i + 1);
                        g2d.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
                    }
                }
            }
        }

        // スパーギアの描画 (円)
        if (spurGearPos != null) {
            g2d.setColor(spurGearColor);
            g2d.drawOval((int) (spurGearPos.x - spurGearRadius), (int) (spurGearPos.y - spurGearRadius),
                    (int) (spurGearRadius * 2), (int) (spurGearRadius * 2));
        }

        // ピニオンギアの描画 (円)
        if (pinionGearPos != null) {
            g2d.setColor(pinionGearColor);
            g2d.drawOval((int) (pinionGearPos.x - pinionGearRadius), (int) (pinionGearPos.y - pinionGearRadius),
                    (int) (pinionGearRadius * 2), (int) (pinionGearRadius * 2));
        }

        // ペンの描画
        if (showPenTip && penPos != null) {
            g2d.setColor(penColor);
            g2d.fillOval((int) (penPos.x - penSize / 2), (int) (penPos.y - penSize / 2),
                    (int) penSize, (int) penSize);
        }

        // スパーギア定義中のガイド描画
        if (isDefiningSpurGear && spurGearCenterScreen != null && currentDragPointScreen != null) {
            g2d.setTransform(originalTransform); // 座標変換をリセットしてスクリーン座標で描画
            g2d.setColor(Color.LIGHT_GRAY);
            Point2D.Double startWorld = screenToWorld(spurGearCenterScreen);
            Point2D.Double endWorld = screenToWorld(currentDragPointScreen);
            double currentRadius = startWorld.distance(endWorld);
            g2d.drawOval((int) (spurGearCenterScreen.x - currentRadius * scale),
                    (int) (spurGearCenterScreen.y - currentRadius * scale),
                    (int) (currentRadius * scale * 2), (int) (currentRadius * scale * 2));
            g2d.setTransform(transform); // 元の座標変換に戻す
        }
        // ピニオンギア定義中のガイド描画
        if (isDefiningPinionGear && pinionGearCenterScreen != null && currentDragPointScreenForPinion != null) {
            g2d.setTransform(originalTransform); // 座標変換をリセットしてスクリーン座標で描画
            g2d.setColor(Color.LIGHT_GRAY);
            Point2D.Double startWorld = screenToWorld(pinionGearCenterScreen);
            Point2D.Double endWorld = screenToWorld(currentDragPointScreenForPinion);
            double currentRadius = startWorld.distance(endWorld);
            g2d.drawOval((int) (pinionGearCenterScreen.x - currentRadius * scale),
                    (int) (pinionGearCenterScreen.y - currentRadius * scale),
                    (int) (currentRadius * scale * 2), (int) (currentRadius * scale * 2));
            g2d.setTransform(transform); // 元の座標変換に戻す
        }

        // 元の変換を復元
        g2d.setTransform(originalTransform);

        // 保存メッセージの表示
        if (saveMessage != null) {
            drawSaveMessage(g2d); // drawSaveMessageメソッドを呼び出す
        }

        // 拡大縮小率の表示 (右下)
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
        String scaleText = percentFormat.format(scale);
        FontMetrics fmScale = g2d.getFontMetrics();
        int scaleX = getWidth() - fmScale.stringWidth(scaleText) - 10;
        int scaleY = getHeight() - 10;
        g2d.drawString(scaleText, scaleX, scaleY);
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
     * スクリーン座標をワールド座標に変換する。
     * @param screenPoint スクリーン座標
     * @return ワールド座標
     */
    public Point2D.Double screenToWorld(Point screenPoint) {
        double worldX = (screenPoint.x - viewOffset.x) / scale;
        double worldY = (screenPoint.y - viewOffset.y) / scale;
        return new Point2D.Double(worldX, worldY);
    }

    /**
     * ワールド座標をスクリーン座標に変換する。
     * @param worldPoint ワールド座標
     * @return スクリーン座標
     */
    public Point worldToScreen(Point2D.Double worldPoint) {
        int screenX = (int) (worldPoint.x * scale + viewOffset.x);
        int screenY = (int) (worldPoint.y * scale + viewOffset.y);
        return new Point(screenX, screenY);
    }

    /**
     * 指定された点を中心にビューをズームする。
     * @param centerPoint ズームの中心となるスクリーン座標
     * @param zoomFactor ズーム倍率
     */
    public void zoomAt(Point centerPoint, double zoomFactor) {
        // ズーム範囲を制限
        double newScale = scale * zoomFactor;
        if (newScale < MIN_SCALE) newScale = MIN_SCALE;
        if (newScale > MAX_SCALE) newScale = MAX_SCALE;

        // ズームの中心がワールド座標のどこにあるかを計算
        Point2D.Double worldCenter = screenToWorld(centerPoint);

        // 新しいオフセットを計算
        // (centerPoint.x - newOffsetX) / newScale = worldCenter.x
        // newOffsetX = centerPoint.x - worldCenter.x * newScale
        viewOffset.x = centerPoint.x - worldCenter.x * newScale;
        viewOffset.y = centerPoint.y - worldCenter.y * newScale;

        scale = newScale;
        repaint();
    }

    /**
     * 保存成功メッセージを表示する。
     * @param message 表示するメッセージ
     */
    public void displaySaveSuccessMessage(String message) {
        this.saveMessage = message;
        messageTimer.restart(); // タイマーをリスタート
        repaint();
    }

    /**
     * マウスカーソルを更新する。
     * @param mouseScreenPoint マウスのスクリーン座標
     */
    public void updateCursor(Point mouseScreenPoint) {
        Point2D.Double world = screenToWorld(mouseScreenPoint);
        Point2D.Double spurCenter = model.getSpurGearPosition();
        double spurRadius = model.getSpurGearRadius();
        Point2D.Double pinionCenter = model.getPinionGearPosition();
        double pinionRadius = model.getPinionGearRadius();

        // どの要素の上にマウスがあるかに応じてカーソルを変更
        if (spurCenter != null && world.distance(spurCenter) < 10 / scale) {
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        } else if (spurCenter != null && Math.abs(world.distance(spurCenter) - spurRadius) < 10 / scale) {
            setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
        } else if (pinionCenter != null && world.distance(pinionCenter) < 10 / scale) {
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

    /**
     * ロードされたデータをViewに設定する
     * @param loadedSegments ロードされたPathSegmentのリスト
     * @param penColor ロードされたペンの色
     * @param penSize ロードされたペンのサイズ
     */
    public void setLocusData(List<PathSegment> loadedSegments, Color penColor, double penSize) {
        this.loadedPathSegments = loadedSegments;
        this.loadedPenColor = penColor;
        this.loadedPenSize = penSize;
        repaint(); // データが更新されたので再描画
    }

    /**
     * スパーギアとピニオンギアの半径が変更されたことをViewに通知する。
     * これは現在使用されていませんが、将来的に必要になる可能性を考慮しています。
     * @param newRadius 新しい半径
     */
    public void changeSpurAndPinionRadius(double newRadius) {
        // 現在はこのメソッドでは特に描画関連の処理は行いませんが、
        // 必要に応じてここに描画ロジックを追加できます。
        repaint();
    }

    /**
     * ビューをパンする
     * @param dx X方向の移動量
     * @param dy Y方向の移動量
     */
    public void pan(int dx, int dy) {
        viewOffset.x += dx;
        viewOffset.y += dy;
        repaint();
    }

    /**
     * 現在のスケールを取得
     * @return スケール
     */
    public double getScale() {
        return scale;
    }

    /**
     * ファイル保存ダイアログを開く
     * @return 選択されたファイル
     */
    public File chooseSaveFile() {
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.getName().toLowerCase().endsWith("." + SPIRO_EXTENSION)) {
                selectedFile = new File(selectedFile.getAbsolutePath() + "." + SPIRO_EXTENSION);
            }
            return selectedFile;
        }
        return null;
    }

    /**
     * ファイル読み込みダイアログを開く
     * @return 選択されたファイル
     */
    public File chooseLoadFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    // --- テスト用のゲッターメソッド ---

    /**
     * 現在の保存メッセージを取得
     * @return 保存メッセージ
     */
    public String getSaveMessage() {
        return saveMessage;
    }

    /**
     * スパーギア定義中フラグの状態を取得
     * @return スパーギア定義中であればtrue
     */
    public boolean isDefiningSpurGear() {
        return isDefiningSpurGear;
    }

    /**
     * スパーギア中心（スクリーン座標）を取得
     * @return スパーギア中心のスクリーン座標
     */
    public Point getSpurGearCenterScreen() {
        return spurGearCenterScreen;
    }

    /**
     * スパーギアドラッグ点（スクリーン座標）を取得
     * @return スパーギアドラッグ点のスクリーン座標
     */
    public Point getCurrentDragPointScreen() {
        return currentDragPointScreen;
    }

    /**
     * ピニオンギア中心（スクリーン座標）を取得
     * @return ピニオンギア中心のスクリーン座標
     */
    public Point getPinionGearCenterScreen() {
        return pinionGearCenterScreen;
    }

    /**
     * ピニオンギアドラッグ点（スクリーン座標）を取得
     * @return ピニオンギアドラッグ点のスクリーン座標
     */
    public Point getCurrentDragPointScreenForPinion() {
        return currentDragPointScreenForPinion;
    }

    /**
     * スパーギア定義中フラグを設定
     * @param definingSpurGear trueならスパーギア定義中
     */
    public void setDefiningSpurGear(boolean definingSpurGear) {
        isDefiningSpurGear = definingSpurGear;
    }

    /**
     * スパーギア中心（スクリーン座標）を設定
     * @param spurGearCenterScreen スパーギア中心のスクリーン座標
     */
    public void setSpurGearCenterScreen(Point spurGearCenterScreen) {
        this.spurGearCenterScreen = spurGearCenterScreen;
    }

    /**
     * スパーギアドラッグ点（スクリーン座標）を設定
     * @param currentDragPointScreen スパーギアドラッグ点のスクリーン座標
     */
    public void setCurrentDragPointScreen(Point currentDragPointScreen) {
        this.currentDragPointScreen = currentDragPointScreen;
    }

    /**
     * ピニオンギア定義中フラグを設定
     * @param definingPinionGear trueならピニオンギア定義中
     */
    public void setDefiningPinionGear(boolean definingPinionGear) {
        this.isDefiningPinionGear = definingPinionGear;
    }

    /**
     * ピニオンギア中心（スクリーン座標）を設定
     * @param pinionGearCenterScreen ピニオンギア中心のスクリーン座標
     */
    public void setPinionGearCenterScreen(Point pinionGearCenterScreen) {
        this.pinionGearCenterScreen = pinionGearCenterScreen;
    }

    /**
     * ピニオンギアドラッグ点（スクリーン座標）を設定
     * @param currentDragPointScreenForPinion ピニオンギアドラッグ点のスクリーン座標
     */
    public void setCurrentDragPointScreenForPinion(Point currentDragPointScreenForPinion) {
        this.currentDragPointScreenForPinion = currentDragPointScreenForPinion;
    }
}
