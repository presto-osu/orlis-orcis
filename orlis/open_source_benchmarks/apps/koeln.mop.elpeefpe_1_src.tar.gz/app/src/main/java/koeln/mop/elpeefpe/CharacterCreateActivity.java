package koeln.mop.elpeefpe;

/**
 * Created by Andreas Streichardt on 22.06.2016.
 */
public class CharacterCreateActivity extends CharacterEditActivity {
    protected Character initCharacter() {
        Character character = new Character();
        character.name = "";
        character.setValues(0, 0);
        return character;
    }

    protected int getContentViewId() {
        return R.layout.activity_character_create;
    }

    protected int getToolbarId() {
        return R.id.create_toolbar;
    }

    protected int getMenuId() {
        return R.menu.create;
    }

    protected int getContainerId() {
        return R.id.character_create_container;
    }
}
