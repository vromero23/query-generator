/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator;

import java.util.ArrayList;
import java.util.List;
import querygenerator.ermodel.Attribute;
import querygenerator.ermodel.Cardinality;
import querygenerator.ermodel.ERModel;
import querygenerator.ermodel.Entity;
import querygenerator.ermodel.Relationship;
import querygenerator.ermodel.RelationshipEnd;
import querygenerator.mapping.MappingModel;
import querygenerator.mongoschema.DocumentType;
import querygenerator.mongoschema.ERMapping;
import querygenerator.mongoschema.EmbeddedField;
import querygenerator.mongoschema.Field;
import querygenerator.mongoschema.FieldMapping;
import querygenerator.mongoschema.MongoSchema;
import querygenerator.mongoschema.SimpleField;


public class ModelSampleFunDep6 {

    public static MappingModel getModel() {
        ERModel erModel = new ERModel();

        Entity funcionario = new Entity("Funcionario");
        funcionario.addAttribute(new Attribute(funcionario, "id", "int", true));
        funcionario.addAttribute(new Attribute(funcionario, "cpf", "string", false));
        funcionario.addAttribute(new Attribute(funcionario, "nome_funcionario", "string", false));
        funcionario.addAttribute(new Attribute(funcionario, "sexo", "string", false));
        erModel.addERElement(funcionario);

        Entity departamento = new Entity("Departamento");
        departamento.addAttribute(new Attribute(departamento, "id", "int", true));
        departamento.addAttribute(new Attribute(departamento, "numero_dep", "string", false));
        departamento.addAttribute(new Attribute(departamento, "nome_departamento", "string", false));
        erModel.addERElement(departamento);

        Relationship gerencia = new Relationship("Gerencia");
        gerencia.addRelationshipEnd(new RelationshipEnd(funcionario, Cardinality.One));
        gerencia.addRelationshipEnd(new RelationshipEnd(departamento, Cardinality.One));
        gerencia.addAttribute(new Attribute(gerencia, "data_inicio", "string",false));
        erModel.addERElement(gerencia);

        MongoSchema mongoSchema = new MongoSchema();

        DocumentType docTypeDepartamento = new DocumentType("DocTypeDepartamento");
        docTypeDepartamento.addERMapping(new ERMapping(departamento, true));
        docTypeDepartamento.addField(new SimpleField(docTypeDepartamento, "_id", "int", new FieldMapping(departamento.getAttribute("id"))));
        docTypeDepartamento.addField(new SimpleField(docTypeDepartamento, "fNumero_dep", "string", new FieldMapping(departamento.getAttribute("numero_dep"))));
        docTypeDepartamento.addField(new SimpleField(docTypeDepartamento, "fNome_departamento", "string", new FieldMapping(departamento.getAttribute("nome_departamento"))));
        mongoSchema.addDocumentType(docTypeDepartamento);
         
        DocumentType docTypeGerencia  = new DocumentType("DocTypeGerencia");
        docTypeGerencia.addERMapping(new ERMapping(gerencia, true));
        docTypeGerencia.addField(new SimpleField(docTypeGerencia, "data_inicio", "string", new FieldMapping(gerencia.getAttribute("data_inicio"))));
        mongoSchema.addDocumentType(docTypeGerencia);
        
        
        DocumentType docTypeFuncionario = new DocumentType("DocTypeFuncionario");
        docTypeFuncionario.addERMapping(new ERMapping(funcionario, true));
        docTypeFuncionario.addField(new SimpleField(docTypeFuncionario, "_id", "int", new FieldMapping(funcionario.getAttribute("id"))));
        docTypeFuncionario.addField(new SimpleField(docTypeFuncionario, "fCpf", "string", new FieldMapping(funcionario.getAttribute("cpf"))));
        docTypeFuncionario.addField(new SimpleField(docTypeFuncionario, "fNome_funcionario", "string", new FieldMapping(funcionario.getAttribute("nome_funcionario"))));
        docTypeFuncionario.addField(new SimpleField(docTypeFuncionario, "fSexo", "string", new FieldMapping(funcionario.getAttribute("sexo"))));

        DocumentType docTypeGerencia2  = new DocumentType("DocTypeGerencia2");
        docTypeGerencia2.addERMapping(new ERMapping(gerencia, false));
        docTypeGerencia2.addField(new SimpleField(docTypeGerencia2, "data_inicio", "string", new FieldMapping(gerencia.getAttribute("data_inicio"))));
        docTypeFuncionario.addField(new EmbeddedField(docTypeFuncionario, "data_Gerencia", docTypeGerencia2));

        DocumentType docTypeDepartamento2 = new DocumentType("DocTypeDepartamento2");
        docTypeDepartamento2.addERMapping(new ERMapping(departamento, false));
        docTypeDepartamento2.addField(new SimpleField(docTypeDepartamento2, "_id", "int", new FieldMapping(departamento.getAttribute("id"))));
        docTypeDepartamento2.addField(new SimpleField(docTypeDepartamento2, "fNumero_dep", "string", new FieldMapping(departamento.getAttribute("numero_dep"))));
        docTypeDepartamento2.addField(new SimpleField(docTypeDepartamento2, "fNome_departamento", "string", new FieldMapping(departamento.getAttribute("nome_departamento"))));

        docTypeFuncionario.addField(new EmbeddedField(docTypeFuncionario, "data_Departamento", docTypeDepartamento2));

        mongoSchema.addDocumentType(docTypeFuncionario);
        
        
        List<String> violations = mongoSchema.validate();
        if(violations.size() > 0) {
            for(String v:violations) {
                System.out.println(v);
            }
        }

        return new MappingModel(erModel, mongoSchema);
    }
}
