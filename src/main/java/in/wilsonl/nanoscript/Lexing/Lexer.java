package in.wilsonl.nanoscript.Lexing;

import in.wilsonl.nanoscript.Exception.InternalStateError;
import in.wilsonl.nanoscript.Parsing.Token;
import in.wilsonl.nanoscript.Parsing.TokenType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static in.wilsonl.nanoscript.Parsing.TokenType.*;

public class Lexer {
  private static final char COMMENT_DELIMITER = '"';

  private static final AcceptableChars WHITESPACE = new AcceptableChars('\r', '\n', '\t', ' ');

  private static final AcceptableChars LITERAL_NUMBER_DECIMAL = new AcceptableChars("0123456789");
  private static final AcceptableChars LITERAL_NUMBER_BIN = new AcceptableChars("01");
  private static final AcceptableChars LITERAL_NUMBER_OCTAL = new AcceptableChars("01234567");
  private static final AcceptableChars LITERAL_NUMBER_HEX = new AcceptableChars("0123456789abcdefABCDEF");
  private static final AcceptableChars LITERAL_NUMBER_SCIENTIFIC = new AcceptableChars("Ee");
  private static final AcceptableChars LITERAL_NUMBER_BASE_DELIMITER = new AcceptableChars('x', 'o', 'b');

  private static final AcceptableChars LITERAL_TEMPLATE_SPECIAL = new AcceptableChars('\\', '`');

  private static final AcceptableChars IDENTIFIER_STARTER = new AcceptableChars("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_$");
  private static final AcceptableChars IDENTIFIER = new AcceptableChars("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_$0123456789");

  private static final Map<String, TokenType> SPECIAL_IDENTIFIER = _createSpecialIdentifierMap();
  private static final OperatorTreeNode OPERATOR_TREE_ROOT_NODE = _createOperatorTreeRootNode();

  private final Code code;

  private final Deque<Token> preemptivelyLexedTokens = new ArrayDeque<>();

  public Lexer (Code code) {
    this.code = code;
  }

  private static OperatorTreeNode _createOperatorTreeRootNode () {
    Map<String, TokenType> sequences = new HashMap<>();

    sequences.put("...", T_ELLIPSIS);
    sequences.put("#", T_HASH);
    sequences.put("(", T_LEFT_PARENTHESIS);
    sequences.put(")", T_RIGHT_PARENTHESIS);
    sequences.put("[", T_LEFT_SQUARE_BRACKET);
    sequences.put("]", T_RIGHT_SQUARE_BRACKET);
    sequences.put("<", T_LEFT_CHEVRON);
    sequences.put(">", T_RIGHT_CHEVRON);
    sequences.put("@{", T_OBJ_LITERAL_PREFIX);
    sequences.put("{", T_LEFT_BRACE);
    sequences.put("}", T_RIGHT_BRACE);

    sequences.put("<-", T_LEFT_ARROW);
    sequences.put("->", T_RIGHT_ARROW);
    sequences.put(".", T_DOT);
    sequences.put("?.", T_QUESTION_AND_DOT);
    sequences.put("?[", T_QUESTION_AND_LEFT_SQUARE_BRACKET);
    sequences.put("?(", T_QUESTION_AND_LEFT_PARENTHESIS);
    sequences.put("??", T_QUESTION_AND_QUESTION);

    // Arithmetic
    sequences.put("+", T_PLUS);
    sequences.put("-", T_HYPHEN_MINUS);
    sequences.put("*", T_ASTERISK);
    sequences.put("/", T_FORWARD_SLASH);
    sequences.put("^", T_CARET);
    sequences.put("%", T_PERCENT);

    // Bitwise
    sequences.put("~", T_TILDE);
    sequences.put("&", T_AMPERSAND);
    sequences.put("|", T_PIPE);
    sequences.put("\\", T_BACKSLASH);
    sequences.put("<<", T_LEFT_CHEVRON_AND_LEFT_CHEVRON);
    sequences.put(">>", T_RIGHT_CHEVRON_AND_RIGHT_CHEVRON);
    sequences.put(">>>", T_RIGHT_CHEVRON_AND_RIGHT_CHEVRON_AND_RIGHT_CHEVRON);

    // Relation
    sequences.put("==", T_EQUALS_AND_EQUALS);
    sequences.put("~=", T_TILDE_AND_EQUALS);
    sequences.put("<=", T_LEFT_CHEVRON_AND_EQUALS);
    sequences.put(">=", T_RIGHT_CHEVRON_AND_EQUALS);
    sequences.put("<=>", T_LEFT_CHEVRON_AND_EQUALS_AND_RIGHT_CHEVRON);

    sequences.put("?", T_QUESTION);
    sequences.put(":", T_COLON);
    sequences.put(",", T_COMMA);
    sequences.put(";", T_SEMICOLON);

    OperatorTreeNode rootNode = new OperatorTreeNode();

    for (Map.Entry<String, TokenType> operator : sequences.entrySet()) {
      rootNode.addSequence(operator
        .getKey()
        .toCharArray(), 0, operator.getValue());
    }

    return rootNode;
  }

