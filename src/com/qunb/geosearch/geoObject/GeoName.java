package com.qunb.geosearch.geoObject;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class GeoName {
	@Persistent
	private String toponymName;
	
	@Persistent
	private String name;
	
	@Persistent
	private double lat;
	
	@Persistent
	private double lng;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private int geonameId;
	
	@Persistent
	private String countryCode;
	
	@Persistent
	private String countryName;
	
	@Persistent
	private String fcl;
	
	@Persistent
	private String fcode;
	
	@Persistent
	private String alternateNames;
	
	public void setToponymName(String toponymName) {
		this.toponymName = toponymName;
	}
	public String getToponymName() {
		return toponymName;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLat() {
		return lat;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public double getLng() {
		return lng;
	}
	public void setGeonameId(int geonameId) {
		this.geonameId = geonameId;
	}
	public int getGeonameId() {
		return geonameId;
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
}
