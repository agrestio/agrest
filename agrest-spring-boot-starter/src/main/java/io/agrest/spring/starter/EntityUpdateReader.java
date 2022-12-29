package io.agrest.spring.starter;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.reflect.Types;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class EntityUpdateReader implements GenericHttpMessageConverter<EntityUpdate<?>> {

  private final EntityUpdateReaderProcessor reader;

  public EntityUpdateReader(EntityUpdateReaderProcessor reader) {this.reader = reader;}

  @Override
  public boolean canRead(Class<?> clazz, MediaType mediaType) {
    // this is not called but we must implement it
    return false;
  }

  @Override
  public boolean canWrite(Class<?> clazz, MediaType mediaType) {
    return false;
  }

  @Override
  public List<MediaType> getSupportedMediaTypes() {
    return List.of(MediaType.APPLICATION_JSON);
  }

  @Override
  public EntityUpdate<?> read(Class<? extends EntityUpdate<?>> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    var entityStream = inputMessage.getBody();
    //todo this clazz here may be wrong
    Collection<EntityUpdate<Object>> updates = reader.read(clazz, entityStream);

    if (updates.isEmpty()) {
      throw AgException.badRequest("No update");
    }

    if (updates.size() > 1) {
      throw AgException.badRequest("Expected single update. Found: %s", updates.size());
    }

    return updates.iterator().next();
  }

  @Override
  public void write(EntityUpdate<?> entityUpdate, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    throw new IllegalStateException("Should never be called!");
  }

  @Override
  public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
    return EntityUpdate.class.equals(Types.getClassForType(type).orElse(Object.class))
        && MediaType.APPLICATION_JSON.isCompatibleWith(mediaType);
  }

  @Override
  public EntityUpdate<?> read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    var entityStream = inputMessage.getBody();
    //todo this clazz here may be wrong
    Collection<EntityUpdate<Object>> updates = reader.read(type, entityStream);

    if (updates.isEmpty()) {
      throw AgException.badRequest("No update");
    }

    if (updates.size() > 1) {
      throw AgException.badRequest("Expected single update. Found: %s", updates.size());
    }

    return updates.iterator().next();
  }

  @Override
  public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
    return false;
  }

  @Override
  public void write(EntityUpdate<?> entityUpdate, Type type, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    throw new IllegalStateException("Should never be called!");
  }
}
