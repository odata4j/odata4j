package org.odata4j.test.unit.format;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.odata4j.format.FormatType;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.producer.ErrorResponse;
import org.odata4j.producer.exceptions.ODataException;
import org.odata4j.producer.exceptions.ServerErrorException;

public abstract class AbstractErrorFormatWriterTest {

  private static final String MESSAGE = "This is an error message";
  private static final Throwable CAUSE = new IllegalArgumentException();

  private static final ODataException ODATA_EXCEPTION = new ServerErrorException();
  private static final ODataException ODATA_EXCEPTION_WITH_MESSAGE = new ServerErrorException(MESSAGE);
  private static final ODataException ODATA_EXCEPTION_WITH_CAUSE = new ServerErrorException(CAUSE);

  private static FormatWriter<ErrorResponse> formatWriter;

  private UriInfo uriInfoMock;
  private MultivaluedMap<String, String> queryParametersMock;

  private StringWriter stringWriter;

  protected static void createFormatWriter(FormatType format) {
    formatWriter = FormatWriterFactory.getFormatWriter(ErrorResponse.class, null, format.toString(), null);
  }

  @Before
  @SuppressWarnings("unchecked")
  public void setup() throws Exception {
    uriInfoMock = mock(UriInfo.class);
    queryParametersMock = mock(MultivaluedMap.class);

    when(uriInfoMock.getQueryParameters()).thenReturn(queryParametersMock);
    when(queryParametersMock.getFirst(anyString())).thenReturn(null);

    stringWriter = new StringWriter();
  }

  private void assertErrorResponse(String code, String message, String innerError) {
    assertTrue(Pattern.compile(buildRegex(code, message, innerError), Pattern.DOTALL).matcher(stringWriter.toString()).matches());
  }

  protected abstract String buildRegex(String code, String message, String innerError);

  private void simulateDebugParameterIsTrue() {
    when(queryParametersMock.getFirst("odata4j.debug")).thenReturn("true");
  }

  @Test
  public void code() throws Exception {
    formatWriter.write(uriInfoMock, stringWriter, ODATA_EXCEPTION);
    assertErrorResponse(ODATA_EXCEPTION.getClass().getSimpleName(), ".+", null);
  }

  @Test
  public void message() throws Exception {
    formatWriter.write(uriInfoMock, stringWriter, ODATA_EXCEPTION_WITH_MESSAGE);
    assertErrorResponse(".+", MESSAGE, null);
  }

  @Test
  public void innerError() throws Exception {
    simulateDebugParameterIsTrue();
    formatWriter.write(uriInfoMock, stringWriter, ODATA_EXCEPTION);
    assertErrorResponse(".+", ".+", ODATA_EXCEPTION.getClass().getName() + ".+");
  }

  @Test
  public void innerErrorWithCausedBy() throws Exception {
    simulateDebugParameterIsTrue();
    formatWriter.write(uriInfoMock, stringWriter, ODATA_EXCEPTION_WITH_CAUSE);
    assertErrorResponse(".+", ".+", ODATA_EXCEPTION_WITH_CAUSE.getClass().getName() + ".+Caused by: " + CAUSE.getClass().getName() + ".+");
  }
}
