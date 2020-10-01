package swing;

import classDiagram.ClassDiagram.ViewEntity;
import classDiagram.IDiagramComponent;
import classDiagram.IDiagramComponent.UpdateMessage;
import classDiagram.components.*;
import classDiagram.components.Method.ParametersViewStyle;
import classDiagram.relationships.*;
import classDiagram.relationships.Association.NavigateDirection;
import classDiagram.verifyName.*;
import graphic.GraphicComponent;
import graphic.GraphicView;
import graphic.entity.EntityView;
import graphic.entity.EnumView;
import graphic.entity.EnumView.TypeEnumDisplay;
import graphic.entity.SimpleEntityView;
import graphic.relations.LineCommentary;
import graphic.relations.LineView;
import graphic.relations.MultiLineView;
import graphic.relations.RelationGrip;
import graphic.textbox.TextBox;
import graphic.textbox.TextBoxCommentary;
import graphic.textbox.TextBoxLabel;
import graphic.textbox.TextBoxRole;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.SwingUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.helpers.DefaultHandler;
import swing.propretiesView.DiagramPropreties;

/**
 * This class read the XML file and create the diagram UML structured from this.
 *
 * @author David Miserez
 * @version 1.0 - 25.07.2011
 */
public class XMLParser extends DefaultHandler {
    public enum Aggregation {
        AGGREGATE, COMPOSE, MULTI, NONE, REL
    }

    private static class Association {
        int id = -1;
        LinkedList<Role> role = new LinkedList<>();
        Aggregation aggregation = Aggregation.NONE;
        NavigateDirection direction = NavigateDirection.BIDIRECTIONAL;
        String name = null;
    }

    private static class ClassDiagram {
        LinkedList<UMLView> uMLView = new LinkedList<>();
        DiagramElements diagramElement = null;

        String name = "";
        String information = "";

        classDiagram.ClassDiagram.ViewEntity defaultViewEntities =
                GraphicView.getDefaultViewEntities();

        ParametersViewStyle defaultViewMethods =
                GraphicView.getDefaultViewMethods();

        boolean defaultViewEnum = GraphicView.getDefaultViewEnum();
        boolean defaultVisibleTypes = GraphicView.getDefaultVisibleTypes();
    }

    private static class ComponentView {
        int color = 0;
        int componentId = -1;
        boolean displayAttributes = true,
                displayMethods = true,
                displayDefault = true;
        TypeEnumDisplay typeEnumDisplay = TypeEnumDisplay.DEFAULT;
        Rectangle geometry = new Rectangle();
    }

    private static class Dependency {
        int id = -1;
        int source = -1;
        int target = -1;
        String label = null;
    }

    private static class DiagramElements {
        LinkedList<Association> association = new LinkedList<>();
        LinkedList<Dependency> dependency = new LinkedList<>();
        LinkedList<Entity> entity = new LinkedList<>();
        LinkedList<Inheritance> inheritance = new LinkedList<>();
    }

    private static class Entity {
        int id = -1;
        int associationClassID = -1;
        boolean isAbstract = false;
        String name = null;
        Visibility visibility = Visibility.PUBLIC;
        EntityType entityType = null;
        LinkedList<Variable> attribute = new LinkedList<>();
        LinkedList<Operation> method = new LinkedList<>();
        LinkedList<EnumValue> enums = new LinkedList<>();
        LinkedList<Operation> triggers = new LinkedList<>();
        String procedure = null;
        LinkedList<Key> keys = new LinkedList<>();
    }

    private static class Key {
        int id = -1;
        String name = null;
        String keyType;
        LinkedList<Integer> components = new LinkedList<>();
    }

    public enum EntityType {
        ASSOCIATION_CLASS, CLASS, INTERFACE, ENUM, TABLE, VIEW
    }

    private static class Inheritance {
        int id = -1;
        int child = -1;
        int parent = -1;
        boolean innerClass = false;
    }

    private static class MultiView {
        int color = 0;
        int relationId = -1;
        Rectangle multiViewBounds = new Rectangle();
        LinkedList<RelationView> multiLineView = new LinkedList<>();
    }

    private static class Note {
        int color = 0;
        String content;
        Rectangle bounds = new Rectangle();
        LinkedList<RelationView> line = new LinkedList<>();
    }

    private static class Operation {
        boolean isAbstract = false;
        boolean isStatic = false;
        String name = null;
        ParametersViewStyle view = ParametersViewStyle.TYPE_AND_NAME;
        String returnType = null;
        LinkedList<Variable> variable = new LinkedList<>();
        Visibility visibility = Visibility.PUBLIC;
        boolean isConstructor = false;
        TriggerType triggerType = TriggerType.FOR_EACH_ROW;
        ActivationTime activationTime = ActivationTime.AFTER_CREATION;
        String procedure = null;
    }

    private static class RelationView {
        int relationId = -1;
        int color = 0;
        Rectangle labelAssociation = new Rectangle();
        LinkedList<Point> line = new LinkedList<>();
        LinkedList<Rectangle> multiplicityAssociations = new LinkedList<>();
        LinkedList<Rectangle> roleAssociations = new LinkedList<>();
    }

    private static class Role {
        int componentId = -1;
        String name = null;
        Multiplicity multiplicity = null;
        Visibility visibility = Visibility.PUBLIC;
    }

