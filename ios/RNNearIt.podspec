require 'json'
package = JSON.parse(File.read('../package.json'))

Pod::Spec.new do |s|
  s.name         = "RNNearIt"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  The official NearIT SDK plugin for React Native (Obj-C Binding)
                   DESC
  s.homepage     = "https://github.com/panz3r/react-native-nearit-sdk#readme"
  s.license      = package["license"]
  s.author       = { "Mattia Panzeri" => "mattia.panzeri93@gmail.com" }
  s.platform     = :ios, "9.0"
  s.source       = { :git => "https://github.com/panz3r/react-native-nearit-sdk", :tag => "master" }
  s.source_files = "react-native-nearit-sdk/**/*.{h,m}"
  s.requires_arc = true

  s.dependency "React"
  s.dependency "NearITSDK"

end

