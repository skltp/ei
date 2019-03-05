package se.skltp.ei.svc.service;

import se.skltp.ei.svc.entity.model.Engagement;

import java.util.List;

public interface MostRecentContentPersistedAsExpected extends CommonProcessTest{
       boolean  isTrueFor(List<Engagement> processResult);

}


