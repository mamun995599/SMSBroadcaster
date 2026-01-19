# SMS Broadcaster by using kotlin

> **Provider:** LMArena  
> **Date:** 1/11/2026, 1:50:45 PM  
> **Chat ID:** `019babdf-9c63-7027-95b2-1f6813b6d054`  
> **Source:** [LMArena](https://lmarena.ai/c/019babdf-9c63-7027-95b2-1f6813b6d054)  

---

## [1] USER


## [2] ASSISTANT (claude-opus-4-5-20251101-thinking-32k)

<details>
<summary><strong>ðŸ’­ Thinking Process</strong></summary>

> The user wants me to create an Android app called "SMS Broadcaster" using Kotlin. Let me break down the requirements:
> 
> 1. **GUI Features:**
>    - Toggle on/off switch button
>    - WebSocket section: only 1 port field (default 8090 if empty)
>    - HTTP GET/POST section: IP address field, port field, radio button for GET or POST
> 
> 2. **Status Section:**
>    - WebSocket server: show connected devices list with their IPs
>    - HTTP GET/POST server: show console log for requests succeeded or failed
> 
> 3. **Permissions needed:**
>    - SMS (receive SMS)
>    - Notification
>    - Full network access
>    - Maybe foreground service permission
> 
> 4. **Background Service:**
>    - App must run in background even when closed
>    - Service should not stop when phone goes idle
>    - Only stops when user turns off the switch
> 
> 5. **Network Access:**
>    - Listen on all addresses (0.0.0.0)
>    - Accessible through local network
> 
> 6. **Core Functionality:**
>    - Listen to all incoming SMS live
>    - Broadcast SMS over WebSocket server
>    - Send SMS data to target HTTP GET/POST server
> 
> Let me create a comprehensive Android app with all these features:
> 
> Files needed:
> 1. AndroidManifest.xml
> 2. build.gradle (app level)
> 3. MainActivity.kt
> 4. SMSBroadcastService.kt
> 5. WebSocketServer.kt
> 6. HttpClient.kt
> 7. SMSReceiver.kt
> 8. activity_main.xml
> 9. Various layout files
> 
> I'll create a complete implementation:
> 
> ```kotlin
> // Project structure:
> // app/
> //   src/main/
> //     java/com/smsbroadcaster/
> //       MainActivity.kt
> //       service/
> //         SMSBroadcastService.kt
> //       websocket/
> //         WebSocketServer.kt
> //       http/
> //         HttpClient.kt
> 
> 
> //       receiver/
> //         SMSReceiver.kt
> //       model/
> //         SMSMessage.kt
> //         ConnectedClient.kt
> //     res/
> //       layout/
> //         activity_main.xml
> //       values/
> //         strings.xml
> //         colors.xml
> //         themes.xml
> //     AndroidManifest.xml
> ```
> 
> Now I'll write out all the code:

</details>

# SMS Broadcaster Android App

I'll create a complete Android app with all the requested features. This is a comprehensive implementation with multiple files.

## Project Structure

<details>
<summary><strong>ðŸ’» Code Block â€” 29 lines</strong></summary>

```
app/
â”œâ”€â”€ src/main/
â”‚   â”œâ”€â”€ java/com/smsbroadcaster/
â”‚   â”‚   â”œâ”€â”€ MainActivity.kt
â”‚   â”‚   â”œâ”€â”€ service/
â”‚   â”‚   â”‚   â””â”€â”€ SMSBroadcastService.kt
â”‚   â”‚   â”œâ”€â”€ websocket/
â”‚   â”‚   â”‚   â””â”€â”€ WebSocketServer.kt
â”‚   â”‚   â”œâ”€â”€ http/
â”‚   â”‚   â”‚   â””â”€â”€ HttpClient.kt
â”‚   â”‚   â”œâ”€â”€ receiver/
â”‚   â”‚   â”‚   â””â”€â”€ SMSReceiver.kt
â”‚   â”‚   â”œâ”€â”€ model/
â”‚   â”‚   â”‚   â”œâ”€â”€ SMSMessage.kt
â”‚   â”‚   â”‚   â””â”€â”€ LogEntry.kt
â”‚   â”‚   â””â”€â”€ utils/
â”‚   â”‚       â””â”€â”€ NetworkUtils.kt
â”‚   â”œâ”€â”€ res/
â”‚   â”‚   â”œâ”€â”€ layout/
â”‚   â”‚   â”‚   â””â”€â”€ activity_main.xml
â”‚   â”‚   â”œâ”€â”€ values/
â”‚   â”‚   â”‚   â”œâ”€â”€ strings.xml
â”‚   â”‚   â”‚   â”œâ”€â”€ colors.xml
â”‚   â”‚   â”‚   â””â”€â”€ themes.xml
â”‚   â”‚   â””â”€â”€ drawable/
â”‚   â”‚       â””â”€â”€ ic_notification.xml
â”‚   â””â”€â”€ AndroidManifest.xml
â”œâ”€â”€ build.gradle.kts (app level)
â””â”€â”€ build.gradle.kts (project level)
```

</details>

---

## 1. build.gradle.kts (Project Level)

<details>
<summary><strong>ðŸ’» Code Block (kotlin) â€” 5 lines</strong></summary>

```kotlin
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}
```

</details>

---

## 2. build.gradle.kts (App Level)

<details>
<summary><strong>ðŸ’» Code Block (kotlin) â€” 66 lines</strong></summary>

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.smsbroadcaster"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.smsbroadcaster"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-service:2.7.0")
    
    // WebSocket Server
    implementation("org.java-websocket:Java-WebSocket:1.5.4")
    
    // HTTP Client
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Gson for JSON
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Local Broadcast
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
}
```

</details>

---

## 3. AndroidManifest.xml

<details>
<summary><strong>ðŸ’» Code Block (xml) â€” 76 lines</strong></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- SMS Permissions -->
    <uses-permission android:name="android.permission.RECEIVE_SMS" />
    <uses-permission android:name="android.permission.READ_SMS" />
    
    <!-- Network Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    
    <!-- Foreground Service & Background -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
    
    <!-- Boot Receiver (Optional - to restart service on boot) -->
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.SMSBroadcaster"
        android:usesCleartextTraffic="true"
        tools:targetApi="31">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- SMS Broadcast Service -->
        <service
            android:name=".service.SMSBroadcastService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="dataSync"
            android:stopWithTask="false" />

        <!-- SMS Receiver -->
        <receiver
            android:name=".receiver.SMSReceiver"
            android:enabled="true"
            android:exported="true"
            android:permission="android.permission.BROADCAST_SMS">
            <intent-filter android:priority="999">
                <action android:name="android.provider.Telephony.SMS_RECEIVED" />
            </intent-filter>
        </receiver>
        
        <!-- Boot Receiver -->
        <receiver
            android:name=".receiver.BootReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.QUICKBOOT_POWERON" />
            </intent-filter>
        </receiver>

    </application>

</manifest>
```

