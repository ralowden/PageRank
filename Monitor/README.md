Set up
------

On local machine, run setup.sh from the main root of the repo: `sh scripts/setup.sh`
Also on local machine, make sure your `variables.sh` variables are set, likely only the username needs to be changed.

Connecting
----------

`./scripts/connect.sh`

Setting up a tunnel for client GUI
----------------------------------

`ssh -YC` is slow and causes problems for chart data. Instead, use a ssh tunnel: `./scripts/tunnel.sh`

Afterwards, run from bin: `sh tclient.sh`

**Warning**: two users can't share the same tunnel, so the tunnel port has to be changed in `variables.sh` and `bin/monitor.properties`.

Compiling
---------

`./scripts/compile.sh` or `sh scripts/compile.sh` on host.

Server
------

From `bin`: `sh server.sh`

Screen-sharing
--------------

https://www.nomachine.com
http://shared-app-vnc.sourceforge.net/
