import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.model.LocalizedText
import com.google.api.services.androidpublisher.model.Track
import com.google.api.services.androidpublisher.model.TrackRelease
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.io.FileInputStream

open class PlayPublisherExtension {
    lateinit var serviceAccountJson: File
    lateinit var packageName: String
    var defaultTrack: String = "internal"
    var defaultUserFraction: Double = 0.1
    var defaultReleaseStatus: String = "completed"
    var artifactPath: String = "build/outputs/bundle/release/app-release.aab"
}

val extension = extensions.create("playPublisher", PlayPublisherExtension::class.java)

tasks.register("publishToPlayStore") {
    group = "publishing"
    description = "Uploads AAB to Google Play Store using Play Publisher API"

    doLast {
        val serviceAccountJson = extension.serviceAccountJson
            .takeIf { it.exists() }
            ?: throw StopExecutionException("playPublisher.serviceAccountJson не указан или файл не существует")

        val packageName = extension.packageName
            .takeIf { it.isNotBlank() }
            ?: throw StopExecutionException("playPublisher.packageName не указан")

        val targetTrack = project.findProperty("playTrack") as? String ?: extension.defaultTrack
        val userFraction = (project.findProperty("playUserFraction") as? String)?.toDoubleOrNull()
            ?: extension.defaultUserFraction
        val releaseStatus =
            project.findProperty("playReleaseStatus") as? String ?: extension.defaultReleaseStatus
        val releaseNotesText = project.findProperty("playReleaseNotes") as? String ?: ""

        val aabFile = project.file(extension.artifactPath)
        if (!aabFile.exists()) {
            throw StopExecutionException("AAB файл не найден: ${aabFile.absolutePath}. Сначала выполните :app:bundleRelease")
        }

        val credentials = GoogleCredentials.fromStream(FileInputStream(serviceAccountJson))
            .createScoped(listOf("https://www.googleapis.com/auth/androidpublisher"))
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()
        val publisher = AndroidPublisher.Builder(
            httpTransport, jsonFactory,
            HttpCredentialsAdapter(credentials)
        )
            .setApplicationName("LinguaCards Publisher")
            .build()

        println("Создание edit-сессии для $packageName...")
        val edit = publisher.edits().insert(packageName, null).execute()
        val editId = edit.id
        println("Edit ID: $editId")

        println("Загрузка ${aabFile.name} (${aabFile.length() / 1024} KB)...")
        val bundleContent = aabFile.readBytes()
        val mediaContent = ByteArrayContent("application/octet-stream", bundleContent)
        val uploadResponse = publisher.edits().bundles()
            .upload(packageName, editId, mediaContent)
            .execute()
        val versionCode = uploadResponse.versionCode
        println("Загружен versionCode = $versionCode")

        val release = TrackRelease()
            .setVersionCodes(listOf(versionCode.toLong()))
            .setStatus(releaseStatus)
            .setUserFraction(userFraction)

        if (releaseNotesText.isNotBlank()) {
            val notes = listOf(
                LocalizedText()
                    .setLanguage("en-US")
                    .setText(releaseNotesText)
            )
            release.setReleaseNotes(notes)
        }

        val trackUpdate = Track().setReleases(listOf(release))
        publisher.edits().tracks()
            .update(packageName, editId, targetTrack, trackUpdate)
            .execute()
        println("Трек '$targetTrack' обновлён (userFraction = $userFraction)")

        publisher.edits().commit(packageName, editId).execute()
        println("Релиз успешно опубликован в трек '$targetTrack'")
    }
}