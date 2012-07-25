package com.qunb.geosearch.geoObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geonames.Toponym;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.gson.Gson;




public class GeoNamesTextSearch {
	private WebService mysearch;
	private String input;
	private String type;
	private String country;
	private String lang;
	private List<GeoName> result;
	private List<JSONObject> returnresult;
	
	@SuppressWarnings("static-access")
	public GeoNamesTextSearch(String input,String type,String country,String lang) throws Exception{
		this.mysearch.setUserName("leipang");//TODO multi-user
		this.input = input.replaceAll(" ", "%20");
		this.type = type;
		this.country = country;
		if(lang!=null&&!lang.isEmpty()){
			this.lang=lang;
		}
		else{
			this.lang="en";
		}
		this.result = this.constructResult();
		this.returnresult = new ArrayList<JSONObject>();
	}
	
	@SuppressWarnings("static-access")
	public List<GeoName> getData() throws Exception{
		List<GeoName> output = new ArrayList<GeoName>();
		String text = this.input;
		if(text.length()==2){
			output = seachCountryCode(text);
			if(output.size()==0){
				System.out.println("The input is not a country code");
				output = searchGeonames(text);
			}
		}
		else{
			output = searchGeonames(text);
		}

		return output;
	}
	public List<GeoName> searchGeonames(String text){
		List<GeoName> result = new ArrayList<GeoName>();
		try {
			String res = "";
            URL url = new URL("http://api.geonames.org/search?formatted=true&type=json" +
            		"&name_equals="+text+"&lang="+this.lang+"&maxRows=5&username="+"leipang");
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
	public List<GeoName> seachCountryCode(String text){
		List<GeoName> result = new ArrayList<GeoName>();
		try {
			System.out.println("Search the country of this country code");
			String res = "";
            URL url = new URL("http://api.geonames.org/countryInfo?formatted=true&type=json" +
            		"&country="+text+"&lang="+this.lang+"&maxRows=5&username="+"leipang");
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

	private List<GeoName> constructResult() throws Exception{
		return this.getData();
	}
	
	public void storeData(String datastore) throws Exception{
		System.out.println("Geonames API find "+this.getData().size()+" results!");
		for(int i =0;i<this.getData().size();i++){
			String input = String.valueOf(this.getData().get(i).getGeonameId());
			String geo_name = this.getData().get(i).getName();
			if(geo_name==null){
				geo_name=this.getData().get(i).getCountryName();
			}
			String get = "http://api.geonames.org/getJSON?geonameId="+input+"&username=leipang";
			String jsonname = input+".json";
			String content = constructContentData(get);
			this.returnresult.add(constructReturnData(content));
			constructS3Object(content,jsonname,datastore);
			addtoIndex(geo_name,input,"index.json",datastore);
		}
	}
	private JSONObject constructReturnData(String content){
		JSONObject json_result = new JSONObject();
		json_result = (JSONObject) JSONValue.parse(content);
		System.out.println(json_result.get("name").toString());
		return json_result;
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
		System.out.println("---data stored---");
	}
	private void addtoIndex(String itemname,String geoid,String indexname,String datastore) throws IOException{
		QunbS3Store mystore = new QunbS3Store(datastore);
		String index_content = new String(mystore.getService().getItem(indexname),"utf-8");
		JSONObject index = (JSONObject) JSONValue.parse(index_content);
		System.out.println(geoid+"---"+itemname);
		index.put(geoid, itemname.toLowerCase());
		String content = index.toJSONString();
		byte[] cont = content.getBytes();
		String filename = "index.json";
		mystore.getService().storeItem(filename, cont);
		System.out.println("---index modified---");
	}
	public List<Map<String,Object>> getReturnResult(){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		if(this.result!=null){
			for(int i = 0;i<this.result.size();i++){
				Map<String,Object> tmpmap = new HashMap<String,Object>();
				tmpmap.put("qunb:geoId", this.returnresult.get(i).get("geonameId"));
				tmpmap.put("qunb:geoName", this.returnresult.get(i).get("name"));
				tmpmap.put("qunb:geoLat", this.returnresult.get(i).get("lat"));
				tmpmap.put("qunb:geoLng", this.returnresult.get(i).get("lng"));
				tmpmap.put("qunb:geoType",this.returnresult.get(i).get("fclName"));
				tmpmap.put("qunb:geoAlterNames", this.returnresult.get(i).get("alternateNames"));
				tmpmap.put("qunb:population", this.returnresult.get(i).get("population"));
				tmpmap.put("qunb:fclcode",this.returnresult.get(i).get("fcl"));
				tmpmap.put("qunb:fcode",this.returnresult.get(i).get("fcode"));
				list.add(tmpmap);
			}
		}
		return list;
	}
	
	public void setFeatureClassName(Toponym topo){
		FeatureClass featureclass = FeatureClass.valueOf(topo.getFeatureClass().name());
		switch (featureclass){
		case A:
			topo.setFeatureClassName("country, state, region");
			break;
		case P:
			topo.setFeatureClassName("city, village");
			break;
		case H:
			topo.setFeatureClassName("stream, lake");
			break;
		case R:
			topo.setFeatureClassName("road, railroad");
			break;
		case S:
			topo.setFeatureClassName("spot, building, farm");
			break;
		case U:
			topo.setFeatureClassName("undersea");
			break;
		case T:
			topo.setFeatureClassName("mountain,hill,rock");
			break;
		case V:
			topo.setFeatureClassName("forest,heath");
			break;
		case L:
			topo.setFeatureClassName("parks,area");
			break;
		}
	}
	
	public List<GeoName> getResult(){
		return this.result;
	}
	public enum FeatureClass {

	    A,P,R,H,L,S,U,T,V
	}
}
