/*
 * View.java - スピログラフ描画アプリケーションのViewコンポーネント
 *
 * このクラスは、MVC（Model-View-Controller）モデルにおけるViewの役割を担う。
 *
 * **変更点履歴:**
 * - `displayDrawPen`および`displaySpirographPath`（現`displaySpirographLocus`）メソッド内の
 * 重複する変数宣言とメソッド呼び出しを修正し、`model.getPenSize()`を使用するよう変更した。
 * - UIコンポーネント（ボタン）に「Save」と「Load」ボタンを追加した。
 * - スピログラフの軌跡描画メソッド`displaySpirographPath`を`displaySpirographLocus`に名称変更し、
 * Modelから軌跡の全データ（`List<Point2D.Double> locus`）を取得して描画するようロジックを修正した。
 * また、Modelがこのデータを提供するよう変更が必要である旨の重要なコメントを追加した。
 * - ユーザー画面移動（パン）機能のためのマウスリスナーをView内部から削除した。
 * これはControllerがマウスイベントを一元的に処理するためである。
 * - スパーギアの半径をピッキングしてドラッグで決定できるようにするための機能を追加した。
 * - `isDefiningSpurGear`, `spurGearCenterScreen`, `currentDragPointScreen`変数を追加した。
 * - `setDefiningSpurGear`, `setSpurGearCenterScreen`, `setCurrentDragPointScreen`, `clearSpurGearDefinition`
 * といったControllerがViewの状態を操作するための公開メソッドを追加した。
 * - `screenToWorld`ヘルパーメソッドを追加し、画面座標をワールド座標に変換できるようにした。
 * - `paintComponent`メソッド内で、スパーギア定義中に一時的な円を描画するロジックを追加した。
 * - ピニオンギアの半径をドラッグで決定するモードを削除した。
 * - ピニオンギアをドラッグすることで位置を移動させる機能を追加した。（スパーギア半径決定モードより優先）
 * - `isDraggingPinionGear`, `pinionGearDragOffsetWorld`変数を追加した。
 * - `setDraggingPinionGear`, `setPinionGearDragOffsetWorld`といったControllerがViewの状態を操作するための公開メソッドを追加した。
 * - `paintComponent`メソッド内で、ピニオンギア移動モードが有効な場合、ピニオンギアとペンに一時的なオフセットを適用するロジックを追加した。
 * - スパーギアの中心をピッキングしてドラッグすることで、スピログラフ全体を並行移動する機能を追加した。（ピニオンギア移動モードより優先度が低い）
 * - `isDraggingSpiroGraph`, `spiroGraphDragOffsetWorld`変数を追加した。
 * - `setDraggingSpiroGraph`, `setSpiroGraphDragOffsetWorld`といったControllerがViewの状態を操作するための公開メソッドを追加した。
 * - `paintComponent`メソッド内で、スピログラフ移動モードが有効な場合、すべての描画要素（ギア、ペン、軌跡）に`spiroGraphDragOffsetWorld`を適用するロジックを追加した。
 * - `displaySpirographLocus`メソッドが一時的なオフセットを受け取るように変更し、軌跡の各点に適用するよう修正した。
 * - コード全体で「Path」という用語を「Locus」に変更した。
 * - `paintComponent`メソッド内で`penColor`の宣言スコープを広げ、メソッド全体でアクセス可能にした。
 * - ペンの位置をピッキングしてドラッグすることで定義できるようにする機能を追加した。（最優先）
 * - `isDefiningPen`, `penDefinitionStartScreenPoint`, `currentPenDefinitionDragScreenPoint`変数を追加した。
 * - `setDefiningPen`, `setPenDefinitionStartScreenPoint`, `setCurrentPenDefinitionDragScreenPoint`, `clearPenDefinition`といったControllerがViewの状態を操作するための公開メソッドを追加した。
 * - `paintComponent`メソッド内で、ペンの位置定義中に仮のペンとピニオンギア中心からの接続線を描画するロジックを追加した。この描画は、通常のペン描画よりも優先される。
 *
 * **他クラスの必要な変更点:**
 * - **Model.java**:
 * - 軌跡（Locus）データを保持する`List<Point2D.Double> locus`フィールドを追加する必要がある。
 * - `updateData()`メソッド内で、現在のペン位置を`locus`リストに追加する処理が必要である。
 * - `public List<Point2D.Double> getLocus()`メソッドを追加する必要がある。
 * - `Pen`クラスの`getPenSize()`、`getColor()`メソッドが存在することを前提としている。
 * - Penのピニオンギアに対する相対的な位置（距離と角度）を保持するフィールド（例: `penDistanceFromPinionCenter`, `penAngleOnPinion`）をPenクラスに追加する必要がある。
 * - これらの新しいペン位置パラメータを設定・取得するメソッド（例: `Pen.setRelativePosition(double distance, double angle)`や個別のsetter/getter）をPenクラスに追加する必要がある。
 * - Modelは、Controllerから受け取った相対位置に基づいてPenのこれらのパラメータを更新し、Penの`move`メソッドがこれらの新しい相対パラメータを考慮して絶対位置を計算するよう調整する必要がある。
 * - **Controller.java**:
 * - `mouseWheelMoved`メソッド内の`view.Point(scroll);`を`view.scaling(amount > 0);`のように修正し、マウスホイールの方向に応じたスケール変更を行う必要がある。
 * - Controllerのコンストラクタは、`Model`も引数として受け取るように変更し、`Model`のデータ操作を可能にする必要がある。
 * - マウスイベント処理のロジックを実装する必要がある。
 * - `mouseDragged`メソッドは現在コメントアウトされているため、実装を有効にする必要がある。
 * - モード（ペンの位置定義、ピニオンギア移動、スピログラフ全体移動、スパーギア定義、パン）を適切に管理するための`enum`や状態変数を導入する必要がある。
 * - `mousePressed`時、クリックされた場所（例: ペン、ピニオンギア、スパーギアの近く）やアクティブなモードに基づいて、現在アクティブなモード（`currentMode`）を設定し、`View`の対応する`setDefining/setDragging`メソッドや開始点を設定するメソッド（例: `view.setPenDefinitionStartScreenPoint(e_press.getPoint())`）を呼び出す必要がある。
 * - `mouseDragged`時、`currentMode`に応じて、`View`の一時的なオフセット設定メソッド（例: `view.setSpiroGraphDragOffsetWorld()`, `view.setPinionGearDragOffsetWorld()`, `view.setCurrentDragPointScreen()`, `view.setCurrentPenDefinitionDragScreenPoint()`) を呼び出し、`View`を`repaint`させる必要がある。
 * - `mouseReleased`時、`currentMode`に応じて、`View`の定義/ドラッグモードをクリアするメソッドを呼び出し、**Modelの実際のデータ（ギアの位置や半径、ペンの相対位置など）を更新するロジックを実装する必要がある。**
 * - 優先順位（ペン位置定義 ＞ ピニオンギア移動 ＞ スピログラフ移動 ＞ スパーギア半径定義 ＞ パン）を考慮したマウスイベントのハンドリングロジックを実装する必要がある。
 * - `view.toWorldCoordinates(pressPoint)`のような呼び出しを`view.screenToWorld(pressPoint)`に修正する必要がある。
 * - `model.Point(clickedpoint);`のようなModelへの直接的な`Point`オブジェクトの受け渡しは、Modelの具体的なセッターメソッド（例: `model.setSpurGearPosition()`, `model.setPinionGearPosition()`, `model.getPen().setRelativePosition()`など）に置き換える必要がある。
 */

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
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.example.model.Model;
import java.io.File;
import java.util.List;

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

    // ユーザー画面移動（パン）用の変数 (Controllerによって管理されるため、Viewからは直接操作しない)
    private Point2D.Double viewOffset = new Point2D.Double(0, 0); // 描画内容のオフセット（ワールド座標系）

    // スパーギアの半径定義用の変数
    private boolean isDefiningSpurGear = false; // スパーギアの半径定義中か
    private Point spurGearCenterScreen = null; // スパーギアの中心点（画面座標）
    private Point currentDragPointScreen = null; // 現在のドラッグ点（画面座標）

    // ピニオンギア移動用の変数
    private boolean isDraggingPinionGear = false; // ピニオンギアを移動中か
    private Point2D.Double pinionGearDragOffsetWorld = new Point2D.Double(0, 0); // ピニオンギア移動中の一時的なワールド座標オフセット

    // スピログラフ全体移動用の変数（スパーギアのピッキング＆ドラッグ）
    private boolean isDraggingSpiroGraph = false; // スピログラフ全体を移動中か
    private Point2D.Double spiroGraphDragOffsetWorld = new Point2D.Double(0, 0); // スピログラフ移動中の一時的なワールド座標オフセット

    // ペンの位置定義用の変数
    private boolean isDefiningPen = false; // ペンの位置定義中か
    private Point penDefinitionStartScreenPoint = null; // ペン定義ドラッグの開始点（画面座標）
    private Point currentPenDefinitionDragScreenPoint = null; // 現在のドラッグ点（画面座標）


    public View(Model model) {
        this.model = model;

        // レイアウトマネージャーは要件によって適切なものを選択する（例: BorderLayout, GridLayoutなど）。
        // 現在はnullレイアウトを使用しているが、これはUIの柔軟性を低下させる。
        this.setLayout(null);
        this.setBackground(Color.WHITE);

        // UIコンポーネントの初期化
        this.MenuDisplay = new JPanel();
        // MenuDisplayも適切なレイアウトマネージャーを使用することを推奨する。
        MenuDisplay.setLayout(null);

        // ボタンのマップを初期化
        this.subButton = new HashMap<>();
        this.penSizeDisplay = new HashMap<>();

        // subButtonマップにボタンを作成し追加
        String[] buttonNames = { "Pen", "SpurGear", "PinionGear", "Start", "Stop", "Clear", "Save", "Load" };
        for (String name : buttonNames) {
            JButton button = new JButton(name);
            subButton.put(name, button);
            MenuDisplay.add(button);
            // アクションリスナーはControllerで追加する必要がある。
        }

        // penSizeDisplayマップにボタンを作成し追加
        String[] penSizes = { "Small", "Medium", "Large" };
        for (String size : penSizes) {
            JButton button = new JButton(size);
            penSizeDisplay.put(size, button);
            MenuDisplay.add(button);
        }

        // スピード表示用テキストフィールドを初期化
        this.speedDisplay = new JTextField("0.0");
        speedDisplay.setEditable(true);
        MenuDisplay.add(speedDisplay);

        // カラーチューザーを初期化し、メニューパネルに追加
        this.colorPalletDisplay = new JColorChooser();
        MenuDisplay.add(colorPalletDisplay);

        // メインパネルにMenuDisplayを追加
        this.add(MenuDisplay);

        // コンポーネントのサイズと位置を設定（仮の値、レイアウトマネージャーの使用を強く推奨）
        MenuDisplay.setBounds(10, 10, 200, 600);
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
        colorPalletDisplay.setBounds(10, yOffset, 180, 250);

        this.setVisible(true);

        // View内部のマウスイベントリスナーは削除された。
        // ControllerがViewにMouseListenerとMouseMotionListenerを追加し、
        // すべてのマウスイベントを処理する。
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

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (model == null) {
            return;
        }

        AffineTransform originalTransform = g2d.getTransform();

        // penColor をここで取得し、メソッド全体でアクセス可能にする
        Color penColor = model.getPenColor();

        // ユーザー画面移動（パン）の適用
        // viewOffsetはワールド座標系でのオフセット
        g2d.translate(viewOffset.x, viewOffset.y);

        // スケーリングの適用
        g2d.scale(scale, scale);

        // Modelから現在の要素のベース位置を取得
        Point2D.Double baseSpurPosition = model.getSpurGearPosition();
        Point2D.Double basePinionPosition = model.getPinionGearPosition();
        Point2D.Double basePenPosition = model.getPenPosition();

        // 描画に適用する一時的なオフセット（初期値は0）
        Point2D.Double effectiveSpurOffset = new Point2D.Double(0, 0);
        Point2D.Double effectivePinionOffset = new Point2D.Double(0, 0);
        Point2D.Double effectivePenOffset = new Point2D.Double(0, 0);
        Point2D.Double effectiveLocusOffset = new Point2D.Double(0, 0);

        // --- 優先順位に応じたオフセットの計算 ---
        // 優先度1: ペンの位置定義モード (最優先)
        // ペンの位置定義中は、他の移動オフセットは適用されない（Controllerがモードを排他的に管理するため）。
        // ただし、もしペンの定義がピニオンギアに「対して」行われる場合、ピニオンギアの現在の位置は考慮する必要がある。
        // このViewの描画ロジックは、あくまでModelのベース位置と、一時的なオフセットを組み合わせて表示する。
        // ペンの定義中は、Modelのベース位置ではなく、ドラッグ中の仮のペン位置が表示される。

        // 優先度2: ピニオンギア個別移動モード
        // ペンの位置定義モードが有効でない場合に限り、ピニオンギアのオフセットを適用する。
        if (isDraggingPinionGear && !isDefiningPen) {
            effectivePinionOffset = pinionGearDragOffsetWorld;
            effectivePenOffset = pinionGearDragOffsetWorld; // ペンはピニオンギアに追従
        }

        // 優先度3: スピログラフ全体移動モード
        // ピニオンギア個別移動モード、ペンの位置定義モードが有効でない場合に限り、スピログラフ全体移動のオフセットを適用する。
        if (isDraggingSpiroGraph && !isDraggingPinionGear && !isDefiningPen) {
            effectiveSpurOffset = spiroGraphDragOffsetWorld;
            effectivePinionOffset = spiroGraphDragOffsetWorld;
            effectivePenOffset = spiroGraphDragOffsetWorld;
            effectiveLocusOffset = spiroGraphDragOffsetWorld;
        }

        // 描画用の最終的な位置を計算 (ベース位置 + 一時的なオフセット)
        Point2D.Double finalSpurPosition = (baseSpurPosition != null) ?
                new Point2D.Double(baseSpurPosition.x + effectiveSpurOffset.x, baseSpurPosition.y + effectiveSpurOffset.y) : null;
        Point2D.Double finalPinionPosition = (basePinionPosition != null) ?
                new Point2D.Double(basePinionPosition.x + effectivePinionOffset.x, basePinionPosition.y + effectivePinionOffset.y) : null;
        Point2D.Double finalPenPosition = (basePenPosition != null) ?
                new Point2D.Double(basePenPosition.x + effectivePenOffset.x, basePenPosition.y + effectivePenOffset.y) : null;


        // スパーギアの描画
        if (finalSpurPosition != null) {
            displaySpur(g2d, finalSpurPosition);
        }

        // ピニオンギアの描画
        if (finalPinionPosition != null) {
            displayPinion(g2d, finalPinionPosition);
        }

        // スピログラフの軌跡を描画（オフセットを渡す）
        displaySpirographLocus(g2d, effectiveLocusOffset);

        // ペンの位置定義中の仮描画（最優先）
        if (isDefiningPen && penDefinitionStartScreenPoint != null && currentPenDefinitionDragScreenPoint != null) {
            Point2D.Double tempPenWorldPosition = screenToWorld(currentPenDefinitionDragScreenPoint);

            // ピニオンギアの中心（現在のドラッグオフセットが適用されたもの）から仮のペン位置まで線を描画
            // finalPinionPosition は既にスピログラフ全体のドラッグオフセットやピニオンギア個別オフセットが適用されているため、それを利用
            if (finalPinionPosition != null) {
                g2d.setColor(Color.LIGHT_GRAY); // 仮の線色
                g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, new float[]{5.0f, 5.0f}, 0.0f)); // 破線
                g2d.drawLine((int) finalPinionPosition.x, (int) finalPinionPosition.y,
                             (int) tempPenWorldPosition.x, (int) tempPenWorldPosition.y);
            }

            // 仮のペンを描画
            g2d.setColor(Color.MAGENTA); // 仮のペン色
            double penSize = model.getPenSize();
            g2d.fillOval((int) (tempPenWorldPosition.x - penSize / 2), (int) (tempPenWorldPosition.y - penSize / 2), (int) penSize, (int) penSize);

        } else {
            // 通常のペンの描画 (現在のペン先の位置)
            if (finalPenPosition != null) {
                displayDrawPen(g2d, finalPenPosition, penColor);
            }
        }

        // スパーギア定義中の仮描画 (ペンの位置定義モード、ピニオンギア移動モード、スピログラフ全体移動モードより優先度が低い)
        // isDefiningPen が true の場合は描画しない
        if (isDefiningSpurGear && spurGearCenterScreen != null && !isDefiningPen) {
            Point2D.Double centerWorld = screenToWorld(spurGearCenterScreen);
            double tempRadius = 0;
            if (currentDragPointScreen != null) {
                // 画面座標での距離を計算し、ワールド座標での半径に変換
                tempRadius = spurGearCenterScreen.distance(currentDragPointScreen) / scale;
            }

            g2d.setColor(Color.ORANGE); // 仮の描画色
            g2d.setStroke(new BasicStroke(1.5f));
            // 円の左上座標を計算（中心座標から半径分を引く）
            double x = centerWorld.x - tempRadius;
            double y = centerWorld.y - tempRadius;
            g2d.drawOval((int) x, (int) y, (int) (tempRadius * 2), (int) (tempRadius * 2));
        }

        g2d.setTransform(originalTransform);

        // 現在のスケールを表示 (スケールが元に戻された後に描画)
        g2d.setColor(Color.BLACK);
        g2d.drawString("Scale: " + getScalePercent(), 10, getHeight() - 10);
    }

    /**
     * ピニオンギアを描画するメソッド
     *
     * @param g Graphics2Dオブジェクト
     * @param position ピニオンギアの中心位置 (Modelからの生座標)
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

    public void displayMousePointer(Graphics2D g, Point2D.Double position, Color color) {
        // 未実装
    }

    /**
     * スパーギアを描画するメソッド
     *
     * @param g 描画コンテキスト
     * @param position スパーギアの中心位置 (Modelからの生座標)
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
     * ペンポイントを描画するメソッド
     *
     * @param g 描画コンテキスト
     * @param position ペンポイントの位置 (Modelからの生座標)
     * @param color ペンの色
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
     * スピログラフの軌跡（Locus）を描画するメソッド
     *
     * @param g2d 描画コンテキスト
     * @param temporaryOffset スピログラフ移動中の一時的なワールド座標オフセット
     */
    private void displaySpirographLocus(Graphics2D g2d, Point2D.Double temporaryOffset) {
        Color originalColor = g2d.getColor();
        java.awt.Stroke originalStroke = g2d.getStroke();

        g2d.setColor(model.getPenColor());
        g2d.setStroke(new BasicStroke((float) model.getPenSize()));

        // Modelから軌跡の全データを取得する。
        // 重要: Penクラスのmoveメソッドで計算されたペン位置をModelが適切に収集し、
        // その履歴を List<Point2D.Double> として保持し、
        // public List<Point2D.Double> getLocus() メソッドで提供するよう
        // Modelクラスを変更する必要がある。
        // Modelが現在の実装のままだと、この描画は正しく機能しない。
        List<Point2D.Double> locus = model.getLocus();

        if (locus != null && locus.size() > 1) {
            // 軌跡の点を線で結んで描画。一時的なオフセットを各点に適用する。
            for (int i = 0; i < locus.size() - 1; i++) {
                Point2D.Double p1 = locus.get(i);
                Point2D.Double p2 = locus.get(i + 1);
                g2d.drawLine((int) (p1.x + temporaryOffset.x), (int) (p1.y + temporaryOffset.y),
                             (int) (p2.x + temporaryOffset.x), (int) (p2.y + temporaryOffset.y));
            }
        }

        g2d.setColor(originalColor);
        g2d.setStroke(originalStroke);
    }

    /**
     * 画面の拡大縮小を行うメソッド
     *
     * @param zoomIn trueなら拡大、falseなら縮小
     */
    public void scaling(boolean zoomIn) {
        double scaleChange = zoomIn ? 0.1 : -0.1;
        double newScale = scale + scaleChange;

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

    /**
     * ファイル保存場所を選択する
     *
     * @return 選択されたファイルパス（Fileオブジェクト）
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
     * ファイル読み込み場所を選択する
     *
     * @return 選択されたファイルパス（Fileオブジェクト）
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
     * 画面座標をワールド座標に変換する。
     *
     * @param screenPoint 画面座標
     * @return ワールド座標
     */
    public Point2D.Double screenToWorld(Point screenPoint) {
        // オフセットとスケールを考慮して変換
        // 描画オフセットを逆変換し、スケールで割る
        double worldX = (screenPoint.getX() / scale) - (viewOffset.x / scale);
        double worldY = (screenPoint.getY() / scale) - (viewOffset.y / scale);
        return new Point2D.Double(worldX, worldY);
    }

    /**
     * スパーギアの半径定義モードを設定する。
     *
     * @param defining trueの場合、定義モードを有効にする。
     */
    public void setDefiningSpurGear(boolean defining) {
        this.isDefiningSpurGear = defining;
        if (!defining) {
            clearSpurGearDefinition(); // 定義モード終了時に状態をクリア
        }
        repaint(); // 描画を更新
    }

    /**
     * スパーギアの中心点（画面座標）を設定する。
     *
     * @param p 中心点（画面座標）
     */
    public void setSpurGearCenterScreen(Point p) {
        this.spurGearCenterScreen = p;
        this.currentDragPointScreen = p; // ドラッグ開始時は中心点と同じ
        repaint();
    }

    /**
     * 現在のドラッグ点（画面座標）を設定する。
     *
     * @param p 現在のドラッグ点（画面座標）
     */
    public void setCurrentDragPointScreen(Point p) {
        this.currentDragPointScreen = p;
        repaint();
    }

    /**
     * スパーギア定義に関する状態をクリアする。
     */
    public void clearSpurGearDefinition() {
        this.isDefiningSpurGear = false;
        this.spurGearCenterScreen = null;
        this.currentDragPointScreen = null;
        repaint();
    }

    /**
     * ピニオンギア移動モードを設定する。
     *
     * @param dragging trueの場合、移動モードを有効にする。
     */
    public void setDraggingPinionGear(boolean dragging) {
        this.isDraggingPinionGear = dragging;
        if (!dragging) {
            this.pinionGearDragOffsetWorld.setLocation(0, 0); // 移動終了時にオフセットをリセット
        }
        repaint();
    }

    /**
     * ピニオンギア移動中の一時的なワールド座標オフセットを設定する。
     *
     * @param offset ワールド座標でのオフセット
     */
    public void setPinionGearDragOffsetWorld(Point2D.Double offset) {
        this.pinionGearDragOffsetWorld = offset;
        repaint();
    }

    /**
     * スピログラフ全体移動モードを設定する。
     *
     * @param dragging trueの場合、移動モードを有効にする。
     */
    public void setDraggingSpiroGraph(boolean dragging) {
        this.isDraggingSpiroGraph = dragging;
        if (!dragging) {
            this.spiroGraphDragOffsetWorld.setLocation(0, 0); // 移動終了時にオフセットをリセット
        }
        repaint(); // 描画を更新
    }

    /**
     * スピログラフ移動中の一時的なワールド座標オフセットを設定する。
     *
     * @param offset ワールド座標でのオフセット
     */
    public void setSpiroGraphDragOffsetWorld(Point2D.Double offset) {
        this.spiroGraphDragOffsetWorld = offset;
        repaint(); // 描画を更新
    }

    /**
     * ペンの位置定義モードを設定する。
     *
     * @param defining trueの場合、定義モードを有効にする。
     */
    public void setDefiningPen(boolean defining) {
        this.isDefiningPen = defining;
        if (!defining) {
            clearPenDefinition(); // 定義モード終了時に状態をクリア
        }
        repaint(); // 描画を更新
    }

    /**
     * ペン定義ドラッグの開始点（画面座標）を設定する。
     *
     * @param p 開始点（画面座標）
     */
    public void setPenDefinitionStartScreenPoint(Point p) {
        this.penDefinitionStartScreenPoint = p;
        this.currentPenDefinitionDragScreenPoint = p; // ドラッグ開始時は開始点と同じ
        repaint();
    }

    /**
     * ペン定義中の現在のドラッグ点（画面座標）を設定する。
     *
     * @param p 現在のドラッグ点（画面座標）
     */
    public void setCurrentPenDefinitionDragScreenPoint(Point p) {
        this.currentPenDefinitionDragScreenPoint = p;
        repaint();
    }

    /**
     * ペンの位置定義に関する状態をクリアする。
     */
    public void clearPenDefinition() {
        this.isDefiningPen = false;
        this.penDefinitionStartScreenPoint = null;
        this.currentPenDefinitionDragScreenPoint = null;
        repaint();
    }


    /**
     * 現在のViewの描画オフセットを取得する。
     * Controllerがパンニング計算に利用する。
     *
     * @return 現在の描画オフセット
     */
    public Point2D.Double getViewOffset() {
        return viewOffset;
    }

    /**
     * Viewの描画オフセットを設定する。
     * Controllerがパンニング計算後にViewを更新する。
     *
     * @param offset 新しい描画オフセット
     */
    public void setViewOffset(Point2D.Double offset) {
        this.viewOffset = offset;
        repaint();
    }
}
