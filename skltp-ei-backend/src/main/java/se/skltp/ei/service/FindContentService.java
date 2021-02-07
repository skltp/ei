package se.skltp.ei.service;

import org.springframework.transaction.annotation.Transactional;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentType;

public interface FindContentService {

  @Transactional(readOnly=true)
  FindContentResponseType findContent(FindContentType parameters);
}
