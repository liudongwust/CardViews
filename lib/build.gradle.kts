import com.vanniktech.maven.publish.SonatypeHost
import com.vanniktech.maven.publish.AndroidSingleVariantLibrary


plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    id("com.vanniktech.maven.publish") version "0.30.0"
    id("signing")
}

android {
    namespace = "com.wustfly.cardviews"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    dataBinding {
        enable = true
    }
    viewBinding {
        enable = true
    }
}

dependencies {

    api(libs.androidx.constraintlayout)
    api(libs.androidx.appcompat)
    api(libs.androidx.viewpager2)
}

mavenPublishing {

    configure(AndroidSingleVariantLibrary(
        // the published variant
        variant = "release",
        // whether to publish a sources jar
        sourcesJar = true,
        // whether to publish a javadoc jar
        publishJavadocJar = true,
    ))

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates("com.wustfly.wult", "cardviews", "1.0.0")

    pom {
        name.set("CardViews")
        description.set("一款好用的android倒角（倒圆角，二阶贝塞尔倒角，三阶贝塞尔倒角）、边框、渐变UI基础组件，他是基于原生组件（FrameLayout、LinearLayout、ConstraintLayout、TextView、ImageView）扩展了更多功能，使用这些组件之后你不必再需要写那些烦人的drawable文件;组件的属性在清单文件中能直接被渲染，所见即所得。")
        inceptionYear.set("2025")
        url.set("https://github.com/liudongwust/CardViews/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("wult")
                name.set("Wult")
                url.set("https://github.com/liudongwust/")
            }
        }
        scm {
            url.set("https://github.com/liudongwust/CardViews/")
            connection.set("scm:git:git://github.com/liudongwust/CardViews.git")
            developerConnection.set("scm:git:ssh://git@github.com/liudongwust/CardViews.git")
        }
    }
}
