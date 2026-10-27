package com.bernaferrari.remarkor.storecapture

/** Fictional sample notes for store screenshots. Store-capture builds only. */
internal object StoreFixtures {
    data class Note(
        val path: String,
        val content: String,
        val color: Int? = null,
        val labels: List<String> = emptyList(),
        val pinned: Boolean = false,
        val favorite: Boolean = false,
    )

    private const val YELLOW = 0xFFFFF8B8.toInt()
    private const val GREEN = 0xFFE2F6D3.toInt()
    private const val TEAL = 0xFFB4DDD3.toInt()
    private const val BLUE = 0xFFD4E4ED.toInt()
    private const val PURPLE = 0xFFD3BFDB.toInt()
    private const val PINK = 0xFFF6E2DD.toInt()
    private const val ORANGE = 0xFFF39F76.toInt()
    private const val BROWN = 0xFFE9E3D4.toInt()

    val notes = listOf(
        Note(
            path = "Lisbon in May.md",
            color = BLUE,
            labels = listOf("Travel"),
            pinned = true,
            favorite = true,
            content = """
                Four slow days. **Book the tram early**, walk everything else.

                ## Day one · Alfama
                - [x] Coffee at the *miradouro*
                - [x] Tram 28 before 9 am
                - [ ] Fado at Tasca do Chico

                ## Day two · Belém
                - [ ] Pastéis at the old bakery
                - [ ] Sunset at the river

                ## Pack
                - Linen shirt, light jacket
                - Film camera and `2 rolls`

                > Eat the pastéis warm. Always.
            """.trimIndent(),
        ),
        Note(
            path = "todo.txt",
            color = YELLOW,
            labels = listOf("Home"),
            pinned = true,
            favorite = true,
            content = """
                # This week

                ## Today
                - [x] Reply to Inês about the venue
                - [x] Renew library card
                - [ ] Call the plumber before 5 pm

                ## Errands
                - [ ] Oat milk, coffee beans, basil
                - [ ] Pick up the film scans
                - [ ] Return the drill to Marta

                ## Someday
                - [ ] Learn to bind a notebook
            """.trimIndent(),
        ),
        Note(
            path = "Q4 planning.md",
            color = PURPLE,
            labels = listOf("Work"),
            content = """
                ## Goals
                1. Ship the **offline sync** beta
                2. Cut onboarding to two screens
                3. Write the launch post

                ## Open questions
                - [ ] Pricing for small teams?
                - [ ] Who owns the help center?

                > Decide by Friday, then stop debating.
            """.trimIndent(),
        ),
        Note(
            path = "Reading list.md",
            color = GREEN,
            labels = listOf("Books"),
            favorite = true,
            content = """
                ## Now
                - [ ] *Piranesi* · Susanna Clarke

                ## Next
                - [ ] *The Overstory*
                - [ ] *A Psalm for the Wild-Built*

                ## Finished
                - [x] *Klara and the Sun*
                - [x] *Station Eleven*
            """.trimIndent(),
        ),
        Note(
            path = "Recipes/Weekend sourdough.md",
            color = ORANGE,
            labels = listOf("Recipes"),
            content = """
                **Levain** 9 pm · **Mix** 9 am · **Bake** 6 pm

                1. 500 g flour, 375 g water
                2. 100 g levain, 10 g salt
                3. Four folds, 30 min apart
                4. Cold proof overnight

                > 250 °C, lid on for 20 minutes.
            """.trimIndent(),
        ),
        Note(
            path = "Journal/27 October.md",
            color = BROWN,
            labels = listOf("Journal"),
            content = """
                Slow morning. Three pages written before the street woke up.

                - Walked to the market in the rain
                - Finished chapter four of *Piranesi*
                - Called Grandma, she says hi

                > Small steps, every day.
            """.trimIndent(),
        ),
        Note(
            path = "Journal/26 October.md",
            color = BROWN,
            labels = listOf("Journal"),
            content = """
                Long day at the studio. The new layout finally **clicks**.

                - Coffee with Ana by the river
                - Fixed the wobbly shelf, at last
            """.trimIndent(),
        ),
        Note(
            path = "Journal/25 October.md",
            color = YELLOW,
            labels = listOf("Journal"),
            content = """
                Clocks went back. An extra hour, spent entirely on *coffee*.

                > Rest is part of the work.
            """.trimIndent(),
        ),
        Note(
            path = "Journal/24 October.md",
            color = GREEN,
            labels = listOf("Journal"),
            content = """
                Hiked the coast path with Rui. 14 km, two blisters, one perfect view.

                - [x] Pack lunch
                - [x] Bring the good socks
            """.trimIndent(),
        ),
        Note(
            path = "Recipes/Lemon pasta.md",
            color = YELLOW,
            labels = listOf("Recipes"),
            content = """
                1. Zest two lemons into butter
                2. Toss with pasta and its water
                3. Parmesan, black pepper, basil
            """.trimIndent(),
        ),
        Note(
            path = "Balcony garden.md",
            color = TEAL,
            labels = listOf("Home"),
            content = """
                - [x] Repot the rosemary
                - [ ] Basil seeds, *after the frost*
                - [ ] Water the tomatoes `every 2 days`
                - [ ] Ask Rui for lavender cuttings
            """.trimIndent(),
        ),
        Note(
            path = "Gift ideas.md",
            color = PINK,
            content = """
                - **Mum** · a hand coffee grinder
                - **Rui** · climbing shoes
                - **Ana** · the Lisbon tram print
                - **Leo** · a good pocket notebook
            """.trimIndent(),
        ),
        Note(
            path = "On slow mornings.md",
            labels = listOf("Writing"),
            content = """
                There is a particular quiet before seven, when the kettle is the loudest thing in the house and nobody needs anything from you yet.

                I have started keeping that hour for **writing**. Not good writing, just *honest* writing.

                > The page does not care how it sounds.
            """.trimIndent(),
        ),
        Note(
            path = "quicknote.md",
            favorite = true,
            content = """
                Newsletter ideas
                - Ten tools I actually use
                - Why I write everything in plain text
                - A tour of my notebook folder
            """.trimIndent(),
        ),
    )
}
