/* generated by template templates.de.monticore.lang.montiarc.tagschema.ValuedTagType*/


package nfp.PowerConsumptionTagSchema;

import de.monticore.lang.montiarc.tagging._symboltable.TagKind;
import de.monticore.lang.montiarc.tagging._symboltable.TagSymbol;


/**
 * Created by ValuedTagType.ftl
 */
public class PowerTesterSymbol extends TagSymbol {
  public static final PowerTesterKind KIND = PowerTesterKind.INSTANCE;

  public PowerTesterSymbol(String value) {
    super(KIND, value);
  }

  protected PowerTesterSymbol(PowerTesterKind kind, String value) {
    super(kind, value);
  }

  public String getValue() {
     return getValue(0);
  }

  @Override
  public String toString() {
    return String.format("PowerTester = \"%s\"",
      getValue().toString());
  }

  public static class PowerTesterKind extends TagKind {
    public static final PowerTesterKind INSTANCE = new PowerTesterKind();

    protected PowerTesterKind() {
    }
  }
}