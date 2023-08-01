package com.sample.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.daffodil.japi.Diagnostic;
import org.apache.daffodil.japi.ParseResult;
import org.apache.daffodil.japi.UnparseResult;
import org.apache.daffodil.japi.infoset.JDOMInfosetInputter;
import org.apache.daffodil.japi.infoset.JDOMInfosetOutputter;
import org.apache.daffodil.japi.io.InputSourceDataInputStream;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sample.config.DaffoConfig;
import com.sample.model.Request;
import com.sample.model.Response;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DaffodilService {

	
	@Autowired
	DaffoConfig dfConfig;


	public String converDataToFixedWidthString(Request request) throws JAXBException, JDOMException, IOException {

        log.info("Convert Jaxb Object to XML String");
		String xmlString = convertObjectToStr(request);
		log.debug("Converted XML String : "+xmlString);
		Document doc = convertStringToDocument(xmlString);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		WritableByteChannel wbc = Channels.newChannel(new DataOutputStream(bos));
		JDOMInfosetInputter inputter = new JDOMInfosetInputter(doc);
		try {
			log.info("Before Daffodil Unparsing");
			UnparseResult result = dfConfig.getDataProcessor().unparse(inputter, wbc);

			if (result.isError()) {
				List<Diagnostic> diags = result.getDiagnostics();
				for (Diagnostic d : diags) {
					log.error("Daffodil Unparsing Error : "+ d.getSomeMessage());
				}
			}
			log.info("After Daffodil Unparsing");

			return bos.toString();
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}finally { 
			wbc.close();
		}
	}

	
	// Convert FixedwidthString to XML object
	public Response parseResponse(String fixedWidthString) {

		InputStream is = new ByteArrayInputStream(fixedWidthString.getBytes());
		InputSourceDataInputStream dis = new InputSourceDataInputStream(is);
		JDOMInfosetOutputter outputter = new JDOMInfosetOutputter();
		Response response = new Response();

		try {
			log.info("Before Daffodil Parsing");
			ParseResult res = dfConfig.getReponseDataProcessor().parse(dis, outputter);
			if (res.isError()) {
				List<Diagnostic> diags = res.getDiagnostics();
				for (Diagnostic d : diags) {
					log.error("Daffodil parsing Error : "+ d.getSomeMessage());
				}
			}
			log.info("After Daffodil Parsing");
			Document doc2 = outputter.getResult().getDocument();
			XMLOutputter xo = new XMLOutputter(org.jdom2.output.Format.getPrettyFormat());
			String xmlString = xo.outputString(doc2);
			log.debug("Parsed xmlString : " +xmlString);
			response = convertXmlStringtToObject(xmlString);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return response;
	}

	public static Response convertXmlStringtToObject(String xmlString) throws JAXBException{
		JAXBContext jaxbContext = null;
		Unmarshaller  unMarshaller = null;
		Response vpc = null;
		jaxbContext=JAXBContext.newInstance(Response.class);
		unMarshaller = jaxbContext.createUnmarshaller();
		StringReader reader = new StringReader(xmlString);
		vpc = (Response) unMarshaller.unmarshal(reader);
		return vpc;

	}

	public static String convertObjectToStr(Request pojoObject) throws JAXBException{
		JAXBContext jaxbContext = null;
		Marshaller marshaller = null;
		StringWriter writer=null;
		jaxbContext=JAXBContext.newInstance(Request.class);
		marshaller = jaxbContext.createMarshaller();
		writer= new StringWriter();
		marshaller.marshal(pojoObject, writer);
		return writer.toString();

	}

	public static Document convertStringToDocument(String xmlStr) throws JDOMException, IOException {

		SAXBuilder sb=new SAXBuilder();
		Document doc=sb.build(new StringReader(xmlStr));
		return doc;
	}

}
