package org.example.spiroIO;

import java.io.Serializable;
import java.util.List;

public class SpiroData implements Serializable {
    private static final long serialVersionUID = 1L;

    public SpurGear spurGear;
    public PinionGear pinionGear;
    public Pen pen;
    public List<Point> locus;
    public long time;

    public SpiroData(SpurGear spurGear, PinionGear pinionGear, Pen pen, List<Point> locus, long time) {
        this.spurGear = spurGear;
        this.pinionGear = pinionGear;
        this.pen = pen;
        this.locus = locus;
        this.time = time;
    }
}
