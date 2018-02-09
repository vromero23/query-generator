/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import querygenerator.ermodel.Attribute;
import querygenerator.ermodel.ERElement;
import querygenerator.ermodel.Entity;
import querygenerator.ermodel.Relationship;
import querygenerator.mapping.MappingModel;
import querygenerator.mongoschema.ArrayField;
import querygenerator.mongoschema.DocumentType;
import querygenerator.mongoschema.EmbeddedField;
import querygenerator.mongoschema.Field;
import querygenerator.mongoschema.SimpleField;

/**
 *
 * @author daniellucredio
 */
public class MainAlgorithm {

    MappingModel mappingModel;

    public MainAlgorithm(MappingModel mappingModel) {
        this.mappingModel = mappingModel;
    }

    public List<Query> binaryJoin(Entity e1, Relationship r, Entity e2, List<Attribute> queryAttributes) {
        List<Query> ret = new ArrayList<>();

        if (!(e1 == r.getRelationshipEnds().get(0).getEntity()
                && e2 == r.getRelationshipEnds().get(1).getEntity())
                || (e1 == r.getRelationshipEnds().get(1).getEntity()
                && e2 == r.getRelationshipEnds().get(0).getEntity())) {
            throw new RuntimeException("As entidades do join ("
                    + e1.getName() + ", " + e2.getName() + ")"
                    + " não estão relacionadas por meio de "
                    + " " + r.getName()
            );
        }
        // TODO: verificar se os atributos de "queryAttributes" 
        // realmente pertencem a e1, r ou e2
        //aqui verificamos a que documentos estão mapeados as entidades e o relacionamento?? certo!      
        List<DocumentType> docTypesE1 = mappingModel.getMongoSchema().findDocumentTypes(e1);
        List<DocumentType> docTypesR = mappingModel.getMongoSchema().findDocumentTypes(r);
        List<DocumentType> docTypesE2 = mappingModel.getMongoSchema().findDocumentTypes(e2);

        if (docTypesE1.isEmpty()) {
            throw new RuntimeException("Impossível fazer consulta pois " + e1.getName() + " não tem documento mapeado!");
        }
        //if (docTypesR.isEmpty()) {
        //    throw new RuntimeException("Impossível fazer consulta pois " + r.getName() + " não tem documento mapeado!");
        //}
        if (docTypesE2.isEmpty()) {
            throw new RuntimeException("Impossível fazer consulta pois " + e2.getName() + " não tem documento mapeado!");
        }   
        if(docTypesR.size()==0){
            for (DocumentType dt1 : docTypesE1) {
                //for (DocumentType dtr : docTypesR) {
                    for (DocumentType dt2 : docTypesE2) {
                        List<ERElement> erElements = new ArrayList<ERElement>();
                        erElements.add(e1);
                        erElements.add(r);
                        erElements.add(e2);

                        List<DocumentType> documentTypes = new ArrayList<DocumentType>();
                        documentTypes.add(dt1);
                        //documentTypes.add(dtr);
                        documentTypes.add(dt2);

                        ComputedEntity ce = new ComputedEntity();

                        if(dt1.getArrayField().size()>0){
                                List<ArrayField> listArrayField = new ArrayList<>();
                                listArrayField = dt1.getArrayField();
                                ce = ComputedEntity.createNew(
                                    dt1.findERMapping(e1).isMain(),
                                    e1.getName()
                                    + "-" + r.getName()
                                    + "-" + e2.getName(),
                                    erElements,
                                    documentTypes,
                                    listArrayField
                            );                             
                        } else 
                        {
                                 ce = ComputedEntity.createNew(
                                    dt1.findERMapping(e1).isMain(),
                                    e1.getName()
                                    + "-" + r.getName()
                                    + "-" + e2.getName(),
                                    erElements,
                                    documentTypes
                            );
                        }
                        
                        Query q = new Query();
                        q.addOperation(new StartOperation("Start, primeira EC não vai ter atributos!!", ce));

                        findDocTypeOperation(q, dt1,r);
                        completeAttributesOperations(q, e1, queryAttributes, r);

                        //joinTwoEntitiesOperation(q, e1, r, q.getCopyOfLastComputedEntity(), dtr);
                        //completeAttributesOperations(q, r, queryAttributes);
                        

                        joinTwoEntitiesOperation(q, e1, e2, q.getCopyOfLastComputedEntity(), dt2, r);
                        completeAttributesOperations(q, e2, queryAttributes,r);

                        if (!ret.contains(q)) {
                            ret.add(q);
                        }
                    }
            }
            return ret;
        } else 
        {
            for (DocumentType dt1 : docTypesE1) {
                for (DocumentType dtr : docTypesR) {
                    for (DocumentType dt2 : docTypesE2) {
                        List<ERElement> erElements = new ArrayList<ERElement>();
                        erElements.add(e1);
                        erElements.add(r);
                        erElements.add(e2);

                        List<DocumentType> documentTypes = new ArrayList<DocumentType>();
                        documentTypes.add(dt1);
                        documentTypes.add(dtr);
                        documentTypes.add(dt2);
                        
                        ComputedEntity ce = new ComputedEntity();

                        if(dt1.getArrayField().size()>0){
                                List<ArrayField> listArrayField = new ArrayList<>();
                                listArrayField = dt1.getArrayField();
                                ce = ComputedEntity.createNew(
                                    dt1.findERMapping(e1).isMain(),
                                    e1.getName()
                                    + "-" + r.getName()
                                    + "-" + e2.getName(),
                                    erElements,
                                    documentTypes,
                                    listArrayField
                            );                             
                        } else 
                        {
                                 ce = ComputedEntity.createNew(
                                    dt1.findERMapping(e1).isMain(),
                                    e1.getName()
                                    + "-" + r.getName()
                                    + "-" + e2.getName(),
                                    erElements,
                                    documentTypes
                            );
                        }
                        
                        Query q = new Query();
                        q.addOperation(new StartOperation("Start, primeira EC não vai ter atributos!!", ce));
                        
                        findDocTypeOperation(q, dt1,r);                        
                        completeAttributesOperations(q, e1, queryAttributes,r);
                        
                        joinTwoEntitiesOperation(q, e1, r, q.getCopyOfLastComputedEntity(), dtr, r);                        
                        completeAttributesOperations(q, r, queryAttributes,r);

                        joinTwoEntitiesOperation(q, e1, e2, q.getCopyOfLastComputedEntity(), dt2, r);                   
                        completeAttributesOperations(q, e2, queryAttributes,r);

                        if (!ret.contains(q)) {
                            ret.add(q);
                        }
                    }
                }
            }
            return ret;
        }
    }

