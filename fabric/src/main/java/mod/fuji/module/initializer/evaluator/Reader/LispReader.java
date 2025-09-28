package mod.fuji.module.initializer.evaluator.Reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.document.annotation.Cite;
import mod.fuji.core.document.annotation.ForDeveloper;
import mod.fuji.module.initializer.evaluator.Reader.exception.LispSyntaxException;
import mod.fuji.module.initializer.evaluator.Reader.structure.StringRange;
import mod.fuji.module.initializer.evaluator.Reader.token.Token;
import mod.fuji.module.initializer.evaluator.Reader.token.TokenType;
import org.jetbrains.annotations.NotNull;

@Cite("https://www.cs.cmu.edu/Groups/AI/html/cltl/clm/node1.html")
public class LispReader {

    private static final char EOF_CHARACTER = '\0';
    private static final Set<Character> ATOM_END_CHARACTERS = Set.of(
        '(',
        ')',
        '\'',
        ';',
        '"',
        '|',
        '#',
        '`',
        ',',
        ':',
        ' '
    );

    final @NotNull String input;
    int start;
    int end;
    private List<Token> tokens;

    public LispReader(@NotNull String input) {
        this.input = input;
    }

    public @NotNull List<Token> read() {
        /* Initialize the reader states. */
        LogUtil.warn("input = {}", input);
        tokens = new ArrayList<>();
        start = 0;
        end = 0;

        /* Read the form. */
        readForm();

        /* Check if the input been read totally. */
        if (hasUnreadCharacters()) {
            throw new LispSyntaxException("Unexpected character at %d".formatted(start));
        }

        /* Return the tokens. */
        return tokens;
    }

    private boolean hasUnreadCharacters() {
        return start < input.length();
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    private void readForm() {
        /* Read form. */
        char peekChar = peekChar();
        LogUtil.warn("readForm(): peek = {}", peekChar);

        // Remove leading blank characters.
        while (peekChar == ' ') {
            forward();
            beginToken();
            peekChar = peekChar();
        }

        if (peekChar == '(') {
            /* Read list. */
            readList();
            return;
        } else {
            /* Read atom. */
            readAtom();
        }
    }


    @ForDeveloper("""
        Any non-list is atom.

        Call the reader functions in the order that:
        1. If the distinguish chars have no intersection, call them at arbitrary order.
        2. Call them in sub-set order.
        """)
    private void readAtom() {
        readNumber();
        readString();

        readSymbol();
    }

    private void readNumber() {
        char peek = peekChar();

        /* Check if there is leading sign character. */
        boolean seenLeadingSignCharacter = false;
        if (isSignCharacter(peek)) {
            seenLeadingSignCharacter = true;
            forward();
        }

        /* Read a number. */
        boolean seenFloatingPointCharacter = false;
        while ((peek = peekChar()) != EOF_CHARACTER) {

            if (isSignCharacter(peek)) {
                // Contains more than 1 sign characters, this is not a number token.
                return;
            }

            if (peek == '.') {
                if (seenFloatingPointCharacter) {
                    // Contains more than 1 floating point characters, this is not a number token.
                    return;
                }

                seenFloatingPointCharacter = true;
                forward();
                continue;
            }

            if (!isNumberCharacter(peek)) {
                if (!ATOM_END_CHARACTERS.contains(peek)) {
                    // This is a symbol whose name starts with a number.
                    return;
                }

                break;
            }

            forward();
        }

        /* Append the token. */
        String tokenString = getTokenString();
        if (!tokenString.isEmpty()) {
            if (seenLeadingSignCharacter && tokenString.length() == 1) {
                return;
            }
            emitToken(TokenType.NUMBER);
        }
    }

    private void readString() {
        char peek = peekChar();

        if (peek == '"') {
            forward();
        } else {
            return;
        }

        boolean stringClosed = false;
        while ((peek = peekChar()) != EOF_CHARACTER) {

            if (previousChar() == '\\') {
                forward();
                continue;
            }

            if (peek == '"') {
                stringClosed = true;
                forward();
                break;
            }

            forward();
        }

        if (!stringClosed) {
            throw new LispSyntaxException("Unclosed string after %d".formatted(end));
        }

        if (!isTokenStringEmpty()) {
            emitToken(TokenType.STRING);
        }
    }

    private boolean isSignCharacter(char ch) {
        return ch == '-' || ch == '+';
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean isNumberCharacter(char ch) {
        if (ch >= '0' && ch <= '9') return true;

        return false;
    }

    private void readSymbol() {
        char peek;
        while ((peek = peekChar()) != EOF_CHARACTER) {
            if (ATOM_END_CHARACTERS.contains(peek)) {
                break;
            }

            forward();
        }

        if (!isTokenStringEmpty()) {
            emitToken(TokenType.SYMBOL);
        }
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    private void readList() {
        if (peekChar() == '(') {
            forward();
            emitToken(TokenType.BEGIN_LIST);


            do {
                if (!hasUnreadCharacters()) {
                    throw new LispSyntaxException("Missing closed parenthesis after index %d".formatted(end));
                }

                readForm();
            } while (peekChar() != ')');

            forward();
            emitToken(TokenType.END_LIST);

            return;
        } else {
            throw new LispSyntaxException("Expected an open-parenthesis at index %d".formatted(end));
        }

    }


    @SuppressWarnings("SameParameterValue")
    private void forward(int distance) {
        end += distance;
    }

    private void forward() {
        forward(1);
    }

    private char peekChar() {
        if (end >= input.length()) {
            return EOF_CHARACTER;
        }

        return input.charAt(end);
    }

    private char previousChar() {
        return input.charAt(end - 1);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isTokenStringEmpty() {
        return start == end;
    }

    private void emitToken(@NotNull TokenType tokenType) {
        Token token = makeToken(tokenType);
        tokens.add(token);

        beginToken();
    }

    private void beginToken() {
        start = end;
    }

    private @NotNull String getTokenString() {
        return input.substring(start, end);
    }

    private @NotNull Token makeToken(@NotNull TokenType tokenType) {
        StringRange stringRange = StringRange.of(start, end);
        String tokenString = getTokenString();
        return Token.of(tokenType, stringRange, tokenString);
    }

}
