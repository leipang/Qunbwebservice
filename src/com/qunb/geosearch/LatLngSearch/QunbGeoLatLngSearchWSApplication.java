package com.qunb.geosearch.LatLngSearch;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;


public class QunbGeoLatLngSearchWSApplication extends Application{
	 @Override
	    public Restlet createRoot() {
		 Router router = new Router(getContext());
		 router.attachDefault(QunbGeoLatLngSearchResource.class);
	     return router;
	    }

}
