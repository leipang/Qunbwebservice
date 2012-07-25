package com.qunb.rest.unit;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;



public class QunbUnitWSApplication extends Application {
	 @Override
	    public Restlet createRoot() {
		 Router router = new Router(getContext());
		 router.attachDefault(QunbUnitResource.class);
	     return router;
	    }
	

}
