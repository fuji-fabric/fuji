package mod.fuji.module.initializer.evaluator.evaluator.compiler;

import java.util.List;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNumber;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispString;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispSymbol;
import mod.fuji.module.initializer.evaluator.reader.structure.StringRange;
import mod.fuji.module.initializer.evaluator.reader.token.Token;
import mod.fuji.module.initializer.evaluator.reader.token.TokenType;
import org.jetbrains.annotations.NotNull;

public class LispCompiler {

    final List<Token> AST;
    int start;
    int end;
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
//            if (!hasUncompiledTokens()) {
//                throw new LispCompilationException("Missing more tokens.");
//            }

            compileTokenStream();

        } while (peek().getTokenType() != TokenType.END_LIST);

        /* Consume END_LIST token. */
        forward();
        this.parent = savedParent;
        emit(result);
    }

    private void compileAtom() {
        Token peek = peek();
        if (isSelfEvaluatingObject(peek)) {
            compileSelfEvaluatingObject();
        } else if (peek.getTokenType().equals(TokenType.SYMBOL)) {
            compileSymbol();
        }
    }

    private void compileSymbol() {
        Token peek = peek();
        forward();

        LispSymbol node = LispSymbol.of(peek.getTokenContent());
        emit(node);
    }

    private void compileSelfEvaluatingObject() {
        compileNumber();
        compileString();
    }

    private void compileNumber() {
        Token peek = peek();
        if (!peek.getTokenType().equals(TokenType.NUMBER)) return;
        forward();

        String tokenContent = peek.getTokenContent();
        double value = Double.parseDouble(tokenContent);
        LispNumber node = LispNumber.of(value);
        emit(node);
    }

    private void compileString() {
        Token peek = peek();
        if (!peek.getTokenType().equals(TokenType.STRING)) return;
        forward();

        String tokenContent = peek.getTokenContent();
        String value = unescapeString(tokenContent);
        LispString node = LispString.of(value);
        emit(node);
    }

    private @NotNull String unescapeString(@NotNull String tokenContent) {
        return tokenContent;
    }

    private boolean isSelfEvaluatingObject(@NotNull Token token) {
        TokenType tokenType = token.getTokenType();
        return tokenType.equals(TokenType.NUMBER)
            || tokenType.equals(TokenType.STRING);
    }

    private void emit(@NotNull LispObject node) {
        this.parent.getObjects().add(node);
        syncStart();
    }

    private void syncStart() {
        this.start = this.end;
    }

    private void forward() {
        this.end++;
    }

    private @NotNull Token peek() {
        if (end >= AST.size()) {
            return TokenKind.EOF_TOKEN;
        }

        return AST.get(end);
    }

//    private boolean hasUncompiledTokens() {
//        return this.start < this.AST.size();
//    }

//    private boolean isSelectionEmpty() {
//        return this.start == this.end;
//    }

//    private @NotNull List<Token> select() {
//        return AST.subList(this.start, this.end);
//    }

    private static class TokenKind {
        private static final Token EOF_TOKEN = Token.of(TokenType.EOF, StringRange.of(-1, -1), "DUMMY EOF TOKEN");

    }

}
