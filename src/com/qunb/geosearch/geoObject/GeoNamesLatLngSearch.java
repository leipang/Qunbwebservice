package com.qunb.geosearch.geoObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;

public class GeoNamesLatLngSearch {
	private static int userNb = 0;
	private double lat;
	private double lng;
	private String level;
	private GeoName result;
	private DatastoreService datastore;
	private static String[] user = {"jbtheard","jbtheardgmail","cvinceyqunb","jbtheard01","jbtheard02","jbtheard03","jbtheard04","jbtheard05","jbtheard06","jbtheard07","jbtheard08","jbtheard09","jbtheard10","jbtheard11","jbtheard12","jbtheard13","jbtheard14","jbtheard15","jbtheard16","jbtheard17","jbtheard18","jbtheard19","jbtheard20","jbtheard21","jbtheard22","jbtheard23","jbtheard24","jbtheard25","jbtheard26","jbtheard27","jbtheard28","jbtheard29","jbtheard30","jbtheard31","jbtheard32","jbtheard33","leipang"};
	
	@SuppressWarnings("static-access")
	public GeoNamesLatLngSearch(double lat,double lng,String level) throws Exception{
		this.datastore=  DatastoreServiceFactory.getDatastoreService();
		this.lat = lat;
		this.lng = lng;
		this.level = level;
		this.result = this.constructResult();
	}
	private GeoName constructResult() throws Exception{
		GeoName mygeo = this.getData().get(0);
		List<GeoName> parents = constructParent(mygeo);
		for(GeoName geo:parents){
			System.out.println(geo.getName()+"---"+geo.getFcode());
		}
		if(this.level!=null&&!this.level.isEmpty()){
			for(GeoName geo:parents){
				String type = geo.getFcode();
				if(type.startsWith(toFcode(this.level))){
					return geo;
				}
			}
			return null;
		}
		else{
			int size = parents.size();
			return parents.get(size-1);
		}
	}
	public List<GeoName> getData() throws Exception{
		List<GeoName> output = new ArrayList<GeoName>();
		output = this.searchGeonames();
		return output;
	}
	public List<GeoName> searchGeonames(){
		List<GeoName> result = new ArrayList<GeoName>();
		try {
			String res = "";
            URL url = new URL("http://api.geonames.org/findNearbyJSON?formatted=true" +
            		"&lat="+this.lat+"&lng="+this.lng+"&lang=en&maxRows=10&username="+user[userNb]);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setReadTimeout(20000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
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
            	return searchGeonames();
            }
            Gson gson = new Gson();
            GeoNames geoNames = gson.fromJson(res, GeoNames.class);

            if(geoNames!=null)
            {
            	result = geoNames.getGeonames();
            }
		} catch (Exception ex) {
				ex.printStackTrace();
		}
		return result;
	}
	
	private List<GeoName> constructParent(GeoName geo){
		List<GeoName> result = new ArrayList<GeoName>();
		String id = geo.getGeonameId();
		try {
			String res = "";
            URL url = new URL("http://api.geonames.org/hierarchy?geonameId="+id+"&type=json&username="
    				+ user[userNb]);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setReadTimeout(20000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
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
            	return constructParent(geo);
            }
            Gson gson = new Gson();
            GeoNames geoNames = gson.fromJson(res, GeoNames.class);

            if(geoNames!=null)
            {
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
		googledata.add(getAllGeoInfo(this.getData().get(0).getGeonameId()).toEntity());
		for (int i = 0; i < googledata.size(); i++) {
			Entity geo = new Entity("GeoName", googledata.get(i)
					.getProperty("geonameId").toString());
			GeoDataStore.storeEntity(this.datastore, googledata.get(i));
			System.out.println("data added");
		}
	}
	public GeoName getAllGeoInfo(String geoid) throws IOException {
		String res = "";
		URL url=new URL("http://api.geonames.org/getJSON?formatted=true"
				+ "&geonameId=" + geoid + "&lang=en"+ "&username="
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
		JSONObject json_result = (JSONObject) JSONValue.parse(res);
		GeoName geoName = JsonToGeoName(json_result);
		return geoName;
	}
	public GeoName JsonToGeoName(JSONObject json) {
		GeoName geo = new GeoName();
		geo.setGeonameId(json.get("geonameId").toString());
		String name = json.get("name").toString().toLowerCase();
		name = name.replaceFirst(".",(name.charAt(0)+"").toUpperCase());
		geo.setName(name);
		String country = json.get("countryName").toString();
		country = country.replaceFirst(".",(country.charAt(0)+"").toUpperCase());
		geo.setCountryName(country);
		geo.setCountryCode(json.get("countryCode").toString().toUpperCase());
		geo.setLat(json.get("lat").toString());
		geo.setLng(json.get("lng").toString());
		geo.setFcl(json.get("fcl").toString());
		geo.setFcode(json.get("fcode").toString());
		if (json.get("population").toString().equals(null)) {
			System.out.println("no population");
			geo.setPopulation(String.valueOf(0));
		} else {
			geo.setPopulation(json.get("population").toString());
		}
		return geo;
	}

	public GeoName getResult(){
		return this.result;
	}
	
	private String toFcode(String type){
		FeatureName myfeature = FeatureName.valueOf(type.toLowerCase());
		String fcode = "";
		switch (myfeature){
		case continent:
			fcode = "CONT";
			break;
		case country:
			fcode = "PCLI";
			break;
		case region:
			fcode = "ADM1";
			break;
		case departement:
			fcode = "ADM2";
			break;
		case city:
			fcode = "PPL";
			break;
		case district:
			fcode = "PPL";
			break;
		case airport:
			fcode = "AIRP";
			break;
		}
		System.out.println(fcode);
		return fcode;
	}
	
	public enum FeatureName{
		continent,country,region,departement,city,airport,district;
	}

}