    private void findDocTypeOperation(Query q, DocumentType dt, Relationship r) {
        ComputedEntity ceResult = q.getCopyOfLastComputedEntity();
        if (ceResult == null) {
            return;

        }
        for (Field f : dt.getFields()) {
            if (f instanceof SimpleField) {
                SimpleField sf = (SimpleField) f;
                if (sf.getFieldMapping() != null
                        && !ceResult.containsMappedField(sf.getFieldMapping().getAttribute())) {
                    ceResult.addNewField(sf);
                }
            } else if (f instanceof EmbeddedField) {
                EmbeddedField ef = (EmbeddedField) f;
                ceResult.addNewField(ef); 
            }
        }
        if (dt.getArrayField().size()>0){
            List<ArrayField> listArrayField = new ArrayList<>();
            listArrayField = dt.getArrayField();
                for(ArrayField af: listArrayField){
                    String name = af.getName();
                    if (af.getFields() instanceof SimpleField) {
                        SimpleField sf = (SimpleField) af.getFields();
                        if (sf.getFieldMapping() != null
                                && !ceResult.containsMappedField(sf.getFieldMapping().getAttribute())) {
                            ArrayField afield = new ArrayField(name, sf);
                            
                            List<ArrayField> ceResultListArrayField = new ArrayList<>();
                            ceResultListArrayField = ceResult.getArrayField();
                            for(ArrayField afceResult: ceResultListArrayField){
                                if (afceResult.getName() != afield.getName()) {
                                    ceResult.addArrayField(afield);
                                }
                            }
                        }
                    } 
                }
        }
        q.addOperation(new FindOperation(dt, "find(" + dt.getName() + ")", ceResult,r));
       
    }

