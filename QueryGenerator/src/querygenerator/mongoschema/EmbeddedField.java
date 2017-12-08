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
public class EmbeddedField extends Field {
    DocumentType subDocType;

    public DocumentType getSubDocType() {
        return subDocType;
    }

    public EmbeddedField(DocumentType parent, String name, DocumentType subDocType) {
        super(parent, name);
        this.subDocType = subDocType;
    }
    
    @Override
    public String toString() {
        String ret = name + " : " + subDocType.toString();
        return ret;
    }    
}
