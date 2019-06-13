package com.xebia.ioc

import com.xebia.IStepExecutor

interface IContext {
    IStepExecutor getStepExecutor()
}