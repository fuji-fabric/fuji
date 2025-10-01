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

    final @NotNull String input;
    int start;
    int end;
    final List<Token> tokens;

    public LispReader(@NotNull String input) {
        /* Initialize the reader states. */
        this.input = input;
        this.tokens = new ArrayList<>();
        this.start = 0;
        this.end = 0;
    }

    public @NotNull List<Token> read() {
        /* Read the form. */
        readForm();

        /* Check if the input been read totally. */
        while (Character.isWhitespace(peek())) {
            forward();
        }
        syncStart();

        // FIXME: read() loop
        if (hasNext()) {
            throw new LispReaderException("Unexpected character at %d".formatted(start));
        }

        /* Return the tokens. */
        return tokens;
    }

    private boolean hasNext() {
        return start < input.length();
    }

    private void readForm() {
        /* Stipe leading blank characters. */
        char peekChar = peek();
        LogUtil.debug("readForm(): peek = {}", peekChar);
        while (Character.isWhitespace(peekChar)) {
            forward();
            peekChar = peek();
        }
        syncStart();

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
        char peek = peek();

        /* Check if there is leading sign character. */
        boolean seenLeadingSignCharacter = false;
        if (CharacterKind.isSignCharacter(peek)) {
            seenLeadingSignCharacter = true;
            forward();
        }

        /* Read a number. */
        boolean seenDecimalPointCharacter = false;
        while ((peek = peek()) != CharacterKind.EOF_CHARACTER) {

            if (CharacterKind.isSignCharacter(peek)) {
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

            if (!CharacterKind.isNumberCharacter(peek)) {
                if (!CharacterKind.isAtomDelimiterCharacter(peek)) {
                    // This is a symbol whose name starts with a number.
                    return;
                }

                break;
            }

            forward();
        }

        /* Append the token. */
        String tokenString = select();
        if (!tokenString.isEmpty()) {
            if (seenLeadingSignCharacter && tokenString.length() == 1) {
                return;
            }
            emit(TokenType.NUMBER);
        }
    }

    private void readString() {
        char peek = peek();

        if (peek == '"') {
            forward();
        } else {
            return;
        }

        boolean stringClosed = false;
        while ((peek = peek()) != CharacterKind.EOF_CHARACTER) {

            if (previous() == '\\') {
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

        if (selectAny()) {
            emit(TokenType.STRING);
        }
    }

    private void readSymbol() {
        char peek;
        while ((peek = peek()) != CharacterKind.EOF_CHARACTER) {
            if (CharacterKind.isAtomDelimiterCharacter(peek)) {
                break;
            }

            forward();
        }

        if (selectAny()) {
            emit(TokenType.SYMBOL);
        }
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    private void readList() {
        if (peek() == '(') {
            forward();
            emit(TokenType.BEGIN_LIST);

            do {
                if (!hasNext()) {
                    throw new LispReaderException("Missing closed parenthesis after index %d".formatted(end));
                }

                readForm();
            } while (peek() != ')');

            forward();
            emit(TokenType.END_LIST);
            return;
        } else {
            throw new LispReaderException("Expected an open-parenthesis at index %d".formatted(end));
        }

    }

    private void forward() {
        end++;
    }

    private char peek() {
        if (end >= input.length()) {
            return CharacterKind.EOF_CHARACTER;
        }

        return input.charAt(end);
    }

    private char previous() {
        return input.charAt(end - 1);
    }

    private boolean selectAny() {
        return start != end;
    }

    private void emit(@NotNull TokenType tokenType) {
        Token token = makeToken(tokenType);
        tokens.add(token);
        syncStart();
    }

    private void syncStart() {
        start = end;
    }

    private @NotNull String select() {
        return input.substring(start, end);
    }

    private @NotNull Token makeToken(@NotNull TokenType tokenType) {
        StringRange stringRange = StringRange.of(start, end);
        String tokenString = select();
        return Token.of(tokenType, stringRange, tokenString);
    }

    private static class CharacterKind {

        private static final char EOF_CHARACTER = '\0';
        private static final Set<Character> ATOM_DELIMITER_CHARACTERS = Set.of(
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

        private static boolean isSignCharacter(char ch) {
            return ch == '-' || ch == '+';
        }

        @SuppressWarnings("RedundantIfStatement")
        private static boolean isNumberCharacter(char ch) {
            if (ch >= '0' && ch <= '9') return true;

            return false;
        }

        private static boolean isAtomDelimiterCharacter(char peek) {
            return ATOM_DELIMITER_CHARACTERS.contains(peek);
        }
    }
}
