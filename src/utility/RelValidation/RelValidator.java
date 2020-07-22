package utility.RelValidation;

import classDiagram.IDiagramComponent;
import classDiagram.components.*;
import classDiagram.relationships.Association;
import classDiagram.relationships.RelAssociation;
import graphic.GraphicComponent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class RelValidator {
    private LinkedList<IDiagramComponent> components;
    private int errors;
    private String errorString;

    private static final RelValidator instance = new RelValidator();

    public static RelValidator getInstance() {
        return instance;
    }

    private RelValidator() {
        components = new LinkedList<>();
        errors = 0;
        errorString = "";
    }

    public void validate() {
        if (components == null) return;
        errors = 0;
        errorString = "";
        LinkedList<RelationalEntity> re = new LinkedList<>();
        LinkedList<RelViewEntity> views = new LinkedList<>();
        LinkedList<RelAssociation> ra = new LinkedList<>();

        StringBuilder sb = new StringBuilder();
        int previousErrors = 0;

        for (IDiagramComponent component : components) {
            if (component instanceof RelationalEntity) {
                re.add((RelationalEntity) component);
            } else if (component instanceof RelViewEntity){
                views.add((RelViewEntity) component);
            } else if (component instanceof RelAssociation) {
                ra.add((RelAssociation) component);
            } else if (component instanceof ClassEntity) {
                sb.append("Class ").append(((ClassEntity) component).getName())
                        .append(" should not be in a relational diagram\n");
                errors++;
            } else if (component instanceof EnumEntity) {
                sb.append("Enum ").append(((EnumEntity) component).getName())
                        .append(" should not be in a relational diagram\n");
                errors++;
            } else if (component instanceof Association) {
                sb.append("Association ").append(((Association) component).getName())
                        .append(" should not be in a relational diagram\n");
                errors++;
            }
        }

        LinkedList<String> reNames = new LinkedList<>();

        for (RelationalEntity entity : re) {
            previousErrors = errors;
            sb.append("Table ").append(entity.getName()).append(" :\n");

            if (reNames.contains(entity.getName())){
                sb.append("There is already a table called ").append(entity.getName()).append("\n");
                errors++;
            }
            reNames.add(entity.getName());

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

    public void setComponents(LinkedList<GraphicComponent> components) {
        this.components.clear();
        components.forEach(graphicComponent -> this.components.add(graphicComponent.getAssociedComponent()));
    }
}
