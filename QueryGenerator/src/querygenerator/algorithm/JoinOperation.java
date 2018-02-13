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
import querygenerator.ermodel.Attribute;
import querygenerator.ermodel.Cardinality;
import querygenerator.ermodel.ERElement;
import querygenerator.ermodel.ERModel;
import querygenerator.ermodel.Entity;
import querygenerator.ermodel.Relationship;
import querygenerator.ermodel.RelationshipEnd;
import querygenerator.mongoschema.ArrayField;
import querygenerator.mongoschema.DocumentType;
import querygenerator.mongoschema.EmbeddedField;
import querygenerator.mongoschema.Field;
import querygenerator.mongoschema.SimpleField;

/**
 *
 * @author Vivi
 */
public class JoinOperation extends Operation {

    private Pair<List<Pair<Field, DocumentType>>, List<Pair<Field, DocumentType>>> fields;
    private int valor;
    private Relationship r;

    //Em Field dentro do fields, sempre vai ser um campo simples, vamos verificar se é embutido verificando o pai
    public JoinOperation(Pair<List<Pair<Field, DocumentType>>, List<Pair<Field, DocumentType>>> fields, String text, ComputedEntity result, int valor, Relationship r) {
        super(text, result);
        this.fields = fields;
        this.valor = valor;
        this.r = r;
    }

