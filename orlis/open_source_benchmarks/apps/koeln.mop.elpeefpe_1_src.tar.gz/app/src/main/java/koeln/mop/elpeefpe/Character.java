package koeln.mop.elpeefpe;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Andreas Streichardt on 18.06.2016.
 */

public class Character {
    public int id;
    public String name;
    public Value elpe;
    public Value efpe;

    public void setValues(int elpe, int efpe) {
        this.elpe = new Value();
        this.elpe.multiplier = 5;
        this.elpe.value = elpe;

        this.efpe = new Value();
        this.efpe.value = efpe;
    }
}
