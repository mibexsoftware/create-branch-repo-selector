# Create Branch Repository Selector for Bitbucket Server

![Travis build status](https://travis-ci.org/mibexsoftware/create-branch-repo-selector.svg?branch=master)

For more information, please see our [Atlassian Marketplace listing](https://marketplace.atlassian.com/plugins/ch.mibex.stash.rightrepo/server/overview).


## Motivation
The basic workflow this add-on contributes to is when you start creating a branch from JIRA:

![JIRA development panel with create branch link](src/main/resources/images/highlights/create-branch.png)

This action forwards you to Bitbucket Servers "Create branch" dialog:

![Bitbucket Server's create branch dialog](src/main/resources/images/highlights/lru-repo.png)

Bitbucket Server selects the last recently used repository in the "Create branch" dialog which can be different from the
repository the JIRA issue targets. This can lead to the situation where a branch is created in the wrong 
repository by mistake when the user does not change the branch field accordingly.

This add-on selects the repository that belongs to the JIRA project in the "Create branch" dialog automatically. It
knows which JIRA project a repository belongs to by scanning the commit messages for JIRA issue keys:

![Bitbucket Server's create branch dialog](src/main/resources/images/highlights/repo-from-jira-project.png)


## TL;DR
We also have a YouTube video explaining this add-ons' functionality:

[![Create Branch Repository Selector for Bitbucket Server screencast](http://img.youtube.com/vi/62TY2rPiNps/0.jpg)](http://www.youtube.com/watch?v=62TY2rPiNps "Create Branch Repository Selector for Bitbucket Server")


## Download
You can download this add-on from the [Atlassian Marketplace](https://marketplace.atlassian.com/plugins/ch.mibex.stash.rightrepo/versions).
