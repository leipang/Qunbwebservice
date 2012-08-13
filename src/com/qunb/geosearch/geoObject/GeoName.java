package com.qunb.geosearch.geoObject;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.json.JSONException;
import org.json.simple.JSONObject;



import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class GeoName {
	private String name;
	private double lat;
	private double lng;
	private String geonameId;
	private String countryCode;
	private String countryName;
	private String fcl;
	private String fcode;
	private String alternateNames;
	private String population;
	private String fclName;
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setLat(String lat) {
		this.lat = Double.valueOf(lat);
	}
	public double getLat() {
		return this.lat;
	}
	public void setLng(String lng) {
		this.lng = Double.valueOf(lng);
	}
	public double getLng() {
		return this.lng;
	}
	public void setGeonameId(String geonameId) {
		this.geonameId = geonameId;
	}
	public String getGeonameId() {
		return this.geonameId;
	}
	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}
	public String getCountryName() {
		return countryName;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	public String getCountryCode() {
		return countryCode;
	}
	public void setFcode(String fcode) {
		this.fcode = fcode;
	}
	public String getFcode() {
		return fcode;
	}
	public void setFcl(String fcl) {
		this.fcl = fcl;
	}
	public String getFcl() {
		return fcl;
	}
	public void setPopulation(String ppl) {
		this.population = ppl;
	}
	public String getPopulation() {
		return this.population;
	}
	public String getFclName(){
		return this.fclName;
	}
	public void setFclName(String fclname){
		this.fclName = fclname;
	}
	
	public Entity toEntity(){
		Key geo_key = KeyFactory.createKey("GeonameId", this.getGeonameId().toString());
		Entity geo = new Entity("GeoName",geo_key);
		geo.setProperty("geonameId", this.getGeonameId());
		geo.setProperty("name", this.getName());
		geo.setProperty("countryName", this.getCountryName());
		geo.setProperty("countryCode", this.getCountryCode());
		geo.setProperty("Lat", this.getLat());
		geo.setProperty("Lng", this.getLng());
		geo.setProperty("Fcl", this.getFcl());
		geo.setProperty("Fcode", this.getFcode());
		geo.setProperty("Population", this.getPopulation());
		return geo;
	}
	
	public  org.json.JSONObject toJson() throws JSONException{
		org.json.JSONObject tmp = new org.json.JSONObject();
		tmp.put("qunb:geoId", this.getGeonameId());
		tmp.put("qunb:geoName",this.getName());
		tmp.put("qunb:geoLat",this.getLat());
		tmp.put("qunb:geoLng",this.getLng());
		tmp.put("qunb:geoFcode",this.getFcode());
		return tmp;
	}
	public String getAlternateNames() {
		return alternateNames;
	}
	public void setAlternateNames(String alternateNames) {
		this.alternateNames = alternateNames;
	}
}
