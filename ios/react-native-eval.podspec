Pod::Spec.new do |s|
  s.name           = "react-native-eval"
  s.version        = "0.2.0"
  s.summary        = "Call any JS functions from your native code"
  s.description    = "React has a good tutorial how to integrate React View to alrady existsing application, but it doesn't provide a good way if you decided to migrate some of your business logic to JS first while maintaining the same UI"
  s.homepage       = "https://github.com/artemyarulin/react-native-eval"
  s.license        = { :type => 'MIT', :file => '../LICENSE' }
  s.author         = { "Artem Yarulin" => "artem.yarulin@fessguid.com" }
  s.platform       = :ios, "7.0"
  s.source         = { :git => "https://github.com/artemyarulin/react-native-eval.git", :tag => s.version.to_s }
  s.source_files   = ["react-native-eval/RNMEvaluator.{h,m}"]
  s.public_header_files = "react-native-eval/RNMEvaluator.h"
  s.requires_arc  = true
  s.dependency "React"
end
