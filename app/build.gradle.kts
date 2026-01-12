plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	id("com.google.gms.google-services")
	alias(libs.plugins.hilt)
	alias(libs.plugins.ksp)

	alias(libs.plugins.google.firebase.crashlytics)
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
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
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
	/// FIREBASE
	implementation(platform(libs.firebase.bom))
	implementation(libs.firebase.analytics)
	implementation(libs.firebase.crashlytics)
	implementation(libs.firebase.database.ktx)
	implementation(libs.firebase.auth)
	implementation(libs.firebase.firestore)
	// GOOGLE
	implementation(libs.googleid)
	implementation(libs.androidx.credentials)
	implementation(libs.androidx.credentials.play.services.auth)

	/// HILT
	implementation(libs.hilt.android)
	ksp(libs.hilt.android.compiler)
	implementation(libs.androidx.hilt.navigation.compose)
	/// IMAGE
	implementation(libs.coil.compose)
	/// COIL IMAGE AND GIF
	implementation(libs.coil3.coil.compose)
	implementation(libs.coil.network.okhttp)
	implementation(libs.coil.gif)
	// LIFECIRLE
	implementation(libs.androidx.lifecycle.viewmodel.compose)
	// TIMBER
	implementation("com.jakewharton.timber:timber:5.0.1")
	implementation(libs.androidx.material.icons.extended)

	/// YOUTUBE PLAYER
	implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:13.0.0")

	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
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