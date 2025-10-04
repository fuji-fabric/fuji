package mod.fuji.evaluator.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import mod.fuji.evaluator.auxiliary.LogUtil;
import mod.fuji.evaluator.reader.exception.LispReaderException;
import mod.fuji.evaluator.reader.structure.StringRange;
import mod.fuji.evaluator.reader.token.Token;
import mod.fuji.evaluator.reader.token.TokenType;
import org.jetbrains.annotations.NotNull;

/**
 * Useful resource:
 * <p></p>
 * <ol>
 *     <li><a href="https://www.cs.cmu.edu/Groups/AI/html/cltl/clm/node1.html">Common Lisp the Language</a></li>
 *     <li><a href="https://groups.csail.mit.edu/mac/ftpdir/scheme-7.4/doc-html/scheme_toc.html">Scheme Manual</a></li>
 *     <li><a href="https://www.gnu.org/software/kawa/index.html">GNU Kawa</a></li>
 * </ol>
 **/
public class LispReader extends LispStreamProcessor<Character, String, Token> {

    final @NotNull String input;
    final List<Token> tokens;

    public LispReader(@NotNull String input) {
        super();
        /* Initialize the reader states. */
        this.input = input;
        this.tokens = new ArrayList<>();
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

    @Override
    protected int streamLength() {
        return input.length();
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


    /**
     * Any non-list is atom.
     * <p>
     * Call the reader functions in the order that:
     * 1. If the distinguish chars have no intersection, call them at arbitrary order.
     * 2. Call them in sub-set order.
     **/
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

    private void readList() {
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
    }

    @Override
    protected @NotNull Character peek() {
        if (end >= input.length()) {
            return CharacterKind.EOF_CHARACTER;
        }

        return input.charAt(end);
    }

    @Override
    protected @NotNull Character previous() {
        return input.charAt(end - 1);
    }

    @Override
    protected void emit(@NotNull Token token) {
        tokens.add(token);
        syncStart();
    }

    private void emit(@NotNull TokenType tokenType) {
        Token token = makeToken(tokenType);
        emit(token);
    }

    private @NotNull Token makeToken(@NotNull TokenType tokenType) {
        StringRange stringRange = StringRange.of(start, end);
        String tokenString = select();
        return Token.of(tokenType, stringRange, tokenString);
    }

    @Override
    protected @NotNull String select() {
        return input.substring(start, end);
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
