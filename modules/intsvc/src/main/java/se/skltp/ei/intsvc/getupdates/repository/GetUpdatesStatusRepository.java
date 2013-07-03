package se.skltp.ei.intsvc.getupdates.repository;

import java.util.List;

import se.skltp.ei.intsvc.getupdates.domain.GetUpdatesStatus;

/**
 * Author: Henrik Rostam
 */

public interface GetUpdatesStatusRepository {

    List<GetUpdatesStatus> fetchAll();

    GetUpdatesStatus getStatusForLogicalAddressAndServiceContract(String logicalPullAddress, String pullServiceDomain);

    void save(GetUpdatesStatus status);

    void update(GetUpdatesStatus status);

    void delete(GetUpdatesStatus status);

}
