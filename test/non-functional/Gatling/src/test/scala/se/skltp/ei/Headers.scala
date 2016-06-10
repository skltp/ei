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

object Headers {

  	// HTTP Headers
    val updateHttp_header = Map(
      "Accept-Encoding" -> "gzip,deflate",
      "Content-Type" -> "text/xml;charset=UTF-8",
      "SOAPAction" -> "urn:riv:itintegration:engagementindex:UpdateResponder:1",
      "Keep-Alive" -> "115",
	  "x-skltp-correlation-id" -> "eicorrelationid")
	  
	  val findContentHttp_header = Map(
      "Accept-Encoding" -> "gzip,deflate",
      "Content-Type" -> "text/xml;charset=UTF-8",
      "SOAPAction" -> "urn:riv:itintegration:engagementindex:FindContentResponder:1",
      "Keep-Alive" -> "115")
  }