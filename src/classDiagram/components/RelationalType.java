package classDiagram.components;

public class RelationalType {
    private String name;

    public RelationalType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String onSelected(){
        return name;
    }
}
