package se.skltp.ei.svc.service;

import riv.itintegration.engagementindex._1.EngagementTransactionType;

import java.util.List;

public interface MostRecentContentReturnedAsExpected extends CommonProcessTest {
    boolean  isTrueFor(List<EngagementTransactionType> processResult);

}
