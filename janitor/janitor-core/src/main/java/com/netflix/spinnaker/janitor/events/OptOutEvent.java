package com.netflix.spinnaker.janitor.events;

public class OptOutEvent implements JanitorEvent {
  public OptOutEvent(String name, String region, String cloudProvider, String account) {
  }
}
