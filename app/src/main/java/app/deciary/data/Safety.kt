package app.deciary.data

/**
 * A light check for words that suggest someone may be thinking about hurting themselves.
 * When it matches, the app offers a helpline and switches off challenge questions for that letter.
 * It never blocks saving.
 */
object Safety {
    private val phrases = listOf(
        "kill myself", "killing myself", "end my life", "ending my life", "end it all",
        "suicide", "suicidal", "hurt myself", "hurting myself", "harm myself", "self harm", "self-harm",
        "don't want to live", "dont want to live", "no reason to live", "better off dead", "want to die",
    )

    fun needsCare(vararg texts: String): Boolean {
        val all = texts.joinToString(" ").lowercase().replace('’', '\'')
        return phrases.any { it in all }
    }

    const val HELPLINE_NAME = "Tele-MANAS"
    const val HELPLINE_NUMBER = "14416"
}
