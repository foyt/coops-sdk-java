package fi.foyt.coops;

public class ForbiddenException extends ServerException {

  private static final long serialVersionUID = -5524254439121764895L;

  public ForbiddenException(String message) {
    super(message);
  }

}
