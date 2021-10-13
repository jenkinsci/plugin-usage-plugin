## Version History

### [Unreleased]

 - Adds integration tests (https://github.com/jenkinsci/plugin-usage-plugin/pull/20)

### [Version 1.2] (2021-02-16)

 - Add API support (https://github.com/jenkinsci/plugin-usage-plugin/pull/18)
 - Suport for Maven projects pre, post builds, parameters, conditional-builder and promotions (https://github.com/jenkinsci/plugin-usage-plugin/pull/21)
 - Add support for @symbol function names in pipeline jobs (https://github.com/jenkinsci/plugin-usage-plugin/pull/22)

### [Version 1.1] (2020-09-09)

 - Added columns to indicate if a plugin has dependants or not
 - Added processing of Publisher in promotions
 - Fixed [JENKINS-63442 ClassNotFoundException: org.jenkinsci.plugins.workflow.job.WorkflowJob](https://issues.jenkins-ci.org/browse/JENKINS-63442)
 - Change minimum supported jenkins version from 2.138.4 to 2.204

### [Version 1.0] (2020-03-09)

**Major Upgrade**

- move wiki documentation to GitHub
- Upgrade to Java 8 and internal changes
- Change minimum supported jenkins version from 1.625.1 to 2.138.4
- separate section for non-jobs plugins (related to [JENKINS-58583](https://issues.jenkins-ci.org/browse/JENKINS-58583))
- Sort plugins and jobs by name (related to [JENKINS-53269](https://issues.jenkins-ci.org/browse/JENKINS-53269))
- use full display name to show jobs with same name (related to [JENKINS-53268](https://issues.jenkins-ci.org/browse/JENKINS-53268) and [JENKINS-53267](https://issues.jenkins-ci.org/browse/JENKINS-53267))
- Support of new types:
   - conditional build step (related to [JENKINS-30673](https://issues.jenkins-ci.org/browse/JENKINS-30673))
   - project parameters for plugins (related to [JENKINS-48978](https://issues.jenkins-ci.org/browse/JENKINS-48978))
   - promoted builds plugin (related to [JENKINS-35159](https://issues.jenkins-ci.org/browse/JENKINS-35159))
   - declarative pipeline support for step functions (related to [JENKINS-53597](https://issues.jenkins-ci.org/browse/JENKINS-53597))
   - Maven projects (related to [JENKINS-41507](https://issues.jenkins-ci.org/browse/JENKINS-41507))


### [Version 0.4] (2018-04-22)

-   add column with plugin's version
-   list all plugins, not only specified types of Extension Points

### [Version 0.3] (2014-07-29)

-   bugfix for sorting the table
-   Added support for french
-   Made the plugin available for every user and not just for the admins

### [Version 0.2] (2014-07-11)

-   UI improvements

### Version 0.1 (2014-07-09)

-   initial Version

[Unreleased]: https://github.com/jenkinsci/plugin-usage-plugin/compare/plugin-usage-plugin-1.2...HEAD
[Version 1.2]: https://github.com/jenkinsci/plugin-usage-plugin/compare/plugin-usage-plugin-1.1...plugin-usage-plugin-1.2
[Version 1.1]: https://github.com/jenkinsci/plugin-usage-plugin/compare/plugin-usage-plugin-1.0...plugin-usage-plugin-1.1
[Version 1.0]: https://github.com/jenkinsci/plugin-usage-plugin/compare/plugin-usage-plugin-0.4...plugin-usage-plugin-1.0
[Version 0.4]: https://github.com/jenkinsci/plugin-usage-plugin/compare/plugin-usage-plugin-0.3...plugin-usage-plugin-0.4
[Version 0.3]: https://github.com/jenkinsci/plugin-usage-plugin/compare/plugin-usage-plugin-0.2...plugin-usage-plugin-0.3
[Version 0.2]: https://github.com/jenkinsci/plugin-usage-plugin/compare/plugin-usage-plugin-0.1...plugin-usage-plugin-0.2
