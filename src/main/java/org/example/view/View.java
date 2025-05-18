package org.example.view;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Map;
import javax.swing.*;
import org.example.model.Model;

public class View extends JPanel {

    private Model model; // Modelへの参照

    public JPanel menuDisplay; // MenuDisplay を小文字始まりの menuDisplay に変更
    public Map<String, JButton> subButton;
    public JTextField speedDisplay;
    public Map<String, JButton> penSizeDisplay;
    public JColorChooser colorPalletDisplay;

    public View(Model model) { // コンストラクタ
        this.model = model;
        initializeComponents();
    }

    private void initializeComponents() {
        // MenuDisplay の初期化
        menuDisplay = new JPanel();
        menuDisplay.setLayout(new FlowLayout(FlowLayout.LEFT)); // FlowLayout を使用

        // subButton の初期化
        subButton = new java.util.HashMap<>(); // 実装クラスを指定
        // ボタンの作成と menuDisplay への追加 (一旦)
        JButton buttonA = new JButton("機能A");
        subButton.put("functionA", buttonA);
        menuDisplay.add(buttonA);

        // speedDisplay の初期化
        speedDisplay = new JTextField(10);
        speedDisplay.setEditable(false);
        menuDisplay.add(new JLabel("速度:"));
        menuDisplay.add(speedDisplay);

        // penSizeDisplay の初期化
        penSizeDisplay = new java.util.HashMap<>(); // 実装クラスを指定
        // ペンサイズボタンの作成と menuDisplay への追加 (一旦)
        JButton sizeSmall = new JButton("細");
        penSizeDisplay.put("small", sizeSmall);
        menuDisplay.add(new JLabel("ペンサイズ:"));
        menuDisplay.add(sizeSmall);

        // colorPalletDisplay の初期化
        colorPalletDisplay = new JColorChooser();
        menuDisplay.add(new JLabel("色選択:"));
        menuDisplay.add(colorPalletDisplay);

        // View 自体のレイアウトを設定し、menuDisplay を追加
        setLayout(new BorderLayout());
        add(menuDisplay, BorderLayout.NORTH);
        // 描画領域は中央に追加?

        // イベントリスナーの設定は Controller
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // Model のデータに基づいて描画処理を行う
        if (model != null) {
            Point2D.Double pinionPosition = model.getPinionPosition(); // Model クラスにこのメソッドが必要
            if (pinionPosition != null) {
                displayPinion(g2d, pinionPosition);
            }
            Point2D.Double mousePosition = model.getMousePosition(); // Model クラスにマウス位置を保持するフィールドとゲッターが必要
            Color mouseColor = model.getMouseColor(); // Model クラスにマウスの色を保持するフィールドとゲッターが必要
            if (mousePosition != null && mouseColor != null) {
                displayMousePointer(g2d, mousePosition, mouseColor);
            }
            // 他の描画処理
            displaySpur(g2d, new Point2D.Double(100, 100)); // 一旦固定位置に描画
            displayDrawPen(g2d, new Point2D.Double(150, 150), Color.BLACK); // 一旦固定位置と色で描画
        }
    }

    public void displayPinion(Graphics2D g, Point2D.Double position) {
        // ピニオンを描画する処理例
        int radius = 20;
        g.setColor(Color.BLUE);
        g.fillOval((int) position.x - radius, (int) position.y - radius, 2 * radius, 2 * radius);
    }

    public void displayMousePointer(Graphics2D g, Point2D.Double position, Color color) {
        // マウスカーソルを描画する処理例
        int size = 10;
        g.setColor(color);
        g.fillRect((int) position.x - size / 2, (int) position.y - size / 2, size, size);
    }

    public void displaySpur(Graphics2D g, Point2D.Double position) {
        // スパーを描画する処理例
        int radius = 30;
        g.setColor(Color.GREEN);
        g.drawOval((int) position.x - radius, (int) position.y - radius, 2 * radius, 2 * radius);
    }

    public void displayDrawPen(Graphics2D g, Point2D.Double position, Color color) {
        // 描画ペンを描画する処理例
        g.setColor(color);
        g.fillOval((int) position.x - 2, (int) position.y - 2, 4, 4);
    }

    public void scaling() {
        //スケーリング処理
        repaint();
    }

    // Model のデータを更新して View を再描画するメソッド (仮)
    public void updateView() {
        if (model != null) {
            speedDisplay.setText(String.valueOf(model.getSpeed())); // Model クラスに getSpeed() が必要
            repaint();
        }
    }

    // ユーザーが選択した色を取得するメソッド (Controller で使用)
    public Color getSelectedColor() {
        return colorPalletDisplay.getColor();
    }

    // ユーザーが選択したペンのサイズを取得するメソッド (Controller で使用)
    public int getSelectedPenSize() {
        // penSizeDisplay から選択されているサイズを取得するロジックを実装
        return 1; // デフォルト値
    }

    // ボタンなどのアクションリスナーを設定するメソッド (Controller で使用)
    public void setActionListener(String buttonName, java.awt.event.ActionListener listener) {
        if (subButton.containsKey(buttonName)) {
            subButton.get(buttonName).addActionListener(listener);
        } else if (penSizeDisplay.containsKey(buttonName)) {
            penSizeDisplay.get(buttonName).addActionListener(listener);
        }
        // 他のコンポーネントにも同様に設定
    }
}
