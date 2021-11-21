import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.html.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.outputStream

private val logger = LoggerFactory.getLogger("Upload")


fun Routing.addUpload() {
    get("/upload") {
        call.respondHtml {

            body {
                form(action = "/ul", method = FormMethod.post, encType = FormEncType.multipartFormData) {
                    input(type = InputType.file, name = "file") {
                    }
                    input(type = InputType.submit)
                }
            }

        }
    }
    post("/ul") {
        logger.info("Starting upload handler.")

        logger.info("Recdeiving multipart data...")
        val multipart = call.receiveMultipart()
        logger.info("Done.")
        multipart.forEachPart {
            if (it is PartData.FileItem) {
                logger.info("Got FileItem ${it.originalFileName}.")
                logger.info(it.headers.entries().joinToString(", ") { it.key + " " + it.value })
                val targetFile = Files.createTempFile(Path.of("downloads"), "test", ".bin")
                logger.info("Starting copy.")

                it.streamProvider().use { partstream ->
                    targetFile.outputStream().use { os ->
                        partstream.copyTo(os, 8192)
                    }
                }
            }
            logger.info("Disposing part.")
            it.dispose()
        }
        call.respondText("OK!")
    }
}