package il.org.spartan.spartanizer.ast.navigate;

import static il.org.spartan.Utils.*;
import static org.eclipse.jdt.core.dom.ASTNode.*;
import static org.eclipse.jdt.core.dom.Assignment.Operator.*;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.*;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.Assignment.*;
import org.eclipse.jdt.core.dom.rewrite.*;

import static il.org.spartan.spartanizer.ast.navigate.step.*;

import il.org.spartan.*;
import il.org.spartan.spartanizer.ast.factory.*;
import il.org.spartan.spartanizer.ast.safety.*;
import il.org.spartan.spartanizer.ast.safety.iz.*;
import il.org.spartan.spartanizer.cmdline.*;
import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.tippers.*;
import il.org.spartan.spartanizer.utils.*;

/** Collection of definitions and functions that capture some of the quirks of
 * the {@link ASTNode} hierarchy.
 * @author Yossi Gil
 * @since 2014 */
public interface wizard {
  PostfixExpression.Operator DECREMENT_POST = PostfixExpression.Operator.DECREMENT;
  PrefixExpression.Operator DECREMENT_PRE = PrefixExpression.Operator.DECREMENT;
  PostfixExpression.Operator INCREMENT_POST = PostfixExpression.Operator.INCREMENT;
  PrefixExpression.Operator INCREMENT_PRE = PrefixExpression.Operator.INCREMENT;
  PrefixExpression.Operator MINUS1 = PrefixExpression.Operator.MINUS;
  InfixExpression.Operator MINUS2 = InfixExpression.Operator.MINUS;
  PrefixExpression.Operator PLUS1 = PrefixExpression.Operator.PLUS;
  InfixExpression.Operator PLUS2 = InfixExpression.Operator.PLUS;
  @SuppressWarnings("serial") Map<InfixExpression.Operator, InfixExpression.Operator> conjugate = new HashMap<InfixExpression.Operator, InfixExpression.Operator>() {
    {
      put(GREATER, LESS);
      put(LESS, GREATER);
      put(GREATER_EQUALS, LESS_EQUALS);
      put(LESS_EQUALS, GREATER_EQUALS);
    }
  };
  @SuppressWarnings("serial") final Set<String> boxedTypes = new LinkedHashSet<String>() {
    {
      for (final String ¢ : new String[] { "Boolean", "Byte", "Character", "Double", "Float", "Integer", "Long", "Short" }) {
        add(¢);
        add("java.lang." + ¢);
      }
    }
  };
  @SuppressWarnings("serial") final Set<String> valueTypes = new LinkedHashSet<String>(boxedTypes) {
    {
      for (final String ¢ : new String[] { "String" }) {
        add(¢);
        add("java.lang." + ¢);
      }
    }
  };
  /** This list was generated by manually editing the original list at
   * {@link Assignment.Operator} . */
  @SuppressWarnings("serial") final Map<InfixExpression.Operator, Assignment.Operator> infix2assign = new HashMap<InfixExpression.Operator, Assignment.Operator>() {
    {
      put(PLUS, PLUS_ASSIGN);
      put(MINUS, MINUS_ASSIGN);
      put(TIMES, TIMES_ASSIGN);
      put(DIVIDE, DIVIDE_ASSIGN);
      put(AND, BIT_AND_ASSIGN);
      put(OR, BIT_OR_ASSIGN);
      put(XOR, BIT_XOR_ASSIGN);
      put(REMAINDER, REMAINDER_ASSIGN);
      put(LEFT_SHIFT, LEFT_SHIFT_ASSIGN);
      put(RIGHT_SHIFT_SIGNED, RIGHT_SHIFT_SIGNED_ASSIGN);
      put(RIGHT_SHIFT_UNSIGNED, RIGHT_SHIFT_UNSIGNED_ASSIGN);
      put(CONDITIONAL_AND, BIT_AND_ASSIGN);
      put(CONDITIONAL_OR, BIT_OR_ASSIGN);
    }
  };
  /** This list was generated by manually from {@link #infix2assign}
   * {@link Assignment.Operator} . */
  @SuppressWarnings("serial") final Map<Assignment.Operator, InfixExpression.Operator> assign2infix = new HashMap<Assignment.Operator, InfixExpression.Operator>() {
    {
      put(PLUS_ASSIGN, PLUS);
      put(MINUS_ASSIGN, MINUS);
      put(TIMES_ASSIGN, TIMES);
      put(DIVIDE_ASSIGN, DIVIDE);
      put(BIT_AND_ASSIGN, AND);
      put(BIT_OR_ASSIGN, OR);
      put(BIT_XOR_ASSIGN, XOR);
      put(REMAINDER_ASSIGN, REMAINDER);
      put(LEFT_SHIFT_ASSIGN, LEFT_SHIFT);
      put(RIGHT_SHIFT_SIGNED_ASSIGN, RIGHT_SHIFT_SIGNED);
      put(RIGHT_SHIFT_UNSIGNED_ASSIGN, RIGHT_SHIFT_UNSIGNED);
    }
  };
  final NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();