</details>

---

## 4. res/values/strings.xml

<details>
<summary><strong>ðŸ’» Code Block (xml) â€” 19 lines</strong></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">SMS Broadcaster</string>
    <string name="server_status">Server Status</string>
    <string name="websocket_settings">WebSocket Server Settings</string>
    <string name="http_settings">HTTP GET/POST Settings</string>
    <string name="port_hint">Port (default: 8090)</string>
    <string name="ip_address_hint">Target IP Address</string>
    <string name="target_port_hint">Target Port</string>
    <string name="connected_clients">Connected Clients</string>
    <string name="http_console">HTTP Console Log</string>
    <string name="server_off">Server is OFF</string>
    <string name="server_on">Server is ON</string>
    <string name="get_method">GET</string>
    <string name="post_method">POST</string>
    <string name="no_clients">No connected clients</string>
    <string name="notification_channel_name">SMS Broadcaster Service</string>
    <string name="notification_channel_desc">Running SMS Broadcaster in background</string>
</resources>
```

</details>

---

## 5. res/values/colors.xml

<details>
<summary><strong>ðŸ’» Code Block (xml) â€” 18 lines</strong></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="primary">#1976D2</color>
    <color name="primary_dark">#1565C0</color>
    <color name="accent">#FF5722</color>
    <color name="background">#F5F5F5</color>
    <color name="card_background">#FFFFFF</color>
    <color name="text_primary">#212121</color>
    <color name="text_secondary">#757575</color>
    <color name="success">#4CAF50</color>
    <color name="error">#F44336</color>
    <color name="warning">#FF9800</color>
    <color name="console_background">#263238</color>
    <color name="console_text">#B2FF59</color>
    <color name="console_error">#FF5252</color>
    <color name="switch_on">#4CAF50</color>
    <color name="switch_off">#9E9E9E</color>
</resources>
```

</details>

---

## 6. res/values/themes.xml

<details>
<summary><strong>ðŸ’» Code Block (xml) â€” 9 lines</strong></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.SMSBroadcaster" parent="Theme.MaterialComponents.DayNight.DarkActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorPrimaryDark">@color/primary_dark</item>
        <item name="colorAccent">@color/accent</item>
        <item name="android:windowBackground">@color/background</item>
    </style>
</resources>
```

</details>

---

## 7. res/layout/activity_main.xml

<details>
<summary><strong>ðŸ’» Code Block (xml) â€” 347 lines</strong></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background"
    tools:context=".MainActivity">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <!-- App Title -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="@string/app_name"
            android:textSize="28sp"
            android:textStyle="bold"
            android:textColor="@color/primary"
            android:gravity="center"
            android:layout_marginBottom="8dp"/>

        <!-- Device IP Display -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            app:cardCornerRadius="8dp"
            app:cardElevation="2dp"
            app:cardBackgroundColor="@color/primary">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="12dp">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Device IP Addresses"
                    android:textColor="@android:color/white"
                    android:textSize="14sp"/>

                <TextView
                    android:id="@+id/tvDeviceIp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Loading..."
                    android:textColor="@android:color/white"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:layout_marginTop="4dp"/>
            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <!-- Main Toggle Switch -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp"
            app:cardBackgroundColor="@color/card_background">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:padding="20dp"
                android:gravity="center_vertical">

                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:orientation="vertical">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="@string/server_status"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="@color/text_primary"/>

                    <TextView
                        android:id="@+id/tvServerStatus"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="@string/server_off"
                        android:textSize="14sp"
                        android:textColor="@color/error"
                        android:layout_marginTop="4dp"/>
                </LinearLayout>

                <androidx.appcompat.widget.SwitchCompat
                    android:id="@+id/switchServer"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:scaleX="1.3"
                    android:scaleY="1.3"/>
            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <!-- WebSocket Settings -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp"
            app:cardBackgroundColor="@color/card_background">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/websocket_settings"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="@color/text_primary"
                    android:drawableStart="@drawable/ic_websocket"
                    android:drawablePadding="8dp"/>

                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="12dp"
                    style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/etWebSocketPort"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:hint="@string/port_hint"
                        android:inputType="number"
                        android:maxLength="5"/>
                </com.google.android.material.textfield.TextInputLayout>
            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <!-- HTTP GET/POST Settings -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp"
            app:cardBackgroundColor="@color/card_background">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/http_settings"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="@color/text_primary"
                    android:drawableStart="@drawable/ic_http"
                    android:drawablePadding="8dp"/>

                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="12dp"
                    style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/etHttpIpAddress"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:hint="@string/ip_address_hint"
                        android:inputType="text"/>
                </com.google.android.material.textfield.TextInputLayout>

                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/etHttpPort"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:hint="@string/target_port_hint"
                        android:inputType="number"
                        android:maxLength="5"/>
                </com.google.android.material.textfield.TextInputLayout>

                <RadioGroup
                    android:id="@+id/rgHttpMethod"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:layout_marginTop="12dp">

                    <com.google.android.material.radiobutton.MaterialRadioButton
                        android:id="@+id/rbGet"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="@string/get_method"
                        android:checked="true"/>

                    <com.google.android.material.radiobutton.MaterialRadioButton
                        android:id="@+id/rbPost"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="@string/post_method"/>
                </RadioGroup>
            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <!-- Connected Clients Section -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp"
            app:cardBackgroundColor="@color/card_background">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/connected_clients"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="@color/text_primary"
                    android:drawableStart="@drawable/ic_clients"
                    android:drawablePadding="8dp"/>

                <TextView
                    android:id="@+id/tvClientCount"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="0 clients connected"
                    android:textSize="12sp"
                    android:textColor="@color/text_secondary"
                    android:layout_marginTop="4dp"/>

                <androidx.recyclerview.widget.RecyclerView
                    android:id="@+id/rvConnectedClients"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:nestedScrollingEnabled="false"/>

                <TextView
                    android:id="@+id/tvNoClients"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="@string/no_clients"
                    android:textSize="14sp"
                    android:textColor="@color/text_secondary"
                    android:gravity="center"
                    android:padding="16dp"
                    android:visibility="visible"/>
            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <!-- HTTP Console Log Section -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp"
            app:cardBackgroundColor="@color/console_background">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="@string/http_console"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="@android:color/white"
                        android:drawableStart="@drawable/ic_console"
                        android:drawablePadding="8dp"/>

                    <Button
                        android:id="@+id/btnClearLog"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Clear"
                        android:textSize="12sp"
                        style="@style/Widget.MaterialComponents.Button.TextButton"/>
                </LinearLayout>

                <ScrollView
                    android:id="@+id/svConsole"
                    android:layout_width="match_parent"
                    android:layout_height="200dp"
                    android:layout_marginTop="8dp"
                    android:background="#1a1a1a"
                    android:padding="8dp">

                    <TextView
                        android:id="@+id/tvConsoleLog"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:fontFamily="monospace"
                        android:textSize="12sp"
                        android:textColor="@color/console_text"
                        android:text="> Console ready...\n"/>
                </ScrollView>
            </LinearLayout>
        </androidx.cardview.widget.CardView>

    </LinearLayout>
</ScrollView>
```

