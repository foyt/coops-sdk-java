package fi.foyt.coops.io;

import java.io.IOException;
import java.net.URI;

import fi.foyt.coops.Auth;
import fi.foyt.coops.ServerException;

public interface IOHandler {

  /**
   * Performs a GET request into requested uri
   * 
   * @param uri requested uri
   * @param auth request authentication
   * @return result contents
   * @throws IOException IOException is thrown when error occurs in server communication
   * @throws ServerException ServerException is thrown when server responds with error code.
   */
  String doGetRequest(URI uri, Auth auth) throws IOException, ServerException;
  
  /**
   * Performs a POST request into the server
   * 
   * @param uri requested uri
   * @param body request body
   * @param contentType request content type
   * @param auth request authentication
   * @return result contents
   * @throws IOException IOException is thrown when error occurs in server communication
   * @throws ServerException ServerException is thrown when server responds with error code.
   */
  String doPostRequest(URI uri, String body, String contentType, Auth auth) throws IOException, ServerException;
  
  /**
   * Performs a PUT request into the server
   * 
   * @param uri requested uri
   * @param body request body
   * @param contentType request content type
   * @param auth request authentication
   * @return result contents
   * @throws IOException IOException is thrown when error occurs in server communication
   * @throws ServerException ServerException is thrown when server responds with error code.
   */
  String doPutRequest(URI uri, String body, String contentType, Auth auth) throws IOException, ServerException;
  
  /**
   * Performs a PATCH request into the server
   * 
   * @param uri requested uri
   * @param body request body
   * @param contentType request content type
   * @param auth request authentication
   * @return result contents
   * @throws IOException IOException is thrown when error occurs in server communication
   * @throws ServerException ServerException is thrown when server responds with error code.
   */
  String doPatchRequest(URI uri, String body, String contentType, Auth auth) throws IOException, ServerException;

}
