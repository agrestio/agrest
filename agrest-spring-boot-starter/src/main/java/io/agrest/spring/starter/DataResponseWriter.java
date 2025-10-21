package io.agrest.spring.starter;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.DataResponse;
import io.agrest.runtime.jackson.IJacksonService;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class DataResponseWriter implements HttpMessageConverter<DataResponse<?>> {

  private final IJacksonService jacksonService;

  public DataResponseWriter(IJacksonService jacksonService) {
    this.jacksonService = jacksonService;
  }

  @Override
  public boolean canRead(Class<?> clazz, MediaType mediaType) {
    return false;
  }

  @Override
  public boolean canWrite(Class<?> clazz, MediaType mediaType) {
    return DataResponse.class.isAssignableFrom(clazz);
  }

  @Override
  public List<MediaType> getSupportedMediaTypes() {
    return null;
  }

  @Override
  public DataResponse<?> read(Class<? extends DataResponse<?>> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    return null;
  }

  @Override
  public void write(DataResponse<?> dataResponse, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    var entityStream = outputMessage.getBody(); // todo: do we need to close this?
    jacksonService.outputJson(out -> writeData(dataResponse, out), entityStream);
  }

  protected void writeData(DataResponse<?> t, JsonGenerator out) throws IOException {
    t.getEncoder().encode(null, t, out);
  }
}
