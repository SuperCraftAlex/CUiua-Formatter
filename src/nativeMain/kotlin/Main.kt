import kotlinx.cinterop.*
import kotlinx.cinterop.internal.CCall
import platform.posix.EOF
import platform.posix.getchar

enum class Inst(
    val gylph: String,
    vararg val names: String
) {

    FUN_DECL("⟼", "fun", "declare"),

    MODULO("◿", "modulo"),
    COUNT("⍟", "count"),
    ALL("⁁", "all"),
    DUP2("˙", "2dup"),
    XOR("⊻", "xor"),
    AND("∧", "and"),
    OR("∨", "or"),
    ACCURACY("℀", "accuracy"),
    MAKE_FRACTION("⑀", "makefraction"),
    CAST_FRACTION("ℚ", "castfraction"),
    CAST_STRING("𝕐", "caststring"),
    CAST_REAL("ℝ", "castreal"),
    CAST_INTEGER("ℤ", "castinteger"),
    NEWLINE("↪", "newline", "nl"),
    READ_FILE("⇲", "readfile"),
    WRITE_FILE("⇱", "writefile"),
    FRAGMENT("⬡", "fragment"),
    DEARRAY("≡", "dearray"),
    BRACKET("⊓", "bracket"),
    GROUP("⊕", "group"),
    TABLE("⊞", "table"),
    SEPARATE("⍞", "separate"),
    SPLIT("◫", "split"),
    MIN("↧", "minimum"),
    MAX("↥", "maximum"),
    ASSERT("⍤", "assert"),
    GTE(">=", "gte", "greaterequal"),
    GT(">", "gt", "greater"),
    LTE("<=", "lte", "lessequal"),
    LT("<", "lt", "less"),
    NEQ("!=", "neq", "notequal"),
    EQ("=", "eq", "equal"),
    NEGATE("¯", "negate"),
    NOT("¬", "not"),
    NAN("♮", "nan"),
    FALSE("⊥", "false"),
    TRUE("⊤", "true"),
    FOLD("fold", "fold"),
    DISTRIBUTE("∺", "distribute"),
    COUPLE("⊟", "couple"),
    EXACT("⌖", "exact"),
    FIND("⌕", "find"),
    KEEP("▽", "keep"),
    DIP("⊙", "dip"),
    BOTH("∩", "both"),
    INF("∞", "inf"),
    RAND("⚂", "rand"),
    TAU("τ", "tau"),
    ETA("η", "eta"),
    PI("π", "pi"),
    TRACE("~", "trace"),
    SORT_DESC("⍖", "sortdesc"),
    SORT_ASC("⍏", "sortasc"),
    TRANSPOSE("⍉", "transpose"),
    ENUMERATE("⋯", "enumerate"),
    BOX("□", "box"),
    UNBOX("⊔", "unbox"),
    DEDUPLICATE("⊝", "deduplicate"),
    WHERE("⊚", "where"),
    RANGE("⇡", "range"),
    FIRST("⊢", "first"),
    LEN("⧻", "length"),
    TYPE("⚙", "typeof"),
    EMPTY_SET("∅", "emptyset"),
    RESHAPE("↯", "reshape"),
    REPEAT("⍥", "repeat"),
    SHAPE("△", "shape"),
    PUSH("∘", "push"),
    MEMBER("∊", "member"),
    INDEX_OF("⊗", "indexof"),
    PICK("⊡", "pick"),
    SELECT("⊏", "select"),
    JOIN("⊂", "join"),
    MATCH("≅", "match"),
    DESHAPE("♭", "deshape"),
    REVERSE("⇌", "reverse"),
    ROT("↻", "rot"),
    DROP("↘", "drop"),
    TAKE("↙", "take"),
    DIVIDE("÷", "divide"),
    MULTIPLY("×", "multiply"),
    ADD("+", "add"),
    SUBTRACT("-", "subtract"),
    EACH("∵", "each"),
    NOOP("∘", "noop", "nop"),
    FORK("⊃", "fork"),

    CALL("!", "call"),
    SWAP(":", "swap"),
    DUP(".", "duplicate"),
    OVER(",", "over"),
    REDUCE("/", "reduce"),
    SCAN("\\", "scan"),
    IF("?", "if"),


    ;

    companion object {

        // if any instruction starts with the given string, return it (UNLESS there are multiple matches)
        fun fromName(name: String): Inst? {
            val matches = entries.filter { inst -> inst.names.any { it.startsWith(name) } }
            return if (matches.size == 1) matches[0] else null
        }

        // if any instruction starts with the start of the given string, return it (UNLESS there are multiple matches)
        // also returns the amount of characters that were matched
        // example: advancedFromName("minabc") returns (3, MIN)
        fun advancedFromName(text: String): Pair<Int, Inst?> {
            var i = text.length - 1
            while (i >= 0) {
                val sub = text.substring(0, i + 1)
                val inst = fromName(sub)
                if (inst != null) {
                    return Pair(i + 1, inst)
                }
                i--
            }
            return Pair(0, null)
        }

    }
}

