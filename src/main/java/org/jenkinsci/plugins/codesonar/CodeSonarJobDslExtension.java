package org.jenkinsci.plugins.codesonar;

import hudson.Extension;
import javaposse.jobdsl.dsl.RequiresPlugin;
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;

/*
 ```
 job{
 publishers{
 codesonar(String hubAddress, String projectName){
 cyclomaticComplexity(int maxComplexity, boolean markAsFailed)
 redAlert(int maxAlerts, boolean markAsFailed)
 yellowAlert(int maxAlerts, boolean markAsFailed)
 newWarningCountIncrease(float percentage, boolean markAsFailed)
 overallWarningCountIncrease(float percentage, boolean markAsFailed)
 rankedWarningCountIncrease(int minRank, float percentage, boolean markAsFailed)
 }
 }
 }
 ```
 For example:
 ```
 job('myProject_GEN'){
 publishers{
 codesonar('hub','proj'){
 cyclomaticComplexity(20, false)
 redAlert(3, true)
 yellowAlert(10, false)
 newWarningCountIncrease(5, true)
 overallWarningCountIncrease(5, false)
 rankedWarningCountIncrease(30, 5, true)
 }
 }
 }
 ```
 */
@Extension(optional = true)
public class CodeSonarJobDslExtension extends ContextExtensionPoint {

    @RequiresPlugin(id = "codesonar", minimumVersion = "2.0.0")
    @DslExtensionMethod(context = PublisherContext.class)
    public Object codesonar(
            String protocol, int socketTimeoutMS, String hubAddress, String projectName, String credentialId, String sslCertificateCredentialId, String visibilityFilter,
            Runnable closure
    ) {
        CodeSonarJobDslContext context = new CodeSonarJobDslContext();
        executeInContext(closure, context);

        CodeSonarPublisher publisher = new CodeSonarPublisher(context.conditions, protocol, hubAddress, projectName, credentialId, sslCertificateCredentialId, visibilityFilter);
        publisher.setSocketTimeoutMS(socketTimeoutMS);
        return publisher;
    }

    public Object codesonar(String protocol, String hubAddress, String projectName, Runnable closure) {
        return codesonar(protocol, -1, hubAddress, projectName, null, null,  "2", closure);
    }
}
