package se.skltp.ei.intsvc.integrationtests.getupdatesservice.main;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.atLeastOnce;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex.getupdates._1.rivtabp21.GetUpdatesResponderInterface;
import riv.itintegration.engagementindex.getupdatesresponder._1.GetUpdatesResponseType;
import riv.itintegration.engagementindex.getupdatesresponder._1.GetUpdatesType;
import riv.itintegration.engagementindex.getupdatesresponder._1.RegisteredResidentEngagementType;
import riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import riv.itintegration.registry.getlogicaladdresseesbyservicecontract._1.rivtabp21.GetLogicalAddresseesByServiceContractResponderInterface;
import riv.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._1.GetLogicalAddresseesByServiceContractResponseType;
import riv.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._1.GetLogicalAddresseesByServiceContractType;
import se.skltp.ei.intsvc.getupdates.main.EngagementIndexPull;
import se.skltp.ei.intsvc.getupdates.service.GetUpdatesService;
import se.skltp.ei.intsvc.getupdates.utils.DateHelper;
import se.skltp.ei.intsvc.getupdates.utils.EngagementIndexHelper;
import se.skltp.ei.intsvc.getupdates.utils.HttpHelper;
import se.skltp.ei.intsvc.getupdates.utils.PropertyResolver;

/**
 * Author: Henrik Rostam
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PropertyResolver.class, DateHelper.class })
public class EngagementIndexPullTest {

    private final String namespacePropertyKey = "ei.push.address.servicedomain";
    private final String updateDestinationProperty = "ei.push.address.logical";
    private final String consumerHsaIdPropertyKey = "ei.pull.belongsto.hsaid";
    private final String addressServicePropertyKey = "ei.address.service.address.logical";
    private final String serviceDomainsPropertyKey = "ei.pull.address.servicedomains";
    private final String timeOffsetPropertyKey = "ei.pull.time.offset";
    private final String timestampFormatPropertyKey = "ei.pull.time.format";
    private final String dateFormat = "yyyyMMddHHmmss";

    @Mock
    private GetLogicalAddresseesByServiceContractResponderInterface getAddressesClient;

    @Mock
    private GetUpdatesResponderInterface getUpdatesClient;

    @Mock
    private UpdateResponderInterface updateClient;

    @Mock
    private Appender appender;

    @Mock
    private GetUpdatesService getUpdatesService;

    @Mock
    private HttpHelper httpHelper;

    private final String configuredPullLogicalAddress = "Mockito producer 1";

    @InjectMocks
    private EngagementIndexPull engagementIndexPull = new EngagementIndexPull(configuredPullLogicalAddress);

    @Before
    public void initTests() throws ParseException {
        mockStatic(PropertyResolver.class);
        mockStatic(DateHelper.class);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        final String addressService = "Mockito-address-service";
        final String serviceDomain = "mockito:test:namespace";
        final String consumerHsaId = "Mock170Test1D";
        final String pushAddress = "Mock170T35t4ddr3ss";
        final String serviceDomain1 = "Mockito";
        final String serviceDomain2 = "Testing";
        final String serviceDomain3 = "Rocks";
        final String serviceDomainList = serviceDomain1 + ", " + serviceDomain2 + ", " + serviceDomain3;
        final String testAddress1 = configuredPullLogicalAddress;
        final String testAddress2 = "Mockito producer 2";
        final String testAddress3 = "Mockito producer 3";
        final String testAddress4 = "Mockito producer 4";
        final String testAddress5 = "Mockito producer 5";
        final String timeOffset = "-123";
        final String engagementBookingId = "bookingId";
        final String engagementCategorization = "Booking";
        final String testDateString = "20120505150000";
        final Date testDate = simpleDateFormat.parse(testDateString);
        final String engagementOwner = "HSA-id";
        final String engagementLogicalAddress = "Landstingets hsaid:Vårdgivarens HSA-id:Enhetens hsaid";
        final String engagementSourceSystem = "Systemets HSA-ID";
        final String patientSsn = "012345678901";
        final List<String> testAddresses = new LinkedList<String>();
        testAddresses.add(testAddress1);
        testAddresses.add(testAddress2);
        testAddresses.add(testAddress3);
        testAddresses.add(testAddress4);
        testAddresses.add(testAddress5);

        when(PropertyResolver.get(eq(namespacePropertyKey))).thenReturn(serviceDomain);
        when(PropertyResolver.get(eq(consumerHsaIdPropertyKey))).thenReturn(consumerHsaId);
        when(PropertyResolver.get(eq(updateDestinationProperty))).thenReturn(pushAddress);
        when(PropertyResolver.get(eq(addressServicePropertyKey))).thenReturn(addressService);
        when(PropertyResolver.get(eq(serviceDomainsPropertyKey))).thenReturn(serviceDomainList);
        when(PropertyResolver.get(eq(timeOffsetPropertyKey))).thenReturn(timeOffset);
        when(PropertyResolver.get(eq(timestampFormatPropertyKey))).thenReturn(dateFormat);
        when(DateHelper.now()).thenReturn(testDate);
        LogManager.getRootLogger().addAppender(appender);

        EngagementType engagement = new EngagementType();
        engagement.setBusinessObjectInstanceIdentifier(engagementBookingId);
        engagement.setCategorization(engagementCategorization);
        engagement.setClinicalProcessInterestId(UUID.randomUUID().toString());
        engagement.setCreationTime(simpleDateFormat.format(testDate));
        engagement.setLogicalAddress(engagementLogicalAddress);
        engagement.setMostRecentContent(simpleDateFormat.format(testDate));
        engagement.setOwner(engagementOwner);
        engagement.setRegisteredResidentIdentification(patientSsn);
        engagement.setServiceDomain(serviceDomain);
        engagement.setSourceSystem(engagementSourceSystem);
        engagement.setUpdateTime(simpleDateFormat.format(testDate));

        RegisteredResidentEngagementType registeredResidentEngagementType = new RegisteredResidentEngagementType();
        registeredResidentEngagementType.setRegisteredResidentIdentification(patientSsn);
        registeredResidentEngagementType.getEngagement().add(engagement);

        GetUpdatesResponseType getUpdatesResponseType = new GetUpdatesResponseType();
        getUpdatesResponseType.getRegisteredResidentEngagement().add(registeredResidentEngagementType);
        getUpdatesResponseType.setResponseIsComplete(true);

        GetLogicalAddresseesByServiceContractResponseType addressResponse = new GetLogicalAddresseesByServiceContractResponseType();
        addressResponse.getLogicalAddress().addAll(testAddresses);

        when(getUpdatesService.getFormattedDateForGetUpdates(anyString(), anyString(), anyString())).thenReturn(testDateString);
        when(getUpdatesClient.getUpdates(anyString(), any(GetUpdatesType.class))).thenReturn(getUpdatesResponseType);
        when(getAddressesClient.getLogicalAddresseesByServiceContract(anyString(), any(GetLogicalAddresseesByServiceContractType.class))).thenReturn(addressResponse);
    }

    @Test
    public void testOneAddressFetchCall() {
        // Test
        engagementIndexPull.doFetchUpdates();
        // Verify
        verify(getAddressesClient, times(1)).getLogicalAddresseesByServiceContract(anyString(), any(GetLogicalAddresseesByServiceContractType.class));
    }

    @Test
    public void testAddressCorrectAddressCall() {
        // Setup
        ArgumentCaptor<String> addressServiceLogicalAddressCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<GetLogicalAddresseesByServiceContractType> getLogicalAddresseesByServiceContractTypeArgumentCaptor = ArgumentCaptor.forClass(GetLogicalAddresseesByServiceContractType.class);
        // Test
        engagementIndexPull.doFetchUpdates();
        // Verify
        verifyStatic(times(1));
        PropertyResolver.get(eq(addressServicePropertyKey));
        verifyStatic(times(1));
        PropertyResolver.get(eq(namespacePropertyKey));
        verify(getAddressesClient, times(1)).getLogicalAddresseesByServiceContract(addressServiceLogicalAddressCaptor.capture(), getLogicalAddresseesByServiceContractTypeArgumentCaptor.capture());
        assertEquals("The contacted address did not match the expected one!", PropertyResolver.get(addressServicePropertyKey), addressServiceLogicalAddressCaptor.getValue());
        String contactedNameSpace = getLogicalAddresseesByServiceContractTypeArgumentCaptor.getValue().getServiceContractNameSpace().getServiceContractNamespace();
        assertEquals("The contacted namespace did not match the expected one!", PropertyResolver.get(namespacePropertyKey), contactedNameSpace);
    }

    @Test
    public void testPushThenPull() {
        // Setup
        InOrder inOrder = inOrder(getUpdatesClient, updateClient);
        // Test
        engagementIndexPull.doFetchUpdates();
        // Verify
        inOrder.verify(getUpdatesClient).getUpdates(anyString(), any(GetUpdatesType.class));
        inOrder.verify(updateClient).update(anyString(), any(UpdateType.class));
    }

    @Test
    public void testFetchAmountOfCalls() {
        // Setup
        int amountOfServiceDomains = StringUtils.countMatches(PropertyResolver.get(serviceDomainsPropertyKey), ",") + 1;
        ArgumentCaptor<String> producerAddressCaptor = ArgumentCaptor.forClass(String.class);
        // Test
        engagementIndexPull.doFetchUpdates();
        // Verify
        verify(getUpdatesClient, times(amountOfServiceDomains)).getUpdates(producerAddressCaptor.capture(), any(GetUpdatesType.class));
    }

    @Test
    public void testTimeOffset() throws ParseException {
        String expectedDate = "20120505151337";
        when(getUpdatesService.getFormattedDateForGetUpdates(anyString(), anyString(), anyString())).thenReturn(expectedDate);
        ArgumentCaptor<GetUpdatesType> getUpdatesTypeArgumentCaptor = ArgumentCaptor.forClass(GetUpdatesType.class);
        // Test
        engagementIndexPull.doFetchUpdates();
        // Verify
        // One method call per configured service domain
        verify(getUpdatesClient, atLeastOnce()).getUpdates(anyString(), getUpdatesTypeArgumentCaptor.capture());
        List<GetUpdatesType> allValues = getUpdatesTypeArgumentCaptor.getAllValues();
        for (int i = 1; i < allValues.size(); i++) {
            String actualDate = allValues.get(i).getTimeStamp();
            assertEquals("The time used in number " + i + " of the getUpdates call differ from the expected one!", expectedDate, actualDate);
        }
    }

    @Test
    public void testUpdateAmountOfCalls() {
        // Setup
        int amountOfServiceDomains = StringUtils.countMatches(PropertyResolver.get(serviceDomainsPropertyKey), ",") + 1;
        // Test
        engagementIndexPull.doFetchUpdates();
        // Verify
        // Should fetch the update address just once.
        verifyStatic(times(1));
        PropertyResolver.get(eq(consumerHsaIdPropertyKey));
        verify(updateClient, times(amountOfServiceDomains)).update(anyString(), any(UpdateType.class));
    }

    @Test
    public void testCheckForUpdatesAddressesAndServiceDomain() {
        // Setup
        List<String> expectedServiceDomains = EngagementIndexHelper.stringToList(PropertyResolver.get(serviceDomainsPropertyKey));
        String expectedAddress = configuredPullLogicalAddress;
        ArgumentCaptor<String> logicalAddressArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<GetUpdatesType> getUpdatesTypeArgumentCaptor = ArgumentCaptor.forClass(GetUpdatesType.class);
        // Test
        engagementIndexPull.doFetchUpdates();
        // Verify
        verify(getUpdatesClient, atLeastOnce()).getUpdates(logicalAddressArgumentCaptor.capture(), getUpdatesTypeArgumentCaptor.capture());
        String actualAddress = logicalAddressArgumentCaptor.getValue();
        List<GetUpdatesType> getUpdatesTypes = getUpdatesTypeArgumentCaptor.getAllValues();
        List<String> actualServiceDomains = new LinkedList<String>();
        for (GetUpdatesType temp : getUpdatesTypes) {
             actualServiceDomains.add(temp.getServiceDomain());
        }
        // Verify that all called addresses were expected.
        // There is a glitch here, because the combination of actual addresses and service domains is not verified, however it seems that Mockito does not support verification of parameter combinations.
        // To accomplish this test, the combinations need to be hard coded.
        for (GetUpdatesType getUpdatesType : getUpdatesTypes) {
            String actualServiceDomain = getUpdatesType.getServiceDomain();
            assertTrue("The actual called address " + actualAddress + " using service domain " + actualServiceDomain + " was not expected!", expectedServiceDomains.contains(actualServiceDomain));
        }
        // Verify that all expected addresses were actually called.
        for (String expectedServiceDomain : expectedServiceDomains) {
            assertTrue("The expected called address " + expectedAddress + " using service domain " + expectedServiceDomain + " was not called!", actualServiceDomains.contains(expectedServiceDomain));
        }
    }

    @Test
    public void testCorrectEngagementIndexAddress() {
        // Setup
        int amountOfServiceDomains = StringUtils.countMatches(PropertyResolver.get(serviceDomainsPropertyKey), ",") + 1;
        // Test
        engagementIndexPull.doFetchUpdates();
        // Verify
        String destinationAddress = PropertyResolver.get(updateDestinationProperty);
        verify(updateClient, Mockito.times(amountOfServiceDomains)).update(eq(destinationAddress), any(UpdateType.class));
    }

    @Test
    public void testPushInformationIntegrity() {
        // Setup
        GetUpdatesResponseType expectedUpdate = getUpdatesClient.getUpdates(null, null);
        ArgumentCaptor<UpdateType> actualUpdatesArgumantCaptor = ArgumentCaptor.forClass(UpdateType.class);
        List<EngagementType> givenEngagementTypesFromUpdateService = new ArrayList<EngagementType>();
        List<RegisteredResidentEngagementType> expectedResidentEngagementTypes = expectedUpdate.getRegisteredResidentEngagement();
        for (RegisteredResidentEngagementType expectedResidentEngagementType : expectedResidentEngagementTypes) {
            givenEngagementTypesFromUpdateService.addAll(expectedResidentEngagementType.getEngagement());
        }
        List<EngagementType> pushedEngagementTypesToEngagementIndex = new ArrayList<EngagementType>();
        // Test
        engagementIndexPull.doFetchUpdates();
        // Get captured values
        updateClient.update(anyString(), actualUpdatesArgumantCaptor.capture());
        List<UpdateType> actualUpdates = actualUpdatesArgumantCaptor.getAllValues();
        // Collect the values in the pushed list
        for (UpdateType actualUpdateType : actualUpdates) {
            List<EngagementTransactionType> actualTransactionTypes = actualUpdateType.getEngagementTransaction();
            for (EngagementTransactionType actualTransactionType : actualTransactionTypes) {
                EngagementType actualSentData = actualTransactionType.getEngagement();
                pushedEngagementTypesToEngagementIndex.add(actualSentData);
            }
        }
        // Verify content
        for (EngagementType expectedEngagement : givenEngagementTypesFromUpdateService) {
            for (EngagementType actualEngagement : pushedEngagementTypesToEngagementIndex) {
                assertEquals("The value of registeredResidentInformation given by the producer mismatched with the value sent to the engagement index!", expectedEngagement.getRegisteredResidentIdentification(), actualEngagement.getRegisteredResidentIdentification());
                assertEquals("The value of serviceDomain given by the producer mismatched with the value sent to the engagement index!", expectedEngagement.getServiceDomain(), actualEngagement.getServiceDomain());
                assertEquals("The value of categorization given by the producer mismatched with the value sent to the engagement index!", expectedEngagement.getCategorization(), actualEngagement.getCategorization());
                assertEquals("The value of logicalAddress given by the producer mismatched with the value sent to the engagement index!", expectedEngagement.getLogicalAddress(), actualEngagement.getLogicalAddress());
                assertEquals("The value of businessObjectInstanceIdentified given by the producer mismatched with the value sent to the engagement index!", expectedEngagement.getBusinessObjectInstanceIdentifier(), actualEngagement.getBusinessObjectInstanceIdentifier());
                assertEquals("The value of clinicalProcessInterestId given by the producer mismatched with the value sent to the engagement index!", expectedEngagement.getClinicalProcessInterestId(), actualEngagement.getClinicalProcessInterestId());
                assertEquals("The value of mostRecentContent given by the producer mismatched with the value sent to the engagement index!", expectedEngagement.getMostRecentContent(), actualEngagement.getMostRecentContent());
                assertEquals("The value of sourceSystem given by the producer mismatched with the value sent to the engagement index!", expectedEngagement.getSourceSystem(), actualEngagement.getSourceSystem());
                assertEquals("The value of creationTime given by the producer mismatched with the value sent to the engagement index!", expectedEngagement.getCreationTime(), actualEngagement.getCreationTime());
                assertEquals("The value of updateTime given by the producer mismatched with the value sent to the engagement index!", expectedEngagement.getUpdateTime(), actualEngagement.getUpdateTime());
                assertEquals("The value of owner given by the producer mismatched with the value sent to the engagement index!", expectedEngagement.getOwner(), actualEngagement.getOwner());
                for (Object expectedEngagementAny : expectedEngagement.getAny()) {
                    for (Object actualEngagementAny : actualEngagement.getAny()) {
                        assertEquals("The extra parameters of the engagement type mismatched with the value sent to the engagement index!", expectedEngagementAny, actualEngagementAny);
                    }
                }
            }
        }
    }

    @Test
    public void testLoggingOfAddressService() {
        // Setup
        ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
        String logicalAddressWhichShouldBeLogged = PropertyResolver.get(addressServicePropertyKey);
        // Generate error
        when(getAddressesClient.getLogicalAddresseesByServiceContract(anyString(), any(GetLogicalAddresseesByServiceContractType.class))).thenThrow(new IllegalArgumentException("Mock-generated exception."));
        // Test
        engagementIndexPull.doFetchUpdates();
        // Verify
        verify(appender, Mockito.atLeastOnce()).doAppend(loggingEventArgumentCaptor.capture());
        // Get the first logging message
        LoggingEvent loggingEvent = loggingEventArgumentCaptor.getAllValues().get(1);
        // Check level of log message
        assertEquals("Logging level for exception should be error!", Level.ERROR, loggingEvent.getLevel());
        // Check that the message contains the logical address
        assertTrue("The logical address of the address service was not in the log message", StringUtils.contains(loggingEvent.getRenderedMessage(), logicalAddressWhichShouldBeLogged));
    }

    @Test
    public void testLoggingOfFetchUpdatesError() {
        // Setup
        ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
        List<String> testAddressesForFetchingData = getAddressesClient.getLogicalAddresseesByServiceContract(null, null).getLogicalAddress();
        List<String> testServiceDomainsForFetchingData = EngagementIndexHelper.stringToList(PropertyResolver.get(serviceDomainsPropertyKey));
        // Generate error after update.
        when(getUpdatesClient.getUpdates(anyString(), any(GetUpdatesType.class))).thenThrow(new IllegalArgumentException("Mock generated exception."));
        // Test
        engagementIndexPull.doFetchUpdates();
        // Verify
        verify(appender, atLeastOnce()).doAppend(loggingEventArgumentCaptor.capture());
        List<LoggingEvent> loggingEvents = loggingEventArgumentCaptor.getAllValues();
        for (LoggingEvent loggingEvent : loggingEvents) {
            if (loggingEvent.getLevel().toInt() != Priority.INFO_INT) {
                String renderedMessage = loggingEvent.getRenderedMessage();
                boolean containsLogicalAddress = false;
                for (String logicalAddressWhichShouldBeLogged : testAddressesForFetchingData) {
                    // One of these addresses should be in the log message
                    containsLogicalAddress = containsLogicalAddress || StringUtils.contains(renderedMessage, logicalAddressWhichShouldBeLogged);
                }
                assertTrue("The logical address of the producer was not in the log message when pulling data did not go as anticipated.", containsLogicalAddress);

                boolean containsServiceContract = false;
                for (String serviceContractWhichShouldBeLogged : testServiceDomainsForFetchingData) {
                    // One of these addresses should be in the log message
                    containsServiceContract = containsServiceContract || StringUtils.contains(renderedMessage, serviceContractWhichShouldBeLogged);
                }
                assertTrue("The service domain used to contact the producer was not in the log message when pulling data did not go as anticipated.", containsLogicalAddress);
            }
        }
    }

    @Test
    public void testLoggingOfLogicalAddresses() {
        ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
        String logicalAddressWhichShouldBeLogged = PropertyResolver.get(updateDestinationProperty);
        // Generate error after update.
        when(updateClient.update(anyString(), any(UpdateType.class))).thenThrow(new IllegalArgumentException("Mock generated exception."));
        // Test
        engagementIndexPull.doFetchUpdates();
        // Verify
        verify(appender, Mockito.atLeastOnce()).doAppend(loggingEventArgumentCaptor.capture());
        List<LoggingEvent> loggingEvents = loggingEventArgumentCaptor.getAllValues();
        for (LoggingEvent loggingEvent : loggingEvents) {
            // Check that the message contains the logical address
            boolean contains = (StringUtils.contains(loggingEvent.getRenderedMessage(), logicalAddressWhichShouldBeLogged) || StringUtils.contains(loggingEvent.getRenderedMessage(), configuredPullLogicalAddress));
            assertTrue("The logical address of the engagement index was not in the log message when pushing data did not go as anticipated.", contains);
        }
    }

    @After
    public void fell() {
        LogManager.getRootLogger().removeAppender(appender);
    }

}
