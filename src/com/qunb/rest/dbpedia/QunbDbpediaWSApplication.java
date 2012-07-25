package com.qunb.rest.dbpedia;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;


public class QunbDbpediaWSApplication extends Application {
	@Override
    public Restlet createRoot() {
	 Router router = new Router(getContext());
	 router.attachDefault(QunbDbpediaResource.class);
     return router;
    }

}
