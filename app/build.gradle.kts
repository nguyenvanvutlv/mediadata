plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.hilt)
	alias(libs.plugins.ksp)
}

android {
	namespace = "com.nvv.mediadata"
	compileSdk {
		version = release(36)
	}

	defaultConfig {
		applicationId = "com.nvv.mediadata"
		minSdk = 26
		targetSdk = 36
		versionCode = 101
		versionName = "1.0.1"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			isDebuggable = false
			isJniDebuggable = false
			isPseudoLocalesEnabled = false
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	kotlinOptions {
		jvmTarget = "11"
	}
	buildFeatures {
		compose = true
		buildConfig = true
	}
}

dependencies {
	implementation(libs.androidx.compose.foundation)
	implementation(libs.androidx.appcompat.resources)
	implementation(libs.androidx.appcompat)
	// PLAYER
	val media3 = "1.9.0"
	implementation("com.google.android.gms:play-services-cast-framework:22.2.0")
	implementation("androidx.media3:media3-session:$media3")
	implementation("androidx.media3:media3-datasource:$media3")
	implementation("androidx.media3:media3-decoder:$media3")
	implementation("androidx.media3:media3-common:$media3")
	implementation("androidx.media3:media3-container:$media3")
	implementation("androidx.media3:media3-extractor:$media3")
	implementation("androidx.mediarouter:mediarouter:1.8.1") {
		exclude(group = "androidx.media3", module = "media3-exoplayer")
	}
	implementation("androidx.media3:media3-cast:${media3}") {
		exclude(group = "androidx.media3", module = "media3-exoplayer")
	}
	implementation("androidx.media3:media3-exoplayer-dash:$media3") {
		exclude(group = "androidx.media3", module = "media3-exoplayer")
	}
	implementation("androidx.media3:media3-exoplayer-hls:$media3") {
		exclude(group = "androidx.media3", module = "media3-exoplayer")
	}
	implementation("androidx.media3:media3-exoplayer-smoothstreaming:$media3") {
		exclude(group = "androidx.media3", module = "media3-exoplayer")
	}
	implementation("androidx.media3:media3-exoplayer-rtsp:$media3") {
		exclude(group = "androidx.media3", module = "media3-exoplayer")
	}
	implementation(fileTree("libs") {
		include("lib-*.aar")
	})
	implementation("androidx.recyclerview:recyclerview:1.4.0")
	implementation("androidx.documentfile:documentfile:1.1.0")
	/// HILT
	implementation(libs.hilt.android)
	ksp(libs.hilt.android.compiler)
	implementation(libs.androidx.hilt.navigation.compose)
	/// NAVIGATION ANIMATION
	implementation(libs.androidx.navigation.compose)
	/// IMAGE
	implementation(libs.coil.compose)
	/// COIL IMAGE AND GIF
	implementation(libs.coil3.coil.compose)
	implementation(libs.coil.network.okhttp)
	implementation(libs.coil.gif)
	// LIFECIRLE
	implementation(libs.androidx.lifecycle.viewmodel.compose)
	// TIMBER
	implementation(libs.timber)
	implementation(libs.androidx.material.icons.extended)
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.work.runtime.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.graphics)
	implementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.compose.material3)
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.compose.ui.test.junit4)
	debugImplementation(libs.androidx.compose.ui.tooling)
	debugImplementation(libs.androidx.compose.ui.test.manifest)
}