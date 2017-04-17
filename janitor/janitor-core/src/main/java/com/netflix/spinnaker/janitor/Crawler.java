package com.netflix.spinnaker.janitor;

import com.netflix.spinnaker.janitor.model.Resource;

import java.util.List;

public interface Crawler {
  List<Resource> getResources();
}

