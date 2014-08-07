RedmineNB
=========

Netbeans bugtrack adapter for Redmine

History
-------
This repository is derived from the [RedmineNB Kenai project](https://kenai.com/projects/redminenb/).  The SVN repostiory was imported into git using git-svn against the Kenai svn repository and the project migrated from a netbeans ant-based build to maven.  Some minor code changes were implemented to allow for complete compilation and the resulting build will install into NetBeans 7.4.  The master branch contains further enhancements to support NetBeans 8.0.

Building
--------
To compile the modules from the command line one requires an installation of Git, JDK 7, and Maven 3.  The respository can be cloned and the nbms compiled with the following commands:
 
```
git clone git@github.com:redminenb/redminenb.git
cd redminenb/dev
mvn install
```

The resulting module can then be found in the target-directory: `redminenb/dev/target/redminenb-<VERSION>.nbm`

Of course the netbeans git facilities can also be used to clone the repository, in this case the project can directly be opened for editing.

To build a signed version of the plugin the following properties need to be defined:

* keystore
* keystorealias
* keystorepass

and a developer certificate has to be created. The properties can be set in `settings.xml` for global use or while doing a build:

```
mvn -Dkeystore=<path_to_certificate> -Dkeystorealias=<key_to_use> -Dkeystorepass=<password_for_keystore> install
```

Development
-----------

The plugin is developed as a maven project. The plugin project can directly be opened in netbeans. Just open the `redminenb/dev` directory in netbeans via "Open Project" and you are good to go. You can modify and build it directly from within the IDE.