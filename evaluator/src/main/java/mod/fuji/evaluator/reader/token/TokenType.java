package mod.fuji.evaluator.reader.token;

public enum TokenType {

    // Token types should not include blank characters, they should only be presented inside a STRING type.

    BEGIN_LIST, END_LIST, SYMBOL, STRING, NUMBER, EOF


}
