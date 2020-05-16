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

package com.netflix.spinnaker.clouddriver.aws.deploy.ops;

import static io.vavr.API.TODO;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.LaunchTemplateSpecification;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateLaunchTemplateVersionRequest;
import com.amazonaws.services.ec2.model.DescribeLaunchTemplateVersionsRequest;
import com.amazonaws.services.ec2.model.DescribeLaunchTemplateVersionsResult;
import com.amazonaws.services.ec2.model.LaunchTemplateInstanceMarketOptionsRequest;
import com.amazonaws.services.ec2.model.LaunchTemplateSpotMarketOptionsRequest;
import com.amazonaws.services.ec2.model.LaunchTemplateVersion;
import com.amazonaws.services.ec2.model.LaunchTemplatesMonitoringRequest;
import com.amazonaws.services.ec2.model.RequestLaunchTemplateData;
import com.netflix.spinnaker.clouddriver.aws.deploy.BlockDeviceConfig;
import com.netflix.spinnaker.clouddriver.aws.deploy.ResolvedAmiResult;
import com.netflix.spinnaker.clouddriver.aws.deploy.description.ModifyLaunchTemplateDescription;
import com.netflix.spinnaker.clouddriver.aws.services.RegionScopedProviderFactory;
import com.netflix.spinnaker.clouddriver.aws.services.RegionScopedProviderFactory.RegionScopedProvider;
import com.netflix.spinnaker.clouddriver.data.task.Task;
import com.netflix.spinnaker.clouddriver.data.task.TaskRepository;
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperation;
import com.netflix.spinnaker.config.AwsConfiguration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;

public class ModifyLaunchTemplateAtomicOperation implements AtomicOperation<Void> {
  private static final String BASE_PHASE = "MODIFY_LAUNCH_TEMPLATE";

  @Autowired private RegionScopedProviderFactory regionScopedProviderFactory;

  @Autowired private AwsConfiguration.DeployDefaults deployDefaults;

  @Autowired private BlockDeviceConfig blockDeviceConfig;

  private ModifyLaunchTemplateDescription description;

  public ModifyLaunchTemplateAtomicOperation(ModifyLaunchTemplateDescription description) {
    this.description = description;
  }

  @Override
  public Void operate(List priorOutputs) {
    final String region = description.getRegion();
    final String amiName = description.getAmiName();
    final String amiId = description.getAmiId();
    final String autoScalingGroupName = description.getAsgName();
    final List<String> securityGroups = description.getSeccurityGroups();

    getTask().updateStatus(BASE_PHASE, "Initializing operation...");
    final RegionScopedProvider regionScopedProvider =
        regionScopedProviderFactory.forRegion(description.getCredentials(), region);
    final AmazonEC2 ec2 = regionScopedProvider.getAmazonEC2();

    final AutoScalingGroup autoScalingGroup =
        regionScopedProvider.getAsgService().getAutoScalingGroup(autoScalingGroupName);

    final LaunchTemplateSpecification launchTemplateSpec = autoScalingGroup.getLaunchTemplate();
    if (launchTemplateSpec == null) {
      throw new IllegalArgumentException(
          String.format(
              "Server group %s doesn't have a launch template", description.getServerGroupNames()));
    }

    final DescribeLaunchTemplateVersionsResult versionsResult =
        ec2.describeLaunchTemplateVersions(
            new DescribeLaunchTemplateVersionsRequest()
                .withLaunchTemplateId(launchTemplateSpec.getLaunchTemplateId())
                .withVersions(launchTemplateSpec.getVersion()));

    final LaunchTemplateVersion currentVersion = versionsResult.getLaunchTemplateVersions().get(0);
    final RequestLaunchTemplateData launchTemplateData = new RequestLaunchTemplateData();

    if (amiName != null && !currentVersion.getLaunchTemplateData().getImageId().equals(amiId)) {
      ResolvedAmiResult ami = getAmi(ec2, priorOutputs, region, amiName, amiId);
      launchTemplateData.setImageId(ami.getAmiId());
    }

    if (description.isSecurityGroupsAppendOnly()) {
      Set<String> allSecurityGroups = new HashSet<>(securityGroups);
      allSecurityGroups.addAll(currentVersion.getLaunchTemplateData().getSecurityGroupIds());
      launchTemplateData.setSecurityGroupIds(allSecurityGroups);
    }

    // set instance market options (TODO: check if the options have changed)
    launchTemplateData.setInstanceMarketOptions(
        getInstanceMarketOptions(launchTemplateData, description));

    final LaunchTemplatesMonitoringRequest monitoring = launchTemplateData.getMonitoring().clone();
    if (description.getInstanceMonitoring()) {}

    launchTemplateData.setMonitoring(new LaunchTemplatesMonitoringRequest());

    ec2.createLaunchTemplateVersion(
        new CreateLaunchTemplateVersionRequest()
            .withSourceVersion(launchTemplateSpec.getVersion())
            .withLaunchTemplateData(launchTemplateData)
            .withLaunchTemplateId(launchTemplateSpec.getLaunchTemplateId()));

    // TODO: handle block devices

    // TODO: handle legacy udf

    // if the spec always says latest, then maybe we dont need to update the server group.
    // create a new version of the launch template and update the launch template latest version

    return null;
  }

  private LaunchTemplateInstanceMarketOptionsRequest getInstanceMarketOptions(
      RequestLaunchTemplateData launchTemplateData, ModifyLaunchTemplateDescription description) {
    LaunchTemplateInstanceMarketOptionsRequest imr =
        launchTemplateData.getInstanceMarketOptions().clone();
    LaunchTemplateSpotMarketOptionsRequest spotOptions = imr.getSpotOptions();

    // TODO: allow to pass more spot price options
    return new LaunchTemplateInstanceMarketOptionsRequest()
        .withSpotOptions(
            new LaunchTemplateSpotMarketOptionsRequest()
                .withMaxPrice(description.getSpotPrice())
                .withSpotInstanceType(spotOptions.getSpotInstanceType()))
        .withMarketType(imr.getMarketType());
  }

  private ResolvedAmiResult getAmi(
      AmazonEC2 ec2, List priorOutputs, String region, String amiName, String amiId) {
    TODO("Implement me");
    return (ResolvedAmiResult)
        priorOutputs.stream()
            .filter(
                i ->
                    i instanceof ResolvedAmiResult
                        && ((ResolvedAmiResult) i).getRegion().equals(region)
                        && (((ResolvedAmiResult) i).getAmiName().equals(amiName)));
  }

  private static Task getTask() {
    return TaskRepository.threadLocalTask.get();
  }
}
