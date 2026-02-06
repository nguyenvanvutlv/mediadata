plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.hilt)
	alias(libs.plugins.ksp)
	alias(libs.plugins.kotlin.serialization)
	id("androidx.room") version "2.7.1"
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
		versionCode = 123
		versionName = "1.2.3"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		ndk {
			//noinspection ChromeOsAbiSupport
			abiFilters += listOf("armeabi-v7a", "arm64-v8a")
		}
	}

	buildTypes {
		release {
			isDebuggable = false
			isJniDebuggable = false
			isPseudoLocalesEnabled = false
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
			ndk {
				debugSymbolLevel = "FULL"
			}
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
	room {
		schemaDirectory("$projectDir/schemas")
	}
}

dependencies {
	implementation(libs.okhttp)
	implementation(libs.nanohttpd)
	implementation(libs.kotlinx.serialization)
	implementation(libs.androidx.compose.foundation)
	implementation(libs.androidx.appcompat.resources)
	implementation(libs.androidx.appcompat)
	implementation(libs.androidx.compose.foundation.layout)
	// PLAYER
	implementation(libs.androidx.media3.exoplayer)
	implementation(libs.androidx.media3.ui)
	implementation(libs.media3.ui)
	implementation(libs.androidx.media3.datasource)
	implementation(libs.androidx.media3.decoder)
	implementation(libs.androidx.media3.common)
	implementation(libs.androidx.media3.container)
	implementation(libs.androidx.media3.extractor)
	implementation(libs.androidx.media3.exoplayer.dash)
	implementation(libs.androidx.media3.exoplayer.hls)
	implementation(libs.androidx.media3.exoplayer.smoothstreaming)
	implementation(libs.androidx.media3.exoplayer.rtsp)
	implementation("io.github.anilbeesetti:nextlib-media3ext:1.8.0-0.9.0")
	implementation("io.github.anilbeesetti:nextlib-mediainfo:1.8.0-0.9.0")
	implementation(fileTree("libs") {
		include("lib-*.aar")
	})
	implementation(libs.androidx.recyclerview)
	implementation(libs.androidx.documentfile)
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
	//// ROOM DATABASE
	implementation(libs.androidx.paging.common)
	implementation(libs.androidx.room.paging)
	implementation(libs.androidx.room.runtime)
	ksp(libs.androidx.room.compiler)
	implementation(libs.androidx.room.ktx)
	implementation(libs.androidx.paging.compose)


	implementation(libs.androidx.material.icons.extended)
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.lifecycle.process)
	implementation(libs.androidx.work.runtime.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.graphics)
	implementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.tv.material)
	implementation(libs.androidx.tv.foundation)
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.compose.ui.test.junit4)
	debugImplementation(libs.androidx.compose.ui.tooling)
	debugImplementation(libs.androidx.compose.ui.test.manifest)
}