</details>

---

## 8. res/drawable/ic_websocket.xml

<details>
<summary><strong>ðŸ’» Code Block (xml) â€” 11 lines</strong></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="@color/primary">
    <path
        android:fillColor="#FF000000"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM11,19.93c-3.95,-0.49 -7,-3.85 -7,-7.93 0,-0.62 0.08,-1.21 0.21,-1.79L9,15v1c0,1.1 0.9,2 2,2v1.93zM17.9,17.39c-0.26,-0.81 -1,-1.39 -1.9,-1.39h-1v-3c0,-0.55 -0.45,-1 -1,-1H8v-2h2c0.55,0 1,-0.45 1,-1V7h2c1.1,0 2,-0.9 2,-2v-0.41c2.93,1.19 5,4.06 5,7.41 0,2.08 -0.8,3.97 -2.1,5.39z"/>
</vector>
```

</details>

---

## 9. res/drawable/ic_http.xml

<details>
<summary><strong>ðŸ’» Code Block (xml) â€” 11 lines</strong></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="@color/primary">
    <path
        android:fillColor="#FF000000"
        android:pathData="M4.5,11h-2V9H1v6h1.5v-2.5h2V15H6V9H4.5v2zM7,10.5h1.5V15H10v-4.5h1.5V9H7v1.5zM12.5,10.5H14V15h1.5v-4.5H17V9h-4.5v1.5zM21.5,9h-3v6h1.5v-2h1.5c0.83,0 1.5,-0.67 1.5,-1.5v-1c0,-0.83 -0.67,-1.5 -1.5,-1.5zM21.5,11.5h-1.5v-1h1.5v1z"/>
</vector>
```

</details>

---

## 10. res/drawable/ic_clients.xml

<details>
<summary><strong>ðŸ’» Code Block (xml) â€” 11 lines</strong></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="@color/primary">
    <path
        android:fillColor="#FF000000"
        android:pathData="M16,11c1.66,0 2.99,-1.34 2.99,-3S17.66,5 16,5c-1.66,0 -3,1.34 -3,3s1.34,3 3,3zM8,11c1.66,0 2.99,-1.34 2.99,-3S9.66,5 8,5C6.34,5 5,6.34 5,8s1.34,3 3,3zM8,13c-2.33,0 -7,1.17 -7,3.5V19h14v-2.5c0,-2.33 -4.67,-3.5 -7,-3.5zM16,13c-0.29,0 -0.62,0.02 -0.97,0.05 1.16,0.84 1.97,1.97 1.97,3.45V19h6v-2.5c0,-2.33 -4.67,-3.5 -7,-3.5z"/>
</vector>
```

</details>

---

## 11. res/drawable/ic_console.xml

<details>
<summary><strong>ðŸ’» Code Block (xml) â€” 11 lines</strong></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="@android:color/white">
    <path
        android:fillColor="#FF000000"
        android:pathData="M20,19V7H4v12h16m0,-14a2,2 0,0 1,2 2v12a2,2 0,0 1,-2 2H4a2,2 0,0 1,-2 -2V7a2,2 0,0 1,2 -2h16M13,13v4h6v-4h-6M6.5,10.5l-1.5,1.5 3,3 -3,3 1.5,1.5 4.5,-4.5 -4.5,-4.5z"/>
</vector>
```

</details>

---

## 12. res/drawable/ic_notification.xml

<details>
<summary><strong>ðŸ’» Code Block (xml) â€” 10 lines</strong></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M20,2H4C2.9,2 2,2.9 2,4v18l4,-4h14c1.1,0 2,-0.9 2,-2V4C22,2.9 21.1,2 20,2zM9,11H7V9h2V11zM13,11h-2V9h2V11zM17,11h-2V9h2V11z"/>
</vector>
```

</details>

---

## 13. res/layout/item_client.xml

<details>
<summary><strong>ðŸ’» Code Block (xml) â€” 40 lines</strong></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="8dp"
    android:background="?attr/selectableItemBackground"
    android:gravity="center_vertical">

    <View
        android:id="@+id/viewStatus"
        android:layout_width="8dp"
        android:layout_height="8dp"
        android:background="@drawable/circle_green"/>

    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:orientation="vertical"
        android:layout_marginStart="12dp">

        <TextView
            android:id="@+id/tvClientIp"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="192.168.1.100"
            android:textSize="14sp"
            android:textColor="@color/text_primary"
            android:textStyle="bold"/>

        <TextView
            android:id="@+id/tvClientInfo"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Connected at 12:00:00"
            android:textSize="12sp"
            android:textColor="@color/text_secondary"/>
    </LinearLayout>
</LinearLayout>
```

