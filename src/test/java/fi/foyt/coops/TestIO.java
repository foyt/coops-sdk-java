package fi.foyt.coops;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import fi.foyt.coops.io.IOHandler;

public class TestIO implements IOHandler {

  @Override
  public String doGetRequest(URI uri, Auth auth) throws IOException, ServerException {
    return executeRequest(uri);
  }

  @Override
  public String doPostRequest(URI uri, String body, String contentType, Auth auth) throws IOException, ServerException {
    return executeRequest(uri);
  }

  @Override
  public String doPutRequest(URI uri, String body, String contentType, Auth auth) throws IOException, ServerException {
    return executeRequest(uri);
  }

  @Override
  public String doPatchRequest(URI uri, String body, String contentType, Auth auth) throws IOException, ServerException {
    return executeRequest(uri);
  }

  public void addMockedResult(String path, String response) {
    mockedResult.put(path, response);
  }

  public void clearMockedResults() {
    mockedResult.clear();
  }
  
  public void addException(String path, Class<? extends Exception> exception) {
    exceptions.put(path, exception);
  }

  public void clearExceptions() {
    exceptions.clear();
  }
  
  private String executeRequest(URI uri) throws IOException, ServerException {
    String path = uri.getPath();
    String query = uri.getQuery();
    if (StringUtils.isNotBlank(query)) {
      path += "?" + query;
    }
    
    String result = mockedResult.get(path);
    if (result != null) {
      return result;
    } else {
      Class<? extends Exception> exceptionClass = exceptions.get(path);
      if (exceptionClass != null) {
        Exception exception;
        try {
          exception = exceptionClass.getConstructor(String.class).newInstance("Message!");
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
          throw new IOException("get request not mocked properly: Could not initialize exception");
        }
        
        if (exception instanceof ServerException) {
          throw (ServerException) exception;
        } else if (exception instanceof IOException) {
          throw (IOException) exception;
        } else {
          throw new IOException("get request not mocked properly: Invalid exception");
        }
      }
    }
    
    throw new IOException("get request not mocked properly: Could not find any mocked action");
  }
  
  private Map<String, String> mockedResult = new HashMap<>();
  private Map<String, Class<? extends Exception>> exceptions = new HashMap<>();
  
  
}
