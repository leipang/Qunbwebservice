package com.qunb.rest.unit;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;
import java.net.URL ; 

import org.json.JSONException;
import org.json.JSONObject;
import com.qunb.fuzzymatch.*;
import com.qunb.fuzzymatch.LetterSimilarity.CouplingLevel;


public class UnitValidator {
	
	   private static int m_rowsCount;
	   private int m_colsCount;
	   private final static char CELL_SEPARATOR = ',';
	   private URL m_url;
	   private Vector<String[]> m_table;
	   
	   public UnitValidator(String path) throws IOException {
		    m_url = new URL(path); 
		   	m_table = new Vector<String[]> ();
		    readFromFile(this.getURL());
		   }
	   public static JSONObject noResult() throws JSONException{
		   JSONObject result = new JSONObject();
		   result.put("result", "no_result");
		   return result;
	   }
	   public static JSONObject returnResult(Vector<String[]> results) throws JSONException{
		   JSONObject result = new JSONObject();
		   Vector<String[]>results_classment= classResult(results);
		   for(int i=0;i<results_classment.size();i++){
			   String[] tmp = results_classment.get(i);
			   JSONObject one_result=  new JSONObject();
			   one_result.put("id", tmp[0]);
			   one_result.put("label", tmp[1]);
			   one_result.put("symbol", tmp[2]);
			   one_result.put("frequency", tmp[7]);
			   if(i==0){
				   try {
					   result.put("result", one_result);
				   } catch (JSONException e) {
					   e.printStackTrace();
				   }
			   }
			   else{
				   try {
					   result.accumulate("result", one_result);
				   } catch (JSONException e) {
					   e.printStackTrace();
				   }
			   }
		   }
		   return result;
	   }
	   public static Vector<String[]> classResult(Vector<String[]> result) throws JSONException{
		   for(int i =0;i<result.size()-1;i++){
			   for(int j = result.size()-1;j>i;j--){
				   String[] tmp = result.get(j);
				   String[] tmp_1 = result.get(j-1);
				   if(Integer.valueOf(tmp[7])>Integer.valueOf(tmp_1[7])){
					   result.set(j, tmp_1);
					   result.set(j-1,tmp);
				   }
				   else{
					   result.set(j, tmp);
					   result.set(j-1,tmp_1);
				   }
			   }
		   }
		   return result;
	   }
	public static JSONObject returnProposition(Vector<String[]> results,int limit,String type) throws JSONException{
		JSONObject result = new JSONObject();
		Boolean propositionexist = false;
		   for(int i=0;i<limit;i++){
			   String[] tmp = results.get(i);
			   JSONObject one_result=  new JSONObject();
			   if(!type.toLowerCase().equals("null")&& tmp[5].toLowerCase().equals(type.toLowerCase())){
				   propositionexist = true;
				   one_result.put("id", tmp[0]);
				   one_result.put("label", tmp[1]);
				   one_result.put("symbol", tmp[2]);
			   }
			   else if(type.toLowerCase().equals("null")){
				   propositionexist = true;
				   one_result.put("id", tmp[0]);
				   one_result.put("label", tmp[1]);
				   one_result.put("symbol", tmp[2]);
			   }
			   if(i==0){
				   try {
					   if(!one_result.isNull("id")){
					   result.put("result", one_result);
					   }
				   } catch (JSONException e) {
					   e.printStackTrace();
				   }
			   }
			   else{
				   try {
					   if(!one_result.isNull("id")){
					   result.accumulate("result", one_result);
					   }
				   } catch (JSONException e) {
					   e.printStackTrace();
				   }
			   }
		   }
		   if (!propositionexist){
			   
			   result.put("error", "There's no result found of this type!");
		   }
		   if(result.getJSONArray("result").length()<limit){
			   String key = "error";
				String value = "There are "+result.getJSONArray("result").length()+" propositions of this type.";
				JSONObject output = new JSONObject();
				output.put(key, value);
				return output;
		   }
		   return result;
	}
	public static JSONObject returnTypeProposition(Vector<String[]> results,String type) throws JSONException{
		JSONObject result = new JSONObject();
		Boolean typeexist = false;
		   for(int i=0;i<results.size();i++){
			   String[] tmp = results.get(i);
			   JSONObject one_result=  new JSONObject();
			   if(tmp[5].toLowerCase().equals(type.toLowerCase())){
				   typeexist = true;
				   one_result.put("id", tmp[0]);
				   one_result.put("label", tmp[1]);
				   one_result.put("symbol", tmp[2]);
				   if(i==0){
					   try {
						   result.put("result", one_result);
					   } catch (JSONException e) {
						   e.printStackTrace();
					   }
				   }
				   else{
					   try {
						   result.accumulate("result", one_result);
					   } catch (JSONException e) {
						   e.printStackTrace();
					   }
				   }
			   }
			   
		   }
		   if (!typeexist){
			   result.put("error", "There's no result found of this type!");
		   }
		   return result;
	}

