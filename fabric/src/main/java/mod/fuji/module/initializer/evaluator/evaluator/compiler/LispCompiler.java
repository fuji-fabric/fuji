package mod.fuji.module.initializer.evaluator.evaluator.compiler;

import com.google.errorprone.annotations.Keep;
import java.util.List;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispListNode;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNode;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispSymbolNode;
import mod.fuji.module.initializer.evaluator.reader.structure.StringRange;
import mod.fuji.module.initializer.evaluator.reader.token.Token;
import mod.fuji.module.initializer.evaluator.reader.token.TokenType;
import org.jetbrains.annotations.NotNull;

public class LispCompiler {

    private static final Token EOF_TOKEN = Token.of(TokenType.EOF, StringRange.of(-1, -1), "DUMMY EOF TOKEN");

    final List<Token> AST;

    @Keep
    int start;
    int end;

    LispListNode parent;

    public LispCompiler(@NotNull List<Token> AST) {
        this.AST = AST;
    }

    public @NotNull LispNode compile() {
        /* Initialize the compiler states. */
        LispListNode rootNode = LispListNode.of();
        this.parent = rootNode;

        /* Call the compiler functions. */
        compileTokenStream();

        /* Return the nodes. */
        return rootNode;
    }

    private void compileTokenStream() {
        Token peek = peek();

        if (peek.getTokenType().equals(TokenType.BEGIN_LIST)) {
            compileListNode();
        } else {
            compileAtomNode();
        }
    }

    private void compileListNode() {
        /* Consume BEGIN_LIST token. */
        forward();
        LispListNode savedParent = this.parent;

        LispListNode result = LispListNode.of();
        this.parent = result;

        /* Consume tokens until the END_LIST token is seen. */
        do {
            LogUtil.warn("peek = {}", peek());
//            if (!hasUncompiledTokens()) {
//                throw new LispCompilationException("Missing more tokens.");
//            }

            compileTokenStream();

        } while (peek().getTokenType() != TokenType.END_LIST);

        /* Consume END_LIST token. */
        forward();
        this.parent = savedParent;
        emitNode(result);
    }

    private void compileAtomNode() {
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

        LispSymbolNode node = LispSymbolNode.of(peek.getTokenContent());
        emitNode(node);
    }

    private void compileSelfEvaluatingObject() {
//        Token peek = peek();
//        forward();
    }

    private boolean isSelfEvaluatingObject(@NotNull Token token) {
        TokenType tokenType = token.getTokenType();
        return tokenType.equals(TokenType.NUMBER)
            || tokenType.equals(TokenType.STRING);
    }

    private void emitNode(@NotNull LispNode node) {
        this.parent.getNodes().add(node);
        beginNode();
    }

    private void beginNode() {
        this.start = this.end;
    }

    private void forward() {
        this.end++;
    }

    private @NotNull Token peek() {
        if (end >= AST.size()) {
            return EOF_TOKEN;
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

}
