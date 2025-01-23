package hci.project.ads

class TaskHelper {
    fun typingTestPhrases(): List<String> {
        return listOf(
            "vani vlada trepet i tama",
            "svjetlost obasjava trijem",
            "galeb se kupa kod fontane",
            "razbila se kristalna vaza",
            "sjene prate uski puteljak",
            "zalijepi ih u plavi album",
            "hladne kapi klize staklom",
            "tanka se ogrlica potrgala",
            "papir se leluja na vjetru",
            "usidrio se francuski brod",
            "kupila sam lijepi suvenir",
            "nove su grane procvjetale",
            "jato je proletjelo parkom",
            "postavljen je ogroman bor",
            "fina torta je u hladnjaku"
        )
    }

    fun correctOrderPhrases(): List<String> {
        return listOf(
            "ne zaboravi me nazvati",
            "upali svjetiljku na stolu",
            "kupi dva kilograma soli",
            "prisjeti se svoje mladosti",
            "netko hoda na tavanu",
            "posudi mi svoje flomastere",
            "izradi zrakoplov od papira",
            "enkaustika je slikarska tehnika",
            "ne mogu dugo roniti",
            "potrudi se na instrukcijama",
            "fen je topao vjetar",
            "sve visibabe imaju lukovicu",
            "fotografirao je polarnu svjetlost",
            "baterija ne radi dobro",
            "klima ne pogoduje limunu",
            "zaviri ispod bijelog tepiha",
            "sad zamotaj kupljene poklone",
            "igrac je zabio gol",
            "rijeka je osvojila kup",
            "put je trajao dugo"
        )
    }

    fun loadImageKeywords(): List<String> {
        return listOf(
            "car", "slon", "cat", "castle", "firework", "mouse"
        )
    }

    fun loadAudioFileNames(): List<String> {
        return listOf(
            "bojice", "tenisice", "fotografije", "haljina", "kalendar", "kontrabas", "medvjed",
            "oblaci", "pokloni", "potkrovlje", "razglednica", "snijeg", "ulaznica"
        )
    }

    fun loadVideoAdNames(): List<String> {
        return listOf(
            "caillou", "mickey"
        )
    }

    fun loadStaticAdNames(): List<String> {
        return listOf(
            "static1", "static2", "static3", "static4", "static5"
        )
    }

    fun loadBlinkingAdNames(): List<String> {
        return listOf(
            "blinking1", "blinking2"
        )
    }

    fun loadCombinations(): List<Pair<String, String>> {
        return listOf(
            Pair("static", "top_right"),
            Pair("static", "middle_right"),
            Pair("static", "bottom_right"),
            Pair("video", "top_right"),
            Pair("video", "middle_right"),
            Pair("video", "bottom_right"),
            Pair("blinking", "top_right"),
            Pair("blinking", "middle_right"),
            Pair("blinking", "bottom_right"),
            Pair("noAd", "noPosition")
        ).shuffled()
    }

    fun getInstructionText(directoryName: String): String {
        return when (directoryName) {
            "car" -> "Označi automobile."
            "slon" -> "Označi slonove."
            "cat" -> "Označi mačke."
            "firework" -> "Označi vatromet."
            "mouse" -> "Označi miševe."
            "castle" -> "Označi dvorce."
            else -> "Izaberi slike."
        }
    }
}
