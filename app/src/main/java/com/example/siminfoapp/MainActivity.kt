package com.example.siminfoapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.siminfoapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val REQUEST_READ_PHONE_STATE = 100
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.infoView.text = "SIM情報を表示します"

        binding.btnGrant.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_PHONE_STATE),
                    REQUEST_READ_PHONE_STATE
                )
            } else {
                loadSimInfo()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_PHONE_STATE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSimInfo()
            } else {
                binding.infoView.text = "READ_PHONE_STATE が拒否されました"
            }
        }
    }

    private fun loadSimInfo() {
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val info = StringBuilder()

        // 権限状態を表示
        val granted = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
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
        } else {
            info.append("→ 権限が未許可のため表示できません\n")
        }

        binding.infoView.text = info.toString()
    }
}
