package org.odata4j.test.unit.format.xml;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.odata4j.format.xml.AtomCollectionInfo;
import org.odata4j.format.xml.AtomServiceDocumentFormatParser;
import org.odata4j.format.xml.AtomWorkspaceInfo;
import org.odata4j.stax2.XMLEventReader2;
import org.odata4j.stax2.util.StaxUtil;




public class AtomServiceDocumentFormatParserTest {


    // Test for Issue #258
    @Test
    public void absentXMLBaseTest() {

        // Arrange
        XMLEventReader2 reader = StaxUtil.newXMLEventReader(new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/META-INF/issue258_service_document.xml"))));

        // Act
        Iterable<AtomWorkspaceInfo> workspaces = AtomServiceDocumentFormatParser.parseWorkspaces(reader);

        // Assert
        AtomWorkspaceInfo workspaceInfo = workspaces.iterator().next();
        List<AtomCollectionInfo> collections = workspaceInfo.getCollections();
        Assert.assertEquals(2, collections.size());
        Assert.assertEquals("http://vnote:8080/InfoBaseOdata/odata/infobase.odata/Catalogs_Shops", collections.get(0).getHref());
        Assert.assertEquals("http://vnote:8080/InfoBaseOdata/odata/infobase.odata/Catalogs_Regions", collections.get(1).getHref());


    }


}
