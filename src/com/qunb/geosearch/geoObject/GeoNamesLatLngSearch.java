package com.qunb.geosearch.geoObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;




import com.google.gson.Gson;

public class GeoNamesLatLngSearch {
	private double lat;
	private double lng;
	private String level;
	private GeoName result;
	
	@SuppressWarnings("static-access")
	public GeoNamesLatLngSearch(double lat,double lng,String level) throws Exception{
		this.lat = lat;
		this.lng = lng;
		this.level = level;
		this.result = this.constructResult();
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
            		"&lat="+this.lat+"&lng="+this.lng+"&lang=en&maxRows=100&username="+"leipang");
            URLConnection urlConnection = url.openConnection();
            urlConnection.setReadTimeout(20000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                res += line;
            }
            reader.close();

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
	private GeoName constructResult() throws Exception{
		GeoName mygeo = this.getData().get(0);
		List<GeoName> parents = constructParent(mygeo);
		for(GeoName geo:parents){
			System.out.println(geo.getToponymName()+"---"+geo.getFcode());
		}
		if(this.level!=null&&!this.level.isEmpty()){
			for(GeoName geo:parents){
				String type = geo.getFcode();
				if(type.equals(this.level)){
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
	private List<GeoName> constructParent(GeoName geo){
		List<GeoName> result = new ArrayList<GeoName>();
		int id = geo.getGeonameId();
		try {
			String res = "";
            URL url = new URL("http://api.geonames.org/hierarchy?geonameId="+String.valueOf(id)+"&type=json&username=jbtheard21");
            URLConnection urlConnection = url.openConnection();
            urlConnection.setReadTimeout(20000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                res += line;
            }
            reader.close();
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
	
	public void storeData(String datastore) throws Exception{
		System.out.println("Geonames finds "+this.getData().size()+" results!");
			String input = String.valueOf(this.getResult().getGeonameId());
			String geo_name = this.getResult().getName();
			System.out.println("Results found: "+ input);
			String get = "http://api.geonames.org/getJSON?geonameId="+input+"&username=jbtheard22";
			String jsonname = input+".json";
			String content = constructContentData(get);
			constructS3Object(content,jsonname,datastore);
			addtoIndex(geo_name,input,"index.json",datastore);
	}
	private String constructContentData(String urlpath) throws IOException{
		URL url = new URL(urlpath);  
		InputStreamReader in = new InputStreamReader(url.openStream());
		BufferedReader buffer=new BufferedReader(in);
		StringWriter out = new StringWriter();
		String line="";
		while ( null!=(line=buffer.readLine())){
			out.write(line); 
		}
		String content = out.toString();  
		return content;
	}
	private void constructS3Object(String content,String jsonname,String datastore) throws NoSuchAlgorithmException, IOException{
		QunbS3Store mystore = new QunbS3Store(datastore);
		byte[] cont = content.getBytes();
		String filename = jsonname.toLowerCase();
		mystore.getService().storeItem(filename, cont);
		System.out.println("---data stored---"+filename);
	}
	private void addtoIndex(String itemname,String geoid,String indexname,String datastore) throws IOException{
		QunbS3Store mystore = new QunbS3Store(datastore);
		String index_content = new String(mystore.getService().getItem(indexname),"utf-8");
		JSONObject index = (JSONObject) JSONValue.parse(index_content);
		index.put(geoid, itemname.toLowerCase());
		String content = index.toJSONString();
		byte[] cont = content.getBytes();
		String filename = "index.json";
		mystore.getService().storeItem(filename, cont);
		System.out.println("---index modified---");
	}
	public GeoName getResult(){
		return this.result;
	}

}
