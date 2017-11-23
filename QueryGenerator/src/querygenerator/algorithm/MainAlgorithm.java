/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.algorithm;

import java.util.ArrayList;
import java.util.List;
import querygenerator.ermodel.Attribute;
import querygenerator.ermodel.ERElement;
import querygenerator.ermodel.Entity;
import querygenerator.ermodel.Relationship;
import querygenerator.mapping.MappingModel;
import querygenerator.mongoschema.DocumentType;
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
                    + " " + r.getName());
        }

        // TODO: verificar se os atributos de "queryAttributes" 
        // realmente pertencem a e1, r ou e2
        
        //aqui verificamos a que documentos estão mapeados as entidades e o relacionamento?? certo!
        List<DocumentType> docTypesE1 = mappingModel.getMongoSchema().findDocumentTypes(e1);
        List<DocumentType> docTypesR = mappingModel.getMongoSchema().findDocumentTypes(r);
        List<DocumentType> docTypesE2 = mappingModel.getMongoSchema().findDocumentTypes(e2);

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

                    ComputedEntity ce = ComputedEntity.createNew(
                            dt1.findERMapping(e1).isMain(),
                            e1.getName()
                            + "-" + r.getName()
                            + "-" + e2.getName(),
                            erElements,
                            documentTypes
                    );
                    Query q = new Query();
                    q.addOperation(new StartOperation("Start, primeira EC não vai ter atributos!!", ce));

                    findDocTypeOperation(q, dt1);
                    completeAttributesOperations(q, e1, queryAttributes);

                    joinTwoEntitiesOperation(q, e1, r, q.getCopyOfLastComputedEntity(), dtr);
                    completeAttributesOperations(q, r, queryAttributes);

                    joinTwoEntitiesOperation(q, e1, e2, q.getCopyOfLastComputedEntity(), dt2);
                    completeAttributesOperations(q, e2, queryAttributes);

                    if (!ret.contains(q)) {
                        ret.add(q);
                    }
                }
            }
        }
        return ret;
    }

    private void findDocTypeOperation(Query q, DocumentType dt) {
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
            }
        }
        q.addOperation(new FindOperation(dt, "find(" + dt.getName() + ")", ceResult));
    }

    private void joinTwoEntitiesOperation(Query q, ERElement e1, ERElement e2, ComputedEntity ce, DocumentType dt2) {
        if (ce == null) {
            q.addOperation(new ImpossibleOperation("impossível pois não há entidade computada válida!"));
        } else {
            if (ce.containsMappedERElement(e2)) {
                return;
            }

            ComputedEntity ceResult = ComputedEntity.createCopy(ce);

            Pair<SimpleField, SimpleField> pairOfFields = findCommonIdFields(e1, e2, ceResult, dt2);
            if (pairOfFields == null) {
                q.addOperation(new ImpossibleOperation("impossível join entre "
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
                //verificar se os pares de campos de junção tem o mesmo pai
                if (pairOfFields.getFirst().getParent()
                        != pairOfFields.getSecond().getParent()) {
                    for (Field f : dt2.getFields()) {
                        if (f instanceof SimpleField) {
                            SimpleField sf = (SimpleField) f;
                            if (sf.getFieldMapping() != null
                                    && !ceResult.containsMappedField(sf.getFieldMapping().getAttribute())) {
                                ceResult.addNewField(sf);
                            }
                        }
                    }
                    q.addOperation(new JoinOperation(pairOfFields,
                            "join("
                            + pairOfFields.getFirst().getParent().getName()
                            + "."
                            + pairOfFields.getFirst().getName()
                            + ", "
                            + pairOfFields.getSecond().getParent().getName()
                            + "."
                            + pairOfFields.getSecond().getName()
                            + ")", ceResult));
                }
            }
        }
    }

    private Pair<SimpleField, SimpleField> findCommonIdFields(
            ERElement er1,
            ERElement er2,
            ComputedEntity ce,
            DocumentType dt2) {

        if (ce == null) {
            return null;
        }

        Pair<SimpleField, SimpleField> ret = null;

        List<SimpleField> mappedIdFields1 = new ArrayList<>();
        List<SimpleField> mappedIdFields2 = new ArrayList<>();

        for (Field f : ce.getFields()) {
            if (f instanceof SimpleField) {
                SimpleField sf = (SimpleField) f;
                if (sf.getFieldMapping() != null) {
                    if (sf.getFieldMapping().getAttribute().isIdentifier()) {
                        if (sf.getFieldMapping().getAttribute().getParent() == er1
                                || sf.getFieldMapping().getAttribute().getParent() == er2) {         
                            mappedIdFields1.add(sf);
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
                            mappedIdFields2.add(sf);                        
                        }
                    }
                }
            }
        }

        for (SimpleField sf1 : mappedIdFields1) {
            for (SimpleField sf2 : mappedIdFields2) {
                if (sf1.getFieldMapping().getAttribute()
                        == sf2.getFieldMapping().getAttribute()) {
                    if (ret != null) {
                        System.out.println("Existem dois pares de campos identificadores"
                                + " para os elementos " + ce.getName() + " e " + er2.getName() + " "
                                + "nos DocumentTypes " + ce.getName() + " e " + dt2.getName()
                                + " (" + sf1.getName() + ", " + sf2.getName() + ")");
                    } else {
                        ret = new Pair(sf1, sf2);
                    }
                }
            }
        }

        return ret;
    }

    private Pair<SimpleField, SimpleField> findCommonIdFields(
            ERElement er,
            ComputedEntity ce,
            DocumentType dt2) {

        if (ce == null) {
            return null;
        }

        Pair<SimpleField, SimpleField> ret = null;

        List<SimpleField> mappedIdFields1 = new ArrayList<>();
        List<SimpleField> mappedIdFields2 = new ArrayList<>();

        for (Field f : ce.getFields()) {
            if (f instanceof SimpleField) {
                SimpleField sf = (SimpleField) f;
                if (sf.getFieldMapping() != null) {
                    if (sf.getFieldMapping().getAttribute().isIdentifier()) {
                        if (sf.getFieldMapping().getAttribute().getParent() == er) {
                            mappedIdFields1.add(sf);
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
                            mappedIdFields2.add(sf);
                        }
                    }
                }
            }
        }

        for (SimpleField sf1 : mappedIdFields1) {
            for (SimpleField sf2 : mappedIdFields2) {
                if (sf1.getFieldMapping().getAttribute()
                        == sf2.getFieldMapping().getAttribute()) {
                    if (ret != null) {
                        System.out.println("Existem dois pares de campos identificadores"
                                + " para o elemento " + ce.getName() + " "
                                + "nos DocumentTypes " + ce.getName() + " e " + dt2.getName()
                                + " (" + sf1.getName() + ", " + sf2.getName() + ")");
                    } else {
                        ret = new Pair(sf1, sf2);
                    }
                }
            }
        }

        return ret;
    }

    private void completeAttributesOperations(Query q, ERElement er, List<Attribute> queryAttributes) {
        ComputedEntity ceResult = q.getCopyOfLastComputedEntity();
        if (ceResult == null) {
            return;
        }
        for (Attribute a : queryAttributes) {
            if (a.getParent() == er) {
                if (!ceResult.containsMappedField(a)) {
                    DocumentType dt = mappingModel.getMongoSchema().findMainDocumentType(er);
                    if (dt == null) {
                        q.addOperation(new ImpossibleOperation("Não pode recuperar "
                                + a.getParent().getName() + "." + a.getName()
                                + ", pois não existe um DocumentType (main=true)"
                                + " mapeado a " + er.getName()));
                    } else {
                        Pair<SimpleField, SimpleField> pairOfFields = findCommonIdFields(er, ceResult, dt);
                        if (pairOfFields == null) {
                            q.addOperation(new ImpossibleOperation("impossível join entre "
                                    + er.getName()
                                    + " via "
                                    + ceResult.getName()
                                    + " e "
                                    + dt.getName()
                                    + ", pois não há atributos id comuns entre os document types"));
                        } else {
                            if (pairOfFields.getFirst().getParent()
                                    != pairOfFields.getSecond().getParent()) {
                                for (Field f : dt.getFields()) {
                                    if (f instanceof SimpleField) {
                                        SimpleField sf = (SimpleField) f;
                                        if (sf.getFieldMapping() != null
                                                && !ceResult.containsMappedField(sf.getFieldMapping().getAttribute())) {
                                            ceResult.addNewField(sf);
                                        }
                                    }
                                }
                                q.addOperation(new JoinOperation(pairOfFields, 
                                        "join("
                                        + pairOfFields.getFirst().getParent().getName()
                                        + "."
                                        + pairOfFields.getFirst().getName()
                                        + ", "
                                        + pairOfFields.getSecond().getParent().getName()
                                        + "."
                                        + pairOfFields.getSecond().getName()
                                        + ")", ceResult));
                            }
                        }
                    }
                }
            }
        }
    }
}
