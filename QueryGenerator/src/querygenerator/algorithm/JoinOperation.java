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

    //Em Field dentro do fields, sempre vai ser um campo simples, vamos verificar se é embutido verificando o pai
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
            lf = "data_" + f.getFieldMapping().getAttribute().getParent().getName() + "." + fFirst.getName();
            lfp = p.getSecond().getName();
        } else {
            EmbeddedField f = (EmbeddedField) fFirst;
            lf = "data_" + f.getSubDocType().getName() + "." + fFirst.getName();
            lfp = p.getSecond().getName();
        }
    }
    for (Pair<Field, DocumentType> p : fields.getSecond()) {
        Field fFirst = p.getFirst();
        if (p.getFirst() instanceof SimpleField) {
            SimpleField f = (SimpleField) fFirst;
            DocumentType dt = p.getSecond();
            //verificar pai, pra saber se é embutido ou nao, se não tem mesmo pai, é porque tem dados embutidos
            if(f.getParent().getName()!=dt.getName())
            {
                rf = "data_" + f.getFieldMapping().getAttribute().getParent().getName()+ "." + fFirst.getName();
                rfp = p.getSecond().getName();
            } else {
                rf = fFirst.getName();
                rfp = p.getSecond().getName();                
            }
        } else {
            EmbeddedField f = (EmbeddedField) fFirst;
            rf = "data_" + f.getSubDocType().getName() + fFirst.getName();
            rfp = p.getSecond().getName(); 
        }
    }    
          
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
            if (f instanceof EmbeddedField){
                EmbeddedField emf = (EmbeddedField) f;
                DocumentType subDocType = emf.getSubDocType();
                for (Field subField : subDocType.getFields()) {
                    if (subField instanceof SimpleField) {
                        SimpleField sf = (SimpleField) subField;
                        ret += "                    '" + emf.getName() + "." + sf.getName() + "': varData." +  emf.getName() + "." + sf.getName() + ",\n";
                    }
                }
            }
        
        }        
        ret += "                          }\n"
                + "                  }\n"
                + "                )\n"
                + "});";
        return ret;
    }

}