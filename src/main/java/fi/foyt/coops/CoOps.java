package fi.foyt.coops;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fi.foyt.coops.model.File;
import fi.foyt.coops.model.FileJoin;
import fi.foyt.coops.model.Patch;

public class CoOps {
  
  public static final String PROTOCOL_VERSION = "1.0.0draft2";
  
  protected static final String CONTENT_TYPE_JSON = "application/json";
  
  public CoOps(String protocol, String host, int port, String basePath) {
    this.gson = createGson();
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.basePath = basePath;
  }
  
  /**
   * Joins collaboration session
   * 
   * @param algorithms algorithms supported by client in preference order
   * @param auth authentication used for this request
   * @return file join result
   * @throws ServerException ServerException is thrown when server returns does not return a successful result
   * @throws IOException IOException is thrown when error occurs in server communication
   * @throws UsageException UsageException is thrown when method is called incorrectly
   */
  public FileJoin joinFile(String[] algorithms, Auth auth) throws UsageException, ServerException, IOException {
    if ((algorithms == null)||(algorithms.length == 0)) {
      throw new UsageException("At least one algorithm needs to be defined");
    }
    
    StringBuilder pathBuilder = new StringBuilder()
      .append(basePath)
      .append("/join?protocolVersion=")
      .append(PROTOCOL_VERSION);
    
    for (String algorithm : algorithms) {
      pathBuilder
        .append("&algorithm=")
        .append(URLEncoder.encode(algorithm, "UTF-8"));
    }
    
    return doGet(FileJoin.class, pathBuilder.toString(), auth);
  }
  
  /**
   * Returns a file
   * 
   * @param auth authentication used for this request
   * @return
   * @throws ServerException ServerException is thrown when server returns does not return a successful result
   * @throws IOException IOException is thrown when error occurs in server communication
   * @throws UsageException UsageException is thrown when method is called incorrectly
   */
  public File getFile(Auth auth) throws UsageException, ServerException, IOException {
    return doGet(File.class, basePath, auth);
  }
  
  /**
   * Returns specific version of a file
   * 
   * @param revisionNumber revision number that will be returned
   * @param auth authentication used for this request
   * @return file as it was in specified version
   * @throws ServerException ServerException is thrown when server returns does not return a successful result
   * @throws IOException IOException is thrown when error occurs in server communication
   * @throws UsageException UsageException is thrown when method is called incorrectly
   */
  public File getFileRevision(Long revisionNumber, Auth auth) throws UsageException, ServerException, IOException {
    if (revisionNumber == null) {
      throw new UsageException("revisionNumber is required");
    }
    
    return doGet(File.class, basePath + "?revisionNumber=" + revisionNumber, auth);
  }
  
  /**
   * Saves a file
   * 
   * @param file a file
   * @param auth authentication used for this request
   * @throws ServerException ServerException is thrown when server returns does not return a successful result
   * @throws IOException IOException is thrown when error occurs in server communication
   * @throws UsageException UsageException is thrown when method is called incorrectly
   */
  public void saveFile(File file, Auth auth) throws UsageException, ServerException, IOException {
    doPut(file, null, basePath, CONTENT_TYPE_JSON, auth);
  }
  
  /**
   * Patches a file
   * 
   * @param patch patch
   * @param auth authentication used for this request
   * @throws ServerException ServerException is thrown when server returns does not return a successful result
   * @throws IOException IOException is thrown when error occurs in server communication
   * @throws UsageException UsageException is thrown when method is called incorrectly
   */
  public void patchFile(Patch patch, Auth auth) throws UsageException, ServerException, IOException {
    if (StringUtils.isBlank(patch.getAlgorithm())) {
      throw new UsageException("algorithm is required");
    }
    
    if (patch.getRevisionNumber() == null) {
      throw new UsageException("revisionNumber is required");
    }
    
    doPatch(patch, null, basePath, CONTENT_TYPE_JSON, auth);
  }
  
