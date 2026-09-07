package ir.atiran.hamrah.viewer.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Everything the app needs to talk to a given Atiran server. */
data class AtiranSettings(
    val serverUrl: String = "",
    val cpuId: String = "",
    val ownerNum: String = "",
    val inventoryNum: String = "",
    val atiranNum: String = "",
    val carrierNum: String = "",
    val activeLine: String = "",
    val serverAdd: String = "",
    val username: String = "",
    val password: String = "",
    val shMo: String = "",
    val visitorId: String = "",
    val appId: String = "",
    val configured: Boolean = false,
) {
    fun normalizedServer(): String = serverUrl.trim().trimEnd('/')

    fun toSetInfo(): SetInfo = SetInfo(
        shmo = shMo.ifBlank { null },
        password = password.ifBlank { null },
        _ownerNum = ownerNum.ifBlank { null },
        _InventoryNum = inventoryNum.ifBlank { null },
        _AtiranNum = atiranNum.ifBlank { null },
        _carrierNum = carrierNum.ifBlank { null },
        _ActiveLine = activeLine.ifBlank { null },
        _serverAdd = serverAdd.ifBlank { null },
        VisitorID = visitorId.toIntOrNull(),
    )
}

private val Context.dataStore by preferencesDataStore(name = "atiran_settings")

class SettingsStore(private val context: Context) {

    private object Keys {
        val serverUrl = stringPreferencesKey("serverUrl")
        val cpuId = stringPreferencesKey("cpuId")
        val ownerNum = stringPreferencesKey("ownerNum")
        val inventoryNum = stringPreferencesKey("inventoryNum")
        val atiranNum = stringPreferencesKey("atiranNum")
        val carrierNum = stringPreferencesKey("carrierNum")
        val activeLine = stringPreferencesKey("activeLine")
        val serverAdd = stringPreferencesKey("serverAdd")
        val username = stringPreferencesKey("username")
        val password = stringPreferencesKey("password")
        val shMo = stringPreferencesKey("shMo")
        val visitorId = stringPreferencesKey("visitorId")
        val appId = stringPreferencesKey("appId")
        val configured = booleanPreferencesKey("configured")
    }

    val settings: Flow<AtiranSettings> = context.dataStore.data.map { p ->
        AtiranSettings(
            serverUrl = p[Keys.serverUrl] ?: "",
            cpuId = p[Keys.cpuId] ?: "",
            ownerNum = p[Keys.ownerNum] ?: "",
            inventoryNum = p[Keys.inventoryNum] ?: "",
            atiranNum = p[Keys.atiranNum] ?: "",
            carrierNum = p[Keys.carrierNum] ?: "",
            activeLine = p[Keys.activeLine] ?: "",
            serverAdd = p[Keys.serverAdd] ?: "",
            username = p[Keys.username] ?: "",
            password = p[Keys.password] ?: "",
            shMo = p[Keys.shMo] ?: "",
            visitorId = p[Keys.visitorId] ?: "",
            appId = p[Keys.appId] ?: "",
            configured = p[Keys.configured] ?: false,
        )
    }

    suspend fun save(s: AtiranSettings, markConfigured: Boolean = true) {
        context.dataStore.edit { p ->
            p[Keys.serverUrl] = s.serverUrl.trim()
            p[Keys.cpuId] = s.cpuId.trim()
            p[Keys.ownerNum] = s.ownerNum.trim()
            p[Keys.inventoryNum] = s.inventoryNum.trim()
            p[Keys.atiranNum] = s.atiranNum.trim()
            p[Keys.carrierNum] = s.carrierNum.trim()
            p[Keys.activeLine] = s.activeLine.trim()
            p[Keys.serverAdd] = s.serverAdd.trim()
            p[Keys.username] = s.username.trim()
            p[Keys.password] = s.password
            p[Keys.shMo] = s.shMo.trim()
            p[Keys.visitorId] = s.visitorId.trim()
            p[Keys.appId] = s.appId.trim()
            p[Keys.configured] = markConfigured
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
