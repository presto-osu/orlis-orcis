package koeln.mop.elpeefpe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andreas Streichardt on 20.06.2016.
 */
public class Value {
    public Map<DamageType, Integer> damage;
    public int value;
    public int multiplier = 1;

    public Value() {
        damage = new HashMap<>();
        for (DamageType damageType: DamageType.ordered) {
            damage.put(damageType, 0);
        }
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public ArrayList<ValueChange> applyValueChange(ValueChange change) {
        ArrayList<ValueChange> actualChanges = new ArrayList<ValueChange>();

        if (Math.abs(change.change) > 1) {
            throw new RuntimeException("Values > 1 not yet implemented");
        }
        if (change.change < 0) {
            if (damage.get(change.type) > 0) {
                damage.put(change.type, damage.get(change.type) + change.change);
                actualChanges.add(change);
            }
        } else {
            int total = 0;
            for (Map.Entry<DamageType, Integer> entry : damage.entrySet()) {
                total += entry.getValue();
            }

            if (total < value * multiplier) {
                damage.put(change.type, damage.get(change.type) + 1);
                actualChanges.add(change);
                return actualChanges;
            } else {
                // mop: return early if we are full
                if (damage.get(DamageType.VERZEHRT) == total) {
                    return actualChanges;
                }
                int damageIndex = Arrays.asList(DamageType.ordered).indexOf(change.type);
                for (int i=0;i<DamageType.ordered.length - 1;i++) {
                    if (damage.get(DamageType.ordered[i]) > 0) {
                        damage.put(DamageType.ordered[i], damage.get(DamageType.ordered[i]) - 1);
                        DamageType appliedDamage = i < damageIndex ? DamageType.ordered[damageIndex] : DamageType.ordered[i + 1];
                        damage.put(appliedDamage, damage.get(appliedDamage) + 1);

                        actualChanges.add(new ValueChange(DamageType.ordered[i], -1));
                        actualChanges.add(new ValueChange(appliedDamage, 1));
                        break;
                    }
                }
            }
        }
        return actualChanges;
    }
}