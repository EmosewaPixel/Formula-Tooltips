package com.scientianovateam.formulatoooltips

import com.emosewapixel.pixellib.extensions.round
import com.emosewapixel.pixellib.extensions.shorten
import com.emosewapixel.pixellib.extensions.toSubscipt
import com.emosewapixel.pixellib.materialsystem.addition.BaseMaterials
import com.emosewapixel.pixellib.materialsystem.elements.ElementUtils
import com.emosewapixel.pixellib.materialsystem.lists.MaterialItems
import com.emosewapixel.pixellib.materialsystem.main.Material
import com.emosewapixel.pixellib.materialsystem.main.MaterialStack
import com.emosewapixel.pixellib.materialsystem.properties.CompoundType
import com.emosewapixel.pixellib.materialsystem.properties.DisplayType
import net.alexwells.kottle.KotlinEventBusSubscriber
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
        MaterialItems.getItemMaterial(item)?.let { mat ->
            formula = if (mat.compoundType == CompoundType.ALLOY) getCompositionString(mat) else getFormulaString(mat)
            if (formula != "?")
                e.toolTip.add(StringTextComponent(TextFormatting.GRAY.toString() + formula))
        }
    }

    private fun getCompositionString(mat: Material, base: Double = 1.0): String =
            if (mat.composition.isEmpty())
                mat.element.symbol
            else {
                val total = mat.composition.map(MaterialStack::count).sum()
                mat.composition.map { (material, count) ->
                    val percent = base * count.toFloat() / total
                    if (material.composition.isNotEmpty())
                        if (material.compoundType == CompoundType.ALLOY)
                            getCompositionString(material, percent)
                        else
                            "${(percent * 100).round().shorten()}% ${getFormulaString(material)}"
                    else
                        "${(percent * 100).round().shorten()}% ${material.element.symbol}"
                }.joinToString { it }
            }

    private fun getFormulaString(mat: Material, split: Boolean = false): String =
            if (mat.composition.isEmpty())
                mat.element.symbol
            else
                mat.composition.map { (material, count) ->
                    val composition = material.composition
                    when {
                        material === BaseMaterials.WATER ->
                            "·" + count + "H₂O"
                        material.displayType == DisplayType.GROUP ->
                            if (count > 1)
                                "(${ElementUtils.getElementalComposition(material)
                                        .map { it.element.symbol + if (it.count > 1) it.count.toSubscipt() else "" }
                                        .joinToString("") { it }})${count.toSubscipt()}"
                            else
                                ElementUtils.getElementalComposition(material).map { it.element.symbol + it.count.toSubscipt() }.joinToString("") { it }
                        composition.size == 1 ->
                            if (composition[0].material.transitionProperties != null)
                                getFormulaString(composition[0].material) + (count * composition[0].count).toSubscipt()
                            else
                                getFormulaString(material) + count.toSubscipt()
                        composition.isNotEmpty() ->
                            if (count > 1)
                                "(${getFormulaString(material, material.compoundType == CompoundType.ALLOY)})${count.toSubscipt()}"
                            else
                                getFormulaString(material, material.compoundType == CompoundType.ALLOY)
                        else -> material.element.symbol + if (count > 1) count.toSubscipt() else ""
                    }
                }.joinToString(if (split) ", " else "") { it }
}