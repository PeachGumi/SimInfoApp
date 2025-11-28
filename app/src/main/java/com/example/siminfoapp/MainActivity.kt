package com.example.siminfoapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.siminfoapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Activity Result API を使ったパーミッションリクエストランチャーを準備
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // 権限が許可された場合
                loadSimInfo()
            } else {
                // 権限が拒否された場合
                binding.infoView.text = "READ_PHONE_STATE が拒否されました"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.infoView.text = "SIM情報を表示します"

        binding.btnGrant.setOnClickListener {
            // 権限がすでに許可されているか確認
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // 許可済みなら、情報をロード
                loadSimInfo()
            } else {
                // 未許可なら、パーミッションをリクエスト
                requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            }
        }

        binding.btnRevoke.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // 画面が表示されるたびにSIM情報を更新
        loadSimInfo()
    }

    private fun loadSimInfo() {
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val info = StringBuilder()

        // 権限状態を表示
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        val permissionStatus = if (granted) "READ_PHONE_STATE: 許可済み ✅" else "READ_PHONE_STATE: 未許可 ❌"
        info.append("$permissionStatus\n\n")

        // ===== 権限不要で取得できる情報 =====
        info.append("【権限不要で取得できる情報】\n")

        val simStateDesc = when (tm.simState) {
            TelephonyManager.SIM_STATE_ABSENT -> "SIMなし"
            TelephonyManager.SIM_STATE_READY -> "利用可能"
            TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PINコード必要"
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUKコード必要"
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "ネットワークロック"
            else -> "不明"
        }

        val phoneTypeDesc = when (tm.phoneType) {
            TelephonyManager.PHONE_TYPE_GSM -> "GSM (携帯電話)"
            TelephonyManager.PHONE_TYPE_CDMA -> "CDMA (携帯電話)"
            TelephonyManager.PHONE_TYPE_SIP -> "SIP (VoIP)"
            else -> "なし"
        }

        // SIM関連
        info.append("SIM Operator（MCC+MNC）: ${tm.simOperator}\n")
        info.append("SIM Operator Name（キャリア名）: ${tm.simOperatorName}\n")
        info.append("SIM Country（SIM国コード）: ${tm.simCountryIso}\n")
        info.append("SIM State（SIM状態）: $simStateDesc\n")

        // ネットワーク関連
        info.append("Network Operator（MCC+MNC）: ${tm.networkOperator}\n")
        info.append("Network Operator Name（ネットワーク事業者名）: ${tm.networkOperatorName}\n")
        info.append("Network Country（ネットワーク国コード）: ${tm.networkCountryIso}\n")
        info.append("Roaming（ローミング中か）: ${if (tm.isNetworkRoaming) "はい" else "いいえ"}\n")

        // 端末機能関連
        info.append("Phone Type（電話タイプ）: $phoneTypeDesc\n")
        info.append("Voice Capable（音声通話可能か）: ${if (tm.isVoiceCapable) "可能" else "不可"}\n")
        info.append("SMS Capable（SMS可能か）: ${if (tm.isSmsCapable) "可能" else "不可"}\n\n")

        // ===== READ_PHONE_STATE が必要な情報 =====
        info.append("【READ_PHONE_STATE 権限が必要な情報】\n")

        if (granted) {
            // 電話番号
            val phoneNumber = try { tm.line1Number } catch (e: SecurityException) { null }
            info.append("Phone number（電話番号）: ${phoneNumber ?: "Android10以降では取得不可"}\n")

            // IMSI
            val subscriberId = try { tm.subscriberId } catch (e: SecurityException) { null }
            info.append("Subscriber ID (IMSI)（加入者ID）: ${subscriberId ?: "Android10以降では取得不可"}\n")

            // ICCID
            val simSerial = try { tm.simSerialNumber } catch (e: SecurityException) { null }
            info.append("SIM Serial (ICCID)（SIMシリアル番号）: ${simSerial ?: "Android10以降では取得不可"}\n")

            // IMEI/MEID
            val deviceId = try { tm.deviceId } catch (e: SecurityException) { null }
            info.append("Device ID (IMEI/MEID)（端末ID）: ${deviceId ?: "Android10以降では取得不可"}\n")

            // 通話状態
            val callState = tm.callState
            val callStateDesc = when (callState) {
                TelephonyManager.CALL_STATE_IDLE -> "待機中"
                TelephonyManager.CALL_STATE_RINGING -> "着信中"
                TelephonyManager.CALL_STATE_OFFHOOK -> "通話中"
                else -> "不明"
            }
            info.append("Call State（通話状態）: $callStateDesc\n")

            // データ接続状態
            val dataState = tm.dataState
            val dataStateDesc = when (dataState) {
                TelephonyManager.DATA_CONNECTED -> "接続中"
                TelephonyManager.DATA_CONNECTING -> "接続試行中"
                TelephonyManager.DATA_DISCONNECTED -> "切断中"
                TelephonyManager.DATA_SUSPENDED -> "一時停止中"
                else -> "不明"
            }
            info.append("Data State（データ接続状態）: $dataStateDesc\n")

            // ネットワークタイプ
            val networkTypeDesc = when (tm.networkType) {
                TelephonyManager.NETWORK_TYPE_LTE -> "LTE (4G)"
                TelephonyManager.NETWORK_TYPE_NR -> "NR (5G)"
                TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA (3G)"
                else -> "その他/不明"
            }
            info.append("Network Type（通信方式）: $networkTypeDesc\n")

            // --- 詳細なネットワーク状態 ---
            info.append("\n【詳細なネットワーク状態】\n")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val serviceState = tm.serviceState
                    val serviceStateDesc = when (serviceState?.state) {
                        android.telephony.ServiceState.STATE_IN_SERVICE -> "圏内 (In Service)"
                        android.telephony.ServiceState.STATE_OUT_OF_SERVICE -> "圏外 (Out of Service)"
                        android.telephony.ServiceState.STATE_EMERGENCY_ONLY -> "緊急通報のみ"
                        android.telephony.ServiceState.STATE_POWER_OFF -> "電源オフ"
                        else -> "不明 (${serviceState?.state})"
                    }
                    info.append("サービス状態: $serviceStateDesc\n")
                    serviceState?.let {
                        info.append("接続中の事業者: ${it.operatorAlphaLong}\n")
                        info.append("ローミング状態 (詳細): ${if (it.roaming) "はい" else "いいえ"}\n")
                        info.append("ネットワーク選択: ${if (it.isManualSelection) "手動" else "自動"}\n")
                    }
                } catch (e: SecurityException) {
                    info.append("サービス状態: 取得に失敗しました (権限不足の可能性があります)\n")
                }
            } else {
                info.append("サービス状態: Android 8.0 (Oreo) 以降で対応しています\n")
            }

        } else {
            info.append("→ 権限が未許可のため表示できません\n")
        }

        binding.infoView.text = info.toString()
    }
}
