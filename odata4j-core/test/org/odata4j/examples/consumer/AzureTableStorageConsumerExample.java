package org.odata4j.examples.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.ODataConsumers;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.examples.BaseExample;

public class AzureTableStorageConsumerExample extends BaseExample {

    public static void main(String[] args) {

        String[] azureCreds = System.getenv("AZURESTORAGE").split(":");
        String account = azureCreds[0];
        String key = azureCreds[1];
        
        ODataConsumer c = ODataConsumers.azureTables(account,key);

       
        report("Create a new temp table to use for the test");
        String tableName = "TempTable" + System.currentTimeMillis();
        c.createEntity("Tables").properties(OProperties.string("TableName", tableName)).execute();
        reportEntities(c,"Tables",100);


        // create composite key to use for entity-level tests
        OProperty<?>[] entityKey = new OProperty<?>[]{OProperties.string("PartitionKey", ""), OProperties.string("RowKey", "1")};
      
        report("Ensure the new entity does not exist");
        c.deleteEntity(tableName, entityKey).execute();
        reportEntities(c,tableName,100);
        
        report("Create a new entity");
        OEntity newEntity = c.createEntity(tableName).properties(entityKey).properties(
                OProperties.string("foo", "bar"))
                .execute();
        reportEntities(c,tableName,100);
        
        report("Update the new entity");
        c.updateEntity(newEntity, tableName,entityKey)
            .properties(OProperties.string("Value", "Sortof Large")).execute();
        reportEntities(c,tableName,100);
        
        report("Merge the new entity");
        c.mergeEntity(tableName, entityKey)
            .properties(OProperties.string("foo", "baz")).execute();
        reportEntities(c,tableName,100);
        
        report("Delete the new entity");
        c.deleteEntity(tableName, entityKey).execute();
        reportEntities(c,tableName,100);
        
        
        
        report("Delete the temp table");
        c.deleteEntity("Tables", tableName).execute();
    }



}
