package com.qunb.geosearch.LatLngSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;

import org.geonames.WebService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;


import com.qunb.geosearch.TextSearch.QunbGeoTextSearchResource;
import com.qunb.geosearch.geoObject.GeoName;
import com.qunb.geosearch.geoObject.GeoNameOperation;
import com.qunb.geosearch.geoObject.GeoNamesLatLngSearch;
import com.qunb.geosearch.geoObject.QunbLatLogSearch;

@Path("/geosearch/latlngsearch/search")
public class QunbGeoLatLngSearchResource extends ServerResource {
	@Get
	public JSONArray searchGeoID() throws Exception{
		WebService svc = new WebService();
		svc.setUserName("leipang");
		double lat = Double.parseDouble(getQuery().getValues("lat"));
		double lng = Double.parseDouble(getQuery().getValues("lng"));
		String level = getQuery().getValues("level");
		String limit = getQuery().getValues("limit");
		List<JSONObject> result = new ArrayList<JSONObject>();
		result = getResultList(lat,lng,level,limit);
		JSONArray jsonresult = getSearchResult(result);
		return jsonresult;
	}
	@SuppressWarnings("null")
	public List<JSONObject> getResultList(double lat,double lng,String level,String limit) throws Exception{
		List<JSONObject> list = new ArrayList<JSONObject>();
		QunbLatLogSearch mysearch = new QunbLatLogSearch(lat,lng);
		if(mysearch.getResult().isEmpty()){
			GeoNamesLatLngSearch mysearch2 = new GeoNamesLatLngSearch(lat,lng,level);
			mysearch2.storeData();
			GeoName geo_search = mysearch2.getResult();
			if(geo_search!=null){
				JSONObject tmp = new JSONObject();
				tmp = geo_search.toJson();
				list.add(tmp);
			}
			else{
				JSONObject tmp = new JSONObject();
				tmp.put("error 400","The level is not a fcode!");
				list.add(tmp);
			}
			
		}
		else{
			List<Map<String,Object>> qunb_search=mysearch.getResult();
			List<Map<String,Object>> qunb_search_class=QunbLatLogSearch.classResult(qunb_search);
			GeoNameOperation.toListJson(qunb_search_class, limit);
		}
		return list;
	}
	@SuppressWarnings("unchecked")
	public JSONArray getSearchResult(List<JSONObject> mylist){
		System.out.println(mylist.size());
		JSONArray output = new JSONArray();
		for(int i = 0;i<mylist.size();i++){
			output.put(mylist.get(i));
		}
		return output;
	}
	

}
