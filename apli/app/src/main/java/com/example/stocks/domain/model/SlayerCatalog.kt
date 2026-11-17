package com.example.stocks.domain.model

object SlayerCatalog {
    val ZOMBIE_DROPS = listOf("REVENANT_FLESH", "REVENANT_VISCERA", "WARDEN_HEART", "SHARD_OF_THE_SHREDDED")
    val SPIDER_DROPS = listOf("TARANTULA_WEB", "TARANTULA_SILK", "DIGESTED_MOSQUITO", "SPIDER_CATALYST")
    val WOLF_DROPS   = listOf("WOLF_TOOTH", "GOLDEN_TOOTH", "OVERFLUX_CAPACITOR", "RED_CLAW_EGG")
    val EMAN_DROPS   = listOf("NULL_SPHERE", "NULL_ATOM", "NULL_EDGE", "JUDGEMENT_CORE")
    val BLAZE_DROPS  = listOf("DERELICT_ASHE", "MOLTEN_CUBE", "GABAGOOL", "SUBZERO_INVERTER")

    fun getCategory(tag: String): String = when {
        ZOMBIE_DROPS.contains(tag) -> "Revenant (Zombie)"
        SPIDER_DROPS.contains(tag) -> "Tarantula (Spider)"
        WOLF_DROPS.contains(tag)   -> "Sven (Wolf)"
        EMAN_DROPS.contains(tag)   -> "Voidgloom (Enderman)"
        BLAZE_DROPS.contains(tag)  -> "Inferno (Blaze)"
        tag.contains("GEM")        -> "Gemstones"
        tag.contains("ENCHANTED")  -> "Farming & Sacks"
        else -> "Miscellaneous"
    }

    val ALL_SLAYER_TAGS = ZOMBIE_DROPS + SPIDER_DROPS + WOLF_DROPS + EMAN_DROPS + BLAZE_DROPS
}
