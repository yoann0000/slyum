package change;

import classDiagram.components.View;

public class BufferView implements Changeable {

    private View current, copy;

    public BufferView(View view) {
        current = view;
        try {
            copy = current.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void restore() {
        current.setName(copy.getName());
        current.setProcedure(copy.getProcedure());
        current.notifyObservers();
    }

    @Override
    public Object getAssociedComponent() {
        return current;
    }

}
