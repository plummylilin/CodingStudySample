import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
 
public class InvokeSFDCByRest {
 
    static final String USERNAME     = "interuser@163.com";
    static final String PASSWORD     = "zaq12wsxOLpwwOgbuEXqUEsAgNR1RWh1";
    static final String LOGINURL     = "https://login.salesforce.com";
    static final String GRANTSERVICE = "/services/oauth2/token?grant_type=password";
    static final String CLIENTID     = "3MVG9ZL0ppGP5UrBYpfWw6zrmOXoAovKp7dtX_uQTLD2_o0.hITANrc_7K4wyzMCty5lwigg9KMW.CzUEM_RD";
    static final String CLIENTSECRET = "7016260483534664487";
    private static String REST_ENDPOINT = "/services/apexrest" ;
    private static String baseUri;
    private static Header oauthHeader;
    private static Header prettyPrintHeader = new BasicHeader("X-PrettyPrint", "1");
    
    /**
     * 判断是否可以访问sfdc
     * return：可以访问sfdc的rest则返回true，否则返回false
     * */
    private static boolean isAccessable() {
        HttpClient httpclient = HttpClientBuilder.create().build();
         
        // Assemble the login request URL
        String loginURL = LOGINURL +
                          GRANTSERVICE +
                          "&client_id=" + CLIENTID +
                          "&client_secret=" + CLIENTSECRET +
                          "&username=" + USERNAME +
                          "&password=" + PASSWORD;
 
        // Login requests must be POSTs
        HttpPost httpPost = new HttpPost(loginURL);
        HttpResponse response = null;
        try {
            // Execute the login POST request
            response = httpclient.execute(httpPost);
        } catch (ClientProtocolException cpException) {
            cpException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        // verify response is HTTP OK
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            System.out.println("Error authenticating to Force.com: "+statusCode);
            return false;
        }
 
        String getResult = null;
        try {
            getResult = EntityUtils.toString(response.getEntity());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
 
        JSONObject jsonObject = null;
        String loginAccessToken = null;
        String loginInstanceUrl = null;
 
        try {
            jsonObject = (JSONObject) new JSONTokener(getResult).nextValue();
            loginAccessToken = jsonObject.getString("access_token");
            loginInstanceUrl = jsonObject.getString("instance_url");
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
 
        baseUri = loginInstanceUrl + REST_ENDPOINT + "/PunchCard";
        oauthHeader = new BasicHeader("Authorization", "OAuth " + loginAccessToken) ;
        System.out.println("oauthHeader1: " + oauthHeader);
        System.out.println(response.getStatusLine());
        System.out.println("Successful login");
        System.out.println("instance URL: "+loginInstanceUrl);
        System.out.println("access token/session ID: "+loginAccessToken);
        System.out.println("baseUri: "+ baseUri);        
        return true;
    }
    
    
    public static void main(String[] args) {
    	createPunchCard("English","English","10","YES");
        //deleteGoods("a052800000880mlAAA");
        PunchCard getPunchCard = getPunchCardById("a0D2800000a6LL6");
        if(getPunchCard != null) {
            System.out.println("PunchCard Category :" + getPunchCard.getCategory());
            System.out.println("PunchCard SubCategory : " + getPunchCard.getSubCategory());
            System.out.println("PunchCard Hours :" +getPunchCard.getHours());
            System.out.println("PunchCard Note : " + getPunchCard.getNote());
        }
//        List<PunchCard> PunchCardList = getPunchCardList(0);
//        System.out.println(PunchCardList.toString());
//        
//        PunchCard updatePunchCard = new PunchCard();
//        updatePunchCard.setPunchCardId("a0D2800000a6LL6");
//        updatePunchCard.setHours("10");
//        updatePunchCard.setNote("Java call SFDC");;
//        updatePunchCard(updatePunchCard);
    }
    
    // Create Goods using REST HttpPost
    public static void createPunchCard(String category,String subCategory,String hours,String isUseful) {
        try {
            if(isAccessable()) {
                String uri = baseUri + "/punchCardGoods";
                JSONObject punchCard = new JSONObject();
                punchCard.put("category", category);
                punchCard.put("subCategory", subCategory);
                punchCard.put("hours", hours);
                punchCard.put("isUseful",isUseful);
     
                System.out.println("JSON for goods record to be inserted:\n" + punchCard.toString(1));
                //Construct the objects needed for the request
                HttpClient httpClient = HttpClientBuilder.create().build();
                System.out.println("oauthHeader" + oauthHeader);
                HttpPost httpPost = new HttpPost(uri);
                httpPost.addHeader(oauthHeader);
                httpPost.addHeader(prettyPrintHeader);
                httpPost.addHeader("encoding", "UTF-8");
                // The message we are going to post
                StringEntity body = new StringEntity(punchCard.toString(1));
                body.setContentType("application/json");
                httpPost.setEntity(body);
     
                //Make the request
                HttpResponse response = httpClient.execute(httpPost);
                System.out.print("response : " + response.toString());
                //Process the results
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("status code : " + statusCode);
                if (statusCode == HttpStatus.SC_OK) {
                    String response_string = EntityUtils.toString(response.getEntity());
                    if(response_string != null ) {
                        System.out.println("New Goods id from response: " + response_string);
                    }
                } else {
                    System.out.println("Insertion unsuccessful. Status code returned is " + statusCode);
                }
                httpPost.releaseConnection();
            }
        } catch (JSONException e) {
            System.out.println("Issue creating JSON or processing results");
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    // Delete PunchCard using REST httpDelete
    public static void deletePunchCard(String punchCardId) {
        try {
            if(isAccessable()) {
                String uri = baseUri + "/deletePunchCard" + "/" + punchCardId;
                HttpClient httpClient = HttpClientBuilder.create().build();
                 
                HttpDelete httpDelete = new HttpDelete(uri);
                httpDelete.addHeader(oauthHeader);
                httpDelete.addHeader(prettyPrintHeader);
     
                //Make the request
                HttpResponse response = httpClient.execute(httpDelete);
     
                //Process the response
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    System.out.println("Deleted the goods successfully.");
                } else {
                    System.out.println("goods delete NOT successful. Status code is " + statusCode);
                }
                httpDelete.releaseConnection();
            }
        } catch (JSONException e) {
            System.out.println("Issue creating JSON or processing results");
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }
    
    // Get PunchCard List using REST httpGet
    public static List<PunchCard> getPunchCardList(Integer pageNumber) {
        try {
            if(isAccessable()) {
                String uri = baseUri + "/getPunchCardById" + "？currentPage=" + pageNumber;
                HttpClient httpClient = HttpClientBuilder.create().build(); 
                HttpGet httpGet = new HttpGet(uri);
                httpGet.addHeader(oauthHeader);
                httpGet.addHeader(prettyPrintHeader);
                //Make the request
                HttpResponse response = httpClient.execute(httpGet);
                //Process the response
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    List<PunchCard> punchCardList = new ArrayList<PunchCard>();
                    String response_string = EntityUtils.toString(response.getEntity());
                    System.out.println("response_string : " + response_string);
                    JSONArray jsonArray = new JSONArray(response_string);
                    JSONObject jsonObject = null;
                    for(int i=0;i<jsonArray.length();i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        PunchCard punchCard = new PunchCard();
                        if(jsonObject != null) {
                        	punchCard.setCategory(jsonObject.getString("Category__c"));
                        	punchCard.setSubCategory(jsonObject.getString("subCategory__c"));
                        	punchCard.setHours(jsonObject.getString("Hours__c"));
                        	punchCard.setNote(jsonObject.getString("Notes__c"));
                        	punchCardList.add(punchCard);
                        }
                    }
                    return punchCardList;
            } else {
                return null;
            }
            }
        }catch (JSONException e) {
            System.out.println("Issue creating JSON or processing results");
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        return null;
    }
    
    // Get PunchCard using REST httpGet
    public static PunchCard  getPunchCardById(String punchCardId) {
        try {
            if(isAccessable()) {
                String uri = baseUri + "/getPunchCardById" + "?punchCardId=" + punchCardId;
                HttpClient httpClient = HttpClientBuilder.create().build();
                 
                HttpGet httpGet = new HttpGet(uri);
                httpGet.addHeader(oauthHeader);
                httpGet.addHeader(prettyPrintHeader);
     
                //Make the request
                HttpResponse response = httpClient.execute(httpGet);
     
                //Process the response
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    String response_string = EntityUtils.toString(response.getEntity());
                    System.out.println("response_string : " + response_string);
                    JSONArray jsonArray = new JSONArray(response_string);
                    JSONObject jsonObject = null;
                    if(jsonArray.length() > 0) {
                        jsonObject = jsonArray.getJSONObject(0);
                    }
                    
                    PunchCard punchCard = new PunchCard();
                    if(jsonObject != null) {
                    	punchCard.setCategory(jsonObject.getString("Category__c"));
                    	punchCard.setSubCategory(jsonObject.getString("subCategory__c"));
                    	punchCard.setHours(String.valueOf(jsonObject.getInt("Hours__c")));
                    	punchCard.setNote(jsonObject.getString("Notes__c"));
                    }
                    return punchCard;
                } else {
                    return null;
                }
            }
        } catch (JSONException e) {
            System.out.println("Issue creating JSON or processing results");
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        return null;
    }
    
    // Update PunchCard using REST httpPatch
    public static void updatePunchCard(PunchCard updatePunchCard) {
        try {
            if(isAccessable()) {
                String uri = baseUri + "/updateGoods/"+updatePunchCard.getPunchCardId();
                JSONObject punchCard = new JSONObject();
                punchCard.put("Hours__c", updatePunchCard.getHours());
                punchCard.put("Notes__c", updatePunchCard.getNote());
                org.apache.http.client.methods.HttpPatch httpPatch = new org.apache.http.client.methods.HttpPatch(uri);
                HttpClient httpClient = HttpClientBuilder.create().build();
                httpPatch.addHeader(oauthHeader);
                httpPatch.addHeader(prettyPrintHeader);
                StringEntity body = new StringEntity(punchCard.toString(1));
                body.setContentType("application/json");
                httpPatch.setEntity(body);
     
                //Make the request
                HttpResponse response = httpClient.execute(httpPatch);
     
                //Process the response
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    System.out.println("Updated the goods successfully.");
                } else {
                    System.out.println("Goods update NOT successfully. Status code is " + statusCode);
                }
            }
        }catch (JSONException e) {
            System.out.println("Issue creating JSON or processing results");
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }
    
    
}