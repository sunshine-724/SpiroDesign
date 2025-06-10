package org.example;

import javax.swing.JFrame;
import org.example.model.Model;
import org.example.controller.Controller;
import org.example.view.View;
import java.awt.BorderLayout; // BorderLayout を使用する場合にインポート

public class Main {
    public static void main(String[] args) {
        // Swing GUI の操作は、イベントディスパッチスレッド (EDT) で行うことが推奨される。
        // これにより、スレッドセーフティが確保され、GUI の応答性が向上する。
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    /**
     * GUI を作成し、表示する。このメソッドは EDT で実行される。
     */
    private static void createAndShowGUI() {
        // 1. JFrame (アプリケーションのメインウィンドウ) を作成し、基本設定を行う。
        JFrame frame = new JFrame("Spirograph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // ウィンドウを閉じたらアプリケーションを終了
        frame.setSize(800, 800); // ウィンドウの初期サイズを設定

        // 2. Model, View, Controller の各コンポーネントをインスタンス化する。
        // Model を最初に作成。
        Model model = new Model();

        // View を作成し、Model を View に渡して View が Model のデータを参照できるようにする。
        View view = new View(model);

        // Model に View を登録する。
        // これにより、Model は View に対して変更を通知できるようになる (オブザーバーパターン)。
        // 特に loadData 後の notifyViewsLoading (現在は setLocusData) の呼び出しのために必要。
        model.addView(view);

        // Controller を作成し、View と Model を Controller に渡して制御できるようにする。
        // Controller は View のマウスイベントリスナーとして自身を登録するため、View の後にインスタンス化する。
        Controller controller = new Controller(view, model);

        // 3. View コンポーネントを JFrame に追加する。
        // View (JPanel を継承) を JFrame の中央領域に追加することで、ウィンドウいっぱいに View が表示される。
        frame.add(view, BorderLayout.CENTER);

        // 4. 全てのコンポーネントが配置され、フレームが完全に準備できてから可視化する。
        // これにより、表示が途切れたり、コンポーネントが正しくレンダリングされない問題を避けることができる。
        frame.setVisible(true);

        // 必要であれば、ここで Model のアニメーションを開始する。
        // 例えば、ユーザーが "Start" ボタンをクリックするまで開始しない、という設計であれば不要。
        // model.start();
    }
}
