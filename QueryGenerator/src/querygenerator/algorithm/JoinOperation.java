/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import querygenerator.mongoschema.DocumentType;
import querygenerator.mongoschema.EmbeddedField;
import querygenerator.mongoschema.Field;
import querygenerator.mongoschema.SimpleField;

/**
 *
 * @author Vivi
 */
public class JoinOperation extends Operation {

    private Pair<List<Pair<Field,DocumentType>>, List<Pair<Field,DocumentType>>> fields;

    
    public JoinOperation(Pair<List<Pair<Field,DocumentType>>, List<Pair<Field,DocumentType>>> fields, String text, ComputedEntity result) {
        super(text, result);
        this.fields = fields;

    }
    
    @Override
    public String generateOperation() {

    String lf = " ";
    String lfp = " ";
    String rf = " ";    
    String rfp = " ";
    
    for (Pair<Field, DocumentType> p : fields.getFirst()) {
        Field fFirst = p.getFirst();
        if (fFirst instanceof SimpleField) {
            SimpleField f = (SimpleField) fFirst;
            lf = "data_" + f.getFieldMapping().getAttribute().getParent().getName() + "."+ fFirst.getName();
            lfp =  p.getSecond().getName();
            

        } else {
            EmbeddedField f = (EmbeddedField) fFirst;
            lf = "data_" + f.getSubDocType().getName() + "."+ fFirst.getName();
            lfp =  p.getSecond().getName();
        }
    }
    for (Pair<Field, DocumentType> p : fields.getSecond()) {
        Field fFirst = p.getFirst();
        if (p.getFirst() instanceof SimpleField) {
            SimpleField f = (SimpleField) fFirst;
            rf = "data_" + f.getFieldMapping().getAttribute().getParent().getName()+ "." + fFirst.getName();
            rfp = p.getSecond().getName();

        } else {
            EmbeddedField f = (EmbeddedField) fFirst;
            rf = "data_" + f.getSubDocType().getName() + fFirst.getName();
            rfp = p.getSecond().getName(); 
        }
    }    
          
        //String lf = "data_A";/// + fields.getFirst().getFieldMapping().getAttribute().getParent().getName()
                //+ "." + fields.getFirst().getName();
        //String lfp = fields.getFirst().getParent().getName();
        //String rf = fields.getSecond().getName();
        //String rfp = fields.getSecond().getParent().getName();

        String ret = "db.EC.find().forEach( function(data){\n"
                + "   var varData = db." + rfp + ".findOne("
                + "{ '" + rf + "': data." + lf + " });\n"
                + "   db.EC.update( {'" + lf + "': data." + lf + "},\n"
                + "                 { $set: { \n";

        for (Field f : result.getNewFields()) {
            if (f instanceof SimpleField) {
                SimpleField sf = (SimpleField) f;
                String erName = sf.getFieldMapping().getAttribute().getParent().getName();
                ret += "                    'data_" + erName + "." + sf.getName() + "': varData." + sf.getName() + ",\n";
            }

        }
        ret += "                          }\n"
                + "                  }\n"
                + "                )\n"
                + "});";
        return ret;
    }

}