    private static class UMLView {

        GraphicView graphicView;
        String name = null;

        LinkedList<Note> notes = new LinkedList<>();

        HashMap<Integer, ComponentView> componentView = new HashMap<>();
        HashMap<Integer, MultiView> multiView = new HashMap<>();
        HashMap<Integer, RelationView> relationView = new HashMap<>();

        public UMLView() {
            graphicView = MultiViewManager.getSelectedGraphicView();
        }

        public UMLView(String name, boolean open, boolean rel) {
            this.name = name;

            if (open)
                graphicView = MultiViewManager.addAndOpenNewView(name, rel);
            else
                graphicView = MultiViewManager.addNewView(name, rel);
        }
    }


    // UML STRUCTURE
    private static class Variable {
        boolean constant = false;
        String defaultValue = null;
        boolean isStatic = false;
        String name = null;
        Type type = null;
        Visibility visibility = null;
        boolean unique = false;
        boolean notNull = false;
        int id = -1;
    }

    private StringBuffer buffer;

    private final classDiagram.ClassDiagram classDiagram;

    Association currentAssociation;
    ComponentView currentComponentView;
    Dependency currentDependency;
    Entity currentEntity;
    Rectangle currentGeometry;
    Inheritance currentInheritance;
    LinkedList<Point> currentLine;
    UMLView currentUMLView;
    Key currentKey;

    Operation currentMethod;
    int currentMin, currentMax;

    MultiView currentMultiView;

    Note currentNote;

    Point currentPoint;

    RelationView currentRelationView;

    Role currentRole;

    private boolean inMultiViewBounds;

    boolean inRelationView = false, inComponentView = false,
            inNoteGeometry = false, inNoteRelation = false,
            inLabelAssociation = false;

    private ClassDiagram umlClassDiagram;

    public XMLParser(classDiagram.ClassDiagram classDiagram) {
        super();

        if (classDiagram == null)
            throw new IllegalArgumentException("classDiagram is null");

        this.classDiagram = classDiagram;
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        final String reader = new String(ch, start, length);

        if (buffer != null)
            buffer.append(reader);
    }

    private void createEntity(Entity e)
            throws SyntaxeNameException, SAXNotRecognizedException {
        classDiagram.components.Entity ce;
        e.name = TypeName.verifyAndAskNewName(e.name);
        int entityTypeNumber = 0; //0 = class, 1 = enum, 2 = table, 3 = view

        switch (e.entityType) {
            case CLASS:
                ce = new ClassEntity(e.name, e.visibility, e.id);

                classDiagram.addClassEntity((ClassEntity) ce);
                ((ClassEntity) ce).setAbstract(e.isAbstract);

                break;

            case INTERFACE:

                ce = new InterfaceEntity(e.name, e.visibility, e.id);
                classDiagram.addInterfaceEntity((InterfaceEntity) ce);
                ((InterfaceEntity) ce).setAbstract(true);

                break;

            case ENUM:
                entityTypeNumber  = 1;
                ce = new EnumEntity(e.name, e.id);
                classDiagram.addEnumEntity((EnumEntity) ce);

                break;

            case ASSOCIATION_CLASS:

                final Binary b = (Binary) classDiagram
                        .searchComponentById(e.associationClassID);
                if (b == null) // création d'une classe normale.
                {
                    ce = new ClassEntity(e.name, e.visibility, e.id);
                    classDiagram.addClassEntity((ClassEntity) ce);
                    ((ClassEntity) ce).setAbstract(e.isAbstract);
                    break;
                }

                ce = new AssociationClass(e.name, e.visibility, b, e.id);
                classDiagram.addAssociationClass((AssociationClass) ce);

                break;

            case TABLE:
                entityTypeNumber = 2;
                ce = new RelationalEntity(e.name, e.id);
                classDiagram.addTableEntity((RelationalEntity) ce);
                break;

            case VIEW:
                entityTypeNumber  = 3;
                ce = new RelViewEntity(e.name, e.id, e.procedure);
                classDiagram.addRelViewEntity((RelViewEntity) ce);
                break;
            default:
                throw new SAXNotRecognizedException(
                        e.entityType + ": wrong entity type.");
        }

        switch (entityTypeNumber) {
            case 0:
                SimpleEntity se = (SimpleEntity) ce;
                for (Variable v : e.attribute) {
                    Attribute a = new Attribute(VariableName.verifyAndAskNewName(v.name),
                            v.type);

                    se.addAttribute(a);
                    se.notifyObservers(UpdateMessage.ADD_ATTRIBUTE_NO_EDIT);
                    a.setConstant(v.constant);
                    a.setDefaultValue(v.defaultValue);
                    a.setStatic(v.isStatic);
                    a.setVisibility(v.visibility);
                    a.notifyObservers();
                }

                for (Operation o : e.method) {
                    Method m;
                    if (o.isConstructor)
                        m = new ConstructorMethod(
                                MethodName.verifyAndAskNewName(o.name), o.visibility, se);
                    else
                        m = new Method(
                                MethodName.verifyAndAskNewName(o.name),
                                new Type(TypeName.verifyAndAskNewName(o.returnType)),
                                o.visibility, se);
                    se.addMethod(m);
                    se.notifyObservers(UpdateMessage.ADD_METHOD_NO_EDIT);

                    m.setParametersViewStyle(o.view);
                    m.setStatic(o.isStatic);
                    m.setAbstract(o.isAbstract);

                    for (Variable v : o.variable) {
                        classDiagram.components.Variable va = new classDiagram.components.Variable(
                                VariableName.verifyAndAskNewName(v.name), v.type);
                        m.addParameter(va);
                    }
                    m.notifyObservers();
                }
                break;

            case 1:
                EnumEntity ee = (EnumEntity) ce;
                for (EnumValue v : e.enums) {
                    ee.addEnumValue(v);
                    ee.notifyObservers(UpdateMessage.ADD_ENUM_NO_EDIT);
                }
                break;

            case 2:
                RelationalEntity re = (RelationalEntity) ce;

                for (Variable v : e.attribute) {
                    RelationalAttribute a = new RelationalAttribute(VariableName.verifyAndAskNewName(v.name), v.type, v.id);

                    re.addAttribute(a);
                    re.notifyObservers(UpdateMessage.ADD_ATTRIBUTE_NO_EDIT);
                    a.setDefaultValue(v.defaultValue);
                    a.setUnique(v.unique);
                    a.setNotNull(v.notNull);
                    a.notifyObservers();
                }

                for (Operation o : e.triggers) {
                    Trigger t = new Trigger(TriggerName.verifyAndAskNewName(o.name));
                    re.addTrigger(t);
                    re.notifyObservers(UpdateMessage.ADD_TRIGGER_NO_EDIT);
                    t.setProcedure(o.procedure);
                    t.setTriggerType(o.triggerType);
                    t.setActivationTime(o.activationTime);
                    t.notifyObservers();
                }

                for (Key key : e.keys) {
                    if ("primary".equals(key.keyType)) {
                        classDiagram.components.Key pk = new classDiagram.components.Key(key.name, re);
                        for (int id : key.components) {
                            RelationalAttribute ra = re.getAttributeById(id);
                            if (ra != null)
                                pk.addKeyComponent(ra);
                        }
                        re.setPrimaryKey(pk);
                    } else if ("alternate".equals(key.keyType)) {
                        classDiagram.components.Key ak = new classDiagram.components.Key(key.name, re);
                        for (int id : key.components) {
                            RelationalAttribute ra = re.getAttributeById(id);
                            if (ra != null)
                                ak.addKeyComponent(ra);
                        }
                        re.addAlternateKey(ak);
                    }
                    re.notifyObservers(UpdateMessage.ADD_KEY_NO_EDIT);
                }
                break;
        }
        ce.notifyObservers();
    }

