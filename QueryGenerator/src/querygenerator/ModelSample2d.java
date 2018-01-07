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
public class ModelSample2d {

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

        Entity car = new Entity("Car");
        car.addAttribute(new Attribute(car, "id", "int", true));
        car.addAttribute(new Attribute(car, "plate", "string", false));
        car.addAttribute(new Attribute(car, "color", "string", false));
        erModel.addERElement(car);

        Entity garage = new Entity("Garage");
        garage.addAttribute(new Attribute(garage, "id", "int", true));
        garage.addAttribute(new Attribute(garage, "name", "string", false));
        garage.addAttribute(new Attribute(garage, "address", "string", false));
        erModel.addERElement(garage);

        Entity insuranceCompany = new Entity("InsuranceCompany");
        insuranceCompany.addAttribute(new Attribute(insuranceCompany, "id", "int", true));
        insuranceCompany.addAttribute(new Attribute(insuranceCompany, "name", "string", false));
        insuranceCompany.addAttribute(new Attribute(insuranceCompany, "phone", "string", false));
        erModel.addERElement(insuranceCompany);

        Relationship registration = new Relationship("Registration");
        registration.addRelationshipEnd(new RelationshipEnd(person, Cardinality.One));
        registration.addRelationshipEnd(new RelationshipEnd(driversLicense, Cardinality.One));
        registration.addAttribute(new Attribute(registration, "observation", "string",false));
        erModel.addERElement(registration);

        Relationship drives = new Relationship("Drives");
        drives.addRelationshipEnd(new RelationshipEnd(person, Cardinality.One));
        drives.addRelationshipEnd(new RelationshipEnd(car, Cardinality.Many));
        erModel.addERElement(drives);

        Relationship contract = new Relationship("Contract");
        contract.addRelationshipEnd(new RelationshipEnd(person, Cardinality.Many));
        contract.addRelationshipEnd(new RelationshipEnd(car, Cardinality.Many));
        contract.addRelationshipEnd(new RelationshipEnd(insuranceCompany, Cardinality.Many));
        contract.addAttribute(new Attribute(contract, "id", "int", true));
        erModel.addERElement(contract);

        Relationship repairs = new Relationship("Repairs");
        repairs.addRelationshipEnd(new RelationshipEnd(car, Cardinality.Many));
        repairs.addRelationshipEnd(new RelationshipEnd(garage, Cardinality.Many));
        repairs.addAttribute(new Attribute(repairs, "date", "date", false));
        repairs.addAttribute(new Attribute(repairs, "fix", "string", false));
        erModel.addERElement(repairs);

        MongoSchema mongoSchema = new MongoSchema();

        DocumentType docTypePerson = new DocumentType("DocTypePerson");
        docTypePerson.addERMapping(new ERMapping(person, true));
        docTypePerson.addField(new SimpleField(docTypePerson, "_id", "int", new FieldMapping(person.getAttribute("id"))));
        docTypePerson.addField(new SimpleField(docTypePerson, "fName", "string", new FieldMapping(person.getAttribute("name"))));
        docTypePerson.addField(new SimpleField(docTypePerson, "fAddress", "string", new FieldMapping(person.getAttribute("address"))));
        mongoSchema.addDocumentType(docTypePerson);

        DocumentType docTypeDriversLicense = new DocumentType("DocTypeDriversLicense");
        docTypeDriversLicense.addERMapping(new ERMapping(driversLicense, true));
        docTypeDriversLicense.addERMapping(new ERMapping(person, false));
        docTypeDriversLicense.addERMapping(new ERMapping(registration, false));
        docTypeDriversLicense.addField(new SimpleField(docTypeDriversLicense, "_id", "int", new FieldMapping(driversLicense.getAttribute("id"))));
        docTypeDriversLicense.addField(new SimpleField(docTypeDriversLicense, "fNumber", "int", new FieldMapping(driversLicense.getAttribute("number"))));
        docTypeDriversLicense.addField(new SimpleField(docTypeDriversLicense, "fDate", "date", new FieldMapping(driversLicense.getAttribute("date"))));
        docTypeDriversLicense.addField(new SimpleField(docTypeDriversLicense, "observation", "string", new FieldMapping(registration.getAttribute("observation"))));
        docTypeDriversLicense.addField(new SimpleField(docTypeDriversLicense, "fPersonId", "int", new FieldMapping(person.getAttribute("id"))));
        //docTypeDriversLicense.addField(new SimpleField(docTypeDriversLicense, "fName", "string", new FieldMapping(person.getAttribute("name"))));
        docTypeDriversLicense.addField(new SimpleField(docTypeDriversLicense, "fAddress", "string", new FieldMapping(person.getAttribute("address"))));
   
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
