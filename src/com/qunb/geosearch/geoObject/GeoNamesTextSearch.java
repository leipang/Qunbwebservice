package com.qunb.geosearch.geoObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.geonames.Toponym;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;




public class GeoNamesTextSearch {
	private volatile WebService mysearch;
	private volatile int userNb = 0;
	private volatile String input;
	private volatile String limit;
	private volatile String lang;
	private volatile List<GeoName> result;
	private volatile List<JSONObject> returnresult;
	private volatile DatastoreService datastore;
	private volatile static String[] user = {"jbtheard","jbtheardgmail","cvinceyqunb","jbtheard01","jbtheard02","jbtheard03","jbtheard04","jbtheard05","jbtheard06","jbtheard07","jbtheard08","jbtheard09","jbtheard10","jbtheard11","jbtheard12","jbtheard13","jbtheard14","jbtheard15","jbtheard16","jbtheard17","jbtheard18","jbtheard19","jbtheard20","jbtheard21","jbtheard22","jbtheard23","jbtheard24","jbtheard25","jbtheard26","jbtheard27","jbtheard28","jbtheard29","jbtheard30","jbtheard31","jbtheard32","jbtheard33","leipang"};
	
	private static final Logger log = Logger.getLogger(GeoNamesTextSearch.class.getName());
	
	@SuppressWarnings("static-access")
	public GeoNamesTextSearch(String input,String limit, String type, String country,
			String lang) throws Exception {
		this.mysearch.setUserName("leipang");// TODO multi-user
		this.datastore=  DatastoreServiceFactory.getDatastoreService();
		this.input = input.replaceAll(" ", "%20");
		this.limit = limit;
		if (lang != null && !lang.isEmpty()) {
			this.lang = lang;
		} else {
			this.lang = "en";
		}
		this.result = this.constructResult();
		this.returnresult = new ArrayList<JSONObject>();
	}

	private List<GeoName> constructResult() throws Exception {
		return this.getData();
	}

	@SuppressWarnings("static-access")
	public List<GeoName> getData() throws Exception {
		List<GeoName> output = new ArrayList<GeoName>();
		String text = this.input;
		if (text.length() == 2) {
			output = seachCountryCode(text);
			if (output.size() == 0) {
				System.out.println("The input is not a country code");
				output = searchGeonames(text,this.limit);
			}
		} else {
			output = searchGeonames(text,this.limit);
		}
		return output;
	}

