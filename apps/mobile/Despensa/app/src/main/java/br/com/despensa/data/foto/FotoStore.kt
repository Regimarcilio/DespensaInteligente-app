package br.com.despensa.data.foto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import br.com.despensa.domain.LIMITE_FOTO_BYTES
import br.com.despensa.domain.precisaReduzir
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Foto do item (issue #1, US02): SEM permissão CAMERA — delega ao app de câmera via
 * `ActivityResultContracts.TakePicture` + FileProvider (`cache-path pictures/`).
 * Compressão p/ WebP ≤ 200 KB + thumbnail amostrado p/ o card.
 */
object FotoStore {

    private const val DIR_TEMP = "pictures"
    private const val DIR_FINAL = "fotos"

    /** Cria o arquivo temporário no cache + Uri FileProvider p/ entregar ao TakePicture. */
    fun novaFotoTemp(contexto: Context): Pair<File, Uri> {
        val dir = File(contexto.cacheDir, DIR_TEMP).apply { mkdirs() }
        val arquivo = File(dir, "foto-${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(
            contexto,
            "${contexto.packageName}.fileprovider",
            arquivo
        )
        return arquivo to uri
    }

    /**
     * Comprime [origem] (JPEG da câmera) p/ WebP ≤ [LIMITE_FOTO_BYTES] em `filesDir/fotos/`.
     * Reduz dimensão (lado maior ≤ 1280px) e qualidade (90→20) até caber no teto.
     * Apaga o temporário. Retorna o arquivo final.
     */
    fun salvarComprimida(contexto: Context, origem: File): File {
        val destino = File(File(contexto.filesDir, DIR_FINAL).apply { mkdirs() }, "${UUID.randomUUID()}.webp")
        val formato = if (Build.VERSION.SDK_INT >= 30) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
        @Suppress("DEPRECATION")
        val formatoLegado = Bitmap.CompressFormat.WEBP
        val fmt = if (Build.VERSION.SDK_INT >= 30) formato else formatoLegado
        var ladoMaior = 1280
        var qualidade = 90
        var bitmap = decodificarAmostrado(origem.absolutePath, ladoMaior)
            ?: throw IllegalArgumentException("Foto ilegível")
        // Tenta qualidade decrescente; se não couber, reduz dimensão e repete.
        while (true) {
            FileOutputStream(destino).use { out -> bitmap.compress(fmt, qualidade, out) }
            if (!precisaReduzir(destino.length()) || (ladoMaior <= 320 && qualidade <= 20)) break
            if (qualidade > 20) {
                qualidade -= 15
            } else {
                qualidade = 90
                ladoMaior /= 2
                bitmap.recycle()
                bitmap = decodificarAmostrado(origem.absolutePath, ladoMaior)
                    ?: throw IllegalArgumentException("Foto ilegível")
            }
        }
        bitmap.recycle()
        origem.delete()
        return destino
    }

    /** Decodifica [path] amostrado p/ caber em [ladoMaior] px (thumbnail 56dp = ~168px). */
    fun decodificarAmostrado(path: String, ladoMaior: Int = 168): Bitmap? {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, limites)
        if (limites.outWidth <= 0 || limites.outHeight <= 0) return null
        var amostra = 1
        val maior = maxOf(limites.outWidth, limites.outHeight)
        while (maior / (amostra * 2) >= ladoMaior) amostra *= 2
        return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = amostra })
    }

    /** true se [path] aponta p/ um arquivo de foto legível (sobrevive a restart: filesDir). */
    fun fotoExiste(path: String?): Boolean =
        !path.isNullOrBlank() && File(path).let { it.isFile && it.canRead() }
}