    private void joinTwoEntitiesOperation(Query q, ERElement e1, ERElement e2, ComputedEntity ce, DocumentType dt2, Relationship r) {
        if (ce == null) {
            q.addOperation(new ImpossibleOperation("impossível pois não há entidade computada válida!"));
        } else {
            if (ce.containsMappedERElement(e2)) {
                return;
            }

            if (ce.containsMappedERElementAsEmbeddedField(e2)) {
                return;
            }
            if (ce.getArrayField().size() > 0) {
                List<ArrayField> listArrayField = new ArrayList<>();
                listArrayField = ce.getArrayField();
                for (ArrayField af : listArrayField) {
                    if (af.getFields() instanceof SimpleField) {
                        SimpleField sf = (SimpleField) af.getFields();
                        if (sf.getFieldMapping() != null && sf.getFieldMapping().getAttribute().getParent().getName() == e2.getName()) {
                            return;
                        }
                    }
                }
                return;
            }

            ComputedEntity ceResult = ComputedEntity.createCopy(ce);        
            Pair<List<Pair<Field, DocumentType>>, List<Pair<Field, DocumentType>>> pairOfFields = findCommonIdFields(e1, e2, ceResult, dt2);
            if (pairOfFields.getFirst().size() == 0 || pairOfFields.getSecond().size() == 0) {
                q.addOperation(new ImpossibleOperation("joinTwoEntitiesOperation impossível join entre "
                        + e1.getName()
                        + " e "
                        + e2.getName()
                        + " via "
                        + ceResult.getName()
                        + " e "
                        + dt2.getName()
                        + ", pois não há atributos id comuns entre os document types"));
            } else if (!dt2.findERMapping(e2).isMain()) {
                q.addOperation(new ImpossibleOperation("impossível join entre "
                        + e1.getName()
                        + " e "
                        + e2.getName()
                        + " via "
                        + ceResult.getName()
                        + " e "
                        + dt2.getName()
                        + ", pois o mapeamento de "
                        + e2.getName()
                        + " em "
                        + dt2.getName()
                        + " não é principal"));
            } else {
                for (Field f : dt2.getFields()) {
                    if (f instanceof SimpleField) {
                        SimpleField sf = (SimpleField) f;
                        if (sf.getFieldMapping() != null
                                && !ceResult.containsMappedNewField(sf.getFieldMapping().getAttribute())) {
                            ceResult.addNewField(sf);
                        }
                    } else if (f instanceof EmbeddedField) {
                        EmbeddedField ef = (EmbeddedField) f;
                        //verificamos se campo embutido ja existe no ceResult,se não existe inserimos.
                        if (!ceResult.containsMappedEmbeddedField(ef)) {
                            ceResult.addNewField(ef);
                        }
                    }
                }
                if (dt2.getArrayField().size() > 0) {
                    List<ArrayField> listArrayField = new ArrayList<>();
                    listArrayField = dt2.getArrayField();
                    for (ArrayField af : listArrayField) {
                        String name = af.getName();
                        if (af.getFields() instanceof SimpleField) {
                            SimpleField sf = (SimpleField) af.getFields();
                            // if (sf.getFieldMapping() != null
                            //        && !ceResult.containsMappedField(sf.getFieldMapping().getAttribute())) {
                            ArrayField afield = new ArrayField(name, sf);
                            if (ce.getArrayField().size() > 0) {
                                for (ArrayField afceResult : ce.getArrayField()) {
                                    if (afceResult.getFields() instanceof SimpleField) {
                                        SimpleField sfCeResult = (SimpleField) afceResult.getFields();
                                        if (sf.getFieldMapping().getAttribute() != sfCeResult.getFieldMapping().getAttribute()) {
                                            ceResult.addArrayField(afield);
                                        }
                                    }
                                }
                            } else {
                                ceResult.addArrayField(afield);
                            }
                            //}
                        }
                    }
                }
                //pra realizar a junção os dois Field tem de ter mesmo pai, ou seja ambos ids tem de estar mapeados pra mesma Entidade
                Field fFirst = null;
                Field fSecond = null;
                for (Pair<Field, DocumentType> pf : pairOfFields.getFirst()) {
                    fFirst = pf.getFirst();
                    for (Pair<Field, DocumentType> ps : pairOfFields.getSecond()) {
                        fSecond = ps.getFirst();
                        SimpleField fA = (SimpleField) fFirst;
                        SimpleField fB = (SimpleField) fSecond;
                        if (fA.getFieldMapping().getAttribute().getParent() == fB.getFieldMapping().getAttribute().getParent()) {
                            q.addOperation(new JoinOperation(pairOfFields,
                                    "joinTwoEntitiesOperation join entre " + e1.getName()
                                    + " e "
                                    + e2.getName()
                                    + " via "
                                    + ceResult.getName()
                                    + " e "
                                    + dt2.getName(), ceResult, 1, r));
                        }
                    }
                }
            }
        }
    }

