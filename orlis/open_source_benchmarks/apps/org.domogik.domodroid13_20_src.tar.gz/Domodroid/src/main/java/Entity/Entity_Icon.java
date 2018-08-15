package Entity;

public class Entity_Icon {
    private String name;
    private String value;
    private int reference;


    public Entity_Icon(String name, String value, int reference) {
        this.name = name;
        this.value = value;
        this.reference = reference;
    }


    public int getReference() {
        return reference;
    }


    public void setReference(int reference) {
        this.reference = reference;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getValue() {
        return value;
    }


    public void setValue(String value) {
        this.value = value;
    }

}