    @Override
    public String generateOperation() {

        String lf = " ";
        String lfp = " ";
        String rf = " ";
        String rfp = " ";
        String newlf = " ";
        String rfManyToOne = " ";
        String lfArrayField = " ";
        String nomeEntidadeValorDois = " ";
        String nomeAtributoIDEntidadeUM = " ";
        String newFieldWhere = " ";

        //PARA COMPLETAR DADOS DA ENTIDADE 1 - COMEÇA AQUI
        //Recuperamos Entidade1
        ERElement er1 = result.erElements.get(0);
        String nameAtribute = " ";
        String nameDocument = " ";
        String nameEntity = " ";
        String EntityCardinality1 = " ";
        String EntityCardinality2 = " ";
        //percorremos documentos do result
        //achamos aquele que tem mapeado a Entidade1
        // recuperamos Campo identificador                  
        DocumentType dtFirst = result.getDocumentTypes().get(0);
        for (Field f : dtFirst.getFields()) {
            if (f instanceof SimpleField) {
                SimpleField sf = (SimpleField) f;
                if (sf.getFieldMapping() != null) {
                    if (sf.getFieldMapping().getAttribute().isIdentifier()) {
                        if (sf.getFieldMapping().getAttribute().getParent() == er1) {
                            nameEntity = er1.getName();
                            nameAtribute = sf.getName();
                        }
                    }
                }
            }
            if (f instanceof EmbeddedField) {
                EmbeddedField emf = (EmbeddedField) f;
                DocumentType subDocType = emf.getSubDocType();
                for (Field subField : subDocType.getFields()) {
                    if (subField instanceof SimpleField) {
                        SimpleField sf = (SimpleField) subField;
                        if (sf.getFieldMapping() != null) {
                            if (sf.getFieldMapping().getAttribute().isIdentifier()) {
                                if (sf.getFieldMapping().getAttribute().getParent() == er1) {
                                    nameEntity = er1.getName();
                                    nameAtribute = sf.getName();
                                }
                            }
                        }
                    }
                }
            }
        }
        if (dtFirst.getArrayField().size()>0 && nameAtribute == " " && nameEntity == " "){
            List<ArrayField> listArrayField = new ArrayList<>();
            listArrayField = dtFirst.getArrayField();
            for (ArrayField af : listArrayField) {
                if (af.getFields() instanceof SimpleField) {
                    SimpleField sf = (SimpleField) af.getFields();
                    if (sf.getFieldMapping() != null) {
                        if (sf.getFieldMapping().getAttribute().isIdentifier()) {
                            if (sf.getFieldMapping().getAttribute().getParent() == er1) {
                                nameEntity = er1.getName();
                                nameAtribute = sf.getName();
                            }
                        }
                    }
                }
            } 
        }
        
        newFieldWhere = nameEntity + "." + nameAtribute;
        //PARA COMPLETAR DADOS DA ENTIDADE 1 - TERMINA AQUI

        //aqui verificamos os left field para construir correctamente consulta javascript
        for (Pair<Field, DocumentType> p : fields.getFirst()) {
            Field fFirst = p.getFirst();
            if (fFirst instanceof SimpleField) {
                SimpleField f = (SimpleField) fFirst;
                lf = "data_" + f.getFieldMapping().getAttribute().getParent().getName() + "." + fFirst.getName();
                lfp = p.getSecond().getName();
                if (valor == 2) {
                    lf = "data_" + f.getFieldMapping().getAttribute().getParent().getName();
                    newlf = "data_" + f.getFieldMapping().getAttribute().getParent().getName() + "." + fFirst.getName();
                    nomeEntidadeValorDois = f.getFieldMapping().getAttribute().getParent().getName();
                    nomeAtributoIDEntidadeUM = fFirst.getName();
                }
                lfArrayField = fFirst.getName();
            } else {
                EmbeddedField f = (EmbeddedField) fFirst;
                lf = "data_" + f.getSubDocType().getName() + "." + fFirst.getName();
                lfp = p.getSecond().getName();
                if (valor == 2) {
                    lf = "data_" + f.getSubDocType().getName();
                    newlf = "data_" + f.getSubDocType().getName() + "." + fFirst.getName();
                    nomeEntidadeValorDois = f.getSubDocType().getName();
                    nomeAtributoIDEntidadeUM = fFirst.getName();
                }
                lfArrayField = fFirst.getName();
            }
        }
        //TERMINA AQUI - aqui verificamos os rigth field e left field para construir correctamente consulta javascript

        //aqui verificamos os rigth field para construir correctamente consulta javascript
        for (Pair<Field, DocumentType> p : fields.getSecond()) {
            Field fFirst = p.getFirst();
            if (p.getFirst() instanceof SimpleField) {
                SimpleField f = (SimpleField) fFirst;
                DocumentType dt = p.getSecond();
                //verificar pai, pra saber se é embutido ou nao, se não tem mesmo pai, é porque tem dados embutidos
                if (f.getParent().getName() != dt.getName()) {
                    rf = "data_" + f.getFieldMapping().getAttribute().getParent().getName() + "." + fFirst.getName();
                    rfManyToOne = "data_" + f.getFieldMapping().getAttribute().getParent().getName() + "." + fFirst.getName();
                    rfp = p.getSecond().getName();
                } else {
                    rf = fFirst.getName();
                    rfManyToOne = "data_" + f.getFieldMapping().getAttribute().getParent().getName() + "." + rf;
                    rfp = p.getSecond().getName();
                }
            }
        }
        //TERMINA AQUI aqui verificamos os rigth field para construir correctamente consulta javascript

        //AGRUPAMOS OS FIELDS, COM CHAVE IGUAL A ENTIDADE AO QUAL PERTENCE
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
         
        Set<ERElement> erElements = fieldsToProject.keySet();
        
        //sempre inserimos atributos do relacionamento no fieldsToProject, se for atualizar pra frente dados do join
        //se for atualizar dados da primeira entidade, nao precisamos inserir
        String RelationshipName = "";
        RelationshipName = r.getName();
        //recuperamos Entidade 1 e 2 para encontrar logo a Cardinalidade de cada um
        ERElement entity1 = result.erElements.get(0);
        Entity ent1 = (Entity) entity1;
        ERElement entity2 = result.erElements.get(2);
        Entity ent2 = (Entity) entity2;

        String entity1Cardinality = r.recoverCardinality(ent1);
        String entity2Cardinality = r.recoverCardinality(ent2);
        
        int existeRelationship = 0;
        for (ERElement ere : erElements) {
            if(ere.getName().equals(RelationshipName)){
                existeRelationship = 1;
            }
        }
        if (existeRelationship == 0 && !nomeEntidadeValorDois.equals(er1.getName())) {
            //if ((!entity1Cardinality.equals("Many") && !entity2Cardinality.equals("Many")) &&
             //       (!entity1Cardinality.equals("One") && !entity2Cardinality.equals("One")) &&
           if ((entity1Cardinality.equals("One") && entity2Cardinality.equals("Many")) ||
                 (entity1Cardinality.equals("Many") && entity2Cardinality.equals("One"))){
                for (Field f : result.getFields()) {
                    if (f instanceof SimpleField) {
                        SimpleField sf = (SimpleField) f;
                        int i = sf.getFieldMapping().toString().indexOf(".");
                        String string = sf.getFieldMapping().toString().substring(0, i);
                        if (string.equals(RelationshipName)) {
                            ERElement ere = r;
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
        }

        
        //AQUI COMEÇA LOGICA DE MAPEAMENTO 1-1
        //VALOR==1 vem do join 
        if (valor == 1) {
            //AQUI COMEÇA LOGICA VALOR == 1 JOIN MAPEAMENTO 1 - 1
            if (entity1Cardinality.equals("One") && entity2Cardinality.equals("One")) {
                String ret = "db.EC.find().forEach( function(data){\n"
                        + "     var varData = [];\n"
                        + "     data.data_Join.forEach(\n"
                        + "         function(dataCopy) {\n"
                        + "              varData.push(dataCopy);\n"
                        + "         });\n"
                        + "     db." + rfp + ".find("
                        + "{ '" + rf + "': data." + lf + " }).forEach(\n"
                        + "     function(data2) {\n"
                        + "     varData.push( { \n";

                for (ERElement ere : erElements) {
                    //inserimos dados só se são distintos da entidade 1, a entidade 1 ja existe como primeiro atributo
                    //da entidade computada             
                    if (ere != er1) {
                        ret += "        data_" + ere.getName() + ": {\n";
                        List<Pair<String, String>> fields = fieldsToProject.get(ere);
                        for (Pair<String, String> fieldName : fields) {
                            ret += "         " + fieldName.getFirst() + ": data2." + fieldName.getSecond() + ",\n";
                        }
                        ret += "      }";
                        //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                        if (erElements.size() > 1) {
                            ret += ",\n";
                        }
                    }
                }
                ret += "}\n"
                        + ")\n"
                        + "}\n"
                        + ");\n"
                        + "db.EC.update( {'" + lf + "': data." + lf + "},\n"
                        + "                 { $set: { \n"
                        + " 'data_Join': varData \n"
                        + "} } );   \n"
                        + "});";
                return ret;
            }
            //AQUI TERMINA LOGICA VALOR == 1 JOIN MAPEAMENTO 1 - 1

            //AQUI COMEÇA LOGICA VALOR == 1 JOIN MAPEAMENTO 1 - N
                //VER PARA ADICIONAR ATRIBUTOS DO RELACIONAMENTO.
            if (entity1Cardinality.equals("One") && entity2Cardinality.equals("Many")) {
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
                    
                    
                    String nameAR = "";
                    String ret = "db.EC.find().forEach( function(data){\n"
                            + "     var varData = [];\n"
                            + "     data.data_Join.forEach(\n"
                            + "         function(dataCopy) {\n"
                            + "              varData.push(dataCopy);\n"
                            + "         });\n"
                            + "     db." + rfp + ".find("
                            + "{ '" + rf + "': data." + lf + " }).forEach(\n"
                            + "     function(data1) {";
                          //  + "     varData.push( { \n";
                    List<ArrayField> listArrayField2 = new ArrayList<>();
                    listArrayField2 = result.getArrayField();
                    for (ArrayField af : listArrayField2) {
                        nameAR = af.getName();
                    }
                    ret += "\n      data1." + nameAR + ".forEach(function(data2){";
                    ret += "\n      db.EC.update( {'data_" + newFieldWhere + "': data.data_" + newFieldWhere + "},\n";
                    ret += "            { $addToSet: { \n";
                    ret += "                'data_Join': {\n";
                    //ret += "                    data_" + ere.getName() + ": {\n";
                    
                   // ret += "         " + nameAR + ":" + "data2." + nameAR + ",\n";
                    for (ERElement ere : erElements) {
                        if (!ere.getName().equals(er1.getName())) {
                            List<Pair<String, String>> fields = fieldsToProject.get(ere);
                            ret += "                        data_" + ere.getName() + ": {\n";
                            for (Pair<String, String> fieldName : fields) {
                                ret += "                            " + fieldName.getFirst() + ": data2." + fieldName.getSecond() + ",\n";
                            }
                            ret += "                        },\n";
                        }
                        /*if (erElements.size() > 1) {
                            ret += ",\n";
                        }*/
                        /*if (nItem == erElementsNew.size()) {
                            ret += "              }}";
                            ret += "\n          });\n";
                        }*/
                    }
                    ret += "                }\n"
                    + "         }});\n"
                    + "       });\n"
                            //+ "db.EC.update( {'data_" + newFieldWhere + "': data.data_" + newFieldWhere + "},\n"
                            //+ "                 { $set: { \n"
                            //+ " 'data_Join': varData \n"
                           // + "} } );   \n"
                     + "   });\n"
                     + "   });";
                    return ret;

                } else {
                    String ret = "db.EC.find().forEach( function(data){\n"
                            + "     var varData = [];\n"
                            + "     var varData2 = [];\n"
                            + "     data.data_Join.forEach(\n"
                            + "         function(dataCopy) {\n"
                            + "              varData.push(dataCopy);\n"
                            + "         });\n"
                            + "     db." + rfp + ".find("
                            + "{ '" + rf + "': data." + lf + " }).forEach(\n"
                            + "     function(data2) {\n"
                            + "     varData.push( { \n";
                    for (ERElement ere : erElements) {
                        if (ere != er1) {
                            List<Pair<String, String>> fields = fieldsToProject.get(ere);
                           // lf = "data_" + ere.getName();
                            ret += "        data_" + ere.getName() + ": {\n";
                            for (Pair<String, String> fieldName : fields) {
                                if (fieldName.getSecond().toString() == "_id") {
                                    ret += "                    " + lfArrayField + ": data2." + fieldName.getSecond() + ",\n";
                                } else {
                                    ret += "                    " + fieldName.getFirst() + ": data2." + fieldName.getSecond() + ",\n";
                                }
                            }
                            //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                            if (erElements.size() > 1) {
                               ret += "        },\n";
                            }
                             
                        }
                    }
                    ret += "});\n"
                    + "});\n"
                    //+ "varData2.push({ " + lf + ": varData});\n"
                    + "db.EC.update( {'data_" + newFieldWhere + "': data.data_" + newFieldWhere + "},\n"
                    + "                 { $set: { \n"
                    + " 'data_Join': varData \n"
                    + "} } );   \n"
                    + "});";
                    return ret;
                }
            }
            //AQUI TERMINA LOGICA VALOR == 1 JOIN MAPEAMENTO 1 - N

            //AQUI COMEÇA LOGICA VALOR == 1 JOIN  MANY TO ONE N-1
            if (entity1Cardinality.equals("Many") && entity2Cardinality.equals("One")) {
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
                    
                    String nameAR = "";
                    String identifierArrayField = " ";
                    String ret = "db.EC.find().forEach( function(data){\n"
                            + "     var varData = [];\n"
                            + "     data.data_Join.forEach(\n"
                            + "         function(dataCopy) {\n"
                            + "              varData.push(dataCopy);\n"
                            + "         });\n"
                            + "     db." + rfp + ".find().forEach(\n"
                            + "         function(data1) {";
                    List<ArrayField> listArrayField2 = new ArrayList<>();
                    listArrayField2 = result.getArrayField();
                    for (ArrayField af : listArrayField2) {
                        nameAR = af.getName();
                        if (af.getFields() instanceof SimpleField) {
                            SimpleField sf = (SimpleField) af.getFields();
                            int resultado = sf.getName().toLowerCase().indexOf("id");
                            if (resultado != -1) {
                                identifierArrayField = sf.getName();
                            }
                        }
                    }
                    ret += "\n              data1." + nameAR + ".forEach(function(data2){";
                    ret += "\n                  db.EC.update( {'data_" + newFieldWhere + "': data2." + identifierArrayField + "},\n";
                    ret += "                        { $addToSet: { \n";
                    ret += "                            'data_Join': {\n";
                    for (ERElement ere : erElements) {
                        if (!ere.getName().equals(er1.getName())) {
                            List<Pair<String, String>> fields = fieldsToProject.get(ere);
                            ret += "                              data_" + ere.getName() + ": {\n";
                            if(ere.getName().equals(RelationshipName)){
                                for (Pair<String, String> fieldName : fields) {
                                    ret += "                                   " + fieldName.getFirst() + ": data2." + fieldName.getSecond() + ",\n";
                                }
                            } else {
                                for (Pair<String, String> fieldName : fields) {
                                    ret += "                                   " + fieldName.getFirst() + ": data1." + fieldName.getSecond() + ",\n";
                                }
                            }
                            ret += "                              },\n";
                        }
                    }
                    ret += "                }\n"
                    + "         }});\n"
                    + "       });\n"
                     + "   });\n"
                     + "   });";
                    return ret;

                } else {
                
                String ret = "db.EC.find().forEach( function(data){\n"
                        + "     var varData = [];\n"
                        + "     data.data_Join.forEach(\n"
                        + "         function(dataCopy) {\n"
                        + "              varData.push(dataCopy);\n"
                        + "         });\n"
                        + "     db." + rfp + ".find("
                        + "{ '" + rfManyToOne + "': data." + lf + " }).forEach(\n"
                        + "     function(data2) {\n"
                        + "     varData.push( { \n";

                for (ERElement ere : erElements) {
                    //inserimos dados só se são distintos da entidade 1, a entidade 1 ja existe como primeiro atributo
                    //da entidade computada             
                    if (ere != er1) {
                        ret += "        data_" + ere.getName() + ": {\n";
                        List<Pair<String, String>> fields = fieldsToProject.get(ere);
                        for (Pair<String, String> fieldName : fields) {
                            ret += "         " + fieldName.getFirst() + ": data2." + fieldName.getSecond() + ",\n";
                        }
                        ret += "      }";
                        //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                        if (erElements.size() > 1) {
                            ret += ",\n";
                        }
                    }
                }
                ret += "}\n"
                        + ")\n"
                        + "}\n"
                        + ");\n"
                        + "db.EC.update( {'" + lf + "': data." + lf + "},\n"
                        + "                 { $set: { \n"
                        + " 'data_Join': varData \n"
                        + "} } );   \n"
                        + "});";
                return ret;
            }
            }
            if (entity1Cardinality.equals("Many") && entity2Cardinality.equals("Many")) {
                String ret = "db.EC.find().forEach( function(data){\n"
                        + "     var varData = [];\n"
                        + "     data.data_Join.forEach(\n"
                        + "         function(dataCopy) {\n"
                        + "              varData.push(dataCopy);\n"
                        + "         });\n"
                        + "     db." + rfp + ".find("
                        + "{ '" + rf + "': data." + lf + " }).forEach(\n"
                        + "     function(data2) {\n"
                        + "     varData.push( { \n";

                for (ERElement ere : erElements) {
                    //inserimos dados só se são distintos da entidade 1, a entidade 1 ja existe como primeiro atributo
                    //da entidade computada             
                    if (ere != er1) {
                        ret += "        data_" + ere.getName() + ": {\n";
                        List<Pair<String, String>> fields = fieldsToProject.get(ere);
                        for (Pair<String, String> fieldName : fields) {
                            ret += "         " + fieldName.getFirst() + ": data2." + fieldName.getSecond() + ",\n";
                        }
                        ret += "      }";
                        //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                        if (erElements.size() > 1) {
                            ret += ",\n";
                        }
                    }
                }
                ret += "}\n"
                        + ")\n"
                        + "}\n"
                        + ");\n"
                        + "db.EC.update( {'" + lf + "': data." + lf + "},\n"
                        + "                 { $set: { \n"
                        + " 'data_Join': varData \n"
                        + "} } );   \n"
                        + "});";
                return ret;
            }

        } else //VALOR==2 COMPLETAR ATRIBUTOS
        {
            //AQUI COMEÇA LOGICA VALOR == 2 JOIN MAPEAMENTO 1 - 1
            if (entity1Cardinality.equals("One") && entity2Cardinality.equals("One")) {
                //vai completar atributos da primeira Entidade, E1
                if (er1.getName() == nomeEntidadeValorDois) {
                    //System.out.println("TEM DE COMPLETAR ATRIBUTOS DA ENTIDADE 1");
                    String ret = "db.EC.find().forEach( function(data){\n"
                            + "     var varData = [];\n"
                            + "     db." + rfp + ".find("
                            + "{ '" + rf + "': data." + newlf + " }).forEach(\n"
                            + "     function(data2) {\n"
                            + "     varData.push( { \n";

                    for (ERElement ere : erElements) {
                        //inserimos dados só se são distintos da entidade 1, a entidade 1 ja existe como primeiro atributo da entidade computada
                        if (ere == er1) {
                            ret += "        data_" + ere.getName() + ": {\n";
                            ret += "         " + nomeAtributoIDEntidadeUM + ": data2._id" + ",\n";
                            List<Pair<String, String>> fields = fieldsToProject.get(ere);
                            for (Pair<String, String> fieldName : fields) {
                                if (fieldName.getSecond().toString() != "_id") {
                                    ret += "         " + fieldName.getFirst() + ": data2." + fieldName.getSecond() + ",\n";
                                }
                            }
                            ret += "      }";
                            //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                            if (erElements.size() > 1) {
                                ret += ",\n";
                            }
                            ret += "})}) ;\n";
                            ret += "db.EC.update( {'data_" + newFieldWhere + "': data.data_" + newFieldWhere + "},\n"
                                    + "                 { $set: { \n"
                                    + " 'data_" + ere.getName() + "': varData[0]." + "data_" + ere.getName() + "\n"
                                    + "} } ) });   \n";
                        } else {
                            //pode acontecer que a ENTIDADE1 contenha atributos de outras entidades que sao parte de query attributes
                            //tem atributos que nao sao da entidade1, mas que precisam ser completados
                            //   aqui vem de completar atributos  que NAO sao da entidade 1    
                            ret += "db.EC.find().forEach( function(data){\n"
                                    + "     var varData = [];\n"
                                    + "     data.data_Join.forEach(\n"
                                    + "         function(dataCopy) {\n"
                                    + "              varData.push(dataCopy);\n"
                                    + "         });\n"
                                    + "     db." + rfp + ".find("
                                    + "{ '" + rf + "': data.data_" + newFieldWhere + " }).forEach(\n"
                                    + "     function(data2) {\n"
                                    + "     varData.push( { \n";

                            //inserimos dados só se são distintos da entidade 1, a entidade 1 ja existe como primeiro atributo
                            //da entidade computada
                            if (ere != er1) {
                                ret += "        data_" + ere.getName() + ": {\n";
                                List<Pair<String, String>> fields = fieldsToProject.get(ere);
                                for (Pair<String, String> fieldName : fields) {
                                    ret += "         " + fieldName.getFirst() + ": data2." + fieldName.getSecond() + ",\n";
                                }
                                ret += "      }";
                                //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                                if (erElements.size() > 1) {
                                    ret += ",\n";
                                }
                                ret += "}\n"
                                        + ")\n"
                                        + "}\n"
                                        + ");\n"
                                        + "db.EC.update( {'data_" + newFieldWhere + "': data.data_" + newFieldWhere + "},\n"
                                        + "                 { $set: { \n"
                                        + " 'data_Join': varData \n"
                                        + "} } );   \n"
                                        + "});";

                            }

                        }
                    }
                    return ret;
                } else {
                    //   aqui completa atributos  que NÃO são da ENTIDADE 1    
                    String ret = "db.EC.find().forEach( function(data){\n"
                            + "     var novoArray = [];\n"
                            + "     data.data_Join.forEach(function(data2) {\n"
                            + "     data2." + lf + " = "
                            + "db." + rfp + ".findOne("
                            + "{ '" + rf + "': data2." + newlf + " });\n"
                            + "     novoArray.push(data2);"
                            + "\n });";

                    ret += "\ndb.EC.update( {'data_" + newFieldWhere + "': data.data_" + newFieldWhere + "},\n"
                            + "                 { $set: { \n"
                            + " 'data_Join': novoArray \n"
                            + "} } );   \n"
                            + "});";
                    return ret;
                }
            } //AQUI TERMINA LOGICA VALOR == 2 JOIN MAPEAMENTO 1 - 1

            //AQUI COMEÇA LOGICA VALOR == 2 JOIN MAPEAMENTO 1 - N
            if (entity1Cardinality.equals("One") && entity2Cardinality.equals("Many")) {               
                if (result.getArrayField().size() > 0 && (!er1.getName().equals(nomeEntidadeValorDois))) {
                    String ret = "db.EC.find().forEach( function(data){\n"
                            + "    var varData = [];\n"
                            + "    data.data_Join.forEach(function(data1) {\n"
                            + "            db." + rfp + ".find("
                            + "{ '" + rf + "': data1.";
                            String nameArrayField2 = " ";
                            List<ArrayField> listArrayField2 = new ArrayList<>();
                            listArrayField2 = result.getArrayField();
                            for (ArrayField af : listArrayField2) {
                                nameArrayField2 = af.getName();
                            }
                            ret += nameArrayField2 + ".";
                            ret += lfArrayField + "}).forEach(function(data2) {\n"
                            + "           varData.push( { \n";
                            
                        for (ERElement ere : erElements) {
                            ret += "                data_" + ere.getName() + ": {\n";
                            List<Pair<String, String>> fields = fieldsToProject.get(ere);
                            for (Pair<String, String> fieldName : fields) {
                                if (fieldName.getSecond().toString() == "_id") {
                                    if(!ere.getName().equals(RelationshipName)){
                                        ret += "                        " + lfArrayField + ": data2." + fieldName.getSecond() + ",\n";
                                    }
                                } else {
                                    if(!ere.getName().equals(RelationshipName)){
                                        ret += "                        " + fieldName.getFirst() + ": data2." + fieldName.getSecond() + ",\n";
                                    }
                                }    
                            }
                    }
                    ret += "                },\n";
                    ret += "                data_" + RelationshipName + ": {\n";
                    List<ArrayField> listArrayField = new ArrayList<>();
                    listArrayField = result.getArrayField();
                    for (ArrayField af : listArrayField) {
                        if (af.getFields() instanceof SimpleField){
                            SimpleField sf = (SimpleField) af.getFields();
                            if(sf.getFieldMapping().getAttribute().getParent().getName().equals(RelationshipName)){
                                 ret += "                        " + sf.getName()  + ": data1." + "data_" + RelationshipName + "." +  sf.getName() + ",\n";
                            }
                        }
                    }
                    ret += "                }\n"
                    + "       })\n"
                    + "   });\n"
                    + "});\n"
                    + "db.EC.update( {'data_" + newFieldWhere + "': data.data_" + newFieldWhere + "},\n"
                    + "{ $set: { \n"
                    + "         'data_Join': varData\n"
                    + "} } );   \n"
                    + "});";
                    return ret;
                } else //vai completar atributos da primeira Entidade, E1
                if (er1.getName() == nomeEntidadeValorDois) {
                    //System.out.println("TEM DE COMPLETAR ATRIBUTOS DA ENTIDADE 1");
                    String ret = "db.EC.find().forEach( function(data){\n"
                            + "     var varData = [];\n"
                            + "     db." + rfp + ".find("
                            + "{ '" + rf + "': data." + newlf + " }).forEach(\n"
                            + "     function(data2) {\n"
                            + "     varData.push( { \n";

                    for (ERElement ere : erElements) {
                        //inserimos dados só se são distintos da entidade 1, a entidade 1 ja existe como primeiro atributo da entidade computada
                        if (ere == er1) {
                            ret += "        data_" + ere.getName() + ": {\n";
                            ret += "         " + nomeAtributoIDEntidadeUM + ": data2._id" + ",\n";
                            List<Pair<String, String>> fields = fieldsToProject.get(ere);
                            for (Pair<String, String> fieldName : fields) {
                                if (fieldName.getSecond().toString() != "_id") {
                                    ret += "         " + fieldName.getFirst() + ": data2." + fieldName.getSecond() + ",\n";
                                }
                            }
                            ret += "      }";
                            //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                            if (erElements.size() > 1) {
                                ret += ",\n";
                            }
                            ret += "})}) ;\n";
                            ret += "db.EC.updateMany( {'data_" + newFieldWhere + "': data.data_" + newFieldWhere + "},\n"
                                    + "                 { $set: { \n"
                                    + " 'data_" + ere.getName() + "': varData[0]." + "data_" + ere.getName() + "\n"
                                    + "} } ) });   \n";
                            return ret;
                        } else {
                            //pode acontecer que a ENTIDADE1 contenha atributos de outras entidades que sao parte de query attributes
                            //tem atributos que nao sao da entidade1, mas que precisam ser completados
                            //   aqui vem de completar atributos  que NAO sao da entidade 1    
                            ret += "db.EC.find().forEach( function(data){\n"
                                    + "     var varData = [];\n"
                                    + "     data.data_Join.forEach(\n"
                                    + "         function(dataCopy) {\n"
                                    + "              varData.push(dataCopy);\n"
                                    + "         });\n"
                                    + "     db." + rfp + ".find("
                                    + "{ '" + rf + "': data.data_" + newFieldWhere + " }).forEach(\n"
                                    + "     function(data2) {\n"
                                    + "     varData.push( { \n";

                            //inserimos dados só se são distintos da entidade 1, a entidade 1 ja existe como primeiro atributo
                            //da entidade computada
                            if (ere != er1) {
                                ret += "        data_" + ere.getName() + ": {\n";
                                List<Pair<String, String>> fields = fieldsToProject.get(ere);
                                for (Pair<String, String> fieldName : fields) {
                                    ret += "         " + fieldName.getFirst() + ": data2." + fieldName.getSecond() + ",\n";
                                }
                                ret += "      }";
                                //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                                if (erElements.size() > 1) {
                                    ret += ",\n";
                                }
                                ret += "}\n"
                                        + ")\n"
                                        + "}\n"
                                        + ");\n"
                                        + "db.EC.update( {'data_" + newFieldWhere + "': data.data_" + newFieldWhere + "},\n"
                                        + "                 { $set: { \n"
                                        + " 'data_Join': varData \n"
                                        + "} } );   \n"
                                        + "});";
                            }
                            return ret;
                        }
                    }
                    //return ret;
                } else {
                    //   aqui completa atributos  que NÃO são da ENTIDADE 1    
                    String ret = "db.EC.find().forEach( function(data){\n"
                            + "     var novoArray = [];\n"
                            + "     data.data_Join.forEach(function(data2) {\n"
                            + "     data2." + lf + " = "
                            + "db." + rfp + ".findOne("
                            + "{ '" + rf + "': data2." + newlf + " });\n"
                            + "     novoArray.push(data2);"
                            + "\n });";

                    ret += "db.EC.update( {'data_" + newFieldWhere + "': data.data_" + newFieldWhere + "},\n"
                            + "                 { $set: { \n"
                            + " 'data_Join': novoArray \n"
                            + "} } );   \n"
                            + "});";
                    return ret;
                }
            }
            if (entity1Cardinality.equals("Many") && entity2Cardinality.equals("One")) {
                //vai completar atributos da primeira Entidade, E1
                if (er1.getName() == nomeEntidadeValorDois) {
                    //System.out.println("TEM DE COMPLETAR ATRIBUTOS DA ENTIDADE 1");
                    String ret = "db.EC.find().forEach( function(data){\n"
                            + "     var varData = [];\n"
                            + "     db." + rfp + ".find("
                            + "{ '" + rf + "': data." + newlf + " }).forEach(\n"
                            + "     function(data2) {\n"
                            + "     varData.push( { \n";

                    for (ERElement ere : erElements) {
                        //inserimos dados só se são distintos da entidade 1, a entidade 1 ja existe como primeiro atributo da entidade computada
                        if (ere == er1) {
                            ret += "        data_" + ere.getName() + ": {\n";
                            ret += "         " + nomeAtributoIDEntidadeUM + ": data2._id" + ",\n";
                            List<Pair<String, String>> fields = fieldsToProject.get(ere);
                            for (Pair<String, String> fieldName : fields) {
                                if (fieldName.getSecond().toString() != "_id") {
                                    ret += "         " + fieldName.getFirst() + ": data2." + fieldName.getSecond() + ",\n";
                                }
                            }
                            ret += "      }";
                            //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                            if (erElements.size() > 1) {
                                ret += ",\n";
                            }
                            ret += "})}) ;\n";
                            ret += "db.EC.update( {'" + newlf + "': data." + newlf + "},\n"
                                    + "                 { $set: { \n"
                                    + " 'data_" + ere.getName() + "': varData[0]." + "data_" + ere.getName() + "\n"
                                    + "} } ) });   \n";
                        } else {
                            //pode acontecer que a ENTIDADE1 contenha atributos de outras entidades que sao parte de query attributes
                            //tem atributos que nao sao da entidade1, mas que precisam ser completados
                            //   aqui vem de completar atributos  que NAO sao da entidade 1    
                            ret += "db.EC.find().forEach( function(data){\n"
                                    + "     var varData = [];\n"
                                    + "     data.data_Join.forEach(\n"
                                    + "         function(dataCopy) {\n"
                                    + "              varData.push(dataCopy);\n"
                                    + "         });\n"
                                    + "     db." + rfp + ".find("
                                    + "{ '" + rf + "': data.data_" + newFieldWhere + " }).forEach(\n"
                                    + "     function(data2) {\n"
                                    + "     varData.push( { \n";

                            //inserimos dados só se são distintos da entidade 1, a entidade 1 ja existe como primeiro atributo
                            //da entidade computada
                            if (ere != er1) {
                                ret += "        data_" + ere.getName() + ": {\n";
                                List<Pair<String, String>> fields = fieldsToProject.get(ere);
                                for (Pair<String, String> fieldName : fields) {
                                    ret += "         " + fieldName.getFirst() + ": data2." + fieldName.getSecond() + ",\n";
                                }
                                ret += "      }";
                                //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                                if (erElements.size() > 1) {
                                    ret += ",\n";
                                }
                                ret += "}\n"
                                        + ")\n"
                                        + "}\n"
                                        + ");\n"
                                        + "db.EC.update( {'data_" + newFieldWhere + "': data.data_" + newFieldWhere + "},\n"
                                        + "                 { $set: { \n"
                                        + " 'data_Join': varData \n"
                                        + "} } );   \n"
                                        + "});";

                            }

                        }
                    }
                    return ret;
                } else {
                    //   aqui completa atributos  que NÃO são da ENTIDADE 1    
                    String ret = "db.EC.find().forEach( function(data){\n"
                            + "     var novoArray = [];\n"
                            + "     data.data_Join.forEach(function(data2) {\n"
                            + "     data2." + lf + " = "
                            + "db." + rfp + ".findOne("
                            + "{ '" + rf + "': data2." + newlf + " });\n"
                            + "     novoArray.push(data2);"
                            + "\n });";

                    ret += "\ndb.EC.update( {'data_" + newFieldWhere + "': data.data_" + newFieldWhere + "},\n"
                            + "                 { $set: { \n"
                            + " 'data_Join': novoArray \n"
                            + "} } );   \n"
                            + "});";
                    return ret;
                }
            }
            if (entity1Cardinality.equals("Many") && entity2Cardinality.equals("Many")) {
                //vai completar atributos da primeira Entidade, E1
                if (er1.getName() == nomeEntidadeValorDois) {
                    //System.out.println("TEM DE COMPLETAR ATRIBUTOS DA ENTIDADE 1");
                    String ret = "db.EC.find().forEach( function(data){\n"
                            + "     var varData = [];\n"
                            + "     db." + rfp + ".find("
                            + "{ '" + rf + "': data." + newlf + " }).forEach(\n"
                            + "     function(data2) {\n"
                            + "     varData.push( { \n";

                    for (ERElement ere : erElements) {
                        //inserimos dados só se são distintos da entidade 1, a entidade 1 ja existe como primeiro atributo da entidade computada
                        if (ere == er1) {
                            ret += "        data_" + ere.getName() + ": {\n";
                            ret += "         " + nomeAtributoIDEntidadeUM + ": data2._id" + ",\n";
                            List<Pair<String, String>> fields = fieldsToProject.get(ere);
                            for (Pair<String, String> fieldName : fields) {
                                if (fieldName.getSecond().toString() != "_id") {
                                    ret += "         " + fieldName.getFirst() + ": data2." + fieldName.getSecond() + ",\n";
                                }
                            }
                            ret += "      }";
                            //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                            if (erElements.size() > 1) {
                                ret += ",\n";
                            }
                            ret += "})}) ;\n";
                            ret += "db.EC.update( {'data_" + newFieldWhere + "': data.data_" + newFieldWhere + "},\n"
                                    + "                 { $set: { \n"
                                    + " 'data_" + ere.getName() + "': varData[0]." + "data_" + ere.getName() + "\n"
                                    + "} } ) });   \n";
                        } else {
                            //pode acontecer que a ENTIDADE1 contenha atributos de outras entidades que sao parte de query attributes
                            //tem atributos que nao sao da entidade1, mas que precisam ser completados
                            //   aqui vem de completar atributos  que NAO sao da entidade 1    
                            ret += "db.EC.find().forEach( function(data){\n"
                                    + "     var varData = [];\n"
                                    + "     data.data_Join.forEach(\n"
                                    + "         function(dataCopy) {\n"
                                    + "              varData.push(dataCopy);\n"
                                    + "         });\n"
                                    + "     db." + rfp + ".find("
                                    + "{ '" + rf + "': data.data_" + newFieldWhere + " }).forEach(\n"
                                    + "     function(data2) {\n"
                                    + "     varData.push( { \n";

                            //inserimos dados só se são distintos da entidade 1, a entidade 1 ja existe como primeiro atributo
                            //da entidade computada
                            if (ere != er1) {
                                ret += "        data_" + ere.getName() + ": {\n";
                                List<Pair<String, String>> fields = fieldsToProject.get(ere);
                                for (Pair<String, String> fieldName : fields) {
                                    ret += "         " + fieldName.getFirst() + ": data2." + fieldName.getSecond() + ",\n";
                                }
                                ret += "      }";
                                //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                                if (erElements.size() > 1) {
                                    ret += ",\n";
                                }
                                ret += "}\n"
                                        + ")\n"
                                        + "}\n"
                                        + ");\n"
                                        + "db.EC.update( {'data_" + newFieldWhere + "': data.data_" + newFieldWhere + "},\n"
                                        + "                 { $set: { \n"
                                        + " 'data_Join': varData \n"
                                        + "} } );   \n"
                                        + "});";

                            }

                        }
                    }
                    return ret;
                } else {
                    if(result.getArrayField().size()>0){
                        String ret = "db.EC.find().forEach( function(data){\n"
                                + "    var varData = [];\n"
                                + "    var varData2 = [];\n"
                                + "    data.data_Join.forEach(\n"
                                + "      function(data1) {\n"
                                + "        data1." + lf + ".forEach(\n"
                                + "        function(data2){\n"
                                + "            db." + rfp + ".find("
                                + "{ '" + rf + "': data2." + lfArrayField + " }).forEach(\n"
                                + "                function(data3) {\n"
                                + "                varData.push( { \n";
                        for (ERElement ere : erElements) {
                            List<Pair<String, String>> fields = fieldsToProject.get(ere);
                            for (Pair<String, String> fieldName : fields) {
                                if (fieldName.getSecond().toString() == "_id") {
                                    if (!ere.getName().equals(RelationshipName)) {
                                        ret += "                    " + lfArrayField + ": data3." + fieldName.getSecond() + ",\n";
                                    }
                                } else if (!ere.getName().equals(RelationshipName)) {
                                    ret += "                    " + fieldName.getFirst() + ": data3." + fieldName.getSecond() + ",\n";
                                }
                            }
                            //se tiver mais de um elemento no erElements, adicionar virgula(foi adicionado porque no mongo da erro se nao tiver)
                            /*if (erElements.size() > 1) {
                            ret += ",\n";
                        }*/
                        }
                        ret += "           })\n"
                                + "       });\n"
                                + "   });\n"
                                // + "});\n"
                                //   + "varData2.push({ " + lf + ": varData});\n"
                                + "varData2.push({ \n";
                        for (ERElement ere : erElements) {
                            List<Pair<String, String>> fields = fieldsToProject.get(ere);
                            for (Pair<String, String> fieldName : fields) {
                                if (ere.getName().equals(RelationshipName)) {
                                    ret += "        data_" + ere.getName() + ": {\n";
                                    ret += "         " + fieldName.getFirst() + ": data1." + "data_" + ere.getName() + "." + fieldName.getSecond() + ",\n";
                                }
                            }
                        }
                        if (erElements.size() > 1) {
                            ret += "},\n";
                        }
                        //ret+= "         },\n"
                        ret += "         " + lf + ": varData});\n"
                                + "});\n"
                                + "db.EC.update( {'data_" + newFieldWhere + "': data.data_" + newFieldWhere + "},\n"
                                + "                 { $set: { \n"
                                + " 'data_Join': varData2 \n"
                                + "} } );   \n"
                                + "});";
                        return ret;
                    } else {

                        //   aqui completa atributos  que NÃO são da ENTIDADE 1    
                        String ret = "db.EC.find().forEach( function(data){\n"
                                + "     var novoArray = [];\n"
                                + "     data.data_Join.forEach(function(data2) {\n"
                                + "     data2." + lf + " = "
                                + "db." + rfp + ".findOne("
                                + "{ '" + rf + "': data2." + newlf + " });\n"
                                + "     novoArray.push(data2);"
                                + "\n });";

                        ret += "\ndb.EC.update( {'data_" + newFieldWhere + "': data.data_" + newFieldWhere + "},\n"
                                + "                 { $set: { \n"
                                + " 'data_Join': novoArray \n"
                                + "} } );   \n"
                                + "});";
                        return ret;
                    }
                }
            }
        }
        return null;
    }
}
