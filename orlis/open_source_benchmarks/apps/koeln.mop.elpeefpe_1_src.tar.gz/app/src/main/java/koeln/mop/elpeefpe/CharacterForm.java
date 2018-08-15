package koeln.mop.elpeefpe;

/**
 * Created by Andreas Streichardt on 03.07.2016.
 */
public class CharacterForm {
    private String name;
    private String elpe;
    private String efpe;

    public CharacterForm(Character character) {
        this.name = character.name;
        this.elpe = Integer.toString(character.elpe.value);
        this.efpe = Integer.toString(character.efpe.value);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getElpe() {
        return elpe;
    }

    public void setElpe(String elpe) {
        this.elpe = elpe;
    }

    public String getEfpe() {
        return efpe;
    }

    public void setEfpe(String efpe) {
        this.efpe = efpe;
    }

    public boolean isValid() {
        try {
            return name.trim().length() > 0
                    && Integer.parseInt(elpe) >= 0
                    && Integer.parseInt(efpe) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
