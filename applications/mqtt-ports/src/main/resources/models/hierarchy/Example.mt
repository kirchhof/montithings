// (c) https://github.com/MontiCore/monticore
package hierarchy;

component Example {
  Source source;
  Sink sink;

  source.value -> sink.value;
}