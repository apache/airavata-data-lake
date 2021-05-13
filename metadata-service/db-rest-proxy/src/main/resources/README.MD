#  Run DRMS envoy proxy locally
1. Build docker image 
 `docker build -t metadata-proxy .`
   
2. Run docker image 
   `docker run --rm -it       -p 10000:50000        metadata-proxy:latest      -c /etc/envoy/envoy.yaml `
    