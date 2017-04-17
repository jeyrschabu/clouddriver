package com.netflix.spinnaker.janitor.controllers;

import java.util.List;
import java.util.Map;
import com.netflix.spinnaker.janitor.model.Resource;

public interface JanitorResourcesDAO {
  List<Resource> findAll();
  List<Resource> filter(Map<String, String> params);

  Resource find(String name, String region);

  Resource update(Resource resource);
}
