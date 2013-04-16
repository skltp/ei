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