data class Token(
    val text: String,
    val type: Type
) {

    enum class Type {
        NUMBER,     // 123.456
        STRING,     // "string"
        CHAR,       // "@" and then a single character
        PAREN,      // "(" or ")"
        BRACKET,    // "[" or "]"
        OPERATOR,   // operator (e.g. "+")
        SPACE,      // space
    }

    override fun toString(): String {
        return "Token($type, \"$text\")"
    }

}

fun lex(code: String): Collection<Token> {
    val tokens = mutableListOf<Token>()
    var i = 0
    while (i < code.length) {
        val c = code[i]
        when {
            c.isWhitespace() -> {
                i++
                tokens += Token(
                    " ",
                    Token.Type.SPACE
                )
            }
            c == '#' -> {
                while (i < code.length && code[i] != '\n') {
                    i++
                }
            }
            c == '"' -> {
                val start = i
                i++
                while (i < code.length && code[i] != '"') {
                    i++
                }
                val end = i
                tokens += Token(
                    code.substring(start, end + 1),
                    Token.Type.STRING
                )
                i++
            }
            c == '@' -> { // single character
                i += 2
                if (i >= code.length) {
                    throw Exception("Expected character after '@'")
                }
                tokens += Token(
                    code.substring(i - 2, i),
                    Token.Type.CHAR
                )
            }
            c == '(' || c == ')' -> {
                tokens += Token(
                    c.toString(),
                    Token.Type.PAREN
                )
                i++
            }
            c == '[' || c == ']' -> {
                tokens += Token(
                    c.toString(),
                    Token.Type.BRACKET
                )
                i++
            }
            c.isDigit() -> {
                val start = i
                i++
                while (i < code.length && code[i].isDigit()) {
                    i++
                }
                val end = i
                tokens += Token(
                    code.substring(start, end),
                    Token.Type.NUMBER
                )
            }
            else -> {
                // until ' ' or '(' or ')' or '[' or ']' or '"' or '#' or 'λ' or digit or '@' or '\n' or end
                val specialChars = listOf(
                    ' ', '(', ')', '[', ']', '"', '#', 'λ', '@', '\n'
                )
                val start = i
                i++
                while (i < code.length && code[i] !in specialChars) {
                    i++
                }
                val end = i
                var text = code.substring(start, end)

                while (text.isNotEmpty()) {
                    val (used, inst) = Inst.advancedFromName(text)
                    if (inst != null) {
                        tokens += Token(
                            inst.gylph,
                            Token.Type.OPERATOR
                        )
                        text = text.substring(used)
                    } else {
                        tokens += Token(
                            text[0].toString(),
                            Token.Type.OPERATOR
                        )
                        text = text.substring(1)
                    }
                }
            }
        }
    }
    return tokens
}

fun main() {
    val sb = CStringBuilder()

    var char: Int
    while (true) {
        char = getchar()
        if (char == EOF) break
        sb += char.toChar()
    }

    val res = lex(sb.toString()).joinToString("") { it.text }

    println(res)

    sb.close()
}