package Optimization;

import LexicalAnalysis.Lexeme;
import LexicalAnalysis.Type;

import static LexicalAnalysis.Type.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// This class walks the parse tree and rewrites parts of it to simpler forms
// It never executes the program
public class Optimizer {

    // We keep counters here so we can print a summary after optimization to see difference, since its not like we can directly compare
    public static class Stats {
        // Number of binary expressions reduced to literals (for example, 2+3 -> 5)
        private int constantsFolded;

        // Number of identifier uses replaced with known literal values
        private int identifiersPropagated;

        // Number of IF nodes simplified to a single chosen branch
        private int conditionalsPruned;

        // Number of WHILE nodes removed because the condition is always false
        private int loopsPruned;

        // Getter for folded-constant count.
        public int getConstantsFolded() {
            return constantsFolded;
        }

        // Getter for propagated-identifier count.
        public int getIdentifiersPropagated() {
            return identifiersPropagated;
        }

        // Getter for pruned-conditional count.
        public int getConditionalsPruned() {
            return conditionalsPruned;
        }

        // Getter for pruned-loop count.
        public int getLoopsPruned() {
            return loopsPruned;
        }
    }

    // Single stats object for one optimizer instance.
    private final Stats stats = new Stats();

    // Expose stats for callers that want to inspect them directly
    public Stats getStats() {
        return stats;
    }

    // Entry point: optimize the root node with a fresh constant map
    // The map tracks known identifier -> literal relationships
    public Lexeme optimize(Lexeme root) {
        return optimizeNode(root, new HashMap<>());
    }

    public String getSummary() {
        return "Optimizer summary: folded " + stats.constantsFolded
            + " constants, propagated " + stats.identifiersPropagated
            + " identifiers, pruned " + stats.conditionalsPruned
            + " conditionals, pruned " + stats.loopsPruned + " loops.";
    }

    // Optimize statements in-order so propagation can flow from earlier lines to later lines
    private Lexeme optimizeStatementList(Lexeme statementList, Map<String, Lexeme> constants) {
        // Children are the statements in this block
        ArrayList<Lexeme> children = statementList.getChildren();

        // Process one statement at a time
        for (int i = 0; i < children.size(); i++) {
            // First optimize this statement using current constant knowledge
            Lexeme optimizedStatement = optimizeNode(children.get(i), constants);

            // Save optimized version back into the list
            children.set(i, optimizedStatement);

            // Then update constant knowledge based on what this statement did
            updateConstantsForStatement(optimizedStatement, constants);
        }

        // Return the same statement list node (mutated in place)
        return statementList;
    }

    // Update our identifier -> literal map after seeing one statement
    private void updateConstantsForStatement(Lexeme statement, Map<String, Lexeme> constants) {
        // nothing to do if statement is missing.
        if (statement == null) return;

        // Handle assignment: identifier = expression
        if (statement.getType() == EQUALS && statement.getChildren().size() >= 2) {
            // Left side should be identifier
            Lexeme identifier = statement.getChildren().get(0);

            // Right side is assigned value expression (already optimized)
            Lexeme value = statement.getChildren().get(1);

            // Only track named identifiers
            if (identifier.getType() == IDENTIFIER && identifier.getStringVal() != null) {
                // If value is a literal, we can remember it for propagation later
                if (isLiteral(value)) {
                    constants.put(identifier.getStringVal(), cloneLiteral(value, value.getLineNumber()));
                }
                else {
                    // If value is not literal anymore, this identifier is no longer constant
                    constants.remove(identifier.getStringVal());
                }
            }

            // Assignment handling is complete.
            return;
        }

        // Nested statement lists are processed recursively for map updates.
        if (statement.getType() == STATEMENT_LIST) {
            for (Lexeme nested : statement.getChildren()) {
                updateConstantsForStatement(nested, constants);
            }
            return;
        }

        // These node types can branch, loop, or cause side effects
        // To stay safe, we clear known constants instead of assuming they still hold
        if (statement.getType() == IF
            || statement.getType() == FOR
            || statement.getType() == WHILE
            || statement.getType() == FUNCTIONDECLARATION
            || statement.getType() == FUNCTIONCALL
            || statement.getType() == UNARYEXPRESSION) {
            constants.clear();
        }
    }

