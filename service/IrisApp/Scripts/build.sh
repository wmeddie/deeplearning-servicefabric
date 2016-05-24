#!/usr/bin/env bash

set -euf -o pipefail

function InstallData
{
    local name=$1
    local packageName=$2

    mkdir -p "../ApplicationPackageRoot/$name/Data"

    cp -r "packages/$packageName/content/" "../ApplicationPackageRoot/$name/Data"
}

function BuildPlayApp
{
    local module=$1
    local name=$2
    local packageName=$3

    pushd "../../$module"

    ../../activator dist

    if [ -e "../IrisApp/ApplicationPackageRoot/$name/Code" ]
    then
        rm -r "../IrisApp/ApplicationPackageRoot/$name/Code"
    fi

    if [ -e "../IrisApp/ApplicationPackageRoot/$name/Data" ]
    then
        rm -r "../IrisApp/ApplicationPackageRoot/$name/Data"
    fi

    unzip "target/universal/$packageName.zip" \
        -d "../IrisApp/ApplicationPackageRoot/$name"

    mv "../IrisApp/ApplicationPackageRoot/$name/$packageName" \
        "../IrisApp/ApplicationPackageRoot/$name/Code"

    cp "../IrisApp/Scripts/play-init.bat" \
        "../IrisApp/ApplicationPackageRoot/$name/Code/bin/"

    popd
}

BuildPlayApp "iris-service" "IrisService" "iris-service-1.0-SNAPSHOT"

nuget install -OutputDirectory "packages"

InstallData "IrisService" "Example.IrisModel.1.0.0"
