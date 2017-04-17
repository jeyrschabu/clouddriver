package com.netflix.spinnaker.janitor.model;

import java.time.LocalDate;

public interface Resource {
  enum ResourceState {
    OPTED_OUT,
    OPTED_IN,
    MARKED,
    UNMARKED,
    JANITOR_TERMINATED,
    USER_TERMINATED
  }

  String getId();
  void setId(String id);

  void setState(ResourceState state);
  ResourceState getState();
  String getTerminationReason();
  LocalDate getNotificationTime();
  void setNotificationTime(LocalDate date);


  //TODO: The idea of a resource as an abstract representation of an <CloudProvider> specific object

}
