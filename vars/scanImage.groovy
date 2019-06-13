def call(String imageNameWithTag){
    sh "docker run --rm -p9279:9279 -v /var/run/docker.sock:/var/run/docker.sock cplee/clair-scanner clair-scanner --ip ${SCANNER_CLIENT_IP}  -c http://${SCANNER_HOST}:${SCANNER_PORT} ${imageNameWithTag}"
}