  private static Map<String, TokenType> _createSpecialIdentifierMap () {
    Map<String, TokenType> map = new HashMap<>();

    map.put("from", T_KEYWORD_FROM);
    map.put("import", T_KEYWORD_IMPORT);
    map.put("export", T_KEYWORD_EXPORT);
    map.put("as", T_KEYWORD_AS);

    map.put("begin", T_KEYWORD_BEGIN);
    map.put("end", T_KEYWORD_END);

    map.put("class", T_KEYWORD_CLASS);
    map.put("endclass", T_KEYWORD_CLASS_END);

    map.put("fn", T_KEYWORD_FUNCTION);
    map.put("endfn", T_KEYWORD_FUNCTION_END);
    map.put("variable", T_KEYWORD_VARIABLE);
    map.put("method", T_KEYWORD_METHOD);
    map.put("endmethod", T_KEYWORD_METHOD_END);
    map.put("constructor", T_KEYWORD_CONSTRUCTOR);
    map.put("endconstructor", T_KEYWORD_CONSTRUCTOR_END);

    map.put("optional", T_KEYWORD_OPTIONAL);
    map.put("self", T_KEYWORD_SELF);
    map.put("static", T_KEYWORD_STATIC);
    map.put("final", T_KEYWORD_FINAL);
    map.put("sealed", T_KEYWORD_SEALED);

    map.put("create", T_KEYWORD_CREATE);
    map.put("set", T_KEYWORD_SET);
    map.put("to", T_KEYWORD_TO);

    map.put("throw", T_KEYWORD_THROW);

    map.put("true", T_LITERAL_BOOLEAN);
    map.put("false", T_LITERAL_BOOLEAN);
    map.put("null", T_LITERAL_NULL);

    map.put("and", T_KEYWORD_AND);
    map.put("or", T_KEYWORD_OR);
    map.put("not", T_KEYWORD_NOT);
    map.put("in", T_KEYWORD_IN);
    map.put("is", T_KEYWORD_IS);
    map.put("ascends", T_KEYWORD_ASCENDS);
    map.put("descends", T_KEYWORD_DESCENDS);

    map.put("if", T_KEYWORD_IF);
    map.put("then", T_KEYWORD_THEN);
    map.put("elseif", T_KEYWORD_ELSEIF);
    map.put("otherwise", T_KEYWORD_OTHERWISE);
    map.put("endif", T_KEYWORD_ENDIF);
    map.put("elif", T_KEYWORD_ELIF);
    map.put("else", T_KEYWORD_ELSE);
    map.put("fi", T_KEYWORD_FI);

    map.put("enduntil", T_KEYWORD_UNTIL_END);
    map.put("until", T_KEYWORD_UNTIL);
    map.put("endwhile", T_KEYWORD_WHILE_END);
    map.put("while", T_KEYWORD_WHILE);
    map.put("before", T_KEYWORD_BEFORE);
    map.put("after", T_KEYWORD_AFTER);

    map.put("for", T_KEYWORD_FOR);
    map.put("endfor", T_KEYWORD_FOR_END);
    map.put("do", T_KEYWORD_DO);

    map.put("case", T_KEYWORD_CASE);
    map.put("endcase", T_KEYWORD_CASE_END);
    map.put("when", T_KEYWORD_WHEN);

    map.put("return", T_KEYWORD_RETURN);
    map.put("break", T_KEYWORD_BREAK);
    map.put("next", T_KEYWORD_NEXT);

    map.put("try", T_KEYWORD_TRY);
    map.put("catch", T_KEYWORD_CATCH);
    map.put("endtry", T_KEYWORD_TRY_END);

    map.put("typeof", T_KEYWORD_TYPEOF);

    return map;
  }

