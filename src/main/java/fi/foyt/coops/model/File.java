package fi.foyt.coops.model;

import org.joda.time.DateTime;

public class File {

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public FileUserRole getRole() {
    return role;
  }

  public void setRole(FileUserRole role) {
    this.role = role;
  }

  public DateTime getModified() {
    return modified;
  }
  
  public void setModified(DateTime modified) {
    this.modified = modified;
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

  private String id;
  
  private String name;
  
  private String content;
  
  private String contentType;
  
  private FileUserRole role;
  
  private DateTime modified;
  
  private Long revisionNumber;
}
