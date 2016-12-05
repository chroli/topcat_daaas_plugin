
# Topcat DAaaS (Data Analysis as a Service) Plugin

The front end interface for accessing the Science and Technology Facilities Council's computational resources for remote analysis.

## Installation

### Bouncy Castle

In order for the SSH to work Bouncy Castle needs to be installed in an 'ext' directory:

* http://www.bouncycastle.org/latest_releases.html


### Websockify

```bash
sudo apt-get install python-dev python-pip
sudo pip install websockify
websockify -v 29876 --target-config=./tokens/
```
