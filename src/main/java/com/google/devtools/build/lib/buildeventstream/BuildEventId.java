// Copyright 2016 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.buildeventstream;

import com.google.protobuf.TextFormat;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

/**
 * Class of identifiers for publically posted events.
 *
 * <p>Since event identifiers need to be created before the actual event, the event IDs are highly
 * structured so that equal identifiers can easily be generated. The main way of pregenerating event
 * identifiers that do not accidentally coincide is by providing a target or a target pattern;
 * therefore, those (if provided) are made specially visible.
 */
@Immutable
public final class BuildEventId implements Serializable {
  private final BuildEventStreamProtos.BuildEventId protoid;

  private BuildEventId(BuildEventStreamProtos.BuildEventId protoid) {
    this.protoid = protoid;
  }

  @Override
  public int hashCode() {
    return Objects.hash(protoid);
  }

  @Override
  public boolean equals(Object other) {
    if (other == null || !other.getClass().equals(getClass())) {
      return false;
    }
    BuildEventId that = (BuildEventId) other;
    return Objects.equals(this.protoid, that.protoid);
  }

  @Override
  public String toString() {
    return "BuildEventId {" + TextFormat.printToString(protoid) + "}";
  }

  public BuildEventStreamProtos.BuildEventId asStreamProto() {
    return protoid;
  }

  public static BuildEventId unknownBuildEventId(String details) {
    BuildEventStreamProtos.BuildEventId.UnknownBuildEventId id =
        BuildEventStreamProtos.BuildEventId.UnknownBuildEventId.newBuilder()
            .setDetails(details)
            .build();
    return new BuildEventId(
        BuildEventStreamProtos.BuildEventId.newBuilder().setUnknown(id).build());
  }

  public static BuildEventId progressId(int count) {
    BuildEventStreamProtos.BuildEventId.ProgressId id =
        BuildEventStreamProtos.BuildEventId.ProgressId.newBuilder().setOpaqueCount(count).build();
    return new BuildEventId(
        BuildEventStreamProtos.BuildEventId.newBuilder().setProgress(id).build());
  }
}
