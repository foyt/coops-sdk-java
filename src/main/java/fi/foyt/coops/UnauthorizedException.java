package fi.foyt.coops;

public class UnauthorizedException extends ServerException {

  private static final long serialVersionUID = -5524254439121764895L;

  public UnauthorizedException(String message) {
    super(message);
  }

}
