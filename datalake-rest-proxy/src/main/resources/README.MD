#  Run DRMS envoy proxy locally
1. Build docker image 
 `docker build -t drms-rest-proxy .`
   
2. Run docker image 
   `docker run --rm -it       -p 10000:50000        drms-rest-proxy:latest      -c /etc/envoy/envoy.yaml `
    