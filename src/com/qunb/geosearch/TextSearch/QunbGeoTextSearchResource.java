package com.qunb.geosearch.TextSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geonames.WebService;
import org.json.*;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.qunb.geosearch.geoObject.GeoNamesTextSearch;
import com.qunb.geosearch.geoObject.QunbTextSearch;

import javax.ws.rs.Path;


@Path("/geosearch/textsearch/search")
public class QunbGeoTextSearchResource extends ServerResource{
	@SuppressWarnings("static-access")
	@Get
	public JSONObject searchGeoID() throws Exception{

		WebService svc = new WebService();
		svc.setUserName("leipang");
		String qunbdatastore = "qunb.geo";
		String input = getQuery().getValues("q");
		String type = getQuery().getValues("type");
		String country = getQuery().getValues("country");
		String lang = getQuery().getValues("lang");
		String limit = getQuery().getValues("limit");
		List<JSONObject> result = new ArrayList<JSONObject>();
		result = getResultList(input,type,country,limit,lang,qunbdatastore);
		JSONObject jsonresult = getSearchResult(result);
		return jsonresult;

	}
	@SuppressWarnings("static-access")
	public List<JSONObject> getResultList(String input,String type,String country,String limit,String lang,String datastore) throws Exception{
		List<JSONObject> list = new ArrayList<JSONObject>();
		QunbTextSearch mysearch = new QunbTextSearch(input,type,country,lang,datastore);
		if(mysearch.getResult().isEmpty()){
			GeoNamesTextSearch mysearch2 = new GeoNamesTextSearch(input,type,country,lang);
			mysearch2.storeData("qunb.geo");
			List<Map<String, Object>> mylist_result = mysearch2.getReturnResult();
			List<Map<String, Object>> mylist_result_class = QunbTextSearch.classResult(mylist_result);
			if(limit!=null&&!limit.isEmpty()&&Integer.valueOf(limit)<=mylist_result_class.size()){
				for(int i = 0;i<Integer.valueOf(limit);i++){
					JSONObject tmp = new JSONObject();
					tmp.put("qunb:geoId",mylist_result_class.get(i).get("qunb:geoId"));
					tmp.put("qunb:geoName", mylist_result_class.get(i).get("qunb:geoName"));
					tmp.put("qunb:geoLat", mylist_result_class.get(i).get("qunb:geoLat"));
					tmp.put("qunb:geoLng", mylist_result_class.get(i).get("qunb:geoLng"));
					tmp.put("qunb:fcode", mylist_result_class.get(i).get("qunb:fcode"));
					//tmp.put("qunb:geoType", mylist_result_class.get(i).get("qunb:geoType"));
					//tmp.put("qunb:geoAlterNames", mylist_result_class.get(i).get("qunb:geoAlterNames"));
					list.add(i,tmp);
				}
			}
			else if(limit!=null&&!limit.isEmpty()&&Integer.valueOf(limit)>mylist_result_class.size()){
				JSONObject tmp = new JSONObject();
				tmp.put("error 401","Limit exceeds the number of results found."+mylist_result_class.size()+" results found!");
				list.add(tmp);
			}
			else{
				for(int i = 0;i<mylist_result_class.size();i++){
					JSONObject tmp = new JSONObject();
					tmp.put("qunb:geoId", mylist_result_class.get(i).get("qunb:geoId"));
					tmp.put("qunb:geoName",mylist_result_class.get(i).get("qunb:geoName"));
					tmp.put("qunb:geoLat",mylist_result_class.get(i).get("qunb:geoLat"));
					tmp.put("qunb:geoLng", mylist_result_class.get(i).get("qunb:geoLng"));
					tmp.put("qunb:fcode", mylist_result_class.get(i).get("qunb:fcode"));
					//tmp.put("qunb:geoType",mylist_result_class.get(i).get("qunb:geoType"));
					//tmp.put("qunb:geoAlterNames", mylist_result_class.get(i).get("qunb:geoAlterNames"));
					list.add(i,tmp);
				}
			}
		}
		else{
			List<Map<String, Object>> mylist_result = mysearch.getResult();
			List<Map<String, Object>> mylist_result_class = QunbTextSearch.classResult(mylist_result);
			if(limit!=null&&!limit.isEmpty()&&Integer.valueOf(limit)<=mylist_result_class.size()){
				for(int i = 0;i<Integer.valueOf(limit);i++){
					JSONObject tmp = new JSONObject();
					tmp.put("qunb:geoId", mylist_result_class.get(i).get("qunb:geoId"));
					tmp.put("qunb:geoName", mylist_result_class.get(i).get("qunb:geoName"));
					tmp.put("qunb:geoLat",mylist_result_class.get(i).get("qunb:geoLat"));
					tmp.put("qunb:geoLng", mylist_result_class.get(i).get("qunb:geoLng"));
					tmp.put("qunb:fcode", mylist_result_class.get(i).get("qunb:fcode"));
					//tmp.put("qunb:geoType",mylist_result_class.get(i).get("qunb:geoType"));
					//tmp.put("qunb:geoAlterNames", mylist_result_class.get(i).get("qunb:geoAlterNames"));
					list.add(i,tmp);
				}
			}
			else if(limit!=null&&!limit.isEmpty()&&Integer.valueOf(limit)>mylist_result_class.size()){
				JSONObject tmp = new JSONObject();
				tmp.put("error 401","Limit exceeds the number of results found."+mylist_result_class.size()+" results found!");
				list.add(tmp);
			}
			else{
				for(int i = 0;i<mysearch.getResult().size();i++){
					JSONObject tmp = new JSONObject();
					tmp.put("qunb:geoId", mylist_result_class.get(i).get("qunb:geoId"));
					tmp.put("qunb:geoName", mylist_result_class.get(i).get("qunb:geoName"));
					tmp.put("qunb:geoLat", mylist_result_class.get(i).get("qunb:geoLat"));
					tmp.put("qunb:geoLng",mylist_result_class.get(i).get("qunb:geoLng"));
					//tmp.put("qunb:geoType", mylist_result_class.get(i).get("qunb:geoType"));
					tmp.put("qunb:fcode", mylist_result_class.get(i).get("qunb:fcode"));
					//tmp.put("qunb:geoAlterNames", mylist_result_class.get(i).get("qunb:geoAlterNames"));
					list.add(i,tmp);
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


