package classDiagram.components;

import java.util.ArrayList;
import java.util.List;

public class StoredProcedure extends Type{
    private String procedure = "";
    private List<String> parameters = new ArrayList<>();

    public StoredProcedure(String name) {
        super(name);
    }

    public StoredProcedure(String name, int id) {
        super(name, id);
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void addParameter(String parameter) {
        parameters.add(parameter);
    }

    public void removeParameter(String parameter) {
        parameters.remove(parameter);
    }
}
