package fi.foyt.coops;

import org.apache.http.client.methods.HttpRequestBase;

public interface Auth {
  
  public void authenticateRequest(HttpRequestBase request);

}