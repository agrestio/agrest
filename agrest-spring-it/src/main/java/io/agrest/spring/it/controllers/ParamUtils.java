package io.agrest.spring.it.controllers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ParamUtils {
  public static Map<String, List<String>> convertParams(Map<String,String> allParams) {
    return allParams.entrySet()
        .stream()
        .collect(Collectors.toMap(Entry::getKey, e -> List.of(e.getValue())));
  }
}
