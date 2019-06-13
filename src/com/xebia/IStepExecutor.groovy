package com.xebia

interface IStepExecutor {
    int scanImage(String imageNameWithTag, String scannerClientIP, String scannerHost, int scannerPort )

    void error(String message)
}