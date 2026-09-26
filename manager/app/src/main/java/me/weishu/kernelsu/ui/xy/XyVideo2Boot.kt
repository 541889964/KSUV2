package me.weishu.kernelsu.ui.xy
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
object XyVideo2Boot {
    suspend fun extractFrames(ctx: Context, uri: Uri, fps: Int, maxW: Int, maxH: Int): List<Bitmap> = withContext(Dispatchers.IO) {
        val frames = mutableListOf<Bitmap>()
        val mmr = MediaMetadataRetriever()
        try {
            mmr.setDataSource(ctx, uri)
            val durMs = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 3000L
            val total = ((durMs / 1000.0) * fps).toInt().coerceIn(1, 300)
            val stepUs = (durMs * 1000) / total
            for (i in 0 until total) {
                val b = mmr.getFrameAtTime(i * stepUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC) ?: continue
                frames.add(scaleCrop(b, maxW, maxH))
            }
        } catch (_: Exception) {}
        mmr.release()
        frames
    }
    private fun scaleCrop(src: Bitmap, w: Int, h: Int): Bitmap {
        val sw = src.width; val sh = src.height
        val r = maxOf(w.toFloat()/sw, h.toFloat()/sh)
        val nw = (sw*r).toInt(); val nh = (sh*r).toInt()
        val s = Bitmap.createScaledBitmap(src, nw, nh, true)
        val l = (nw-w)/2; val t = (nh-h)/2
        val c = Bitmap.createBitmap(s, l, t, w, h)
        if (c != s) s.recycle()
        return c
    }
    suspend fun packBootAnim(frames: List<Bitmap>, out: File, fps: Int, loop: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val zos = ZipOutputStream(FileOutputStream(out))
            zos.setLevel(0)
            val w = frames.firstOrNull()?.width ?: 1080
            val h = frames.firstOrNull()?.height ?: 2400
            zos.putNextEntry(ZipEntry("desc.txt"))
            zos.write("$w $h $fps\np $loop 0 part0\n".toByteArray())
            zos.closeEntry()
            frames.forEachIndexed { i, b ->
                zos.putNextEntry(ZipEntry("part0/${"%04d".format(i)}.png"))
                val b2 = ByteArrayOutputStream()
                b.compress(Bitmap.CompressFormat.PNG, 100, b2)
                zos.write(b2.toByteArray()); b2.close()
                zos.closeEntry()
            }
            zos.close()
            true
        } catch (_: Exception) { false }
    }
    suspend fun packKsuModule(bootZip: File, out: File, id: String, name: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val zos = ZipOutputStream(FileOutputStream(out))
            zos.putNextEntry(ZipEntry("module.prop"))
            zos.write(("id=$id\nname=$name\nversion=v1.0\nversionCode=1\nauthor=XuanYi\ndescription=$name").toByteArray())
            zos.closeEntry()
            zos.putNextEntry(ZipEntry("customize.sh"))
            zos.write("#!/sbin/sh\nSKIPUNZIP=0\nui_print 'XuanYi 开机动画'\nset_perm_recursive \$MODPATH 0 0 0755 0644\n".toByteArray())
            zos.closeEntry()
            zos.putNextEntry(ZipEntry("system/media/bootanimation.zip"))
            bootZip.inputStream().use { it.copyTo(zos) }
            zos.closeEntry()
            zos.putNextEntry(ZipEntry("system/media/.keep")); zos.closeEntry()
            zos.close()
            true
        } catch (_: Exception) { false }
    }
    suspend fun installModule(zipPath: String): Pair<Boolean,String> = withContext(Dispatchers.IO) {
        try {
            val tmp = "/data/local/tmp/xy_boot_module.zip"
            Runtime.getRuntime().exec(arrayOf("su","-c","cp $zipPath $tmp")).waitFor()
            val p = Runtime.getRuntime().exec(arrayOf("su","-c","ksud module install $tmp"))
            p.inputStream.bufferedReader().readText()
            p.errorStream.bufferedReader().readText()
            val code = p.waitFor()
            if (code == 0) return@withContext Pair(true, "安装成功，重启生效")
            Runtime.getRuntime().exec(arrayOf("su","-c",
                "rm -rf /data/adb/ksu/modules_update/xy_boot_anim && mkdir -p /data/adb/ksu/modules_update/xy_boot_anim && cd /data/adb/ksu/modules_update/xy_boot_anim && unzip -o $tmp")).waitFor()
            Pair(true, "已复制到 modules_update，重启生效")
        } catch (e: Exception) { Pair(false, "失败: ${e.message}") }
    }
}
