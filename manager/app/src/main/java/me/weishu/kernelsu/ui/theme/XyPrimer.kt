package me.weishu.kernelsu.ui.theme
import androidx.compose.ui.graphics.Color
object XyPrimer {
    val gray1000=Color(0xFF050505); val gray950=Color(0xFF0B0B0D)
    val gray900=Color(0xFF17181C); val gray850=Color(0xFF1F1F24)
    val gray800=Color(0xFF2E2F37); val gray750=Color(0xFF383A42)
    val gray700=Color(0xFF41434E); val gray400=Color(0xFF9194A1)
    val gray350=Color(0xFFA9ABB6); val gray300=Color(0xFFBFC1C9)
    val gray250=Color(0xFFD6D7DC); val gray200=Color(0xFFE3E4E8)
    val gray150=Color(0xFFEFF0F5); val gray100=Color(0xFFF7F7F9)
    val gray050=Color(0xFFFBFBFC); val gray000=Color(0xFFFFFFFF)
    val brand=Color(0xFF774F6F); val brandLight=Color(0xFFB088A8); val brandDark=Color(0xFF4A2F48)
    val red900=Color(0xFF8F1D22); val red700=Color(0xFFBD222D); val red500=Color(0xFFE04352)
    val red400=Color(0xFFF55363); val red100=Color(0xFFFFE0E4); val red000=Color(0xFFFFF0F2)
}
enum class XyColorSpec(val id:String,val label:String,val seed:Long){
    ROSE("rose","玫瑰",0xFF774F6F), SAKURA("sakura","樱花",0xFFE1A0B5),
    RED("red","红",0xFFE04352), PINK("pink","粉",0xFFF051B0),
    PURPLE("purple","紫",0xFF9C27B0), DEEP_PURPLE("deep_purple","深紫",0xFF673AB7),
    INDIGO("indigo","靛蓝",0xFF3F51B5), BLUE("blue","蓝",0xFF0D6EDB),
    CYAN("cyan","青",0xFF00BCD4), TEAL("teal","蓝绿",0xFF009688),
    GREEN("green","绿",0xFF32B24F), AMBER("amber","琥珀",0xFFFFC107),
    ORANGE("orange","橙",0xFFFA6F0F), BROWN("brown","棕",0xFF795548),
    BLUE_GREY("blue_grey","蓝灰",0xFF607D8B);
    val color: Color get() = Color(seed)
    companion object { fun from(s:String?) = entries.find { it.id == s } ?: ROSE }
}