	public List<GeoName> searchGeonames(String text,String limit) throws MalformedURLException {
		URL url = null;
		if(limit==null||limit.isEmpty()){
			url = new URL(
					"http://api.geonames.org/search?formatted=true&type=json"
							+ "&name_equals=" + text + "&lang=" + this.lang
							+ "&maxRows=5&username=" + user[userNb]);
		}
		else{
			url = new URL(
					"http://api.geonames.org/search?formatted=true&type=json"
							+ "&name_equals=" + text + "&lang=" + this.lang
							+ "&maxRows="+limit+"&username=" + user[userNb]);
		}
		List<GeoName> result = new ArrayList<GeoName>();
		try {
			String res = "";
			URLConnection urlConnection = url.openConnection();
			urlConnection.setReadTimeout(20000);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				res += line;
			}
			reader.close();
			if(res.contains("limit")){
            	userNb++;
            	if(userNb>=user.length){
            		userNb=0;
            	}
            	return searchGeonames(text,limit);
            }
			Gson gson = new Gson();
			GeoNames geoNames = gson.fromJson(res, GeoNames.class);

			if (geoNames != null) {
				result = geoNames.getGeonames();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	public List<GeoName> seachCountryCode(String text) {
		List<GeoName> result = new ArrayList<GeoName>();
		try {
			System.out.println("Search the country of this country code");
			String res = "";
			URL url = new URL(
					"http://api.geonames.org/countryInfo?formatted=true&type=json"
							+ "&country=" + text + "&lang=" + this.lang
							+ "&maxRows=5&username=" + user[userNb]);
			URLConnection urlConnection = url.openConnection();
			urlConnection.setReadTimeout(20000);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				res += line;
			}
			reader.close();
			if(res.contains("limit")){
            	userNb++;
            	if(userNb>=user.length){
            		userNb=0;
            	}
            	return seachCountryCode(text);
            }

			Gson gson = new Gson();
			GeoNames geoNames = gson.fromJson(res, GeoNames.class);

			if (geoNames != null) {
				result = geoNames.getGeonames();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
	public void storeData() throws Exception {
		System.out.println("Geonames API find " + this.getData().size()
				+ " results!");
		List<Entity> googledata = new ArrayList<Entity>();
		for (GeoName geo : this.getData()) {
			googledata.add(getAllGeoInfo(geo.getGeonameId()).toEntity());
		}
		for (int i = 0; i < googledata.size(); i++) {
			GeoDataStore.storeEntity(this.datastore, googledata.get(i));
			System.out.println("data added");
		}
	}
	public synchronized GeoName getAllGeoInfo(String geoid) throws IOException {
		String res = "";
		URL url = new URL("http://api.geonames.org/getJSON?formatted=true"
				+ "&geonameId=" + geoid + "&lang=" + this.lang + "&username="
				+ user[userNb]);
		URLConnection urlConnection = url.openConnection();
		urlConnection.setReadTimeout(20000);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				urlConnection.getInputStream(),"UTF-8"));
		String line;
		while ((line = reader.readLine()) != null) {
			res += line;
		}
		reader.close();
		if(res.contains("limit")){
        	userNb++;
        	if(userNb>=user.length){
        		userNb=0;
        	}
        	return getAllGeoInfo(geoid);
        }
		if(res.contains("status")){
			log.info(res);
			System.out.println("relance la recherche!");
			return getAllGeoInfo(geoid);
		}
		JSONObject json_result = (JSONObject) JSONValue.parse(res);
		GeoName geoName = JsonToGeoName(json_result);
		this.returnresult.add(json_result);
		return geoName;
	}

	public GeoName JsonToGeoName(JSONObject json) {
		GeoName geo = new GeoName();
		geo.setGeonameId(json.get("geonameId").toString());
		String name = json.get("name").toString().toLowerCase();
		name = name.replaceFirst(".", (name.charAt(0) + "").toUpperCase());
		geo.setName(name);
		if (json.containsKey("countryName")) {
			String country = json.get("countryName").toString();
			country = country.replaceFirst(".",
					(country.charAt(0) + "").toUpperCase());
			geo.setCountryName(country);
		}
		else{
			geo.setCountryName("");
		}
		if (json.containsKey("countryCode")) {
			geo.setCountryCode(json.get("countryCode").toString());
		}
		else{
			geo.setCountryCode("");
		}
		
		if (json.containsKey("lat")) {
			geo.setLat(json.get("lat").toString());
		}
		else{
			geo.setLat("");
		}
		if (json.containsKey("lng")) {
			geo.setLng(json.get("lng").toString());
		}
		else{
			geo.setLng("");
		}
		if (json.containsKey("fcl")) {
			geo.setFcl(json.get("fcl").toString());
		}
		else{
			geo.setFcl("");
		}
		if (json.containsKey("fcode")) {
			geo.setFcode(json.get("fcode").toString());
		}
		else{
			geo.setFcode("");
		}
		
		if (json.get("population").toString().equals(null)) {
			System.out.println("no population");
			geo.setPopulation(String.valueOf(0));
		} else {
			geo.setPopulation(json.get("population").toString());
		}
		geo.setFclName(json.get("fclName").toString());
		return geo;
	}
	
	public List<Map<String, Object>> getReturnResult() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if (this.result != null) {
			for (int i = 0; i <=this.returnresult.size()-1; i++) {
				Map<String, Object> tmpmap = new HashMap<String, Object>();
				tmpmap.put("qunb:geoId", this.returnresult.get(i).get("geonameId"));
				tmpmap.put("qunb:geoName", this.returnresult.get(i).get("name"));
				tmpmap.put("qunb:geoLat", this.returnresult.get(i).get("lat"));
				tmpmap.put("qunb:geoLng", this.returnresult.get(i).get("lng"));
				tmpmap.put("qunb:population", this.returnresult.get(i).get("population"));
				tmpmap.put("qunb:fclcode",this.returnresult.get(i).get("fcl"));
				tmpmap.put("qunb:fcode",this.returnresult.get(i).get("fcode"));
				tmpmap.put("qunb:countryName",this.returnresult.get(i).get("countryName"));
				tmpmap.put("qunb:countryCode",this.returnresult.get(i).get("countryCode"));
				list.add(tmpmap);
			}
		}
		return list;
	}

	public List<GeoName> getResult() {
		return this.result;
	}
}
