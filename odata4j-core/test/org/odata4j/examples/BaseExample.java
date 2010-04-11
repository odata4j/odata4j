package org.odata4j.examples;

import java.util.List;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmAssociation;
import org.odata4j.edm.EdmAssociationSet;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmFunctionParameter;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSchema;

import org.core4j.Enumerable;

public class BaseExample {

    protected static void report(String msg) {
        System.out.println(msg);
    }

    protected static void report(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }
    
    protected static void reportEntity(String caption, OEntity entity){
        report(caption);
        for(OProperty<?> p : entity.getProperties()){
            report("  %s: %s",p.getName(),p.getValue());
        }
    }
    protected static int reportEntities(ODataConsumer c, String entitySet, int limit){
        report("entitySet: " + entitySet);
        Enumerable<OEntity> entities =  c.getEntities(entitySet).execute().take(limit);
        return reportEntities(entitySet, entities);
    }
    protected static int reportEntities(String entitySet, Enumerable<OEntity> entities){
        
        int count = 0;
        
        for(OEntity e :entities){
            reportEntity(entitySet + " entity" + count,e);
            count++;
        }
        report("total count: %s \n\n" , count);
        
        return count;
    }
    
    private static void reportProperties(List<EdmProperty> properties){
        for(EdmProperty property : properties){
            String p = String.format("Property Name=%s Type=%s Nullable=%s",property.name,property.type,property.nullable);
            if (property.maxLength != null)
                p = p + " MaxLength="+ property.maxLength;
            if (property.unicode != null)
                p = p + " Unicode="+ property.unicode;
            if (property.fixedLength != null)
                p = p + " FixedLength="+ property.fixedLength;   
            
            if (property.storeGeneratedPattern != null)
                p = p + " StoreGeneratedPattern="+ property.storeGeneratedPattern;  
            
            if (property.fcTargetPath != null)
                p = p + " TargetPath="+ property.fcTargetPath;
            if (property.fcContentKind != null)
                p = p + " ContentKind="+ property.fcContentKind;
            if (property.fcKeepInContent != null)
                p = p + " KeepInContent="+ property.fcKeepInContent;
            if (property.fcEpmContentKind != null)
                p = p + " EpmContentKind="+ property.fcEpmContentKind;
            if (property.fcEpmKeepInContent != null)
                p = p + " EpmKeepInContent="+ property.fcEpmKeepInContent;
            report("    "+ p);
        }
    }
    
    protected static void reportMetadata(EdmDataServices services){
        
        for(EdmSchema schema : services.schemas){
            report("Schema Namespace=%s",schema.namespace);
            
            for(EdmEntityType et : schema.entityTypes){
                String ets = String.format("  EntityType Name=%s",et.name);
                if (et.hasStream != null)
                    ets = ets + " HasStream="+et.hasStream;
                report(ets);
                
                for(String key : et.keys){
                    report("    Key PropertyRef Name=%s",key);
                }
                
                reportProperties(et.properties);
                for(EdmNavigationProperty np : et.navigationProperties){
                    report("    NavigationProperty Name=%s Relationship=%s FromRole=%s ToRole=%s",
                            np.name,np.relationship.getFQName(),np.fromRole.role,np.toRole.role);
                }
                 
            }
            for(EdmComplexType ct : schema.complexTypes){
                report("  ComplexType Name=%s",ct.name);
                
                reportProperties(ct.properties);
               
            }
            for(EdmAssociation assoc : schema.associations){
                report("  Association Name=%s",assoc.name);
                report("    End Role=%s Type=%s Multiplicity=%s",assoc.end1.role,assoc.end1.type.getFQName(),assoc.end1.multiplicity);
                report("    End Role=%s Type=%s Multiplicity=%s",assoc.end2.role,assoc.end2.type.getFQName(),assoc.end2.multiplicity);
            }
            for(EdmEntityContainer ec : schema.entityContainers){
                report("  EntityContainer Name=%s IsDefault=%s LazyLoadingEnabled=%s",ec.name,ec.isDefault,ec.lazyLoadingEnabled);
                
                for(EdmEntitySet ees : ec.entitySets){
                    report("    EntitySet Name=%s EntityType=%s",ees.name,ees.type.getFQName());
                }
                
                for(EdmAssociationSet eas : ec.associationSets){
                    report("    AssociationSet Name=%s Association=%s",eas.name,eas.association.getFQName());
                    report("      End Role=%s EntitySet=%s",eas.end1.role.role,eas.end1.entitySet.name);
                    report("      End Role=%s EntitySet=%s",eas.end2.role.role,eas.end2.entitySet.name);
                }
                
                for(EdmFunctionImport efi : ec.functionImports){
                    report("    FunctionImport Name=%s EntitySet=%s ReturnType=%s HttpMethod=%s",
                            efi.name,efi.entitySet.name,efi.returnType.getFQName(),efi.httpMethod);
                    for(EdmFunctionParameter efp : efi.parameters){
                        report("      Parameter Name=%s Type=%s Mode=%s",efp.name,efp.type,efp.mode);
                    }
                }
            }
        }
    }
    
    
}
