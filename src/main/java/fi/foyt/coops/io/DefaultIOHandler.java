package fi.foyt.coops.io;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import fi.foyt.coops.Auth;
import fi.foyt.coops.ForbiddenException;
import fi.foyt.coops.ServerException;
import fi.foyt.coops.UnauthorizedException;

public class DefaultIOHandler implements IOHandler {

  @Override
  public String doGetRequest(URI uri, Auth auth) throws IOException, ServerException {
    return executeRequest(auth, new HttpGet(uri));
  }

  @Override
  public String doPostRequest(URI uri, String body, String contentType, Auth auth) throws IOException, ServerException {
    return doEntityEnclosingRequest(new HttpPost(uri), body, contentType, auth);
  }

  @Override
  public String doPutRequest(URI uri, String body, String contentType, Auth auth) throws IOException, ServerException {
    return doEntityEnclosingRequest(new HttpPut(uri), body, contentType, auth);
  }

  @Override
  public String doPatchRequest(URI uri, String body, String contentType, Auth auth) throws IOException, ServerException {
    return doEntityEnclosingRequest(new HttpPatch(uri), body, contentType, auth);
  }

  private String doEntityEnclosingRequest(HttpEntityEnclosingRequestBase request, String body, String contentType, Auth auth) throws IOException, ServerException {
    if (contentType != null) {
      request.setHeader("Content-type", contentType);
    }
    
    if (body != null) {
      request.setEntity(new StringEntity(body));
    }
    
    return executeRequest(auth, request);
  }
  
  private String executeRequest(Auth auth, HttpRequestBase request) throws IllegalStateException, IOException, ServerException {
    DefaultHttpClient httpclient = new DefaultHttpClient();

    authenticateRequest(auth, request);
    
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
  
  private void authenticateRequest(Auth auth, HttpRequestBase request) {
    if (auth != null) {
      if (auth.getHeaders() != null) {
        for (String headerName : auth.getHeaders().keySet()) {
          String headerValue = auth.getHeaders().get(headerName);
          request.addHeader(headerName, headerValue);
        }
      }
    }
  }
}
