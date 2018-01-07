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
import querygenerator.ermodel.ERElement;
import querygenerator.ermodel.Entity;
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

    //Em Field dentro do fields, sempre vai ser um campo simples, vamos verificar se é embutido verificando o pai
    public JoinOperation(Pair<List<Pair<Field, DocumentType>>, List<Pair<Field, DocumentType>>> fields, String text, ComputedEntity result, int valor) {
        super(text, result);
        this.fields = fields;
        this.valor = valor;
    }

    @Override
    public String generateOperation() {

        String lf = " ";
        String lfp = " ";
        String rf = " ";
        String rfp = " ";
        String newlf = " ";
        String nomeEntidadeValorDois = " ";
        String nomeAtributoIDEntidadeUM = " ";
        String newFieldWhere = " ";
        //Recuperamos Entidade1
        ERElement er1 = result.erElements.get(0);

        String nameAtribute = " ";
        String nameDocument = " ";
        String nameEntity = " ";
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
        newFieldWhere = nameEntity + "." + nameAtribute;

        //aqui verificamos os rigth field e left field para construir correctamente consulta javascript
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
            }
        }
        for (Pair<Field, DocumentType> p : fields.getSecond()) {
            Field fFirst = p.getFirst();
            if (p.getFirst() instanceof SimpleField) {
                SimpleField f = (SimpleField) fFirst;
                DocumentType dt = p.getSecond();
                //verificar pai, pra saber se é embutido ou nao, se não tem mesmo pai, é porque tem dados embutidos
                if (f.getParent().getName() != dt.getName()) {
                    rf = "data_" + f.getFieldMapping().getAttribute().getParent().getName() + "." + fFirst.getName();
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

        //VERIFICAMOS VALOR
        //VALOR==1 vem do join mesmo
        if (valor == 1) {
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
        } else //VALOR==2
        {
            //vai completar atributos da primeira Entidade, E1
            if (er1.getName() == nomeEntidadeValorDois) {
                //System.out.println("TEM DE COMPLETAR ATRIBUTOS DA ENTIDADE 1");

                String ret = "db.EC.find().forEach( function(data){\n"
                        + "     var varData = [];\n"
                        + "     db." + rfp + ".find("
                        + "{ '" + rf + "': data." + newlf + " }).forEach(\n"
                        + "     function(data2) {\n"
                        + "     varData.push( { \n";

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

                //   aqui vem de completar atributos  que NAO sao da ENTIDADE 1    
                String ret = "db.EC.find().forEach( function(data){\n"
                        + "     var novoArray = [];\n"
                        + "     data.data_Join.forEach(function(data2) {\n"
                        + "     data2." + lf + " = "
                        + "db." + rfp + ".findOne("
                        + "{ '" + rf + "': data2." + newlf + " });\n"
                        + "     novoArray.push(data2);"
                        + "\n });";

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
                ret += "db.EC.update( {'data_" + newFieldWhere + "': data.data_" + newFieldWhere + "},\n"
                        + "                 { $set: { \n"
                        + " 'data_Join': novoArray \n"
                        + "} } );   \n"
                        + "});";
                return ret;
            }
        }
    }
}
