package koeln.mop.elpeefpe;

import android.app.Activity;
import android.net.LinkAddress;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * A fragment representing a single Character detail screen.
 * This fragment is either contained in a {@link CharacterListActivity}
 * in two-pane mode (on tablets) or a {@link CharacterDetailActivity}
 * on handsets.
 */
public class CharacterDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_CHARACTER_ID = "id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Character mCharacter;

    private DBHandler db;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CharacterDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_CHARACTER_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            db = new DBHandler(getContext());
            mCharacter = db.find(getArguments().getInt(ARG_CHARACTER_ID));

            getActivity().setTitle(mCharacter.name);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.character_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mCharacter != null) {
            if (mCharacter.elpe.value > 0) {
                rootView.findViewById(R.id.elpe_container).setVisibility(LinearLayout.VISIBLE);
                ((TextView) rootView.findViewById(R.id.elpe_value)).setText(Integer.toString(mCharacter.elpe.value));
                ElpeEfpeTableView elpeTable = (ElpeEfpeTableView) rootView.findViewById(R.id.elpe_table);
                elpeTable.setValue(mCharacter.elpe);
                elpeTable.setDamage(mCharacter.elpe.damage);

                HashMap<Integer, DamageType> m = new HashMap<>();
                m.put(R.id.elpe_kanalisiert, DamageType.KANALISIERT);
                m.put(R.id.elpe_erschoepft, DamageType.ERSCHOEPFT);
                m.put(R.id.elpe_verzehrt, DamageType.VERZEHRT);

                for (Map.Entry<Integer, DamageType> entry : m.entrySet()) {
                    ElpeEfpeControlView elpeEfpeControlView = (ElpeEfpeControlView) rootView.findViewById(entry.getKey());
                    ValueChange change = new ValueChange(entry.getValue(), 1);
                    elpeEfpeControlView.onAdd(new ElpeEfpeControlClickBridge(db, mCharacter, mCharacter.elpe, elpeTable, change));
                }

                for (Map.Entry<Integer, DamageType> entry : m.entrySet()) {
                    ElpeEfpeControlView elpeEfpeControlView = (ElpeEfpeControlView) rootView.findViewById(entry.getKey());
                    ValueChange change = new ValueChange(entry.getValue(), -1);
                    elpeEfpeControlView.onRemove(new ElpeEfpeControlClickBridge(db, mCharacter, mCharacter.elpe, elpeTable, change));
                }
            }
            if (mCharacter.efpe.value > 0) {
                rootView.findViewById(R.id.efpe_container).setVisibility(LinearLayout.VISIBLE);
                ((TextView) rootView.findViewById(R.id.efpe_value)).setText(Integer.toString(mCharacter.efpe.value));
                ElpeEfpeTableView efpeTable = (ElpeEfpeTableView) rootView.findViewById(R.id.efpe_table);
                efpeTable.setValue(mCharacter.efpe);
                efpeTable.setDamage(mCharacter.efpe.damage);

                HashMap<Integer, DamageType> m = new HashMap<>();
                m.put(R.id.efpe_kanalisiert, DamageType.KANALISIERT);
                m.put(R.id.efpe_erschoepft, DamageType.ERSCHOEPFT);
                m.put(R.id.efpe_verzehrt, DamageType.VERZEHRT);

                for (Map.Entry<Integer, DamageType> entry : m.entrySet()) {
                    ElpeEfpeControlView elpeEfpeControlView = (ElpeEfpeControlView) rootView.findViewById(entry.getKey());
                    ValueChange change = new ValueChange(entry.getValue(), 1);
                    elpeEfpeControlView.onAdd(new ElpeEfpeControlClickBridge(db, mCharacter, mCharacter.efpe, efpeTable, change));
                }

                for (Map.Entry<Integer, DamageType> entry : m.entrySet()) {
                    ElpeEfpeControlView elpeEfpeControlView = (ElpeEfpeControlView) rootView.findViewById(entry.getKey());
                    ValueChange change = new ValueChange(entry.getValue(), -1);
                    elpeEfpeControlView.onRemove(new ElpeEfpeControlClickBridge(db, mCharacter, mCharacter.efpe, efpeTable, change));
                }
            }
        }

        return rootView;
    }
}
