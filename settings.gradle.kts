rootProject.name = "Hyssentials"
plugins {
    id("dev.scaffoldit") version "0.2.+"
}
hytale {
    manifest {
        Group = "dev.hytalemodding"
        Name = "Hyssentials"
        Main = "dev.hytalemodding.hyssentials.HyssentialsPlugin"
        IncludesAssetPack = true
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:26.0.2-1")
        compileOnly("org.jspecify:jspecify:1.0.0")
        runtimeOnly("com.buuz135:BetterModlist:1.+")
    }
}