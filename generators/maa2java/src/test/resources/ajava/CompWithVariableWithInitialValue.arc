package ajava;

component CompWithVariableWithInitialValue {

  port
    in Boolean b,
    out String s;

  var Integer counter;
  
  init {
    counter = 0;
  }
  
  compute Count {
    counter+=1;
  }  
  

}