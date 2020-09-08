/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.clouddriver.deploy;

import com.netflix.spinnaker.kork.artifacts.model.Artifact;
import java.util.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class DeploymentResult {
  private List<String> serverGroupNames = new ArrayList<>();
  private Map<String, String> serverGroupNameByRegion = new HashMap<>();
  private List<String> messages = new ArrayList<>();

  private List<String> deployedNames = new ArrayList<>();
  private Map<String, List<String>> deployedNamesByLocation = new HashMap<>();

  private List<Artifact> createdArtifacts = new ArrayList<>();
  private Set<Deployment> deployments = new HashSet<>();

  public DeploymentResult normalize() {
    if (deployments != null) {
      return this;
    }

    //    serverGroupNameByRegion.forEach(i -> {
    //      Deployment deployment = new Deployment();
    //      deployment.location = i.
    //      deployments.add(new Deployment())
    //      deployments.add(new Deployment(location: key, serverGroupName: value))
    //    });
    //
    //    deployedNamesByLocation.each { key, values ->
    //      values.each { value ->
    //        deployments.add(new Deployment(location: key, serverGroupName: value))
    //      }
    //    }

    return this;
  }

  @Data
  public static class Deployment {
    private String cloudProvider;
    private String account;
    private String location;
    private String serverGroupName;

    Capacity capacity;

    Map<String, Object> metadata = new HashMap<>();

    public boolean equals(Object o) {
      if (this == o) return true;
      if (getClass() != o.getClass()) return false;

      Deployment that = (Deployment) o;

      if (location != that.location) return false;
      if (serverGroupName != that.serverGroupName) return false;

      return true;
    }

    public int hashCode() {
      int result;
      result = (location != null ? location.hashCode() : 0);
      result = 31 * result + (serverGroupName != null ? serverGroupName.hashCode() : 0);
      return result;
    }

    @Override
    public String toString() {
      return "${location}:${serverGroupName}";
    }

    @Data
    public static class Capacity {
      private Integer min;
      private Integer max;
      private Integer desired;

      public boolean equals(Object o) {
        if (this.equals(o)) return true;
        if (getClass() != o.getClass()) return false;

        Capacity capacity = (Capacity) o;

        if (desired != capacity.desired) return false;
        if (max != capacity.max) return false;
        if (min != capacity.min) return false;

        return true;
      }

      public int hashCode() {
        int result;
        result = (min != null ? min.hashCode() : 0);
        result = 31 * result + (max != null ? max.hashCode() : 0);
        result = 31 * result + (desired != null ? desired.hashCode() : 0);
        return result;
      }
    }
  }
}
