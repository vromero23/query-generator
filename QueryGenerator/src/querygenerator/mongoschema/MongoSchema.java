/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.mongoschema;

import java.util.ArrayList;
import java.util.List;
import querygenerator.ermodel.ERElement;
import querygenerator.ermodel.Entity;
import querygenerator.ermodel.Relationship;

/**
 *
 * @author daniellucredio
 */
public class MongoSchema {

    List<DocumentType> documentTypes;

    public MongoSchema() {
        this.documentTypes = new ArrayList<>();
    }

    public List<DocumentType> getDocumentTypes() {
        return documentTypes;
    }

    
    public void addDocumentType(DocumentType dt) {
        this.documentTypes.add(dt);
    }
    
    public List<String> validate() {
        List<String> violations = new ArrayList<>();
        for(DocumentType dt: documentTypes) {
            dt.validate(violations);
        }
        return violations;
    }

    @Override
    public String toString() {
        String ret = "";
        
        for(DocumentType dt: documentTypes) {
            ret += dt.toString();
            ret += "\n";
        }
        
        return ret;
    }

    public List<DocumentType> findDocumentTypes(ERElement e1) {
        List<DocumentType> ret = new ArrayList<>();
        for(DocumentType dt: documentTypes){
            for(ERMapping erMapping:dt.erMappingList) {
                if(erMapping.erElement == e1) {
                    ret.add(dt);
                }
            }
        }
        return ret;
    }
}
