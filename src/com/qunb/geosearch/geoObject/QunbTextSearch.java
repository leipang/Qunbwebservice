package com.qunb.geosearch.geoObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jclouds.aws.s3.blobstore.*;
import org.jclouds.blobstore.BlobMap;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.options.ListContainerOptions;
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

public class QunbTextSearch {
	private volatile DatastoreService datastore;
	private volatile String input;
	private volatile String type;
	private volatile String country;
	private volatile String lang;
	private volatile List<JSONObject> result;
	
	public QunbTextSearch(String input,String type,String country,String lang) throws Exception{
		if(input.contains(",")){
			
			this.input =input.substring(0, input.indexOf(","));
		}
		else{
			this.input=input;
		}
		if(lang!=null&&!lang.isEmpty()){
			this.lang=lang;
		}
		else{
			this.lang="en";
		}
		this.type = type;
		this.country = country;
		this.datastore=  DatastoreServiceFactory.getDatastoreService();
		this.result = this.constructResult();
	}

	
	public List<JSONObject> constructResult() throws Exception{
		List<JSONObject> mydata = new ArrayList<JSONObject>();
		mydata = this.getData();
		if(mydata.isEmpty()){
			return null;
		}
		return mydata;
	}

	@SuppressWarnings("deprecation")
	public  synchronized List<JSONObject> getData() throws IOException{
		List<JSONObject> output = new ArrayList<JSONObject>();
		List<Entity> entities = new ArrayList<Entity>();
		if (this.input.length() == 2) {
			entities = GeoDataStore.searchCountryCode(this.input);
		}
		if (entities.size() == 0) {
			entities = GeoDataStore.searchName(this.input, this.country, this.type);
		}
		for(Entity geo:entities){
			JSONObject mygeo = new JSONObject();
			mygeo = GeoNameOperation.EntitytoJson(geo);
			output.add(mygeo);
		}
		if(output.size()==0){
			System.out.println("---Result Not Found at Qunb---");
		}
		return output;
	}
	public List<Map<String,Object>> getResult(){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		if(this.result!=null){
			for(int i = 0;i<this.result.size();i++){
				Map<String,Object> tmpmap = new HashMap<String,Object>();
				tmpmap = GeoNameOperation.JsonToMap(this.result.get(i));
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
				if(Integer.valueOf(mylist.get(j).get("qunb:population").toString())!=0 &&Integer.valueOf(mylist.get(j-1).get("qunb:population").toString())!=0){
					if(Integer.valueOf(mylist.get(j).get("qunb:population").toString())>Integer.valueOf(mylist.get(j-1).get("qunb:population").toString())){
						tmpmap_j=mylist.get(j);
						tmpmap_j_1=mylist.get(j-1);
						mylist.set(j, tmpmap_j_1);
						mylist.set(j-1,tmpmap_j);
					}
					else if(Integer.valueOf(mylist.get(j).get("qunb:population").toString())==Integer.valueOf(mylist.get(j-1).get("qunb:population").toString())){
						if(getfclordre(mylist.get(j))>getfclordre(mylist.get(j-1))){
							tmpmap_j=mylist.get(j);
							tmpmap_j_1=mylist.get(j-1);
							mylist.set(j, tmpmap_j_1);
							mylist.set(j-1,tmpmap_j);
						}
						else{
							if(getfclordre(mylist.get(j))==0&&getfclordre(mylist.get(j-1))==0){
								if(getfcodeorder_A(mylist.get(j))>getfcodeorder_A(mylist.get(j-1))){
									tmpmap_j=mylist.get(j);
									tmpmap_j_1=mylist.get(j-1);
									mylist.set(j, tmpmap_j_1);
									mylist.set(j-1,tmpmap_j);
								}
							}
							else if(getfclordre(mylist.get(j))==1&&getfclordre(mylist.get(j-1))==1){
								if(getfcodeorder_P(mylist.get(j))>getfcodeorder_P(mylist.get(j-1))){
									tmpmap_j=mylist.get(j);
									tmpmap_j_1=mylist.get(j-1);
									mylist.set(j, tmpmap_j_1);
									mylist.set(j-1,tmpmap_j);
								}
							}
						}
					}
				}
				else{ 
						if(getfclordre(mylist.get(j))>getfclordre(mylist.get(j-1))){
							tmpmap_j=mylist.get(j);
							tmpmap_j_1=mylist.get(j-1);
							mylist.set(j, tmpmap_j_1);
							mylist.set(j-1,tmpmap_j);
						}
						else{
							if(getfclordre(mylist.get(j))==0&&getfclordre(mylist.get(j-1))==0){
								if(getfcodeorder_A(mylist.get(j))>getfcodeorder_A(mylist.get(j-1))){
									tmpmap_j=mylist.get(j);
									tmpmap_j_1=mylist.get(j-1);
									mylist.set(j, tmpmap_j_1);
									mylist.set(j-1,tmpmap_j);
								}
							}
							else if(getfclordre(mylist.get(j))==1&&getfclordre(mylist.get(j-1))==1){
								if(getfcodeorder_P(mylist.get(j))>getfcodeorder_P(mylist.get(j-1))){
									tmpmap_j=mylist.get(j);
									tmpmap_j_1=mylist.get(j-1);
									mylist.set(j, tmpmap_j_1);
									mylist.set(j-1,tmpmap_j);
								}
							}
						}
				}
			}
		}
		return mylist;
	}
	public static int getfclordre(Map<String,Object> map){
		if(map.get("qunb:fclcode").equals("A")){
			return 2;
		}
		else if(map.get("qunb:fclcode").equals("P")){
			return 1;
		}
		else {
			return 0;
		}
	}
	public static int getfcodeorder_P(Map<String,Object> map){
		if(map.get("qunb:fcode").equals("PPLC")){
			return 5;
		}
		else if(map.get("qunb:fcode").equals("PPLA")){
			return 4;
		}
		else if(map.get("qunb:fcode").equals("PPLA2")){
			return 3;
		}
		else if(map.get("qunb:fcode").equals("PPLA3")){
			return 2;
		}
		else if(map.get("qunb:fcode").equals("PPLA4")){
			return 1;
		}
		else {
			return 0;
		}
	}
	public static int getfcodeorder_A(Map<String,Object> map){
		if(map.get("qunb:fcode").equals("PCL")){
			return 8;
		}
		else if(map.get("qunb:fcode").equals("PCLD")){
			return 7;
		}
		else if(map.get("qunb:fcode").equals("PCLF")){
			return 6;
		}
		else if(map.get("qunb:fcode").equals("ADMD")){
			return 5;
		}
		else if(map.get("qunb:fcode").equals("ADM1")){
			return 4;
		}
		else if(map.get("qunb:fcode").equals("ADM2")){
			return 3;
		}
		else if(map.get("qunb:fcode").equals("ADM3")){
			return 2;
		}
		else if(map.get("qunb:fcode").equals("ADM4")){
			return 1;
		}
		else {
			return 0;
		}
	}
	

}
