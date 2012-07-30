GroupBuilder
=====

Group building block for Blackboard Learn 9

Tested on Blackboard Learn 9 SP 8

To compile the project:
* Change the bbsdk directory in build.properties to point to your bbsdk
* Run "ant install-ivy" to install Apache Ivy (dependency management). Ivy will be installed in to ${user.home}/.ant/. If Ivy is already installed, skip this step.
* Run "ant"
* (Optional, for developer) auto deploy the war file by running "ant deploy"