    // Core  optimizer
    // "constants" is the current known mapping for this path.
    private Lexeme optimizeNode(Lexeme node, Map<String, Lexeme> constants) {
        // Null node stays null - got erorrs because I didn't have this :) 
        if (node == null) return null;

        // Statement list has special behavior
        if (node.getType() == STATEMENT_LIST) {
            return optimizeStatementList(node, constants);
        }

        // Replace identifier use with literal if we know its value
        // Example: if map says x=5, then expression node x can become integer literal 5
        if (node.getType() == IDENTIFIER && node.getStringVal() != null && constants.containsKey(node.getStringVal())) {
            stats.identifiersPropagated++;
            return cloneLiteral(constants.get(node.getStringVal()), node.getLineNumber());
        }

        // For assignment, only optimize the right-hand side
        // Left side must stay as identifier name, not literal
        if (node.getType() == EQUALS) {
            if (node.getChildren().size() >= 2) {
                node.getChildren().set(1, optimizeNode(node.getChildren().get(1), constants));
            }
            return node;
        }

        // Only optimize argument list subtree for fnx
        if (node.getType() == FUNCTIONCALL) {
            if (node.getChildren().size() >= 2) {
                node.getChildren().set(1, optimizeNode(node.getChildren().get(1), constants));
            }
            return node;
        }

        // For parameter statement, type marker child must stay as-is
        // We only optimize expression/value child
        if (node.getType() == PARAMETER_STATEMENT) {
            if (!node.getChildren().isEmpty()) {
                node.getChildren().set(0, optimizeNode(node.getChildren().get(0), constants));
            }
            return node;
        }

        // Function declarations get optimized in isolation from caller constants
        if (node.getType() == FUNCTIONDECLARATION) {
            ArrayList<Lexeme> children = node.getChildren();

            if (children.isEmpty()) return node;

            int functionNameIndex = (children.get(0).getType() == CONSISTENCYINDICATOR) ? 3 : 2;

            int bodyIndex = children.size() - 1;

            for (int i = 0; i < children.size(); i++) {
                if (i == functionNameIndex) continue;

                if (i == bodyIndex) {
                    children.set(i, optimizeNode(children.get(i), new HashMap<>()));
                }
                else {
                    children.set(i, optimizeNode(children.get(i), new HashMap<>()));
                }
            }
            return node;
        }

        // For control flow nodes, optimize each child with a copied map
        if (node.getType() == IF || node.getType() == WHILE || node.getType() == FOR) {
            ArrayList<Lexeme> children = node.getChildren();
            for (int i = 0; i < children.size(); i++) {
                children.set(i, optimizeNode(children.get(i), new HashMap<>(constants)));
            }
        }
        else {
            // For normal nodes, optimize children with shared current map
            ArrayList<Lexeme> children = node.getChildren();
            for (int i = 0; i < children.size(); i++) {
                children.set(i, optimizeNode(children.get(i), constants));
            }
        }

        // After children are optimized, try to fold this node if it is a foldable binary operation!
        if (isBinaryOperator(node.getType())) {
            Lexeme folded = foldBinary(node);
            if (folded != null) {
                stats.constantsFolded++;
                return folded;
            }
        }

        // Try to prune constant IF chains
        if (node.getType() == IF) {
            Lexeme prunedIf = pruneIf(node);
            if (prunedIf != null) {
                stats.conditionalsPruned++;
                return prunedIf;
            }
        }

        // Try to prune WHILE false loops
        if (node.getType() == WHILE) {
            Lexeme prunedWhile = pruneWhile(node);
            if (prunedWhile != null) {
                stats.loopsPruned++;
                return prunedWhile;
            }
        }

        // If nothing changed at this node, return the original node instead
        return node;
    }

    // Build a new literal node with the same literal value but a target line number
    private Lexeme cloneLiteral(Lexeme literal, Integer lineNumber) {
        if (literal.getType() == INTEGER) return new Lexeme(INTEGER, lineNumber, literal.getIntVal());
        if (literal.getType() == DOUBLE) return new Lexeme(DOUBLE, lineNumber, literal.getRealVal());
        if (literal.getType() == STRING) return new Lexeme(STRING, lineNumber, literal.getStringVal());
        if (literal.getType() == BOOLEAN) return new Lexeme(BOOLEAN, lineNumber, literal.getBooleanVal());

        // if none of these as backup, java not pleased if I don't
        return literal;
    }

    // Return true only for binary operators we know how to fold
    private boolean isBinaryOperator(Type type) {
        return type == PLUS
            || type == MINUS
            || type == TIMES
            || type == DIVIDE
            || type == MOD
            || type == POWEROF
            || type == GREATER
            || type == GREATEROREQUALS
            || type == LESS
            || type == LESSOREQUALS
            || type == EQUALS_EQUALS
            || type == DOESNOTEQUAL;
    }

