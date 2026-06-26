# 🔒 SecureVPN - Android VPN App

Complete Android VPN application built with **Kotlin + Jetpack Compose + Hilt + AdMob**.

---

## 📁 Project Structure

```
app/src/main/java/com/securevpn/app/
├── VPNApplication.kt              # App class (Hilt + AdMob init)
├── MainActivity.kt                # Entry point + VPN permission handling
├── data/
│   ├── model/VpnModels.kt         # VpnServer, VpnState, SessionStats
│   └── repository/
│       └── VpnServerRepository.kt # VPNGate API + IKEv2 free servers
├── service/
│   ├── VpnTunnelService.kt        # Android VpnService + TUN interface
│   └── BootReceiver.kt            # Auto-connect on boot
├── ui/
│   ├── navigation/NavGraph.kt
│   ├── screens/
│   │   ├── HomeScreen.kt          # Main screen with power button
│   │   ├── ServerListScreen.kt    # Server browser with search
│   │   └── SettingsScreen.kt      # App settings
│   ├── components/
│   │   ├── VpnPowerButton.kt      # Animated power button
│   │   └── BannerAdView.kt        # AdMob banner composable
│   └── theme/
│       ├── Theme.kt               # Dark VPN theme
│       └── Typography.kt
├── viewmodel/VpnViewModel.kt      # State + AdMob + VPN logic
├── utils/AdManager.kt             # AdMob (banner, interstitial, rewarded)
└── di/AppModule.kt                # Hilt DI
```

---

## 🚀 Getting Started

### Step 1 — Setup

1. Clone / open in Android Studio
2. Add your `google-services.json` to `app/`
3. Replace AdMob IDs in `AdManager.kt`:
   ```kotlin
   const val BANNER_AD_UNIT_ID       = "ca-app-pub-YOUR_ID/BANNER_ID"
   const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-YOUR_ID/INTERSTITIAL_ID"
   const val REWARDED_AD_UNIT_ID     = "ca-app-pub-YOUR_ID/REWARDED_ID"
   ```
4. Update App ID in `build.gradle.kts`:
   ```kotlin
   manifestPlaceholders["admobAppId"] = "ca-app-pub-YOUR_APP_ID"
   ```

### Step 2 — VPN Servers (FREE, Zero Cost)

This app uses **two free server sources**:

#### A) VPNGate (University of Tsukuba, Japan)
- Public relay VPN service with 3,000+ community servers worldwide
- API: `https://www.vpngate.net/api/iphone/`
- Provides OpenVPN configs as Base64 in CSV format
- **No account needed. Completely free.**

#### B) VPNBook (Free static servers)
- Free IKEv2/OpenVPN servers in US, DE, FR, CA
- Credentials rotate monthly — check `vpnbook.com` for current password
- Update `VPNBOOK_PASS` in `VpnServerRepository.kt` monthly
- **Use Firebase Remote Config to push updates without app update:**
  ```kotlin
  remoteConfig.fetchAndActivate().addOnCompleteListener {
      val newPass = remoteConfig.getString("vpnbook_pass")
      // Update locally
  }
  ```

### Step 3 — Integrate Real VPN Libraries

#### OpenVPN (ics-openvpn)
```gradle
// Add to build.gradle.kts
implementation("de.blinkt.openvpn:openvpn:0.7.33")
// or build ics-openvpn as a module:
// https://github.com/schwabe/ics-openvpn
```
In `VpnTunnelService.connectOpenVpn()`:
```kotlin
val profile = ConfigParser().parseConfig(StringReader(server.ovpnConfig))
profileManager.saveProfile(this, profile)
VpnProfile.doConnect(this, profile.uuidString)
```

#### IKEv2 (Android 12+ native)
```kotlin
// In connectIkeV2()
val ikev2Profile = Ikev2VpnProfile.Builder(server.ikev2Host)
    .setAuthUsernamePassword(server.ikev2User, server.ikev2Pass, null)
    .build()
val cm = getSystemService(ConnectivityManager::class.java)
cm.startIkeSession(executor, ikev2Profile, ikeSessionCallback)
```

#### IKEv2 (Android < 12 — Strongswan)
- SDK: https://github.com/strongswan/strongswan/tree/master/src/frontends/android

---

## 💰 AdMob Monetization Strategy

| Ad Type | Placement | Typical eCPM | Notes |
|---------|-----------|-------------|-------|
| **Banner** | Bottom of home screen | $0.50–$1.50 | Always visible |
| **Interstitial** | Before connecting | $3–$8 | 2-min cooldown enforced |
| **Rewarded** | Unlock premium server | $10–$20 | User-initiated, highest value |
| **Native** | Between server list items | $2–$5 | Add when scaling |

### Revenue Estimate
- 1,000 DAU × $0.05 avg RPM = ~$50/day = **$1,500/month**
- Rewarded ads from 10% of users = extra **$300–$600/month**
- Scale to 10K DAU = **$15,000–$20,000/month**

### Maximize Revenue
1. Show interstitial EVERY time user connects (respects 2-min cooldown)
2. Use rewarded ads to unlock "Premium" servers (create perceived value)
3. Show native ads between free/premium server rows
4. Add `AdSize.MEDIUM_RECTANGLE` (300×250) on server list for higher CPM
5. Enable **Adaptive Banner** for auto-sizing

---

## 🔧 Monthly Maintenance

| Task | Frequency | How |
|------|-----------|-----|
| Update VPNBook password | Monthly | Check vpnbook.com → push via Firebase Remote Config |
| Check VPNGate server quality | Weekly | API auto-fetches fresh servers |
| Monitor AdMob policy compliance | Always | Never show >3 ads per session |

---

## 🏪 Play Store Tips

1. **Title**: "SecureVPN - Fast & Safe VPN"
2. **Category**: Tools
3. **Description**: Mention free, no-log, military-grade encryption
4. **Screenshots**: Show connected state with green UI
5. **Content Rating**: Everyone
6. **Privacy Policy**: Required — must disclose VPN + ad data collection

### Required Play Store Declaration
- Declare VPN use in store listing
- Add `<uses-feature android:name="android.software.vpn" />` to manifest
- Answer "Does your app use VPN?" = Yes in Data Safety form

---

## 📈 Growth Strategy

1. **Phase 1** (0–1K users): Launch free, focus on ASO
2. **Phase 2** (1K–10K users): Add rewarded server unlocks, push notifications
3. **Phase 3** (10K+ users): Add premium subscription ($2.99/mo) alongside ads
4. **Phase 4** (50K+ users): Deploy own VPN server ($5/mo Vultr) for premium tier

---

## ⚠️ Legal Notes

- VPNGate is operated by the University of Tsukuba under academic research — legal to use commercially
- VPNBook credentials are publicly shared and free for commercial use
- Add a no-logs privacy policy to your website
- Do not market as bypassing regional restrictions in countries where VPN is restricted
