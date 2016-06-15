Running the Amazon S3 Personal File Store Sample
================================================
This sample demonstrates interaction with a specialized version of the IdentityTVM.  This specialized version of the 
IdentityTVM requires the user to register with the App by first connecting to an external website and provide a 
username and password.  The username and username will then be required to log into the sample App.  In this sample 
the registration website is a specific page on the IdentityTVM.  

It is assumed that the Token Vending Machine specific to this sample application is already running.

To run this specific sample you will need to do the following:

1. Import the project into Eclipse 
   * Go to File -> Import.  Import Wizard will open.
   * Select General -> Existing Projects into Workspace.  Click Next.
   * In Select root directory, browse to S3PersonalFileStore-Android directory.
   * Select the projects you want to import
   * Click Finish.

2. Update your App configuration:
   * Open the AwsCredential.properties file located in src/com/amazonaws/demo/personalfilestore.
   * Edit the file and provide:
     + The DNS domain name where your Token Vending Machine is running (ex: tvm.elasticbeanstalk.com)
     + The App Name you configured your Token Vending Machine with (ex: MyMobileAppName)
     + Set useSSL to "true" or "false" based on whether your configured your Token Vending Machine with SSL or not.
     + Set the bucket to use for this app.  This should be an existing bucket in Amazon S3 and should match the bucket name you used in the token vending machine policies.

3. The Amazon S3 Personal File Store uses the AWS SDK for Android so it needs to be included into the project as follows:
   * Download and unzip the AWS SDK for Android here: http://aws.amazon.com/sdkforandroid
   * Copy the AWS SDK for Android jar into the libs directory for the S3PersonalFileStore sample.
     + Go to the unzipped AWS SDK for Android directory.
     + cp lib/aws-android-sdk-X.X.X-debug.jar into the libs directory.
   * Refresh the Project by pressing F5.

4. Add the AWS SDK for Android to the build path.
   * Right-click on the Project name in Eclipse.
   * Select the Menu Option Build Path->Configure Build Path...
   * Click on "Add JARs..."
   * Navigate to the libs directory and select the file aws-android-sdk-X.X.X-debug.jar
   * Click OK to add the jar to the build path.
   * Click OK to finish configuring the build path.

5. Build and Run the project:
   * Go to Project ->  Clean.
   * Go to Project ->  Build All.
   * Go to Run -> Run.
   

