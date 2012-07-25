package com.qunb.geosearch.geoObject;

import java.util.ArrayList;
import java.util.List;
import org.geonames.Toponym;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;



public class GeoObject {
	private static int geo_id;
	private static String geo_name;
	private static String type;
	private static List<JSONObject> alternames; //try to construct List<String>
	private static double lat;
	private static double lng;
	private static Toponym parent;
	private static List<Toponym> children;
	private static List<Toponym> neighbours;

	
	public GeoObject(JSONObject myobj) throws Exception{
		this.geo_id=((Long) myobj.get("geonameId")).intValue();
		this.geo_name=(String) myobj.get("name");
		this.type = (String) myobj.get("fclName");
		this.lat=(Double) myobj.get("lat");
		this.lng = (Double) myobj.get("lng");
		this.alternames=this.constructAlternames((JSONArray) myobj.get("alternateNames"));
		this.parent=this.constructParent();
		this.children = this.constructChildren();
		this.neighbours = this.constructNeighbours();
	}
	
	
	// TODO stoker name+lang ou name tout simple
	public List<JSONObject> constructAlternames(JSONArray allnames){
		List<JSONObject> alter = new ArrayList<JSONObject>();
		for(int i = 0; i<allnames.size();i++){
			alter.add((JSONObject)allnames.get(i));
			//JSONObject n = (JSONObject) allnames.get(i);
			// alter.add(n.get("name"));
		}
		return alter;
	}
	@SuppressWarnings("static-access")
	public Toponym constructParent() throws Exception{
		WebService findparent = new WebService();
		findparent.setUserName("leipang");
		List<Toponym> parents = findparent.hierarchy(this.geo_id, "en", null);
		int num_parents = parents.size();
		for(int i = num_parents-1;i>=0;i--){
			if(!parents.get(i).getName().equals(this.geo_name)){
				return parents.get(i);
			}
		}
		return null;
	}
	
	@SuppressWarnings("static-access")
	public List<Toponym> constructChildren() throws Exception{
		List<Toponym> children = new ArrayList<Toponym>();
		WebService findchildren = new WebService();
		//TODO multi-users
		findchildren.setUserName("leipang");
		List<Toponym> topo = findchildren.children(this.geo_id, "en", null).getToponyms();
		for(Toponym child : topo){
			children.add(child);
		}
		return children;
	}
	
	@SuppressWarnings("static-access")
	public List<Toponym> constructNeighbours() throws Exception{
		List<Toponym> neighbours = new ArrayList<Toponym>();
		WebService findneighbours = new WebService();
		//TODO multi-users
		findneighbours.setUserName("leipang");
		try {
			List<Toponym> topo = findneighbours.neighbours(this.geo_id, "en", null).getToponyms();
			for(Toponym neighbour : topo){
				neighbours.add(neighbour);
			}
			return neighbours;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int getGeoId(){
		return this.geo_id;
	}
	public String getGeoName(){
		return this.geo_name;
	}
	public String getType(){
		return this.type;
	}
	public List<JSONObject> getAlternateNames(){
		return this.alternames;
	}
	public double getLat(){
		return this.lat;
	}
	public double getLng(){
		return this.lng;
	}
	public Toponym getParent(){
		return this.parent;
	}
	public List<Toponym> getChildrent(){
		return this.children;
	}
	public List<Toponym> getNeighbours(){
		return this.neighbours;
	}
}