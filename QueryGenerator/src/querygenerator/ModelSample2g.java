/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator;

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
import querygenerator.mongoschema.FieldMapping;
import querygenerator.mongoschema.MongoSchema;
import querygenerator.mongoschema.SimpleField;

/**
 *
 * @author daniellucredio
 */
public class ModelSample2g {

    public static MappingModel getModel() {
        ERModel erModel = new ERModel();

        Entity person = new Entity("Person");
        person.addAttribute(new Attribute(person, "id", "int", true));
        person.addAttribute(new Attribute(person, "name", "string", false));
        person.addAttribute(new Attribute(person, "address", "string", false));
        erModel.addERElement(person);

        Entity driversLicense = new Entity("DriversLicense");
        driversLicense.addAttribute(new Attribute(driversLicense, "id", "int", true));
        driversLicense.addAttribute(new Attribute(driversLicense, "number", "int", false));
        driversLicense.addAttribute(new Attribute(driversLicense, "date", "date", false));
        erModel.addERElement(driversLicense);

        Relationship registration = new Relationship("Registration");
        registration.addRelationshipEnd(new RelationshipEnd(person, Cardinality.One));
        registration.addRelationshipEnd(new RelationshipEnd(driversLicense, Cardinality.One));
        registration.addAttribute(new Attribute(registration, "observation", "string",false));
        erModel.addERElement(registration); 

        MongoSchema mongoSchema = new MongoSchema();

        DocumentType docTypePerson = new DocumentType("DocTypePerson");
        docTypePerson.addERMapping(new ERMapping(person, true));
        docTypePerson.addERMapping(new ERMapping(registration, false));
        docTypePerson.addField(new SimpleField(docTypePerson, "_id", "int", new FieldMapping(person.getAttribute("id"))));
        docTypePerson.addField(new SimpleField(docTypePerson, "fName", "string", new FieldMapping(person.getAttribute("name"))));
        docTypePerson.addField(new SimpleField(docTypePerson, "fAddress", "string", new FieldMapping(person.getAttribute("address"))));
        docTypePerson.addField(new SimpleField(docTypePerson, "observation", "string", new FieldMapping(registration.getAttribute("observation"))));
        mongoSchema.addDocumentType(docTypePerson);

        DocumentType docTypeDriversLicense = new DocumentType("DocTypeDriversLicense");
        docTypeDriversLicense.addERMapping(new ERMapping(driversLicense, true));
        docTypeDriversLicense.addField(new SimpleField(docTypeDriversLicense, "_id", "int", new FieldMapping(driversLicense.getAttribute("id"))));
        //docTypeDriversLicense.addField(new SimpleField(docTypeDriversLicense, "fNumber", "int", new FieldMapping(driversLicense.getAttribute("number"))));
        docTypeDriversLicense.addField(new SimpleField(docTypeDriversLicense, "fDate", "date", new FieldMapping(driversLicense.getAttribute("date"))));
     
        DocumentType docTypePerson2 = new DocumentType("DocTypePerson2");
        docTypePerson2.addERMapping(new ERMapping(person, true));
        docTypePerson2.addField(new SimpleField(docTypePerson2, "fPersonId", "int", new FieldMapping(person.getAttribute("id"))));
        docTypePerson2.addField(new SimpleField(docTypePerson2, "fName", "string", new FieldMapping(person.getAttribute("name"))));
        docTypePerson2.addField(new SimpleField(docTypePerson2, "fAddress", "string", new FieldMapping(person.getAttribute("address"))));
        docTypeDriversLicense.addField(new EmbeddedField(docTypeDriversLicense, "data_Person", docTypePerson2));    
        
        mongoSchema.addDocumentType(docTypeDriversLicense);

        DocumentType docTypeRegistration = new DocumentType("DocTypeRegistration");
        docTypeRegistration.addERMapping(new ERMapping(registration, true));
        docTypeRegistration.addField(new SimpleField(docTypeRegistration, "_id", "int", null));
        docTypeRegistration.addField(new SimpleField(docTypeRegistration, "observation", "string", new FieldMapping(registration.getAttribute("observation"))));
        mongoSchema.addDocumentType(docTypeRegistration);
        
        List<String> violations = mongoSchema.validate();
        if(violations.size() > 0) {
            for(String v:violations) {
                System.out.println(v);
            }
        }

        return new MappingModel(erModel, mongoSchema);
    }
}
