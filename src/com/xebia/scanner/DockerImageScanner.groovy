package com.xebia.scanner

import com.xebia.IStepExecutor
import com.xebia.ioc.ContextRegistry

class DockerImageScanner implements  Serializable{
    private String imageNameWithTag
    private String scannerClientIP
    private String scannerHost
    private int scannerPort

    DockerImageScanner(String imageNameWithTag, String scannerClientIP, String scannerHost, int scannerPort) {
        this.imageNameWithTag = imageNameWithTag
        this.scannerClientIP= scannerClientIP
        this.scannerHost= scannerHost
        this.scannerPort= scannerPort
    }

    void scan(){
        IStepExecutor stepExecutor   = ContextRegistry.getContext().getStepExecutor();
        int returnStatus = stepExecutor.scanImage (imageNameWithTag, scannerClientIP, scannerHost, scannerPort)
        if (returnStatus != 0) {
            stepExecutor.error ("Error in image scanning")
        }
    }
}
