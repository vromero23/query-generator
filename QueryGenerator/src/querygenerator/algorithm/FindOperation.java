/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import querygenerator.ermodel.ERElement;
import querygenerator.mongoschema.DocumentType;
import querygenerator.mongoschema.EmbeddedField;
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
        Map<ERElement, List<Pair<String, String>>> fieldsToProject = new HashMap<>();
        for (Field f : result.getNewFields()) {
            if (f instanceof SimpleField) {
                SimpleField sf = (SimpleField) f;
                if (sf.getFieldMapping() != null) {
                    ERElement ere = sf.getFieldMapping().getAttribute().getParent();
                    List<Pair<String, String>> fp = fieldsToProject.get(ere);
                    if (fp == null) {
                        fp = new ArrayList<>();
                        fieldsToProject.put(ere, fp);
                    }

                    fp.add(new Pair<>(sf.getName(), sf.getName()));
                }
            } else if (f instanceof EmbeddedField) {
                EmbeddedField ef = (EmbeddedField) f;
                DocumentType subDocType = ef.getSubDocType();
                for (Field subField : subDocType.getFields()) {
                    if (subField instanceof SimpleField) {
                        SimpleField sf = (SimpleField) subField;
                        if (sf.getFieldMapping() != null) {
                            ERElement ere = sf.getFieldMapping().getAttribute().getParent();
                            List<Pair<String, String>> fp = fieldsToProject.get(ere);
                            if (fp == null) {
                                fp = new ArrayList<>();
                                fieldsToProject.put(ere, fp);
                            }
                            fp.add(new Pair<>(sf.getName(), ef.getName() + "." + sf.getName()));
                        }
                    }
                }
            }
        }
        
        int nI= 1;
        ERElement er1 = null;
        //Recuperamos Entidade1
        for(ERElement ere: result.erElements){
            if(nI==1){
                er1 = ere;
            }
            nI++;
        }
        
        //É PRECISO ORDENAR erElements A PARTIR DO result.erElements, TEM DE FICAR ORDENADO
        //ENTIDADE 1 , RELACIONAMENTO, ENTIDADE 2
        
        String ret = "db." + docType.getName() + ".find().forEach( function(data) {\n"
                + "   db.EC.insert( {\n";
        Set<ERElement> erElements = fieldsToProject.keySet();  
        
        List<ERElement> erElementsNew = new ArrayList<ERElement>();
        for (ERElement ere1 : result.erElements){
            for (ERElement ere2 : erElements){
                 if(ere1.equals(ere2)){
                     erElementsNew.add(ere2);
                 }
            }
        } 
        
        int nItem = 1;
        for (ERElement ere : erElementsNew) {
            if (nItem == 1) {
                ret += "      data_" + ere.getName() + ": {\n";
                List<Pair<String, String>> fields = fieldsToProject.get(ere);
                for (Pair<String, String> fieldName : fields) {
                    ret += "         " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                }
                ret += "      }";
                /*adicionar campo data_Join*/
                if(erElementsNew.size() ==1)
                {
                   ret += "\n ,data_Join: []";
                }
                else
                {
                    ret += "\n ,data_Join: [{";
                }
            }else {
                ret += "      data_" + ere.getName() + ": {\n";
                List<Pair<String, String>> fields = fieldsToProject.get(ere);
                for (Pair<String, String> fieldName : fields) {
                    ret += "         " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                }
                ret += "      }";
                ret += "\n";
                //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                if (erElementsNew.size() > 1) {
                    ret += ",\n";
                }
                if(nItem==erElementsNew.size()){
                    ret += "}]";
                }
              
            }
            
            nItem++;
        }
        ret += "   });\n";
        ret += "});";

        return ret;
    }

}
