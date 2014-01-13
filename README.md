RedmineNB
=========

Netbeans bugtrack adapter for Redmine

This repository is derived from the [RedmineNB Kenai project](https://kenai.com/projects/redminenb/).  The SVN repostiory was imported into git using git-svn against the Kenai svn repository and the project migrated from a netbeans ant-based build to maven.  Some minor code changes were implemented to allow for complete compilation and the resulting build will install into NetBeans 7.4.

To compile the modules from the command line one requires an installation of Git, JDK 7, and Maven 3.  The respository can be cloned and the nbms compiled with the following commands: 
```
git clone git@github.com:tkunicki/redminenb.git
cd redminenb/dev
mvn install
```

The 3 requried nbm files are now compiled and can be gathered into a single directory for install into Netbeans 7.4 with:
```
mkdir install-files
cp */target/*.nbm install-files
```

Of course, you could also open the parent maven project `dev/pom.xml` in NetBeans and build it...
