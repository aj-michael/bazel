load(":aar_import.bzl", "aar_import")
aar_import(
    name = "foo",
    aar = "foo.aar",
)
android_binary(
    name = "bin",
    manifest = "AndroidManifest.xml",
    custom_package = "com.foo",
    srcs = ["Bin.java"],
    deps = [":foo"],
)
