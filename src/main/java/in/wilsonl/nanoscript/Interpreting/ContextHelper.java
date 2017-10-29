package in.wilsonl.nanoscript.Interpreting;

import in.wilsonl.nanoscript.Exception.InternalStateError;
import in.wilsonl.nanoscript.Interpreting.Data.NSData;
import in.wilsonl.nanoscript.Syntax.CodeBlock;
import in.wilsonl.nanoscript.Syntax.Expression.AnonymousObjectExpression;
import in.wilsonl.nanoscript.Syntax.Expression.CallExpression;
import in.wilsonl.nanoscript.Syntax.Expression.ConditionalBranchesExpression;
import in.wilsonl.nanoscript.Syntax.Expression.Expression;
import in.wilsonl.nanoscript.Syntax.Expression.General.BinaryExpression;
import in.wilsonl.nanoscript.Syntax.Expression.General.UnaryExpression;
import in.wilsonl.nanoscript.Syntax.Expression.IdentifierExpression;
import in.wilsonl.nanoscript.Syntax.Expression.LambdaExpression;
import in.wilsonl.nanoscript.Syntax.Expression.Literal.LiteralBooleanExpression;
import in.wilsonl.nanoscript.Syntax.Expression.Literal.LiteralNullExpression;
import in.wilsonl.nanoscript.Syntax.Expression.Literal.LiteralNumberExpression;
import in.wilsonl.nanoscript.Syntax.Expression.Literal.LiteralStringExpression;
import in.wilsonl.nanoscript.Syntax.Expression.LookupExpression;
import in.wilsonl.nanoscript.Syntax.Expression.SelfExpression;
import in.wilsonl.nanoscript.Syntax.Statement.BreakStatement;
import in.wilsonl.nanoscript.Syntax.Statement.CaseStatement;
import in.wilsonl.nanoscript.Syntax.Statement.ConditionalBranchesStatement;
import in.wilsonl.nanoscript.Syntax.Statement.ExportStatement;
import in.wilsonl.nanoscript.Syntax.Statement.ExpressionStatement;
import in.wilsonl.nanoscript.Syntax.Statement.ForStatement;
import in.wilsonl.nanoscript.Syntax.Statement.LoopStatement;
import in.wilsonl.nanoscript.Syntax.Statement.NextStatement;
import in.wilsonl.nanoscript.Syntax.Statement.ReturnStatement;
import in.wilsonl.nanoscript.Syntax.Statement.Statement;
import in.wilsonl.nanoscript.Syntax.Statement.ThrowStatement;
import in.wilsonl.nanoscript.Syntax.Statement.VariableDeclarationStatement;

import java.util.Map;
import java.util.TreeMap;

public class ContextHelper {
    private final Context parent;
    // <variables> can be cleared
    private final Map<String, NSData<?>> variables = new TreeMap<>();

    public ContextHelper(Context parent) {
        this.parent = parent;
    }

    public ContextHelper() {
        this(null);
    }

    public static class EvaluationResult {
        // TODO
    }

    public static EvaluationResult evaluateCodeBlock(Context context, CodeBlock codeBlock) {
        for (Statement statement : codeBlock.getBody()) {
            if (statement instanceof BreakStatement) {
                // TODO
            } else if (statement instanceof CaseStatement) {
                // TODO
            } else if (statement instanceof ConditionalBranchesStatement) {
                // TODO
            } else if (statement instanceof ExportStatement) {
                // TODO
            } else if (statement instanceof ExpressionStatement) {
                // TODO
            } else if (statement instanceof ForStatement) {
                // TODO
            } else if (statement instanceof LoopStatement) {
                // TODO
            } else if (statement instanceof NextStatement) {
                // TODO
            } else if (statement instanceof ReturnStatement) {
                // TODO
            } else if (statement instanceof ThrowStatement) {
                // TODO
            } else if (statement instanceof VariableDeclarationStatement) {
                // TODO
            } else {
                throw new InternalStateError("Unknown statement type");
            }
        }
    }

    public static NSData<?> evaluateExpression(Context context, Expression expression) {
        if (expression instanceof LambdaExpression) {
            // TODO
        } else if (expression instanceof AnonymousObjectExpression) {
            // TODO
        } else if (expression instanceof CallExpression) {
            // TODO
        } else if (expression instanceof LookupExpression) {
            // TODO
        } else if (expression instanceof IdentifierExpression) {
            // TODO
        } else if (expression instanceof LiteralBooleanExpression) {
            // TODO
        } else if (expression instanceof LiteralStringExpression) {
            // TODO
        } else if (expression instanceof LiteralNullExpression) {
            // TODO
        } else if (expression instanceof LiteralNumberExpression) {
            // TODO
        } else if (expression instanceof BinaryExpression) {
            // TODO
        } else if (expression instanceof UnaryExpression) {
            // TODO
        } else if (expression instanceof SelfExpression) {
            // TODO
        } else if (expression instanceof ConditionalBranchesExpression) {
            // TODO
        } else {
            throw new InternalStateError("Unknown expression type");
        }
    }

    public NSData<?> getSymbol(String name) {
        NSData<?> nsData = variables.get(name);
        if (nsData == null && parent != null) {
            nsData = parent.getContextSymbol(name);
        }
        return nsData;
    }

    public boolean setSymbol(String name, NSData<?> value) {
        if (!variables.containsKey(name)) {
            return false;
        }
        variables.put(name, value);
        return true;
    }

    public void clear() {
        variables.clear();
    }
}
