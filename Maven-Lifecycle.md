# Maven-Lifecycle

## 基础知识

| name | description |
| - | - |
| validate | validate the project is correct and all necessary information is available. |
| generate-sources | generate any source code for inclusion in compilation. |
| process-sources | process the source code, for example to filter any values. |
| generate-resources | generate resources for inclusion in the package. |
| process-resources | copy and process the resources into the destination directory, ready for packaging. |
| compile | compile the source code of the project. |
| process-classes | post-process the generated files from compilation, for example to do bytecode enhancement on Java classes. |
| generate-test-sources | generate any test source code for inclusion in compilation. |
| process-test-sources | process the test source code, for example to filter any values. |
| generate-test-resources | create resources for testing. |
| process-test-resources | copy and process the resources into the test destination directory. |
| test-compile | compile the test source code into the test destination directory. |
| test | run tests using a suitable unit testing framework. These tests should not require the code be packaged or deployed. |
| prepare-package | perform any operations necessary to prepare a package before the actual packaging. This often results in an unpacked, processed version of the package. (Maven 2.1 and above) |
| package | take the compiled code and package it in its distributable format, such as a JAR. |
| pre-integration-test | perform actions required before integration tests are executed. This may involve things such as setting up the required environment. |
| integration-test | process and deploy the package if necessary into an environment where integration tests can be run. |
| post-integration-test | perform actions required after integration tests have been executed. This may including cleaning up the environment. |
| verify | run any checks to verify the package is valid and meets quality criteria. |
| install | install the package into the local repository, for use as a dependency in other projects locally. |
| deploy | done in an integration or release environment, copies the final package to the remote repository for sharing with other developers and projects. |

## package、install、deploy 的联系与区别

### 1 package

```bash
[INFO] Scanning for projects...
[INFO] 
[INFO] ----------------------< com.example:demo-service >----------------------
[INFO] Building demo-service 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-resources-plugin:3.2.0:resources (default-resources) @ demo-service ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Using 'UTF-8' encoding to copy filtered properties files.
[INFO] Copying 0 resource
[INFO] Copying 0 resource
[INFO] 
[INFO] --- maven-compiler-plugin:3.8.1:compile (default-compile) @ demo-service ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-resources-plugin:3.2.0:testResources (default-testResources) @ demo-service ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Using 'UTF-8' encoding to copy filtered properties files.
[INFO] skip non existing resourceDirectory D:\demo\demo-service\src\test\resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.8.1:testCompile (default-testCompile) @ demo-service ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-surefire-plugin:2.22.2:test (default-test) @ demo-service ---
[INFO] 
[INFO] --- maven-jar-plugin:3.2.0:jar (default-jar) @ demo-service ---
[INFO] 
[INFO] --- spring-boot-maven-plugin:2.5.1:repackage (repackage) @ demo-service ---
[INFO] Replacing main artifact with repackaged archive
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

### 2 install

```bash
[INFO] Scanning for projects...
[INFO] 
[INFO] ----------------------< com.example:demo-service >----------------------
[INFO] Building demo-service 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-resources-plugin:3.2.0:resources (default-resources) @ demo-service ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Using 'UTF-8' encoding to copy filtered properties files.
[INFO] Copying 0 resource
[INFO] Copying 0 resource
[INFO] 
[INFO] --- maven-compiler-plugin:3.8.1:compile (default-compile) @ demo-service ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-resources-plugin:3.2.0:testResources (default-testResources) @ demo-service ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Using 'UTF-8' encoding to copy filtered properties files.
[INFO] skip non existing resourceDirectory D:\demo\demo-service\src\test\resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.8.1:testCompile (default-testCompile) @ demo-service ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-surefire-plugin:2.22.2:test (default-test) @ demo-service ---
[INFO] 
[INFO] --- maven-jar-plugin:3.2.0:jar (default-jar) @ demo-service ---
[INFO] 
[INFO] --- spring-boot-maven-plugin:2.5.1:repackage (repackage) @ demo-service ---
[INFO] Replacing main artifact with repackaged archive
[INFO] 
[INFO] --- maven-install-plugin:2.5.2:install (default-install) @ demo-service ---
[INFO] Skipping artifact installation
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

### 3 deploy

```bash
[INFO] Scanning for projects...
[INFO] 
[INFO] ----------------------< com.example:demo-service >----------------------
[INFO] Building demo-service 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-resources-plugin:3.2.0:resources (default-resources) @ demo-service ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Using 'UTF-8' encoding to copy filtered properties files.
[INFO] Copying 0 resource
[INFO] Copying 0 resource
[INFO] 
[INFO] --- maven-compiler-plugin:3.8.1:compile (default-compile) @ demo-service ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-resources-plugin:3.2.0:testResources (default-testResources) @ demo-service ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Using 'UTF-8' encoding to copy filtered properties files.
[INFO] skip non existing resourceDirectory D:\demo\demo-service\src\test\resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.8.1:testCompile (default-testCompile) @ demo-service ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-surefire-plugin:2.22.2:test (default-test) @ demo-service ---
[INFO] 
[INFO] --- maven-jar-plugin:3.2.0:jar (default-jar) @ demo-service ---
[INFO] 
[INFO] --- spring-boot-maven-plugin:2.5.1:repackage (repackage) @ demo-service ---
[INFO] Replacing main artifact with repackaged archive
[INFO] 
[INFO] --- maven-install-plugin:2.5.2:install (default-install) @ demo-service ---
[INFO] Skipping artifact installation
[INFO] 
[INFO] --- maven-deploy-plugin:2.8.2:deploy (default-deploy) @ demo-service ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
```

从以上的输出结果中，可以看出:

- package 依次执行了 resources、compile、testResources、testCompile、test、jar、repackage 这 7 个过程
- install 依次执行了 resources、compile、testResources、testCompile、test、jar、repackage、install 这 8 个过程
- deploy 依次执行了 resources、compile、testResources、testCompile、test、jar、repackage、install、deploy 这 9 个过程

综上所述:

- package 完成了项目编译、单元测试、打包功能，但没有把 jar 文件部署到本地 maven 仓库和远程 maven 仓库
- install 完成了项目编译、单元测试、打包功能，同时把 jar 文件安装到本地 maven 仓库，但没有部署到远程 maven 仓库
- deploy 完成了项目编译、单元测试、打包功能，同时把 jar 文件安装到本地 maven 仓库，并且部署到远程 maven 仓库
