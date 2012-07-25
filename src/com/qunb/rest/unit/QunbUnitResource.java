package com.qunb.rest.unit;
import java.io.IOException;
import java.util.Vector;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.json.JSONException;
import org.json.JSONObject;
import javax.ws.rs.Path;


@Path("/unit/search")
public class QunbUnitResource extends ServerResource {
	
	@Get
    public JSONObject download() throws IOException, JSONException {

		String file = "https://s3-eu-west-1.amazonaws.com/qunb01/upload/units.csv";
		String input = getQuery().getValues("q");
		String limit = null;
		String type = null;
		Vector<String []> results = UnitValidator.identifyCommonUnit(file, input);
		if (results==null){
			JSONObject no_result = UnitValidator.noResult();
			return no_result;
			
		}
		JSONObject result = UnitValidator.returnResult(results);
		if(getQuery().getFirst("limit")!=null&&!getQuery().getFirst("limit").getValue().isEmpty()){
				
				limit = getQuery().getValues("limit");
				if(Integer.parseInt(limit)<=results.size()){
					if(getQuery().getFirst("type")!=null&&!getQuery().getFirst("type").getValue().isEmpty()){
					type = getQuery().getValues("type");
					JSONObject output = UnitValidator.returnProposition(results, Integer.parseInt(limit),type);
					return output;
					}
					else{
						JSONObject output = UnitValidator.returnProposition(results, Integer.parseInt(limit),"null");
						return output;
					}
				}
				else{
					String key = "error";
					String value = "There are "+results.size()+" propositions.";
					JSONObject output = new JSONObject();
					output.put(key, value);
					return output;
				}
				
			
		}
		else{
			if(getQuery().getFirst("type")!=null&&!getQuery().getFirst("type").getValue().isEmpty()){
				type = getQuery().getValues("type");
				JSONObject output = UnitValidator.returnTypeProposition(results, type);
				return output;
			}
			
		}
		

			return result;

    }
}
