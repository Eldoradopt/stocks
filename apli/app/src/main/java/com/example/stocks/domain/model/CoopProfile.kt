package com.example.stocks.domain.model

data class CoopProfile(
    val profileId: String,
    val cuteName: String,
    val isSelected: Boolean,
    val gameMode: String = "Normal",
    val coopBankCoins: Double,
    val members: List<CoopMember>,
    val totalCoopCoins: Double,
)

data class CoopMember(
    val uuid: String,
    val purseCoins: Double,
    val sacksCounts: Map<String, Long>,
    val sacksValue: Double,
    val slayerValue: Double,
    val isCurrentPlayer: Boolean,
)

data class SlayerDropItem(
    val tag: String,
    val displayName: String,
    val bossCategory: String, // Zombie, Spider, Wolf, Enderman, Blaze
    val quantity: Long,
    val unitPrice: Double,
    val totalValue: Double,
)

data class SacksBreakdown(
    val totalSacksValue: Double,
    val slayerDropsValue: Double,
    val gemstonesValue: Double,
    val farmingValue: Double,
    val topValuedItems: List<SlayerDropItem>,
)
