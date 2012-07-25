package com.qunb.rest.dbpedia;
import java.io.IOException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

import org.xml.sax.SAXException;



public class DBpediaSearch {
	public static JSONObject findresult(URL input) throws IOException, ParserConfigurationException, SAXException, DOMException, JSONException{
		JSONObject result = new JSONObject();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(input.openStream());
		document.getDocumentElement ().normalize (); 
		int result_number= document.getDocumentElement().getElementsByTagName("Result").getLength();
		for(int i=0;i<result_number;i++){
			   JSONObject one_result=  new JSONObject();
			   String label = document.getDocumentElement().getElementsByTagName("Result").item(i).getChildNodes().item(1).getTextContent();
			   String uri = document.getDocumentElement().getElementsByTagName("Result").item(i).getChildNodes().item(3).getTextContent();
			   one_result.put("Label",label );
			   one_result.put("URI", uri);
			   
			   if(i==0){
				   try {
					   result.put("Results", one_result);
				   } catch (JSONException e) {
					   e.printStackTrace();
				   }
			   }
			   else{
				   try {
					   result.accumulate("Results", one_result);
				   } catch (JSONException e) {
					   e.printStackTrace();
				   }
			   }
		   }
		return result;
	}

}
