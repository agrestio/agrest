package io.agrest.spring.starter;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.SimpleResponse;
import io.agrest.runtime.jackson.IJacksonService;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class SimpleResponseWriter implements HttpMessageConverter<SimpleResponse> {

  private final IJacksonService jacksonService;

  public SimpleResponseWriter(IJacksonService jacksonService) {
    this.jacksonService = jacksonService;
  }

  @Override
  public boolean canRead(Class<?> clazz, MediaType mediaType) {
    return false;
  }

  @Override
  public boolean canWrite(Class<?> clazz, MediaType mediaType) {
    return SimpleResponse.class.isAssignableFrom(clazz);
  }

  @Override
  public List<MediaType> getSupportedMediaTypes() {
    return null;
  }

  @Override
  public SimpleResponse read(Class<? extends SimpleResponse> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    return null;
  }

  @Override
  public void write(SimpleResponse simpleResponse, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    var entityStream = outputMessage.getBody();
    jacksonService.outputJson(out -> writeData(simpleResponse, out), entityStream);
  }

  protected void writeData(SimpleResponse t, JsonGenerator out) throws IOException {
    out.writeStartObject();
    if (t.getMessage() != null) {
      out.writeStringField("message", t.getMessage());
    }
    out.writeEndObject();
  }
}
