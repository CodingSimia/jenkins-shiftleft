# shiftleft-jenkins

## Introduction

Reusable Jenkins build step for Upwind Security CloudScanner that supports scanning for vulnerabilities
in Docker images during a build.

## Installation Instructions

Download the plugin from s3.  Then goto `Manage Jenkins` > `Plugins` > `Advanced Settings` and upload the
`shift-left-jenkins.hpi` file before clicking deploy.  Alternatively, install with the jenkins cli:
```commandline
jenkins-plugin-cli --plugin shift-left-jenkins.hpi
```

## Usage Instructions

To use this build step you will need to add your `Upwind Client ID` and `Upwind Client Secret` to the Jenkins
System Settings.

Include the following in your `Jenkinsfile`:
```groovy
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm  // This will populate the GIT_COMMIT and other Git-related environment variables
            }
        }
        stage('Build Image') {
            steps {
                sh 'docker build . -t=shift-left-test'
            }
        }
        stage('Shift Left Scan') {
            steps {
                shiftLeftScan(
                        dockerImage: 'shift-left-test',
                        commit: "${GIT_COMMIT}",
                        branch: "${GIT_BRANCH}",
                        repo: "${GIT_URL}",
                        upwindUri: 'upwind.dev'
                )
            }
        }
        stage('Publish Image') {
            steps {
                sh 'echo publish image here'
            }
        }
        stage('Shift Left Publish Event') {
            steps {
                shiftLeftEvent(
                        eventType: 'IMAGE_PUBLISH',
                        dockerImage: 'shift-left-test',
                        commit: "${GIT_COMMIT}",
                        branch: "${GIT_BRANCH}",
                        repo: "${GIT_URL}",
                        upwindUri: 'upwind.dev'
                )
            }
        }
        stage('Deploy Image') {
            steps {
                sh 'echo deploy image here'
            }
        }
        stage('Shift Left Deploy Event') {
            steps {
                shiftLeftEvent(
                        eventType: 'IMAGE_DEPLOY',
                        dockerImage: 'shift-left-test',
                        commit: "${GIT_COMMIT}",
                        branch: "${GIT_BRANCH}",
                        repo: "${GIT_URL}",
                        upwindUri: 'upwind.dev'
                )
            }
        }
    }
}
```

