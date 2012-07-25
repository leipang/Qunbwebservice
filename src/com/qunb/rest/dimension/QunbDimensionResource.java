package com.qunb.rest.dimension;
import java.io.IOException;
import java.util.Vector;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.json.JSONException;
import org.json.JSONObject;



import javax.ws.rs.Path;

@Path("/dimension/search")
public class QunbDimensionResource extends ServerResource {
	@Get
    public JSONObject download() throws IOException, JSONException {
		String file = "https://s3-eu-west-1.amazonaws.com/qunb01/upload/dimension.csv";
		String input = getQuery().getValues("q");
		String limit = null;
		String type = null;
		Vector<String []> results = DimensionValidator.identifyCommonUnit(file, input);
		if (results==null){
			JSONObject no_result = DimensionValidator.noResult();
			return no_result;
			
		}
		Vector<String []> results_classement = DimensionValidator.classResult(results);
		JSONObject result = DimensionValidator.returnResult(results_classement);
		if(getQuery().getFirst("limit")!=null&&!getQuery().getFirst("limit").getValue().isEmpty()){
				
				limit = getQuery().getValues("limit");
				if(Integer.parseInt(limit)<=results.size()){
					if(getQuery().getFirst("type")!=null&&!getQuery().getFirst("type").getValue().isEmpty()){
					type = getQuery().getValues("type");
					JSONObject output = DimensionValidator.returnProposition(results_classement, Integer.parseInt(limit),type);
					return output;
					}
					else{
						JSONObject output = DimensionValidator.returnProposition(results_classement, Integer.parseInt(limit),"null");
						return output;
					}
				}
				else{
					String key = "error";
					String value = "There are "+results_classement.size()+" propositions.";
					JSONObject output = new JSONObject();
					output.put(key, value);
					return output;
				}
				
			
		}
		else{
			if(getQuery().getFirst("type")!=null&&!getQuery().getFirst("type").getValue().isEmpty()){
				type = getQuery().getValues("type");
				JSONObject output = DimensionValidator.returnTypeProposition(results_classement, type);
				return output;
			}
			
		}
		

			return result;

    }

}
