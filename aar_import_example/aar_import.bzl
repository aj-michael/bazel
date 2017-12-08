def aar_import_impl(ctx):
  resource_files_zip = ctx.outputs.resource_files_zip

  aar = ctx.attr.aar.files.to_list()[0]
  zipper = ctx.attr._zipper.files.to_list()[0]
  manifest = ctx.actions.declare_file(
    ctx.attr.name + "/_unzipped_aar/AndroidManifest.xml")
  ctx.actions.run_shell(
    inputs = [zipper, aar],
    outputs = [manifest],
    command = "{} x {} -d {} AndroidManifest.xml".format(
        zipper.path, aar.path, manifest.dirname),
  )

  aar_resources_extractor = ctx.attr._aar_resources_extractor
  resources_dir = ctx.actions.declare_directory(
    ctx.attr.name + "/_unzipped_aar/res")
  ctx.actions.run_shell(
    inputs = [aar] + aar_resources_extractor.files.to_list(),
    outputs = [resources_dir],
    command = "{} --input_aar={} --output_res_dir={}".format(
        aar_resources_extractor.files_to_run.executable.path,
        aar.path,
        resources_dir.path
    )
  )

  android_common.build_resource_apk(
    ctx,
    manifest = manifest,
    resources = [resources_dir], 
    output = resource_files_zip,
  )


aar_import_attrs = {
    "_aar_resources_extractor": attr.label(
        default=Label("@bazel_tools//tools/android:aar_resources_extractor"),
        cfg="host",
        executable=True),
    "_zipper": attr.label(default=Label("@bazel_tools//tools/zip:zipper"), single_file=True),
    "_android_sdk": attr.label(
        default=configuration_field(fragment = "android", name = "android_sdk_label")),
    "_android_resources_busybox": attr.label(
        default=Label("@bazel_tools//tools/android:busybox"),
        cfg="host",
        executable=True),
    "aar": attr.label(allow_files=FileType([".aar"])),
    "resources": attr.label_list(allow_files=FileType([""])),
}

aar_import = rule(
    aar_import_impl,
    attrs = aar_import_attrs,
    outputs = {
        "resource_files_zip": "%{name}_resource_files.zip",
    },
)
