package com.xebia

class StepExecutor implements  IStepExecutor{
    @Override
    int scanImage(String imageNameWithTag, String scannerClientIP, String scannerHost, int scannerPort) {
        this._steps.sh returnStatus: true, script: "docker run --rm -p9279:9279 -v /var/run/docker.sock:/var/run/docker.sock cplee/clair-scanner clair-scanner --ip ${scannerClientIP}  -c http://${scannerHost}:${scannerPort} ${imageNameWithTag}"
    }
    private _steps

    @Override
    void error(String message) {
        this._steps.error(message)
    }

    StepExecutor(steps) {
        this._steps = steps
    }

}
