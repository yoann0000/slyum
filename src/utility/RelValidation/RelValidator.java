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
    private final LinkedList<IDiagramComponent> components;
    private int errors;
    private String errorString;
    private boolean hasCycle;

    private static final RelValidator instance = new RelValidator();

    public static RelValidator getInstance() {
        return instance;
    }

    /**
     * Constructor
     */
    private RelValidator() {
        components = new LinkedList<>();
        errors = 0;
        errorString = "";
        hasCycle = false;
    }

    /**
     * Validate the relational diagram
     */
    public void validate() {
        errors = 0;
        errorString = "";
        hasCycle = false;
        if (components.isEmpty()) {
            errorString = "Diagram is empty";
            errors++;
            return;
        }
        LinkedList<RelationalEntity> re = new LinkedList<>();
        LinkedList<RelViewEntity> views = new LinkedList<>();
        LinkedList<RelAssociation> ra = new LinkedList<>();

        StringBuilder sb = new StringBuilder();
        int previousErrors;

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

        LinkedList<String> entityNames = new LinkedList<>();

        for (RelationalEntity entity : re) {
            previousErrors = errors;
            sb.append("Table ").append(entity.getName()).append(" :\n");

            if (entityNames.contains(entity.getName())){
                sb.append("There is already an entity called ").append(entity.getName()).append("\n");
                errors++;
            }
            entityNames.add(entity.getName());

            validateEntity(entity, sb);
            if (errors == previousErrors){
                sb.append("None\n");
            }
            sb.append("\n");
        }

        for (RelViewEntity entity : views) {
            previousErrors = errors;
            sb.append("View ").append(entity.getName()).append(" :\n");
            if (entityNames.contains(entity.getName())){
                sb.append("There is already an entity called ").append(entity.getName()).append("\n");
                errors++;
            }
            entityNames.add(entity.getName());

            validateView(entity, sb);
            if (errors == previousErrors){
                sb.append("None\n");
            }
            sb.append("\n");
        }


        checkNoCycles(re, ra, sb);
        errorString = sb.toString();
    }

    /**
     * Validates a table
     * @param entity the table to validate
     * @param sb string builder
     */
    private void validateEntity(RelationalEntity entity, StringBuilder sb) {
        int previousErrors;
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

        LinkedList<String> uniqueNames = new LinkedList<>();
        for(RelationalAttribute attribute : entity.getAttributes()) {
            if (uniqueNames.contains(attribute.getName())){
                sb.append("Table contains multiple attribute called ").append(attribute.getName()).append("\n");
                errors++;
            }
            uniqueNames.add(attribute.getName());
        }

        uniqueNames = new LinkedList<>();
        for (Trigger t : entity.getTriggers()) {
            previousErrors = errors;
            sb.append("Trigger ").append(t.getName()).append(":\n");
            if (uniqueNames.contains(t.getName())) {
                sb.append("Table already contains a trigger named ").append(t.getName()).append("\n");
            }
            uniqueNames.add(t.getName());

            if (t.getProcedure() == null || t.getProcedure().isEmpty()) {
                sb.append("has no procedure\n");
                errors++;
            }

            if (errors == previousErrors){
                sb.append("None\n");
            }
        }

        for (Key ak : entity.getAlternateKeys()) {
            validateKey(ak, sb, "Alternate ");
        }
    }

    /**
     * Validate a key
     * @param key the key to validate
     * @param sb string builder
     * @param type key type (primary, alternate, foreign)
     */
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
                if (ra.isUnique())
                    return;
            }
            sb.append(type).append(" key does not uniquely identify entity (no UNIQUE attribute)\n");
            errors++;
        }
    }

    /**
     * Validate a relational view
     * @param entity view to validate
     * @param sb string builder
     */
    private void validateView(RelViewEntity entity, StringBuilder sb) {
        if(entity.getProcedure() == null || entity.getProcedure().isEmpty()) {
            sb.append("view has no procedure.\n");
            errors++;
        }
    }

    /**
     * Check that a diagram has no cycles
     * @param re list of all tables in the diagram
     * @param ra list of all relations in the diagram
     * @param sb string builder
     */
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
            sb.append("Table relations are cyclic. Check semantic.\n");
            hasCycle = true;
        }
    }

    /**
     * Get the number of errors
     * @return the number of errors
     */
    public int getErrors() {
        return errors;
    }

    /**
     * Get the error string
     * @return the error string
     */
    public String getErrorString() {
        return errorString;
    }

    /**
     * Get the hasCycle value
     * @return true is the graph has a cycle
     */
    public boolean isCyclic() {
        return hasCycle;
    }

    /**
     * Set the components to validate from a list of graphical components
     * @param components the list of graphical components to validate
     */
    public void setComponents(LinkedList<GraphicComponent> components) {
        this.components.clear();
        components.forEach(graphicComponent -> this.components.add(graphicComponent.getAssociedComponent()));
    }
}
