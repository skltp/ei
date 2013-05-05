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
package se.skltp.ei.intsvc.findcontent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.engagementindex.findcontent._1.rivtabp21.FindContentResponderInterface;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentType;
import se.skltp.ei.svc.service.api.FindContentInterface;

public class FindContentWSBean implements FindContentResponderInterface {

    @SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(FindContentWSBean.class);

    private FindContentInterface blBean = null;
    
    public void setBlBean(FindContentInterface blBean) {
    	this.blBean = blBean;
    }
    
    /**
     *
     * @param logicalAddress
     * @param parameters
     * @return
     */
    @Override
    public FindContentResponseType findContent(String logicalAddress, FindContentType parameters) {
    	return blBean.findContent(null, parameters);
    }
}