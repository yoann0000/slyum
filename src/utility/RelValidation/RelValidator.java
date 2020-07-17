package utility.RelValidation;

import classDiagram.ClassDiagram;
import classDiagram.IDiagramComponent;
import classDiagram.components.*;
import classDiagram.relationships.RelAssociation;

import java.util.*;

public class RelValidator {
    private ClassDiagram classDiagram;
    private int errors;
    private String errorString;

    private static RelValidator instance;

    public static RelValidator getInstance() {
        return instance == null ? new RelValidator() : instance;
    }

    private RelValidator() {
        errors = 0;
        errorString = "";
    }

    public void validate() {
        if (classDiagram == null) return;
        errors = 0;
        errorString = "";
        LinkedList<IDiagramComponent> components = classDiagram.getComponents();
        LinkedList<RelationalEntity> re = new LinkedList<>();
        LinkedList<View> views = new LinkedList<>();
        LinkedList<RelAssociation> ra = new LinkedList<>();
        for (IDiagramComponent component : components) {
            if (component instanceof RelationalEntity) {
                re.add((RelationalEntity) component);
            } else if (component instanceof View){
                views.add((View) component);
            } else if (component instanceof RelAssociation) {
                ra.add((RelAssociation) component);
            } else {
                //TODO verify no non relational components are present
                System.out.println("TODO");
            }
        }

        StringBuilder sb = new StringBuilder();
        int previousErrors = 0;
        for (RelationalEntity entity : re) {
            previousErrors = errors;
            sb.append("Table ").append(entity.getName()).append(" :\n");
            validateEntity(entity, sb);
            if (errors == previousErrors){
                sb.append("None\n");
            }
            sb.append("\n");
        }

        checkNoCycles(re, ra, sb);
        errorString = sb.toString();
    }

    private void validateEntity(RelationalEntity entity, StringBuilder sb) {
        if (entity.getPrimaryKey() == null) {
            sb.append("Table has no primary key.\n");
            errors++;
        } else {
            validateKey(entity.getPrimaryKey(), sb, "Primary");
        }

        if (entity.getAttributes() == null || entity.getAttributes().isEmpty()) {
            sb.append("Table has no attributes\n");
            errors++;
        }

        for (Key ak : entity.getAlternateKeys()) {
            validateKey(ak, sb, "Alternate ");
        }
    }

    private void validateKey(Key key, StringBuilder sb, String type){
        if (key.getName().equals("")){
            sb.append(type).append(" key has no name.\n");
            errors++;
        }

        if (key.getKeyComponents() == null || key.getKeyComponents().isEmpty()) {
            sb.append(type).append(" key has no attributes\n");
            errors++;
        } else {
            for (RelationalAttribute ra : key.getKeyComponents()) {
                if (ra.isUnique() && ra.isNotNull())
                    return;
            }
            sb.append(type).append(" key does not uniquely identify entity (no UNIQUE NOT NULL attribute)\n");
            errors++;
        }
    }

    private void checkNoCycles(LinkedList<RelationalEntity> re, LinkedList<RelAssociation> ra, StringBuilder sb) {
        Map<Entity, Integer> vertexIds = new HashMap<>();
        for (int i = 0; i < re.size(); i++) {
            vertexIds.put(re.get(i), i);
        }

        Graph graph = new Graph(re.size());
        for (RelAssociation relA : ra) {
            if (relA.getSource() instanceof RelationalEntity && relA.getTarget() instanceof RelationalEntity) {
                graph.addEdge(vertexIds.get(relA.getSource()), vertexIds.get(relA.getTarget()));
            }
        }

        if(graph.isCyclic()) {
            sb.append("Table relations are cyclic\n");
            errors++;
        }
    }

    public int getErrors() {
        return errors;
    }

    public String getErrorString() {
        return errorString;
    }

    public void setClassDiagram(ClassDiagram classDiagram) {
        this.classDiagram = classDiagram;
    }
}
