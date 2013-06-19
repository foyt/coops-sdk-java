package fi.foyt.coops.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import fi.foyt.coops.CoOps;
import fi.foyt.coops.ForbiddenException;
import fi.foyt.coops.ServerException;
import fi.foyt.coops.TestIO;
import fi.foyt.coops.UnauthorizedException;
import fi.foyt.coops.UsageException;
import fi.foyt.coops.io.IOHandler;
import fi.foyt.coops.model.File;
import fi.foyt.coops.model.FileJoin;
import fi.foyt.coops.model.FileUserRole;
import fi.foyt.coops.model.Patch;

public class CoOpsTest {

  @Test
  public void testCoOpsStringStringIntString() {
    CoOps coOps = new CoOps("http", "localhost", 80, "");
    assertNotNull(coOps);
  }
  
  @Test (expected = UnauthorizedException.class)
  public void testUnauthorizedException() throws UsageException, ServerException, IOException {
    TestIO testIO = new TestIO();
    CoOps coOps = new CoOps(testIO, "http", "localhost", 80, "");
    testIO.addException("", UnauthorizedException.class);
    coOps.getFile(null);
  }
  
  @Test (expected = ForbiddenException.class)
  public void testForbiddenException() throws UsageException, ServerException, IOException {
    TestIO testIO = new TestIO();
    CoOps coOps = new CoOps(testIO, "http", "localhost", 80, "");
    testIO.addException("", ForbiddenException.class);
    coOps.getFile(null);
  }
  
  @Test (expected = ServerException.class)
  public void testServerException() throws UsageException, ServerException, IOException {
    TestIO testIO = new TestIO();
    CoOps coOps = new CoOps(testIO, "http", "localhost", 80, "");
    testIO.addException("", ServerException.class);
    coOps.getFile(null);
  }
  
  @Test (expected = IOException.class)
  public void testIOException() throws UsageException, ServerException, IOException {
    TestIO testIO = new TestIO();
    CoOps coOps = new CoOps(testIO, "http", "localhost", 80, "");
    testIO.addException("", IOException.class);
    coOps.getFile(null);
  }  

  @Test
  public void testCoOpsIOHandlerStringStringIntString() {
    IOHandler ioHandler = new TestIO();
    
    CoOps coOps = new CoOps(ioHandler, "http", "localhost", 80, "");
    assertNotNull(coOps);
    assertEquals(ioHandler, coOps.getIoHandler());
  }

  @Test
  public void testCoOpsIOHandlerGsonStringStringIntString() {
    IOHandler ioHandler = new TestIO();
    Gson gson = new Gson();
    
    CoOps coOps = new CoOps(ioHandler, gson, "http", "localhost", 80, "");
    assertNotNull(coOps);
    assertEquals(ioHandler, coOps.getIoHandler());
    assertEquals(gson, coOps.getGson());
  }

