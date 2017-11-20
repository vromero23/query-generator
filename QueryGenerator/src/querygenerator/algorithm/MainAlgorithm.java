/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.algorithm;

import java.util.ArrayList;
import java.util.List;
import querygenerator.ermodel.ERElement;
import querygenerator.ermodel.Entity;
import querygenerator.ermodel.Relationship;
import querygenerator.ermodel.RelationshipEnd;
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

    public List<Query> binaryJoin(Entity e1, Relationship r, Entity e2) {
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

        List<DocumentType> docTypesE1 = mappingModel.getMongoSchema().findDocumentTypes(e1);
        List<DocumentType> docTypesR = mappingModel.getMongoSchema().findDocumentTypes(r);
        List<DocumentType> docTypesE2 = mappingModel.getMongoSchema().findDocumentTypes(e2);

        for (DocumentType dt1 : docTypesE1) {
            for (DocumentType dtr : docTypesR) {
                for (DocumentType dt2 : docTypesE2) {

                    ComputedEntity ce = ComputedEntity.createNew(
                            dt1.findERMapping(e1).isMain(),
                            e1.getName()
                            + "-" + r.getName()
                            + "-" + e2.getName()
                            + "(" + dt1.getName() + "," + dtr.getName() + "," + dt2.getName() + ")");
                    Query q = new Query();
                    q.addOperation(new SimpleOperation("Start", ce));

                    findDocTypeOperation(q, dt1);
                    aggregateFieldsOperations(q, e1, dt1);

                    joinOperations(q, e1, r, q.getCopyOfLastComputedEntity(), dtr);
                    joinOperations(q, e1, e2, q.getCopyOfLastComputedEntity(), dt2);

                    ret.add(q);
                }
            }
        }
        return ret;
    }

    private void findDocTypeOperation(Query q, DocumentType dt) {
        q.addOperation(new SimpleOperation("find(" + dt.getName() + ")", q.getCopyOfLastComputedEntity()));
    }

    private void aggregateFieldsOperations(Query q, ERElement e, DocumentType dt) {
        ComputedEntity ceResult = q.getCopyOfLastComputedEntity();
        if(ceResult == null) return;
        String ret = "project(";
        for (Field f : dt.getFields()) {
            if (f instanceof SimpleField) {
                SimpleField sf = (SimpleField) f;
                if (sf.getFieldMapping() != null
                        && sf.getFieldMapping().getAttribute().getParent() == e
                        && !ceResult.containsMappedField(sf.getFieldMapping().getAttribute())) {
                    ret += sf.getParent().getName() + "." + sf.getName() + ":1,";
                    ceResult.addField(sf);
                }
            }
        }
        ret += ")";
        q.addOperation(new SimpleOperation(ret, ceResult));

        if (e instanceof Relationship) {
            Relationship rE2 = (Relationship) e;
            for (RelationshipEnd end : rE2.getRelationshipEnds()) {
                aggregateFieldsOperations(q, end.getEntity(), dt);
            }
        } else if(e instanceof Entity) {
            Entity entE2 = (Entity)e;
            List<Relationship> allRelationships = mappingModel.getERModel().findAllRelationships(entE2);
            for(Relationship r: allRelationships) {
                if(r != e) {
                    aggregateFieldsOperations(q, r, dt);
                }
            }
        }

    }

    private void joinOperations(Query q, ERElement e1, ERElement e2, ComputedEntity ce, DocumentType dt2) {
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
                        + ", pois não há atributos comuns entre os document types"));
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
                if (pairOfFields.getFirst().getParent()
                        != pairOfFields.getSecond().getParent()) {
                    q.addOperation(new SimpleOperation(
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
                aggregateFieldsOperations(q, e2, dt2);
            }
        }
//        }
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
}
//
//findDocTypeOperation( DocumentType )
//{
//    // retorna uma operação simples, do tipo "find"
//}
//
//aggregateFieldsOperation( Entity, DocumentType )
//{
//    // juntar todos os campos de Entity que estão presentes em DocumentType
//    // se faltar algum, não tem problema, tentaremos buscar posteriormente
//}
//
//findCommonIdField( DT1, DT2, ER1, ER2 )
//{
//    // encontrar um par de campos para fazer o join
//    // sendo que deve ser um campo mapeado a um
//    // atributo do tipo ID da entidade/relacionamento ER1
//    // ou atributo do tipo ID da entidade/relacionamento ER2
//} 
//}
