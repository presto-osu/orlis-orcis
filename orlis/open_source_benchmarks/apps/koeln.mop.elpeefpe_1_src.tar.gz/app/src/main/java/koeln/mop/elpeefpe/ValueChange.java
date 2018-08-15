package koeln.mop.elpeefpe;

/**
 * Created by Andreas Streichardt on 20.06.2016.
 */
public class ValueChange {
    public int change;
    public DamageType type;

    public ValueChange(DamageType type, int change) {
        this.change = change;
        this.type = type;
    }
}
