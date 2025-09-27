package mod.fuji.module.initializer.evaluator.parser;

import java.util.ArrayList;
import java.util.List;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.document.annotation.Cite;
import mod.fuji.core.document.annotation.ForDeveloper;
import mod.fuji.module.initializer.evaluator.parser.exception.ParserSyntaxException;
import mod.fuji.module.initializer.evaluator.parser.structure.StringRange;
import mod.fuji.module.initializer.evaluator.parser.token.Token;
import mod.fuji.module.initializer.evaluator.parser.token.TokenType;
import org.jetbrains.annotations.NotNull;

@Cite("https://www.cs.cmu.edu/Groups/AI/html/cltl/clm/node1.html")
public class LispParser {

    private static final char EOF_CHARACTER = '\0';

    final @NotNull String input;
    int start;
    int end;
    private List<Token> tokens;

    public LispParser(@NotNull String input) {
        this.input = input;
    }

    public @NotNull List<Token> parse() {
        /* Initialize the parser states. */
        LogUtil.warn("input = {}", input);
        tokens = new ArrayList<>();
        start = 0;
        end = 0;

        /* Parse the form. */
        parseForm();

        /* Check if the input been parsed totally. */
        if (hasUnparsedCharacters()) {
            throw new ParserSyntaxException("Unexpected character at %d".formatted(start));
        }

        /* Return the tokens. */
        return tokens;
    }

    private boolean hasUnparsedCharacters() {
        return start < input.length();
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    private void parseForm() {
        /* Parse form. */
        char peekChar = peekChar();
        LogUtil.warn("parseForm(): peek = {}", peekChar);

        // Remove leading blank characters.
        while (peekChar == ' ') {
            forward();
            endToken();
            peekChar = peekChar();
        }

        if (peekChar == '(') {
            /* Parse list. */
            parseList();
            return;
        } else {
            /* Parse atom. */
            parseAtom();
        }
    }


    @ForDeveloper("Any non-list is atom.")
    private void parseAtom() {
        parseNumber();
        parseSymbol();
    }

    private void parseNumber() {
        char peek = peekChar();

        /* Check if there is leading sign character. */
        boolean seenLeadingSignCharacter = false;
        if (isSignCharacter(peek)) {
            seenLeadingSignCharacter = true;
            forward();
        }

        while ((peek = peekChar()) != EOF_CHARACTER) {

            if (isSignCharacter(peek)) {
                // Contains more than 1 sign characters, this is not a number token.
                return;
            }

            if (!isNumberCharacter(peek)) {
                break;
            }

            forward();
        }

        String tokenString = getTokenString();
        if (!tokenString.isEmpty()) {
            if (seenLeadingSignCharacter && tokenString.length() == 1) {
                return;
            }
            appendToken(TokenType.NUMBER);
        }
    }

    private boolean isSignCharacter(char ch) {
        return ch == '-' || ch == '+';
    }

    private boolean isNumberCharacter(char ch) {
        if (ch >= '0' && ch <= '9') return true;

        return false;
    }

    private void parseSymbol() {
        char peek;
        while ((peek = peekChar()) != EOF_CHARACTER) {
            if (peek == ':') {
                throw new ParserSyntaxException("Colon character are banned in symbol name, at %d".formatted(end));
            }

            if (peek == '(' || peek == ')' || peek == ' ') {
                break;
            }

            forward();
        }

        if (!isEmptyString()) {
            appendToken(TokenType.SYMBOL);
        }
    }

    private void forward(int distance) {
        end += distance;
    }

    private void forward() {
        forward(1);
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    private void parseList() {
        if (peekChar() == '(') {
            forward();
            appendToken(TokenType.OPEN_PARENTHESES);


            do {
                if (!hasUnparsedCharacters()) {
                    throw new ParserSyntaxException("Missing closed parenthesis after index %d".formatted(end));
                }

                parseForm();
            } while (peekChar() != ')');

            forward();
            appendToken(TokenType.CLOSED_PARENTHESES);

            return;
        } else {
            throw new ParserSyntaxException("Expected an open-parenthesis at index %d".formatted(end));
        }

    }

    private char peekChar() {
        if (end >= input.length()) {
            return EOF_CHARACTER;
        }

        return input.charAt(end);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isEmptyString() {
        return start == end;
    }

    private void appendToken(@NotNull TokenType tokenType) {
        String stringText = getTokenString();
        Token token = makeToken(tokenType, stringText);
        endToken();
        tokens.add(token);
    }

    private @NotNull String getTokenString() {
        return input.substring(start, end);
    }

    private void endToken() {
        start = end;
    }

    private @NotNull Token makeToken(@NotNull TokenType tokenType, @NotNull String stringText) {
        StringRange stringRange = StringRange.of(start, end);
        return Token.of(tokenType, stringRange, stringText);
    }

}
