#!/usr/bin/env bash

set -euf -o pipefail

function CleanPlayApp
{
    local name=$1

    rm -r "../ApplicationPackageRoot/$name/Code"
    rm -r "../ApplicationPackageRoot/$name/Data"

    mkdir -p "../ApplicationPackageRoot/$name/Code"
    mkdir -p "../ApplicationPackageRoot/$name/Data"

    touch "../ApplicationPackageRoot/$name/Code/.gitkeep"

    touch "../ApplicationPackageRoot/$name/Data/.gitkeep"
}

CleanPlayApp "IrisService"

rm -r packages
