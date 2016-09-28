android_binary(
    name = 'my_app',
    manifest = "AndroidManifest.xml",
    custom_package = 'bazel.android',
    deps = [':my_aar'],
)

aar_import(
    name = 'my_aar',
    aar = 'support-v4-24.2.0.aar',
)
