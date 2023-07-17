// (c) https://github.com/MontiCore/monticore
package montithings.generator.config;

/**
 * The names of the specific MontiThings options used in this configuration.
 */
public enum Options {

  LANGUAGEPATH("languagePath"),
  MODELPATH("modelPath"),
  MODELPATH_SHORT("mp"),
  TESTPATH("testPath"),
  HANDWRITTENCODEPATH("handwrittenCode"),
  HANDWRITTENCODEPATH_MONTICORE("handcodedPath"),
  HANDWRITTENCODEPATH_SHORT("hwc"),
  OUT("out"),
  OUT_MONTICORE("outputDir"),
  OUT_SHORT("o"),
  PLATFORM("platform"),
  SPLITTING("splitting"),
  SERIALIZATION("serialization"),
  LOGTRACING("logtracing"),
  RECORDING("recording"),
  PORTNAME("portsToMain"),
  COMPONENT_ADDITIONS("automaticComponentAdditions"),
  MESSAGEBROKER("messageBroker"),
  MESSAGEBROKER_SHORT("broker"),
  REPLAYMODE("replayMode"),
  APPLYANOMALYDETECTIONPATTERN("applyAnomalyDetectionPattern"),
  APPLYNETWORKMINIMIZATIONPATTERN("applyNetworkMinimizationPattern"),
  APPLYGRAFANAPATTERN("applyGrafanaPattern"),
  GRAFANAINSTANCEURL("grafanaInstanceUrl"),
  GRAFANAAPIKEY("grafanaApiKey"),
  REPLAYDATAFILE("replayDataPath"),
  MAINCOMP("mainComponent"),
  MAINCOMP_SHORT("main"),
  VERSION("version");

  final String name;

  Options(String name) {
    this.name = name;
  }

  /**
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return this.name;
  }

}
