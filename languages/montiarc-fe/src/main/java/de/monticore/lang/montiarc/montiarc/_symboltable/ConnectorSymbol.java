package de.monticore.lang.montiarc.montiarc._symboltable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import de.monticore.lang.montiarc.helper.SymbolPrinter;
import de.monticore.lang.montiarc.montiarc._ast.ASTConnector;
import de.monticore.symboltable.CommonSymbol;

/**
 * Symbol for {@link ASTConnector}s. The name of a connector symbol equals its target and vice
 * versa. This is valid since data for a port may only result from a single source. <br/>
 * <br/>
 * The port names (source and target of the connector) must be set <b>relative to the component
 * scope</b> that the connector is defined in. This means that the sourceName may be any in port of
 * the component itself (e.g., "myInPort") or an out port of any subcomponent (e.g.,
 * "subComponent.someOutPort"). The targetName is either a out port of the component itself( e.g.,
 * "myOutPort") or any of the sub components in ports (e.g., "subComponent.someInPort").
 * 
 * @author Arne Haber, Michael von Wenckstern, Robert Heim
 */
public class ConnectorSymbol extends CommonSymbol {
  
  public static final ConnectorKind KIND = ConnectorKind.INSTANCE;
  
  private final Map<String, Optional<String>> stereotype = new HashMap<>();
  
  /**
   * Source of this connector.
   */
  protected String source;
  
  /**
   * Creates a ConnectorSymbol.
   * 
   * @param sourceName the relative name of the source port (e.g., "subComponent.someOutPort" or
   * "myInPort").
   * @param targetName relative name of the target port (e.g., "subComponent.someInPort" or
   * "myOutPort").
   * @return
   */
  public ConnectorSymbol(String sourceName, String targetName) {
    super(targetName, KIND);
    setSource(sourceName);
  }
  
  /**
   * @return the source
   */
  public String getSource() {
    return source;
  }
  
  /**
   * @param source the source to set
   */
  public void setSource(String source) {
    this.source = source;
  }
  
  /**
   * @return the target
   */
  public String getTarget() {
    return getName();
  }
  
  /**
   * Adds the stereotype key=value to this entry's map of stereotypes
   *
   * @param key the stereotype's key
   * @param optional the stereotype's value
   */
  public void addStereotype(String key, Optional<String> optional) {
    stereotype.put(key, optional);
  }
  
  /**
   * Adds the stereotype key=value to this entry's map of stereotypes
   *
   * @param key the stereotype's key
   * @param value the stereotype's value
   */
  public void addStereotype(String key, @Nullable String value) {
    if (value != null && value.isEmpty()) {
      value = null;
    }
    stereotype.put(key, Optional.ofNullable(value));
  }
  
  /**
   * @return map representing the stereotype of this component
   */
  public Map<String, Optional<String>> getStereotype() {
    return stereotype;
  }
  
  @Override
  public String toString() {
    return SymbolPrinter.printConnector(this);
  }
  
}
