package il.org.spartan.plugin;

// TODO: Matteo: check this out, its Yossi's example for a configurable object.
// See what you can get out of it.
/** Demo of recommended use of {@link Listener.S}
 * <p>
 * Copy the code, changing the name {@link ConfigurableObjectTemplate} to
 * whatever you need. provide.
 * @author Yossi Gil
 * @year 2016 */
public interface ConfigurableObjectTemplate {
  /** [[SuppressWarningsSpartan]] */
  class Settings extends Listener.S {
    private static final long serialVersionUID = 1L;
    //@formatter:off
    /* default access */ int howMany;
    /* required here! */ boolean robustMode;
    /* the reason is: */ Some other; Configuration variables;
    /* class 'Action' */ Add as; Many you, need;
    /* (extending our */ Some fields;
    /* current class) */ May be;
    /* may then write */ final If changes = null;
    /* or read any of */ Other might, assume, the, form, of;
    /* the Settings's */ static When its, appropriate;
    /* fields without */ Or even;
    /* any setters or */ static final If particular = null;
    /* any getters... */ Or these, occassions, in, which,it, makes, sense;

    //
    // The following are typically generated automatically
    // since class Settings is a Plain Old Java Object:
    /* public access */ public int getHowMany() {return howMany;}

    /* makes methods */ public void setHowMany(final int n) {howMany = n;}
    /* the means for */ public boolean isRobustMode() {return robustMode;}
    /* configurable- */ public void setRobustMode(final boolean b) {robustMode = b;}
    /* objects as in */ public Some getOther() {return other; }
    /* 'Action', see */ public void setOther(final Some other) {this.other = other;}
    /* below v V V v */ public Add getAs() {return as; /* etc., etc. */}
    //@formatter:on

    /** Demo of the implementation. Don't change the name. Just change services
     * @see #go() the only service provided by this template
     * @author Yossi Gil
     * @year 2016 */
    class Action extends Settings {
      /** real serialvVersionUID comes much later in production code */
      private static final long serialVersionUID = 1L;

      int go() {
        listeners().push("started going");
        if (Settings.this.robustMode) {
          listeners().pop("we dare do nothing in robust mode");
          return 0;
        }
        for (int i = 0, $ = 0; i < howMany; ++i) {
          listeners().tick("Iteration", Integer.valueOf(i), "of", Integer.valueOf(howMany));
          $ += fields.hashCode();
          $ *= in.hashCode();
          form.notify();
          if ($ < 0) {
            listeners().pop("overflow");
            return $;
          }
        }
        listeners().pop("exhausted");
        return defaultValue();
      }

      private int defaultValue() {
        return hashCode();
      }
    }

    //@formatter:off
    class Some { /**/ } class Configuration { /**/ } class Add { /**/ } class
    Or { /**/ } class May { /**/ } class Many { /**/ } class If { /**/ } class
    Other { /**/ } class When { /**/ }
  }
}