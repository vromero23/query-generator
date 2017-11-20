/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.mongoschema;

/**
 *
 * @author daniellucredio
 */
public class SimpleField extends Field {

    String type;
    FieldMapping fieldMapping;

    public SimpleField(DocumentType parent, String name, String type, FieldMapping fieldMapping) {
        super(parent, name);
        this.type = type;
        this.fieldMapping = fieldMapping;
        if (this.fieldMapping != null
                && !this.type.equals(fieldMapping.attribute.getType())) {
            throw new RuntimeException("Tipo do campo "
                    + parent.name + "." + name + " (" + type + ") "
                    + "não é o mesmo que o tipo do atributo mapeado "
                    + fieldMapping.attribute.getParent().getName() + "."
                    + fieldMapping.attribute.getName() + " (" + fieldMapping.attribute.getType() + ")");
        }
    }

    public String getType() {
        return type;
    }

    public FieldMapping getFieldMapping() {
        return fieldMapping;
    }

    @Override
    public String toString() {
        String ret = name + " : " + type + " [ ";
        if (fieldMapping != null) {
            ret += fieldMapping.toString();
        }
        ret += " ]";
        return ret;
    }

}