</details>

---

## 14. res/drawable/circle_green.xml

<details>
<summary><strong>ðŸ’» Code Block (xml) â€” 6 lines</strong></summary>

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="@color/success"/>
    <size android:width="8dp" android:height="8dp"/>
</shape>
```

</details>

---

## 15. Model Classes

### model/SMSMessage.kt

<details>
<summary><strong>ðŸ’» Code Block (kotlin) â€” 16 lines</strong></summary>

```kotlin
package com.smsbroadcaster.model

import com.google.gson.Gson

data class SMSMessage(
    val sender: String,
    val message: String,
    val timestamp: Long,
    val simSlot: Int = -1
) {
    fun toJson(): String = Gson().toJson(this)
    
    companion object {
        fun fromJson(json: String): SMSMessage = Gson().fromJson(json, SMSMessage::class.java)
    }
}
```

</details>

### model/LogEntry.kt

<details>
<summary><strong>ðŸ’» Code Block (kotlin) â€” 17 lines</strong></summary>

```kotlin
package com.smsbroadcaster.model

data class LogEntry(
    val timestamp: Long,
    val message: String,
    val isSuccess: Boolean,
    val type: LogType
)

enum class LogType {
    HTTP_REQUEST,
    HTTP_RESPONSE,
    WEBSOCKET,
    SMS,
    ERROR,
    INFO
}
```

</details>

### model/ConnectedClient.kt

<details>
<summary><strong>ðŸ’» Code Block (kotlin) â€” 7 lines</strong></summary>

```kotlin
package com.smsbroadcaster.model

data class ConnectedClient(
    val id: String,
    val ipAddress: String,
    val connectedAt: Long
)
```

</details>

---

## 16. Utils

### utils/NetworkUtils.kt

<details>
<summary><strong>ðŸ’» Code Block (kotlin) â€” 58 lines</strong></summary>

```kotlin
package com.smsbroadcaster.utils

import android.content.Context
import android.net.wifi.WifiManager
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

object NetworkUtils {
    
    fun getDeviceIpAddresses(): List<String> {
        val ipAddresses = mutableListOf<String>()
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        address.hostAddress?.let { ipAddresses.add(it) }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ipAddresses
    }
    
    fun getWifiIpAddress(context: Context): String? {
        try {
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipInt = wifiInfo.ipAddress
            return if (ipInt != 0) {
                String.format(
                    "%d.%d.%d.%d",
                    ipInt and 0xff,
                    ipInt shr 8 and 0xff,
                    ipInt shr 16 and 0xff,
                    ipInt shr 24 and 0xff
                )
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    
    fun getAllIpAddressesFormatted(): String {
        val ips = getDeviceIpAddresses()
        return if (ips.isEmpty()) {
            "No network connection"
        } else {
            ips.joinToString("\n")
        }
    }
}
```

</details>

### utils/PreferenceManager.kt

<details>
<summary><strong>ðŸ’» Code Block (kotlin) â€” 39 lines</strong></summary>

```kotlin
package com.smsbroadcaster.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREF_NAME = "sms_broadcaster_prefs"
        private const val KEY_SERVER_ENABLED = "server_enabled"
        private const val KEY_WEBSOCKET_PORT = "websocket_port"
        private const val KEY_HTTP_IP = "http_ip"
        private const val KEY_HTTP_PORT = "http_port"
        private const val KEY_HTTP_METHOD = "http_method"
        const val DEFAULT_WEBSOCKET_PORT = 8090
    }
    
    var isServerEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVER_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVER_ENABLED, value).apply()
    
    var webSocketPort: Int
        get() = prefs.getInt(KEY_WEBSOCKET_PORT, DEFAULT_WEBSOCKET_PORT)
        set(value) = prefs.edit().putInt(KEY_WEBSOCKET_PORT, value).apply()
    
    var httpIp: String
        get() = prefs.getString(KEY_HTTP_IP, "") ?: ""
        set(value) = prefs.edit().putString(KEY_HTTP_IP, value).apply()
    
    var httpPort: Int
        get() = prefs.getInt(KEY_HTTP_PORT, 80)
        set(value) = prefs.edit().putInt(KEY_HTTP_PORT, value).apply()
    
    var httpMethod: String
        get() = prefs.getString(KEY_HTTP_METHOD, "GET") ?: "GET"
        set(value) = prefs.edit().putString(KEY_HTTP_METHOD, value).apply()
}
```

</details>

---

## 17. WebSocket Server

### websocket/SMSWebSocketServer.kt

<details>
<summary><strong>ðŸ’» Code Block (kotlin) â€” 101 lines</strong></summary>

```kotlin
package com.smsbroadcaster.websocket

import android.util.Log
import com.smsbroadcaster.model.ConnectedClient
import com.smsbroadcaster.model.SMSMessage
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

