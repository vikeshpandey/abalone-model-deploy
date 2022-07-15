import javaposse.jobdsl.dsl.DslException
import jenkins.model.Jenkins
import hudson.model.AbstractProject

// Check if AWS credential parameter is passed or not
def awsCredentialId = getBinding().getVariables()['AWS_CREDENTIAL']
if (awsCredentialId == null) {
  throw new DslException('Please pass AWS credential parameter ' + 'AWS_CREDENTIAL' )
}

def sagemakerProjectName = "github-jenkins-3"
def sagemakerProjectId = "p-zngjwip38zwa"
def sagemakerProjectArn= "arn:aws:sagemaker:eu-west-1:683262351304:project/github-jenkins-3"
def sourceModelPackageGroupName = "github-jenkins-3-p-zngjwip38zwa"
def modelExecutionRole = "arn:aws:iam::683262351304:role/service-role/AmazonSageMakerServiceCatalogProductsUseRole"
def awsRegion = "eu-west-1"
def artifactBucket = "sagemaker-project-p-zngjwip38zwa"

def pipelineName = "sagemaker-" + sagemakerProjectName + "-" + sagemakerProjectId + "-modeldeploy"

// Get git details used in JOB DSL so that can be used for pipeline SCM also
def jobName = getBinding().getVariables()['JOB_NAME']
def gitUrl = getBinding().getVariables()['GIT_URL']
def gitBranch = getBinding().getVariables()['GIT_BRANCH']
def jenkins = Jenkins.getInstance()
def job = (AbstractProject)jenkins.getItem(jobName)
def remoteSCM = job.getScm()
def credentialsId = remoteSCM.getUserRemoteConfigs()[0].getCredentialsId()

pipelineJob(pipelineName) {
  description("Sagemaker Model Deploy Pipeline")
  keepDependencies(false)
  authenticationToken('token')
  properties {
    disableConcurrentBuilds()
  }
  parameters {
    stringParam("ARTIFACT_BUCKET", artifactBucket, "S3 bucket to store training artifact")
    stringParam("SAGEMAKER_PROJECT_NAME", sagemakerProjectName, "Sagemaker Project Name")
    stringParam("SAGEMAKER_PROJECT_ID", sagemakerProjectId, "Sagemaker Project Id")
    stringParam("SAGEMAKER_PROJECT_ARN", sagemakerProjectArn, "Sagemaker Project Arn")
    stringParam("SOURCE_MODEL_PACKAGE_GROUP_NAME", sourceModelPackageGroupName, "Model Package Group Name")
    stringParam("MODEL_EXECUTION_ROLE_ARN", modelExecutionRole, "Role to be used by Model execution.")
    stringParam("AWS_REGION", awsRegion, "AWS region to use for creating entity")
    credentialsParam("AWS_CREDENTIAL") {
      description("AWS credentials to use for creating entity")
      defaultValue(awsCredentialId)
      type("com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl")
      required(true)
    }
  }
  definition {
    cpsScm {
      scm {
        git {
          remote {
            url(gitUrl)
            credentials(credentialsId)
          }
          branch(gitBranch)
        }
      }
      scriptPath("jenkins/Jenkinsfile")
    }
  }
  disabled(false)
  triggers {
    scm("* * * * *")
  }
}