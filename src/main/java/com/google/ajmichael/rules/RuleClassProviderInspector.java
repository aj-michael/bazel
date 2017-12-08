package com.google.ajmichael.rules;

import com.google.devtools.build.lib.analysis.ConfiguredRuleClassProvider;
import com.google.devtools.build.lib.analysis.RuleDefinition;
import com.google.devtools.build.lib.bazel.rules.BazelRuleClassProvider;
import com.google.devtools.build.lib.packages.Attribute;
import com.google.devtools.build.lib.packages.RuleClass;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class RuleClassProviderInspector {

  private RuleClassProviderInspector() {}

  public static void main(String[] args) {
    ConfiguredRuleClassProvider ruleClassProvider = BazelRuleClassProvider.create();
    for (RuleClass ruleClass : ruleClassProvider.getRuleClassMap().values()) {
      RuleDefinition ruleDefinition = ruleClassProvider.getRuleClassDefinition(ruleClass.getName());
      if (!ruleDefinition.getClass().getPackage().toString().contains("android")) {
        continue;
      }
      if (ruleClass.getWorkspaceOnly()) {
        continue;
      }
      System.out.println(ruleClass.getName());
      List<Attribute> attributes = ruleClass.getAttributes();
      List<String> attributeNames =
          attributes.stream().map(Attribute::getName).collect(Collectors.toList());
      if (ruleClass.getName().equals("android_library")) {
        System.out.println(attributeNames);
      }
      //System.out.println(attributeNames);
    }
  }
}
