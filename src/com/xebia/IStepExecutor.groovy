package com.xebia

interface IStepExecutor {
    int scanImage(String imageNameWithTag, String scannerClientIP, String scannerHost, String scannerPort )

    void error(String message)
}