  @Test
  public void testJoinFile() throws UsageException, ServerException, IOException {
    TestIO testIO = new TestIO();
    
    CoOps coOps = new CoOps(testIO, "http", "localhost", 80, "");
    assertNotNull(coOps);
    
    // Most common case
    
    testIO.addMockedResult("/join?protocolVersion=" + CoOps.PROTOCOL_VERSION + "&algorithm=dummy&algorithm=bogus", 
        "{" +
          "\"extensions\": [\"dummy\", \"bogus\"]," +
          "\"fileId\": \"00001111\"," +
          "\"revisionNumber\": 12," +
          "\"content\": \"abc\"," + 
          "\"contentType\": \"text/plain\"" +
        "}"
    );
    
    FileJoin joinFile = coOps.joinFile(new String[] { "dummy", "bogus" }, null);
    assertNotNull(joinFile);
    assertEquals("00001111", joinFile.getFileId());
    assertNotNull(joinFile.getExtensions());
    assertEquals(2, joinFile.getExtensions().length);
    assertEquals("dummy", joinFile.getExtensions()[0] );
    assertEquals("bogus", joinFile.getExtensions()[1] );
    assertEquals(new Long(12), joinFile.getRevisionNumber() );
    assertEquals("abc", joinFile.getContent() );
    assertEquals("text/plain", joinFile.getContentType() );
    
    // WebSocket 
    
    testIO.addMockedResult("/join?protocolVersion=" + CoOps.PROTOCOL_VERSION + "&algorithm=dummy", 
        "{" +
          "\"extensions\": [\"dummy\"]," +
          "\"fileId\": \"00001111\"," +
          "\"revisionNumber\": 12," +
          "\"content\": \"abc\"," + 
          "\"contentType\": \"text/plain\"," +
          "\"clientId\": \"123\"," + 
          "\"unsecureWebSocketUrl\": \"ws://url\"," + 
          "\"secureWebSocketUrl\": \"wss://url\"" + 
        "}"
    );
    
    joinFile = coOps.joinFile(new String[] { "dummy" }, null);
    assertNotNull(joinFile);
    assertEquals(joinFile.getClientId(), "123"  );
    assertEquals(joinFile.getUnsecureWebSocketUrl(), "ws://url" );
    assertEquals(joinFile.getSecureWebSocketUrl(), "wss://url" );
  }
  
  @Test (expected = UsageException.class)
  public void testJoinFileNullAlgorithm() throws UsageException, ServerException, IOException {
    TestIO testIO = new TestIO();
    
    CoOps coOps = new CoOps(testIO, "http", "localhost", 80, "");
    coOps.joinFile(null, null);
  }
  
  @Test (expected = UsageException.class)
  public void testJoinFileEmptyAlgorithm() throws UsageException, ServerException, IOException {
    TestIO testIO = new TestIO();
    
    CoOps coOps = new CoOps(testIO, "http", "localhost", 80, "");
    coOps.joinFile(new String[] { }, null);
  }

  @Test
  public void testGetFile() throws UsageException, ServerException, IOException {
    TestIO testIO = new TestIO();
    CoOps coOps = new CoOps(testIO, "http", "localhost", 80, "");
    testIO.addMockedResult("", 
      "{" + 
      "  \"id\": \"1234\"," +
      "  \"name\": \"Name\"," +
      "  \"modified\": \"2010-02-03T04:05:06.078Z\"," +
      "  \"revisionNumber\": 22," +
      "  \"content\": \"bca\"," +
      "  \"contentType\": \"text/html;editor=CKEditor\"," +
      "  \"role\": \"OWNER\"" +
      "}");
    
    File file = coOps.getFile(null);

    assertNotNull(file);
    assertEquals("1234", file.getId());
    assertEquals("Name", file.getName());
    assertEquals(2010, file.getModified().getYear());
    assertEquals(2, file.getModified().getMonthOfYear());
    assertEquals(3, file.getModified().getDayOfMonth());
    assertEquals(new Long(22), file.getRevisionNumber());
    assertEquals("bca", file.getContent());
    assertEquals("text/html;editor=CKEditor", file.getContentType());
    assertEquals(FileUserRole.OWNER, file.getRole());
  }

  @Test
  public void testGetFileRevision() throws UsageException, ServerException, IOException {
    TestIO testIO = new TestIO();
    CoOps coOps = new CoOps(testIO, "http", "localhost", 80, "");
    testIO.addMockedResult("?revisionNumber=6", 
      "{" + 
      "  \"id\": \"1234\"," +
      "  \"name\": \"Name\"," +
      "  \"modified\": \"2010-02-02T04:05:06.078Z\"," +
      "  \"revisionNumber\": 6," +
      "  \"content\": \"bbb\"," +
      "  \"contentType\": \"text/html;editor=CKEditor\"," +
      "  \"role\": \"OWNER\"" +
      "}");
    
    File fileRevision = coOps.getFileRevision(6l, null);

    assertNotNull(fileRevision);
    assertEquals("1234", fileRevision.getId());
    assertEquals("Name", fileRevision.getName());
    assertEquals(2010, fileRevision.getModified().getYear());
    assertEquals(2, fileRevision.getModified().getMonthOfYear());
    assertEquals(2, fileRevision.getModified().getDayOfMonth());
    assertEquals(new Long(6), fileRevision.getRevisionNumber());
    assertEquals("bbb", fileRevision.getContent());
    assertEquals("text/html;editor=CKEditor", fileRevision.getContentType());
    assertEquals(FileUserRole.OWNER, fileRevision.getRole());
  }
  
