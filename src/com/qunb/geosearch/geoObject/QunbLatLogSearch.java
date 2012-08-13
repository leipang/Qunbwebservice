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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.qunb.fuzzymatch.LetterSimilarity;
import com.qunb.fuzzymatch.LetterSimilarity.CouplingLevel;

public class QunbLatLogSearch {
	private double lat;
	private double lng;
	private double dis;
	private DatastoreService datastore;
	private List<Double> zone;
	private List<JSONObject> result;

	public QunbLatLogSearch(double lat, double lng)
			throws Exception {
		this.lat = lat;
		this.lng = lng;
		this.dis = 0.5;
		this.datastore = DatastoreServiceFactory.getDatastoreService();
		this.zone = this.constructZone();
		this.result = this.getData_latlng();
	}

	@SuppressWarnings("unchecked")
	public List<JSONObject> getData_latlng() throws IOException {
		List<JSONObject> output = new ArrayList<JSONObject>();
		JSONObject result = new JSONObject();
		List<Entity> entities = new ArrayList<Entity>();
		Query q_lat = new Query("GeoName");
		q_lat.addFilter("Lat", FilterOperator.GREATER_THAN_OR_EQUAL,
				this.zone.get(1));
		q_lat.addFilter("Lat", FilterOperator.LESS_THAN, this.zone.get(0));
		
		Query q_lng = new Query("GeoName");
		q_lng.addFilter("Lng", FilterOperator.GREATER_THAN_OR_EQUAL,
				this.zone.get(3));
		q_lng.addFilter("Lng", FilterOperator.LESS_THAN, this.zone.get(2));
		PreparedQuery pq_lat = datastore.prepare(q_lat);
		PreparedQuery pq_lng = datastore.prepare(q_lng);
		List<String> lats = new ArrayList<String>();
		List<String> lngs = new ArrayList<String>();
		for (Entity entity : pq_lat.asIterable()) {
			String name = (String) entity.getProperty("name");
			lats.add(name);
		}
		for (Entity entity : pq_lng.asIterable()) {
			String name = (String) entity.getProperty("name");
			lngs.add(name);
		}
		for (Entity entity : pq_lat.asIterable()) {
			String name = (String) entity.getProperty("name");
			if(lngs.contains(name)){
				double dis = calculDis(Double.valueOf(entity.getProperty("Lat").toString()),Double.valueOf(entity.getProperty("Lng").toString()));
				if(dis<=this.dis){
					entities.add(entity);
				}
			}
		}

		for (Entity geo : entities) {
			JSONObject mygeo = new JSONObject();
			mygeo = GeoNameOperation.EntitytoJson(geo);
			output.add(mygeo);
		}
		if (output.size() == 0) {
			System.out.println("---Result Not Found at Qunb---");
		}
		return output;
	}

	public List<Double> constructZone() {
		List<Double> zone = new ArrayList<Double>();
		double convertissor = 3.141592653589793 / 180;
		double radius = 6371;
		double lat = this.lat * convertissor;
		double lng = this.lng * convertissor;
		double lat_max = Math.asin(Math.sin(lat) * Math.cos(1 / radius)
				+ Math.cos(lat) * Math.sin(1 / radius) * Math.cos(0));
		double lat_min = Math.asin(Math.sin(lat) * Math.cos(1 / radius)
				+ Math.cos(lat) * Math.sin(1 / radius) * Math.cos(180));
		double lat_90 = Math.asin(Math.sin(lat) * Math.cos(1 / radius)
				+ Math.cos(lat) * Math.sin(1 / radius) * Math.cos(90));
		double lat_270 = Math.asin(Math.sin(lat) * Math.cos(1 / radius)
				+ Math.cos(lat) * Math.sin(1 / radius) * Math.cos(270));
		double lng_max = lng
				+ Math.atan2(
						Math.sin(90) * Math.sin(1 / radius) * Math.cos(lat),
						Math.cos(1 / radius) - Math.sin(lat) * Math.sin(lat_90));
		double lng_min = lng
				+ Math.atan2(
						Math.sin(270) * Math.sin(1 / radius) * Math.cos(lat),
						Math.cos(1 / radius) - Math.sin(lat)
								* Math.sin(lat_270));
		zone.add(0, lat_max / convertissor);
		zone.add(1, lat_min / convertissor);
		zone.add(2, lng_max / convertissor);
		zone.add(3, lng_min / convertissor);
		System.out.println("lat_max: " + zone.get(0));
		System.out.println("lat_min: " + zone.get(1));
		System.out.println("lng_max: " + zone.get(2));
		System.out.println("lng_min: " + zone.get(3));
		return zone;
	}

	public double calculDis(double lat, double lng) {
		double PI = 3.141592653589793;
		double dis_lat = (lat - this.lat) * PI / 180;
		double dis_lng = (lng - this.lng) * PI / 180;
		;
		double a = Math.sin(dis_lat / 2) * Math.sin(dis_lat / 2)
				+ Math.cos(lat * PI / 180) * Math.cos(this.lat * PI / 180)
				* Math.sin(dis_lng / 2) * Math.sin(dis_lng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return c * 6371;
	}

	public List<Map<String, Object>> getResult() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if (this.result != null) {
			for (int i = 0; i < this.result.size(); i++) {
				Map<String, Object> tmpmap = new HashMap<String, Object>();
				tmpmap = GeoNameOperation.JsonToMap(this.result.get(i));
				tmpmap.put("qunb:geoDistance",
						this.result.get(i).get("geoDistance"));
				list.add(tmpmap);
			}
		}
		return list;
	}

	public static List<Map<String, Object>> classResult(
			List<Map<String, Object>> mylist) {
		for (int i = 0; i < mylist.size() - 1; i++) {
			Map<String, Object> tmpmap_j = null;
			Map<String, Object> tmpmap_j_1 = null;
			for (int j = mylist.size() - 1; j > i; j--) {
				if (Double.parseDouble(mylist.get(j).get("qunb:geoDistance")
						.toString()) < Double.parseDouble(mylist.get(j - 1)
						.get("qunb:geoDistance").toString())) {
					tmpmap_j = mylist.get(j);
					tmpmap_j_1 = mylist.get(j - 1);
					mylist.set(j, tmpmap_j_1);
					mylist.set(j - 1, tmpmap_j);
				}
			}
		}
		return mylist;
	}
}