class SMSWebSocketServer(
    port: Int,
    private val listener: WebSocketServerListener
) : WebSocketServer(InetSocketAddress("0.0.0.0", port)) {

    private val connectedClients = ConcurrentHashMap<String, ConnectedClient>()
    
    companion object {
        private const val TAG = "SMSWebSocketServer"
    }
    
    interface WebSocketServerListener {
        fun onClientConnected(client: ConnectedClient)
        fun onClientDisconnected(clientId: String)
        fun onServerStarted(port: Int)
        fun onServerStopped()
        fun onServerError(error: String)
        fun onClientsUpdated(clients: List<ConnectedClient>)
    }

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        conn?.let { socket ->
            val clientIp = socket.remoteSocketAddress?.address?.hostAddress ?: "Unknown"
            val clientId = "${clientIp}:${socket.remoteSocketAddress?.port}"
            val client = ConnectedClient(
                id = clientId,
                ipAddress = clientIp,
                connectedAt = System.currentTimeMillis()
            )
            connectedClients[clientId] = client
            Log.d(TAG, "Client connected: $clientIp")
            listener.onClientConnected(client)
            listener.onClientsUpdated(connectedClients.values.toList())
            
            // Send welcome message
            socket.send("""{"type":"welcome","message":"Connected to SMS Broadcaster"}""")
        }
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        conn?.let { socket ->
            val clientIp = socket.remoteSocketAddress?.address?.hostAddress ?: "Unknown"
            val clientId = "${clientIp}:${socket.remoteSocketAddress?.port}"
            connectedClients.remove(clientId)
            Log.d(TAG, "Client disconnected: $clientIp")
            listener.onClientDisconnected(clientId)
            listener.onClientsUpdated(connectedClients.values.toList())
        }
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        Log.d(TAG, "Message received: $message")
        // Echo back or handle commands if needed
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        Log.e(TAG, "WebSocket error: ${ex?.message}")
        listener.onServerError(ex?.message ?: "Unknown error")
    }

    override fun onStart() {
        Log.d(TAG, "WebSocket server started on port ${this.port}")
        listener.onServerStarted(this.port)
    }
    
    fun broadcastSMS(smsMessage: SMSMessage) {
        val json = smsMessage.toJson()
        val wrappedMessage = """{"type":"sms","data":$json}"""
        broadcast(wrappedMessage)
        Log.d(TAG, "Broadcasted SMS to ${connectedClients.size} clients")
    }
    
    fun getConnectedClients(): List<ConnectedClient> {
        return connectedClients.values.toList()
    }
    
    fun getClientCount(): Int {
        return connectedClients.size
    }
    
    fun stopServer() {
        try {
            stop(1000)
            connectedClients.clear()
            listener.onServerStopped()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server: ${e.message}")
        }
    }
}
```

</details>

---

## 18. HTTP Client

### http/HttpClient.kt

<details>
<summary><strong>ðŸ’» Code Block (kotlin) â€” 100 lines</strong></summary>

```kotlin
package com.smsbroadcaster.http

import android.util.Log
import com.smsbroadcaster.model.SMSMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class HttpClient(
    private val listener: HttpClientListener
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
    
    companion object {
        private const val TAG = "HttpClient"
    }
    
    interface HttpClientListener {
        fun onRequestSuccess(message: String)
        fun onRequestFailure(error: String)
    }
    
    suspend fun sendSMS(
        ipAddress: String,
        port: Int,
        method: String,
        smsMessage: SMSMessage
    ) = withContext(Dispatchers.IO) {
        if (ipAddress.isBlank()) {
            Log.d(TAG, "HTTP target not configured, skipping")
            return@withContext
        }
        
        val baseUrl = "http://$ipAddress:$port/sms"
        
        try {
            val request = when (method.uppercase()) {
                "GET" -> createGetRequest(baseUrl, smsMessage)
                "POST" -> createPostRequest(baseUrl, smsMessage)
                else -> createGetRequest(baseUrl, smsMessage)
            }
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val successMsg = "[${getTimestamp()}] âœ“ $method $baseUrl - ${response.code}"
                Log.d(TAG, successMsg)
                listener.onRequestSuccess(successMsg)
            } else {
                val errorMsg = "[${getTimestamp()}] âœ— $method $baseUrl - ${response.code} ${response.message}"
                Log.e(TAG, errorMsg)
                listener.onRequestFailure(errorMsg)
            }
            response.close()
        } catch (e: Exception) {
            val errorMsg = "[${getTimestamp()}] âœ— $method $baseUrl - ${e.message}"
            Log.e(TAG, errorMsg)
            listener.onRequestFailure(errorMsg)
        }
    }
    
    private fun createGetRequest(baseUrl: String, smsMessage: SMSMessage): Request {
        val encodedSender = URLEncoder.encode(smsMessage.sender, "UTF-8")
        val encodedMessage = URLEncoder.encode(smsMessage.message, "UTF-8")
        val url = "$baseUrl?sender=$encodedSender&message=$encodedMessage&timestamp=${smsMessage.timestamp}"
        
        return Request.Builder()
            .url(url)
            .get()
            .addHeader("Content-Type", "application/json")
            .build()
    }
    
    private fun createPostRequest(baseUrl: String, smsMessage: SMSMessage): Request {
        val json = smsMessage.toJson()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toRequestBody(mediaType)
        
        return Request.Builder()
            .url(baseUrl)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()
    }
    
    private fun getTimestamp(): String {
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}
```

</details>

---

## 19. SMS Receiver

### receiver/SMSReceiver.kt

<details>
<summary><strong>ðŸ’» Code Block (kotlin) â€” 50 lines</strong></summary>

```kotlin
package com.smsbroadcaster.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.smsbroadcaster.model.SMSMessage
import com.smsbroadcaster.service.SMSBroadcastService

class SMSReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "SMSReceiver"
        const val ACTION_SMS_RECEIVED = "com.smsbroadcaster.SMS_RECEIVED"
        const val EXTRA_SMS_MESSAGE = "sms_message"
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }
        
        context?.let { ctx ->
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            
            messages?.forEach { smsMessage ->
                val sender = smsMessage.displayOriginatingAddress ?: "Unknown"
                val body = smsMessage.messageBody ?: ""
                val timestamp = smsMessage.timestampMillis
                
                Log.d(TAG, "SMS received from: $sender")
                
                val sms = SMSMessage(
                    sender = sender,
                    message = body,
                    timestamp = timestamp
                )
                
                // Send to service
                val serviceIntent = Intent(ctx, SMSBroadcastService::class.java).apply {
                    action = ACTION_SMS_RECEIVED
                    putExtra(EXTRA_SMS_MESSAGE, sms.toJson())
                }
                ctx.startService(serviceIntent)
            }
        }
    }
}
```

</details>

### receiver/BootReceiver.kt

<details>
<summary><strong>ðŸ’» Code Block (kotlin) â€” 37 lines</strong></summary>

```kotlin
package com.smsbroadcaster.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.smsbroadcaster.service.SMSBroadcastService
import com.smsbroadcaster.utils.PreferenceManager

