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
        }

        // Create and add buttons to penSizeDisplay map
        String[] penSizes = { "Small", "Medium", "Large" };
        for (String size : penSizes) {
            JButton button = new JButton(size);
            penSizeDisplay.put(size, button);
            MenuDisplay.add(button);
            // Add button locations and sizes as needed
        }

        // Initialize text field for speed display
        this.speedDisplay = new JTextField("0.0");
        speedDisplay.setEditable(true);
        MenuDisplay.add(speedDisplay);

        // Initialize color chooser
        this.colorPalletDisplay = new JColorChooser();

        // Add MenuDisplay to the main panel
        this.add(MenuDisplay);

        // Set sizes and positions
        MenuDisplay.setBounds(10, 10, 200, 600); // Adjust as needed

        // Make the panel visible
        this.setVisible(true);
    }

    /**
     * ピニオンギアを描画するメソッド
     *
     * @param g        Graphics2Dオブジェクト
     * @param position ピニオンギアの中心位置
     */
    public void displayPinion(Graphics2D g, Point2D.Double position) {
        // 元のグラフィックス設定を保存
        Color originalColor = g.getColor();
        java.awt.Stroke originalStroke = g.getStroke();

        // ピニオンギアの描画設定
        g.setColor(Color.BLUE); // 青色に設定
        g.setStroke(new BasicStroke(2)); // 線の太さを2ピクセルに設定

        // アンチエイリアスを有効にして滑らかな円を描画
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // モデルからピニオンギアの半径を取得
        double radius = model.getPinionGearRadius();

        // 円の描画（中心座標から半径を引いて左上の座標を計算）
        g.drawOval(
                (int) (position.x - radius),
                (int) (position.y - radius),
                (int) (radius * 2),
                (int) (radius * 2));

        // グラフィックス設定を元に戻す
        g.setColor(originalColor);
        g.setStroke(originalStroke);
    }

    public void displayMousePointer(Graphics2D g, Point2D.Double position, Color color) {

    }

    /**
     * スパーギアを描画するメソッド
     *
     * @param g        描画コンテキスト
     * @param position スパーギアの中心位置
     */
    public void displaySpur(Graphics2D g, Point2D.Double position) {
        // 元の設定を保存
        Color originalColor = g.getColor();
        java.awt.Stroke originalStroke = g.getStroke();

        // 描画設定
        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(2.0f));

        // アンチエイリアス設定
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // モデルからスパーギアの半径を取得
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
     * @param position ペンポイントの位置
     * @param color    ペンの色（引数として渡されるが、デフォルトは緑色）
     */
    public void displayDrawPen(Graphics2D g, Point2D.Double position, Color color) {
        // 元の設定を保存
        Color originalColor = g.getColor();
        java.awt.Stroke originalStroke = g.getStroke();

        // 描画設定
        // 引数colorが指定されていなければ緑色を使用
        g.setColor(color != null ? color : Color.GREEN);
        g.setStroke(new BasicStroke(2.0f));

        // アンチエイリアス設定
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ペンポイントのサイズ
        int penSize = 2;

        // 点（小さな円）を描画
        g.fillOval((int) (position.x - penSize / 2), (int) (position.y - penSize / 2), penSize, penSize);

        // 元の設定に戻す
        g.setColor(originalColor);
        g.setStroke(originalStroke);
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
