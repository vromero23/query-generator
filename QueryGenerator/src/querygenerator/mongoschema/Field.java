/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.mongoschema;

import java.util.List;
import querygenerator.algorithm.ComputedEntity;
import querygenerator.algorithm.Pair;
import querygenerator.ermodel.ERElement;

/**
 *
 * @author daniellucredio
 */
public abstract class Field {
    
    
    String name;
    DocumentType parent;

    public Field(DocumentType parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DocumentType getParent() {
        return parent;
    }
    
}
