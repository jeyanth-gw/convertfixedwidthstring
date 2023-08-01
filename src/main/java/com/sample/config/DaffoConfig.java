package com.sample.config;

import java.io.File;
import java.io.IOException;

import org.apache.daffodil.japi.Compiler;
import org.apache.daffodil.japi.Daffodil;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.ProcessorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DaffoConfig {

	@Bean
	public DataProcessor getDataProcessor() throws IOException {
		Compiler c = Daffodil.compiler();
		DaffoConfig dc = new DaffoConfig();
		File schemaFile = dc.getResource("Request.xsd");
		ProcessorFactory pf = c.compileFile(schemaFile);
		DataProcessor dp = pf.onPath("/");
		
		return dp;
	}
	
	@Bean
	public DataProcessor getReponseDataProcessor() throws IOException {
		Compiler c = Daffodil.compiler();
		DaffoConfig dc = new DaffoConfig();
		File schemaFile = dc.getResource("Response.xsd");
		ProcessorFactory pf = c.compileFile(schemaFile);
		DataProcessor dp = pf.onPath("/");
		
		return dp;
	}
	
	public File getResource(String resPath) {
        try {
        	 ClassLoader classLoader = getClass().getClassLoader();
        	 return new File(classLoader.getResource(resPath).toURI());
        } catch (Exception e) {
        	e.printStackTrace();
        	return null;
        }
    }

}
