package com.qunb.rest.dbpedia;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.xml.sax.SAXException;
import org.json.JSONException;
import org.json.JSONObject;

import com.qunb.rest.unit.UnitValidator;

import javax.ws.rs.Path;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

@Path("/dbpedia/search")

public class QunbDbpediaResource extends ServerResource {
	
	
		
		@Get
	    public JSONObject search() throws IOException, JSONException, ParserConfigurationException, SAXException, TransformerException {
			String path = "http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?";
			String input = getQuery().getValues("q");
			String search = path+"QueryString="+input;
			if(getQuery().getValues("class")!=null&&!getQuery().getValues("class").isEmpty()){
				String type = getQuery().getValues("class");
				search = search+"&QueryClass="+type;
			}
			if(getQuery().getValues("limit")!=null&&!getQuery().getValues("limit").isEmpty()){
				String limit = getQuery().getValues("limit");
				search = search+"&MaxHits="+limit;
			}
			
			URL search_url = new URL(search);
			JSONObject results = new JSONObject();
			results = DBpediaSearch.findresult(search_url);
			return results;
			
			}

	}