  private Token constructToken (TokenType type) {
    return new Token(type, code.getCurrentPosition());
  }

  private Token constructToken (TokenType type, String value) {
    return new Token(type, value, code.getCurrentPosition());
  }

  private int requireValidHex (String value) {
    try {
      return Integer.parseInt(value, 16);
    } catch (NumberFormatException nfe) {
      throw code.constructMalformedSyntaxException("Invalid hexadecimal value");
    }
  }

  public Token lex () {
    if (!preemptivelyLexedTokens.isEmpty()) {
      return preemptivelyLexedTokens.removeFirst();
    }

    char nextChar = code.peek();

    Token t;

    if (nextChar == COMMENT_DELIMITER) {
      lexComment();
      t = null;
    } else if (LITERAL_NUMBER_DECIMAL.has(nextChar) || nextChar == '-' && LITERAL_NUMBER_DECIMAL.has(code.peek(2))) {
      t = lexLiteralNumber();
    } else if (OPERATOR_TREE_ROOT_NODE.hasChild(nextChar)) {
      TokenType tokenType = OPERATOR_TREE_ROOT_NODE.match(code);
      if (tokenType == null) {
        throw code.constructMalformedSyntaxException("Invalid syntax");
      }
      t = constructToken(tokenType);
    } else if (nextChar == '`') {
      t = lexLiteralString();
    } else if (IDENTIFIER_STARTER.has(nextChar)) {
      t = lexIdentifier();
    } else if (WHITESPACE.has(nextChar)) {
      code.skipGreedyBeforeEnd(WHITESPACE);
      t = null;
    } else {
      throw code.constructMalformedSyntaxException("Unknown syntax");
    }

    if (t != null) {
      switch (t.getType()) {
      case T_RIGHT_SQUARE_BRACKET:
      case T_LITERAL_STRING:
      case T_RIGHT_PARENTHESIS:
      case T_IDENTIFIER:
        // Note that it's not the next token type, but rather the direct
        // character adjacent to the current character, including whitespace
        switch (code.peek()) {
        case '(':
          if (t.getType() != T_LITERAL_STRING) {
            code.skip();
            preemptivelyLexedTokens.addLast(constructToken(T_CALL));
          }
          break;

        case '[':
          code.skip();
          preemptivelyLexedTokens.addLast(constructToken(T_LOOKUP));
          break;
        }
        break;

      default:
        // No action necessary
        break;
      }
    }

    return t;
  }