    // Try to fold one binary node into a literal
    private Lexeme foldBinary(Lexeme node) {
        // Need two children for a binary operation
        if (node.getChildren().size() < 2) return null;

        Lexeme left = node.getChildren().get(0);
        Lexeme right = node.getChildren().get(1);

        if (!isLiteral(left) || !isLiteral(right)) return null;

        switch (node.getType()) {
            case PLUS:
                return foldPlus(left, right, node.getLineNumber());
            case MINUS:
                return foldMinus(left, right, node.getLineNumber());
            case TIMES:
                return foldTimes(left, right, node.getLineNumber());
            case DIVIDE:
                return foldDivide(left, right, node.getLineNumber());
            case MOD:
                return foldMod(left, right, node.getLineNumber());
            case POWEROF:
                return foldPower(left, right, node.getLineNumber());
            case GREATER:
            case GREATEROREQUALS:
            case LESS:
            case LESSOREQUALS:
                return foldMagnitudeComparison(left, right, node.getType(), node.getLineNumber());
            case EQUALS_EQUALS:
            case DOESNOTEQUAL:
                return foldEquality(left, right, node.getType(), node.getLineNumber());
            default:
                return null;
        }
    }

    // Simplify IF/ELSE chain when all conditions are literal booleans
    private Lexeme pruneIf(Lexeme ifNode) {
        // IF node children layout follows parser's format
        ArrayList<Lexeme> children = ifNode.getChildren();

        // Nothing to prune if there are no children
        if (children.isEmpty()) return null;

        int ifCount = 0;
        while (ifCount < children.size() && children.get(ifCount).getType() == IF) {
            ifCount++;
        }

        if (ifCount == 0) return null;

        boolean hasElse = (ifCount < children.size() && children.get(ifCount).getType() == ELSE);
        int elseCount = hasElse ? 1 : 0;

        int firstConditionIndex = ifCount + elseCount;
        if (children.size() < firstConditionIndex + ifCount) return null;

        for (int i = 0; i < ifCount; i++) {
            if (!isBooleanLiteral(children.get(firstConditionIndex + i))) {
                return null;
            }
        }

        int firstBodyIndex = firstConditionIndex + ifCount;
        if (children.size() < firstBodyIndex + ifCount + elseCount) return null;

        for (int i = 0; i < ifCount; i++) {
            if (children.get(firstConditionIndex + i).getBooleanVal()) {
                return children.get(firstBodyIndex + i);
            }
        }

        if (hasElse) {
            return children.get(firstBodyIndex + ifCount);
        }

        return new Lexeme(STATEMENT_LIST, ifNode.getLineNumber());
    }

    // Simplify WHILE nodes when condition is literally false.
    private Lexeme pruneWhile(Lexeme whileNode) {
        ArrayList<Lexeme> children = whileNode.getChildren();

        if (children.isEmpty()) return null;

        Lexeme condition = children.get(0);

        if (!isBooleanLiteral(condition)) return null;

        if (!condition.getBooleanVal()) {
            return new Lexeme(STATEMENT_LIST, whileNode.getLineNumber());
        }

        return null;
    }

    private boolean isLiteral(Lexeme lexeme) {
        Type type = lexeme.getType();
        return type == INTEGER || type == DOUBLE || type == STRING || type == BOOLEAN;
    }

    private boolean isBooleanLiteral(Lexeme lexeme) {
        return lexeme.getType() == BOOLEAN && lexeme.getBooleanVal() != null;
    }

    private boolean isNumericLiteral(Lexeme lexeme) {
        return lexeme.getType() == INTEGER || lexeme.getType() == DOUBLE;
    }

    private double numericValue(Lexeme lexeme) {
        if (lexeme.getType() == INTEGER) return lexeme.getIntVal();
        return lexeme.getRealVal();
    }

    private boolean isZero(Lexeme lexeme) {
        if (!isNumericLiteral(lexeme)) return false;
        return numericValue(lexeme) == 0.0;
    }

    // Fold PLUS
    private Lexeme foldPlus(Lexeme left, Lexeme right, Integer lineNumber) {
        if (isNumericLiteral(left) && isNumericLiteral(right)) {
            if (left.getType() == INTEGER && right.getType() == INTEGER) {
                return new Lexeme(INTEGER, lineNumber, left.getIntVal() + right.getIntVal());
            }
            return new Lexeme(DOUBLE, lineNumber, numericValue(left) + numericValue(right));
        }

        if (left.getType() == STRING && right.getType() == STRING) {
            return new Lexeme(STRING, lineNumber, left.getStringVal() + right.getStringVal());
        }

        return null;
    }

