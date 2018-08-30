const config = require('react-native/local-cli/core/config');
const PbxFile = require('xcode/lib/pbxFile');
const path = require('path');
const xcode = require('xcode');
const fs = require('fs');
const createGroupWithMessage = require('react-native/local-cli/link/ios/createGroupWithMessage');

function addNearITiOSResources() {
    const iOSconfig = config.getProjectConfig().ios;
    const pw_iOSconfig = config.getDependencyConfig('react-native-nearit').ios;

    const project = xcode.project(iOSconfig.pbxprojPath).parseSync();

    var targets = project.pbxNativeTargetSection();

    for (uuid in targets) {
        var libFiles = project.pbxFrameworksBuildPhaseObj(uuid).files;
        var filesCount = libFiles.length;
        for (var f = 0; f < filesCount; ++f) {
            var fileRef = project.pbxBuildFileSection()[libFiles[f].value].fileRef;
            var file = project.pbxFileReferenceSection()[fileRef];
            if (file != null) {
                if (file.path == 'libRNNearIt.a') {
                    createGroupWithMessage(project, 'Resources');
                    project.addResourceFile(path.relative(iOSconfig.sourceDir, path.join(pw_iOSconfig.sourceDir, 'NearITResources.bundle')), {'target' : uuid }, null);
                }
            }
        }
    }
    fs.writeFileSync(
        iOSconfig.pbxprojPath,
        project.writeSync()
      );
};

addNearITiOSResources();
