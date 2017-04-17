package com.netflix.spinnaker.janitor;

public interface Janitor {

  /**
   * Mark cloud resources as cleanup candidates and remove the marks for resources
   * that no longer exist or should not be cleanup candidates anymore.
   */

  void mark();

  /**
   * Clean the resources up that are marked as cleanup candidates when appropriate.
   */

  void cleanup();
}