  static Expression applyDeMorgan(final InfixExpression inner) {
    final List<Expression> operands = new ArrayList<>();
    for (final Expression ¢ : hop.operands(flatten.of(inner)))
      operands.add(make.notOf(¢));
    return subject.operands(operands).to(PrefixNotPushdown.conjugate(inner.getOperator()));
  }

  static InfixExpression.Operator assign2infix(final Assignment.Operator ¢) {
    return assign2infix.get(¢);
  }

  /** Obtain a condensed textual representation of an {@link ASTNode}
   * @param ¢ JD
   * @return textual representation of the parameter, */
  static String asString(final ASTNode ¢) {
    return removeWhites(wizard.body(¢));
  }

  /** Converts a string into an AST, depending on it's form, as determined
   * by @link{GuessedContext.find}.
   * @param p string to convert
   * @return AST, if string is not a valid AST according to any form, then
   *         null */
  static ASTNode ast(final String p) {
    switch (GuessedContext.find(p)) {
      case COMPILATION_UNIT_LOOK_ALIKE:
        return into.cu(p);
      case EXPRESSION_LOOK_ALIKE:
        return into.e(p);
      case OUTER_TYPE_LOOKALIKE:
        return into.t(p);
      case STATEMENTS_LOOK_ALIKE:
        return into.s(p);
      default:
        break;
    }
    return null;
  }

  static String body(final ASTNode ¢) {
    return tide.clean(¢ + "");
  }

  /** the function checks if all the given assignments have the same left hand
   * side(variable) and operator
   * @param base The assignment to compare all others to
   * @param as The assignments to compare
   * @return <code><b>true</b></code> <em>iff</em>all assignments has the same
   *         left hand side and operator as the first one or false otherwise */
  static boolean compatible(final Assignment base, final Assignment... as) {
    if (hasNull(base, as))
      return false;
    for (final Assignment ¢ : as)
      if (wizard.incompatible(base, ¢))
        return false;
    return true;
  }

  static boolean compatible(final Assignment.Operator o1, final InfixExpression.Operator o2) {
    return infix2assign.get(o2) == o1;
  }

  /** @param o the assignment operator to compare all to
   * @param os A unknown number of assignments operators
   * @return <code><b>true</b></code> <em>iff</em>all the operator are the same
   *         or false otherwise */
  static boolean compatibleOps(final Assignment.Operator o, final Assignment.Operator... os) {
    if (hasNull(o, os))
      return false;
    for (final Assignment.Operator ¢ : os)
      if (¢ == null || ¢ != o)
        return false;
    return true;
  }

  /** Obtain a condensed textual representation of an {@link ASTNode}
   * @param ¢ JD
   * @return textual representation of the parameter, */
  static String condense(final ASTNode ¢) {
    return removeWhites(wizard.body(¢));
  }

