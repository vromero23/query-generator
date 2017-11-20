/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.algorithm;

import java.util.ArrayList;
import java.util.List;
import querygenerator.ermodel.Attribute;
import querygenerator.ermodel.ERElement;
import querygenerator.mongoschema.Field;
import querygenerator.mongoschema.SimpleField;

/**
 *
 * @author daniellucredio
 */
public class ComputedEntity {

    boolean main;
    String name;
    List<Field> fields;

    private ComputedEntity(boolean main, String name) {
        this.main = main;
        this.name = name;
        fields = new ArrayList<>();
    }

    public static ComputedEntity createNew(boolean main, String name) {
        return new ComputedEntity(main, name);
    }

    public static ComputedEntity createCopy(ComputedEntity ce) {
        if (ce != null) {
            ComputedEntity ret = new ComputedEntity(ce.isMain(), ce.getName());
            for (Field f : ce.fields) {
                ret.addField(f);
            }
            return ret;
        }
        return null;
    }

    public boolean isMain() {
        return main;
    }

    public String getName() {
        return name;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void addField(Field f) {
        this.fields.add(f);
    }

    public boolean containsMappedField(Attribute attribute) {
        for (Field f : fields) {
            if (f instanceof SimpleField) {
                SimpleField sf = (SimpleField) f;
                if (sf.getFieldMapping() != null
                        && sf.getFieldMapping().getAttribute() == attribute) {
                    return true;

                }
            }
        }
        return false;
    }

    boolean containsMappedERElement(ERElement erElement) {
        for (Field f : fields) {
            if (f instanceof SimpleField) {
                SimpleField sf = (SimpleField) f;
                if (sf.getFieldMapping() != null
                        && sf.getFieldMapping().getAttribute().getParent() == erElement) {
                    return true;

                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String ret = "";
        ret += "(main=" + main + ") ";
        ret += name + "\n";
        ret += "{";
        ret += "\n";
        for (Field f : fields) {
            ret += "   " + f.toString();
            ret += "\n";
        }
        ret += "}";
        ret += "\n";
        return ret;
    }

}
