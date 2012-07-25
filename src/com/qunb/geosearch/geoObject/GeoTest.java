package com.qunb.geosearch.geoObject;
import java.util.ArrayList;
import java.util.List;

import org.geonames.WebService;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStoreContextFactory;
import org.junit.Test;

public class GeoTest {
	
	//@Test
	/*public void test() throws Exception{
		WebService svc = new WebService();
		svc.setUserName("leipang");
		String qunbdatastore = "qunb.geo";
		String input = "Paris";
		String type = "city";
		String country = "France";
		List<GeoObject> result = new ArrayList<GeoObject>();
		result = getResultList(input,type,country,qunbdatastore);
		System.out.println(result.size());
	}
	public List<GeoObject> getResultList(String input,String type,String country,String datastore) throws Exception{
		List<GeoObject> list = new ArrayList<GeoObject>();
		
		QunbTextSearch mysearch = new QunbTextSearch(input,type,country,datastore);
		if(mysearch.getResult()==null){
			System.out.println("EEEEE");
			GeoNamesTextSearch mysearch2 = new GeoNamesTextSearch(input,type,country);
			mysearch2.storeData("qunb.geo");
			mysearch = new QunbTextSearch(input,type,country,datastore);
			list = mysearch.getResult();
		}
		else{
			System.out.println("oooooooo");
			list = mysearch.getResult();
		}
		return list;
	}*/
}