  /** Makes an opposite operator from a given one, which keeps its logical
   * operation after the node swapping. ¢.¢. "&" is commutative, therefore no
   * change needed. "<" isn'¢ commutative, but it has its opposite: ">=".
   * @param ¢ The operator to flip
   * @return correspond operator - ¢.¢. "<=" will become ">", "+" will stay
   *         "+". */
  static InfixExpression.Operator conjugate(final InfixExpression.Operator ¢) {
    return !wizard.conjugate.containsKey(¢) ? ¢ : wizard.conjugate.get(¢);
  }

  /** @param ns unknown number of nodes to check
   * @return <code><b>true</b></code> <em>iff</em>one of the nodes is an
   *         Expression Statement of type Post or Pre Expression with ++ or --
   *         operator. false if none of them are or if the given parameter is
   *         null. */
  static boolean containIncOrDecExp(final ASTNode... ns) {
    if (ns == null)
      return false;
    for (final ASTNode ¢ : ns)
      if (¢ != null && iz.incrementOrDecrement(¢))
        return true;
    return false;
  }

  /** Compute the "de Morgan" conjugate of the operator present on an
   * {@link InfixExpression}.
   * @param x an expression whose operator is either
   *        {@link Operator#CONDITIONAL_AND} or {@link Operator#CONDITIONAL_OR}
   * @return {@link Operator#CONDITIONAL_AND} if the operator present on the
   *         parameter is {@link Operator#CONDITIONAL_OR}, or
   *         {@link Operator#CONDITIONAL_OR} if this operator is
   *         {@link Operator#CONDITIONAL_AND}
   * @see duplicate#deMorgan(Operator) */
  static InfixExpression.Operator deMorgan(final InfixExpression ¢) {
    return wizard.deMorgan(¢.getOperator());
  }

  /** Compute the "de Morgan" conjugate of an operator.
   * @param o must be either {@link Operator#CONDITIONAL_AND} or
   *        {@link Operator#CONDITIONAL_OR}
   * @return {@link Operator#CONDITIONAL_AND} if the parameter is
   *         {@link Operator#CONDITIONAL_OR} , or
   *         {@link Operator#CONDITIONAL_OR} if the parameter is
   *         {@link Operator#CONDITIONAL_AND}
   * @see wizard#deMorgan(InfixExpression) */
  static InfixExpression.Operator deMorgan(final InfixExpression.Operator ¢) {
    assert iz.deMorgan(¢);
    return ¢.equals(CONDITIONAL_AND) ? CONDITIONAL_OR : CONDITIONAL_AND;
  }

  static String essence(final String codeFragment) {
    return tide.clean(wizard.removeComments2(codeFragment));
  }

  /** Find the first matching expression to the given boolean (b).
   * @param b JD,
   * @param xs JD
   * @return first expression from the given list (es) whose boolean value
   *         matches to the given boolean (b). */
  static Expression find(final boolean b, final List<Expression> xs) {
    for (final Expression $ : xs)
      if (iz.booleanLiteral($) && b == az.booleanLiteral($).booleanValue())
        return $;
    return null;
  }

  static boolean incompatible(final Assignment a1, final Assignment a2) {
    return hasNull(a1, a2) || !compatibleOps(a1.getOperator(), a2.getOperator()) || !wizard.same(to(a1), to(a2));
  }

  static Operator infix2assign(final InfixExpression.Operator ¢) {
    assert ¢ != null;
    final Operator $ = infix2assign.get(¢);
    assert $ != null : "No assignment equivalent to " + ¢;
    return $;
  }

  /** Determine whether an InfixExpression.Operator is a comparison operator or
   * not
   * @param o JD
   * @return <code><b>true</b></code> <em>iff</em>one of
   *         {@link #InfixExpression.Operator.XOR},
   *         {@link #InfixExpression.Operator.OR},
   *         {@link #InfixExpression.Operator.AND}, and false otherwise */
  static boolean isBitwiseOperator(final InfixExpression.Operator ¢) {
    return in(¢, XOR, OR, AND);
  }

