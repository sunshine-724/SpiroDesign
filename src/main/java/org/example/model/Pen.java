import java.awt.geom.Point2D;

public class Pen{
    Point2D.Double position;
    Color color;

    Pen(Point2D.Double position,Color color){
        this.position = position;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public Point2D.Double getPosition(){
        return position;
    }
}