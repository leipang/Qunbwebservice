package com.qunb.geosearch.TextSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geonames.WebService;
import org.json.*;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.qunb.geosearch.geoObject.GeoNameOperation;
import com.qunb.geosearch.geoObject.GeoNamesTextSearch;
import com.qunb.geosearch.geoObject.QunbTextSearch;

import javax.ws.rs.Path;


@Path("/geosearch/textsearch/search")
public class QunbGeoTextSearchResource extends ServerResource{
	@SuppressWarnings("static-access")
	@Get
	public JSONArray searchGeoID() throws Exception{

		WebService svc = new WebService();
		svc.setUserName("leipang");
		String input = getQuery().getValues("q");
		String type = getQuery().getValues("type");
		String country = getQuery().getValues("country");
		String lang = getQuery().getValues("lang");
		String limit = getQuery().getValues("limit");
		List<JSONObject> result = new ArrayList<JSONObject>();
		result = getResultList(input,type,country,limit,lang);
		JSONArray jsonresult = getSearchResult(result);
		return jsonresult;

	}
	@SuppressWarnings("static-access")
	public List<JSONObject> getResultList(String input,String type,String country,String limit,String lang) throws Exception{
		List<JSONObject> list = new ArrayList<JSONObject>();
		QunbTextSearch mysearch = new QunbTextSearch(input,type,country,lang);
		if(mysearch.getResult().isEmpty()){
			GeoNamesTextSearch mysearch2 = new GeoNamesTextSearch(input,limit,type,country,lang);
			mysearch2.storeData();
			List<Map<String, Object>> mylist_result = mysearch2.getReturnResult();
			List<Map<String, Object>> mylist_result_class = QunbTextSearch.classResult(mylist_result);
			list = GeoNameOperation.toListJson(mylist_result_class,limit);
		}
		else{
			List<Map<String, Object>> mylist_result = mysearch.getResult();
			List<Map<String, Object>> mylist_result_class = QunbTextSearch.classResult(mylist_result);
			list = GeoNameOperation.toListJson(mylist_result_class,limit);
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



