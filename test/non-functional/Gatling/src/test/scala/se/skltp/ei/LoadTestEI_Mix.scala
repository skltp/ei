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

class LoadTestEI_Mix extends Simulation {

    setUp(
		Scenarios.scn_Update_1_Ok.inject(rampUsers(Conf.noOfUsers) over (30 seconds)).protocols(Conf.httpConf),
		Scenarios.scn_Update_50_Ok.inject(nothingFor(5 seconds),rampUsers(Conf.noOfUsers) over (25 seconds)).protocols(Conf.httpConf),
		Scenarios.scn_Update_500_Ok.inject(nothingFor(10 seconds),rampUsers(Conf.noOfUsers) over (20 seconds)).protocols(Conf.httpConf),
		Scenarios.scn_Update_1000_Ok.inject(nothingFor(15 seconds),rampUsers(Conf.noOfUsers) over (15 seconds)).protocols(Conf.httpConf)		
	)
}