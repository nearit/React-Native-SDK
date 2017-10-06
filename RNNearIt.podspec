require "json"
pkg = JSON.parse(File.read("package.json"))

Pod::Spec.new do |s|
  s.name         = "RNNearIt"
  s.version      = pkg['version']
  s.description  = pkg['description']
  s.homepage     = pkg['homepage']
  s.summary      = pkg['description']
  s.license      = pkg['license']
  s.author       = { "Mattia Panzeri" => "mattia.panzeri93@gmail.com" }
  s.ios.deployment_target = "9.0"
  s.source       = { git: pkg['repository']['url'], tag: s.version.to_s }
  s.source_files = "ios/**/*.{h,m}"
  s.requires_arc = true

  s.dependency "React"
  s.dependency "NearITSDK", "2.2.4"
end