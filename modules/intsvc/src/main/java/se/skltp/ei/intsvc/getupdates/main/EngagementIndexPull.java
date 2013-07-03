package se.skltp.ei.intsvc.getupdates.main;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.getupdates._1.rivtabp21.GetUpdatesResponderInterface;
import riv.itintegration.engagementindex.getupdatesresponder._1.GetUpdatesResponseType;
import riv.itintegration.engagementindex.getupdatesresponder._1.GetUpdatesType;
import riv.itintegration.engagementindex.getupdatesresponder._1.RegisteredResidentEngagementType;
import riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import riv.itintegration.registry._1.ServiceContractNamespaceType;
import riv.itintegration.registry.getlogicaladdresseesbyservicecontract._1.rivtabp21.GetLogicalAddresseesByServiceContractResponderInterface;
import riv.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._1.GetLogicalAddresseesByServiceContractResponseType;
import riv.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._1.GetLogicalAddresseesByServiceContractType;
import se.skltp.ei.intsvc.getupdates.service.GetUpdatesService;
import se.skltp.ei.intsvc.getupdates.utils.DateHelper;
import se.skltp.ei.intsvc.getupdates.utils.EngagementIndexHelper;
import se.skltp.ei.intsvc.getupdates.utils.HttpHelper;
import se.skltp.ei.intsvc.getupdates.utils.PropertyResolver;

/**
 * Authors: Hans Thunberg, Henrik Rostam
 */

public class EngagementIndexPull {

    @Autowired
    private GetLogicalAddresseesByServiceContractResponderInterface getAddressesClient;

    @Autowired
    private GetUpdatesResponderInterface getUpdatesClient;

    @Autowired
    private UpdateResponderInterface updateClient;

    @Autowired
    private GetUpdatesService getUpdatesService;

    @Autowired
    private HttpHelper httpHelper;

    private static final Logger log = LoggerFactory.getLogger(EngagementIndexPull.class);

    private String configuredPullLogicalAddress;

    public EngagementIndexPull(String pullLogicalAddress) {
        this.configuredPullLogicalAddress = pullLogicalAddress;
    }

    public void doFetchUpdates() {
        log.info("Pull/Push-sequence for " + configuredPullLogicalAddress + " started.");
        httpHelper.configHttpConduit(getAddressesClient);
        httpHelper.configHttpConduit(updateClient);
        httpHelper.configHttpConduit(getUpdatesClient);

        final String pushServiceContractNamespace = PropertyResolver.get("ei.push.address.servicedomain");
        final String serviceConsumerHsaId = PropertyResolver.get("ei.pull.belongsto.hsaid");
        final String logicalAddress = PropertyResolver.get("ei.address.service.address.logical");
        final String pushLogicalAddress = PropertyResolver.get("ei.push.address.logical");
        final String commaSeparatedDomains = PropertyResolver.get("ei.pull.address.servicedomains");
        final String timestampFormat = PropertyResolver.get("ei.pull.time.format");
        final List<String> serviceDomainList = EngagementIndexHelper.stringToList(commaSeparatedDomains);
        final GetLogicalAddresseesByServiceContractType parameters = generateAddressParameters(pushServiceContractNamespace, serviceConsumerHsaId);
        final String pullLogicalAddress = configuredPullLogicalAddress;
        List<String> possiblePullAddresses = new ArrayList<String>();
        try {
            GetLogicalAddresseesByServiceContractResponseType addressResponse = getAddressesClient.getLogicalAddresseesByServiceContract(logicalAddress, parameters);
            possiblePullAddresses = addressResponse.getLogicalAddress();
        } catch (Exception e) {
            log.error("Could not acquire addresses from " + logicalAddress + " which should be contacted for pulling data. Reason:\n", e);
        }
        pushAndPull(possiblePullAddresses, pullLogicalAddress, pushLogicalAddress, serviceDomainList, timestampFormat);
        log.info("Pull/Push-sequence for " + configuredPullLogicalAddress + " ended.");
    }

    private void pushAndPull(List<String> possiblePullAddresses, String pullLogicalAddress, String pushLogicalAddress, List<String> serviceDomainList, String timestampFormat) {
        if (possiblePullAddresses.contains(pullLogicalAddress)) {
            for (String serviceDomain : serviceDomainList) {
                doPushAndPull(pullLogicalAddress, serviceDomain, pushLogicalAddress, timestampFormat);
            }
        } else {
            log.error("The address list of allowed logical addresses does not contain the requested logical address '" + pullLogicalAddress + "'. No fetching of updates could be done at this time.");
            for (String serviceDomain : serviceDomainList) {
                getUpdatesService.incrementErrorsSinceLastFetch(pullLogicalAddress, serviceDomain);
            }
        }
    }