  @Test (expected = UsageException.class)
  public void testGetFileRevisionNullRevision() throws UsageException, ServerException, IOException {
    TestIO testIO = new TestIO();
    CoOps coOps = new CoOps(testIO, "http", "localhost", 80, "");
    coOps.getFileRevision(null, null);
  }

  @Test
  public void testSaveFile() throws UsageException, ServerException, IOException {
    TestIO testIO = new TestIO();
    CoOps coOps = new CoOps(testIO, "http", "localhost", 80, "");
    testIO.addMockedResult("", "");
    
    File file = new File();
    file.setContent("abcd");
    file.setContentType("text/plain");
    file.setModified(new DateTime(2012, 11, 10, 9, 8, 7, 6, GregorianChronology.getInstance()));
    file.setName("asd");
    file.setRevisionNumber(66l);
    file.setRole(FileUserRole.OWNER);
    
    coOps.saveFile(file, null);
  }

  @Test
  public void testPatchFile() throws UsageException, ServerException, IOException {
    TestIO testIO = new TestIO();
    CoOps coOps = new CoOps(testIO, "http", "localhost", 80, "");
    testIO.addMockedResult("", "");
    
    Map<String, String> properties = new HashMap<>();
    properties.put("meta", "value");
    
    Patch patch = new Patch();
    patch.setAlgorithm("dummy");
    patch.setPatch("change");
    patch.setProperties(properties);
    patch.setRevisionNumber(666l);
    
    coOps.patchFile(patch, null);
  }
  
  @Test (expected = UsageException.class)
  public void testPatchFileNullAlgorithm() throws UsageException, ServerException, IOException {
    CoOps coOps = new CoOps("http", "localhost", 80, "");
    
    Patch patch = new Patch();
    patch.setPatch("change");
    patch.setRevisionNumber(666l);
    
    coOps.patchFile(patch, null);
  }
  
  @Test (expected = UsageException.class)
  public void testPatchFileNullRevisionNumber() throws UsageException, ServerException, IOException {
    CoOps coOps = new CoOps("http", "localhost", 80, "");
    
    Patch patch = new Patch();
    patch.setAlgorithm("dummy");
    patch.setPatch("change");
    
    coOps.patchFile(patch, null);
  }

  @Test (expected = JsonSyntaxException.class)
  public void testInvalidResult() throws UsageException, ServerException, IOException {
    TestIO testIO = new TestIO();
    CoOps coOps = new CoOps(testIO, "http", "localhost", 80, "");
    testIO.addMockedResult("", 
      "{" + 
      "  \"modified\": \"sss\"" + 
      "}");
    
    coOps.getFile(null);
  }
  
  @Test
  public void testExtendedResult() throws UsageException, ServerException, IOException {
    TestIO testIO = new TestIO();
    CoOps coOps = new CoOps(testIO, "http", "localhost", 80, "");
    testIO.addMockedResult("", 
      "{" + 
      "  \"id\": \"1234\"," +
      "  \"name\": \"Name\"," +
      "  \"modified\": \"2010-02-03T04:05:06.078Z\"," +
      "  \"revisionNumber\": 22," +
      "  \"content\": \"bca\"," +
      "  \"contentType\": \"text/html;editor=CKEditor\"," +
      "  \"role\": \"OWNER\"," +
      "  \"extensionProvided\": \"field\"" +
      "}");
    
    File file = coOps.getFile(null);
    assertNotNull(file);
  }

}
