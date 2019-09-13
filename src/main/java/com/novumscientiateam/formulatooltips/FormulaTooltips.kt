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
            if (block in MaterialBlocks) {
                formula = getFormulaString(MaterialBlocks.getBlockMaterial(block)!!)
                if (formula != "?")
                    e.toolTip.add(StringTextComponent(TextFormatting.GRAY.toString() + formula))
            }
        } else if (item in MaterialItems) {
            formula = getFormulaString(MaterialItems.getItemMaterial(item)!!)
            if (formula != "?")
                e.toolTip.add(StringTextComponent(TextFormatting.GRAY.toString() + formula))
        }
    }

    private fun getFormulaString(mat: Material): String =
            if (mat.composition.isEmpty())
                mat.element.symbol
            else
                mat.composition.map { (material, count) ->
                    val composition = material.composition
                    when {
                        material === MaterialRegistry.WATER ->
                            "·" + count + "H2O"
                        material is GroupMaterial ->
                            if (count > 1)
                                "(${ElementUtils.getElementalComposition(material).map { it.element.symbol + if (it.count > 1) it.count else "" }.joinToString(separator = "", transform = { it })})$count"
                            else
                                ElementUtils.getElementalComposition(material).map { it.element.symbol + it.count }.joinToString(separator = "", transform = { it })
                        composition.size == 1 ->
                            if (composition[0].material is TransitionMaterial)
                                getFormulaString(composition[0].material) + count * composition[0].count
                            else
                                getFormulaString(material) + count
                        composition.isNotEmpty() ->
                            if (count > 1)
                                "(${getFormulaString(material)})$count"
                            else
                                getFormulaString(material)
                        else -> material.element.symbol + if (count > 1) count else ""
                    }
                }.joinToString(separator = "", transform = { it })
}