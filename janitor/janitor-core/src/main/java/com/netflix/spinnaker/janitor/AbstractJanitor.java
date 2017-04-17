package com.netflix.spinnaker.janitor;

import com.netflix.spinnaker.cats.agent.RunnableAgent;
import com.netflix.spinnaker.clouddriver.cache.CustomScheduledAgent;

public abstract class AbstractJanitor implements Janitor, RunnableAgent, CustomScheduledAgent {
  @Override
  public void mark() {

  }

  @Override
  public void cleanup() {

  }

  @Override
  public void run() {

  }
}
