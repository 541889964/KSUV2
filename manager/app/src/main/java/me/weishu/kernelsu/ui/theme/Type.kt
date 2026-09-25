package me.weishu.kernelsu.ui.theme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
val XyTypography = Typography(
    displaySmall=TextStyle(fontFamily=FontFamily.Default,fontWeight=FontWeight.W600,fontSize=32.sp),
    headlineSmall=TextStyle(fontFamily=FontFamily.Default,fontWeight=FontWeight.W600,fontSize=22.sp),
    titleLarge=TextStyle(fontFamily=FontFamily.Default,fontWeight=FontWeight.W600,fontSize=20.sp),
    titleMedium=TextStyle(fontFamily=FontFamily.Default,fontWeight=FontWeight.W500,fontSize=16.sp),
    bodyLarge=TextStyle(fontFamily=FontFamily.Default,fontWeight=FontWeight.W400,fontSize=16.sp),
    bodyMedium=TextStyle(fontFamily=FontFamily.Default,fontWeight=FontWeight.W400,fontSize=14.sp),
    bodySmall=TextStyle(fontFamily=FontFamily.Default,fontWeight=FontWeight.W400,fontSize=12.sp),
    labelLarge=TextStyle(fontFamily=FontFamily.Default,fontWeight=FontWeight.W500,fontSize=14.sp)
)
val KernelSUTypography = XyTypography
