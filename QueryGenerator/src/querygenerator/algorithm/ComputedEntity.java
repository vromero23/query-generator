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
import querygenerator.mongoschema.DocumentType;
import querygenerator.mongoschema.EmbeddedField;
import querygenerator.mongoschema.Field;
import querygenerator.mongoschema.SimpleField;

/**
 *
 * @author daniellucredio
 */
public class ComputedEntity {

    boolean main;
    String name;
    List<ERElement> erElements;
    List<DocumentType> documentTypes;
    List<Field> fields;
    List<Field> newFields;

    private ComputedEntity(boolean main, String name, List<ERElement> erElements, List<DocumentType> documentTypes) {
        this.main = main;
        this.name = name;
        fields = new ArrayList<>();
        newFields = new ArrayList<>();
        this.erElements = erElements;
        this.documentTypes = documentTypes;
    }

    public static ComputedEntity createNew(boolean main, String name, List<ERElement> erElements, List<DocumentType> documentTypes) {
        return new ComputedEntity(main, name, erElements, documentTypes);
    }

    public static ComputedEntity createCopy(ComputedEntity ce) {
        if (ce != null) {
            ComputedEntity ret = new ComputedEntity(ce.isMain(), ce.getName(), ce.getErElements(), ce.getDocumentTypes());
            for (Field f : ce.fields) {
                ret.addField(f);
            }
            return ret;
        }
        return null;
    }

    public boolean isMain() {
        return main;
    }

    public String getName() {
        return name;
    }

    public List<ERElement> getErElements() {
        return erElements;
    }

    public List<DocumentType> getDocumentTypes() {
        return documentTypes;
    }

    public List<Field> getFields() {
        return fields;
    }

    public List<Field> getNewFields() {
        return newFields;
    }

    private void addField(Field f) {
        this.fields.add(f);
    }

    public void addNewField(Field f) {
        this.addField(f);
        this.newFields.add(f);
    }

    public boolean containsMappedField(Attribute attribute) {
        for (Field f : fields) {
            if (f instanceof SimpleField) {
                SimpleField sf = (SimpleField) f;
                if (sf.getFieldMapping() != null
                        && sf.getFieldMapping().getAttribute() == attribute) {
                    return true;

                }
            } else if (f instanceof EmbeddedField) {
                EmbeddedField ef = (EmbeddedField) f;
                DocumentType subDocType = ef.getSubDocType();
                for (Field subField : subDocType.getFields()) {
                    if (subField instanceof SimpleField) {
                        SimpleField sf = (SimpleField) subField;
                        if (sf.getFieldMapping() != null
                                && sf.getFieldMapping().getAttribute() == attribute) {
                            return true;

                        }
                    }
                }
            }
        }
        return false;
    }
    
    public boolean containsMappedNewField(Attribute attribute) {
        for (Field f : newFields) {
            if (f instanceof SimpleField) {
                SimpleField sf = (SimpleField) f;
                if (sf.getFieldMapping() != null
                        && sf.getFieldMapping().getAttribute() == attribute) {
                    return true;

                }
            } else if (f instanceof EmbeddedField) {
                EmbeddedField ef = (EmbeddedField) f;
                DocumentType subDocType = ef.getSubDocType();
                for (Field subField : subDocType.getFields()) {
                    if (subField instanceof SimpleField) {
                        SimpleField sf = (SimpleField) subField;
                        if (sf.getFieldMapping() != null
                                && sf.getFieldMapping().getAttribute() == attribute) {
                            return true;

                        }
                    }
                }
            }
        }
        return false;
    }
    
   public boolean containsMappedEmbeddedField(EmbeddedField embField){
        for (Field f : newFields) {
            if (f instanceof EmbeddedField) {
                EmbeddedField ef = (EmbeddedField) f;
                if (ef.getName()==embField.getName()) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean containsMappedERElement(ERElement erElement) {
        for (Field f : fields) {
            if (f instanceof SimpleField) {
                SimpleField sf = (SimpleField) f;
                if (sf.getFieldMapping() != null
                        && sf.getFieldMapping().getAttribute().getParent() == erElement) {
                    return true;

                }
            }
        }
        return false;
    }

    boolean containsMappedERElementAsEmbeddedField(ERElement e2) {
        for (Field f : fields) {
            if (f instanceof EmbeddedField) {
                EmbeddedField ef = (EmbeddedField) f;
                DocumentType subDocType = ef.getSubDocType();
                if (subDocType.findERMapping(e2) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String ret = "";
        ret += "(main=" + main + ") ";
        ret += name + " [ ";
        for (int i = 0; i < documentTypes.size(); i++) {
            ret += documentTypes.get(i).getName();
            if (i < documentTypes.size() - 1) {
                ret += ", ";
            }
        }
        ret += " ]\n";
        ret += "{";
        ret += "\n";
        for (Field f : fields) {
            ret += "   " + f.toString();
            ret += "\n";
        }
        ret += "}";
        ret += "\n";
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ComputedEntity)) {
            return false;
        }
        ComputedEntity otherCE = (ComputedEntity) obj;
        if (this.main != otherCE.main) {
            return false;
        }
        if (!this.name.equals(otherCE.name)) {
            return false;
        }
        if (this.fields.size() != otherCE.fields.size()) {
            return false;
        }
        for (int i = 0; i < fields.size(); i++) {
            Field f1 = fields.get(i);
            Field f2 = otherCE.fields.get(i);
            if (f1 != f2) {
                return false;
            }
        }
        return true;
    }

}