	   public static Vector<String[]>identifyCommonUnit(String file,String input) throws IOException{
		   UnitValidator monFichier = new UnitValidator(file); 
		   Vector<String[]> value = new Vector<String[]>();
		   value.add(monFichier.getData(input)); 
		   if(value.get(0)==null){
			   value = monFichier.getProposition(input);
			   if(value.size()==0){
				   return null;
			   }
		   }
		   return value;
	   }
	   @SuppressWarnings("deprecation")
	   private void readFromFile(URL monURL){
		   DataInputStream m_data ;
		   try {
			m_data = new DataInputStream(monURL.openStream());
			String tempLine;
			tempLine = m_data.readLine();
			while (tempLine != null) {
					String[] element = readFromLine(tempLine);
	               m_table.add(element);
	               tempLine =m_data.readLine();
	            }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   }
	   
	   public static String[] readFromLine(String tempLine) {
		      if (tempLine == null) {
		         return null;
		      }
		      String[] cur =new String[8];
		      m_rowsCount++;
		      if (tempLine.trim().length() == 0) {
		         return null;
		      }
		      int colCount = 0;
		      int cursorBegin = 0;
		      int cursorEnd = tempLine.indexOf(CELL_SEPARATOR);
		      int cur_col=0;
		      while (cursorBegin > -1 && cur_col<cur.length) {
			         if (cursorEnd == -1) {
			            cur[cur.length-1]= tempLine.substring(cursorBegin);
			            cursorBegin = cursorEnd;
			         } else {
			            cur[cur_col]= tempLine.substring(cursorBegin, cursorEnd);
			            cur_col++;
			            cursorBegin = cursorEnd + 1;
			            
			         }
			         cursorEnd = tempLine.indexOf(CELL_SEPARATOR, cursorBegin);
			         colCount++;
			      }
			      return cur;
		   }

	   public int getColsCount() {
	      return m_colsCount;
	   }

	   public int getRowsCount() {
	      return m_rowsCount;
	   }
	   public URL getURL(){
		   return this.m_url;
	   }
	   public Vector<String[]> getTable(){
		   return this.m_table;
	   }

	   public int findRow(String input){
		   int row = -1;
		   if (input == null) {
		         return row;
		      }
		   for (int j = 0;j<this.m_table.size();j++){
			   
			   String[] tmp = this.m_table.get(j);
			   Boolean find = false;
			   for (int i = 0;i<3;i++){
				   if(tmp[i].toLowerCase().equals(input)){
					   find = true;
					   break;
				   }
			   }
			   if (find){
				   row = j;
				   break;
			   }
			   
		   }
		   
		   if(row == -1){
				  System.out.println("---Can not find the unit---\n\n");
			   }
		   
		   return row;
		   
	   }
	   
	   public int[] findPorpositionRow(String input){
		   Vector tmpvector = new Vector();
		   if (input == null) {
		         return null;
		      }
		   for (int j = 0;j<m_table.size();j++){
			   
			   String[] tmp = this.m_table.get(j);
			   Boolean find = false;
			   for(int i = 0;i<2;i++){
			  		   if(tmp[i].length()!=0 && LetterSimilarity.isSimilarEnough(tmp[i], input, CouplingLevel.LOW)){
						   find = true;
						   break;
					   }
					  
			   }
			   if (find){
				   tmpvector.add(j) ;
			   }
		   }
		   int[] propositionrow = new int[tmpvector.size()];
		   for(int k = 0;k<propositionrow.length;k++){
			   propositionrow[k] = Integer.parseInt(tmpvector.get(k).toString());
		   }
		   return propositionrow;
		   
	   }
	   
	   public String[] getData(String input) {
	       
		   int row = this.findRow(input);
		  if (row < 0 
	          || row > (getRowsCount() - 1)) {
	         return null;
	      }

	      try {
	         String[] theRow = m_table.get(row);
	         return theRow;
	      } catch (IndexOutOfBoundsException e) {
	         return null;
	      }
	   }
	   
	   public Vector<String[]> getProposition(String input){
		   int [] row = this.findPorpositionRow(input);
		   Vector<String[]> proposition = new Vector<String[]>();
		   for(int i = 0;i<row.length;i++){
			   proposition.add(m_table.get(row[i]));
		   }
		   return proposition;
	   }

	   
	   
	   
}