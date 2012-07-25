package com.qunb.rest.dimension;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.qunb.rest.unit.QunbUnitResource;

public class QunbDimensionWSApplication extends Application {
	@Override
	public Restlet createRoot() {
	 Router router = new Router(getContext());
	 router.attachDefault(QunbDimensionResource.class);
     return router;
    }

}
