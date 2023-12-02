package com.example.messagingrabbitmq.utilities;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class DummyRestaurants {

  public static final Map<Integer, String> entries = new HashMap<>();

  static {
    entries.put(1, "rest_1");
    entries.put(2, "rest_2");
    entries.put(3, "rest_3");
    entries.put(4, "rest_4");
    entries.put(5, "rest_5");
    entries.put(6, "rest_6");
    entries.put(7, "rest_7");
    entries.put(8, "rest_8");
  }

  private DummyRestaurants() {
    throw new IllegalStateException("Utility class");
  }
}
