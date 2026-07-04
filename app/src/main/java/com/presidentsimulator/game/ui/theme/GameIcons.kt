package com.presidentsimulator.game.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.ui.graphics.vector.ImageVector
import com.presidentsimulator.game.data.InfrastructureType
import com.presidentsimulator.game.data.MilitaryHardware
import com.presidentsimulator.game.data.MissionType
import com.presidentsimulator.game.data.TradeCommodity
import com.presidentsimulator.game.ui.navigation.GameDestination

/**
 * Centralized Material icon map for ministries, resources, and gameplay domains.
 * Prefer these constants over ad-hoc icon choices in individual screens.
 */
object GameIcons {

    // ── Ministries / destinations ────────────────────────────────────────────

    val MinistryEconomy: ImageVector = Icons.Default.AttachMoney
    val MinistryDefense: ImageVector = Icons.Default.Security
    val MinistryForeignAffairs: ImageVector = Icons.Default.Public
    val MinistrySecretService: ImageVector = Icons.Default.Groups
    val MinistryScience: ImageVector = Icons.Default.Science
    val MinistryLawsSociety: ImageVector = Icons.Default.Balance
    val MinistryCommerce: ImageVector = Icons.Default.Storefront
    val MinistryIndustry: ImageVector = Icons.Default.Factory
    val MinistryGovernance: ImageVector = Icons.Default.Language
    val MinistryStatistics: ImageVector = Icons.Default.AccountBalance
    val MinistryInterior: ImageVector = Icons.Default.Gavel
    val MinistryHealth: ImageVector = Icons.Default.HealthAndSafety
    val MinistryEducation: ImageVector = Icons.AutoMirrored.Filled.MenuBook
    val MinistryCulture: ImageVector = Icons.Default.TheaterComedy
    val MinistrySocial: ImageVector = Icons.Default.VolunteerActivism

    // ── Resources / commodities ──────────────────────────────────────────────

    val ResourceOil: ImageVector = Icons.Default.WaterDrop
    val ResourceFood: ImageVector = Icons.Default.Eco
    val ResourceSteel: ImageVector = Icons.Default.Construction
    val ResourceEnergy: ImageVector = Icons.Default.Bolt
    val ResourceGrain: ImageVector = Icons.Default.Agriculture
    val ResourceConsumerGoods: ImageVector = Icons.Default.ShoppingCart
    val ResourceMaterials: ImageVector = Icons.Default.Construction

    // ── Infrastructure ───────────────────────────────────────────────────────

    val InfraFactory: ImageVector = Icons.Default.Factory
    val InfraFarm: ImageVector = Icons.Default.Agriculture
    val InfraHousing: ImageVector = Icons.Default.Home
    val InfraPowerPlant: ImageVector = Icons.Default.Bolt
    val InfraMine: ImageVector = Icons.Default.Construction

    // ── Military hardware ────────────────────────────────────────────────────

    val HardwareTanks: ImageVector = Icons.Default.MilitaryTech
    val HardwareJets: ImageVector = Icons.Default.AirplanemodeActive
    val HardwareShips: ImageVector = Icons.Default.DirectionsBoat
    val HardwareNukes: ImageVector = Icons.Default.Whatshot

    // ── Events / alerts ──────────────────────────────────────────────────────

    val EventWarning: ImageVector = Icons.Default.Warning
    val EventEpidemic: ImageVector = Icons.Default.LocalHospital
    val EventEconomy: ImageVector = Icons.Default.AttachMoney
    val EventConflict: ImageVector = Icons.Default.Security

    fun forDestination(destination: GameDestination): ImageVector = when (destination) {
        GameDestination.Dashboard -> Icons.Default.AccountBalance
        GameDestination.Economy -> MinistryEconomy
        GameDestination.Military -> MinistryDefense
        GameDestination.Diplomacy -> MinistryForeignAffairs
        GameDestination.SecretService -> MinistrySecretService
        GameDestination.Science -> MinistryScience
        GameDestination.LawsSociety -> MinistryLawsSociety
        GameDestination.AudioSettings -> Icons.Default.VolumeUp
    }

    fun forTradeCommodity(commodity: TradeCommodity): ImageVector = when (commodity) {
        TradeCommodity.OIL -> ResourceOil
        TradeCommodity.STEEL -> ResourceSteel
        TradeCommodity.GRAIN -> ResourceGrain
        TradeCommodity.CONSUMER_GOODS -> ResourceConsumerGoods
    }

    fun forInfrastructure(type: InfrastructureType): ImageVector = when (type) {
        InfrastructureType.FACTORY -> InfraFactory
        InfrastructureType.FARM -> InfraFarm
        InfrastructureType.HOUSING -> InfraHousing
        InfrastructureType.POWER_PLANT -> InfraPowerPlant
        InfrastructureType.MINE -> InfraMine
    }

    fun forMilitaryHardware(hardware: MilitaryHardware): ImageVector = when (hardware) {
        MilitaryHardware.TANKS -> HardwareTanks
        MilitaryHardware.FIGHTER_JETS -> HardwareJets
        MilitaryHardware.NAVAL_SHIPS -> HardwareShips
        MilitaryHardware.NUCLEAR_ARSENAL -> HardwareNukes
    }

    fun forMissionType(type: MissionType): ImageVector = when (type) {
        MissionType.STEAL_TECHNOLOGY -> MinistryScience
        MissionType.SABOTAGE_ECONOMY -> MinistryEconomy
        MissionType.FUND_REBELS -> EventConflict
        MissionType.ASSASSINATE_LEADER -> MinistrySecretService
    }

    /**
     * Resolves an event-type key (e.g. "epidemic", "riot") to a thematic Material icon.
     */
    fun forEventType(eventType: String): ImageVector {
        val key = eventType.lowercase().trim()
        return when {
            key.contains("epidemic") || key.contains("plague") || key.contains("disease") ->
                EventEpidemic
            key.contains("market") || key.contains("crash") || key.contains("boom") ||
                key.contains("strike") || key.contains("economic") || key.contains("trade") ->
                EventEconomy
            key.contains("war") || key.contains("rebel") || key.contains("border") ||
                key.contains("military") || key.contains("riot") || key.contains("uprising") ->
                EventConflict
            key.contains("science") || key.contains("breakthrough") ->
                MinistryScience
            else -> EventWarning
        }
    }
}
