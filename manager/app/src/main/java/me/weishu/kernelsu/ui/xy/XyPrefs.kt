package me.weishu.kernelsu.ui.xy
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
enum class XyParticle(val id:String,val label:String){
    NONE("none","关闭"), SNOW("snow","雪花"), TROLL("troll","巨魔雨");
    companion object { fun from(s:String?) = entries.find { it.id == s } ?: SNOW }
}
data class XyConfig(
    val wallpaper:Boolean=true,
    val particle:XyParticle=XyParticle.SNOW,
    val particleCount:Int=70,
    val tiltStrength:Float=1.4f,
    val backgroundDim:Float=0.2f
)
object XyPrefs {
    private const val P="xy_main_v26"
    var config by mutableStateOf(XyConfig()); private set
    fun load(c:Context){
        val s=c.getSharedPreferences(P,Context.MODE_PRIVATE)
        config=XyConfig(
            wallpaper=s.getBoolean("wallpaper",true),
            particle=XyParticle.from(s.getString("particle","snow")),
            particleCount=s.getInt("count",70),
            tiltStrength=s.getFloat("tilt",1.4f),
            backgroundDim=s.getFloat("dim",0.2f))
    }
}
