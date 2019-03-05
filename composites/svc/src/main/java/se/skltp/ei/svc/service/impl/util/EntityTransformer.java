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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.mule.util.Preconditions;
import riv.itintegration.engagementindex._1.EngagementType;
import se.skltp.ei.svc.entity.model.Engagement;

/**
 * Transforms service model beans (JAXB), to/from persistent entity model beans (JPA).
 * 
 * @author Magnus Larsson
 * @author Peter
 */
public class EntityTransformer {

    /**
     * Makes sure multi-threaded access to date formatting and parsing is supported, 
     * i.e. SimpleDateFormat is not thread-safe.
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
     * @param eIn the service model bean.
     * @return the corresponding entity model bean.
     */
    public static Engagement toEntity(EngagementType eIn) {
        return toEntity(eIn, eIn.getOwner());
    }

    /**
     * Transform an engagement from the service model to the entity model
     * 
     * @param eIn the service model bean
     * @param owner owner of this entity
     * @return the corresponding entity model bean
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
     * @param eIn the entity model bean
     * @return the corresponding service model bean
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
        eOut.setCreationTime(formatDate(eIn.getCreationTime()));
        eOut.setMostRecentContent(formatDate(eIn.getMostRecentContent()));	    
        eOut.setUpdateTime(formatDate(eIn.getUpdateTime()));

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
    public static String formatDate(Date date) {
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


    private static Date dateDaysFromDate(LocalDateTime pSource,int pDays){
            return localDateTime2Date(pSource.plusDays(pDays));
    }

    private static Date localDateTime2Date(LocalDateTime pSource){
        return Date.from(pSource.atZone(ZoneId.systemDefault()).toInstant());
    }
    private static LocalDateTime date2LocalDateTime(Date pSource){
        return LocalDateTime.ofInstant(pSource.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Returns a date: prior, equal or greater than the given pDate
     * dependent on pDays being lt|eqt|gt than zero
     * @param pDate source
     * @param pDays deviation in days negative is before positive after
     * @return
     */
    public static Date dateDaysFromDate(Date pDate ,int pDays){
        Preconditions.checkState(pDate!=null,"Please make sure pDate is Assigned");
        return dateDaysFromDate(date2LocalDateTime(pDate),pDays);
    }

    /**
     *
     * @param pDays positive values render a result date in the future and negative renders dates in the past
     * @return returns a date that deviates from to days date by the given amount of pDate
     */
    public static Date dateDaysFromNow(int pDays){

        return dateDaysFromDate(new Date(),pDays);
    }

    public static String dateDaysFromStrDate(String pStrDate,int pDays){
        Preconditions.checkArgument(pStrDate!=null,"pStrDate must be assigned");
        return formatDate(dateDaysFromDate(parse(pStrDate),pDays));
    }


}