If everything works as expected, you should get something like this:
```commandline
[🏄] Shift Left scan starting...
     - Initiator:    anonymous (null)
     - Docker Image: shift-left-test
     - Commit:       ccd2e9659089f7b656ac2da59915e665d88de95b
     - Branch:       origin/AG-1626
     - Repo:         git@github.com:upwindsecurity/shiftleft-jenkins.git
[⏳] Downloading binary...
[✅] Downloading binary complete
[⏳] Scanning...
Setting up client 
Preparing Docker Image 
Sending build image event 
Scanning image 
Scan complete 
▒▒▒▒▒▒▒▒▒                           ▒▒▒                 ▒▒▒
                                                        ▒▒▒
▒▒▒   ▒▒▒ ▒▒▒▒▒▒▒▒▒ ▒▒▒   ▒▒▒   ▒▒▒ ▒▒▒ ▒▒▒▒▒▒▒▒▒ ▒▒▒▒▒▒▒▒▒
▒▒▒   ▒▒▒ ▒▒▒   ▒▒▒ ▒▒▒   ▒▒▒   ▒▒▒ ▒▒▒ ▒▒▒   ▒▒▒ ▒▒▒   ▒▒▒
▒▒▒▒▒▒▒▒▒ ▒▒▒▒▒▒▒▒▒ ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒ ▒▒▒ ▒▒▒   ▒▒▒ ▒▒▒▒▒▒▒▒▒
          ▒▒▒
          ▒▒▒  Cloud Runtime Security Platform


Upwind has detected 45 vulnerabilities in image shift-left-test

2 Critical  
13 High  
19 Medium  
8 Low  
3 Unclassified  

+--------------------------------+--------------+------+-------------------------+------------------+------------------+------------------+
| Package Name                   | Severity     | CVSS | Package Version         | Package Type     | Fixed In Version | CVE ID           |
+--------------------------------+--------------+------+-------------------------+------------------+------------------+------------------+
| stdlib                         | CRITICAL     | 9.8  | go1.21.8                | GoModulePkg      | 1.21.11          | CVE-2024-24790   |
| openssl                        | CRITICAL     | 9.1  | 3.0.14-1~deb12u2        | DebPkg           | 3.0.15-1~deb12u1 | CVE-2024-5535    |
| perl                           | HIGH         | 8.1  | 5.36.0-7+deb12u1        | DebPkg           |                  | CVE-2023-31486   |
| perl                           | HIGH         | 8.1  | 5.36.0-7+deb12u1        | DebPkg           |                  | CVE-2023-31484   |
| xdg-user-dirs                  | HIGH         | 7.8  | 0.18-1                  | DebPkg           |                  | CVE-2017-15131   |
| libxml2                        | HIGH         | 7.5  | 2.9.14+dfsg-1.3~deb12u1 | DebPkg           |                  | CVE-2024-34459   |
| git                            | HIGH         | 7.5  | 1:2.39.5-0+deb12u1      | DebPkg           |                  | CVE-2022-24975   |
| stdlib                         | HIGH         | 7.5  | go1.21.8                | GoModulePkg      | 1.22.7           | CVE-2024-34158   |
| libxml2                        | HIGH         | 7.5  | 2.9.14+dfsg-1.3~deb12u1 | DebPkg           |                  | CVE-2024-25062   |
| xstream                        | HIGH         | 7.5  | 1.4.20                  | JavaPkg          | 1.4.21           | CVE-2024-47072   |
| perl                           | HIGH         | 7.5  | 5.36.0-7+deb12u1        | DebPkg           |                  | CVE-2011-4116    |
| stdlib                         | HIGH         | 7.5  | go1.21.8                | GoModulePkg      | 1.21.9           | CVE-2023-45288   |
| stdlib                         | HIGH         | 7.5  | go1.21.8                | GoModulePkg      | 1.21.12          | CVE-2024-24791   |
| libgcrypt20                    | HIGH         | 7.5  | 1.10.1-3                | DebPkg           |                  | CVE-2018-6829    |
| stdlib                         | HIGH         | 7.5  | go1.21.8                | GoModulePkg      | 1.22.7           | CVE-2024-34156   |
| libxml2                        | MEDIUM       | 6.5  | 2.9.14+dfsg-1.3~deb12u1 | DebPkg           |                  | CVE-2023-45322   |
| libxml2                        | MEDIUM       | 6.5  | 2.9.14+dfsg-1.3~deb12u1 | DebPkg           |                  | CVE-2023-39615   |
| coreutils                      | MEDIUM       | 6.5  | 9.1-1                   | DebPkg           |                  | CVE-2016-2781    |
| curl                           | MEDIUM       | 6.5  | 7.88.1-10+deb12u8       | DebPkg           |                  | CVE-2024-9681    |
| stdlib                         | MEDIUM       | 6.4  | go1.21.8                | GoModulePkg      | 1.21.10          | CVE-2024-24787   |
| curl                           | MEDIUM       | 6.3  | 7.88.1-10+deb12u8       | DebPkg           |                  | CVE-2024-2379    |
| libgcrypt20                    | MEDIUM       | 5.9  | 1.10.1-3                | DebPkg           |                  | CVE-2024-2236    |
| guava                          | MEDIUM       | 5.5  | 31.1-jre                | JavaPkg          | 32.0.0-android   | CVE-2023-2976    |
| stdlib                         | MEDIUM       | 5.5  | go1.21.8                | GoModulePkg      | 1.21.11          | CVE-2024-24789   |
| util-linux                     | MEDIUM       | 5.5  | 2.38.1-5+deb12u2        | DebPkg           |                  | CVE-2022-0563    |
| systemd                        | MEDIUM       | 5.3  | 252.31-1~deb12u1        | DebPkg           |                  | CVE-2023-31439   |
| systemd                        | MEDIUM       | 5.3  | 252.31-1~deb12u1        | DebPkg           |                  | CVE-2023-31438   |
| systemd                        | MEDIUM       | 5.3  | 252.31-1~deb12u1        | DebPkg           |                  | CVE-2023-31437   |
| git                            | MEDIUM       | 5.0  | 1:2.39.5-0+deb12u1      | DebPkg           |                  | CVE-2018-1000021 |
| coreutils                      | MEDIUM       | 4.7  | 9.1-1                   | DebPkg           |                  | CVE-2017-18018   |
| commons-io                     | MEDIUM       | 4.3  | 2.11.0                  | JavaPkg          | 2.14.0           | CVE-2024-47554   |
| openssl                        | MEDIUM       | 4.3  | 3.0.14-1~deb12u2        | DebPkg           | 3.0.15-1~deb12u1 | CVE-2024-9143    |
| script-security                | MEDIUM       | 4.3  | 1362.v67dc1f0e1b_b_3    | JenkinsPluginPkg | 1368.vb          | CVE-2024-52549   |
| stdlib                         | MEDIUM       | 4.3  | go1.21.8                | GoModulePkg      | 1.22.7           | CVE-2024-34155   |
| apparmor                       | LOW          | 3.9  | 3.0.8-3                 | DebPkg           |                  | CVE-2016-1585    |
| apt                            | LOW          | 3.7  | 2.6.1                   | DebPkg           |                  | CVE-2011-3374    |
| github.com/opencontainers/runc | LOW          | 3.6  | v1.1.14-0-g2c9f560      | GoModulePkg      | 1.1.14           | CVE-2024-45310   |
| guava                          | LOW          | 3.3  | 31.1-jre                | JavaPkg          | 32.0.0-android   | CVE-2020-8908    |
| unzip                          | LOW          | 3.3  | 6.0-28                  | DebPkg           |                  | CVE-2021-4217    |
| gnupg2                         | LOW          | 3.3  | 2.2.40-1.1              | DebPkg           |                  | CVE-2022-3219    |
| github.com/golang-jwt/jwt/v4   | LOW          | 3.1  | v4.5.0                  | GoModulePkg      | 4.5.1            | CVE-2024-51744   |
| procps                         | LOW          | 2.5  | 2:4.0.2-3               | DebPkg           |                  | CVE-2023-4016    |
| systemd                        | UNCLASSIFIED |      | 252.31-1~deb12u1        | DebPkg           |                  | CVE-2013-4392    |
| tar                            | UNCLASSIFIED |      | 1.34+dfsg-1.2+deb12u1   | DebPkg           |                  | CVE-2005-2541    |
| iptables                       | UNCLASSIFIED |      | 1.8.9-2                 | DebPkg           |                  | CVE-2012-2663    |
+--------------------------------+--------------+------+-------------------------+------------------+------------------+------------------+
[✅] Scanning complete
[Pipeline] }
[Pipeline] // stage
[Pipeline] stage
[Pipeline] { (Publish Image)
[Pipeline] sh
+ echo publish image here
publish image here
[Pipeline] }
[Pipeline] // stage
[Pipeline] stage
[Pipeline] { (Shift Left Publish Event)
[Pipeline] shiftLeftEvent
[🏄] Shift Left sending event...
     - Initiator:    anonymous (null)
     - Event Type:   IMAGE_PUBLISH
     - Docker Image: shift-left-test
     - Commit:       ccd2e9659089f7b656ac2da59915e665d88de95b
     - Branch:       origin/AG-1626
     - Repo:         git@github.com:upwindsecurity/shiftleft-jenkins.git
[⏳] Downloading binary...
[✅] Downloading binary complete
[⏳] Sending Event...
[✅] Sending Event complete
[Pipeline] }
[Pipeline] // stage
[Pipeline] stage
[Pipeline] { (Deploy Image)
[Pipeline] sh
+ echo deploy image here
deploy image here
[Pipeline] }
[Pipeline] // stage
[Pipeline] stage
[Pipeline] { (Shift Left Deploy Event)
[Pipeline] shiftLeftEvent
[🏄] Shift Left sending event...
     - Initiator:    anonymous (null)
     - Event Type:   IMAGE_DEPLOY
     - Docker Image: shift-left-test
     - Commit:       ccd2e9659089f7b656ac2da59915e665d88de95b
     - Branch:       origin/AG-1626
     - Repo:         git@github.com:upwindsecurity/shiftleft-jenkins.git
[⏳] Downloading binary...
[✅] Downloading binary complete
[⏳] Sending Event...
[✅] Sending Event complete
[Pipeline] }
[Pipeline] // stage
[Pipeline] }
[Pipeline] // withEnv
[Pipeline] }
[Pipeline] // node
[Pipeline] End of Pipeline
Finished: SUCCESS
```
## Troubleshooting

### Report not formatted correctly

The scan results contain ansi colour codes.  You can install the `ansicolor` plugin to see the report formatted
properly.

## Developer Setup

Requirements
* Java 17
* Maven
* Docker

You can build and run the shift-left plugin in a local docker container by running:
```commandline
docker compose up --build
```

```commandline
Jenkins IU        -> http://localhost:8080
Remote debugging  -> localhost:5005
```