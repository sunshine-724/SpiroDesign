package org.example.model;



public enum Command {
    NONE, // コマンドなし
    MOVE_SMALL_CIRCLE, // 内側の円を移動
    MOVE_BIG_CIRCLE, // 外側の円を移動
    CHANGE_SMALL_CIRCLE_RADIUS, // 小さい円の半径を変更
    CHANGE_BIG_CIRCLE_RADIUS, // 大きい円の半径を変更
    // SET_CENTER, // 中心座標を設定
    // SET_RADIUS, // 半径を設定
    // SET_CENTER_POINT_RADIUS, // 中心点の半径を設定
    // SET_RED_POINT, // 赤い点の位置を設定
    // SET_BIG_CIRCLE // 大きな円の位置と半径を設定
}

// この列挙型は、DrawModelの操作をコマンドとして定義しています。
// 各コマンドは、モデルの状態を変更するために使用されます。
// 例えば、SET_CENTERは中心座標を設定するためのコマンドです。
// このようにコマンドを定義することで、モデルの操作を明確にし、拡張性を持たせることができます。
// 将来的にコマンドパターンを適用する際にも、この列挙型を利用してコマンドの実行や取り消しを管理できます。

interface ICommand {
    void execute(Model model); // コマンドを実行するメソッド
    void undo(Model model); // コマンドを取り消すメソッド
}