/**
 * Copyright (c) 2013 Center for eHalsa i samverkan (CeHis).
 * 							<http://cehis.se/>
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
package se.skltp.ei 

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._
import java.util.concurrent.atomic._

object Scenarios {

    val rampUpTimeSecs = 10
	
	// Update engagement 1 OK
	val valueCitizen_1 = new AtomicInteger(1)
	val ids_1 = Iterator.continually(Map("citizenId1" -> (Conf.startPnr + "1" +  valueCitizen_1.getAndAdd(1).toString )))
	
	val scn_Update_1_Ok = scenario("Update 1 OK")	
  		.during(Conf.testTimeSecs) {     
    		feed(ids_1)
			.exec(
				http("Update 1 OK")
				.post("/skltp-ei/update-service/v1")
				.headers(Headers.updateHttp_header)
				.body(ELFileBody("data/Update_1_OK.xml")).asXML
				.check(status.is(200))
				.check(xpath("soap:Envelope", List("soap" -> "http://schemas.xmlsoap.org/soap/envelope/")).exists)
				.check(xpath("//pr:UpdateResponse", List("pr" ->"urn:riv:itintegration:engagementindex:UpdateResponder:1")).count.is(1))
	            .check(xpath("//pr:ResultCode/text()", List("pr" ->"urn:riv:itintegration:engagementindex:UpdateResponder:1")).is("OK"))
				)
	  	.pause(500 milliseconds, 1500 milliseconds)
	}
	
	// Update engagement 1 OK
	val format = new java.text.SimpleDateFormat("yyyyMMddHHmmss")
	val timestamp = Iterator.continually(Map("timestamp" -> ( format.format(new java.util.Date()) )))
	val pnr = csv("pnr.csv").circular
	
	val scn_Update_1_Duplicates_Ok = scenario("Update 1 DUPLICATES OK")	
  		.during(Conf.testTimeSecs) {     
    		feed(timestamp).feed(pnr)
			.exec(
				http("Update 1 Duplicates OK")
				.post("/skltp-ei/update-service/v1")
				.headers(Headers.updateHttp_header)
				.body(ELFileBody("data/Update_1_DUPLICATES_OK.xml")).asXML
				.check(status.is(200))
				.check(xpath("soap:Envelope", List("soap" -> "http://schemas.xmlsoap.org/soap/envelope/")).exists)
				.check(xpath("//pr:UpdateResponse", List("pr" ->"urn:riv:itintegration:engagementindex:UpdateResponder:1")).count.is(1))
	            .check(xpath("//pr:ResultCode/text()", List("pr" ->"urn:riv:itintegration:engagementindex:UpdateResponder:1")).is("OK"))
				)
	  	.pause(19 seconds, 21 seconds)
	}

	// Update engagement 50 OK
	val valueCitizen_50 = new AtomicInteger(1)
	val ids_50 = Iterator.continually(Map("citizenId1" -> (Conf.startPnr + "2" + valueCitizen_50.getAndAdd(1).toString )))
	
	val scn_Update_50_Ok = scenario("Update 50 OK")	
  		.during(Conf.testTimeSecs) {     
    		feed(ids_50)
			.exec(
				http("Update 50 OK")
				.post("/skltp-ei/update-service/v1")
				.headers(Headers.updateHttp_header)
				.body(ELFileBody("data/Update_50_OK.xml")).asXML
				.check(status.is(200))
				.check(xpath("soap:Envelope", List("soap" -> "http://schemas.xmlsoap.org/soap/envelope/")).exists)
				.check(xpath("//pr:UpdateResponse", List("pr" ->"urn:riv:itintegration:engagementindex:UpdateResponder:1")).count.is(1))	        
	            .check(xpath("//pr:ResultCode/text()", List("pr" ->"urn:riv:itintegration:engagementindex:UpdateResponder:1")).is("OK"))
				)
	  	.pause(2000 milliseconds, 2800 milliseconds)
	}

	// Update engagement 500 OK
	val valueCitizen_1_500 = new AtomicInteger(1)
	val valueCitizen_2_500 = new AtomicInteger(1)
	val ids1_500 = Iterator.continually(Map("citizenId1" -> (Conf.startPnr + "3" + valueCitizen_1_500.getAndAdd(1).toString )))
	val ids2_500 = Iterator.continually(Map("citizenId2" -> (Conf.startPnr + "4" + valueCitizen_2_500.getAndAdd(1).toString )))
	
	val scn_Update_500_Ok = scenario("Update 500 OK")	
  		.during(Conf.testTimeSecs) {     
    		feed(ids1_500).feed(ids2_500)
			.exec(
				http("Update 500 OK")
				.post("/skltp-ei/update-service/v1")
				.headers(Headers.updateHttp_header)
				.body(ELFileBody("data/Update_500_OK.xml")).asXML
				.check(status.is(200))
				.check(xpath("soap:Envelope", List("soap" -> "http://schemas.xmlsoap.org/soap/envelope/")).exists)
				.check(xpath("//pr:UpdateResponse", List("pr" ->"urn:riv:itintegration:engagementindex:UpdateResponder:1")).count.is(1))	        
	            .check(xpath("//pr:ResultCode/text()", List("pr" ->"urn:riv:itintegration:engagementindex:UpdateResponder:1")).is("OK"))
				)
	  	.pause(25 seconds, 35 seconds)
	}

	// Update engagement 1000 OK
	val valueCitizen_1_1000 = new AtomicInteger(1);
	val valueCitizen_2_1000 = new AtomicInteger(1);
	val valueCitizen_3_1000 = new AtomicInteger(1);
	val valueCitizen_4_1000 = new AtomicInteger(1);
	
	val ids1_1000 = Iterator.continually(Map("citizenId1" -> (Conf.startPnr + "5" + valueCitizen_1_1000.getAndAdd(1).toString )))
	val ids2_1000 = Iterator.continually(Map("citizenId2" -> (Conf.startPnr + "6" + valueCitizen_2_1000.getAndAdd(1).toString )))
	val ids3_1000 = Iterator.continually(Map("citizenId3" -> (Conf.startPnr + "7" + valueCitizen_3_1000.getAndAdd(1).toString )))
	val ids4_1000 = Iterator.continually(Map("citizenId4" -> (Conf.startPnr + "8" + valueCitizen_4_1000.getAndAdd(1).toString )))
	
	val scn_Update_1000_Ok = scenario("Update 1000 OK")	
  		.during(Conf.testTimeSecs) {     
    		feed(ids1_1000).feed(ids2_1000).feed(ids3_1000).feed(ids4_1000)
			.exec(
				http("Update 1000 OK")
				.post("/skltp-ei/update-service/v1")
				.headers(Headers.updateHttp_header)
				.body(ELFileBody("data/Update_1000_OK.xml")).asXML
				.check(status.is(200))
				.check(xpath("soap:Envelope", List("soap" -> "http://schemas.xmlsoap.org/soap/envelope/")).exists)
				.check(xpath("//pr:UpdateResponse", List("pr" ->"urn:riv:itintegration:engagementindex:UpdateResponder:1")).count.is(1))	        
	            .check(xpath("//pr:ResultCode/text()", List("pr" ->"urn:riv:itintegration:engagementindex:UpdateResponder:1")).is("OK"))
				)
		.pause(9 seconds, 11 seconds)
	}
	
	// FindContent
	val valueCitizen_FindContent = new AtomicInteger(1);
	val ids_FC = Iterator.continually(Map("citizenId1" -> (Conf.startPnr + "2" + valueCitizen_FindContent.getAndAdd(1).toString )))
	val scn_FindContent_Ok = scenario("FindContent OK")	
  		.during(Conf.testTimeSecs) {     
			feed(ids_FC)
			.exec(
				http("FindContent OK")
				.post("/skltp-ei/find-content-service/v1")
				.headers(Headers.findContentHttp_header)
				.body(ELFileBody("data/FindContent_OK.xml")).asXML
				.check(status.is(200))
				.check(xpath("soap:Envelope", List("soap" -> "http://schemas.xmlsoap.org/soap/envelope/")).exists)
				.check(xpath("//pr:FindContentResponse", List("pr" ->"urn:riv:itintegration:engagementindex:FindContentResponder:1")).count.is(1))	        
				.check(xpath("//pl:engagement", List("pl" ->"urn:riv:itintegration:engagementindex:FindContentResponder:1")).count.is(50))	        
				)
	  	.pause(5 seconds, 6 seconds)
	}
}
