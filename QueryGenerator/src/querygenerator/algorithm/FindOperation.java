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
import querygenerator.ermodel.Entity;
import querygenerator.ermodel.Relationship;
import querygenerator.mongoschema.ArrayField;
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
    private Relationship r;

    public FindOperation(DocumentType docType, String text, ComputedEntity result, Relationship r) {
        super(text, result);
        this.docType = docType;
        this.r = r;
    }

    @Override
    public String generateOperation() {

        String EntityCardinality1 = " ";
        String EntityCardinality2 = " ";
        String RelationshipName = "";
        RelationshipName = r.getName();

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

        if (result.getArrayField().size() > 0) {
            List<ArrayField> listArrayField = new ArrayList<>();
            listArrayField = result.getArrayField();
            for (ArrayField af : listArrayField) {
                if (af.getFields() instanceof SimpleField) {
                    SimpleField sf = (SimpleField) af.getFields();
                    if (sf.getFieldMapping() != null) {
                        ERElement ere = sf.getFieldMapping().getAttribute().getParent();
                        List<Pair<String, String>> fp = fieldsToProject.get(ere);
                        if (fp == null) {
                            fp = new ArrayList<>();
                            fieldsToProject.put(ere, fp);
                        }
                        fp.add(new Pair<>(sf.getName(), sf.getName()));
                    }
                }
            }
        }
        Set<ERElement> erElements = fieldsToProject.keySet();


        //É PRECISO ORDENAR erElements A PARTIR DO result.erElements, TEM DE FICAR ORDENADO
        //ENTIDADE 1 , RELACIONAMENTO, ENTIDADE 2
        List<ERElement> erElementsNew = new ArrayList<ERElement>();
        for (ERElement ere1 : result.erElements) {
            for (ERElement ere2 : erElements) {
                if (ere1.equals(ere2)) {
                    erElementsNew.add(ere2);
                    // System.out.println("ELEMENTOS DENTRO DO FIND: " + ere2.getName().toString());
                }
            }
        }

        //recuperamos Entidade 1 e 2 para encontrar logo a Cardinalidade de cada um
        ERElement entity1 = result.erElements.get(0);
        Entity ent1 = (Entity) entity1;
        ERElement entity2 = result.erElements.get(2);
        Entity ent2 = (Entity) entity2;

        String entity1Cardinality = r.recoverCardinality(ent1);
        String entity2Cardinality = r.recoverCardinality(ent2);

        ERElement er1 = null;
        //Recuperamos Entidade1
        er1 = result.erElements.get(0);
        
        int existeRelationship = 0;
        for (ERElement ere : erElements) {
            if(ere.getName().equals(RelationshipName)){
                existeRelationship = 1;
            }
        }
        
        int nItem = 1;
        String identificadorManytoOne = " ";
        String updateManytoOne = " ";
        
        String ret = "db." + docType.getName() + ".find().forEach( function(data) {\n";
        for (ERElement ere : erElementsNew) {
            List<Pair<String, String>> fields = fieldsToProject.get(ere);

            if (nItem == 1) {
                if (entity1Cardinality.equals("Many") && ere.getName() == entity1.getName() && !entity2Cardinality.equals("Many") ) {
                    if (result.getArrayField().size() > 0) {
                        // System.out.println("AQUI DEVERIA ENTRAR SO SE TIVER getArrayField!!!!!!!!!!!");
                        ret += "data.data_" + ere.getName() + ".forEach(function(data1){\n";
                        ret += "   db.EC.insert( {\n";
                        ret += "      data_" + ere.getName() + ": {\n";
                        for (Pair<String, String> fieldName : fields) {
                            int resultado = fieldName.getSecond().toLowerCase().indexOf("id");
                            if (resultado != -1) {
                                identificadorManytoOne = fieldName.getSecond();
                                ret += "         " + fieldName.getFirst() + ": data1." + fieldName.getSecond() + ",\n";
                            } else {
                                ret += "         " + fieldName.getFirst() + ": data1." + fieldName.getSecond() + ",\n";
                            }
                        }
                        ret += "      }});\n";
                        updateManytoOne = "data_" + ere.getName();

                    } else {
                        ret += "   db.EC.insert( {\n";
                        ret += "      data_" + ere.getName() + ": {\n";
                        for (Pair<String, String> fieldName : fields) {
                            ret += "         " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                        }
                        ret += "      }";
                        if (erElementsNew.size() == 1) {
                            ret += "\n      ,data_Join: []";
                        } else {
                            ret += "\n      ,data_Join: [{\n";
                        }
                    }
                }  else if (entity1Cardinality.equals("One") && ere.getName() == entity1.getName()) {
                    if(result.getArrayField().size() > 0)
                    {    
                        ret += "   db.EC.insert( {\n";
                        ret += "      data_" + ere.getName() + ": {\n";
                        for (Pair<String, String> fieldName : fields) {
                                int resultado = fieldName.getSecond().toLowerCase().indexOf("id");
                                if (resultado != -1) {
                                    identificadorManytoOne = fieldName.getSecond();
                                    ret += "         " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                                } else {
                                    ret += "         " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                                }
                            // ret += "         " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                        }
                        ret += "      }";
                        updateManytoOne = "data_" + ere.getName();
                        ret += "\n      ,data_Join: []});";
                    } else {
                        ret += "   db.EC.insert( {\n";
                        ret += "      data_" + ere.getName() + ": {\n";
                        for (Pair<String, String> fieldName : fields) {
                            ret += "         " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                        }
                        ret += "      }";
                        if (erElementsNew.size() == 1) {
                            ret += "\n      ,data_Join: []";
                        } else {
                            ret += "\n      ,data_Join: [{\n";
                        }
                    }
                } else if (entity1Cardinality.equals("Many") && ere.getName() == entity1.getName() && entity2Cardinality.equals("Many") ) 
                {
                    ret += "   db.EC.insert( {\n";
                    ret += "      data_" + ere.getName() + ": {\n";
                    for (Pair<String, String> fieldName : fields) {
                        ret += "         " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                    }
                    ret += "      }";
                    if (erElementsNew.size() == 1) {
                        ret += "\n      ,data_Join: []";
                    } else {
                        ret += "\n      ,data_Join: [{\n";
                    }
                }
            } else {
                if (entity1Cardinality.equals("Many") && (result.getArrayField().size() > 0)){
                    if (entity2Cardinality.equals("Many") && !updateManytoOne.equals(" ")) {
                        //if (ere.getName().equals(RelationshipName)) {
                            ret += "   db.EC.update( {";
                            ret += "'" + updateManytoOne + "." + identificadorManytoOne + "':data1." + identificadorManytoOne + "},\n";
                            ret += "{   $set: { \n";
                            ret += "        'data_Join': [{\n";
                            ret += "                data_" + ere.getName() + ": {\n";
                            for (Pair<String, String> fieldName : fields) {
                                ret += "                " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                            }
                            ret += "        }";
                        //}
                        /*else if (!ere.getName().equals(RelationshipName)) {
                            ret += "\n                data_" + ere.getName() + ": {\n";
                            for (Pair<String, String> fieldName : fields) {
                                ret += "                " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                            }
                            ret += "        }";
                        }*/
                        if (erElements.size() == nItem) {
                            ret += "        }]\n";
                            ret += "}});\n";
                        }
                    }
                    else 
                    {    
                        if(ere.getName().equals(RelationshipName)) {
                            
                            ret += "   db.EC.update( {";
                            ret += "'" + updateManytoOne + "." + identificadorManytoOne + "':data1." + identificadorManytoOne + "},\n";
                            ret += "{   $set: { \n";
                            ret += "        'data_Join': [{\n";
                            ret += "                data_" + ere.getName() + ": {\n";
                            for (Pair<String, String> fieldName : fields) {
                                ret += "                " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                            }
                            ret += "        },";
                        } else if (!ere.getName().equals(RelationshipName) && result.getArrayField().size() > 0){
                            String nameArrayField2 = " ";
                            List<ArrayField> listArrayField = new ArrayList<>();
                            listArrayField = result.getArrayField();
                            for (ArrayField af : listArrayField) {
                                nameArrayField2 = af.getName();
                            }
                            int resultado = nameArrayField2.toLowerCase().indexOf(ere.getName().toLowerCase());
                            if (resultado != -1) {
                                ret += "                data_" + ere.getName() + ":" + "data.data_" + ere.getName();
                                if (nItem == erElementsNew.size()) {
                                    ret += "}]\n";
                                }
                            } else {
                                ret += "                data_" + ere.getName() + ": {\n";
                                for (Pair<String, String> fieldName : fields) {
                                    ret += "                    " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                                }
                                ret += "      }";
                                //ret += "\n";
                                //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                                if (erElementsNew.size() > 1) {
                                    ret += ",\n";
                                }
                            }
                        }
                        //modificado porque gerava com erro consulta manyToone1a
                       // if (ere.getName().equals(RelationshipName)){
                            if(erElements.size() == nItem) {
                                ret += "        }]\n";
                                ret += "}});\n";
                            }
                        //}
                        /*if (!ere.getName().equals(RelationshipName)) {
                            if (erElements.size() == nItem) {
                                ret += "        }]\n";
                                ret += "}});\n";
                            }
                        }*/
                    }
                } else {
                    String nameArrayField = " ";
                    List<ArrayField> listArrayField = new ArrayList<>();
                    listArrayField = result.getArrayField();
                    for (ArrayField af : listArrayField) {
                        nameArrayField = af.getName();
                    }
                    if(ere.getName().equals(RelationshipName) && !nameArrayField.equals(" ") ) {
                            ret += "\n      data." + nameArrayField + ".forEach(function(data1){";
                            ret += "\n      db.EC.update( {";
                            ret += "'" + updateManytoOne + "." + identificadorManytoOne + "':data." + identificadorManytoOne + "},\n";
                            ret += "            { $addToSet: { \n";
                            ret += "                'data_Join': {\n";
                            ret += "                    data_" + ere.getName() + ": {\n";
                            for (Pair<String, String> fieldName : fields) {
                                ret += "                        " + fieldName.getFirst() + ": data1." + fieldName.getSecond() + ",\n";
                            }
                            ret += "                    },";
                    }  else if (!ere.getName().equals(RelationshipName) && result.getArrayField().size() > 0){
                            String nameArrayField2 = " ";
                            List<ArrayField> listArrayField2 = new ArrayList<>();
                            listArrayField2 = result.getArrayField();
                            for (ArrayField af : listArrayField2) {
                                nameArrayField2 = af.getName();
                            }
                            int resultado = nameArrayField2.toLowerCase().indexOf(ere.getName().toLowerCase());
                            if (resultado != -1) {
                               /* ret += "                data_" + ere.getName() + ":" + "data.data_" + ere.getName();
                                if (nItem == erElementsNew.size()) {
                                    ret += "}]\n";
                                }
                            } else {*/
                                ret += "\n                   data_" + ere.getName() + ": {\n";
                                for (Pair<String, String> fieldName : fields) {
                                    ret += "                        " + fieldName.getFirst() + ": data1." + fieldName.getSecond() + ",\n";
                                }
                                ret += "                    }";
                                //ret += "\n";
                                //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                                if (erElementsNew.size() > 1) {
                                    ret += ",\n";
                                }
                                if (nItem == erElementsNew.size()) {
                                    ret += "              }}";
                                    ret += "\n          });\n";
                                }
                            }
                    } else {
                        ret += "                data_" + ere.getName() + ": {\n";
                        for (Pair<String, String> fieldName : fields) {
                            ret += "                    " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                        }
                        ret += "                }";
                        //ret += "\n";
                        //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                        if (erElementsNew.size() > 1) {
                            ret += ",\n";
                        }
                        if (nItem == erElementsNew.size()) {
                            ret += "            }]\n";
                        }
                    }
                }
            }
            nItem++;
        }
        ret += "        });\n";
        ret += "});";
        return ret;
    }
}
