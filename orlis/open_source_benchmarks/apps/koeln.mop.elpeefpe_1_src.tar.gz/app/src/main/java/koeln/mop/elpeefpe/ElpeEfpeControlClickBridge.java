package koeln.mop.elpeefpe;

import android.content.Context;
import android.renderscript.Sampler;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Andreas Streichardt on 20.06.2016.
 */
public class ElpeEfpeControlClickBridge implements View.OnClickListener {
    private DBHandler dbHandler;
    private Character character;

    private Value mValue;
    private ElpeEfpeTableView mTableView;
    private ValueChange mValueChange;

    public ElpeEfpeControlClickBridge(DBHandler dbHandler, Character character, Value value, ElpeEfpeTableView tableView, ValueChange valueChange) {
        this.dbHandler = dbHandler;
        this.character = character;
        mValue = value;
        mTableView = tableView;
        mValueChange = valueChange;
    }

    @Override
    public void onClick(View v) {
        ArrayList<ValueChange> changes = mValue.applyValueChange(mValueChange);
        for (ValueChange change: changes) {
            if (change.change > 0) {
                for (int i=0;i<change.change;i++) {
                    mTableView.addDamage(change.type);
                }
            } else {
                for (int i=change.change;i<0;i++) {
                    mTableView.removeDamage(change.type);
                }
            }
        }
        dbHandler.save(character);
    }
}
