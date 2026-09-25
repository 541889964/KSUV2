package me.weishu.kernelsu.ui.xy
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
enum class XyThemeMode(val id:String,val label:String){
    AUTO("auto","跟随系统"), LIGHT("light","浅色"),
    DARK("dark","深色"), AMOLED("amoled","AMOLED");
    companion object { fun from(s:String?) = entries.find { it.id == s } ?: DARK }
}
enum class XyParticle(val id:String,val label:String){
    NONE("none","关闭"), SNOW("snow","雪花"), TROLL("troll","巨魔雨");
    companion object { fun from(s:String?) = entries.find { it.id == s } ?: NONE }
}
data class XyConfig(
    val themeMode:XyThemeMode=XyThemeMode.DARK,
    val colorSeed:Long=0xFF774F6F,
    val stealth:Boolean=false,
    val stealthCode:String="*#*#70707#*#*",
    val wallpaper:Boolean=true,
    val particle:XyParticle=XyParticle.SNOW,
    val particleCount:Int=70,
    val tiltStrength:Float=1.4f,
    val backgroundDim:Float=0.15f,
    val statusCardText:String="希望以后你能开心"
)
object XyPrefs {
    private const val P="xy_main_v7"
    var config by mutableStateOf(XyConfig()); private set
    fun load(c:Context){
        val s=c.getSharedPreferences(P,Context.MODE_PRIVATE)
        config=XyConfig(
            themeMode=XyThemeMode.from(s.getString("theme","dark")),
            colorSeed=s.getLong("seed",0xFF774F6F),
            stealth=s.getBoolean("stealth",false),
            stealthCode=s.getString("stealthCode","*#*#70707#*#*")?:"*#*#70707#*#*",
            wallpaper=s.getBoolean("wallpaper",true),
            particle=XyParticle.from(s.getString("particle","snow")),
            particleCount=s.getInt("count",70),
            tiltStrength=s.getFloat("tilt",1.4f),
            backgroundDim=s.getFloat("dim",0.15f),
            statusCardText=s.getString("cardText","希望以后你能开心")?:"希望以后你能开心")
    }
    fun update(c:Context,n:XyConfig){
        config=n
        c.getSharedPreferences(P,Context.MODE_PRIVATE).edit().apply{
            putString("theme",n.themeMode.id); putLong("seed",n.colorSeed)
            putBoolean("stealth",n.stealth); putString("stealthCode",n.stealthCode)
            putBoolean("wallpaper",n.wallpaper); putString("particle",n.particle.id)
            putInt("count",n.particleCount); putFloat("tilt",n.tiltStrength)
            putFloat("dim",n.backgroundDim); putString("cardText",n.statusCardText)
            apply()
        }
        XyStealth.setEnabled(c,n.stealth)
        if(n.stealthCode.isNotEmpty()) XyStealth.setCode(c,n.stealthCode)
    }
}
