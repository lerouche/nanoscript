package in.wilsonl.nanoscript.Syntax.Statement;

import in.wilsonl.nanoscript.Exception.InternalStateError;
import in.wilsonl.nanoscript.Parsing.AcceptableTokenTypes;
import in.wilsonl.nanoscript.Parsing.TokenType;
import in.wilsonl.nanoscript.Parsing.Tokens;
import in.wilsonl.nanoscript.Syntax.CodeBlock;
import in.wilsonl.nanoscript.Syntax.Expression.Expression;
import in.wilsonl.nanoscript.Utils.Position;
import in.wilsonl.nanoscript.Utils.ROList;

import java.util.List;

import static in.wilsonl.nanoscript.Parsing.TokenType.*;

public class ConditionalBranchesStatement extends Statement {

  private static final AcceptableTokenTypes BRANCH_BODY_END_DELIMITER = new AcceptableTokenTypes(
    T_KEYWORD_ELSEIF,
    T_KEYWORD_OTHERWISE,
    T_KEYWORD_ENDIF
  );
  private final List<Branch> conditionalBranches = new ROList<>();
  private boolean hasFinalBranch = false;

  public ConditionalBranchesStatement (Position position) {
    super(position);
  }

  public static ConditionalBranchesStatement parseConditionalBranchesStatement (Tokens tokens) {
    Position position = tokens.require(T_KEYWORD_IF).getPosition();
    ConditionalBranchesStatement branches = new ConditionalBranchesStatement(position);

    boolean done = false;
    do {
      Expression condition = Expression.parseExpression(tokens, new AcceptableTokenTypes(T_KEYWORD_THEN));
      tokens.require(T_KEYWORD_THEN);
      CodeBlock body = CodeBlock.parseCodeBlock(tokens, BRANCH_BODY_END_DELIMITER);

      branches.addBranch(condition, body);

      TokenType nextToken = tokens.peekType();

      switch (nextToken) {
      case T_KEYWORD_ELSEIF:
        tokens.skip();
        break;

      case T_KEYWORD_OTHERWISE:
      case T_KEYWORD_ENDIF:
        done = true;
        break;

      default:
        throw new InternalStateError("Unknown token type after parsing conditional branch body: " + nextToken);
      }
    } while (!done);

    if (tokens.skipIfNext(T_KEYWORD_OTHERWISE)) {
      CodeBlock body = CodeBlock.parseCodeBlock(tokens, T_KEYWORD_ENDIF);

      branches.setFinalBranch(body);
    }

    tokens.require(T_KEYWORD_ENDIF);

    return branches;
  }

  public void addBranch (Expression condition, CodeBlock body) {
    conditionalBranches.add(new Branch(condition, body));
  }

  public void setFinalBranch (CodeBlock body) {
    if (hasFinalBranch) {
      throw new InternalStateError("Final branch already set");
    }
    if (body == null) {
      throw new InternalStateError("<body> is null");
    }
    hasFinalBranch = true;
    conditionalBranches.add(new Branch(null, body));
  }

  public List<Branch> getConditionalBranches () {
    return conditionalBranches;
  }

  public static class Branch {
    private final Expression condition; // Can be null
    private final CodeBlock body;

    private Branch (Expression condition, CodeBlock body) {
      this.condition = condition;
      this.body = body;
    }

    public Expression getCondition () {
      return condition;
    }

    public CodeBlock getBody () {
      return body;
    }
  }

}
