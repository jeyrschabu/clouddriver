package com.netflix.spinnaker.janitor.events;

public interface JanitorEventHandler {
  void handle(OptInEvent optInEvent);
}
