package utility.SQLconverter;

import classDiagram.components.Key;
import classDiagram.components.RelViewEntity;
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
                return convertToPostgres();
            default:
                return "unimplemented";
        }
    }

    private String convertToMYSQL() {
        LinkedList<GraphicComponent> components = relGraphicView.getAllDiagramComponents();
        StringBuilder sb = new StringBuilder("CREATE DATABASE ");
        sb.append(relGraphicView.getName()).append(";\n");
        sb.append("\nUSE ").append(relGraphicView.getName()).append("\n\n");
        for (GraphicComponent gc : components) {
            if (gc instanceof RelationalEntityView) {
                sb.append(convertTableMYSQL((RelationalEntity)gc.getAssociedComponent()));
                sb.append("\n\n");
            } else if (gc instanceof RelViewEntityView) {
                sb.append(convertView((RelViewEntity)gc.getAssociedComponent()));
                sb.append("\n\n");
            }
        }
        return sb.toString();
    }

    private String convertTableMYSQL(RelationalEntity entity) {
        StringBuilder sb = new StringBuilder("DROP TABLE IF EXISTS ");
        sb.append(entity.getName()).append(";\n\n");
        sb.append("CREATE TABLE ").append(entity.getName()).append(" (\n");

        //attributes
        for (RelationalAttribute ra : entity.getAttributes()) {
            sb.append("  ").append(convertAttribute(ra)).append(",\n");
        }

        //add fk attributes
        entity.getForeignKeys().forEach(fk -> fk.getKeyComponents().forEach(
                ra -> sb.append("  ").append(fk.getTable().getName()).append("_").append(convertAttribute(ra)).append(",\n")
        ));

        //primary key
        sb.append("  PRIMARY KEY(");
        for (int i = 0; i < entity.getPrimaryKey().getKeyComponents().size(); i++) {
            RelationalAttribute ra = entity.getPrimaryKey().getKeyComponents().get(i);
            sb.append(ra.getName());
            if (i+1 < entity.getPrimaryKey().getKeyComponents().size()) {
                sb.append(",");
            }
        }
        sb.append(")");

        //alternate keys
        for (Key ak : entity.getAlternateKeys()) {
            sb.append(",\n  KEY ").append(ak.getName());
            keyComponents(sb, ak, null);
        }

        //foreign keys
        for (Key fk : entity.getForeignKeys()) {
            sb.append(",\n  FOREIGN KEY ");
            keyComponents(sb, fk, fk.getTable().getName());
            sb.append(" REFERENCES ");
            keyComponents(sb, fk, null);
        }

        sb.append("\n) ENGINE=InnoDB;");
        return sb.toString();
    }

    private void keyComponents(StringBuilder sb, Key key, String prefix) {
        sb.append("(");
        for (int i = 0; i < key.getKeyComponents().size(); i++) {
            RelationalAttribute ra = key.getKeyComponents().get(i);
            if (prefix != null)
                sb.append(key.getTable().getName()).append("_");
            sb.append(ra.getName());
            if (i+1 < key.getKeyComponents().size()) {
                sb.append(",");
            }
        }
        sb.append(")");
    }

    private String convertAttribute(RelationalAttribute ra) {
        StringBuilder sb = new StringBuilder();
        sb.append(ra.getName()).append(" ").append(ra.getType());
        if (ra.isUnique())
            sb.append(" UNIQUE");
        if (ra.isNotNull())
            sb.append(" NOT NULL");
        return sb.toString();
    }

    private String convertView(RelViewEntity rv) {
        return "CREATE VIEW " + rv.getName() + " AS\n" + rv.getProcedure() + ";";
    }

    private String convertToPostgres() {
        LinkedList<GraphicComponent> components = relGraphicView.getAllDiagramComponents();
        StringBuilder sb = new StringBuilder("CREATE DATABASE ");
        sb.append(relGraphicView.getName()).append(";\n\n");

        for (GraphicComponent gc : components) {
            if (gc instanceof RelationalEntityView) {
                convertTablePostgres((RelationalEntity)gc.getAssociedComponent(), sb);
                sb.append("\n\n");
            } else if (gc instanceof RelViewEntityView) {
                convertViewPostgres((RelViewEntity)gc.getAssociedComponent(), sb);
                sb.append("\n\n");
            }
        }
        return sb.toString();
    }

    private void convertTablePostgres(RelationalEntity re, StringBuilder sb) {
        sb.append("DROP TABLE IF EXISTS ").append(re.getName()).append(";\n\n");
        sb.append("CREATE TABLE ").append(re.getName()).append(" (");

        //attributes
        for (RelationalAttribute ra : re.getAttributes()) {
            sb.append("\n  ").append(convertAttribute(ra)).append(",");
        }

        //fk attributes
        re.getForeignKeys().forEach(fk -> fk.getKeyComponents().forEach(
                ra -> sb.append("\n  ").append(fk.getName()).append("_").append(convertAttribute(ra)).append(",")
        ));

        //primary key
        sb.append("\n  CONSTRAINT ").append(re.getPrimaryKey().getName()).append(" PRIMARY KEY ");
        keyComponents(sb, re.getPrimaryKey(), null);

        //alternate keys
        for (Key ak : re.getAlternateKeys()) {
            sb.append(",\n  CONSTRAINT ").append(ak.getName()).append(" UNIQUE ");
            keyComponents(sb, ak, null);
        }

        //foreign keys
        for (Key fk : re.getForeignKeys()) {
            sb.append(",\n  CONSTRAINT ").append(fk.getName());
            sb.append("\n    FOREIGN KEY ");
            keyComponents(sb, fk, fk.getTable().getName());
            sb.append("\n      REFERENCES ").append(fk.getTable().getName()).append(" ");
            keyComponents(sb, fk, null);
        }

        sb.append("\n);");
    }

    private void convertViewPostgres(RelViewEntity rv, StringBuilder sb) {
        sb.append("CREATE VIEW ").append(rv.getName()).append(" AS\n");
        String[] lines = rv.getProcedure().split("\n");
        for (int i = 0; i < lines.length; i++) {
            sb.append("  ").append(lines[i]).append("\n");
            if (i + 1 == lines.length) {
                sb.append(";");
            }
        }
    }
}
