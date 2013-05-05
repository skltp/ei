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
package se.skltp.ei.intsvc.process;

import org.mule.api.MuleMessage;
import org.mule.api.expression.ExpressionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotifyEvaluator implements ExpressionEvaluator {

    private static final Logger log = LoggerFactory.getLogger(NotifyEvaluator.class);

    private static final String NAME = "ei-perform-notify";
    
    public String getName() {
    	log.debug("Return evaluator name {}", NAME);
        return NAME;
    }

    public void setName(String name) {
        throw new UnsupportedOperationException("setName");
    }

    public Object evaluate(String expression, MuleMessage message) {
        try {
        	log.info("Evaluate: {} on message {}", expression, message.getPayload());

    		// FIXME - ML: True if Update or if PN and ET.size > 0

        	log.info("Evaluator return true");
        	return true;

        } catch (Exception e) {
        	log.warn("Evaluator failed, return true", e);
        	return true;
        }
    }
}