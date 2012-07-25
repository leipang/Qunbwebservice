package com.qunb.geosearch.geoObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.qunb.fuzzymatch.LetterSimilarity;
import com.qunb.fuzzymatch.LetterSimilarity.CouplingLevel;

public class QunbLatLogSearch {
	private double lat;
	private double lng;
	private double dis;
	private String datastore;
	private List<Double> zone;
	private List<JSONObject> result;
	public QunbLatLogSearch(double lat,double lng,String datastore) throws Exception{
		this.lat = lat;
		this.lng = lng;
		this.dis = 0.5;
		this.datastore = datastore;//TODO change to a URL
		this.zone = this.constructZone();
		this.result = this.getData_latlng();
	}
	
	@SuppressWarnings("unchecked")
	public  List<JSONObject> getData_latlng() throws IOException{
		List<JSONObject> output = new ArrayList<JSONObject>();
		JSONObject result = new JSONObject();
		QunbS3Store mystore = new QunbS3Store(this.datastore);
		System.out.println("There are "+mystore.getService().listItems().size()+" data in qunbstore");
		List<String> list_item = mystore.getService().listItems();
		for (String item:list_item) {
			if(item.endsWith(".json")){
				String name = item;
				String content = new String(mystore.getService().getItem(name),"utf-8");
				result = (JSONObject) JSONValue.parse(content);
				double result_lat = Double.parseDouble(result.get("lat").toString());
				double result_lng = Double.parseDouble(result.get("lng").toString());
				
				if(this.zone.get(1)<=result_lat&&result_lat<=this.zone.get(0)&&this.zone.get(3)<=result_lng&&result_lng<=this.zone.get(2)){
					double dis = calculDis(result_lat,result_lng);
					if(dis<=this.dis){
						result.put("qunb:geoDistance", dis);
						output.add(result);
					}
				}
			}
		}
		if(output.size()==0){
			System.out.println("---Result Not Found at Qunb---");
		}
		return output;
	}
	public List<Double> constructZone(){
		List<Double> zone = new ArrayList<Double>();
		double convertissor = 3.141592653589793/180;
		double radius = 6371;
		double lat = this.lat*convertissor;
		double lng = this.lng*convertissor;
		double lat_max = Math.asin(Math.sin(lat)*Math.cos(1/radius) + Math.cos(lat)*Math.sin(1/radius)*Math.cos(0));
		double lat_min = Math.asin(Math.sin(lat)*Math.cos(1/radius) + Math.cos(lat)*Math.sin(1/radius)*Math.cos(180));
		double lat_90 = Math.asin(Math.sin(lat)*Math.cos(1/radius) + Math.cos(lat)*Math.sin(1/radius)*Math.cos(90));
		double lat_270 = Math.asin(Math.sin(lat)*Math.cos(1/radius) + Math.cos(lat)*Math.sin(1/radius)*Math.cos(270));
		double lng_max =lng + Math.atan2(Math.sin(90)*Math.sin(1/radius)*Math.cos(lat), Math.cos(1/radius)-Math.sin(lat)*Math.sin(lat_90));
		double lng_min =lng + Math.atan2(Math.sin(270)*Math.sin(1/radius)*Math.cos(lat), Math.cos(1/radius)-Math.sin(lat)*Math.sin(lat_270));
		zone.add(0, lat_max/convertissor);
		zone.add(1, lat_min/convertissor);
		zone.add(2, lng_max/convertissor);
		zone.add(3, lng_min/convertissor);
		System.out.println("lat_max: "+zone.get(0));
		System.out.println("lat_min: "+zone.get(1));
		System.out.println("lng_max: "+zone.get(2));
		System.out.println("lng_min: "+zone.get(3));
		return zone;
	}
	public double calculDis(double lat,double lng){
		double PI = 3.141592653589793;
		double dis_lat = (lat-this.lat)*PI/180;
		double dis_lng = (lng-this.lng)*PI/180;;
		double a = Math.sin(dis_lat/2)*Math.sin(dis_lat/2) + Math.cos(lat*PI/180)*Math.cos(this.lat*PI/180)*Math.sin(dis_lng/2)*Math.sin(dis_lng/2);
		double c = 2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
		return c*6371;
	}
	public List<Map<String,Object>> getResult(){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		if(this.result!=null){
			for(int i = 0;i<this.result.size();i++){
				Map<String,Object> tmpmap = new HashMap<String,Object>();
				tmpmap.put("qunb:geoId", this.result.get(i).get("geonameId"));
				tmpmap.put("qunb:geoName", this.result.get(i).get("toponymName"));
				tmpmap.put("qunb:geoLat", this.result.get(i).get("lat"));
				tmpmap.put("qunb:geoLng", this.result.get(i).get("lng"));
				tmpmap.put("qunb:geoType",this.result.get(i).get("fclName"));
				tmpmap.put("qunb:geoAlterNames", this.result.get(i).get("alternateNames"));
				tmpmap.put("qunb:geoFcode", this.result.get(i).get("fcode"));
				tmpmap.put("qunb:geoDistance",this.result.get(i).get("qunb:geoDistance"));
				list.add(tmpmap);
			}
		}
		return list;
	}
	public static List<Map<String,Object>> classResult(List<Map<String,Object>> mylist){
		for(int i =0;i<mylist.size()-1;i++){
			Map<String,Object> tmpmap_j = null;
			Map<String,Object> tmpmap_j_1 = null;
			for(int j=mylist.size()-1;j>i;j--){
					if(Double.parseDouble(mylist.get(j).get("qunb:geoDistance").toString())<Double.parseDouble(mylist.get(j-1).get("qunb:geoDistance").toString())){
						tmpmap_j=mylist.get(j);
						tmpmap_j_1=mylist.get(j-1);
						mylist.set(j, tmpmap_j_1);
						mylist.set(j-1,tmpmap_j);
					}
			}
		}
		return mylist;
	}
}
