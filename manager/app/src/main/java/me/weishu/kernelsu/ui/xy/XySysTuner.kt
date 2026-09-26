package me.weishu.kernelsu.ui.xy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
object XySysTuner {
    suspend fun sh(cmd:String):String = withContext(Dispatchers.IO) {
        try {
            val p = Runtime.getRuntime().exec(arrayOf("su","-c",cmd))
            val o = p.inputStream.bufferedReader().readText()
            val e = p.errorStream.bufferedReader().readText()
            p.waitFor(); (o+e).trim()
        } catch (_:Exception) { "" }
    }
    suspend fun read(p:String) = sh("cat $p 2>/dev/null")
    suspend fun write(p:String, v:String):Boolean {
        val r = sh("echo '$v' > $p 2>&1; echo \$?")
        return r.trim().endsWith("0") || r.isBlank()
    }
    suspend fun hasRoot() = sh("id").contains("uid=0")
    suspend fun cpuCount():Int = try { Runtime.getRuntime().availableProcessors() } catch(_:Exception){8}
    suspend fun cpuCurFreq(i:Int) = read("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq").toIntOrNull()?.div(1000) ?: 0
    suspend fun cpuMaxFreq(i:Int) = read("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq").toIntOrNull()?.div(1000) ?: 0
    suspend fun cpuMinFreq(i:Int) = read("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_min_freq").toIntOrNull()?.div(1000) ?: 0
    suspend fun cpuGovernor(i:Int) = read("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_governor")
    suspend fun cpuAvailableGovernors(i:Int) = read("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_available_governors").split(" ").filter{it.isNotBlank()}
    suspend fun setCpuGovernor(i:Int, g:String) = write("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_governor", g)
    suspend fun setCpuMaxFreq(i:Int, khz:Int) = write("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_max_freq", (khz*1000).toString())
    suspend fun setCpuMinFreq(i:Int, khz:Int) = write("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_min_freq", (khz*1000).toString())
    suspend fun gpuCurFreq() = (read("/sys/class/kgsl/kgsl-3d0/gpuclk").ifEmpty { read("/sys/class/kgsl/kgsl-3d0/devfreq/cur_freq") }).toIntOrNull()?.div(1_000_000) ?: 0
    suspend fun gpuMaxFreq() = read("/sys/class/kgsl/kgsl-3d0/devfreq/max_freq").toIntOrNull()?.div(1_000_000) ?: 0
    suspend fun setGpuMaxFreq(mhz:Int):Boolean {
        val hz = mhz * 1_000_000L
        write("/sys/class/kgsl/kgsl-3d0/devfreq/max_freq", hz.toString())
        write("/sys/class/kgsl/kgsl-3d0/max_gpuclk", hz.toString())
        return true
    }
    suspend fun gpuGovernor() = read("/sys/class/kgsl/kgsl-3d0/devfreq/governor")
    suspend fun setGpuGovernor(g:String) = write("/sys/class/kgsl/kgsl-3d0/devfreq/governor", g)
    suspend fun blockDevices():List<String> = withContext(Dispatchers.IO) {
        read("/proc/partitions").lines().drop(2).mapNotNull { l ->
            val p = l.trim().split(Regex("\\s+")); if (p.size>=4) p[3] else null
        }.filter { it.startsWith("sd")||it.startsWith("nvme")||it.startsWith("mmcblk")||it.startsWith("dm-") }
    }
    suspend fun ioScheduler(d:String):String {
        val s = read("/sys/block/$d/queue/scheduler")
        return Regex("\\[([^\\]]+)\\]").find(s)?.groupValues?.get(1) ?: s.split(" ").firstOrNull() ?: ""
    }
    suspend fun ioSchedulers(d:String):List<String> =
        read("/sys/block/$d/queue/scheduler").replace("[","").replace("]","").split(" ").filter{it.isNotBlank()}
    suspend fun setIoScheduler(d:String, s:String) = write("/sys/block/$d/queue/scheduler", s)
    suspend fun swappiness() = read("/proc/sys/vm/swappiness").toIntOrNull() ?: 60
    suspend fun setSwappiness(v:Int) = write("/proc/sys/vm/swappiness", v.toString())
    suspend fun dirtyRatio() = read("/proc/sys/vm/dirty_ratio").toIntOrNull() ?: 20
    suspend fun setDirtyRatio(v:Int) = write("/proc/sys/vm/dirty_ratio", v.toString())
    suspend fun dirtyBgRatio() = read("/proc/sys/vm/dirty_background_ratio").toIntOrNull() ?: 10
    suspend fun setDirtyBgRatio(v:Int) = write("/proc/sys/vm/dirty_background_ratio", v.toString())
    enum class Profile(val id:String, val label:String) { BALANCE("balance","均衡"), PERFORMANCE("performance","性能"), POWERSAVE("powersave","省电") }
    suspend fun applyProfile(p:Profile):Boolean {
        val cpus = cpuCount()
        when (p) {
            Profile.PERFORMANCE -> {
                for (i in 0 until cpus) {
                    val a = cpuAvailableGovernors(i)
                    val t = when { "performance" in a -> "performance"; "schedutil" in a -> "schedutil"; else -> a.firstOrNull() ?: continue }
                    setCpuGovernor(i, t)
                    val m = cpuMaxFreq(i); if (m>0) setCpuMinFreq(i, m*80/100)
                }
                setGpuMaxFreq(900); setGpuGovernor("performance")
                setSwappiness(10); setDirtyRatio(20); setDirtyBgRatio(10)
                for (d in blockDevices()) setIoScheduler(d, "none")
            }
            Profile.BALANCE -> {
                for (i in 0 until cpus) {
                    val a = cpuAvailableGovernors(i)
                    val t = when { "schedutil" in a -> "schedutil"; "walt" in a -> "walt"; else -> a.firstOrNull() ?: continue }
                    setCpuGovernor(i, t)
                }
                setGpuGovernor("msm-adreno-tz")
                setSwappiness(60); setDirtyRatio(20); setDirtyBgRatio(10)
                for (d in blockDevices()) setIoScheduler(d, "mq-deadline")
            }
            Profile.POWERSAVE -> {
                for (i in 0 until cpus) {
                    val a = cpuAvailableGovernors(i)
                    val t = when { "powersave" in a -> "powersave"; "schedutil" in a -> "schedutil"; else -> a.firstOrNull() ?: continue }
                    setCpuGovernor(i, t)
                    val m = cpuMaxFreq(i); if (m>0) setCpuMaxFreq(i, m*60/100)
                }
                setGpuMaxFreq(400); setGpuGovernor("powersave")
                setSwappiness(100); setDirtyRatio(15); setDirtyBgRatio(5)
                for (d in blockDevices()) setIoScheduler(d, "mq-deadline")
            }
        }
        return true
    }
}
