/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.svc.service.impl.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import riv.itintegration.engagementindex._1.EngagementType;
import se.skltp.ei.svc.entity.model.Engagement;

public class EntityTransformer {

    /**
     * Makes sure multi-threaded access can be supported, i.e. SimpleDateFormat is not thread-safe.
     */
    static ThreadLocal<SimpleDateFormat> dateFormatters = new ThreadLocal<SimpleDateFormat>() {
      @Override
      protected SimpleDateFormat initialValue() {
          // strict syntax mode
          SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
          dateFormatter.setLenient(false);
          return dateFormatter;
      }
    };

    /**
     * Transform an engagement from the service model to the entity model
     * 
     * @param eIn
     * @return eOut
     */
    public static Engagement toEntity(EngagementType eIn) {

        Engagement eOut = new Engagement();

        eOut.setBusinessKey(eIn.getRegisteredResidentIdentification(),
                eIn.getServiceDomain(),
                eIn.getCategorization(),
                eIn.getLogicalAddress(),
                eIn.getBusinessObjectInstanceIdentifier(),
                eIn.getSourceSystem(),
                eIn.getDataController(),
                eIn.getOwner(),
                eIn.getClinicalProcessInterestId());

        eOut.setMostRecentContent(parseDate(eIn.getMostRecentContent()));

        return eOut;
    }

    /**
     * Transform an engagement from the service model to the entity model
     * 
     * @param eIn
     * @param owner The owner that should be used instead of the current one in ein
     * @return eOut
     */
    public static Engagement toEntity(EngagementType eIn, String owner) {

        Engagement eOut = new Engagement();

        eOut.setBusinessKey(eIn.getRegisteredResidentIdentification(),
                eIn.getServiceDomain(),
                eIn.getCategorization(),
                eIn.getLogicalAddress(),
                eIn.getBusinessObjectInstanceIdentifier(),
                eIn.getSourceSystem(),
                eIn.getDataController(),
                owner,
                eIn.getClinicalProcessInterestId());

        eOut.setMostRecentContent(parseDate(eIn.getMostRecentContent()));

        return eOut;
    }

    /**
     * Transform an engagement from the entity model to the service model
     * 
     * @param eIn
     * @return eOut
     */
    public static EngagementType fromEntity(Engagement eIn) {

        EngagementType eOut = new EngagementType();
        eOut.setRegisteredResidentIdentification(eIn.getRegisteredResidentIdentification());
        eOut.setServiceDomain(eIn.getServiceDomain());
        eOut.setCategorization(eIn.getCategorization());
        eOut.setLogicalAddress(eIn.getLogicalAddress());
        eOut.setBusinessObjectInstanceIdentifier(eIn.getBusinessObjectInstanceIdentifier());
        eOut.setSourceSystem(eIn.getSourceSystem());
        eOut.setOwner(eIn.getOwner());
        eOut.setClinicalProcessInterestId(eIn.getClinicalProcessInterestId());
        eOut.setDataController(eIn.getDataController());
        eOut.setCreationTime(forrmatDate(eIn.getCreationTime()));
        eOut.setMostRecentContent(forrmatDate(eIn.getMostRecentContent()));	    
        eOut.setUpdateTime(forrmatDate(eIn.getUpdateTime()));

        return eOut;
    }

    //
    private static String format(Date date) {
        return dateFormatters.get().format(date);
    }

    //
    private static Date parse(String date) {
        try {
            return dateFormatters.get().parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Input date is not according to expected format \"YYYYMMDDhhmmss\": " + date, e);
        }

    }

    /**
     * Returns a formatted date according to the service contract format.
     * 
     * @param date the date to format.
     * @return the formatted date.
     */
    public static String forrmatDate(Date date) {
        return (date == null) ? null : format(date);
    }

    /**
     * Returns a date.
     * 
     * @param date the date in text.
     * @return the date.
     */
    public static Date parseDate(String date) {
        return (date == null) ? null : parse(date);
    }

}
