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
