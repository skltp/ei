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
package se.skltp.ei.intsvc.dynamicFlows;

import groovy.text.GStringTemplateEngine;
import groovy.text.Template;

import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceCodeGenerator {

	private static final Logger log = LoggerFactory.getLogger(SourceCodeGenerator.class);

	private GStringTemplateEngine gStringEngine = new GStringTemplateEngine();
	private String templateFile;
	private Template template;
	
	public SourceCodeGenerator(String templateFile) {

		this.templateFile = templateFile;
		
		URL url = getClass().getClassLoader().getResource(templateFile);
		if (url == null) {
			String erroMsg = "NO TEMPLATE FOUND FOR " + templateFile;
			log.error(erroMsg);
			throw new RuntimeException(erroMsg);
		} else {
			log.debug("Loaded template: " + url.getPath());
		}
		
		try {
			template = gStringEngine.createTemplate(url);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String generateContent(Map<String, Object> parameters) {
		try {
			String content = template.make(parameters).toString();
			log.debug("Generated content:\n{}", content);
			return content;
		} catch (Exception e) {
			log.error("Failed generate templatefile: " + templateFile, e);
			throw new RuntimeException(e);
		}
	}
}
