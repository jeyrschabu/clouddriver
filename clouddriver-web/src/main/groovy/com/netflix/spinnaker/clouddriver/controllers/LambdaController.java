package com.netflix.spinnaker.clouddriver.controllers;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.ListFunctionsRequest;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.netflix.spinnaker.clouddriver.aws.security.AmazonClientProvider;
import com.netflix.spinnaker.clouddriver.aws.security.NetflixAmazonCredentials;
import com.netflix.spinnaker.clouddriver.security.AccountCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/lambdas")
public class LambdaController {

  @Autowired
  private AmazonClientProvider amazonClientProvider;

  @Autowired
  private AccountCredentialsProvider accountCredentialsProvider;

  @RequestMapping(value = "/{region}/{account}", method = RequestMethod.GET)
  public List<FunctionConfiguration> listFunctions(String account, String region) {
    NetflixAmazonCredentials credentials = (NetflixAmazonCredentials) accountCredentialsProvider.getCredentials(account);
    AWSLambda lambda = amazonClientProvider.getAmazonLambda(credentials, region);

    List<FunctionConfiguration> allFunctions = new ArrayList<>();

    String next = null;
    do {
      ListFunctionsResult result = lambda.listFunctions(new ListFunctionsRequest().withMarker(next));
      List<FunctionConfiguration> functions = result.getFunctions();

      if (functions != null) {
        for (FunctionConfiguration function : functions) {
          allFunctions.add(function);
        }
      }

      next = result.getNextMarker();

    } while (next != null);

    return allFunctions;
  }
}