    @Override
    public void endDocument() {

    }

    public void createDiagram()
            throws SyntaxeNameException, SAXNotRecognizedException {

        MultiViewManager.setSelectedGraphicView(0);

        GraphicView rootGraphicView = MultiViewManager.getSelectedGraphicView();
        classDiagram.setName(umlClassDiagram.name);
        classDiagram.setInformation(umlClassDiagram.information);
        DiagramPropreties.setDiagramsInformations(umlClassDiagram.information);
        classDiagram.setViewEntity(umlClassDiagram.defaultViewEntities);
        classDiagram.setDefaultViewMethods(umlClassDiagram.defaultViewMethods);
        classDiagram.setDefaultViewEnum(umlClassDiagram.defaultViewEnum);
        classDiagram.setVisibleType(umlClassDiagram.defaultVisibleTypes);
        classDiagram.notifyObservers();

        // Don't change the order !!
        importClassesAndInterfaces(); // <- need nothing :D

        importAssociations(); // <- need importation classes
        importAssociationClass(); // <- need importation classes and associations
        importAssociations(); // Import associations that cannot be imported first
        // time
        importInheritances(); // <- ...
        importDependency();

        rootGraphicView.setPaintBackgroundLast(true);
        rootGraphicView.goRepaint();

        locateComponentBounds();
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        switch (qName) {
            case "entity":
                currentEntity = null;
                break;
            case "method":
                currentMethod = null;
                break;
            case "key":
                currentKey = null;
                break;
            case "EnumValue":
                currentEntity.enums.add(new EnumValue(buffer.toString()));
                buffer = null;
                break;
            case "associationClassID":
                currentEntity.associationClassID = Integer.parseInt(buffer.toString());
                break;
            case "association":
                currentAssociation = null;
                break;
            case "role":
                currentRole = null;
                break;
            case "multiplicity":
                currentRole.multiplicity = new Multiplicity(currentMin, currentMax);
                break;
            case "min":
                currentMin = Integer.parseInt(buffer.toString());
                buffer = null;
                break;
            case "max":
                currentMax = Integer.parseInt(buffer.toString());
                buffer = null;
                break;
            case "child":
                currentInheritance.child = Integer.parseInt(buffer.toString());
                buffer = null;
                break;
            case "parent":
                currentInheritance.parent = Integer.parseInt(buffer.toString());
                buffer = null;
                break;
            case "source":
                currentDependency.source = Integer.parseInt(buffer.toString());
                buffer = null;
                break;
            case "target":
                currentDependency.target = Integer.parseInt(buffer.toString());
                buffer = null;
                break;
            case "relationView":
                inRelationView = false;
                currentUMLView.relationView.put(
                        currentRelationView.relationId, currentRelationView);
                break;
            case "multiLineView":
                inRelationView = false;
                currentMultiView.multiLineView.add(currentRelationView);
                break;
            case "multiView":
                currentUMLView.multiView.put(
                        currentMultiView.relationId, currentMultiView);
                break;
            case "geometry":
                inNoteGeometry = false;
                currentComponentView.geometry = currentGeometry;
                currentGeometry = null;
                break;
            case "noteGeometry":
                inNoteGeometry = false;
                currentNote.bounds = currentGeometry;
                currentGeometry = null;
                break;
            case "componentView":
                inComponentView = false;
                currentUMLView.componentView.put(
                        currentComponentView.componentId, currentComponentView);
                break;
            case "note":
                currentUMLView.notes.add(currentNote);
                currentNote = null;
                break;
            case "multiViewBounds":
                inMultiViewBounds = false;
                currentMultiView.multiViewBounds = currentGeometry;
                currentGeometry = null;
                break;
            case "line":
                currentRelationView.line = currentLine;
                currentLine = null;
                break;
            case "noteLine":
                inNoteRelation = false;
                currentRelationView.line = currentLine;
                currentNote.line.addLast(currentRelationView);
                currentLine = null;
                currentRelationView = null;
                break;
            case "point":
                currentLine.add(currentPoint);
                currentPoint = null;
                break;
            case "labelAssociation":
                currentRelationView.labelAssociation = currentGeometry;
                currentGeometry = null;
                inLabelAssociation = false;
                break;
            case "roleAssociation":
                currentRelationView.roleAssociations.add(currentGeometry);
                currentGeometry = null;
                inLabelAssociation = false;
                break;
            case "multipliciteAssociation":
                currentRelationView.multiplicityAssociations.add(currentGeometry);
                currentGeometry = null;
                inLabelAssociation = false;
                break;
            case "x":
                final int x = Integer.parseInt(buffer.toString());
                if (inComponentView || inNoteGeometry || inLabelAssociation
                        || inMultiViewBounds)

                    currentGeometry.x = x;

                else if (inRelationView || inNoteRelation)

                    currentPoint.x = x;
                buffer = null;
                break;
            case "y":
                final int y = Integer.parseInt(buffer.toString());
                if (inComponentView || inNoteGeometry || inLabelAssociation
                        || inMultiViewBounds)

                    currentGeometry.y = y;

                else if (inRelationView || inNoteRelation)

                    currentPoint.y = y;
                buffer = null;
                break;
            case "w":
                currentGeometry.width = Integer.parseInt(buffer.toString());
                buffer = null;
                break;
            case "h":
                currentGeometry.height = Integer.parseInt(buffer.toString());
                buffer = null;
                break;
        }
    }

