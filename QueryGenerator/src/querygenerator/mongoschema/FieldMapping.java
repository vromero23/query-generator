/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.mongoschema;

import querygenerator.ermodel.Attribute;

/**
 *
 * @author daniellucredio
 */
public class FieldMapping {

    Attribute attribute;

    public FieldMapping(Attribute attribute) {
        this.attribute = attribute;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    @Override
    public String toString() {
        return attribute.getParent().getName() + "." + attribute.getName();
    }

}
