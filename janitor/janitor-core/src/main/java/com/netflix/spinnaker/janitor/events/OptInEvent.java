package com.netflix.spinnaker.janitor.events;

import com.netflix.spinnaker.janitor.model.Resource;

public class OptInEvent implements JanitorEvent {
  public OptInEvent(String name, String region, String cloudProvider, String account) {
  }

  public OptInEvent(Resource resource) {
  }
}
