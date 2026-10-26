# Setting up JFrog Artifactory

You can run JFrog Artifactory as a systemd service by:

``` bash
sudo cp artifactory_data.volume artifactory.container /etc/containers/systemd/
sudo systemctl daemon-reload
sudo systemctl start artifactory.service
sudo systemctl status artifactory.service
```

You can then access JFrog Artifactory at:

[https://localhost:8082](https://localhost:8082)

The default login is admin:password

# Configuring for a Lightwell demo

* Create a Remote Repository of 'Maven' type named 'maven-central'. Accept all the defaults.
* Create a Remote Repository of 'Maven' type named 'lightwell-remediated'. The URL should be 'https://packages.redhat.com/lightwell/public-lightwell-demo/java/remediated/'. Accept all the other defaults.
* Create a Remote Repository of 'Maven' type named 'lightwell-validated'. The URL should be 'https://packages.redhat.com/lightwell/public-lightwell-demo/java/validated/'. Accept all the other defaults.
* Create a Virtual Repository of 'Maven' type named 'lightwell-enabled-maven'. Turn on 'Force Authentication'. Add the three repositories, ensuring they are listed (from top to bottom) as 'lightwell-remediated', 'lightwell-validated', 'maven-central'. Accept all the other defaults.
* Create a user
* Create a token for the user
