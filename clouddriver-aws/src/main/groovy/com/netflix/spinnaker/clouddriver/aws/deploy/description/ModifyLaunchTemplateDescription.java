/*
 * Copyright 2020 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.clouddriver.aws.deploy.description;

import com.netflix.spinnaker.clouddriver.aws.model.AmazonBlockDevice;
import com.netflix.spinnaker.clouddriver.aws.security.NetflixAmazonCredentials;
import com.netflix.spinnaker.clouddriver.security.resources.ServerGroupsNameable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.Data;

@Data
public class ModifyLaunchTemplateDescription extends AbstractAmazonCredentialsDescription
    implements ServerGroupsNameable {
  private String region;
  private String asgName;
  private String amiName;
  private String instanceType;
  private String subnetType;
  private String iamRole;
  private String keyPair;
  private Boolean associatePublicIpAddress;
  private String spotRice;
  private String ramdiskId;
  private Boolean instanceMonitoring;
  private Boolean ebsOptimized;
  private Boolean legacyUdf;
  private String base64UserData;
  private String defaultVersion;

  List<AmazonBlockDevice> blockDevices;
  List<String> securityGroups;
  private Boolean securityGroupsAppendOnly;

  /**
   * If false, the newly created server group will not pick up block device mapping customizations
   * from an ancestor group
   */
  boolean copySourceCustomBlockDeviceMappings = true;

  @Override
  public Collection<String> getServerGroupNames() {
    return Collections.singletonList(asgName);
  }

  public ModifyLaunchTemplateDescription withCredentials(NetflixAmazonCredentials credentials) {
    setCredentials(credentials);
    return this;
  }
}