  protected <T> T doGet(Class<T> resultClass, String path, Auth auth) throws ServerException, IOException {
    String response = doGetRequest(path, auth);
    return objectFromJson(resultClass, response);
  }
  
  protected <T> T doPost(Object entity, Class<T> resultClass, String path, String contentType, Auth auth) throws ServerException, IOException {
    String response = doPostRequest("POST", path, objectToJson(entity), contentType, auth);
    if (resultClass != null) {
      return objectFromJson(resultClass, response);
    } else {
      return null;
    }
  }

  protected <T> T doPut(Object entity, Class<T> resultClass, String path, String contentType, Auth auth) throws ServerException, IOException {
    String response = doPostRequest("PUT", path, objectToJson(entity), contentType, auth);
    if (resultClass != null) {
      return objectFromJson(resultClass, response);
    } else {
      return null;
    }
  }

  protected <T> T doPatch(Object entity, Class<T> resultClass, String path, String contentType, Auth auth) throws ServerException, IOException {
    String response = doPostRequest("PATCH", path, objectToJson(entity), contentType, auth);
    if (resultClass != null) {
      return objectFromJson(resultClass, response);
    } else {
      return null;
    }
  }
  
  protected <T> T objectFromJson(Class<T> resultClass, String json) {
    return gson.fromJson(json, resultClass);
  }
  
  protected String objectToJson(Object object) {
    return gson.toJson(object);
  }
  
  protected URI getURI(String path) throws IOException {
    try {
      return new URL(protocol, host, port, path).toURI();
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }
  }
  
  protected String doGetRequest(String path, Auth auth) throws IOException, ServerException {
    HttpGet httpGet = new HttpGet(getURI(path)); 
    
    DefaultHttpClient httpclient = new DefaultHttpClient();
    auth.authenticateRequest(httpGet);
    
    HttpResponse response = httpclient.execute(httpGet);
    HttpEntity entity = response.getEntity();
    try {
      int status = response.getStatusLine().getStatusCode();
      if (status == 204) {
       // No Content
        return null;
      }
      
      String content = IOUtils.toString(entity.getContent());
      if (status == 200) {
        return content;
      }
      
      throw new ServerException(content);
    } finally {
      EntityUtils.consume(entity);
    }
  }
  
  protected String doPostRequest(String verb, String path, String body, String contentType, Auth auth) throws IOException, ServerException {
    HttpEntityEnclosingRequestBase request = null;
    
    switch (verb) {
      case "POST":
        request = new HttpPost(getURI(path));
      break;
      case "PUT":
        request = new HttpPut(getURI(path));
      break;
      case "PATCH":
        request = new HttpPatch(getURI(path));
      break;
    }
    
    DefaultHttpClient httpclient = new DefaultHttpClient();
    auth.authenticateRequest(request);
    
    if (contentType != null) {
      request.setHeader("Content-type", contentType);
    }
    
    if (body != null) {
      request.setEntity(new StringEntity(body));
    }
    
    HttpResponse response = httpclient.execute(request);
    HttpEntity entity = response.getEntity();
    try {
      int status = response.getStatusLine().getStatusCode();
      if (status == 204) {
       // No Content
        return null;
      }
      
      String content = IOUtils.toString(entity.getContent());
      if (status == 200) {
        return content;
      }
      
      switch (status) {
        case 200:
          return content;
        case 401:
          throw new UnauthorizedException(content);
        case 403:
          throw new ForbiddenException(content);
      }

      throw new ServerException(content);
    } finally {
      EntityUtils.consume(entity);
    }
  }
  
  private String protocol;
  private String host;
  private int port;
  private String basePath;
  private Gson gson;
  
  private Gson createGson() {
    return new GsonBuilder()
      .registerTypeAdapter(DateTime.class, new JodaDateTimeTypeConverter())
      .create();
  }
}