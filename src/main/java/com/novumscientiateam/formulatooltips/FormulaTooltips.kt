package com.novumscientiateam.formulatooltips

import com.emosewapixel.pixellib.materialsystem.MaterialRegistry
import com.emosewapixel.pixellib.materialsystem.element.ElementUtils
import com.emosewapixel.pixellib.materialsystem.lists.MaterialBlocks
import com.emosewapixel.pixellib.materialsystem.lists.MaterialItems
import com.emosewapixel.pixellib.materialsystem.materials.Material
import com.emosewapixel.pixellib.materialsystem.materials.utility.GroupMaterial
import com.emosewapixel.pixellib.materialsystem.materials.utility.TransitionMaterial
import net.alexwells.kottle.KotlinEventBusSubscriber
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod(FormulaTooltips.MOD_ID)
object FormulaTooltips {
    const val MOD_ID = "formulatooltips"
}

@KotlinEventBusSubscriber(value = [Dist.CLIENT], modid = FormulaTooltips.MOD_ID, bus = KotlinEventBusSubscriber.Bus.FORGE)
object GameEvents {
    @SubscribeEvent
    fun tooltipEvent(e: ItemTooltipEvent) {
        val formula: String
        val item = e.itemStack.item
        if (item is BlockItem) {
            val block = Block.getBlockFromItem(item)
            if (MaterialBlocks.getAll().contains(block)) {
                formula = getFormulaString(MaterialBlocks.getBlockMaterial(block)!!)
                if (formula != "?")
                    e.toolTip.add(StringTextComponent(TextFormatting.GRAY.toString() + formula))
            }
        } else if (MaterialItems.getAll().contains(item)) {
            formula = getFormulaString(MaterialItems.getItemMaterial(item)!!)
            if (formula != "?")
                e.toolTip.add(StringTextComponent(TextFormatting.GRAY.toString() + formula))
        }
    }

    private fun getFormulaString(mat: Material): String {
        return if (mat.composition.size == 0)
            mat.element.symbol
        else
            mat.composition.map { (material, count) ->
                if (material === MaterialRegistry.WATER)
                    "·" + count + "H2O"
                if (material is GroupMaterial)
                    if (count > 1)
                        "(" + ElementUtils.getElementalComposition(material).map { s -> s.element.symbol + if (s.count > 1) s.count else "" }.joinToString { it } + ")" + count
                    else
                        ElementUtils.getElementalComposition(material).map { s -> s.element.symbol + s.count }.joinToString { it }
                val composition = material.composition
                if (composition.size == 1) {
                    if (composition[0].material is TransitionMaterial)
                        getFormulaString(composition[0].material) + count * composition[0].count
                    getFormulaString(material) + count
                }
                if (composition.size > 1)
                    if (count > 1)
                        "(" + getFormulaString(material) + ")" + count
                    else
                        getFormulaString(material)
                material.element.symbol + if (count > 1) count else ""
            }.joinToString { it }
    }
}