    private Lexeme foldMinus(Lexeme left, Lexeme right, Integer lineNumber) {
        if (!isNumericLiteral(left) || !isNumericLiteral(right)) return null;

        if (left.getType() == INTEGER && right.getType() == INTEGER) {
            return new Lexeme(INTEGER, lineNumber, left.getIntVal() - right.getIntVal());
        }

        return new Lexeme(DOUBLE, lineNumber, numericValue(left) - numericValue(right));
    }

    private Lexeme foldTimes(Lexeme left, Lexeme right, Integer lineNumber) {
        if (!isNumericLiteral(left) || !isNumericLiteral(right)) return null;

        if (left.getType() == INTEGER && right.getType() == INTEGER) {
            return new Lexeme(INTEGER, lineNumber, left.getIntVal() * right.getIntVal());
        }

        return new Lexeme(DOUBLE, lineNumber, numericValue(left) * numericValue(right));
    }

    private Lexeme foldDivide(Lexeme left, Lexeme right, Integer lineNumber) {
        if (!isNumericLiteral(left) || !isNumericLiteral(right)) return null;

        if (isZero(right)) return null;

        if (left.getType() == INTEGER && right.getType() == INTEGER) {
            return new Lexeme(INTEGER, lineNumber, left.getIntVal() / right.getIntVal());
        }

        return new Lexeme(DOUBLE, lineNumber, numericValue(left) / numericValue(right));
    }

    private Lexeme foldMod(Lexeme left, Lexeme right, Integer lineNumber) {
        if (!isNumericLiteral(left) || !isNumericLiteral(right)) return null;

        if (isZero(right)) return null;

        if (left.getType() == INTEGER && right.getType() == INTEGER) {
            return new Lexeme(INTEGER, lineNumber, left.getIntVal() % right.getIntVal());
        }

        return new Lexeme(DOUBLE, lineNumber, numericValue(left) % numericValue(right));
    }

    private Lexeme foldPower(Lexeme left, Lexeme right, Integer lineNumber) {
        if (!isNumericLiteral(left) || !isNumericLiteral(right)) return null;

        if (left.getType() == INTEGER && right.getType() == INTEGER) {
            return new Lexeme(INTEGER, lineNumber, (int) Math.pow(left.getIntVal(), right.getIntVal()));
        }

        return new Lexeme(DOUBLE, lineNumber, Math.pow(numericValue(left), numericValue(right)));
    }

    private Lexeme foldMagnitudeComparison(Lexeme left, Lexeme right, Type operator, Integer lineNumber) {
        // Numeric comparisons.
        if (isNumericLiteral(left) && isNumericLiteral(right)) {
            double l = numericValue(left);
            double r = numericValue(right);
            return new Lexeme(BOOLEAN, lineNumber, applyMagnitude(operator, l, r));
        }

        if (left.getType() == BOOLEAN && right.getType() == BOOLEAN) {
            int l = left.getBooleanVal() ? 1 : 0;
            int r = right.getBooleanVal() ? 1 : 0;
            return new Lexeme(BOOLEAN, lineNumber, applyMagnitude(operator, l, r));
        }

        return null;
    }

    private boolean applyMagnitude(Type operator, double left, double right) {
        switch (operator) {
            case GREATER:
                return left > right;
            case GREATEROREQUALS:
                return left >= right;
            case LESS:
                return left < right;
            case LESSOREQUALS:
                return left <= right;
            default:
                return false;
        }
    }

    private Lexeme foldEquality(Lexeme left, Lexeme right, Type operator, Integer lineNumber) {
        Boolean equalsResult = null;

        if (isNumericLiteral(left) && isNumericLiteral(right)) {
            equalsResult = numericValue(left) == numericValue(right);
        }

        else if (left.getType() == BOOLEAN && right.getType() == BOOLEAN) {
            equalsResult = left.getBooleanVal().equals(right.getBooleanVal());
        }

        else if (left.getType() == STRING && right.getType() == STRING) {
            equalsResult = left.getStringVal().equals(right.getStringVal());
        }

        if (equalsResult == null) return null;

        if (operator == EQUALS_EQUALS) {
            return new Lexeme(BOOLEAN, lineNumber, equalsResult);
        }
        return new Lexeme(BOOLEAN, lineNumber, !equalsResult);
    }
}
