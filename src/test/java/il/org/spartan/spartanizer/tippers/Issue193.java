package il.org.spartan.spartanizer.tippers;

import static il.org.spartan.spartanizer.tippers.TrimmerTestsUtils.*;

import org.junit.*;
import org.junit.runners.*;
/**
 * 
 * @author Dor Ma'ayan
 * @since 2016-09-25
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING) @SuppressWarnings({ "static-method", "javadoc" }) public class Issue193 {
  @Test public void t10() {
    trimmingOf("x*0").gives("0").stays();
  }
  
  @Test public void t20() {
    trimmingOf("0*x").gives("0").stays();
  }
  
  @Test public void t30() {
    trimmingOf("(x+y)*0").gives("0").stays();
  }
}
