package classDiagram.components;

import change.BufferCreationView;
import change.Change;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import swing.XMLParser.EntityType;

public class ViewList extends Entity {

    public static final String STEREOTYPE_VIEW = "viewList";

    private List<View> views = new LinkedList<>();

    public ViewList() {
        super("Views");
        initializeComponents();
    }

    public ViewList(int id) {
        super("Views", id);
        initializeComponents();
    }

    public ViewList(Entity e) {
        super(e);
        initializeComponents();
    }

    public void initializeComponents() {
        setStereotype(STEREOTYPE_VIEW);
    }

    public boolean addView(View view) {
        if (!views.contains(view)) {
            views.add(view);
            setChanged();
            int index = views.indexOf(view);
            Change.push(new BufferCreationView(this, view, false, index));
            Change.push(new BufferCreationView(this, view, true, index));
            return true;
        }
        return false;
    }

    public void createView() {
        View view = new View("VIEW", "");

        if (addView(view)) notifyObservers(UpdateMessage.ADD_VIEW);
    }

    public boolean removeView(View view) {
        int index = views.indexOf(view);
        boolean success = views.remove(view);
        if (success) {
            setChanged();
            Change.push(new BufferCreationView(this, view, true, index));
            Change.push(new BufferCreationView(this, view, false, index));
        }
        return success;
    }

    public void moveEnumPosition(View value, int offset) {
        moveComponentPosition(views, value, offset);
    }

    /**
     * Return views. Not a copy.
     *
     * @return list of views (not copied).
     */
    public List<View> getEnumValues() {
        return views;
    }

    @Override
    protected String getEntityType() {
        return EntityType.ENUM.toString();
    }

    @Override
    public Element getXmlElement(Document doc) {
        Element viewList = super.getXmlElement(doc);
        for (View view : views)
            viewList.appendChild(view.getXmlElement(doc));
        return viewList;
    }

    @Override
    public ViewList clone() throws CloneNotSupportedException {
        ViewList entity = (ViewList) super.clone();

        for (View view : views)
            entity.addView(new View(view.getName(), view.getProcedure()));

        return entity;
    }
}
