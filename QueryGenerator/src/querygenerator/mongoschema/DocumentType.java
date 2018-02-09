/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.mongoschema;

import java.util.ArrayList;
import java.util.List;
import querygenerator.ermodel.ERElement;

/**
 *
 * @author daniellucredio
 */
public class DocumentType {

    String name;
    List<ERMapping> erMappingList;
    List<Field> fields;
    List<ArrayField> arrayField;

    public DocumentType(String name) {
        this.name = name;
        this.erMappingList = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.arrayField = new ArrayList<>();
    }

    public List<ArrayField> getArrayField() {
        return arrayField;
    }

    public void setArrayField(List<ArrayField> arrayField) {
        this.arrayField = arrayField;
    }

    public String getName() {
        return name;
    }

    public List<ERMapping> getERMappingList() {
        return erMappingList;
    }
    
    public ERMapping findERMapping(ERElement erElement) {
        for(ERMapping erM: erMappingList) {
            if(erM.erElement == erElement) {
                return erM;
            }
        }
        
        //new 22/12/2017
        List<Field> fields = this.getFields();
        for (Field f : fields) {
            if (f instanceof EmbeddedField) {
                EmbeddedField ef = (EmbeddedField) f;
                DocumentType embeddedDt = ef.getSubDocType();
                for (ERMapping erMapping : embeddedDt.erMappingList) {
                    if (erMapping.erElement == erElement) {
                        return erMapping;
                    }
                }
            }
        }
        return null;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void addERMapping(ERMapping em) {
        this.erMappingList.add(em);
    }

    public void addField(Field f) {
        if (f.parent != this) {
            throw new RuntimeException("O pai do campo " + f.name + " deve ser " + name);
        }
        this.fields.add(f);
    }
    
    public void addArrayField(ArrayField af){
        //verificar depois se tem como validar!
        //if (af.fields.parent != this) {
        //    throw new RuntimeException("O pai do campo " + af.fields.name + " deve ser " + name);
        //}
        this.arrayField.add(af);
    }
   
    @Override
    public String toString() {
        String ret = "";
        ret = name + " [ ";
        for (int i = 0; i < erMappingList.size(); i++) {
            ret += erMappingList.get(i).toString();
            if (i < erMappingList.size() - 1) {
                ret += ", ";
            }
        }
        ret += " ] ";
        ret += "\n";
        ret += "{";
        ret += "\n";
        for (Field f : fields) {
            ret += "   " + f.toString();
            ret += "\n";
        }
        if (arrayField.size() > 0) {
           // ret += " ARRAY [ ";
            for (ArrayField af : arrayField) {
                ret += "\n" + af.toString();
                ret += "\n";
            }
           // ret += " ] ";
        }

        ret += "}";
        ret += "\n";
        return ret;
    }

    void validate(List<String> violations) {
        for (ERMapping m : erMappingList) {
            boolean hasMappedField = false;
            for (Field f : fields) {
                if (f instanceof SimpleField) {
                    SimpleField sf = (SimpleField) f;
                    if (sf.fieldMapping != null
                            && sf.fieldMapping.attribute.getParent() == m.erElement) {
                        hasMappedField = true;
                    }
                }
            }
            if (!hasMappedField) {
                violations.add("Entidade " + m.erElement.getName() + " está mapeada"
                        + " dentro do DocumentType " + name + ", porém não há nenhum"
                        + " atributo da entidade mapeado.");
            }
        }

        for (Field f : fields) {
            if (f instanceof SimpleField) {
                SimpleField sf = (SimpleField) f;
                if (sf.fieldMapping != null) {
                    boolean hasMappedEntity = false;
                    for (ERMapping m : erMappingList) {
                        if (m.erElement == sf.fieldMapping.attribute.getParent()) {
                            hasMappedEntity = true;
                        }
                    }
                    if (!hasMappedEntity) {
                        violations.add("Campo " + name + "." + sf.name + " está mapeado para"
                                + " o atributo " + sf.fieldMapping.attribute.getParent().getName()
                                + "." + sf.fieldMapping.attribute.getName()
                                + ", porém essa entidade não está mapeada no DocumentType "
                                + name);
                    }
                }
            }
        }
    }
}
