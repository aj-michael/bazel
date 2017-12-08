def _extract_manifest_from_aar(ctx, aar_file):
  manifest = ctx.actions.declare_file(
    ctx.attr.name + "_files/AndroidManifest.xml")
  ctx.actions.run(
    inputs = [aar_file],
    outputs = [manifest],
    executable = ctx.executable._zipper,
    arguments = ["x", aar_file.path, "-d", manifest.dirname,
                 "AndroidManifest.xml"],
  )
  return manifest

def _extract_resources_from_aar(ctx, aar_file):
  resources_dir = ctx.actions.declare_directory(
    ctx.attr.name + "_files/res")
  ctx.actions.run(
    inputs = [aar_file],
    outputs = [resources_dir],
    executable = ctx.executable._aar_resources_extractor,
    arguments = [
        "--input_aar",
        aar_file.path,
        "--output_res_dir",
        resources_dir.path
    ],
  )
  return resources_dir

def _aar_import_impl(ctx):
  resource_files_zip = ctx.outputs.resource_files_zip
  aar = ctx.file.aar
  manifest = _extract_manifest_from_aar(ctx, aar)
  resources_dir = _extract_resources_from_aar(ctx, aar)
  resources_provider = android_common.build_resource_apk(
    ctx,
    manifest = manifest,
    resources = [resources_dir], 
    output = resource_files_zip,
  )
  return [resources_provider]

_aar_import_attrs = {
    "_aar_resources_extractor": attr.label(
        default=Label("@bazel_tools//tools/android:aar_resources_extractor"),
        cfg="host",
        executable=True),
    "_zipper": attr.label(
        default=Label("@bazel_tools//tools/zip:zipper"),
        cfg="host",
        executable=True),
    "_android_sdk": attr.label(
        default=configuration_field(fragment = "android", name = "android_sdk_label")),
    "_android_resources_busybox": attr.label(
        default=Label("@bazel_tools//tools/android:busybox"),
        cfg="host",
        executable=True),
    "aar": attr.label(allow_single_file=FileType([".aar"])),
}

aar_import = rule(
    _aar_import_impl,
    attrs = _aar_import_attrs,
    outputs = {
        "resource_files_zip": "%{name}_files/resource_files.zip",
    },
)