class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            context?.let { ctx ->
                val prefManager = PreferenceManager(ctx)
                if (prefManager.isServerEnabled) {
                    Log.d(TAG, "Boot completed, restarting SMS Broadcaster service")
                    val serviceIntent = Intent(ctx, SMSBroadcastService::class.java).apply {
                        action = SMSBroadcastService.ACTION_START_SERVICE
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ctx.startForegroundService(serviceIntent)
                    } else {
                        ctx.startService(serviceIntent)
                    }
                }
            }
        }
    }
}
```

</details>

---

## 20. Main Service

### service/SMSBroadcastService.kt

<details>
<summary><strong>ðŸ’» Code Block (kotlin) â€” 310 lines</strong></summary>

```kotlin
package com.smsbroadcaster.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.smsbroadcaster.MainActivity
import com.smsbroadcaster.R
import com.smsbroadcaster.http.HttpClient
import com.smsbroadcaster.model.ConnectedClient
import com.smsbroadcaster.model.SMSMessage
import com.smsbroadcaster.receiver.SMSReceiver
import com.smsbroadcaster.utils.PreferenceManager
import com.smsbroadcaster.websocket.SMSWebSocketServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SMSBroadcastService : Service(), 
    SMSWebSocketServer.WebSocketServerListener,
    HttpClient.HttpClientListener {

    private val binder = LocalBinder()
    private var webSocketServer: SMSWebSocketServer? = null
    private lateinit var httpClient: HttpClient
    private lateinit var prefManager: PreferenceManager
    private var wakeLock: PowerManager.WakeLock? = null
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        private const val TAG = "SMSBroadcastService"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "sms_broadcaster_channel"
        
        const val ACTION_START_SERVICE = "com.smsbroadcaster.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.smsbroadcaster.STOP_SERVICE"
        
        // Broadcast actions for UI updates
        const val ACTION_CLIENT_UPDATE = "com.smsbroadcaster.CLIENT_UPDATE"
        const val ACTION_LOG_UPDATE = "com.smsbroadcaster.LOG_UPDATE"
        const val ACTION_SERVER_STATUS = "com.smsbroadcaster.SERVER_STATUS"
        
        const val EXTRA_CLIENTS = "clients"
        const val EXTRA_LOG_MESSAGE = "log_message"
        const val EXTRA_LOG_SUCCESS = "log_success"
        const val EXTRA_SERVER_RUNNING = "server_running"
        const val EXTRA_PORT = "port"
    }
    
    inner class LocalBinder : Binder() {
        fun getService(): SMSBroadcastService = this@SMSBroadcastService
    }

    override fun onCreate() {
        super.onCreate()
        prefManager = PreferenceManager(this)
        httpClient = HttpClient(this)
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> startBroadcastServer()
            ACTION_STOP_SERVICE -> stopBroadcastServer()
            SMSReceiver.ACTION_SMS_RECEIVED -> {
                intent.getStringExtra(SMSReceiver.EXTRA_SMS_MESSAGE)?.let { json ->
                    val smsMessage = SMSMessage.fromJson(json)
                    handleIncomingSMS(smsMessage)
                }
            }
        }
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SMSBroadcaster::WakeLock"
        ).apply {
            acquire()
        }
    }
    
    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    private fun startBroadcastServer() {
        val port = prefManager.webSocketPort.takeIf { it > 0 } 
            ?: PreferenceManager.DEFAULT_WEBSOCKET_PORT
        
        try {
            webSocketServer?.stopServer()
            webSocketServer = SMSWebSocketServer(port, this)
            webSocketServer?.isReuseAddr = true
            webSocketServer?.start()
            
            startForeground(NOTIFICATION_ID, createNotification("Server running on port $port"))
            prefManager.isServerEnabled = true
            
            Log.d(TAG, "WebSocket server started on port $port")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start server: ${e.message}")
            broadcastLog("Failed to start server: ${e.message}", false)
        }
    }

    private fun stopBroadcastServer() {
        webSocketServer?.stopServer()
        webSocketServer = null
        prefManager.isServerEnabled = false
        
        broadcastServerStatus(false, 0)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        
        Log.d(TAG, "WebSocket server stopped")
    }
    
    private fun handleIncomingSMS(smsMessage: SMSMessage) {
        Log.d(TAG, "Processing SMS from: ${smsMessage.sender}")
        
        // Broadcast via WebSocket
        webSocketServer?.broadcastSMS(smsMessage)
        broadcastLog("SMS received from ${smsMessage.sender}", true)
        
        // Send via HTTP if configured
        val httpIp = prefManager.httpIp
        val httpPort = prefManager.httpPort
        val httpMethod = prefManager.httpMethod
        
        if (httpIp.isNotBlank()) {
            serviceScope.launch {
                httpClient.sendSMS(httpIp, httpPort, httpMethod, smsMessage)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, SMSBroadcastService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .addAction(0, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    fun updateNotification(text: String) {
        val notification = createNotification(text)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    // WebSocket Server Listener implementations
    override fun onClientConnected(client: ConnectedClient) {
        broadcastClientUpdate()
        updateNotification("${getClientCount()} client(s) connected")
    }
    
    override fun onClientDisconnected(clientId: String) {
        broadcastClientUpdate()
        updateNotification("${getClientCount()} client(s) connected")
    }
    
    override fun onServerStarted(port: Int) {
        broadcastServerStatus(true, port)
        broadcastLog("WebSocket server started on port $port", true)
    }
    
    override fun onServerStopped() {
        broadcastServerStatus(false, 0)
        broadcastLog("WebSocket server stopped", true)
    }
    
    override fun onServerError(error: String) {
        broadcastLog("Server error: $error", false)
    }
    
    override fun onClientsUpdated(clients: List<ConnectedClient>) {
        broadcastClientUpdate()
    }
    
    // HTTP Client Listener implementations
    override fun onRequestSuccess(message: String) {
        broadcastLog(message, true)
    }
    
    override fun onRequestFailure(error: String) {
        broadcastLog(error, false)
    }
    
    // Broadcast helpers
    private fun broadcastClientUpdate() {
        val clients = webSocketServer?.getConnectedClients() ?: emptyList()
        val intent = Intent(ACTION_CLIENT_UPDATE).apply {
            putExtra(EXTRA_CLIENTS, ArrayList(clients.map { "${it.ipAddress}|${it.connectedAt}" }))
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
    
    private fun broadcastLog(message: String, isSuccess: Boolean) {
        val intent = Intent(ACTION_LOG_UPDATE).apply {
            putExtra(EXTRA_LOG_MESSAGE, message)
            putExtra(EXTRA_LOG_SUCCESS, isSuccess)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
    
    private fun broadcastServerStatus(isRunning: Boolean, port: Int) {
        val intent = Intent(ACTION_SERVER_STATUS).apply {
            putExtra(EXTRA_SERVER_RUNNING, isRunning)
            putExtra(EXTRA_PORT, port)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
    
    fun getConnectedClients(): List<ConnectedClient> {
        return webSocketServer?.getConnectedClients() ?: emptyList()
    }
    
    fun getClientCount(): Int {
        return webSocketServer?.getClientCount() ?: 0
    }
    
    fun isServerRunning(): Boolean {
        return webSocketServer != null
    }

    override fun onDestroy() {
        releaseWakeLock()
        webSocketServer?.stopServer()
        super.onDestroy()
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        // Restart service if task is removed but server should be running
        if (prefManager.isServerEnabled) {
            val restartIntent = Intent(this, SMSBroadcastService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(restartIntent)
            } else {
                startService(restartIntent)
            }
        }
        super.onTaskRemoved(rootIntent)
    }
}
```

</details>

---

## 21. Client Adapter

### adapter/ClientAdapter.kt

<details>
<summary><strong>ðŸ’» Code Block (kotlin) â€” 49 lines</strong></summary>

```kotlin
package com.smsbroadcaster.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smsbroadcaster.R
import com.smsbroadcaster.model.ConnectedClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClientAdapter : ListAdapter<ConnectedClient, ClientAdapter.ClientViewHolder>(ClientDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_client, parent, false)
        return ClientViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ClientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvClientIp: TextView = itemView.findViewById(R.id.tvClientIp)
        private val tvClientInfo: TextView = itemView.findViewById(R.id.tvClientInfo)
        
        fun bind(client: ConnectedClient) {
            tvClientIp.text = client.ipAddress
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val connectedTime = sdf.format(Date(client.connectedAt))
            tvClientInfo.text = "Connected at $connectedTime"
        }
    }
    
    class ClientDiffCallback : DiffUtil.ItemCallback<ConnectedClient>() {
        override fun areItemsTheSame(oldItem: ConnectedClient, newItem: ConnectedClient): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: ConnectedClient, newItem: ConnectedClient): Boolean {
            return oldItem == newItem
        }
    }
}
```

</details>

---

## 22. MainActivity

### MainActivity.kt

<details>
<summary><strong>ðŸ’» Code Block (kotlin) â€” 326 lines</strong></summary>

```kotlin
package com.smsbroadcaster

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.smsbroadcaster.adapter.ClientAdapter
import com.smsbroadcaster.databinding.ActivityMainBinding
import com.smsbroadcaster.model.ConnectedClient
import com.smsbroadcaster.service.SMSBroadcastService
import com.smsbroadcaster.utils.NetworkUtils
import com.smsbroadcaster.utils.PreferenceManager

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefManager: PreferenceManager
    private lateinit var clientAdapter: ClientAdapter
    
    private var smsBroadcastService: SMSBroadcastService? = null
    private var isServiceBound = false
    
    private val requiredPermissions = mutableListOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Some permissions denied. App may not work properly.", Toast.LENGTH_LONG).show()
        }
    }
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SMSBroadcastService.LocalBinder
            smsBroadcastService = binder.getService()
            isServiceBound = true
            updateUIFromService()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            smsBroadcastService = null
            isServiceBound = false
        }
    }
    
    private val serviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                SMSBroadcastService.ACTION_CLIENT_UPDATE -> {
                    updateClientList()
                }
                SMSBroadcastService.ACTION_LOG_UPDATE -> {
                    val message = intent.getStringExtra(SMSBroadcastService.EXTRA_LOG_MESSAGE) ?: ""
                    val isSuccess = intent.getBooleanExtra(SMSBroadcastService.EXTRA_LOG_SUCCESS, true)
                    appendToConsole(message, isSuccess)
                }
                SMSBroadcastService.ACTION_SERVER_STATUS -> {
                    val isRunning = intent.getBooleanExtra(SMSBroadcastService.EXTRA_SERVER_RUNNING, false)
                    val port = intent.getIntExtra(SMSBroadcastService.EXTRA_PORT, 0)
                    updateServerStatus(isRunning, port)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefManager = PreferenceManager(this)
        
        setupUI()
        checkAndRequestPermissions()
        requestBatteryOptimizationExemption()
        displayDeviceIPs()
        loadSavedSettings()
        
        // Register broadcast receivers
        val filter = IntentFilter().apply {
            addAction(SMSBroadcastService.ACTION_CLIENT_UPDATE)
            addAction(SMSBroadcastService.ACTION_LOG_UPDATE)
            addAction(SMSBroadcastService.ACTION_SERVER_STATUS)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceReceiver, filter)
        
        // Bind to service if it's running
        if (prefManager.isServerEnabled) {
            bindToService()
        }
    }
    
    private fun setupUI() {
        // Setup RecyclerView for clients
        clientAdapter = ClientAdapter()
        binding.rvConnectedClients.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = clientAdapter
        }
        
        // Setup switch listener
        binding.switchServer.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                saveSettings()
                startBroadcastService()
            } else {
                stopBroadcastService()
            }
        }
        
        // Setup radio button listener
        binding.rgHttpMethod.setOnCheckedChangeListener { _, checkedId ->
            prefManager.httpMethod = if (checkedId == R.id.rbGet) "GET" else "POST"
        }
        
        // Setup clear log button
        binding.btnClearLog.setOnClickListener {
            binding.tvConsoleLog.text = "> Console cleared...\n"
        }
    }
    
    private fun loadSavedSettings() {
        binding.etWebSocketPort.setText(
            if (prefManager.webSocketPort > 0) prefManager.webSocketPort.toString() else ""
        )
        binding.etHttpIpAddress.setText(prefManager.httpIp)
        binding.etHttpPort.setText(
            if (prefManager.httpPort > 0) prefManager.httpPort.toString() else ""
        )
        
        if (prefManager.httpMethod == "POST") {
            binding.rbPost.isChecked = true
        } else {
            binding.rbGet.isChecked = true
        }
        
        // Set switch state based on service status
        binding.switchServer.isChecked = prefManager.isServerEnabled
    }
    
    private fun saveSettings() {
        val wsPort = binding.etWebSocketPort.text.toString().toIntOrNull() 
            ?: PreferenceManager.DEFAULT_WEBSOCKET_PORT
        prefManager.webSocketPort = wsPort
        
        prefManager.httpIp = binding.etHttpIpAddress.text.toString().trim()
        prefManager.httpPort = binding.etHttpPort.text.toString().toIntOrNull() ?: 80
        prefManager.httpMethod = if (binding.rbPost.isChecked) "POST" else "GET"
    }
    
    private fun displayDeviceIPs() {
        val ips = NetworkUtils.getAllIpAddressesFormatted()
        binding.tvDeviceIp.text = ips
    }
    
    private fun startBroadcastService() {
        val intent = Intent(this, SMSBroadcastService::class.java).apply {
            action = SMSBroadcastService.ACTION_START_SERVICE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        bindToService()
        
        val port = prefManager.webSocketPort.takeIf { it > 0 } 
            ?: PreferenceManager.DEFAULT_WEBSOCKET_PORT
        updateServerStatus(true, port)
    }
    
    private fun stopBroadcastService() {
        val intent = Intent(this, SMSBroadcastService::class.java).apply {
            action = SMSBroadcastService.ACTION_STOP_SERVICE
        }
        startService(intent)
        
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
        
        updateServerStatus(false, 0)
        clientAdapter.submitList(emptyList())
        updateClientCount(0)
    }
    
    private fun bindToService() {
        val intent = Intent(this, SMSBroadcastService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    private fun updateUIFromService() {
        smsBroadcastService?.let { service ->
            if (service.isServerRunning()) {
                updateServerStatus(true, prefManager.webSocketPort)
                updateClientList()
            }
        }
    }
    
    private fun updateClientList() {
        smsBroadcastService?.let { service ->
            val clients = service.getConnectedClients()
            clientAdapter.submitList(clients)
            updateClientCount(clients.size)
            
            binding.tvNoClients.visibility = if (clients.isEmpty()) View.VISIBLE else View.GONE
            binding.rvConnectedClients.visibility = if (clients.isEmpty()) View.GONE else View.VISIBLE
        }
    }
    
    private fun updateClientCount(count: Int) {
        binding.tvClientCount.text = "$count client(s) connected"
    }
    
    private fun updateServerStatus(isRunning: Boolean, port: Int) {
        if (isRunning) {
            binding.tvServerStatus.text = "Server is ON (Port: $port)"
            binding.tvServerStatus.setTextColor(ContextCompat.getColor(this, R.color.success))
        } else {
            binding.tvServerStatus.text = getString(R.string.server_off)
            binding.tvServerStatus.setTextColor(ContextCompat.getColor(this, R.color.error))
        }
    }
    
    private fun appendToConsole(message: String, isSuccess: Boolean) {
        val color = if (isSuccess) {
            ContextCompat.getColor(this, R.color.console_text)
        } else {
            ContextCompat.getColor(this, R.color.console_error)
        }
        
        val spannable = SpannableStringBuilder("$message\n")
        spannable.setSpan(
            ForegroundColorSpan(color),
            0,
            spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        binding.tvConsoleLog.append(spannable)
        
        // Auto-scroll to bottom
        binding.svConsole.post {
            binding.svConsole.fullScroll(View.FOCUS_DOWN)
        }
    }
    
    private fun checkAndRequestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
    
    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                AlertDialog.Builder(this)
                    .setTitle("Battery Optimization")
                    .setMessage("To ensure the service runs reliably in background, please disable battery optimization for this app.")
                    .setPositiveButton("Open Settings") { _, _ ->
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("Later", null)
                    .show()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        displayDeviceIPs()
        if (prefManager.isServerEnabled && !isServiceBound) {
            bindToService()
        }
    }
    
    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceReceiver)
        if (isServiceBound) {
            unbindService(serviceConnection)
        }
        super.onDestroy()
    }
}
```

</details>

---

## How to Build and Run

### Steps:

1. **Create a new Android project** in Android Studio with:
   - Name: SMS Broadcaster
   - Package: com.smsbroadcaster
   - Minimum SDK: API 26 (Android 8.0)
   - Language: Kotlin

2. **Replace/Add all the files** according to the structure provided above.

3. **Sync Gradle** and build the project.

4. **Install on device** (not emulator for SMS testing).

5. **Grant all permissions** when prompted.

6. **Disable battery optimization** for the app when prompted.

---

## Usage

### WebSocket Connection Example (JavaScript):

<details>
<summary><strong>ðŸ’» Code Block (javascript) â€” 17 lines</strong></summary>

```javascript
const ws = new WebSocket('ws://192.168.1.100:8090');

ws.onopen = () => {
    console.log('Connected to SMS Broadcaster');
};

ws.onmessage = (event) => {
    const data = JSON.parse(event.data);
    if (data.type === 'sms') {
        console.log('SMS Received:', data.data);
        // data.data contains: sender, message, timestamp
    }
};

ws.onerror = (error) => {
    console.error('WebSocket error:', error);
};
```

</details>

### HTTP Endpoint (Target Server Example - Node.js):

<details>
<summary><strong>ðŸ’» Code Block (javascript) â€” 21 lines</strong></summary>

```javascript
const express = require('express');
const app = express();
app.use(express.json());

// For GET requests
app.get('/sms', (req, res) => {
    const { sender, message, timestamp } = req.query;
    console.log(`SMS from ${sender}: ${message}`);
    res.json({ status: 'received' });
});

// For POST requests
app.post('/sms', (req, res) => {
    const { sender, message, timestamp } = req.body;
    console.log(`SMS from ${sender}: ${message}`);
    res.json({ status: 'received' });
});

app.listen(3000, () => {
    console.log('Server running on port 3000');
});
```

</details>

---

## Features Summary

| Feature | Implementation |
|---------|---------------|
| Toggle Switch | Starts/stops the broadcast service |
| WebSocket Server | Broadcasts SMS to all connected clients on 0.0.0.0 |
| Default Port | 8090 if WebSocket port field is empty |
| HTTP GET/POST | Sends SMS data to configured target server |
| Connected Clients | Real-time list with IP addresses |
| Console Log | Shows HTTP request success/failure |
| Background Service | Foreground service with wake lock |
| Persist on Kill | Service restarts if killed |
| Boot Start | Restarts on device boot if enabled |
| Battery Optimization | Prompts to disable for reliability |

---

