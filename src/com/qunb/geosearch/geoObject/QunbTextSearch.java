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


import com.qunb.fuzzymatch.LetterSimilarity;
import com.qunb.fuzzymatch.LetterSimilarity.CouplingLevel;

public class QunbTextSearch {
	private String input;
	private String type;
	private String country;
	private String lang;
	private String datastore;
	private List<JSONObject> result;
	
	public QunbTextSearch(String input,String type,String country,String lang,String datastore) throws Exception{
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
		this.datastore = datastore;//TODO change to a URL
		this.result = this.constructResult();
	}
	
	//TODO if ever not found, do a fuzzy match
	@SuppressWarnings("deprecation")
	public  List<JSONObject> getData_3parameters() throws IOException{
		List<JSONObject> output = new ArrayList<JSONObject>();
		JSONObject result = new JSONObject();
		QunbS3Store mystore = new QunbS3Store(this.datastore);
		System.out.println("There are "+mystore.getService().listItems().size()+" data in qunbstore");
		List<String> list_item = mystore.getService().listItems();
		if(list_item.contains("index.json")){
			String index_content = new String(mystore.getService().getItem("index.json"),"utf-8");
			JSONObject index = (JSONObject) JSONValue.parse(index_content);
			if(!index.containsValue(this.input.toLowerCase())){
				return output;
			}
		}
		else{
			JSONObject index = new JSONObject();
			index.put("000000", "index");
			String content = index.toJSONString();
			byte[] cont = content.getBytes();
			String filename = "index.json";
			mystore.getService().storeItem(filename, cont);
			System.out.println("---index stored---");
		}
		for (String item:list_item) {
			if(item.endsWith(".json")&&!item.equals("index.json")){
				String name = item;
				String content = new String(mystore.getService().getItem(name),"utf-8");
				result = (JSONObject) JSONValue.parse(content);
				if(!(this.type==null||this.type.isEmpty())&&(this.country==null||this.country.isEmpty())){
					if(result.get("fclName").toString().contains(this.type.toLowerCase())){
						if(result.get("name").toString().toLowerCase().equals(this.input.toLowerCase())){
							System.out.println("---Result Found at Qunb---");
							output.add(result);
						}
					}
				}
				else if((this.type==null||this.type.isEmpty())&&!(this.country==null||this.country.isEmpty())){
					if(result.get("countryName").toString().toLowerCase().equals(this.country.toLowerCase())){
						if(result.get("name").toString().toLowerCase().equals(this.input.toLowerCase())){
							System.out.println("---Result Found at Qunb---");
							output.add(result);
						}
					}
				}
				else if(!(this.type==null||this.type.isEmpty())&&!(this.country==null||this.country.isEmpty())){
					if(result.get("fclName").toString().contains(this.type.toLowerCase())){
						if(result.get("name").toString().toLowerCase().equals(this.input.toLowerCase())&&result.get("countryName").toString().toLowerCase().equals(this.country.toLowerCase())){
							System.out.println("---Result Found at Qunb---");
							output.add(result);
						}
					}
				}
			}
		}
		System.out.println("---"+output.size()+" results found---");
		/* try to search on geonames in order to do the fuzzy match!
		if(output.size()==0){
			//add fuzzy match
			System.out.println("----Launch the Fuzzy Match----");
			for (String item:list_item) {
				if(item.endsWith(".json")){
					String name = item;
					String content = new String(mystore.getService().getItem(name),"utf-8");
					result = (JSONObject) JSONValue.parse(content);
					if(LetterSimilarity.isSimilarEnough(result.get("toponymName").toString().toLowerCase(), this.input.toLowerCase(), CouplingLevel.LOW)&&result.get("countryName").toString().toLowerCase().equals(this.country.toLowerCase())){
						System.out.println("---Match Found at Qunb---");
						output.add(result);
					}
				}
			}
		}*/
		
		if(output.size()==0){
			System.out.println("---Result Not Found at Qunb---");
		}
		return output;
	}
	public List<JSONObject> getData_countryCode() throws IOException{
		List<JSONObject> output = new ArrayList<JSONObject>();
		JSONObject result = new JSONObject();
		QunbS3Store mystore = new QunbS3Store(this.datastore);
		System.out.println("There are "+mystore.getService().listItems().size()+" data in qunbstore");
		List<String> list_item = mystore.getService().listItems();
		if(list_item.contains("index.json")){
			String index_content = new String(mystore.getService().getItem("index.json"),"utf-8");
			JSONObject index = (JSONObject) JSONValue.parse(index_content);
			if(!index.containsValue(this.input.toLowerCase())){
				return output;
			}
		}
		else{
			JSONObject index = new JSONObject();
			index.put("000000", "index");
			String content = index.toJSONString();
			byte[] cont = content.getBytes();
			String filename = "index.json";
			mystore.getService().storeItem(filename, cont);
			System.out.println("---index stored---");
		}
		boolean iscountryCode = false;
		for (String item:list_item) {
			if(item.endsWith(".json")&&!item.equals("index.json")){
				String name = item;
				String content = new String(mystore.getService().getItem(name),"utf-8");
				result = (JSONObject) JSONValue.parse(content);
				System.out.println(result.get("countryCode").toString());
					if(result.get("countryCode").toString().toLowerCase().equals(this.input.toLowerCase())&&result.get("fcode").toString().equals("PCLI")){
						output.add(result);
					}
			}
		}
		System.out.println("---"+output.size()+" results found---");
		if(output.size()==0){
			System.out.println("---Result Not Found at Qunb---");
		}
		return output;
	}
	public List<JSONObject> getData_1parameter() throws IOException{
		List<JSONObject> output = new ArrayList<JSONObject>();
		JSONObject result = new JSONObject();
		QunbS3Store mystore = new QunbS3Store(this.datastore);
		System.out.println("There are "+mystore.getService().listItems().size()+" data in qunbstore");
		List<String> list_item = mystore.getService().listItems();
		if(list_item.contains("index.json")){
			String index_content = new String(mystore.getService().getItem("index.json"),"utf-8");
			JSONObject index = (JSONObject) JSONValue.parse(index_content);
			if(!index.containsValue(this.input.toLowerCase())){
				return output;
			}
		}
		else{
			JSONObject index = new JSONObject();
			index.put("000000", "index");
			String content = index.toJSONString();
			byte[] cont = content.getBytes();
			String filename = "index.json";
			mystore.getService().storeItem(filename, cont);
			System.out.println("---index stored---");
		}
		System.out.println("There are "+mystore.getService().listItems().size()+" data in qunbstore");
		for (String item:list_item) {
			if(item.endsWith(".json")&&!item.equals("index.json")){
				String name = item;
				System.out.println(item);
				String content = new String(mystore.getService().getItem(name),"utf-8");

				result = (JSONObject) JSONValue.parse(content);
				//System.out.println(result.get("name").toString());
					if(result.get("name").toString().toLowerCase().equals(this.input.toLowerCase())){
						System.out.println("---Result Found at Qunb---");
						output.add(result);
					}
			}
		}
		System.out.println("---"+output.size()+" results found---");
//		if(output.size()==0){
//			for (String item:list_item) {
//				if(item.endsWith(".json")){
//					String name = item;
//					String content = new String(mystore.getService().getItem(name),"utf-8");
//					result = (JSONObject) JSONValue.parse(content);
//					if(LetterSimilarity.isSimilarEnough(result.get("toponymName").toString().toLowerCase(), this.input.toLowerCase(), CouplingLevel.MODERATE)){
//						System.out.println("---Match Found at Qunb---");
//						output.add(result);
//					}
//				}
//			}
//			System.out.println("---"+output.size()+" match found---");
//		}
		if(output.size()==0){
			System.out.println("---Result Not Found at Qunb---");
		}
		return output;
	}
	//problem!!!
	public List<JSONObject> constructResult() throws Exception{
		List<JSONObject> mydata = new ArrayList<JSONObject>();
		if((this.type==null||this.type.isEmpty())&&(this.country==null||this.country.isEmpty())&&this.input.length()!=2){
			System.out.println("Search with one parameter");
			mydata = this.getData_1parameter();
		}
		else if(this.input.length()==2){
			System.out.println("Search with country code");
			mydata = this.getData_countryCode();
		}
		else{
			System.out.println("Search with  parameters");
			mydata = this.getData_3parameters();
		}
		if(mydata.isEmpty()){
			return null;
		}
		return mydata;
	}
	
	public String getInput(){
		return this.input;
	}
	public String getType(){
		return this.type;
	}
	public String getDataStore(){
		return this.datastore;
	}
	
	public List<Map<String,Object>> getResult(){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		if(this.result!=null){
			for(int i = 0;i<this.result.size();i++){
				Map<String,Object> tmpmap = new HashMap<String,Object>();
				tmpmap.put("qunb:geoId", this.result.get(i).get("geonameId"));
				tmpmap.put("qunb:geoName", this.result.get(i).get("name"));
				tmpmap.put("qunb:geoLat", this.result.get(i).get("lat"));
				tmpmap.put("qunb:geoLng", this.result.get(i).get("lng"));
				tmpmap.put("qunb:geoType",this.result.get(i).get("fclName"));
				tmpmap.put("qunb:geoAlterNames", this.result.get(i).get("alternateNames"));
				tmpmap.put("qunb:population", this.result.get(i).get("population"));
				tmpmap.put("qunb:fclcode",this.result.get(i).get("fcl"));
				tmpmap.put("qunb:fcode",this.result.get(i).get("fcode"));
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
