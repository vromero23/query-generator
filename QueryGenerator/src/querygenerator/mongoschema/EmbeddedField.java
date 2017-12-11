/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.mongoschema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import querygenerator.algorithm.ComputedEntity;
import querygenerator.algorithm.Pair;
import querygenerator.ermodel.ERElement;

/**
 *
 * @author daniellucredio
 */
public class EmbeddedField extends Field {
    DocumentType subDocType;

    public String getName() {
        return name;
    }

    public DocumentType getSubDocType() {
        return subDocType;
    }

    public EmbeddedField(DocumentType parent, String name, DocumentType subDocType) {
        super(parent, name);
        this.subDocType = subDocType;
    }
    
    @Override
   public String toString() {
        String ret = name + " : " + " { \n";
        for (Field f : subDocType.fields){
            ret += "   " + f.toString();
            ret += "\n";
        }
        ret += " }";
        return ret;
    }
}
