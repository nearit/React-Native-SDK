require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name                  = "RNNearIt"
  s.version               = package['version']
  s.description           = package['description']
  s.summary               = 'nearit.com React Native SDK'

  s.homepage              = package['repository']['url']
  s.license               = package['license']
  s.author                = {
      'Boschini Federico' => 'federico@nearit.com',
      'Mattia Panzeri'    => 'mattia.panzeri93@gmail.com'
  }
  s.source                = { :git => package['repository']['url'], :tag => "#{s.version}" }
  s.source_files          = '**/*.{h,m}'

  s.ios.deployment_target = '9.0'

  s.dependency            'React'
  s.dependency            'NearITSDK', '~> 2.12.0'
  s.dependency            'NearITSDKSwift', '~> 2.12.0'
  s.dependency            'NearUIBinding', '~> 2.12.0'
end