# Setting up Sonatype Nexus OSS

You can run Sonatype Nexus OSS as a systemd service by:

``` bash
sudo cp sonatype.volume sonatype.container /etc/containers/systemd/
sudo systemctl daemon-reload
sudo systemctl start sonatype.service
sudo systemctl status sonatype.service
```

You can then access Sonatype Nexus OSS at:

[https://localhost:8083](https://localhost:8083)

To get the admin password:

```bash
sudo podman cp systemd-sonatype:/nexus-data/admin.password .
echo ./admin-password
```

The admin username is 'admin'

# Configuring for a Lightwell demo

TODO:

Need to go to the API -> search for ssrf -> do a PUT to add "packages.redhat.com" as an allowed domain


# Sonatype Nexus Config Automation

To apply default repo configuration, from this folder create a vars file containing the definitions you need called extra_vars.yml:

```yaml
---
lw_username: <Lightwell Service Account username>
lw_token: <Lightwell Service Account token>
nexus_api_scheme: <http or https>
nexus_api_hostname: <Nexus hostname>
nexus_api_port: <Nexus port number>
nexus_api_username: <Nexus admin username>
nexus_api_password: <Nexus admin password>
```

Then run the following ansible command:

```bash
echo "localhost" > inventory
ansible-playbook -i inventory -e @extra_vars.yml set_repo.yml
```




Then add two Proxy libraries for remediated and validated Maven
Then add a group repository with the two lightwell and then the maven-central repo
Do the same for npm?
Configure your build tools
