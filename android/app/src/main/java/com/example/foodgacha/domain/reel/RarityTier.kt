package com.example.foodgacha.domain.reel

import androidx.compose.ui.graphics.Color
import com.example.foodgacha.R

enum class RarityTier(
    val tier: Int,
    val hexColor: String,
    val color: Color,
    val nameEn: String,
    val nameVi: String,
    val soundResId: Int
) {
    MIL_SPEC(0, "#4B69FF", Color(0xFF4B69FF), "Mil-Spec", "Quốc Dân", R.raw.item_reveal3_rare),
    RESTRICTED(1, "#8847FF", Color(0xFF8847FF), "Restricted", "Hiếm", R.raw.item_reveal4_mythical),
    CLASSIFIED(2, "#D32CE6", Color(0xFFD32CE6), "Classified", "Cực Phẩm", R.raw.item_reveal5_legendary),
    COVERT(3, "#EB4B4B", Color(0xFFEB4B4B), "Covert", "Tối Mật", R.raw.item_reveal6_ancient),
    SPECIAL(4, "#E4AE39", Color(0xFFE4AE39), "★ Special", "★ Đặc Biệt", R.raw.item_reveal6_ancient);

    companion object {
        fun fromTier(tier: Int): RarityTier {
            return entries.find { it.tier == tier } ?: MIL_SPEC
        }

        val allTiers: List<RarityTier> = entries
    }
}
