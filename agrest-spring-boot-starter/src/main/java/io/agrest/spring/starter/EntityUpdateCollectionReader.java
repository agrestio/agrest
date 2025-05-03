package io.agrest.spring.starter;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.reflect.Types;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class EntityUpdateCollectionReader<T> implements GenericHttpMessageConverter<Collection<EntityUpdate<T>>> {

  private final EntityUpdateReaderProcessor reader;

  public EntityUpdateCollectionReader(EntityUpdateReaderProcessor reader) {this.reader = reader;}

  @Override
  public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
    var classForType = Types.getClassForType(type).orElse(Object.class);
    if (!Collection.class.isAssignableFrom(classForType) || !MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
      return false;
    }

    return Types.unwrapTypeArgument(type)
        .filter(collectionParam -> collectionParam instanceof ParameterizedType)
        .map(collectionParam -> (ParameterizedType) collectionParam)
        .filter(collectionParam -> EntityUpdate.class.equals((collectionParam).getRawType()))
        .isPresent();
  }

  @Override
  public Collection<EntityUpdate<T>> read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    Type entityUpdateType = Types.unwrapTypeArgument(type)
        .orElseThrow(() -> AgException.internalServerError("Invalid request entity collection type: %s", type));

    return reader.read(entityUpdateType, inputMessage.getBody());
  }

  @Override
  public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
    return false;
  }

  @Override
  public void write(Collection<EntityUpdate<T>> entityUpdates, Type type, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {

  }

  @Override
  public boolean canRead(Class<?> clazz, MediaType mediaType) {
    return false;
  }

  @Override
  public boolean canWrite(Class<?> clazz, MediaType mediaType) {
    return false;
  }

  @Override
  public List<MediaType> getSupportedMediaTypes() {
    return null;
  }

  @Override
  public Collection<EntityUpdate<T>> read(Class<? extends Collection<EntityUpdate<T>>> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    return null;
  }

  @Override
  public void write(Collection<EntityUpdate<T>> entityUpdates, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {

  }
}
