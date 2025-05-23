/*
 * 実際のボタンの配置やサイズはプロジェクトの要件に合わせて調整が必要
   レイアウトマネージャーは要件によって適切なものを選択
   ボタンやテキストフィールドにアクションリスナーを追加する必要（Controller との連携部分
   Graphic2Dは正しくはGraphics2D
 */

package org.example.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform; // AffineTransformをインポート
import java.util.Map;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.example.model.Model;

public class View extends JPanel {

    private Model model;

    public JPanel MenuDisplay;

    public Map<String, JButton> subButton;

    public JTextField speedDisplay;

    public Map<String, JButton> penSizeDisplay;

    public JColorChooser colorPalletDisplay;

    private double scale = 1.0; // デフォルトスケール（100%）
    private static final double MIN_SCALE = 0.5; // 最小スケール（50%）
    private static final double MAX_SCALE = 2.0; // 最大スケール（200%）

    public View(Model model) {
        // Initialize the view
        this.model = model;

        // Set panel properties
        this.setLayout(null); // or use appropriate layout manager
        this.setBackground(Color.WHITE);

        // Initialize UI components
        this.MenuDisplay = new JPanel();
        MenuDisplay.setLayout(null); // or use appropriate layout manager

        // Initialize maps for buttons
        this.subButton = new HashMap<>();
        this.penSizeDisplay = new HashMap<>();

        // Create and add buttons to subButton map
        String[] buttonNames = { "Pen", "SpurGear", "PinionGear", "Start", "Stop", "Clear" };
        for (String name : buttonNames) {
            JButton button = new JButton(name);
            subButton.put(name, button);
            MenuDisplay.add(button);
            // Add button locations and sizes as needed
            // 例: button.setBounds(x, y, width, height);
        }

        // Create and add buttons to penSizeDisplay map
        String[] penSizes = { "Small", "Medium", "Large" };
        for (String size : penSizes) {
            JButton button = new JButton(size);
            penSizeDisplay.put(size, button);
            MenuDisplay.add(button);
            // Add button locations and sizes as needed
            // 例: button.setBounds(x, y, width, height);
        }

        // Initialize text field for speed display
        this.speedDisplay = new JTextField("0.0");
        speedDisplay.setEditable(true);
        MenuDisplay.add(speedDisplay);
        // 例: speedDisplay.setBounds(x, y, width, height);

        // Initialize color chooser
        this.colorPalletDisplay = new JColorChooser();
        MenuDisplay.add(colorPalletDisplay); // メニューパネルに追加
        // 例: colorPalletDisplay.setBounds(x, y, width, height);

        // Add MenuDisplay to the main panel
        this.add(MenuDisplay);

        // Set sizes and positions (仮の値、要調整)
        MenuDisplay.setBounds(10, 10, 200, 600); // Adjust as needed
        // ボタンやテキストフィールドの位置とサイズもここで設定するか、
        // MenuDisplayのレイアウトマネージャーを使う
        int yOffset = 10;
        for (JButton button : subButton.values()) {
            button.setBounds(10, yOffset, 180, 30);
            yOffset += 35;
        }
        speedDisplay.setBounds(10, yOffset, 180, 30);
        yOffset += 35;
        for (JButton button : penSizeDisplay.values()) {
            button.setBounds(10, yOffset, 180, 30);
            yOffset += 35;
        }
        colorPalletDisplay.setBounds(10, yOffset, 180, 250); // カラーチューザーは大きめに

        // Make the panel visible
        this.setVisible(true);
    }

