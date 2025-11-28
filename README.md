# SimInfoApp

SIMカードや端末の通信機能に関する様々な情報を表示するAndroidアプリケーションです。

## 主な機能

このアプリでは、以下の情報を確認できます。

### 権限なしで取得できる情報

-   SIMオペレーター (MCC+MNC)
-   SIMオペレーター名（キャリア名）
-   SIMの国コード
-   SIMの状態 (利用可能, PINロック, SIMなし 等)
-   ネットワークオペレーター (MCC+MNC)
-   ネットワーク事業者名
-   ネットワークの国コード
-   ローミング状態
-   電話のタイプ (GSM, CDMA, SIP)
-   音声通話やSMSが可能か

### `READ_PHONE_STATE` 権限が必要な情報

-   電話番号 (※Android 10以降では取得できません)
-   IMSI (加入者識別番号) (※Android 10以降では取得できません)
-   ICCID (SIMシリアル番号) (※Android 10以降では取得できません)
-   IMEI/MEID (端末識別番号) (※Android 10以降では取得できません)
-   通話状態 (待機中, 着信中, 通話中)
-   データ通信の状態
-   ネットワークの通信方式 (LTE, 5G 等)

## パーミッションについて

-   `android.permission.READ_PHONE_STATE`
    -   電話番号やIMSIといった機密性の高い情報を表示するために必要な権限です。
    -   プライバシー保護のため、Android 10以降では、この権限があっても主要な識別番号（電話番号、IMSI, IMEI等）にはアクセスできなくなっています。

## 使い方

1.  アプリを起動すると、特別な権限が不要な情報がまず表示されます。
2.  `READ_PHONE_STATE` 権限が必要な情報を表示するには、「権限を許可」ボタンをタップしてください。
3.  権限を許可すると、追加の情報が表示されます。
4.  「権限を取り消す」ボタンをタップすると、アプリの設定画面が開き、手動で権限を取り消すことができます。

## 使用技術

-   **言語**: Kotlin
-   **UI**: XMLレイアウト + [View Binding](https://developer.android.com/topic/libraries/view-binding)
-   **パーミッション管理**: [Activity Result API](https://developer.android.com/training/permissions/requesting)
-   **アーキテクチャ**: Single Activity