  static boolean isBoxedType(final String typeName) {
    return boxedTypes.contains(typeName);
  }

  /** Determine whether an InfixExpression.Operator is a comparison operator or
   * not
   * @param o JD
   * @return <code><b>true</b></code> <em>iff</em>one of
   *         {@link #InfixExpression.Operator.LESS},
   *         {@link #InfixExpression.Operator.GREATER},
   *         {@link #InfixExpression.Operator.LESS_EQUALS},
   *         {@link #InfixExpression.Operator.GREATER_EQUALS},
   *         {@link #InfixExpression.Operator.EQUALS},
   *         {@link #InfixExpression.Operator.NOT_EQUALS},
   *         {@link #InfixExpression.Operator.CONDITIONAL_OR},
   *         {@link #InfixExpression.Operator.CONDITIONAL_AND} and false
   *         otherwise */
  static boolean isComparison(final InfixExpression.Operator ¢) {
    return in(¢, LESS, GREATER, LESS_EQUALS, GREATER_EQUALS, EQUALS, //
        NOT_EQUALS, CONDITIONAL_OR, CONDITIONAL_AND);
  }

  static boolean isDefaultLiteral(final Expression ¢) {
    return !iz.nullLiteral(¢) && !iz.literal0(¢) && !literal.false¢(¢) && !iz.literal(¢, 0.0) && !iz.literal(¢, 0L);
  }

  /** Determine whether an InfixExpression.Operator is a shift operator or not
   * @param o JD
   * @return <code><b>true</b></code> <em>iff</em>one of
   *         {@link #InfixExpression.Operator.LEFT_SHIFT},
   *         {@link #InfixExpression.Operator.RIGHT_SHIFT_SIGNED},
   *         {@link #InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED} and false
   *         otherwise */
  static boolean isShift(final InfixExpression.Operator ¢) {
    return in(¢, LEFT_SHIFT, RIGHT_SHIFT_SIGNED, RIGHT_SHIFT_UNSIGNED);
  }

  static boolean isValueType(final String typeName) {
    return valueTypes.contains(typeName);
  }

  static boolean isValueType(final Type ¢) {
    return isValueType(!haz.binding(¢) ? ¢ + "" : ¢.resolveBinding().getBinaryName());
  }

  /** Determine whether a node is an infix expression whose operator is
   * non-associative.
   * @param pattern JD
   * @return <code><b>true</b></code> <i>iff</i> the parameter is a node which
   *         is an infix expression whose operator is */
  static boolean nonAssociative(final ASTNode ¢) {
    return nonAssociative(az.infixExpression(¢));
  }

  static boolean nonAssociative(final InfixExpression ¢) {
    return ¢ != null && (in(¢.getOperator(), MINUS, DIVIDE, REMAINDER, LEFT_SHIFT, RIGHT_SHIFT_SIGNED, RIGHT_SHIFT_UNSIGNED)
        || iz.infixPlus(¢) && !type.isNotString(¢));
  }

  /** Parenthesize an expression (if necessary).
   * @param x JD
   * @return a
   *         {@link il.org.spartan.spartanizer.ast.factory.duplicate#duplicate(Expression)}
   *         of the parameter wrapped in parenthesis. */
  static Expression parenthesize(final Expression ¢) {
    return iz.noParenthesisRequired(¢) ? duplicate.of(¢) : make.parethesized(¢);
  }

