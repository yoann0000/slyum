package utility.SQLconverter;

import classDiagram.components.Key;
import classDiagram.components.RelationalAttribute;
import classDiagram.components.RelationalEntity;
import graphic.GraphicComponent;
import graphic.GraphicView;
import graphic.entity.RelViewEntityView;
import graphic.entity.RelationalEntityView;

import java.util.LinkedList;

public class SQLconverter {

    private static final SQLconverter instance = new SQLconverter();

    public static SQLconverter getInstance() {
        return instance;
    }

    private GraphicView relGraphicView;

    private SQLconverter(){

    }

    public void setRelGraphicView(GraphicView relGraphicView) {
        this.relGraphicView = relGraphicView;
    }

    public String convertToSQL(String sgbd) {
        if(relGraphicView == null)
            throw new RuntimeException("no graphic view to convert");
        switch (sgbd) {
            case "MYSQL":
                return convertToMYSQL();
            case "POSTGRES":
                return "unimplemented";
            default:
                return "unimplemented";
        }
    }

    private String convertToMYSQL() {
        LinkedList<GraphicComponent> components = relGraphicView.getAllDiagramComponents();
        StringBuilder sb = new StringBuilder("CREATE DATABASE `");
        sb.append(relGraphicView.getName()).append("`;\n");
        sb.append("\nUSE `").append(relGraphicView.getName()).append("`\n\n");
        for (GraphicComponent component : components) {
            if (component instanceof RelationalEntityView) {
                sb.append(convertTable((RelationalEntity)component.getAssociedComponent()));
                sb.append("\n\n");
            } else if (component instanceof RelViewEntityView) {

            }
        }
        return sb.toString();
    }

    private String convertTable(RelationalEntity entity) {
        StringBuilder sb = new StringBuilder("DROP TABLE IF EXISTS `");
        sb.append(entity.getName()).append("`;\n\n");
        sb.append("CREATE TABLE `").append(entity.getName()).append("` (\n");

        //attributes
        for (RelationalAttribute ra : entity.getAttributes()) {
            sb.append("  ").append(convertAttribute(ra)).append(",\n");
        }

        //add fk attributes
        entity.getForeignKeys().forEach(fk -> fk.getKeyComponents().forEach(
                ra -> sb.append("  ").append(convertAttribute(ra)).append(",\n")
        ));

        //primary key
        sb.append("  PRIMARY KEY(");
        for (int i = 0; i < entity.getPrimaryKey().getKeyComponents().size(); i++) {
            RelationalAttribute ra = entity.getPrimaryKey().getKeyComponents().get(i);
            sb.append("`").append(ra.getName()).append("`");
            if (i+1 < entity.getPrimaryKey().getKeyComponents().size()) {
                sb.append(",");
            }
        }
        sb.append(")");

        //alternate keys
        for (Key ak : entity.getAlternateKeys()) {
            sb.append(",\n  KEY `").append(ak.getName()).append("` ");
            keyComponents(sb, ak);
        }

        //foreign keys
        for (Key fk : entity.getForeignKeys()) {
            sb.append(",\n  FOREIGN KEY ");
            keyComponents(sb, fk);
            sb.append(" REFERENCES ");
            keyComponents(sb, fk);
        }

        sb.append("\n) ENGINE=InnoDB;");
        return sb.toString();
    }

    private void keyComponents(StringBuilder sb, Key key) {
        sb.append("(");
        for (int i = 0; i < key.getKeyComponents().size(); i++) {
            RelationalAttribute ra = key.getKeyComponents().get(i);
            sb.append("`").append(ra.getName()).append("`");
            if (i+1 < key.getKeyComponents().size()) {
                sb.append(",");
            }
        }
        sb.append(")");
    }

    private String convertAttribute(RelationalAttribute ra) {
        StringBuilder sb = new StringBuilder("`");
        sb.append(ra.getName()).append("` ").append(ra.getType());
        if (ra.isUnique())
            sb.append(" UNIQUE");
        if (ra.isNotNull())
            sb.append(" NOT NULL");
        return sb.toString();
    }
}
