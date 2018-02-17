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
import querygenerator.mongoschema.ERMapping;
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
        
        String dataUpdateManyToMany = " ";

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
        
        //verificar se faz find da Entidade1 num documento onde o mapeamento dela
        //esteja como main=false
        for (ERMapping  eMapping: docType.getERMappingList()){
            if (eMapping.getERElement().equals(er1)){
                if (eMapping.isMain()==false){
                    System.out.println("\nA consulta pode não retornar todos os dados, a Entidade 1: " +
                            er1.getName() + " não é principal no documento: " + docType.getName() + "\n");
                }
            }
        }
        
        String ret = "db." + docType.getName() + ".find().forEach( function(data) {\n";
        for (ERElement ere : erElementsNew) {
            List<Pair<String, String>> fields = fieldsToProject.get(ere);

            if (nItem == 1) {
                if (entity1Cardinality.equals("Many") && ere.getName() == entity1.getName() && !entity2Cardinality.equals("Many") ) {
                    if (result.getArrayField().size() > 0) {
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
                            //ret += "         " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
  
                            int resultado = fieldName.getSecond().toLowerCase().indexOf("id");
                                if (resultado != -1) {
                                    identificadorManytoOne = fieldName.getSecond();
                                    ret += "         " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                                } else {
                                    ret += "         " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                                }
                        }
                        ret += "      }";
                        if (erElementsNew.size() == 1) {
                            ret += "\n      ,data_Join: []";
                        } else {
                            if(erElementsNew.size()>0 && entity2Cardinality.equals("Many")){
                                ret += "\n      ,data_Join: []";
                            } else {
                                ret += "\n      ,data_Join: [{\n";
                            }
                        }
                        updateManytoOne = "data_" + ere.getName();
                    }
                } else if (entity1Cardinality.equals("Many") && ere.getName() == entity1.getName() && entity2Cardinality.equals("Many") ) 
                {
                    //entra aqui 
                    if(result.getArrayField().size() > 0){
                        String nameArrayField = " ";
                        List<ArrayField> listArrayField = new ArrayList<>();
                        listArrayField = result.getArrayField();
                        for (ArrayField af : listArrayField) {
                            nameArrayField = af.getName();
                        }
                        ret += "   data." + nameArrayField + ".forEach(function(data1){\n";
                        ret += "        db.EC.insert( {\n";
                        ret += "            data_" + ere.getName() + ": {\n";
                        
                       
                        for (Pair<String, String> fieldName : fields) {
                            int docTypeE1 = docType.getName().toLowerCase().indexOf(er1.getName().toLowerCase().toString());
                            int resultado = fieldName.getSecond().toLowerCase().indexOf("id");
                            //Do docType tem mesmo nome da Entidade 1, então os dados não estarão dentro do documento embutido, array field
                            if (docTypeE1 != -1) {
                                dataUpdateManyToMany = "data.";
                                if (resultado != -1) {
                                    identificadorManytoOne = fieldName.getSecond();
                                    ret += "                " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                                } else {
                                    ret += "                " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                                }  
                            } else { //provavelmente estarão os dados da E1, dentro do arrayfield o doc embutido
                                dataUpdateManyToMany = "data1.";
                                if (resultado != -1) {
                                    identificadorManytoOne = fieldName.getSecond();
                                    ret += "                " + fieldName.getFirst() + ": data1." + fieldName.getSecond() + ",\n";
                                } else {
                                    ret += "                " + fieldName.getFirst() + ": data1." + fieldName.getSecond() + ",\n";
                                }
                            }
                           
                        }
                        ret += "            },";
                        ret += "\n          data_Join: []});\n";
                        updateManytoOne = "data_" + ere.getName();
                    } else {
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
                        }
                        ret += "      }";
                        //if (erElementsNew.size() == 1) {
                            ret += "\n      ,data_Join: []});\n";
                        //} else {
                        //    ret += "\n      ,data_Join: [{\n";
                       // }
                         updateManytoOne = "data_" + ere.getName();
                    }
                }
            } else {
                if (entity1Cardinality.equals("Many") && (result.getArrayField().size() > 0)){
                    if (entity2Cardinality.equals("Many") && !updateManytoOne.equals(" ")) {
                        //aqui entra solo cuando es N -N?????
                        if (ere.getName().equals(RelationshipName)) {
                            ret += "   db.EC.createIndex({ '"+ updateManytoOne + "." + identificadorManytoOne + "': 1 }, {unique: true } );\n";
                            ret += "   db.EC.update( {";
                            ret += "'" + updateManytoOne + "." + identificadorManytoOne + "':" + dataUpdateManyToMany + identificadorManytoOne + "},\n";
                            //ret += "   {$set: { \n";
                            ret += "   {$addToSet: { \n"; 
                            ret += "        'data_Join': {\n";
                            ret += "                data_" + ere.getName() + ": {\n";
                            for (Pair<String, String> fieldName : fields) {
                                ret += "                    " + fieldName.getFirst() + ": data1." + fieldName.getSecond() + ",\n";
                            }
                            ret += "                },\n";
                        } else if (!ere.getName().equals(RelationshipName) && result.getArrayField().size() > 0) {
                            //int existeE1ArrayField = nameArrayField2.toLowerCase().indexOf(er1.getName().toLowerCase().toString());
                            int docTypeE2 = docType.getName().toLowerCase().indexOf(ere.getName().toLowerCase().toString());                      
                            if (docTypeE2 != -1) {
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
                            }else {
                                ret += "                data_" + ere.getName() + ": {\n";
                                for (Pair<String, String> fieldName : fields) {
                                    ret += "                    " + fieldName.getFirst() + ": data1." + fieldName.getSecond() + ",\n";
                                }
                                ret += "                }";
                                //ret += "\n";
                                //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                                if (erElementsNew.size() > 1) {
                                    ret += ",\n";
                                }
                            }

                        //modificado porque gerava com erro consulta manyToone1a
                        // if (ere.getName().equals(RelationshipName)){
                        if (erElements.size() == nItem) {
                            ret += "        }\n";
                            ret += "     }});\n";
                        }
                        }
                    }
                    else 
                    {    
                        if(ere.getName().equals(RelationshipName)) {
                            
                            ret += "   db.EC.update( {";
                            ret += "'" + updateManytoOne + "." + identificadorManytoOne + "':data1." + identificadorManytoOne + "},\n";
                            ret += "   {$set: { \n";
                            ret += "        'data_Join': [{\n";
                            ret += "                data_" + ere.getName() + ": {\n";
                            for (Pair<String, String> fieldName : fields) {
                                ret += "                    " + fieldName.getFirst() + ": data1." + fieldName.getSecond() + ",\n";
                            }
                            ret += "                },\n";
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
                                    ret += "                }]\n";
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
                            }
                        }
                        //modificado porque gerava com erro consulta manyToone1a
                       // if (ere.getName().equals(RelationshipName)){
                            if(erElements.size() == nItem) {
                                ret += "        }]\n";
                                ret += "     }});\n";
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
                                ret += "\n                   data_" + ere.getName() + ": {\n";
                                for (Pair<String, String> fieldName : fields) {
                                    ret += "                        " + fieldName.getFirst() + ": data1." + fieldName.getSecond() + ",\n";
                                }
                                ret += "                    }";
                                //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                                if (erElementsNew.size() > 1) {
                                    ret += ",\n";
                                }
                                if (nItem == erElementsNew.size()) {
                                    ret += "              }}";
                                    ret += "\n          });\n";
                                }
                            }
                    } else if (entity2Cardinality.equals("One")){
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
                    } else if (entity1Cardinality.equals("One") && entity2Cardinality.equals("Many")){
                        if (ere.getName().equals(RelationshipName)) {
                            ret += "    });\n";
                            
                            //se identificadorManytoOne contem ponto (.) é porque esta concatenado com NomeDocEmbutido.atributo
                            int resultado = identificadorManytoOne.toLowerCase().indexOf(".");
                            if (resultado != -1) {
                                ret += "   db.EC.createIndex({ '"+ identificadorManytoOne + "': 1 }, {unique: true } );\n";
                                ret += "   db.EC.update( {";
                                ret += "'" + identificadorManytoOne + "':" + "data." + identificadorManytoOne + "},\n";
                            } else {
                                ret += "   db.EC.createIndex({ '"+ updateManytoOne + "." + identificadorManytoOne + "': 1 }, {unique: true } );\n"; 
                                ret += "   db.EC.update( {";
                                ret += "'" + updateManytoOne + "." + identificadorManytoOne + "':" + "data." + identificadorManytoOne + "},\n";
                                
                            }
                            
                            ret += "   {$addToSet: { \n"; 
                            ret += "        'data_Join': {\n";
                            ret += "                data_" + ere.getName() + ": {\n";
                            for (Pair<String, String> fieldName : fields) {
                                ret += "                    " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                            }
                            ret += "                },\n";
                        } else if (!ere.getName().equals(RelationshipName) && result.getArrayField().size() == 0) {
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
                        }
                        if (erElements.size() == nItem) {
                            ret += "        }\n";
                            ret += "     }\n";
                        }
                    } else if (entity1Cardinality.equals("Many") && entity2Cardinality.equals("Many")){
                       if (ere.getName().equals(RelationshipName)) {
                          
                            //se identificadorManytoOne contem ponto (.) é porque esta concatenado com NomeDocEmbutido.atributo
                            int resultado = identificadorManytoOne.toLowerCase().indexOf(".");
                            if (resultado != -1) {
                                ret += "   db.EC.createIndex({ '"+ identificadorManytoOne + "': 1 }, {unique: true } );\n";
                                ret += "   db.EC.update( {";
                                ret += "'" + identificadorManytoOne + "':" + "data." + identificadorManytoOne + "},\n";
                            } else {
                                ret += "   db.EC.createIndex({ '"+ updateManytoOne + "." + identificadorManytoOne + "': 1 }, {unique: true } );\n"; 
                                ret += "   db.EC.update( {";
                                ret += "'" + updateManytoOne + "." + identificadorManytoOne + "':" + "data." + identificadorManytoOne + "},\n";
                                
                            }
                            
                            ret += "   {$addToSet: { \n"; 
                            ret += "        'data_Join': {\n";
                            ret += "                data_" + ere.getName() + ": {\n";
                            for (Pair<String, String> fieldName : fields) {
                                ret += "                    " + fieldName.getFirst() + ": data." + fieldName.getSecond() + ",\n";
                            }
                            ret += "                },\n";
                        } else if (!ere.getName().equals(RelationshipName) && result.getArrayField().size() == 0) {
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
                        }
                        if (erElements.size() == nItem) {
                            ret += "        }\n";
                            ret += "     }\n";
                        } 
                    }
                }
            }
            nItem++;
        }
        ret += "    });\n";
        ret += "});";
        return ret;
    }
}