  // Called when next char is '-' or a digit
  private Token lexLiteralNumber () {
    StringBuilder value = new StringBuilder(code.acceptOptional('-'));

    if (code.peek(1) == '0' && LITERAL_NUMBER_BASE_DELIMITER.has(code.peek(2))) {
      code.skip();
      int base;
      String raw;
      switch (code.accept()) {
      case 'x':
        raw = code.acceptGreedy(LITERAL_NUMBER_HEX);
        base = 16;
        break;

      case 'o':
        raw = code.acceptGreedy(LITERAL_NUMBER_OCTAL);
        base = 8;
        break;

      case 'b':
        raw = code.acceptGreedy(LITERAL_NUMBER_BIN);
        base = 2;
        break;

      default:
        throw new InternalStateError("Unknown number literal base");
      }

      try {
        value.append(Long.parseLong(raw, base));
      } catch (NumberFormatException nfe) {
        throw code.constructMalformedSyntaxException("Invalid literal number in base " + base);
      }
    } else {
      StringBuilder raw = new StringBuilder();

      raw
        .append(code.acceptGreedy(LITERAL_NUMBER_DECIMAL))
        .append(code.acceptOptional('.'))
        .append(code.acceptGreedy(LITERAL_NUMBER_DECIMAL))
        .append(code.acceptOptional(LITERAL_NUMBER_SCIENTIFIC))
        .append(code.acceptOptional('-'))
        .append(code.acceptGreedy(LITERAL_NUMBER_DECIMAL));

      try {
        value.append(Double.parseDouble(raw.toString()));
      } catch (NumberFormatException nfe) {
        throw code.constructMalformedSyntaxException("Invalid literal number");
      }
    }

    return constructToken(T_LITERAL_NUMBER, value.toString());
  }

  // This is called when starting literal (at char '`')
  private Token lexLiteralString () {
    code.skip();

    StringBuilder value = new StringBuilder();

    while (true) {
      value.append(code.acceptUntil(LITERAL_TEMPLATE_SPECIAL));

      char c1 = code.accept();

      switch (c1) {
      case '\\':
        char c2 = code.accept();

        switch (c2) {
        case '`':
        case '\\':
          value.append(c2);
          break;

        case 'x':
          if (code.peek() == '{') {
            code.skip();
            String hexValue = code.acceptGreedy(LITERAL_NUMBER_HEX);
            if (code.accept() != '}') {
              throw code.constructMalformedSyntaxException("Invalid hex code point string escape");
            }
            // Exception will be thrown if not valid hex
            value.append(Character.toChars(requireValidHexCodePoint(hexValue)));
          } else {
            value
              .append(c1)
              .append(c2);
          }
          break;

        default:
          value
            .append(c1)
            .append(c2);
        }
        break;

      case '`':
        return constructToken(T_LITERAL_STRING, value.toString());

      default:
        throw new InternalStateError(String.format("Unrecognised literal string character: %#x", (int) c1));
      }
    }

  }

  // Called when next char is in IDENTIFIER_STARTER
  private Token lexIdentifier () {
    String value = code.acceptGreedy(IDENTIFIER);

    if (SPECIAL_IDENTIFIER.containsKey(value)) {
      TokenType specialIdentifierType = SPECIAL_IDENTIFIER.get(value);
      String specialIdentifierValue = null;
      if (specialIdentifierType == T_LITERAL_BOOLEAN) {
        specialIdentifierValue = value;
      }
      return constructToken(specialIdentifierType, specialIdentifierValue);
    } else {
      return constructToken(T_IDENTIFIER, value);
    }
  }

  private int requireValidHexCodePoint (String value) {
    int intval = requireValidHex(value);
    if (intval < 0 || intval > 0x10ffff) {
      throw code.constructMalformedSyntaxException("Invalid hexadecimal code point");
    }
    return intval;
  }

  // Called when next char is '"'
  private void lexComment () {
    char terminator = code.accept();
    int terminatorCount = 1;
    while (code.skipIfNext(terminator)) {
      terminatorCount++;
    }

    while (true) {
      code.skipUntil(terminator);
      code.skip();

      int endTerminatorCount = 1;
      while (endTerminatorCount < terminatorCount) {
        if (code.skipIfNext(terminator)) {
          endTerminatorCount++;
        } else {
          break;
        }
      }

      if (endTerminatorCount == terminatorCount) {
        break;
      }
    }
  }
}
