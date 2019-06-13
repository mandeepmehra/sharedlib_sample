package com.xebia.ioc

import com.xebia.IStepExecutor
import com.xebia.StepExecutor

class DefaultContext implements  IContext, Serializable {

    private _steps

    DefaultContext(steps) {
        this._steps = steps
    }

    @Override
    IStepExecutor getStepExecutor() {
        return new StepExecutor(this._steps)
    }
}
