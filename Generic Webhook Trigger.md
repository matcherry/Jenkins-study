# Generic Webhook Trigger

# Generic Webhook Triggered

Generic github webhook is a Jenkins Plugin that could help to integrate sepecific branch, folder, and files in github to trigger job on jenkins using webhook tools. 

This is a Jenkins plugin that can:

1. Receive any HTTP request, `JENKINS_URL/generic-webhook-trigger/invoke/`
2. Extract values
- From `POST` content with [JSONPath](https://github.com/json-path/JsonPath) or [XPath](https://www.w3schools.com/xml/xpath_syntax.asp)
- From the `query` parameters
- From the `headers`
1. Trigger a build with those values contribute as variables

There is an optional feature to trigger jobs only if a supplied regular expression matches the extracted variables. Here is an example, let's say the post content looks like this:

`{
  "before": "1848f1236ae15769e6b31e9c4477d8150b018453",
  "after": "5cab18338eaa83240ab86c7b775a9b27b51ef11d",
  "ref": "refs/heads/develop"
}`

Then you can have a variable, resolved from post content, named `ref` of type `JSONPath` and with expression like `$.ref` . The optional filter text can be set to `$ref` and the filter regexp set to [^(refs/heads/develop|refs/heads/feature/.+)$](https://jex.im/regulex/#!embed=false&flags=&re=%5E(refs%2Fheads%2Fdevelop%7Crefs%2Fheads%2Ffeature%2F.%2B)%24) to trigger builds only for develop and feature-branches.

There are more [examples of use cases here](https://github.com/jenkinsci/generic-webhook-trigger-plugin/blob/master/src/test/resources/org/jenkinsci/plugins/gwt/bdd).

Video showing an example usage:

[https://camo.githubusercontent.com/54c05019ffe0c8a2e20a44fdf22dbb7adde7f78aa13e072cc07d091813c78c7b/68747470733a2f2f696d672e796f75747562652e636f6d2f76692f386d724a4e6b6f667871342f302e6a7067](https://camo.githubusercontent.com/54c05019ffe0c8a2e20a44fdf22dbb7adde7f78aa13e072cc07d091813c78c7b/68747470733a2f2f696d672e796f75747562652e636f6d2f76692f386d724a4e6b6f667871342f302e6a7067)

It can trigger on any webhook, like:

- [Bitbucket Cloud](https://confluence.atlassian.com/bitbucket/manage-webhooks-735643732.html)
- [Bitbucket Server](https://confluence.atlassian.com/bitbucketserver/managing-webhooks-in-bitbucket-server-938025878.html)
- [GitHub](https://developer.github.com/webhooks/)
- [GitLab](https://docs.gitlab.com/ce/user/project/integrations/webhooks.html)
- [Gogs](https://gogs.io/docs/features/webhook) and [Gitea](https://docs.gitea.io/en-us/webhooks/)
- [Assembla](https://blog.assembla.com/AssemblaBlog/tabid/12618/bid/107614/Assembla-Bigplans-Integration-How-To.aspx)
- [Jira](https://developer.atlassian.com/server/jira/platform/webhooks/)
- And many many more!

The original use case was to build merge/pull requests. You may use the Git Plugin as described in [this blog post](http://bjurr.com/continuous-integration-with-gitlab-and-jenkins/) to do that. There is also an example of this on the [Violation Comments to GitLab Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Violation+Comments+to+GitLab+Plugin) page.

You may want to report back to the invoking system. [HTTP Request Plugin](https://wiki.jenkins-ci.org/display/JENKINS/HTTP+Request+Plugin) is a very convenient plugin for that.

If a node is selected, then all leafs in that node will be contributed. If a leaf is selected, then only that leaf will be contributed.

## **Trigger only specific job**

When using the plugin in several jobs, you will have the same URL trigger all jobs. If you want to trigger only a certain job you can:

- Use the `token`parameter have different tokens for different jobs. Using only the token means only jobs with that exact token will be visible for that request. This will increase performance and reduce responses of each invocation.
- Or, add some request parameter (or header, or post content) and use the **regexp filter** to trigger only if that parameter has a specific value.

### **Token parameter**

There is a special `token` parameter. When supplied, the invocat

---

# How to use it :

1. Install generic  webhook trigger plugin on jenkins
2. Set up the generic webhook plugin on the specify job. For the setting, I reference this link to set up the plugin, whether we would like to specify a certain files, folder, or branch that could be trigger the job : 
    
    [generic-webhook-trigger-plugin/github-push-trigger-when-specific-file-changes.feature at master · jenkinsci/generic-webhook-trigger-plugin](https://github.com/jenkinsci/generic-webhook-trigger-plugin/blob/master/src/test/resources/org/jenkinsci/plugins/gwt/bdd/github/github-push-trigger-when-specific-file-changes.feature)
    
3. We have to specify the payload url on webhook. The payload url have to be like this : `http://user:password@jenkins_url/generic-webhook-trigger/invoke/` this payload for the authentication without token. if we want to set the payload without using the credential we can use token. And instead using the credential, the payload has to specify the token and the payload has to be like this : `http://jenkins_url/generic-webhook-trigger/invoke?token=(TOKEN_HERE)`

---

Here is some reference how to use it :

[Jenkins Integration on Steroids](https://bjurr.com/jenkins-integration-on-steroids/)