    private void importAssociationClass()
            throws SyntaxeNameException, SAXNotRecognizedException {
        for (final Entity e : umlClassDiagram.diagramElement.entity)
            if (e.entityType == EntityType.ASSOCIATION_CLASS)
                createEntity(e);
    }

    public void importAssociations() {
        final LinkedList<Association> associationsNotAdded = new LinkedList<>();

        for (final Association a : umlClassDiagram.diagramElement.association) {
            classDiagram.relationships.Association ac = null;

            if (a.role.size() < 2)
                throw new IllegalArgumentException("An association must have at least two roles.");

            final classDiagram.components.Entity source = (classDiagram.components.Entity) classDiagram
                    .searchComponentById(a.role.getFirst().componentId);
            final classDiagram.components.Entity target = (classDiagram.components.Entity) classDiagram
                    .searchComponentById(a.role.getLast().componentId);

            if (source == null || target == null) {
                associationsNotAdded.add(a);
                continue;
            }

            switch (a.aggregation) {
                case NONE:
                    ac = new Binary(source, target, a.direction, a.id);
                    classDiagram.addBinary((Binary) ac);

                    break;

                case AGGREGATE:
                    ac = new classDiagram.relationships.Aggregation(source, target,
                            a.direction, a.id);
                    classDiagram
                            .addAggregation((classDiagram.relationships.Aggregation) ac);

                    break;

                case COMPOSE:
                    ac = new Composition(source, target, a.direction, a.id);
                    classDiagram.addComposition((Composition) ac);
                    break;

                case MULTI:
                    final LinkedList<classDiagram.components.ClassEntity> entities = new LinkedList<>();

                    for (final Role role : a.role)

                        entities.add((classDiagram.components.ClassEntity) classDiagram
                                .searchComponentById(role.componentId));

                    ac = new Multi(entities, a.id);
                    classDiagram.addMulti((Multi) ac);
                    break;
                case REL:
                    ac = new RelAssociation(source, target, a.direction, a.id);
                    classDiagram.addRelAssociation((RelAssociation) ac);
                    if (source instanceof RelationalEntity && target instanceof RelationalEntity)
                        ((RelationalEntity) source).addForeignKey(((RelationalEntity) target).getPrimaryKey());
                    break;
            }

            for (int i = 0; i < a.role.size(); i++) {
                ac.getRoles().get(i).setName(a.role.get(i).name);
                ac.getRoles().get(i).setVisibility(a.role.get(i).visibility);
                ac.getRoles().get(i).setMultiplicity(a.role.get(i).multiplicity);

                ac.getRoles().get(i).notifyObservers();
                ac.getRoles().get(i).getMultiplicity().notifyObservers();
            }

            ac.setName(a.name);

            ac.notifyObservers();
        }

        umlClassDiagram.diagramElement.association = associationsNotAdded;
    }

