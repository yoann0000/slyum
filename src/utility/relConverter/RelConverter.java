package utility.relConverter;

import classDiagram.ClassDiagram;
import classDiagram.components.*;
import classDiagram.relationships.*;
import graphic.GraphicComponent;
import graphic.GraphicView;
import graphic.entity.ClassView;
import graphic.entity.InterfaceView;
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

    private ClassDiagram classDiagram;

    private boolean converted = false;

    private Map<Entity, RelationalEntity> alreadyConverted;

    public void setGraphicView(GraphicView graphicView) {
        this.graphicView = graphicView;
        converted = false;
    }

    public void umlToRel() {
        if (graphicView.isRelational() || converted)
            return;

        classDiagram = graphicView.getClassDiagram();

        if (classDiagram == null)
            return;

        alreadyConverted = new HashMap<>();

        relGraphicView = MultiViewManager.addNewView(graphicView.getName() + " - REL", true);

        for (GraphicComponent gc : graphicView.getAllDiagramComponents()) {
            if (gc instanceof ClassView || gc instanceof InterfaceView) {
                RelationalEntity re = convertSimpleEntity((SimpleEntity) gc.getAssociedComponent());
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

        for (RelationalAttribute ra : re.getAttributes()) {
            ((RelationalEntityView) component).addAttribute(ra, false);
        }

        classDiagram.addTableEntity(re, false);
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

        classDiagram.addRelAssociation(ra, false);
    }

    /**
     * Convert a class to a table or returns the associated table if it already has been converted
     * @param simpleEntity the class to convert
     * @return the converted class
     */
    private RelationalEntity convertSimpleEntity(SimpleEntity simpleEntity) {
        if (!alreadyConverted.containsKey(simpleEntity)) {
            RelationalEntity re = new RelationalEntity(simpleEntity.getName());
            for (Attribute a : simpleEntity.getAttributes()) {
                re.addAttribute(convertAttribute(a));
            }
            alreadyConverted.put(simpleEntity, re);
        }
        return alreadyConverted.get(simpleEntity);
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
        if (!(source instanceof ClassEntity || source instanceof InterfaceEntity)
                || !(target instanceof ClassEntity || source instanceof InterfaceEntity))
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
    private void convertBinary(Binary binary) {
        Association.NavigateDirection nav = binary.getDirected();
        Multiplicity m1 = binary.getRoles().getFirst().getMultiplicity();
        Multiplicity m2 = binary.getRoles().getLast().getMultiplicity();
        String name = binary.getName();
        RelationalEntity source;
        RelationalEntity target;

        if (m1.isZero()  || m2.isZero()) { // shouldn't be zeroes so we use nav dir

            if (nav == Association.NavigateDirection.FIRST_TO_SECOND ||
                    nav == Association.NavigateDirection.BIDIRECTIONAL) {
                source = convertSimpleEntity((ClassEntity) binary.getSource());
                target = convertSimpleEntity((ClassEntity) binary.getTarget());
            } else {
                target = convertSimpleEntity((ClassEntity) binary.getSource());
                source = convertSimpleEntity((ClassEntity) binary.getTarget());
            }
            addRelAssociation(source, target, name);

        } else if (m1.isZeroToOne() || m1.isOne()) {
            if (m2.isZeroToOne()) { //1-0..1 or 0..1-0..1
                source = convertSimpleEntity((ClassEntity) binary.getSource());
                target = convertSimpleEntity((ClassEntity) binary.getTarget());
            } else if (m2.isOne()) { //1-1 or 0..1-1
                source = convertSimpleEntity((ClassEntity) binary.getTarget());
                target = convertSimpleEntity((ClassEntity) binary.getSource());
            } else { //1-N
                source = convertSimpleEntity((ClassEntity) binary.getTarget());
                target = convertSimpleEntity((ClassEntity) binary.getSource());
            }
            addRelAssociation(source, target, name);
        } else {
            if (m2.isZeroToOne() || m2.isOne()) { //N-1
                source = convertSimpleEntity((ClassEntity) binary.getSource());
                target = convertSimpleEntity((ClassEntity) binary.getTarget());
                addRelAssociation(source, target, name);
            } else { //N-M
                source = convertSimpleEntity((ClassEntity) binary.getSource());
                target = convertSimpleEntity((ClassEntity) binary.getTarget());
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
        RelationalEntity target = convertSimpleEntity((ClassEntity) inheritance.getSource());
        RelationalEntity source = convertSimpleEntity((ClassEntity) inheritance.getTarget());
        source.setPrimaryKey(target.getPrimaryKey());
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
                RelationalEntity target = convertSimpleEntity((ClassEntity) role.getEntity());
                addRelAssociation(source, target, "");
            }
        }
    }
}
