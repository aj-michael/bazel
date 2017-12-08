// Copyright 2016 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.rules.android;

import com.google.devtools.build.lib.actions.Artifact;
import com.google.devtools.build.lib.analysis.FileProvider;
import com.google.devtools.build.lib.analysis.RuleContext;
import com.google.devtools.build.lib.analysis.config.BuildOptions;
import com.google.devtools.build.lib.analysis.skylark.SkylarkRuleContext;
import com.google.devtools.build.lib.collect.nestedset.NestedSetBuilder;
import com.google.devtools.build.lib.collect.nestedset.Order;
import com.google.devtools.build.lib.packages.Attribute.SplitTransition;
import com.google.devtools.build.lib.packages.RuleClass;
import com.google.devtools.build.lib.rules.java.JavaCommon;
import com.google.devtools.build.lib.skylarkinterface.Param;
import com.google.devtools.build.lib.skylarkinterface.SkylarkCallable;
import com.google.devtools.build.lib.skylarkinterface.SkylarkModule;
import com.google.devtools.build.lib.syntax.SkylarkList;
import com.google.devtools.build.lib.vfs.PathFragment;

/**
 * Common utilities for Skylark rules related to Android.
 */
@SkylarkModule(
  name = "android_common",
  doc = "Common utilities and fucntionality related to Android rules."
)
public class AndroidSkylarkCommon {

  @SkylarkCallable(
    name = "resource_source_directory",
    allowReturnNones = true,
    doc =
        "Returns a source directory for Android resource file. "
            + "The source directory is a prefix of resource's relative path up to "
            + "a directory that designates resource kind (cf. "
            + "http://developer.android.com/guide/topics/resources/providing-resources.html)."
  )
  public PathFragment getSourceDirectoryRelativePathFromResource(Artifact resource) {
    return AndroidCommon.getSourceDirectoryRelativePathFromResource(resource);
  }

  @SkylarkCallable(
      name = "multi_cpu_configuration",
      doc = "A configuration for rule attributes that compiles native code according to "
          + "the --fat_apk_cpu and --android_crosstool_top flags.",
      structField = true
  )
  public SplitTransition<BuildOptions> getAndroidSplitTransition() {
    return AndroidRuleClasses.ANDROID_SPLIT_TRANSITION;
  }

  @SkylarkCallable(
      name = "build_resource_apk",
      doc = "tbd",
      mandatoryPositionals = 1,
      parameters = {
          @Param(
              name = "manifest",
              positional = false,
              named = true,
              type = Artifact.class,
              doc = "The Android manifest"
          ),
          @Param(
              name = "resources",
              positional = false,
              named = true,
              type = SkylarkList.class,
              generic1 = Artifact.class,
              defaultValue = "[]",
              doc = "The resources to compile"
          ),
          @Param(
              name = "output",
              positional = false,
              named = true,
              type = Artifact.class
          )
      }
  )
  public AndroidResourcesProvider buildResourceApk(
      SkylarkRuleContext skylarkRuleContext,
      Artifact androidManifest,
      SkylarkList<Artifact> resources,
      Artifact outputResourceApk)
      throws RuleClass.ConfiguredTargetFactory.RuleErrorException, InterruptedException {
    RuleContext ruleContext = skylarkRuleContext.getRuleContext();
    ApplicationManifest applicationManifest =
        ApplicationManifest.fromExplicitManifest(ruleContext, androidManifest);
    FileProvider resourcesProvider = new FileProvider(
        new NestedSetBuilder<Artifact>(Order.NAIVE_LINK_ORDER).addAll(resources).build());

    ResourceApk resourceApk =
        applicationManifest.packAarWithDataAndResources(
            skylarkRuleContext.getRuleContext(),
            LocalResourceContainer.forResourceFileProvider(
                ruleContext, resourcesProvider, "resources"),
            ResourceDependencies.fromRuleDeps(ruleContext, JavaCommon.isNeverLink(ruleContext)),
            ruleContext.getImplicitOutputArtifact(AndroidRuleClasses.ANDROID_R_TXT),
            ruleContext.getImplicitOutputArtifact(AndroidRuleClasses.ANDROID_LOCAL_SYMBOLS),
            ruleContext.getImplicitOutputArtifact(AndroidRuleClasses.ANDROID_PROCESSED_MANIFEST),
            outputResourceApk);
    return resourceApk.toResourceProvider(ruleContext.getLabel(), false);
  }
}
