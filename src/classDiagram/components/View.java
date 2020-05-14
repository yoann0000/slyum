package classDiagram.components;

public class View extends Type {
    private String procedure = "";

    public View(String name) {
        super(name);
    }

    public View(String name, int id) {
        super(name, id);
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }
}
