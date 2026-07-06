[![](https://jitpack.io/v/appmeasurely/android-sdk.svg)](https://jitpack.io/#appmeasurely/android-sdk)

# AppMeasurely Android SDK

Mobile attribution and analytics tracking for Android apps.

## Installation

### Step 1 — Add JitPack to your project `build.gradle`

```gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2 — Add the dependency

```gradle
dependencies {
    implementation 'com.github.appmeasurely:android-sdk:1.0.0'
}
```

### Step 3 — Add Internet permission to `AndroidManifest.xml`

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### Step 4 — Initialize in your `Application` class

**Kotlin:**
```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppMeasurely.init(this, "YOUR_APP_KEY")
    }
}
```

**Java:**
```java
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppMeasurely.init(this, "YOUR_APP_KEY");
    }
}
```

Register your Application class in `AndroidManifest.xml`:
```xml
<application
    android:name=".MyApp"
    ...>
```

---

## Usage

### Track Custom Events

**Kotlin:**
```kotlin
// Simple event
AppMeasurely.trackEvent("level_complete")

// Event with properties
AppMeasurely.trackEvent("purchase_initiated", mapOf(
    "product_id" to "gold_pack",
    "price" to 4.99,
    "currency" to "USD"
))
```

**Java:**
```java
// Simple event
AppMeasurely.trackEvent("level_complete");

// Event with properties
Map<String, Object> props = new HashMap<>();
props.put("product_id", "gold_pack");
props.put("price", 4.99);
props.put("currency", "USD");
AppMeasurely.trackEvent("purchase_initiated", props);
```

### Track Revenue

**Kotlin:**
```kotlin
AppMeasurely.trackRevenue(9.99, "USD")
AppMeasurely.trackRevenue(4.99, "USD", "subscription_monthly", null)
```

**Java:**
```java
AppMeasurely.trackRevenue(9.99, "USD");
AppMeasurely.trackRevenue(4.99, "USD", "subscription_monthly", null);
```

### Set User Properties

```kotlin
AppMeasurely.setUserProperty("user_type", "premium")
AppMeasurely.setUserProperty("account_age_days", 30)
```

---

## What's Tracked Automatically

| Event | Description |
|-------|-------------|
| `install` | Fired once on first app launch |
| `app_open` | Fired on every subsequent launch |
| `session_start` | When app comes to foreground |
| `session_end` | When app goes to background (includes duration) |

---

## Advanced Configuration

```kotlin
val config = AMConfig("YOUR_APP_KEY")
    .setDebugMode(true)          // Enable logging
    .setTrackSessions(true)      // Auto session tracking
    .setTrackInstalls(true)      // Auto install detection
    .setSendInterval(30)         // Send queue every 30 seconds

AppMeasurely.init(this, config)
```

---

## Attribution Support

The SDK automatically parses UTM parameters from the Google Play install referrer:

| Parameter | Mapped To |
|-----------|-----------|
| `utm_source` | `media_source` |
| `utm_campaign` | `campaign` |
| `utm_term` | `ad_set` |
| `af_pid` | `media_source` (AppsFlyer format) |
| `af_c` | `campaign` (AppsFlyer format) |

---

## Get Your App Key

1. Log in to your [AppMeasurely dashboard](https://app.appmeasurely.com)
2. Go to **SDK Docs** in the left sidebar
3. Select your app from the dropdown
4. Copy your App Key

---

## Support

- Documentation: [appmeasurely.com/docs](https://appmeasurely.com/docs)
- Email: support@appmeasurely.com
