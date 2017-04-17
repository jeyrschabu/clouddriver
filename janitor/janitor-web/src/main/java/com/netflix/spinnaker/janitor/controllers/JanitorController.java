package com.netflix.spinnaker.janitor.controllers;

import com.netflix.spinnaker.janitor.events.JanitorEventHandler;
import com.netflix.spinnaker.janitor.events.OptInEvent;
import com.netflix.spinnaker.janitor.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/janitor")
public class JanitorController {
  @Autowired
  private JanitorResourcesDAO janitorResourcesDAO;

  @Autowired
  private List<JanitorEventHandler> handlers;

  @RequestMapping(
    method = RequestMethod.GET
  )
  public List<Resource> resources(@RequestParam Map<String, String> params) {
    return params.isEmpty() ? janitorResourcesDAO.findAll() : janitorResourcesDAO.filter(params);
  }

  /**
   * @param name the resource name
   * @param region the region where the resource resides
   * @param cloudProvider not too convinced on the value of this field
   * @param account the resource account
   * @return the opted in resource
   */

  @RequestMapping(
    method = RequestMethod.PUT,
    value = "/{name}/{region}/{cloudProvider}/{account}/optin"
  )
  public Resource optIn(@PathVariable String name,
                        @PathVariable String region,
                        @PathVariable String cloudProvider,
                        @PathVariable String account) {
    Resource resource = janitorResourcesDAO.find(name, region);
    resource.setState(Resource.ResourceState.OPTED_IN);
    handlers.forEach(handler -> handler.handle(new OptInEvent(name, region, cloudProvider, account)));

    return janitorResourcesDAO.update(resource);
  }

  @RequestMapping(
    method = RequestMethod.PUT,
    value = "/{name}/{region}/{cloudProvider}/{account}/optout"
  )
  public Resource optOut(@PathVariable String name,
                         @PathVariable String region,
                         @PathVariable String cloudProvider,
                         @PathVariable String account) {

    Resource resource = janitorResourcesDAO.find(name, region);
    resource.setState(Resource.ResourceState.OPTED_OUT);
    handlers.forEach(event -> event.handle(new OptInEvent(name, region, cloudProvider, account)));

    return janitorResourcesDAO.update(resource);
  }
}
