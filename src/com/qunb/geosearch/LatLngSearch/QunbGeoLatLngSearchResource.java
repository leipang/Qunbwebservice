package com.qunb.geosearch.LatLngSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;

import org.geonames.WebService;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import com.qunb.geosearch.geoObject.GeoName;
import com.qunb.geosearch.geoObject.GeoNamesLatLngSearch;
import com.qunb.geosearch.geoObject.QunbLatLogSearch;

@Path("/geosearch/latlngsearch/search")
public class QunbGeoLatLngSearchResource extends ServerResource {
	@Get
	public JSONObject searchGeoID() throws Exception{
		WebService svc = new WebService();
		svc.setUserName("leipang");
		String qunbdatastore = "qunb.geo";
		double lat = Double.parseDouble(getQuery().getValues("lat"));
		double lng = Double.parseDouble(getQuery().getValues("lng"));
		String level = getQuery().getValues("level");
		String limit = getQuery().getValues("limit");
		List<JSONObject> result = new ArrayList<JSONObject>();
		result = getResultList(lat,lng,level,limit,qunbdatastore);
		JSONObject jsonresult = getSearchResult(result);
		return jsonresult;
	}
	@SuppressWarnings("null")
	public List<JSONObject> getResultList(double lat,double lng,String level,String limit,String datastore) throws Exception{
		List<JSONObject> list = new ArrayList<JSONObject>();
		QunbLatLogSearch mysearch = new QunbLatLogSearch(lat,lng,datastore);
		if(mysearch.getResult().isEmpty()){
			GeoNamesLatLngSearch mysearch2 = new GeoNamesLatLngSearch(lat,lng,level);
			mysearch2.storeData("qunb.geo");
			GeoName geo_search = mysearch2.getResult();
			if(geo_search!=null){
				JSONObject tmp = new JSONObject();
				tmp.put("qunb:geoId", geo_search.getGeonameId());
				tmp.put("qunb:geoName",geo_search.getToponymName());
				tmp.put("qunb:geoLat",geo_search.getLat());
				tmp.put("qunb:geoLng",geo_search.getLng());
				tmp.put("qunb:geoFcode",geo_search.getFcode());
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
			if(limit!=null&&!limit.isEmpty()&&Integer.valueOf(limit)<=qunb_search_class.size()){
				for(int i = 0;i<Integer.valueOf(limit);i++){
					JSONObject tmp = new JSONObject();
					tmp.put("qunb:geoId", qunb_search_class.get(i).get("qunb:geoId"));
					tmp.put("qunb:geoName", qunb_search_class.get(i).get("qunb:geoName"));
					tmp.put("qunb:geoLat", qunb_search_class.get(i).get("qunb:geoLat"));
					tmp.put("qunb:geoLng", qunb_search_class.get(i).get("qunb:geoLng"));
					tmp.put("qunb:geoDistance", qunb_search_class.get(i).get("qunb:geoDistance"));
					//tmp.put("qunb:geoType", qunb_search_class.get(i).get("qunb:geoType"));
					//tmp.put("qunb:geoAlterNames", qunb_search_class.getResult().get(i).get("qunb:geoAlterNames"));
					list.add(tmp);
				}
			}
			else if(limit!=null&&!limit.isEmpty()&&Integer.valueOf(limit)>qunb_search_class.size()){
				JSONObject tmp = new JSONObject();
				tmp.put("error 401","Limit exceeds the number of results found."+qunb_search_class.size()+" results found!");
				list.add(tmp);
			}
			else{
				for(int i = 0;i<mysearch.getResult().size();i++){
					JSONObject tmp = new JSONObject();
					tmp.put("qunb:geoId", qunb_search_class.get(i).get("qunb:geoId"));
					tmp.put("qunb:geoName", qunb_search_class.get(i).get("qunb:geoName"));
					tmp.put("qunb:geoLat", qunb_search_class.get(i).get("qunb:geoLat"));
					tmp.put("qunb:geoLng", qunb_search_class.get(i).get("qunb:geoLng"));
					tmp.put("qunb:geoDistance", qunb_search_class.get(i).get("qunb:geoDistance"));
					//tmp.put("qunb:geoType", qunb_search_class.get(i).get("qunb:geoType"));
					//tmp.put("qunb:geoAlterNames",qunb_search_class.get(i).get("qunb:geoAlterNames"));
					list.add(tmp);
				}
			}
		}
		return list;
	}
	@SuppressWarnings("unchecked")
	public JSONObject getSearchResult(List<JSONObject> mylist){
		System.out.println(mylist.size());
		JSONObject output = new JSONObject();
		for(int i = 0;i<mylist.size();i++){
			 if(i==0){
				   try {
					   output.put("result", mylist.get(i));
				   } catch (JSONException e) {
					   e.printStackTrace();
				   }
			   }
			   else{
				   try {
					   output.accumulate("result", mylist.get(i));
				   } catch (JSONException e) {
					   e.printStackTrace();
				   }
			   }
  
		}
		return output;
	}
	

}
