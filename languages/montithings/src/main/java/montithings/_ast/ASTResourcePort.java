// (c) https://github.com/MontiCore/monticore
/* generated from model MontiThings */
/* generated by template ast.AstClass*/

package montithings._ast;


import montiarc._ast.ASTStereotype;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Optional;

public class ASTResourcePort extends ASTResourcePortTOP {

  private boolean tcp = false;
  private boolean WS = false;
  private boolean Ipc = false;
  private boolean FileSystem = false;
  private String uriString = null;


  @Override
  public String getUri() {

    if (uriString == null){
      if (uri.isPresent()){
        uriString = uri.get();
      } else {
        uriString = "ipc://" + name + "/" + RandomStringUtils.randomAlphanumeric(12);
        Ipc = true;
      }
    }

    return uriString;
  }

  @Override
  public void setUri(String uri) {

    if (uri.startsWith("tcp://")){
      tcp = true;
    }
    else if (uri.startsWith("ipc://")){
      Ipc = true;
    }
    else if (uri.startsWith("ws://")){
      WS = true;
    }
    else {
      FileSystem = true;
    }

    this.uriString = uri;
  }

  protected ASTResourcePort(/* generated by template ast.ConstructorParametersDeclaration*/
          Optional<ASTStereotype> stereotype,
          de.monticore.types.types._ast.ASTType type,
          String name,
          Optional<montithings._ast.ASTResourceOption> resourceOption,
          Optional<String> uriOpt ,
          java.util.List<montithings._ast.ASTResourceParameter> resourceParameters,
          Optional<String> requiresType,
          Optional<de.monticore.literals.literals._ast.ASTStringLiteral> requiresName,
          boolean incoming,
          boolean outgoing
  ) {
    super(stereotype, type, name, resourceOption, uriOpt, resourceParameters ,requiresType, requiresName, incoming, outgoing);



    if (uriString.startsWith("tcp://")){
      tcp = true;
    }
    else if (uriString.startsWith("ipc://")){
      Ipc = true;
    }
    else if (uriString.startsWith("ws://")){
      WS = true;
    }
    else {
      FileSystem = true;
    }
  }


  public boolean isIpc() {
    return Ipc;
  }

  public boolean isWebSocket() {
    return WS;
  }

  public boolean isTcp() {
    return tcp;
  }

  public boolean isFileSystem() {
    return FileSystem;
  }

  protected ASTResourcePort(){
    super();
  }

}
