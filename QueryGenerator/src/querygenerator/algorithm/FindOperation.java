/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import querygenerator.ermodel.ERElement;
import querygenerator.mongoschema.DocumentType;
import querygenerator.mongoschema.Field;
import querygenerator.mongoschema.SimpleField;

/**
 *
 * @author Vivi
 */
public class FindOperation extends Operation {

    private DocumentType docType;

    public FindOperation(DocumentType docType, String text, ComputedEntity result) {
        super(text, result);
        this.docType = docType;
    }

    @Override
    public String generateOperation() {
        Map<ERElement, List<SimpleField>> fieldsToProject = new HashMap<>();
        for (Field f : result.getNewFields()) {
            if (f instanceof SimpleField) {
                SimpleField sf = (SimpleField) f;
                if (sf.getFieldMapping() != null) {
                    ERElement ere = sf.getFieldMapping().getAttribute().getParent();
                    List<SimpleField> fp = fieldsToProject.get(ere);
                    if (fp == null) {
                        fp = new ArrayList<>();
                        fieldsToProject.put(ere, fp);
                    }
                    fp.add(sf);
                }
            }
        }
        String ret = "db." + docType.getName() + ".find().forEach( function(data) {\n"
                + "   db.EC.insert( {\n";
        Set<ERElement> erElements = fieldsToProject.keySet();
        for (ERElement ere : erElements) {
            ret += "      data_" + ere.getName() + ": {\n";
            List<SimpleField> fields = fieldsToProject.get(ere);
            for (SimpleField sf : fields) {
                ret+= "         " + sf.getName() + ": data." + sf.getName() + ",\n";
            }
            ret += "      }\n";
        }
        ret += "   });\n";
        ret += "});";
        return ret;
    }

}