    private void doPushAndPull(String pullLogicalAddress, String serviceDomain, String pushLogicalAddress, String timestampFormat) {
        String lastFetchedRegisteredResidentIdentification = "";
        int amountOfFetchedResults = 0;
        // Continue while there is more data to fetch
        boolean done = false;
        boolean success;
        Date dateForLastFetch = DateHelper.now();
        do {
            String timeForLastSuccessfulUpdate = getUpdatesService.getFormattedDateForGetUpdates(pullLogicalAddress, serviceDomain, timestampFormat);
            GetUpdatesResponseType updates = pull(pullLogicalAddress, serviceDomain, timeForLastSuccessfulUpdate, lastFetchedRegisteredResidentIdentification);
            if (updates != null) {
                done = updates.isResponseIsComplete();
                log.info("Received " + updates.getRegisteredResidentEngagement().size() + " updates from: " + pullLogicalAddress + " using service domain: " + serviceDomain + ".");
                success = push(pushLogicalAddress, updates);
                if (!done) {
                    // There are more results to fetch, build list of what we fetched so far, since the producer is stateless.
                    lastFetchedRegisteredResidentIdentification = getLastRegisteredResidentEngagementFromList(updates);
                    amountOfFetchedResults += updates.getRegisteredResidentEngagement().size();
                }
            } else {
                success = false;
                log.error("Received null when pulling data since: " + timeForLastSuccessfulUpdate + ", from address: " + pullLogicalAddress + ", using service domain: " + serviceDomain + ".\nPreviously fetched: " + amountOfFetchedResults + " partial results from this address.");
            }
        } while (!done && success);
        if (success) {
            getUpdatesService.updateDateForGetUpdates(pullLogicalAddress, serviceDomain, dateForLastFetch);
        } else {
            log.error("Unable to push data from '" + pullLogicalAddress + "' using service domain '" + serviceDomain + "'.\nEither an error was in the response code from '" + pushLogicalAddress + "', or the updates from the producer was null.");
            getUpdatesService.incrementErrorsSinceLastFetch(pullLogicalAddress, serviceDomain);
        }
    }

    private String getLastRegisteredResidentEngagementFromList(GetUpdatesResponseType updates) {
        List<RegisteredResidentEngagementType> registeredResidentEngagements = updates.getRegisteredResidentEngagement();
        int listSize = registeredResidentEngagements.size();
        int indexOfLastElement = listSize - 1;
        return registeredResidentEngagements.get(indexOfLastElement).getRegisteredResidentIdentification();
    }

    private GetUpdatesResponseType pull(String pullLogicalAddress, String serviceDomain, String timeForLastSuccessfulUpdate, String lastFetchedRegisteredResidentIdentification) {
        GetUpdatesType updateRequest = new GetUpdatesType();
        updateRequest.setServiceDomain(serviceDomain);
        updateRequest.setTimeStamp(timeForLastSuccessfulUpdate);
        updateRequest.setRegisteredResidentLastFetched(lastFetchedRegisteredResidentIdentification);
        try {
            return getUpdatesClient.getUpdates(pullLogicalAddress, updateRequest);
        } catch (Exception e) {
            log.error("Could not aquire updates from " + pullLogicalAddress + ", using service domain: " + updateRequest.getServiceDomain() + ". Reason:\n", e);
        }
        return null;
    }

    private boolean push(String logicalAddress, GetUpdatesResponseType updates) {
        UpdateType requestForUpdate = createRequestForUpdate(updates);
        try {
            UpdateResponseType updateResponse = updateClient.update(logicalAddress, requestForUpdate);
            ResultCodeEnum resultCode = updateResponse.getResultCode();
            switch (resultCode) {
                case OK:
                    log.info("Received " + resultCode.name() + " when updating to " + logicalAddress + ".");
                    break;
                case INFO:
                    log.warn("Received unexpected result with code " + resultCode.name() + ". Response comment: " + updateResponse.getComment() + "." + updates.getRegisteredResidentEngagement().size() + " posts was however, successfully pushed to " + logicalAddress + ".");
                    break;
                case ERROR:
                    log.error("Result containing " + updates.getRegisteredResidentEngagement().size() + " posts was pushed to " + logicalAddress + ", however an error response code was in the reply!\nResult code: " + resultCode.name() + ".\nUpdate response comment: " + updateResponse.getComment());
                    return false;
            }
        } catch (Exception e) {
            log.error("Error while trying to update index! " + updates.getRegisteredResidentEngagement().size() + " posts were unable to be pushed to: " + logicalAddress + ". Reason:\n", e);
            return false;
        }
        return true;
    }

    private UpdateType createRequestForUpdate(GetUpdatesResponseType updateResponse) {
        UpdateType requestForUpdate = new UpdateType();
        List<RegisteredResidentEngagementType> registeredEngagementTypes = updateResponse.getRegisteredResidentEngagement();
        if (!registeredEngagementTypes.isEmpty()) {
            for (RegisteredResidentEngagementType registeredResidentEngagementType : registeredEngagementTypes) {
                List<EngagementType> engagementTypes = registeredResidentEngagementType.getEngagement();
                if (!engagementTypes.isEmpty()) {
                    addTransactionsToUpdateRequest(engagementTypes, requestForUpdate);
                } else {
                    // Engagement list was either null or empty
                    log.debug("Engagement list was either null or empty, no data added to the engagement transaction.");
                }
            }
        } else {
            // RegisteredResidentEngagement list was either null or empty.
            log.debug("Registered resident engagement list was either null or empty, no data added to the engagement transaction.");
        }
        return requestForUpdate;
    }

    private void addTransactionsToUpdateRequest(List<EngagementType> engagementTypes, UpdateType requestForUpdate) {
        for (EngagementType engagementType : engagementTypes) {
            EngagementTransactionType engagementTransaction = new EngagementTransactionType();
            // Var hittar man informtion om vad som skall tas bort?
            // engagementTransaction.setDeleteFlag(value);
            engagementTransaction.setEngagement(engagementType);
            requestForUpdate.getEngagementTransaction().add(engagementTransaction);
        }
    }

    private GetLogicalAddresseesByServiceContractType generateAddressParameters(String pushServiceContractNamespace, String belongsToHsaId) {
        GetLogicalAddresseesByServiceContractType parameters = new GetLogicalAddresseesByServiceContractType();
        ServiceContractNamespaceType serviceContractNameSpace = new ServiceContractNamespaceType();
        serviceContractNameSpace.setServiceContractNamespace(pushServiceContractNamespace);
        parameters.setServiceContractNameSpace(serviceContractNameSpace);
        parameters.setServiceConsumerHsaId(belongsToHsaId);
        return parameters;
    }

}
