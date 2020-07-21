package utility.relConverter;

import classDiagram.ClassDiagram;
import classDiagram.IDiagramComponent;
import classDiagram.components.*;
import classDiagram.relationships.*;
import graphic.GraphicComponent;
import graphic.GraphicView;
import graphic.entity.RelationalEntityView;

import java.util.HashMap;
import java.util.LinkedList;
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

    public GraphicView getRelGraphicView() {
        if(converted)
            return relGraphicView;
        else
            return umlToRel() ? relGraphicView : null;
    }

    public boolean umlToRel() {
        if (graphicView.isRelational())
            return false;

        classDiagramUmlToRel();

        relGraphicView = new GraphicView(cdResult, false);

        relGraphicView.setName(graphicView.getName() + " - REL");

        relGraphicView.setRelational(true);

        for (Entity entity : relGraphicView.getClassDiagram().getEntities()) {
            if(entity instanceof RelationalEntity) {
                relGraphicView.addTableEntity((RelationalEntity) entity);
            }
        }

        for (Relation relation : relGraphicView.getClassDiagram().getRelations()) {
            if(relation instanceof RelAssociation) {
                relGraphicView.addRelAssociation((RelAssociation) relation);
            }
        }

        for (GraphicComponent gc : graphicView.getAllDiagramComponents()) {
            IDiagramComponent component = gc.getAssociedComponent();
            if(component instanceof ClassEntity) {
                GraphicComponent relgc = relGraphicView.searchAssociedComponent(alreadyConverted.get(component));
                relgc.setBounds(gc.getBounds());
                for (RelationalAttribute ra : ((RelationalEntity)relgc.getAssociedComponent()).getAttributes()) {
                    ((RelationalEntityView)relgc).addAttribute(ra, false);
                }
            } else if (component instanceof Multi) {
                GraphicComponent relgc = relGraphicView.searchAssociedComponent(createdFromMulti.get(component));
                relgc.setBounds(gc.getBounds());
                for (RelationalAttribute ra : ((RelationalEntity)relgc.getAssociedComponent()).getAttributes()) {
                    ((RelationalEntityView)relgc).addAttribute(ra, false);
                }
            }
        }
        converted = true;
        return true;
    }

    /**
     * convert a uml class diagram to a relational diagram
     * @return the converted diagram
     */
    public void classDiagramUmlToRel() {

        alreadyConverted = new HashMap<>();
        createdFromMulti = new HashMap<>();
        cdResult = new ClassDiagram();

        LinkedList<ClassEntity> classEntities = new LinkedList<>();
        LinkedList<Relation> relations = new LinkedList<>();
        ClassDiagram cd = graphicView.getClassDiagram();


        for (Entity entity : cd.getEntities()) {
            if(entity instanceof ClassEntity) {
                classEntities.add((ClassEntity) entity);
            }
        }

        for (Relation relation : cd.getRelations()) {
            if (relation.getSource() instanceof ClassEntity && relation.getTarget() instanceof ClassEntity) {
                relations.add(relation);
            }
        }

        for (ClassEntity classEntity : classEntities) {
            cdResult.addTableEntity(convertClass(classEntity), false);
        }

        for (Relation relation : relations) {
            convertRelation(relation);
        }
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
            cdResult.addTableEntity(re, false);
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
            RelAssociation r = convertInheritance((Inheritance) relation);
            cdResult.addRelAssociation(r, false);

        } else if (relation instanceof Multi) {
            convertMulti((Multi) relation);
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
                addRelAssociation(source, target, name);
            } else { //1-N
                source = convertClass((ClassEntity) binary.getTarget());
                target = convertClass((ClassEntity) binary.getSource());
                addRelAssociation(source, target, name);
            }
        } else {
            if (m2.isZeroToOne() || m2.isOne()) { //N-1
                source = convertClass((ClassEntity) binary.getSource());
                target = convertClass((ClassEntity) binary.getTarget());
                addRelAssociation(source, target, name);
            } else { //N-M
                source = convertClass((ClassEntity) binary.getSource());
                target = convertClass((ClassEntity) binary.getTarget());
                RelationalEntity re = new RelationalEntity(source.getName() + " - " + target.getName());
                source.getPrimaryKey().getKeyComponents().forEach(re::addAttribute);
                target.getPrimaryKey().getKeyComponents().forEach(re::addAttribute);
                re.getAttributes().forEach(attribute -> re.getPrimaryKey().addKeyComponent(attribute));
                cdResult.addTableEntity(re);

                addRelAssociation(re, source, name + " 1");
                addRelAssociation(re, target, name + " 2");
            }
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
        cdResult.addRelAssociation(ra, false);
    }

    /**
     * Convert an inheritance to a relational association
     * @param inheritance the inheritance to convert
     * @return the converted inheritance
     */
    private RelAssociation convertInheritance(Inheritance inheritance) {
        RelationalEntity target = convertClass((ClassEntity) inheritance.getSource());
        RelationalEntity source = convertClass((ClassEntity) inheritance.getTarget());
        target.setPrimaryKey(source.getPrimaryKey());
        return new RelAssociation(source, target);
    }

    /**
     * Convert a multi association to table and relational associations
     * @param multi the multi association to convert
     */
    private void convertMulti(Multi multi) {
        String name = multi.getName().equals("") ? "Multi" : multi.getName();
        RelationalEntity source = new RelationalEntity(name);
        cdResult.addTableEntity(source, false);
        for (Role role : multi.getRoles()){
            if (role.getEntity() instanceof ClassEntity) {
                RelationalEntity target = convertClass((ClassEntity) role.getEntity());
                RelAssociation ra = new RelAssociation(source, target);
                cdResult.addRelAssociation(ra, false);
            }
        }
        createdFromMulti.put(multi, source);
    }
}
