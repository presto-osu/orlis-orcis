package koeln.mop.elpeefpe;

/**
 * Created by Andreas Streichardt on 18.06.2016.
 */
public enum DamageType {
    KANALISIERT, ERSCHOEPFT, VERZEHRT;

    public static DamageType[] ordered = new DamageType[]{DamageType.KANALISIERT, DamageType.ERSCHOEPFT, DamageType.VERZEHRT};
}