    private void importClassesAndInterfaces()
            throws SyntaxeNameException, SAXNotRecognizedException {
        for (final Entity e : umlClassDiagram.diagramElement.entity)
            if (!(e.entityType == EntityType.ASSOCIATION_CLASS)) createEntity(e);

    }

    public void importDependency() {
        for (final Dependency d : umlClassDiagram.diagramElement.dependency) {
            classDiagram.components.Entity source = (classDiagram.components.Entity) classDiagram
                    .searchComponentById(d.source);
            classDiagram.components.Entity target = (classDiagram.components.Entity) classDiagram
                    .searchComponentById(d.target);
            classDiagram.relationships.Dependency dr = new classDiagram.relationships.Dependency(
                    source, target, d.id);
            classDiagram.addDependency(dr);

            dr.setLabel(d.label);
            dr.notifyObservers();
        }
    }

    // view

    public void importInheritances() {
        for (final Inheritance h : umlClassDiagram.diagramElement.inheritance) {

            if (h.innerClass) {
                final classDiagram.components.Entity child =
                        (classDiagram.components.Entity) classDiagram.searchComponentById(h.child);

                final classDiagram.components.Entity parent =
                        (classDiagram.components.Entity) classDiagram.searchComponentById(h.parent);

                final classDiagram.relationships.InnerClass innerClass =
                        new classDiagram.relationships.InnerClass(child, parent, h.id);

                classDiagram.addInnerClass(innerClass);
                innerClass.notifyObservers();

            } else {
                final classDiagram.components.SimpleEntity child =
                        (classDiagram.components.SimpleEntity) classDiagram.searchComponentById(h.child);

                final classDiagram.components.SimpleEntity parent =
                        (classDiagram.components.SimpleEntity) classDiagram.searchComponentById(h.parent);

                final classDiagram.relationships.Inheritance i =
                        new classDiagram.relationships.Inheritance(child, parent, h.id);
                classDiagram.addInheritance(i);
                i.notifyObservers();
            }
        }
    }

    private void importNotes() {
        for (UMLView umlView : umlClassDiagram.uMLView) {
            GraphicView graphicView = umlView.graphicView;
            for (final Note note : umlView.notes) {
                final TextBoxCommentary noteView = new TextBoxCommentary(graphicView,
                        note.content);

                noteView.setBounds(note.bounds);

                for (final RelationView rv : note.line) {
                    GraphicComponent component = graphicView
                            .searchAssociedComponent(classDiagram
                                    .searchComponentById(rv.relationId));

                    if (rv.relationId == -1)
                        component = graphicView;

                    if (LineCommentary.checkCreate(noteView, component, false)) {
                        final LineCommentary lc = new LineCommentary(
                                graphicView, noteView, component, rv.line.getFirst(),
                                rv.line.getLast(), false);

                        for (int i = 1; i < rv.line.size() - 1; i++) {
                            final RelationGrip rg = new RelationGrip(graphicView, lc);
                            rg.setAnchor(rv.line.get(i));
                            lc.addGrip(rg, i);
                        }

                        lc.getFirstPoint().setAnchor(rv.line.getFirst());
                        lc.getLastPoint().setAnchor(rv.line.getLast());
                        lc.setColor(rv.color);
                        graphicView.addLineView(lc);
                    }
                }

                noteView.setColor(note.color);
                graphicView.addNotes(noteView);
            }
        }
    }

