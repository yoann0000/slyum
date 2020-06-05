package change;

import classDiagram.IDiagramComponent.UpdateMessage;
import classDiagram.components.View;
import classDiagram.components.ViewList;

public class BufferCreationView implements Changeable {

    private ViewList viewList;
    private View view;
    private boolean creation;
    private int index;

    public BufferCreationView(ViewList viewList, View view, boolean creation, int index) {
        this.viewList = viewList;
        this.view = view;
        this.creation = creation;
        this.index = index;
    }

    @Override
    public void restore() {
        if (creation) {
            viewList.addView(view);
            viewList.notifyObservers(UpdateMessage.ADD_ENUM_NO_EDIT);
            viewList.moveEnumPosition(view, index
                    - viewList.getEnumValues().size() + 1);
            viewList.notifyObservers();
        } else {
            viewList.removeView(view);
            viewList.notifyObservers();
        }
    }

    @Override
    public Object getAssociedComponent() {
        return view;
    }

}
