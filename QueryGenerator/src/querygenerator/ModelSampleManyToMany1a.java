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
import querygenerator.mongoschema.ArrayField;
import querygenerator.mongoschema.DocumentType;
import querygenerator.mongoschema.ERMapping;
import querygenerator.mongoschema.EmbeddedField;
import querygenerator.mongoschema.FieldMapping;
import querygenerator.mongoschema.MongoSchema;
import querygenerator.mongoschema.SimpleField;


public class ModelSampleManyToMany1a {

    public static MappingModel getModel() {
        ERModel erModel = new ERModel();


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
       
        Relationship repairs = new Relationship("Repairs");
        repairs.addRelationshipEnd(new RelationshipEnd(car, Cardinality.Many));
        repairs.addRelationshipEnd(new RelationshipEnd(garage, Cardinality.Many));
        repairs.addAttribute(new Attribute(repairs, "date", "date",false));
        repairs.addAttribute(new Attribute(repairs, "fix", "string",false));
        erModel.addERElement(repairs);

        MongoSchema mongoSchema = new MongoSchema();

        DocumentType docTypeCar = new DocumentType("DocTypeCar");
        docTypeCar.addERMapping(new ERMapping(car, true));
        docTypeCar.addField(new SimpleField(docTypeCar, "_id", "int", new FieldMapping(car.getAttribute("id"))));
        docTypeCar.addField(new SimpleField(docTypeCar, "fPlate", "string", new FieldMapping(car.getAttribute("plate"))));
        docTypeCar.addField(new SimpleField(docTypeCar, "fColor", "string", new FieldMapping(car.getAttribute("color"))));
        mongoSchema.addDocumentType(docTypeCar);
        
        DocumentType docTypeGarage = new DocumentType("DocTypeGarage");
        docTypeGarage.addERMapping(new ERMapping(garage, true));
        docTypeGarage.addField(new SimpleField(docTypeGarage, "_id", "int", new FieldMapping(garage.getAttribute("id"))));
        docTypeGarage.addField(new SimpleField(docTypeGarage, "fName", "string", new FieldMapping(garage.getAttribute("name"))));
        docTypeGarage.addField(new SimpleField(docTypeGarage, "fColor", "string", new FieldMapping(garage.getAttribute("address"))));
        mongoSchema.addDocumentType(docTypeGarage);
        
        
        DocumentType docTypeRepairs = new DocumentType("DocTypeRepairs");
        docTypeRepairs.addERMapping(new ERMapping(repairs, true));
        docTypeRepairs.addERMapping(new ERMapping(car, false));
        docTypeRepairs.addERMapping(new ERMapping(garage, false));
        docTypeRepairs.addField(new SimpleField(docTypeRepairs, "fDate", "date", new FieldMapping(repairs.getAttribute("date"))));
        docTypeRepairs.addField(new SimpleField(docTypeRepairs, "fFix", "string", new FieldMapping(repairs.getAttribute("fix"))));
        docTypeRepairs.addField(new SimpleField(docTypeRepairs, "fCarId", "int", new FieldMapping(car.getAttribute("id"))));
        docTypeRepairs.addField(new SimpleField(docTypeRepairs, "fGarageId", "int", new FieldMapping(garage.getAttribute("id"))));
        mongoSchema.addDocumentType(docTypeRepairs);
        
        
        List<String> violations = mongoSchema.validate();
        if(violations.size() > 0) {
            for(String v:violations) {
                System.out.println(v);
            }
        }

        return new MappingModel(erModel, mongoSchema);
    }
}
