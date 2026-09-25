package me.weishu.kernelsu.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun FreeOpenSourceDialog(onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("KernelSU 美化版 是免费开源的", style = MaterialTheme.typography.headlineSmall) },
        text = { Text("""
            • 本项目是 KernelSU 的第三方修改版（非官方分支），遵循上游的开源协议，源码公开可获取。
            • 它完全免费：没有付费版、没有会员、没有卡密、没有激活码，也不卖任何东西。
            • 所以 —— 如果你是花钱买来的，那你就是被骗了。请立刻找卖家退款、举报。
            • 致谢 KernelSU 及其作者 weishu(tiann) 和所有社区贡献者。
        """.trimIndent(), style = MaterialTheme.typography.bodyMedium) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("我知道了") } })
}
