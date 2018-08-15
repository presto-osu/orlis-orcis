package koeln.mop.elpeefpe;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Map;

/**
 * Created by Andreas Streichardt on 22.06.2016.
 */
public class CharacterEditActivity extends AppCompatActivity {
    protected Character character;
    protected CharacterForm characterForm;

    protected int getContentViewId() {
        return R.layout.activity_character_edit;
    }

    protected int getToolbarId() {
        return R.id.edit_toolbar;
    }

    protected int getMenuId() {
        return R.menu.edit;
    }

    protected int getContainerId() {
        return R.id.character_edit_container;
    }

    protected Character initCharacter() {
        DBHandler dbHandler = new DBHandler(this);
        return dbHandler.find(getIntent().getIntExtra(CharacterDetailFragment.ARG_CHARACTER_ID, 0));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());
        Toolbar toolbar = (Toolbar) findViewById(getToolbarId());
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();

            character = this.initCharacter();

            CharacterEditFragment fragment = new CharacterEditFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(getContainerId(), fragment)
                    .commit();

            characterForm = new CharacterForm(character);
            fragment.setCharacterForm(characterForm);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            Intent intent = getParentActivityIntent();
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                intent.putExtras(extras);
            }
            NavUtils.navigateUpTo(this, intent);
            return true;
        } else if (id == R.id.action_save) {
            DBHandler db = new DBHandler(this);

            if (characterForm.isValid()) {
                character.name = characterForm.getName().trim();

                int oldElpe = character.elpe.value;
                int oldEfpe = character.efpe.value;

                character.elpe.value = Integer.parseInt(characterForm.getElpe());
                character.efpe.value = Integer.parseInt(characterForm.getEfpe());

                // mop: ugly!
                if (oldElpe > character.elpe.value) {
                    int total = 0;
                    for (Map.Entry<DamageType, Integer> entry : character.elpe.damage.entrySet()) {
                        total += entry.getValue();
                    }

                    int toRemove = total - character.elpe.value * character.elpe.multiplier;
                    if (toRemove > 0) {
                        for (DamageType damage : DamageType.ordered) {
                            int value = character.elpe.damage.get(damage);
                            int sub;
                            if (value < toRemove) {
                                sub = value;
                            } else {
                                sub = toRemove;
                            }
                            character.elpe.damage.put(damage, value - sub);
                            toRemove -= sub;
                            if (toRemove == 0) {
                                break;
                            }
                        }
                    }
                }

                if (oldEfpe > character.efpe.value) {
                    int total = 0;
                    for (Map.Entry<DamageType, Integer> entry : character.efpe.damage.entrySet()) {
                        total += entry.getValue();
                    }

                    int toRemove = total - character.efpe.value * character.efpe.multiplier;
                    if (toRemove > 0) {
                        for (DamageType damage : DamageType.ordered) {
                            int value = character.efpe.damage.get(damage);
                            int sub;
                            if (value < toRemove) {
                                sub = value;
                            } else {
                                sub = toRemove;
                            }
                            character.efpe.damage.put(damage, value - sub);
                            toRemove -= sub;
                            if (toRemove == 0) {
                                break;
                            }
                        }
                    }
                }
                character = db.save(character);

                Intent intent = new Intent(this, CharacterDetailActivity.class);
                intent.putExtra(CharacterDetailFragment.ARG_CHARACTER_ID, character.id);

                this.startActivity(intent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(getMenuId(), menu);
        return true;
    }
}
