package com.qunb.geosearch.TextSearch;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;


public class QunbGeoTextSearchWSApplication extends Application{
	 @Override
	    public Restlet createRoot() {
		 Router router = new Router(getContext());
		 router.attachDefault(QunbGeoTextSearchResource.class);
	     return router;
	    }

}
