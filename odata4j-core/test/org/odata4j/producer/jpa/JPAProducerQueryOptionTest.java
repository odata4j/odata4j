package org.odata4j.producer.jpa;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.examples.producer.ProducerUtil;
import org.odata4j.producer.resources.ODataProducerProvider;
import org.odata4j.producer.server.JerseyServer;

/**
 *
 * @author sergei.grizenok
 */
public class JPAProducerQueryOptionTest {

    private static final String endpointUri = "http://localhost:8810/northwind/Northwind.svc/";
    private static EntityManagerFactory emf;
    private static JerseyServer server;

    @BeforeClass
    public static void setUpClass() throws Exception {
        String persistenceUnitName = "NorthwindService";
        String namespace = "Northwind";

        emf = Persistence.createEntityManagerFactory(
                persistenceUnitName);

        JPAProducer producer = new JPAProducer(
                emf,
                namespace,
                50);

        NorthwindTestUtils.fillDatabase(emf);

        ODataProducerProvider.setInstance(producer);
        server = ProducerUtil.startODataServer(endpointUri);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (server != null) {
            server.stop();
        }

        if (emf != null) {
            emf.close();
        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void SystemQueryOptionOrderByTest() {
        String inp = "SystemQueryOptionOrderByTest";
        String uri = "Products?$top=20&$orderby=ProductID";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void SystemQueryOptionOrderByDescTest() {
        String inp = "SystemQueryOptionOrderByDescTest";
        String uri = "Products?$top=10&$orderby=ProductID desc";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void SystemQueryOptionTopTest() {
        String inp = "SystemQueryOptionTopTest";
        String uri = "Products?$top=5";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void SystemQueryOptionOrderByTopTest() {
        String inp = "SystemQueryOptionOrderByTopTest";
        String uri = "Products?$top=5&$orderby=ProductName desc";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void SystemQueryOptionSkipTest() {
        String inp = "SystemQueryOptionSkipTest";
        String uri = "Categories(1)/Products?$skip=2";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void SystemQueryOptionOrderBySkipTest() {
        String inp = "SystemQueryOptionOrderBySkipTest";
        String uri = "Products?$skip=2&$top=2&$orderby=ProductName";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void SystemQueryOptionFilterEqualTest() {
        String inp = "SystemQueryOptionFilterEqualTest";
        String uri = "Suppliers?$filter=Country eq 'Brazil'";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void SystemQueryOptionFilterNotEqualTest() {
        String inp = "SystemQueryOptionFilterNotEqualTest";
        String uri = "Suppliers?$filter=Country ne 'UK'";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void SystemQueryOptionFilterGreaterThanTest() {
        String inp = "SystemQueryOptionFilterGreaterThanTest";
        String uri = "Products?$top=20&$filter=UnitPrice gt 20";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void SystemQueryOptionFilterGreaterThanOrEqualTest() {
        String inp = "SystemQueryOptionFilterGreaterThanOrEqualTest";
        String uri = "Products?$top=20&$filter=UnitPrice ge 10";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void SystemQueryOptionFilterLessThanTest() {
        String inp = "SystemQueryOptionFilterLessThanTest";
        String uri = "Products?$top=20&$filter=UnitPrice lt 20";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void SystemQueryOptionFilterLessThanOrEqualTest() {
        String inp = "SystemQueryOptionFilterLessThanOrEqualTest";
        String uri = "Products?$top=20&$filter=UnitPrice le 100";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void SystemQueryOptionFilterLogicalAndTest() {
        String inp = "SystemQueryOptionFilterLogicalAndTest";
        String uri = "Products?$top=20&$filter=UnitPrice le 200 and UnitPrice gt 3.5";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void SystemQueryOptionFilterLogicalOrTest() {
        String inp = "SystemQueryOptionFilterLogicalOrTest";
        String uri = "Products?$filter=UnitPrice le 3.5 or UnitPrice gt 200";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

    @Test
    public void SystemQueryOptionFilterGroupingLogicalAndTest() {
        String inp = "SystemQueryOptionFilterGroupingLogicalAndTest";
        String uri = "Products?$top=10&$filter=%28UnitPrice%20gt%205%29%20and%20%28UnitPrice%20lt%2020%29";
        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
    }

//    @Test
//    public void SystemQueryOptionFilterLogicalNotTest() {
//        String inp = "SystemQueryOptionFilterLogicalNotTest";
//        String uri = "Products?$filter=not endswith(QuantityPerUnit,'bags')";
//         NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterAdditionTest() {
//        String inp = "SystemQueryOptionFilterAdditionTest";
//        String uri = "Products?$filter=(UnitPrice add 5) gt 10";
//         NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterSubtractionTest() {
//        String inp = "SystemQueryOptionFilterSubtractionTest";
//        String uri = "Products?$filter=UnitPrice sub 5 gt 10";
//         NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterMultiplicationTest() {
//        String inp = "SystemQueryOptionFilterMultiplicationTest";
//        String uri = "Products?$filter=UnitPrice mul 2 gt 2000";
//         NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterDivisionTest() {
//        String inp = "SystemQueryOptionFilterDivisionTest";
//        String uri = "Products?$filter=UnitPrice div 2 gt 4";
//         NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterModuloTest() {
//        String inp = "SystemQueryOptionFilterModuloTest";
//        String uri = "Products?$filter=UnitPrice mod 2 eq 1.5";
//         NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterGroupingArithmeticSubTest() {
//        String inp = "SystemQueryOptionFilterGroupingArithmeticSubTest";
//        String uri = "Products?$filter=(UnitPrice sub 5) gt 10";
//         NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }

//    @Test
//    public void SystemQueryOptionFilterBoolSubstringOfTest() {
//        String inp = "SystemQueryOptionFilterBoolSubstringOfTest";
//        String uri = "Customers?$filter=substringof('Alfreds', CompanyName) eq true";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterBoolEndswithTest() {
//        String inp = "SystemQueryOptionFilterBoolEndswithTest";
//        String uri = "Customers?$filter=endswith(CompanyName, 'Futterkiste') eq true";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterBoolStartswithTest() {
//        String inp = "SystemQueryOptionFilterBoolStartswithTest";
//        String uri = "Customers?$filter=startswith(CompanyName, 'Alfr') eq true";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterIntLengthTest() {
//        String inp = "SystemQueryOptionFilterIntLengthTest";
//        String uri = "Customers?$filter=length(CompanyName) eq 19";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterIntIndexofTest() {
//        String inp = "SystemQueryOptionFilterIntIndexofTest";
//        String uri = "Customers?$filter=indexof(CompanyName, 'lfreds') eq 1";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterStringReplaceTest() {
//        String inp = "SystemQueryOptionFilterStringReplaceTest";
//        String uri = "Customers?$filter=replace(CompanyName, ' ', '') eq 'AlfredsFutterkiste'";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterStringSubstringTest() {
//        String inp = "SystemQueryOptionFilterStringSubstringTest";
//        String uri = "Customers?$filter=substring(CompanyName, 1) eq 'lfreds Futterkiste'";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterStringSubstring2Test() {
//        String inp = "SystemQueryOptionFilterStringSubstring2Test";
//        String uri = "Customers?$filter=substring(CompanyName, 1, 2) eq 'lf'";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterStringToLowerTest() {
//        String inp = "SystemQueryOptionFilterStringToLowerTest";
//        String uri = "Customers?$filter=tolower(CompanyName) eq 'alfreds futterkiste'";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterStringToupperTest() {
//        String inp = "SystemQueryOptionFilterStringToupperTest";
//        String uri = "Customers?$filter=toupper(CompanyName) eq 'ALFREDS FUTTERKISTE'";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterStringTrimTest() {
//        String inp = "SystemQueryOptionFilterStringTrimTest";
//        String uri = "Customers?$filter=trim(CompanyName) eq 'Alfreds Futterkiste'";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterStringConcatTest() {
//        String inp = "SystemQueryOptionFilterStringConcatTest";
//        String uri = "Customers?$filter=concat(concat(City, ', '), Country) eq 'Berlin, Germany'";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterIntDayTest() {
//        String inp = "SystemQueryOptionFilterIntDayTest";
//        String uri = "Employees?$filter=day(BirthDate) eq 8";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterIntHourTest() {
//        String inp = "SystemQueryOptionFilterIntHourTest";
//        String uri = "Employees?$filter=hour(BirthDate) eq 0";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterIntMinuteTest() {
//        String inp = "SystemQueryOptionFilterIntMinuteTest";
//        String uri = "Employees?$filter=minute(BirthDate) eq 0";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterIntMonthTest() {
//        String inp = "SystemQueryOptionFilterIntMonthTest";
//        String uri = "Employees?$filter=month(BirthDate) eq 12";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterIntSecondTest() {
//        String inp = "SystemQueryOptionFilterIntSecondTest";
//        String uri = "Employees?$filter=second(BirthDate) eq 0";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterIntYearTest() {
//        String inp = "SystemQueryOptionFilterIntYearTest";
//        String uri = "Employees?$filter=year(BirthDate) eq 1948";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterRoundTest() {
//        String inp = "SystemQueryOptionFilterRoundTest";
//        String uri = "Orders?$filter=round(Freight) eq 32";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterDecimalFloorTest() {
//        String inp = "SystemQueryOptionFilterDecimalFloorTest";
//        String uri = "Orders?$filter=floor(Freight) eq 32";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterDoubleCeilingTest() {
//        String inp = "SystemQueryOptionFilterDoubleCeilingTest";
//        String uri = "Orders?$filter=ceiling(Freight) eq 33";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterDecimalCeilingTest() {
//        String inp = "SystemQueryOptionFilterDecimalCeilingTest";
//        String uri = "Orders?$filter=floor(Freight) eq 33";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterBoolIsOfTest() {
//        String inp = "SystemQueryOptionFilterBoolIsOfTest";
//        String uri = "Orders?$filter=isof('NorthwindModel.Order')";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionFilterBoolIsOf2Test() {
//        String inp = "SystemQueryOptionFilterBoolIsOf2Test";
//        String uri = "Orders?$filter=isof(ShipCountry, 'Edm.String')";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }

//    @Test
//    public void SystemQueryOptionExpand1Test() {
//        String inp = "SystemQueryOptionExpand1Test";
//        String uri = "Categories?$expand=Products";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionExpand2Test() {
//        String inp = "SystemQueryOptionExpand2Test";
//        String uri = "Categories?$expand=Products/Supplier";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionExpand3Test() {
//        String inp = "SystemQueryOptionExpand3Test";
//        String uri = "Products?$expand=Category,Supplier";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }

//    @Test
//    public void SystemQueryOptionFormatAtomTest() {
//        String inp = "SystemQueryOptionFormatAtomTest";
//        String uri = "Products?$format=atom";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }

//    @Test
//    public void SystemQueryOptionSelect1Test() {
//        String inp = "SystemQueryOptionSelect1Test";
//        String uri = "Products?$top=20&$select=UnitPrice,ProductName";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
//
//    @Test
//    public void SystemQueryOptionSelect2Test() {
//        String inp = "SystemQueryOptionSelect2Test";
//        String uri = "Products?$top=20&$select=ProductName,Category";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
    
//    @Test
//    public void SystemQueryOptionFormatJsonTest() {
//        String inp = "SystemQueryOptionFormatJsonTest";
//        String uri = "Products?$top=20&$format=json";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }

    
//    @Test
//    public void SystemQueryOptionInlinecountTest() {
//        String inp = "SystemQueryOptionInlinecountTest";
//        String uri = "Products?$top=20&$inlinecount=allpages";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }

//    @Test
//    public void SystemQueryOptionInlinecountTopTest() {
//        String inp = "SystemQueryOptionInlinecountTopTest";
//        String uri = "Products?$inlinecount=allpages&$top=5&Price gt 200";
//        NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
//    }
}
