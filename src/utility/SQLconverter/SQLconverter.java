package utility.SQLconverter;

import classDiagram.components.*;
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

    /**
     * Sets the graphic view to convert
     * @param relGraphicView
     */
    public void setRelGraphicView(GraphicView relGraphicView) {
        this.relGraphicView = relGraphicView;
    }

    /**
     * Converts the graphic view to an sql script for a given database
     * @param sgbd the given database
     * @return the script as a string
     */
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

    /**
     * Converts the graphic view to a mysql script
     * @return the script as a string
     */
    private String convertToMYSQL() {
        LinkedList<GraphicComponent> components = relGraphicView.getAllDiagramComponents();
        StringBuilder sb = new StringBuilder("CREATE DATABASE ");
        sb.append(relGraphicView.getName()).append(";\n");
        sb.append("\nUSE ").append(relGraphicView.getName()).append("\n\n");
        for (GraphicComponent gc : components) {
            if (gc instanceof RelationalEntityView) {
                RelationalEntity re = ((RelationalEntity)gc.getAssociedComponent());
                sb.append(convertTableMYSQL(re));
                sb.append("\n\n");
                re.getTriggers().forEach(t -> sb.append(convertTriggerMysql(re.getName(), t)).append("\n\n"));
            } else if (gc instanceof RelViewEntityView) {
                sb.append(convertViewMysql((RelViewEntity)gc.getAssociedComponent()));
                sb.append("\n\n");
            }
        }
        return sb.toString();
    }

    /**
     * Converts a table to a mysql table
     * @param entity the table to convert
     * @return the table as a string
     */
    private String convertTableMYSQL(RelationalEntity entity) {
        StringBuilder sb = new StringBuilder("DROP TABLE IF EXISTS ");
        sb.append(entity.getName()).append(";\n\n");
        sb.append("CREATE TABLE ").append(entity.getName()).append(" (\n");

        //attributes
        for (RelationalAttribute ra : entity.getAttributes()) {
            sb.append("  ").append(convertAttribute(ra, false)).append(",\n");
        }

        //add fk attributes
        entity.getForeignKeys().forEach(fk -> fk.getKeyComponents().forEach(
                ra -> sb.append("  ").append(fk.getTable().getName()).append("_").append(convertAttribute(ra, true)).append(",\n")
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

    /**
     * Converts a key
     * @param sb the string builder building the script
     * @param key the key
     * @param prefix a prefix to the key name
     */
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

    /**
     * converts an attribute
     * @param ra the attribute to convert
     * @param fkAttribute is the attribute  from a foreign key
     * @return the converted attribute
     */
    private String convertAttribute(RelationalAttribute ra, boolean fkAttribute) {
        StringBuilder sb = new StringBuilder();
        sb.append(ra.getName()).append(" ").append(ra.getType());
        if (!fkAttribute) {
            if (ra.isUnique())
                sb.append(" UNIQUE");
            if (ra.isNotNull())
                sb.append(" NOT NULL");
        }
        return sb.toString();
    }

    /**
     * Converts a trigger to mysql trigger
     * @param tableName the name of the table associated with the trigger
     * @param trigger the trigger to convert
     * @return the converted trigger as a string
     */
    private String convertTriggerMysql(String tableName, Trigger trigger) {
        return "CREATE TRIGGER " + tableName + "\n" +
                "    " + trigger.getActivationTime().getName() + " ON " + tableName + "\n" +
                "    " + trigger.getTriggerType() + "\n" +
                trigger.getProcedure() + ";";
    }

    /**
     * Converts a view
     * @param rv the view to convert
     * @return the converted view
     */
    private String convertViewMysql(RelViewEntity rv) {
        return "CREATE VIEW " + rv.getName() + " AS\n" + rv.getProcedure() + ";";
    }

    /**
     * Converts the graphic view to a postgres script
     * @return the script as a string
     */
    private String convertToPostgres() {
        LinkedList<GraphicComponent> components = relGraphicView.getAllDiagramComponents();
        StringBuilder sb = new StringBuilder("CREATE DATABASE ");
        sb.append(relGraphicView.getName()).append(";\n\n");

        for (GraphicComponent gc : components) {
            if (gc instanceof RelationalEntityView) {
                RelationalEntity re = (RelationalEntity)gc.getAssociedComponent();
                convertTablePostgres(re, sb);
                sb.append("\n\n");
                re.getTriggers().forEach(t -> sb.append(convertTriggerPostgres(re.getName(), t)).append("\n\n"));

            } else if (gc instanceof RelViewEntityView) {
                convertViewPostgres((RelViewEntity)gc.getAssociedComponent(), sb);
                sb.append("\n\n");
            }
        }
        return sb.toString();
    }

    /**
     * Convert a table to a postgres table
     * @param re the table to convert
     * @param sb the string builder building the script
     */
    private void convertTablePostgres(RelationalEntity re, StringBuilder sb) {
        sb.append("DROP TABLE IF EXISTS ").append(re.getName()).append(";\n\n");
        sb.append("CREATE TABLE ").append(re.getName()).append(" (");

        //attributes
        for (RelationalAttribute ra : re.getAttributes()) {
            sb.append("\n  ").append(convertAttribute(ra, false)).append(",");
        }

        //fk attributes
        re.getForeignKeys().forEach(fk -> fk.getKeyComponents().forEach(
                ra -> sb.append("\n  ").append(fk.getTable().getName()).append("_").append(convertAttribute(ra, true)).append(",")
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

    /**
     * convert trigger to postgres trigger
     * @param tableName the name of the table associated to the trigger
     * @param t the trigger to convert
     * @return the converted trigger as a string
     */
    private String convertTriggerPostgres(String tableName, Trigger t) {
        StringBuilder sb = new StringBuilder("CREATE OR REPLACE FUNCTION ");
        sb.append(t.getName()).append("_function()\n");
        sb.append("  RETURNS trigger AS\n");
        sb.append("$BODY$\n").append("BEGIN\n");
        for (String s : t.getProcedure().split("\n")) {
            sb.append("    ").append(s);
        }
        sb.append("END;\n").append("$BODY$\n\n");

        sb.append("CREATE TRIGGER ").append(t.getName()).append("\n");
        sb.append("  ").append(t.getActivationTime()).append("\n");
        sb.append("  ON ").append(tableName).append("\n");
        sb.append("  ").append(t.getTriggerType()).append("\n");
        sb.append("  EXECUTE PROCEDURE ").append(t.getName()).append("_function();");

        return sb.toString();
    }

    /**
     * converts a view to a postgres view
     * @param rv the view to convert
     * @param sb the string builder building the script
     */
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
