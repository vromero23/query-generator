/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.mongoschema;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Vivi
 */
public class ArrayField {
    String name;
    SimpleField fields;

    public SimpleField getFields() {
        return fields;
    }

    public void setFields(SimpleField fields) {
        this.fields = fields;
    }

    public ArrayField(String name) {
        this.name = name;
        //this.fields = new ArrayList<>();
    }

    public ArrayField(String name, SimpleField field) {
        this.name = name;
        this.fields = field;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String toString() {
        String ret = "      "+ name + " : [" + fields.type +  "[ ";
        if (fields.fieldMapping != null) {
            ret += fields.fieldMapping.toString();
        }
        ret += " ]]";
        return ret;
    }
}