  static ASTParser parser(final int kind) {
    final ASTParser $ = ASTParser.newParser(ASTParser.K_COMPILATION_UNIT);
    $.setKind(kind);
    $.setResolveBindings(false);
    final Map<String, String> options = JavaCore.getOptions();
    options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8); // or newer
    // version
    $.setCompilerOptions(options);
    return $;
  }

  /** Make a duplicate, suitable for tree rewrite, of the parameter
   * @param ¢ JD
   * @param ¢ JD
   * @return a duplicate of the parameter, downcasted to the returned type.
   * @see ASTNode#copySubtree
   * @see ASTRewrite */
  @SuppressWarnings("unchecked") static <N extends ASTNode> N rebase(final N n, final AST t) {
    return (N) copySubtree(t, n);
  }

  /** As {@link elze(ConditionalExpression)} but returns the last else statement
   * in "if - else if - ... - else" statement
   * @param ¢ JD
   * @return last nested else statement */
  static Statement recursiveElze(final IfStatement ¢) {
    for (Statement $ = ¢.getElseStatement();; $ = ((IfStatement) $).getElseStatement())
      if (!($ instanceof IfStatement))
        return $;
  }

  /** Remove all occurrences of a boolean literal from a list of
   * {@link Expression}¢
   * <p>
   * @param ¢ JD
   * @param xs JD */
  static void removeAll(final boolean ¢, final List<Expression> xs) {
    for (;;) {
      final Expression x = find(¢, xs);
      if (x == null)
        return;
      xs.remove(x);
    }
  }

  static String removeComments(final String codeFragment) {
    return codeFragment.replaceAll("//.*?\n", "\n").replaceAll("/\\*(?=(?:(?!\\*/)[\\s\\S])*?)(?:(?!\\*/)[\\s\\S])*\\*/", "");
  }

  static String removeComments2(final String codeFragment) {
    return codeFragment//
        .replaceAll("//.*?\n", "\n")//
        .replaceAll("/\\*(?=(?:(?!\\*/)[\\s\\S])*?)(?:(?!\\*/)[\\s\\S])*\\*/", "");
  }

  /** replaces an ASTNode with another
   * @param n
   * @param with */
  static <N extends ASTNode> void replace(final N n, final N with, final ASTRewrite r) {
    r.replace(n, with, null);
  }

  /** Determine whether two nodes are the same, in the sense that their textual
   * representations is identical.
   * <p>
   * Each of the parameters may be <code><b>null</b></code>; a
   * <code><b>null</b></code> is only equal to< code><b>null</b></code>
   * @param n1 JD
   * @param n2 JD
   * @return <code><b>true</b></code> if the parameters are the same. */
  static boolean same(final ASTNode n1, final ASTNode n2) {
    return n1 == n2 || n1 != null && n2 != null && n1.getNodeType() == n2.getNodeType() && body(n1).equals(body(n2));
  }

  /** String wise comparison of all the given SimpleNames
   * @param ¢ string to compare all names to
   * @param xs SimplesNames to compare by their string value to cmpTo
   * @return <code><b>true</b></code> <em>iff</em>all names are the same (string
   *         wise) or false otherwise */
  static boolean same(final Expression x, final Expression... xs) {
    for (final Expression ¢ : xs)
      if (!same(¢, x))
        return false;
    return true;
  }

  /** Determine whether two lists of nodes are the same, in the sense that their
   * textual representations is identical.
   * @param ns1 first list to compare
   * @param ns2 second list to compare
   * @return are the lists equal string-wise */
  static <¢ extends ASTNode> boolean same(final List<¢> ns1, final List<¢> ns2) {
    if (ns1 == ns2)
      return true;
    if (ns1.size() != ns2.size())
      return false;
    for (int ¢ = 0; ¢ < ns1.size(); ++¢)
      if (!same(ns1.get(¢), ns2.get(¢)))
        return false;
    return true;
  }

  @SuppressWarnings("unchecked") static List<MethodDeclaration> getMethodsSorted(ASTNode n) {
    List<MethodDeclaration> $ = new ArrayList<>();
    n.accept(new ASTVisitor() {
      @Override public boolean visit(final MethodDeclaration ¢) {
        $.add(¢);
        return false;
      }
    });
    return (List<MethodDeclaration>) $.stream().sorted((x, y) -> metrics.countStatements(x) > metrics.countStatements(y)
        || metrics.countStatements(x) == metrics.countStatements(y) && x.parameters().size() > y.parameters().size() ? -1 : 1);
  }
}
