package utility.relConverter;

import classDiagram.ClassDiagram;
import classDiagram.components.*;
import classDiagram.relationships.*;
import graphic.GraphicComponent;
import graphic.GraphicView;
import graphic.entity.ClassView;
import graphic.entity.RelationalEntityView;
import graphic.relations.MultiView;
import graphic.relations.RelationView;
import swing.MultiViewManager;
import swing.PanelClassDiagram;
import swing.propretiesView.PropretiesChanger;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class RelConverter {
    private static final RelConverter instance = new RelConverter();

    public static RelConverter getInstance() {
        return instance;
    }

    private GraphicView graphicView;
    private GraphicView relGraphicView;

    private boolean converted = false;

    private Map<Entity, RelationalEntity> alreadyConverted;

    private Map<Multi, RelationalEntity> createdFromMulti;

    ClassDiagram cdResult;

    public void setGraphicView(GraphicView graphicView) {
        this.graphicView = graphicView;
        converted = false;
    }

    public void umlToRel() {
        if (graphicView.isRelational() || converted)
            return;

        alreadyConverted = new HashMap<>();
        createdFromMulti = new HashMap<>();

        relGraphicView = MultiViewManager.addNewView(graphicView.getName() + " - REL", true);

        for (GraphicComponent gc : graphicView.getAllDiagramComponents()) {
            if (gc instanceof ClassView) {
                RelationalEntity re = convertClass((ClassEntity) gc.getAssociedComponent());
                addRelationalEntity(re, gc.getBounds());
            }
        }

        for (GraphicComponent gc : graphicView.getAllDiagramComponents()) {
            if (gc instanceof RelationView) {
                convertRelation((Relation)gc.getAssociedComponent());
            } else if (gc instanceof MultiView) {
                convertMulti((Multi) gc.getAssociedComponent(), gc.getBounds());
            }
        }

        converted = true;
        MultiViewManager.openView(relGraphicView);
    }

    /**
     * Add a relational entity to the diagram
     * @param re the relational entity to add
     * @param bounds the entity's position
     */
    public void addRelationalEntity(RelationalEntity re, Rectangle bounds) {
        relGraphicView.addTableEntity(re);
        GraphicComponent component = relGraphicView.searchAssociedComponent(re);
        component.setBounds(bounds);

        PropretiesChanger.getInstance().addRelationalEntity(re);
        PanelClassDiagram.getInstance().getHierarchicalView().addRelationalEntity(re);

        for (RelationalAttribute ra : re.getAttributes()) {
            ((RelationalEntityView) component).addAttribute(ra, false);
        }
    }

    /**
     * Create a converted association and add it to the class diagram
     * @param source the source entity of the converted association
     * @param target the target entity of the converted association
     * @param name the name of the converted association
     */
    private void addRelAssociation(RelationalEntity source, RelationalEntity target, String name) {
        RelAssociation ra = new RelAssociation(source, target);
        ra.setName(name);

        relGraphicView.addRelAssociation(ra);

        PropretiesChanger.getInstance().addRelationalAssociation(ra);
        PanelClassDiagram.getInstance().getHierarchicalView().addRelationalAssociation(ra);
    }

    /**
     * Convert a class to a table or returns the associated table if it already has been converted
     * @param classEntity the class to convert
     * @return the converted class
     */
    private RelationalEntity convertClass(ClassEntity classEntity) {
        if (alreadyConverted.containsKey(classEntity)) {
            return alreadyConverted.get(classEntity);
        }else {
            RelationalEntity re = new RelationalEntity(classEntity.getName());
            for (Attribute a : classEntity.getAttributes()) {
                re.addAttribute(convertAttribute(a));
            }
            alreadyConverted.put(classEntity, re);
            return re;
        }
    }

    /**
     * Convert an attribute to a relational attribute
     * @param attribute the attribute to convert
     * @return the converted attribute
     */
    private RelationalAttribute convertAttribute(Attribute attribute) {
        return new RelationalAttribute(attribute.getName(), attribute.getType());
    }

    /**
     * Convert relations according to what type they are.
     * @param relation the relation to convert
     */
    private void convertRelation(Relation relation) {
        Entity source = relation.getSource();
        Entity target = relation.getTarget();
        if (!(source instanceof ClassEntity) || !(target instanceof ClassEntity))
            return;

        if (relation instanceof RelAssociation)
            return;

        if (relation instanceof Inheritance) {
            convertInheritance((Inheritance) relation);
        } else {
            convertBinary((Binary) relation);
        }

    }

    /**
     * Converts a binary association to a relational association
     * @param binary the association to convert
     */
    private void convertBinary(Binary binary) { //TODO make sure this works
        Association.NavigateDirection nav = binary.getDirected();
        Multiplicity m1 = binary.getRoles().getFirst().getMultiplicity();
        Multiplicity m2 = binary.getRoles().getLast().getMultiplicity();
        String name = binary.getName();
        RelationalEntity source;
        RelationalEntity target;

        if (m1.isZero()  || m2.isZero()) { // shouldn't be zeroes so we use nav dir

            if (nav == Association.NavigateDirection.FIRST_TO_SECOND ||
                    nav == Association.NavigateDirection.BIDIRECTIONAL) {
                source = convertClass((ClassEntity) binary.getSource());
                target = convertClass((ClassEntity) binary.getTarget());
            } else {
                target = convertClass((ClassEntity) binary.getSource());
                source = convertClass((ClassEntity) binary.getTarget());
            }
            addRelAssociation(source, target, name);

        } else if (m1.isZeroToOne() || m1.isOne()) {
            if (m2.isZeroToOne() || m2.isOne()) { //1-1
                source = convertClass((ClassEntity) binary.getSource());
                target = convertClass((ClassEntity) binary.getTarget());
            } else { //1-N
                source = convertClass((ClassEntity) binary.getTarget());
                target = convertClass((ClassEntity) binary.getSource());
            }
            addRelAssociation(source, target, name);
        } else {
            if (m2.isZeroToOne() || m2.isOne()) { //N-1
                source = convertClass((ClassEntity) binary.getSource());
                target = convertClass((ClassEntity) binary.getTarget());
                addRelAssociation(source, target, name);
            } else { //N-M
                source = convertClass((ClassEntity) binary.getSource());
                target = convertClass((ClassEntity) binary.getTarget());
                RelationalEntity re = new RelationalEntity(source.getName() + "-" + target.getName());
                source.getPrimaryKey().getKeyComponents().forEach(re::addAttribute);
                target.getPrimaryKey().getKeyComponents().forEach(re::addAttribute);
                re.getAttributes().forEach(attribute -> re.getPrimaryKey().addKeyComponent(attribute));

                Rectangle sourceBounds = relGraphicView.searchAssociedComponent(source).getBounds();
                Rectangle targetBounds = relGraphicView.searchAssociedComponent(target).getBounds();

                Rectangle bounds = new Rectangle((sourceBounds.x + targetBounds.x)/2,
                        (sourceBounds.y + targetBounds.y)/2, sourceBounds.width, sourceBounds.height);

                addRelationalEntity(re, bounds);

                addRelAssociation(re, source, name + " 1");
                addRelAssociation(re, target, name + " 2");
            }
        }
    }

    /**
     * Convert an inheritance to a relational association
     * @param inheritance the inheritance to convert
     */
    private void convertInheritance(Inheritance inheritance) {
        RelationalEntity target = convertClass((ClassEntity) inheritance.getSource());
        RelationalEntity source = convertClass((ClassEntity) inheritance.getTarget());
        target.setPrimaryKey(source.getPrimaryKey());
        addRelAssociation(source, target, "");
    }

    /**
     * Convert a multi association to table and relational associations
     * @param multi the multi association to convert
     */
    private void convertMulti(Multi multi, Rectangle bounds) {
        String name = multi.getName().equals("") ? "Multi" : multi.getName();
        RelationalEntity source = new RelationalEntity(name);
        addRelationalEntity(source, bounds);
        for (Role role : multi.getRoles()){
            if (role.getEntity() instanceof ClassEntity) {
                RelationalEntity target = convertClass((ClassEntity) role.getEntity());
                addRelAssociation(source, target, "");
            }
        }
    }
}
