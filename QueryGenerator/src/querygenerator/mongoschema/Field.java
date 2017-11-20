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

    public DocumentType getParent() {
        return parent;
    }
    
    
}
