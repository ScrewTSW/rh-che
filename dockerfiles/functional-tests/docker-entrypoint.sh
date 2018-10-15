#!/bin/bash
echo -n Running Xvfb...
nohup chromedriver &
nohup /usr/bin/Xvfb :99 -screen 0 1920x1080x24 +extension RANDR > /dev/null 2>&1 &

export TARGET="rhel"
export PR_CHECK_BUILD="true"

pwd
