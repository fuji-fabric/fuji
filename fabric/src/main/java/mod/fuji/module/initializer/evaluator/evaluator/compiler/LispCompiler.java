package mod.fuji.module.initializer.evaluator.evaluator.compiler;

import java.util.List;
import mod.fuji.module.initializer.evaluator.evaluator.compiler.exception.LispCompilationException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNumber;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispString;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispSymbol;
import mod.fuji.module.initializer.evaluator.reader.LispStreamProcessor;
import mod.fuji.module.initializer.evaluator.reader.exception.LispReaderException;
import mod.fuji.module.initializer.evaluator.reader.structure.StringRange;
import mod.fuji.module.initializer.evaluator.reader.token.Token;
import mod.fuji.module.initializer.evaluator.reader.token.TokenType;
import org.jetbrains.annotations.NotNull;

public class LispCompiler extends LispStreamProcessor<Token, List<Token>, LispObject> {

    final List<Token> AST;
    LispList parent;

    public LispCompiler(@NotNull List<Token> AST) {
        this.AST = AST;
    }

    public @NotNull LispList compile() {
        /* Initialize the compiler states. */
        LispList rootNode = LispList.of();
        this.parent = rootNode;

        /* Call the compiler functions. */
        compileTokenStream();

        if (hasNext()) {
            throw new LispReaderException("Unexpected token at %d".formatted(start));
        }

        /* Return the nodes. */
        return rootNode;
    }

    private void compileTokenStream() {
        Token peek = peek();

        if (peek.getTokenType().equals(TokenType.BEGIN_LIST)) {
            compileList();
        } else {
            compileAtom();
        }
    }

    private void compileList() {
        /* Consume BEGIN_LIST token. */
        forward();
        LispList savedParent = this.parent;

        LispList result = LispList.of();
        this.parent = result;

        /* Consume tokens until the END_LIST token is seen. */
        do {
            if (!hasNext()) {
                throw new LispCompilationException("Missing more tokens.");
            }

            compileTokenStream();

        } while (peek().getTokenType() != TokenType.END_LIST);

        /* Consume END_LIST token. */
        forward();
        this.parent = savedParent;
        emit(result);
    }

    private void compileAtom() {
        Token peek = peek();
        TokenType tokenType = peek.getTokenType();

        if (tokenType.equals(TokenType.NUMBER)) {
            compileNumber();
        } else if (tokenType.equals(TokenType.STRING)) {
            compileString();
        } else if (tokenType.equals(TokenType.SYMBOL)) {
            compileSymbol();
        }
    }

    private void compileSymbol() {
        Token peek = peek();
        forward();

        LispSymbol node = LispSymbol.of(peek.getTokenContent());
        emit(node);
    }

    private void compileNumber() {
        Token peek = peek();
        forward();

        String tokenContent = peek.getTokenContent();
        double value = Double.parseDouble(tokenContent);
        LispNumber node = LispNumber.of(value);
        emit(node);
    }

    private void compileString() {
        Token peek = peek();
        forward();

        String tokenContent = peek.getTokenContent();
        String value = unescapeString(tokenContent);
        LispString node = LispString.of(value);
        emit(node);
    }

    private @NotNull String unescapeString(@NotNull String tokenContent) {
        return tokenContent;
    }

    @Override
    protected void emit(@NotNull LispObject node) {
        this.parent.getObjects().add(node);
        syncStart();
    }

    @Override
    protected @NotNull List<Token> select() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int streamLength() {
        return AST.size();
    }

    @Override
    protected @NotNull Token peek() {
        if (end >= AST.size()) {
            return TokenKind.EOF_TOKEN;
        }

        return AST.get(end);
    }

    @Override
    protected @NotNull Token previous() {
        throw new UnsupportedOperationException();
    }

    private static class TokenKind {
        private static final Token EOF_TOKEN = Token.of(TokenType.EOF, StringRange.of(-1, -1), "DUMMY EOF TOKEN");

    }

}
