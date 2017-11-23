/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.algorithm;

import querygenerator.mongoschema.DocumentType;
import querygenerator.mongoschema.Field;
import querygenerator.mongoschema.SimpleField;

/**
 *
 * @author Vivi
 */
public class JoinOperation extends Operation {

    private Pair<SimpleField, SimpleField> fields;

    public JoinOperation(Pair<SimpleField, SimpleField> fields, String text, ComputedEntity result) {
        super(text, result);
        this.fields = fields;
    }

    @Override
    public String generateOperation() {
        String lf = "data_" + fields.getFirst().getFieldMapping().getAttribute().getParent().getName()
                + "." + fields.getFirst().getName();
        String lfp = fields.getFirst().getParent().getName();
        String rf = fields.getSecond().getName();
        String rfp = fields.getSecond().getParent().getName();

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
