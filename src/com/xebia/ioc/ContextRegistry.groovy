package com.xebia.ioc

class ContextRegistry {
    private static IContext _context

    static void registerContext(IContext context) {
        _context = context
    }

    static void registerDefaultContext(Object steps) {
        _context = new DefaultContext(steps)
    }

    static IContext getContext() {
        return _context
    }
}