    public void locateComponentBounds() {

        for (UMLView umlView : umlClassDiagram.uMLView) {

            GraphicView graphicView = umlView.graphicView;
            graphicView.setName(umlView.name);

            // Generals bounds
            for (GraphicComponent g : graphicView.getAllComponents()) {
                IDiagramComponent component = g.getAssociedComponent();

                if (component != null) {
                    ComponentView cv = umlView.componentView.get(component.getId());

                    if (cv != null) {
                        g.setBounds(cv.geometry);
                        g.setColor(cv.color);

                        // Gestion des entités
                        if (g instanceof SimpleEntityView) {
                            SimpleEntityView entityView = (SimpleEntityView) g;
                            entityView.setDisplayAttributes(cv.displayAttributes);
                            entityView.setDisplayMethods(cv.displayMethods);
                            entityView.setDisplayDefault(cv.displayDefault);
                        } else if (g instanceof EnumView) {
                            ((EnumView) g).setTypeEnumDisplay(cv.typeEnumDisplay);
                        }
                    } else {
                        if (g instanceof EntityView)
                            g.lightDelete();
                    }
                }
            }

            // Associations
            for (LineView l : graphicView.getLinesView()) {
                IDiagramComponent component = l.getAssociedXmlElement();

                if (component != null) {
                    final RelationView rl = umlView.relationView.get(component.getId());
                    if (rl == null) continue;

                    LinkedList<Point> points = rl.line;

                    for (int i = 1; i < points.size() - 1; i++) {
                        final RelationGrip rg = new RelationGrip(graphicView, l);
                        rg.setAnchor(points.get(i));
                        rg.notifyObservers();
                        l.addGrip(rg, i);
                    }

                    RelationGrip first = l.getFirstPoint(), last = l.getLastPoint();

                    first.setAnchor(points.getFirst());
                    last.setAnchor(points.getLast());

                    first.notifyObservers();
                    last.notifyObservers();

                    l.setColor(rl.color);
                    final LinkedList<TextBox> tb = l.getTextBoxRole();

                    SwingUtilities.invokeLater(() -> {
                        if (tb.size() >= 1) {
                            ((TextBoxLabel) tb.getFirst()).computeDeplacement(new Point(
                                    rl.labelAssociation.x, rl.labelAssociation.y));

                            if (tb.size() >= 3) {
                                ((TextBoxLabel) tb.get(1)).computeDeplacement(new Point(
                                        rl.roleAssociations.get(0).x, rl.roleAssociations
                                        .get(0).y));
                                ((TextBoxLabel) tb.get(2)).computeDeplacement(new Point(
                                        rl.roleAssociations.get(1).x, rl.roleAssociations
                                        .get(1).y));

                                ((TextBoxRole) tb.get(1)).getTextBoxMultiplicity()
                                        .computeDeplacement(
                                                new Point(rl.multiplicityAssociations.get(0).x,
                                                        rl.multiplicityAssociations.get(0).y));
                                ((TextBoxRole) tb.get(2)).getTextBoxMultiplicity()
                                        .computeDeplacement(
                                                new Point(rl.multiplicityAssociations.get(1).x,
                                                        rl.multiplicityAssociations.get(1).y));
                            }
                        }
                    });

                }
            }

            // Multi-association
            for (final graphic.relations.MultiView mv : graphicView.getMultiView()) {
                final IDiagramComponent component = mv.getAssociedXmlElement();

                if (component != null) {
                    final MultiView xmlMV = umlView.multiView.get(component.getId());

                    final LinkedList<MultiLineView> multiLinesView = mv.getMultiLinesView();

                    mv.setBounds(xmlMV.multiViewBounds);

                    for (int j = 0; j < multiLinesView.size(); j++) {
                        final RelationView rl = xmlMV.multiLineView.get(j);
                        final LinkedList<Point> points = rl.line;
                        final MultiLineView mlv = multiLinesView.get(j);

                        for (int i = 1; i < points.size() - 1; i++) {
                            final RelationGrip rg = new RelationGrip(graphicView, mlv);
                            rg.setAnchor(points.get(i));
                            rg.notifyObservers();
                            mlv.addGrip(rg, i);
                        }

                        RelationGrip first = mlv.getFirstPoint(), last = mlv.getLastPoint();

                        first.setAnchor(points.getFirst());
                        last.setAnchor(points.getLast());

                        first.notifyObservers();
                        last.notifyObservers();

                        // Role
                        final LinkedList<TextBox> tb = mlv.getTextBoxRole();

                        SwingUtilities.invokeLater(() -> {
                            if (tb.size() == 1) {
                                ((TextBoxLabel) tb.getFirst()).computeDeplacement(new Point(
                                        rl.roleAssociations.get(0).x, rl.roleAssociations
                                        .get(0).y));

                                ((TextBoxRole) tb.getFirst()).getTextBoxMultiplicity()
                                        .computeDeplacement(
                                                new Point(rl.multiplicityAssociations.get(0).x,
                                                        rl.multiplicityAssociations.get(0).y));
                            }
                        });
                    }

                    mv.setColor(xmlMV.color);
                    mv.setBounds(xmlMV.multiViewBounds);
                }
            }
        }

        importNotes();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        switch (qName) {
            case "classDiagram":
                try {
                    umlClassDiagram = new ClassDiagram();
                } catch (final Exception e) {
                    throw new SAXException(e);
                } break;
            case "diagramElements":
                umlClassDiagram.diagramElement = new DiagramElements();

                umlClassDiagram.name = attributes.getValue("name");
                umlClassDiagram.information = attributes.getValue("informations");

                if (attributes.getValue("defaultViewEntities") != null)
                    umlClassDiagram.defaultViewEntities = ViewEntity.valueOf(
                            attributes.getValue("defaultViewEntities"));

                if (attributes.getValue("defaultViewMethods") != null)
                    umlClassDiagram.defaultViewMethods =
                            ParametersViewStyle.valueOf(attributes.getValue("defaultViewMethods"));

                if (attributes.getValue("defaultViewEnum") != null)
                    umlClassDiagram.defaultViewEnum =
                            Boolean.parseBoolean(attributes.getValue("defaultViewEnum"));

                if (attributes.getValue("defaultVisibleTypes") != null)
                    umlClassDiagram.defaultVisibleTypes =
                            Boolean.parseBoolean(attributes.getValue("defaultVisibleTypes"));

                break;
            case "entity":
                try {
                    currentEntity = new Entity();
                    currentEntity.id = Integer.parseInt(attributes.getValue("id"));
                    currentEntity.name = attributes.getValue("name");

                    String currentAttributeValue = attributes.getValue("entityType");
                    if (currentAttributeValue != null)
                        currentEntity.entityType = EntityType.valueOf(attributes
                                .getValue("entityType"));

                    currentAttributeValue = attributes.getValue("visibility");
                    if (currentAttributeValue != null)
                        currentEntity.visibility = Visibility.valueOf(attributes
                                .getValue("visibility"));

                    currentAttributeValue = attributes.getValue("isAbstract");
                    if (currentAttributeValue != null)
                        currentEntity.isAbstract = Boolean.parseBoolean(attributes
                                .getValue("isAbstract"));

                    currentAttributeValue = attributes.getValue("procedure");
                    if (currentAttributeValue != null)
                        currentEntity.procedure = attributes.getValue("procedure");


                    umlClassDiagram.diagramElement.entity.add(currentEntity);
                } catch (final NumberFormatException e) {
                    throw new SAXException(e);
                } break;
            case "method":
                try {
                    currentMethod = new Operation();
                    currentMethod.name = attributes.getValue("name");
                    currentMethod.returnType = attributes.getValue("returnType");
                    currentMethod.visibility = Visibility.valueOf(attributes
                            .getValue("visibility"));
                    currentMethod.isStatic = Boolean.parseBoolean(attributes
                            .getValue("isStatic"));
                    currentMethod.isAbstract = Boolean.parseBoolean(attributes
                            .getValue("isAbstract"));

                    if (attributes.getValue("view") != null)
                        currentMethod.view = ParametersViewStyle.valueOf(attributes
                                .getValue("view"));

                    if (attributes.getValue("is-constructor") != null)
                        currentMethod.isConstructor = Boolean.parseBoolean(attributes
                                .getValue("is-constructor"));

                    currentEntity.method.add(currentMethod);
                } catch (final Exception e) {
                    throw new SAXException(e);
                } break;
            case "variable":
                try {
                    final Variable variable = new Variable();
                    variable.name = attributes.getValue("name");
                    variable.type = new Type(TypeName.verifyAndAskNewName(attributes
                            .getValue("type")));
                    variable.constant = Boolean.parseBoolean(attributes.getValue("const"));

                    currentMethod.variable.add(variable);
                } catch (final SyntaxeNameException e) {
                    throw new SAXException(e);
                } break;
            case "attribute":
                try {
                    final Variable variable = new Variable();
                    variable.name = attributes.getValue("name");
                    variable.type = new Type(TypeName.verifyAndAskNewName(attributes
                            .getValue("type")));
                    variable.constant = Boolean.parseBoolean(attributes.getValue("const"));
                    variable.visibility = Visibility.valueOf(attributes
                            .getValue("visibility"));
                    variable.defaultValue = attributes.getValue("defaultValue");
                    // variable.collection =
                    // Integer.parseInt(attributes.getValue("collection"));
                    variable.isStatic = Boolean.parseBoolean(attributes
                            .getValue("isStatic"));

                    currentEntity.attribute.add(variable);
                } catch (final SyntaxeNameException e) {
                    throw new SAXException(e);
                } break;
            case "relationalAttribute":
                try {
                    final Variable variable = new Variable();
                    variable.name = attributes.getValue("name");
                    variable.type = new Type(TypeName.verifyAndAskNewName(attributes
                            .getValue("type")));
                    variable.defaultValue = attributes.getValue("defaultValue");
                    variable.unique = Boolean.parseBoolean(attributes.getValue("unique"));
                    variable.notNull = Boolean.parseBoolean(attributes.getValue("notNull"));
                    variable.id = Integer.parseInt(attributes.getValue("id"));

                    currentEntity.attribute.add(variable);
                } catch (SyntaxeNameException e) {
                    throw new SAXException(e);
                } break;
            case "key":
                currentKey = new Key();
                currentKey.keyType = attributes.getValue("keyType");
                currentKey.id = Integer.parseInt(attributes.getValue("id"));
                currentKey.name = attributes.getValue("name");
                currentEntity.keys.add(currentKey);
                break;
            case "raID":
                currentKey.components.add(Integer.parseInt(attributes.getValue("id")));
                break;
            case "trigger":
                final Operation trigger = new Operation();
                trigger.name = attributes.getValue("name");
                trigger.procedure = attributes.getValue("procedure");
                trigger.activationTime = ActivationTime.getFromName(attributes.getValue("activationTime"));
                trigger.triggerType = TriggerType.getFromName(attributes.getValue("triggerType"));
                currentEntity.triggers.add(trigger);
                break;
            case "association":
                try {
                    currentAssociation = new Association();
                    currentAssociation.id = Integer.parseInt(attributes.getValue("id"));
                    currentAssociation.name = attributes.getValue("name");
                    try {
                        currentAssociation.direction = NavigateDirection.valueOf(attributes
                                .getValue("direction"));
                    } catch (IllegalArgumentException e) {
                        // For older version of sly file. Convert boolean value to
                        // navigability.
                        if (Boolean.parseBoolean(attributes.getValue("direction")))
                            currentAssociation.direction = NavigateDirection.FIRST_TO_SECOND;
                        else
                            currentAssociation.direction = NavigateDirection.BIDIRECTIONAL;

                    }
                    currentAssociation.aggregation = Aggregation.valueOf(attributes
                            .getValue("aggregation"));

                    umlClassDiagram.diagramElement.association.add(currentAssociation);
                } catch (final NumberFormatException e) {
                    throw new SAXException(e);
                } break;
            case "role":
                try {
                    currentRole = new Role();
                    currentRole.name = attributes.getValue("name");
                    currentRole.componentId = Integer.parseInt(attributes
                            .getValue("componentId"));
                    currentRole.visibility = Visibility.valueOf(attributes
                            .getValue("visibility"));

                    currentAssociation.role.add(currentRole);
                } catch (final NumberFormatException e) {
                    throw new SAXException(e);
                } break;
            case "multiplicity":
                break;
            case "inheritance":
                try {
                    currentInheritance = new Inheritance();
                    currentInheritance.id = Integer.parseInt(attributes.getValue("id"));
                    currentInheritance.innerClass = Boolean.parseBoolean(attributes
                            .getValue("innerClass"));

                    buffer = new StringBuffer();

                    umlClassDiagram.diagramElement.inheritance.add(currentInheritance);
                } catch (final NumberFormatException e) {
                    throw new SAXException(e);
                } break;
            case "dependency":
                try {
                    currentDependency = new Dependency();
                    currentDependency.id = Integer.parseInt(attributes.getValue("id"));
                    currentDependency.label = attributes.getValue("label");

                    buffer = new StringBuffer();

                    umlClassDiagram.diagramElement.dependency.add(currentDependency);
                } catch (final NumberFormatException e) {
                    throw new SAXException(e);
                } break;
            case "umlView":
                try {
                    UMLView newUMLView;
                    boolean open = true;
                    boolean rel = false;

                    if (attributes.getValue("open") != null)
                        open = Boolean.parseBoolean(attributes.getValue("open"));

                    if (attributes.getValue("rel") != null)
                        rel = Boolean.parseBoolean(attributes.getValue("rel"));

                    if (umlClassDiagram.uMLView.size() == 0) // root graphic view
                        newUMLView = new UMLView();
                    else // new view
                        newUMLView = new UMLView(attributes.getValue("name"), open, rel);

                    currentUMLView = newUMLView;
                    umlClassDiagram.uMLView.add(newUMLView);
                } catch (final Exception e) {
                    throw new SAXException(e);
                } break;
            case "componentView":
                try {
                    inComponentView = true;
                    currentComponentView = new ComponentView();
                    currentComponentView.componentId = Integer.parseInt(attributes
                            .getValue("componentID"));
                    currentComponentView.color = Integer.parseInt(attributes
                            .getValue("color"));

                    if (attributes.getValue("displayAttributes") != null)
                        currentComponentView.displayAttributes = Boolean
                                .parseBoolean(attributes.getValue("displayAttributes"));

                    if (attributes.getValue("displayMethods") != null)
                        currentComponentView.displayMethods = Boolean.parseBoolean(attributes
                                .getValue("displayMethods"));

                    if (attributes.getValue("displayDefault") != null)
                        currentComponentView.displayDefault = Boolean.parseBoolean(attributes
                                .getValue("displayDefault"));

                    if (attributes.getValue("enumValuesVisible") != null)
                        currentComponentView.typeEnumDisplay = TypeEnumDisplay
                                .valueOf(attributes.getValue("enumValuesVisible"));

                } catch (final NumberFormatException e) {
                    throw new SAXException(e);
                } break;
            case "geometry":
            case "noteGeometry":
                inNoteGeometry = true;
                currentGeometry = new Rectangle();
                break;
            case "relationView":
            case "multiLineView":
                try {
                    inRelationView = true;
                    currentRelationView = new RelationView();
                    currentRelationView.relationId = Integer.parseInt(attributes
                            .getValue("relationId"));
                    currentRelationView.color = Integer.parseInt(attributes
                            .getValue("color"));
                } catch (final NumberFormatException e) {
                    throw new SAXException(e);
                } break;
            case "multiView":
                try {
                    currentMultiView = new MultiView();
                    currentMultiView.relationId = Integer.parseInt(attributes
                            .getValue("relationId"));
                    currentMultiView.color = Integer.parseInt(attributes.getValue("color"));
                } catch (final NumberFormatException e) {
                    throw new SAXException(e);
                } break;
            case "multiViewBounds":
                inMultiViewBounds = true;
                currentGeometry = new Rectangle();
                break;
            case "line":
                currentLine = new LinkedList<>();
                break;
            case "noteLine":
                inNoteRelation = true;
                currentLine = new LinkedList<>();
                currentRelationView = new RelationView();
                currentRelationView.relationId = Integer.parseInt(attributes
                        .getValue("relationId"));
                currentRelationView.color = Integer
                        .parseInt(attributes.getValue("color"));
                break;
            case "point":
                currentPoint = new Point();
                break;
            case "labelAssociation":
            case "roleAssociation":
            case "multipliciteAssociation":
                currentGeometry = new Rectangle();
                inLabelAssociation = true;
                break;
            case "note":
                try {
                    currentNote = new Note();
                    currentNote.content = attributes.getValue("content");
                    currentNote.color = Integer.parseInt(attributes.getValue("color"));
                } catch (final NumberFormatException e) {
                    throw new SAXException(e);
                } break;
            case "EnumValue":
            case "min":
            case "max":
            case "associationClassID":
            case "child":
            case "parent":
            case "source":
            case "target":
            case "x":
            case "y":
            case "w":
            case "h":
                buffer = new StringBuffer();
                break;
        }
    }
}
