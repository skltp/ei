package se.skltp.ei.intsvc.getupdates.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.skltp.ei.intsvc.getupdates.domain.GetUpdatesStatus;
import se.skltp.ei.intsvc.getupdates.repository.GetUpdatesStatusRepository;
import se.skltp.ei.intsvc.getupdates.utils.DateHelper;
import se.skltp.ei.intsvc.getupdates.utils.EngagementIndexHelper;
import se.skltp.ei.intsvc.getupdates.utils.PropertyResolver;

/**
 * Author: Henrik Rostam
 */

@Service("getUpdatesService")
public class GetUpdatesService {

    @Autowired
    private GetUpdatesStatusRepository getUpdatesStatusRepository;

    public List<GetUpdatesStatus> fetchAll() {
        return getUpdatesStatusRepository.fetchAll();
    }

    public String getFormattedDateForGetUpdates(String logicalAddress, String serviceDomain, String timestampFormat) {
        GetUpdatesStatus status = getUpdatesStatusRepository.getStatusForLogicalAddressAndServiceContract(logicalAddress, serviceDomain);
        if (status == null || status.getLastSuccess() == null) {
            // No recorded date for last success existed, let's default to property offset.
            String timeOffset = PropertyResolver.get("ei.pull.time.offset");
            return EngagementIndexHelper.getFormattedOffsetTime(DateHelper.now(), timeOffset, timestampFormat);
        }
        int secondsToRemove = (-(NumberUtils.toInt(PropertyResolver.get("ei.pull.time.margin"))));
        Date returnDate = getPastTimeInSeconds(status.getLastSuccess(), secondsToRemove);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timestampFormat);
        return simpleDateFormat.format(returnDate);
    }

    private synchronized Date getPastTimeInSeconds(Date date, int secondsToRemove) {
        // Remove some time from last success time to make sure in case updates were made just when the update was made.
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, secondsToRemove);
        return calendar.getTime();
    }

    public void updateDateForGetUpdates(String logicalAddress, String serviceDomain, Date timeOfFetch) {
        GetUpdatesStatus status = getUpdatesStatusRepository.getStatusForLogicalAddressAndServiceContract(logicalAddress, serviceDomain);
        if (status == null) {
            // Let's create one
            status = new GetUpdatesStatus();
            status.setLogicalAddress(logicalAddress);
            status.setServiceDomain(serviceDomain);
            status.setLastSuccess(timeOfFetch);
            status.setAmountOfErrorsSinceLastSuccess(0);
            getUpdatesStatusRepository.save(status);
        } else {
            // Just update time
            status.setLastSuccess(timeOfFetch);
            status.setAmountOfErrorsSinceLastSuccess(0);
            getUpdatesStatusRepository.update(status);
        }
    }

    public void incrementErrorsSinceLastFetch(String logicalAddress, String serviceDomain) {
        GetUpdatesStatus status = getUpdatesStatusRepository.getStatusForLogicalAddressAndServiceContract(logicalAddress, serviceDomain);
        if (status == null) {
            // Let's create one
            status = new GetUpdatesStatus();
            status.setLogicalAddress(logicalAddress);
            status.setServiceDomain(serviceDomain);
            status.setLastSuccess(null);
            status.setAmountOfErrorsSinceLastSuccess(1);
            getUpdatesStatusRepository.save(status);
        } else {
            // Just increase errors by one
            int amountOfErrorsSinceLastSuccess = status.getAmountOfErrorsSinceLastSuccess() + 1;
            status.setAmountOfErrorsSinceLastSuccess(amountOfErrorsSinceLastSuccess);
            getUpdatesStatusRepository.update(status);
        }
    }

}
