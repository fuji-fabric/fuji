package mod.fuji.module.initializer.evaluator.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.document.annotation.ForDeveloper;
import mod.fuji.module.initializer.evaluator.reader.exception.LispReaderException;
import mod.fuji.module.initializer.evaluator.reader.structure.StringRange;
import mod.fuji.module.initializer.evaluator.reader.token.Token;
import mod.fuji.module.initializer.evaluator.reader.token.TokenType;
import org.jetbrains.annotations.NotNull;

@ForDeveloper("""
    The reference resource can be read in:
    1. https://www.cs.cmu.edu/Groups/AI/html/cltl/clm/node1.html
    2. https://groups.csail.mit.edu/mac/ftpdir/scheme-7.4/doc-html/scheme_toc.html
    """)
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
        /* Initialize the reader states. */
        this.input = input;
        LogUtil.debug("input = {}", input);
        tokens = new ArrayList<>();
        start = 0;
        end = 0;
    }

    public @NotNull List<Token> read() {
        /* Read the form. */
        readForm();

        /* Check if the input been read totally. */
        while (Character.isWhitespace(peekChar())) {
            forward();
        }
        beginToken();

        // FIXME: read() loop
        if (hasUnreadCharacters()) {
            throw new LispReaderException("Unexpected character at %d".formatted(start));
        }

        /* Return the tokens. */
        return tokens;
    }

    private boolean hasUnreadCharacters() {
        return start < input.length();
    }

    private void readForm() {
        /* Stipe leading blank characters. */
        char peekChar = peekChar();
        LogUtil.debug("readForm(): peek = {}", peekChar);
        while (Character.isWhitespace(peekChar)) {
            forward();
            peekChar = peekChar();
        }
        beginToken();

        /* Atom or List? */
        if (peekChar == '(') {
            /* Read list. */
            readList();
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
        boolean seenDecimalPointCharacter = false;
        while ((peek = peekChar()) != EOF_CHARACTER) {

            if (isSignCharacter(peek)) {
                // Contains more than 1 sign characters, this is not a number token.
                return;
            }

            if (peek == '.') {
                if (seenDecimalPointCharacter) {
                    // Contains more than 1 floating point characters, this is not a number token.
                    return;
                }

                seenDecimalPointCharacter = true;
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
            throw new LispReaderException("Unclosed string after %d".formatted(end));
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
                    throw new LispReaderException("Missing closed parenthesis after index %d".formatted(end));
                }

                readForm();
            } while (peekChar() != ')');

            forward();
            emitToken(TokenType.END_LIST);
            return;
        } else {
            throw new LispReaderException("Expected an open-parenthesis at index %d".formatted(end));
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
