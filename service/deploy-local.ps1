Connect-ServiceFabricCluster 127.0.0.1:19000

$RegKey = "HKLM:\SOFTWARE\Microsoft\Service Fabric SDK"
$ModuleFolderPath = (Get-ItemProperty -Path $RegKey -Name FabricSDKPSModulePath).FabricSDKPSModulePath

Import-Module "$ModuleFolderPath\ServiceFabricSDK.psm1"

Publish-NewServiceFabricApplication `
    -ApplicationPackagePath .\IrisApp\ApplicationPackageRoot `
    -ApplicationParameterFilePath .\IrisApp\ApplicationParameters\Local.xml `
    -Action RegisterAndCreate `
    -OverwriteBehavior Always `
    -ErrorAction Stop
