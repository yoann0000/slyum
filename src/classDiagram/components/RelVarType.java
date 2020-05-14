package classDiagram.components;

public class RelVarType extends RelationalType{
    private int length;

    public RelVarType(String name) {
        super(name);
        length = 0;
    }

    public RelVarType(String name, int length) {
        super(name);
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String onSelected(){
        return this.getName() + "(" + length + ")";
    }
}