    /**
     * コンポーネントの描画メソッド
     *
     * @param g グラフィックスコンテキスト
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // アンチエイリアス設定
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 背景をクリア
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // モデルが設定されていない場合は何もしない
        if (model == null) {
            return;
        }

        // 現在のTransformを保存
        // これにより、スケーリングが適用された後も、他のUIコンポーネント（MenuDisplayなど）
        // が元のスケールで描画されるようにできます。
        AffineTransform originalTransform = g2d.getTransform();

        // スケーリングの適用
        // これ以降の描画はすべてこのスケールで描画されます
        g2d.scale(scale, scale);

        // スパーギアの描画
        Point2D.Double spurPosition = model.getSpurGearPosition();
        if (spurPosition != null) {
            // displaySpurメソッド内ではすでにGraphics2Dにスケールが適用されているため、
            // ここでさらにscaleを乗算する必要はありません。
            displaySpur(g2d, spurPosition);
        }

        // ピニオンギアの描画
        Point2D.Double pinionPosition = model.getPinionGearPosition();
        if (pinionPosition != null) {
            // displayPinionメソッド内ではすでにGraphics2Dにスケールが適用されているため、
            // ここでさらにscaleを乗算する必要はありません。
            displayPinion(g2d, pinionPosition);
        }

        // スピログラフの軌跡を描画
        displaySpirographPath(g2d);

        // ペンの描画 (現在のペン先の位置)
        Point2D.Double penPosition = model.getPenPosition();
        Color penColor = model.getPenColor();
        if (penPosition != null) {
            // displayDrawPenメソッド内ではすでにGraphics2Dにスケールが適用されているため、
            // ここでさらにscaleを乗算する必要はありません。
            displayDrawPen(g2d, penPosition, penColor);
        }

        // スケールを元に戻す
        // これを忘れると、MenuDisplayなどのUIコンポーネントも拡大縮小されてしまいます。
        g2d.setTransform(originalTransform);

        // 現在のスケールを表示 (スケールが元に戻された後に描画)
        g2d.setColor(Color.BLACK);
        // getWidth()やgetHeight()はパネルの実際のサイズなので、スケールを元に戻した後に描画
        g2d.drawString("Scale: " + getScalePercent(), 10, getHeight() - 10);
    }

    /**
     * ピニオンギアを描画するメソッド
     *
     * @param g        Graphics2Dオブジェクト
     * @param position ピニオンギアの中心位置 (Modelからの生座標)
     */
    public void displayPinion(Graphics2D g, Point2D.Double position) {
        // 元の設定を保存
        Color originalColor = g.getColor();
        java.awt.Stroke originalStroke = g.getStroke();

        // 描画設定
        g.setColor(Color.BLUE);
        g.setStroke(new BasicStroke(2.0f));

        // アンチエイリアス設定はpaintComponentで設定済みだが、念のため
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // モデルからピニオンギアの半径を取得。
        // Graphics2Dにすでにスケールが適用されているため、ここではscaleを乗算しない。
        double radius = model.getPinionGearRadius();

        // 円の左上座標を計算（中心座標から半径分を引く）
        double x = position.x - radius;
        double y = position.y - radius;

        // 円を描画
        g.drawOval((int) x, (int) y, (int) (radius * 2), (int) (radius * 2));

        // グラフィックス設定を元に戻す
        g.setColor(originalColor);
        g.setStroke(originalStroke);
    }

    public void displayMousePointer(Graphics2D g, Point2D.Double position, Color color) {
        // 未実装
    }

    /**
     * スパーギアを描画するメソッド
     *
     * @param g        描画コンテキスト
     * @param position スパーギアの中心位置 (Modelからの生座標)
     */
    public void displaySpur(Graphics2D g, Point2D.Double position) {
        // 元の設定を保存
        Color originalColor = g.getColor();
        java.awt.Stroke originalStroke = g.getStroke();

        // 描画設定
        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(2.0f));

        // アンチエイリアス設定はpaintComponentで設定済みだが、念のため
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // モデルからスパーギアの半径を取得。
        // Graphics2Dにすでにスケールが適用されているため、ここではscaleを乗算しない。
        double radius = model.getSpurGearRadius();

        // 円の左上座標を計算（中心座標から半径分を引く）
        double x = position.x - radius;
        double y = position.y - radius;

