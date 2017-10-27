package me.mrmakeit.ocjs;

import org.mozilla.javascript.*;

class APIContextFactory extends ContextFactory {
  private static class APIContext extends Context {
    long startTime;
  }
  static {
    ContextFactory.initGlobal(new APIContextFactory());
  }
  @Override
  protected Context makeContext() {
    APIContext cx = new APIContext();
    cx.setOptimizationLevel(-1);
    return cx;
  }
}
