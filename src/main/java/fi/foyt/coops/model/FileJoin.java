package fi.foyt.coops.model;

public class FileJoin {

  public String[] getExtensions() {
    return extensions;
  }

  public void setExtensions(String[] extensions) {
    this.extensions = extensions;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public Long getRevisionNumber() {
    return revisionNumber;
  }

  public void setRevisionNumber(Long revisionNumber) {
    this.revisionNumber = revisionNumber;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getUnsecureWebSocketUrl() {
    return unsecureWebSocketUrl;
  }

  public void setUnsecureWebSocketUrl(String unsecureWebSocketUrl) {
    this.unsecureWebSocketUrl = unsecureWebSocketUrl;
  }

  public String getSecureWebSocketUrl() {
    return secureWebSocketUrl;
  }

  public void setSecureWebSocketUrl(String secureWebSocketUrl) {
    this.secureWebSocketUrl = secureWebSocketUrl;
  }

  private String[] extensions;
  private String fileId;
  private Long revisionNumber;
  private String content;
  private String contentType;
  
  // WebSocket extension
  private String clientId;
  private String unsecureWebSocketUrl;
  private String secureWebSocketUrl;
}