        // 円を描画
        g.drawOval((int) x, (int) y, (int) (radius * 2), (int) (radius * 2));

        // 元の設定に戻す
        g.setColor(originalColor);
        g.setStroke(originalStroke);
    }

    /**
     * ペンポイントを描画するメソッド
     *
     * @param g        描画コンテキスト
     * @param position ペンポイントの位置 (Modelからの生座標)
     * @param color    ペンの色（引数として渡されるが、デフォルトは緑色）
     */
    public void displayDrawPen(Graphics2D g, Point2D.Double position, Color color) {
        // 元の設定を保存
        Color originalColor = g.getColor();
        java.awt.Stroke originalStroke = g.getStroke();

        // 描画設定
        // 引数colorが指定されていなければ緑色を使用
        g.setColor(color != null ? color : Color.GREEN);
        // ペン先のサイズはModelから取得するか、固定値とする
        g.setStroke(new BasicStroke(2.0f));
        // ペンポイントのサイズ

        double penSize = model.getPenStrokeWidth(); // ペン先の太さとして利用

        // アンチエイリアス設定はpaintComponentで設定済みだが、念のため
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 点（小さな円）を描画
        g.fillOval((int) (position.x - penSize / 2), (int) (position.y - penSize / 2), penSize, penSize);

        // 元の設定に戻す
        g.setColor(originalColor);
        g.setStroke(originalStroke);
    }

    private void displaySpirographPath(Graphics2D g2d) {
        // 元の設定を保存
        Color originalColor = g2d.getColor();
        java.awt.Stroke originalStroke = g2d.getStroke();

        // 軌跡の描画設定
        g2d.setColor(model.getPenColor());
        g2d.setStroke(new BasicStroke((float) model.getPenStrokeWidth()));

        long startTime = model.getSpirographStartTime();
        long currentTime = model.getSpirographCurrentTime();

        // 描画の解像度 (ミリ秒単位)。この値が小さいほど滑らかだが、計算量が増える
        // 例: 1ミリ秒ごとに点を計算
        long step = 1;

        Point2D.Double prevPoint = null;

        // 描画開始から現在までの軌跡を再計算して描画
        for (long t = 0; t <= currentTime; t += step) {
            Point2D.Double currentPoint = model.getPenPositionAtTime(t);
            if (currentPoint != null) {
                if (prevPoint != null) {
                    // 前の点と現在の点を線で結ぶ
                    g2d.drawLine((int) prevPoint.x, (int) prevPoint.y,
                            (int) currentPoint.x, (int) currentPoint.y);
                }
                prevPoint = currentPoint;
            }
        }

        // 元の設定に戻す
        g2d.setColor(originalColor);
        g2d.setStroke(originalStroke);
    }

    /**
     * 画面の拡大縮小を行うメソッド
     *
     * @param zoomIn trueなら拡大、falseなら縮小
     */
    public void scaling(boolean zoomIn) {
        // 拡大または縮小
        double scaleChange = zoomIn ? 0.1 : -0.1; // 10%ずつ変更
        double newScale = scale + scaleChange;

        // スケール制限内かチェック
        if (newScale >= MIN_SCALE && newScale <= MAX_SCALE) {
            scale = newScale;
            repaint(); // 新しいスケールで再描画
        }
    }

    /**
     * 現在のスケール係数を取得
     *
     * @return 現在のスケール係数
     */
    public double getScale() {
        return scale;
    }

    /**
     * スケール係数を直接設定
     *
     * @param newScale 設定したいスケール係数（0.5〜2.0）
     */
    public void setScale(double newScale) {
        if (newScale >= MIN_SCALE && newScale <= MAX_SCALE) {
            scale = newScale;
            repaint(); // 新しいスケールで再描画
        }
    }

    /**
     * スケールのパーセント表示を取得（表示用）
     *
     * @return スケールのパーセント表示（例: "100%"）
     */
    public String getScalePercent() {
        return (int) (scale * 100) + "%";
    }

}