    private Pair<List<Pair<Field, DocumentType>>, List<Pair<Field, DocumentType>>> findCommonIdFields(
            ERElement er1,
            ERElement er2,
            ComputedEntity ce,
            DocumentType dt2) {

        if (ce == null) {
            return null;
        }

        /*
        mappedIdFields1/2: contem List<Pair<Field,DocumentType>> , onde Field sempre vai ser SimpleField e
        DocumentType indicará que ele é o pai de Field.
        Para saber que o Field é um EmbeddedField, precisaremos verificar mais pra frente se o pai do SimpleField
        é igual a DocumentType. Se não é igual, é porque é um campo embutido
        
         */
        Pair<List<Pair<Field, DocumentType>>, List<Pair<Field, DocumentType>>> ret = null;

        List<Pair<Field, DocumentType>> mappedIdFields1 = new ArrayList<>();
        List<Pair<Field, DocumentType>> mappedIdFields2 = new ArrayList<>();

        for (Field f : ce.getFields()) {
            if (f instanceof SimpleField) {
                SimpleField sf = (SimpleField) f;
                if (sf.getFieldMapping() != null) {
                    if (sf.getFieldMapping().getAttribute().isIdentifier()) {
                        if (sf.getFieldMapping().getAttribute().getParent() == er1
                                || sf.getFieldMapping().getAttribute().getParent() == er2) {
                            mappedIdFields1.add(new Pair<>(sf, sf.getParent()));
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
                                if (sf.getFieldMapping().getAttribute().getParent() == er1
                                        || sf.getFieldMapping().getAttribute().getParent() == er2) {
                                    mappedIdFields1.add(new Pair<>(sf, emf.getParent()));
                                }
                            }
                        }
                    }
                }
            }
        }
        if (ce.getArrayField().size() > 0) {
            List<ArrayField> listArrayField = new ArrayList<>();
            listArrayField = ce.getArrayField();
            for (ArrayField af : listArrayField) {
                if (af.getFields() instanceof SimpleField) {
                    SimpleField sf = (SimpleField) af.getFields();
                    if (sf.getFieldMapping() != null) {
                        if (sf.getFieldMapping().getAttribute().isIdentifier()) {
                            if (sf.getFieldMapping().getAttribute().getParent() == er1
                                    || sf.getFieldMapping().getAttribute().getParent() == er2) {
                                if (ce.getName() != sf.getParent().getName()) {
                                    mappedIdFields1.add(new Pair<>(sf, dt2));
                                } else {
                                    mappedIdFields1.add(new Pair<>(sf, sf.getParent()));
                                }
                            }
                        }
                    }
                }
            }
        }
  
        
        for (Field f : dt2.getFields()) {
            if (f instanceof SimpleField) {
                SimpleField sf = (SimpleField) f;
                if (sf.getFieldMapping() != null) {
                    if (sf.getFieldMapping().getAttribute().isIdentifier()) {
                        if (sf.getFieldMapping().getAttribute().getParent() == er1
                                || sf.getFieldMapping().getAttribute().getParent() == er2) {
                            mappedIdFields2.add(new Pair<>(sf, sf.getParent()));
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
                                if (sf.getFieldMapping().getAttribute().getParent() == er1
                                        || sf.getFieldMapping().getAttribute().getParent() == er2) {
                                    //sf.setName(emf.getName() + "." + sf.getName());
                                    mappedIdFields2.add(new Pair<>(sf, emf.getParent()));
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (dt2.getArrayField().size() > 0) {
            List<ArrayField> listArrayField = new ArrayList<>();
            listArrayField = dt2.getArrayField();
            for (ArrayField af : listArrayField) {
                if (af.getFields() instanceof SimpleField) {
                    SimpleField sf = (SimpleField) af.getFields();
                    if (sf.getFieldMapping() != null) {
                        if (sf.getFieldMapping().getAttribute().isIdentifier()) {
                            if (sf.getFieldMapping().getAttribute().getParent() == er1
                                    || sf.getFieldMapping().getAttribute().getParent() == er2) {
                                if (dt2.getName() != sf.getParent().getName()) {
                                    mappedIdFields2.add(new Pair<>(sf, dt2));
                                } else {
                                    mappedIdFields2.add(new Pair<>(sf, sf.getParent()));
                                }
                            }
                        }
                    }
                }
            }
        }

        ret = new Pair(mappedIdFields1, mappedIdFields2);
        return ret;
    }

    private Pair<List<Pair<Field, DocumentType>>, List<Pair<Field, DocumentType>>> findCommonIdFields(
            ERElement er,
            ComputedEntity ce,
            DocumentType dt2) {

        if (ce == null) {
            return null;
        }

        Pair<List<Pair<Field, DocumentType>>, List<Pair<Field, DocumentType>>> ret = null;

        List<Pair<Field, DocumentType>> mappedIdFields1 = new ArrayList<>();
        List<Pair<Field, DocumentType>> mappedIdFields2 = new ArrayList<>();

        for (Field f : ce.getFields()) {
            if (f instanceof SimpleField) {
                SimpleField sf = (SimpleField) f;
                if (sf.getFieldMapping() != null) {
                    if (sf.getFieldMapping().getAttribute().isIdentifier()) {
                        if (sf.getFieldMapping().getAttribute().getParent() == er) {
                            mappedIdFields1.add(new Pair<>(sf, sf.getParent()));
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
                                if (sf.getFieldMapping().getAttribute().getParent() == er) {
                                    //sf.setName(emf.getName() + "." + sf.getName());
                                    mappedIdFields1.add(new Pair<>(sf, emf.getParent()));
                                }
                            }
                        }
                    }
                }
            }
        }
        if(ce.getArrayField().size()>0){
             List<ArrayField> listArrayField = new ArrayList<>();
             listArrayField = ce.getArrayField();
             for (ArrayField af : listArrayField) {
                 if (af.getFields() instanceof SimpleField) {
                     SimpleField sf = (SimpleField) af.getFields();
                     if (sf.getFieldMapping() != null) {
                         if (sf.getFieldMapping().getAttribute().isIdentifier()) {
                             if (sf.getFieldMapping().getAttribute().getParent() == er) {
                                 if (ce.getName() != sf.getParent().getName()) {
                                     mappedIdFields1.add(new Pair<>(sf, dt2));
                                 }
                                 else {
                                     mappedIdFields1.add(new Pair<>(sf, sf.getParent()));
                                 }
                             }
                         }
                     }
                 }
             }   
       }

        for (Field f : dt2.getFields()) {
            if (f instanceof SimpleField) {
                SimpleField sf = (SimpleField) f;
                if (sf.getFieldMapping() != null) {
                    if (sf.getFieldMapping().getAttribute().isIdentifier()) {
                        if (sf.getFieldMapping().getAttribute().getParent() == er) {
                            mappedIdFields2.add(new Pair<>(sf, sf.getParent()));
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
                                if (sf.getFieldMapping().getAttribute().getParent() == er) {
                                    // sf.setName(emf.getName() + "." + sf.getName());
                                    mappedIdFields2.add(new Pair<>(sf, emf.getParent()));
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (dt2.getArrayField().size() > 0) {
                List<ArrayField> listArrayField = new ArrayList<>();
                listArrayField = dt2.getArrayField();
                for (ArrayField af : listArrayField) {
                    if (af.getFields() instanceof SimpleField) {
                        SimpleField sf = (SimpleField) af.getFields();
                        if (sf.getFieldMapping() != null) {
                            if (sf.getFieldMapping().getAttribute().isIdentifier()) {
                                if (sf.getFieldMapping().getAttribute().getParent() == er) {
                                    if (dt2.getName() != sf.getParent().getName()) {
                                        mappedIdFields2.add(new Pair<>(sf, dt2));
                                    }
                                    else {
                                        mappedIdFields2.add(new Pair<>(sf, sf.getParent()));
                                    }
                                }
                            }
                        }
                    }
                }   
            }

        ret = new Pair(mappedIdFields1, mappedIdFields2);
        return ret;

    }

    private void completeAttributesOperations(Query q, ERElement er, List<Attribute> queryAttributes, Relationship r) {
        //System.out.println("PASA POR completeAttributesOperations: " + er.getName().toString());
        ComputedEntity ceResult = q.getCopyOfLastComputedEntity();
        if (ceResult == null) {
            return;
        }
        for (Attribute a : queryAttributes) {
            if (a.getParent() == er) {
                if (!ceResult.containsMappedArrayField(a)) {
                    if (!ceResult.containsMappedField(a)) {
                        DocumentType dt = mappingModel.getMongoSchema().findMainDocumentType(er);
                        if (dt == null) {
                            q.addOperation(new ImpossibleOperation("Não pode recuperar "
                                    + a.getParent().getName() + "." + a.getName()
                                    + ", pois não existe um DocumentType (main=true)"
                                    + " mapeado a " + er.getName()));
                        } else {
                            Pair<List<Pair<Field, DocumentType>>, List<Pair<Field, DocumentType>>> pairOfFields = findCommonIdFields(er, ceResult, dt);
                            if (pairOfFields.getFirst().size() == 0 || pairOfFields.getSecond().size() == 0) {
                                q.addOperation(new ImpossibleOperation("completeAttributesOperations impossível join entre "
                                        + er.getName()
                                        + " via "
                                        + ceResult.getName()
                                        + " e "
                                        + dt.getName()
                                        + ", pois não há atributos id comuns entre os document types"));
                            } else {
                                for (Field f : dt.getFields()) {
                                    if (f instanceof SimpleField) {
                                        SimpleField sf = (SimpleField) f;
                                        if (sf.getFieldMapping() != null
                                                && !ceResult.containsMappedNewField(sf.getFieldMapping().getAttribute())) {
                                            ceResult.addNewField(sf);
                                        }
                                    }
                                    if (f instanceof EmbeddedField) {
                                        EmbeddedField ef = (EmbeddedField) f;
                                        //verificamos se campo embutido ja existe no ceResult,se não existe inserimos.
                                        if (!ceResult.containsMappedEmbeddedField(ef)) {
                                            ceResult.addNewField(ef);
                                        }
                                    }
                                } 
                                Field fFirst = null;
                                Field fSecond = null;
                                for (Pair<Field, DocumentType> pf : pairOfFields.getFirst()) {
                                    fFirst = pf.getFirst();
                                    for (Pair<Field, DocumentType> ps : pairOfFields.getSecond()) {
                                        fSecond = ps.getFirst();                                      
                                        //pra realizar a junção os dois Field tem de ter mesmo pai, ou seja ambos ids tem de estar mapeados pra mesma Entidade
                                        SimpleField fA = (SimpleField) fFirst;
                                        SimpleField fB = (SimpleField) fSecond;
                                        if (fA.getFieldMapping().getAttribute().getParent() == fB.getFieldMapping().getAttribute().getParent()) {
                                            q.addOperation(new JoinOperation(pairOfFields, "completeAttributesOperations join entre "
                                                    + er.getName()
                                                    + " via "
                                                    + ceResult.getName()
                                                    + " e "
                                                    + dt.getName(), ceResult, 2, r));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
