package fi.foyt.coops;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fi.foyt.coops.io.DefaultIOHandler;
import fi.foyt.coops.io.IOHandler;
import fi.foyt.coops.model.File;
import fi.foyt.coops.model.FileJoin;
import fi.foyt.coops.model.Patch;

public class CoOps {
  
  public static final String PROTOCOL_VERSION = "1.0.0draft2";
  
  protected static final String CONTENT_TYPE_JSON = "application/json";
  
  public CoOps(String protocol, String host, int port, String basePath) {
    this(new DefaultIOHandler(), protocol, host, port, basePath);
  }
  
  public CoOps(IOHandler ioHandler, String protocol, String host, int port, String basePath) {
    this(ioHandler, createGson(), protocol, host, port, basePath);
  }
  
  public CoOps(IOHandler ioHandler, Gson gson, String protocol, String host, int port, String basePath) {
    this.gson = gson;
    this.ioHandler = ioHandler;
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
   * @return a file
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

  /**
   * Returns used IO handler
   * 
   * @return IO handler
   */
  public IOHandler getIoHandler() {
    return ioHandler;
  }
  
  /**
   * Returns used Gson instance
   * 
   * @return Gson instance
   */
  public Gson getGson() {
    return gson;
  }
  
  protected <T> T doGet(Class<T> resultClass, String path, Auth auth) throws ServerException, IOException {
    String response = ioHandler.doGetRequest(getURI(path), auth);
    return objectFromJson(resultClass, response);
  }
  
  protected <T> T doPost(Object entity, Class<T> resultClass, String path, String contentType, Auth auth) throws ServerException, IOException {
    String response = ioHandler.doPostRequest(getURI(path), objectToJson(entity), contentType, auth);
    if (resultClass != null) {
      return objectFromJson(resultClass, response);
    } else {
      return null;
    }
  }

  protected <T> T doPut(Object entity, Class<T> resultClass, String path, String contentType, Auth auth) throws ServerException, IOException {
    String response = ioHandler.doPutRequest(getURI(path), objectToJson(entity), contentType, auth);
    if (resultClass != null) {
      return objectFromJson(resultClass, response);
    } else {
      return null;
    }
  }

  protected <T> T doPatch(Object entity, Class<T> resultClass, String path, String contentType, Auth auth) throws ServerException, IOException {
    String response = ioHandler.doPatchRequest(getURI(path), objectToJson(entity), contentType, auth);
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
  
  private static Gson createGson() {
    return new GsonBuilder()
      .registerTypeAdapter(DateTime.class, new JodaDateTimeTypeConverter())
      .create();
  }
  
  private String protocol;
  private String host;
  private int port;
  private String basePath;
  private Gson gson;
  private IOHandler ioHandler;
}